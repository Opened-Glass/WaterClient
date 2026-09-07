package com.water.gui.hud;

import com.water.module.modules.client.SpotifyHud;
import com.water.module.modules.client.WaterPlus;
import com.water.utils.renderer.GuiRenderer;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public final class SpotifyQueueHud {
   public static int posX = 10;
   public static int posY = 200;
   public static boolean visible = true;
   private static boolean dragging = false;
   private static double dragOffX = 0.0;
   private static double dragOffY = 0.0;
   private static final int W = 220;
   private static final int HEAD_H = 18;
   private static final int ROW_H = 13;
   private static final int MAX_ROWS = 12;
   private static final int PAD = 5;
   private static final int MAX_KNOWN = 300;
   private static final List<String[]> ALL_KNOWN = new ArrayList<>();
   private static volatile String currentTitle = "";
   private static volatile String currentArtist = "";
   private static volatile int currentIdx = 0;
   private static int scroll = 0;
   private static final ExecutorService exec = Executors.newSingleThreadExecutor(r -> {
      Runnable var1 = new Thread(r, "spotify-queue");
      var1.setDaemon(true);
      return var1;
   });
   private static final AtomicBoolean historyLoaded = new AtomicBoolean(false);
   private static volatile long lastClickMs = 0L;
   private static final long CLICK_DEBOUNCE_MS = 500L;

   private static void ensureLoaded() {
      if (historyLoaded.compareAndSet(false, true)) {
         loadFromDisk();
      }
   }

   public static void onSongChanged(String artist, String title) {
      ensureLoaded();
      if (title != null && !title.isEmpty()) {
         if (!title.equals(currentTitle) || !artist.equals(currentArtist)) {
            currentTitle = title;
            currentArtist = artist;
            synchronized (ALL_KNOWN) {
               boolean var3 = false;

               for (int var4 = 0; var4 < ALL_KNOWN.size(); var4++) {
                  if (ALL_KNOWN.get(var4)[1].equals(title) && ALL_KNOWN.get(var4)[0].equals(artist)) {
                     currentIdx = var4;
                     var3 = true;
                     break;
                  }
               }

               if (!var3) {
                  ALL_KNOWN.add(new String[]{artist, title});
                  currentIdx = ALL_KNOWN.size() - 1;
                  if (ALL_KNOWN.size() > 300) {
                     int var7 = ALL_KNOWN.size() - 300;

                     for (String var6 = 0; var6 < var7; var6++) {
                        ALL_KNOWN.remove(0);
                     }

                     currentIdx = Math.max(0, currentIdx - var7);
                     scroll = Math.max(0, scroll - var7);
                  }

                  exec.submit(SpotifyQueueHud::saveToDisk);
               }
            }

            scrollToCurrent();
         }
      }
   }

   private static void scrollToCurrent() {
      if (currentIdx < scroll) {
         scroll = Math.max(0, currentIdx);
      } else if (currentIdx >= scroll + 12) {
         scroll = currentIdx - 12 + 1;
      }
   }

   private static int maxScroll() {
      synchronized (ALL_KNOWN) {
         return Math.max(0, ALL_KNOWN.size() - 12);
      }
   }

   public static void render(DrawContext ctx, double mx, double my) {
      if (visible) {
         ensureLoaded();
         MinecraftClient var5 = MinecraftClient.getInstance();
         if (var5 != null) {
            TextRenderer var17 = var5.textRenderer;
            int var6 = WaterPlus.getAccentARGB();
            var6 &= 16777215;
            ArrayList var7;
            synchronized (ALL_KNOWN) {
               var7 = new ArrayList<>(ALL_KNOWN);
            }

            int var19 = Math.max(1, Math.min(12, var7.size()));
            int var9 = 18 + var19 * 13 + 2;
            GuiRenderer.a(ctx, (float)posX, (float)posY, 220.0F, (float)var9, 8.0F, -871556572, false);
            GuiRenderer.a(ctx, (float)posX, (float)posY, 220.0F, (float)var9, 8.0F, 1.0F, 1073741824 | var6, false);
            ctx.fill(posX + 1, posY + 1, posX + 220 - 1, posY + 2, 369098751);
            ctx.fill(posX, posY, posX + 220, posY + 18, 587202559);
            ctx.fill(posX, posY + 3, posX + 3, posY + 18 - 3, 0xFF000000 | var6);
            String var20 = "QUEUE  " + (var7.isEmpty() ? "play a song" : var7.size() + " songs");
            ctx.drawText(var17, var20, posX + 5 + 5, posY + 9 - 4, -1117449, false);
            if (var7.isEmpty()) {
               ctx.drawText(var17, "Play a song to start", posX + 5, posY + 18 + 4, 1442840575, false);
            } else {
               var9 = Math.min(scroll + 12, var7.size());

               for (int var10 = scroll; var10 < var9; var10++) {
                  String[] var11 = (String[])var7.get(var10);
                  int var12 = posY + 18 + (var10 - scroll) * 13;
                  boolean var13 = var11[1].equals(currentTitle) && var11[0].equals(currentArtist);
                  int var14 = mx >= posX && mx < posX + 220 && my >= var12 && my < var12 + 13;
                  boolean var15 = var10 < currentIdx;
                  if (var13) {
                     ctx.fill(posX, var12, posX + 220, var12 + 13, 620756992 | var6);
                     ctx.fill(posX, var12, posX + 3, var12 + 13, 0xFF000000 | var6);
                  } else if (var14) {
                     ctx.fill(posX, var12, posX + 220, var12 + 13, 369098751);
                  }

                  var14 = var13 ? 0xFF000000 | var6 : (var15 ? -10456960 : -5588020);
                  String var23 = (var11[0].isEmpty() ? "" : var11[0] + " - ") + var11[1];
                  String var24 = var17.trimToWidth(var23, 210 - (var13 ? 6 : 2));
                  ctx.drawText(var17, var24, posX + 5 + (var13 ? 5 : 2), var12 + 6 - 4, var14, false);
               }

               if (var7.size() > 12) {
                  int var22 = posX + 220 - 3;
                  int var25 = posY + 18;
                  int var26 = var19 * 13;
                  float var27 = Math.max(16.0F, var26 * (12.0F / var7.size()));
                  float var29 = var25 + (var26 - var27) * ((float)scroll / Math.max(1, maxScroll()));
                  ctx.fill(var22, var25, var22 + 3, var25 + var26, 369098751);
                  ctx.fill(var22, (int)var29, var22 + 3, (int)(var29 + var27), -2147483648 | var6);
               }
            }
         }
      }
   }

   public static boolean onMouseClick(double mx, double my, int button) {
      if (!visible) {
         return false;
      } else {
         ArrayList var5;
         synchronized (ALL_KNOWN) {
            var5 = new ArrayList<>(ALL_KNOWN);
         }

         int var10 = Math.max(1, Math.min(12, var5.size()));
         int var7 = 18 + var10 * 13 + 2;
         if (mx < posX || mx > posX + 220 || my < posY || my > posY + var7) {
            return false;
         } else if (my < posY + 18) {
            if (button == 0) {
               dragging = true;
               dragOffX = mx - posX;
               dragOffY = my - posY;
            }

            return true;
         } else {
            if (button == 0 && my < posY + 18 + var10 * 13) {
               double var9 = ((int)my - posY - 18) / 13 + scroll;
               if (var9 >= 0 && var9 < var5.size()) {
                  skipTo(((String[])var5.get(var9))[0], ((String[])var5.get(var9))[1], var9);
                  return true;
               }
            }

            return true;
         }
      }
   }

   public static boolean onMouseDrag(double mx, double my) {
      if (!dragging) {
         return false;
      } else {
         posX = (int)(mx - dragOffX);
         posY = (int)(my - dragOffY);
         double var4 = MinecraftClient.getInstance();
         if (var4 != null) {
            posX = Math.max(0, Math.min(var4.getWindow().getScaledWidth() - 220, posX));
            posY = Math.max(0, Math.min(var4.getWindow().getScaledHeight() - 50, posY));
         }

         return true;
      }
   }

   public static void onMouseRelease() {
      dragging = false;
   }

   public static boolean isDragging() {
      return dragging;
   }

   public static boolean onScroll(double mx, double my, double v) {
      if (!visible) {
         return false;
      } else {
         ArrayList var6;
         synchronized (ALL_KNOWN) {
            var6 = new ArrayList<>(ALL_KNOWN);
         }

         int var10 = Math.max(1, Math.min(12, var6.size()));
         int var9 = 18 + var10 * 13 + 2;
         if (!(mx < posX) && !(mx > posX + 220) && !(my < posY) && !(my > posY + var9)) {
            scroll = Math.max(0, Math.min(maxScroll(), scroll + (v > 0.0 ? -1 : 1)));
            return true;
         } else {
            return false;
         }
      }
   }

   private static void skipTo(String artist, String title, int targetIdx) {
      if (!title.equals(currentTitle) || !artist.equals(currentArtist)) {
         long var3 = System.currentTimeMillis();
         if (var3 - lastClickMs >= 500L) {
            lastClickMs = var3;
            String var5 = targetIdx - currentIdx;
            if (var5 != 0) {
               SpotifyHud.m(var5 > 0 ? "next" : "prev");
            }
         }
      }
   }

   private static Path queueFile() {
      MinecraftClient var0 = MinecraftClient.getInstance();
      return var0 != null && var0.runDirectory != null ? var0.runDirectory.toPath().resolve("water_spotify_history.txt") : null;
   }

   private static void loadFromDisk() {
      try {
         Path var0 = queueFile();
         if (var0 == null || !Files.isRegularFile(var0)) {
            return;
         }

         List var7 = Files.readAllLines(var0, StandardCharsets.UTF_8);
         synchronized (ALL_KNOWN) {
            ALL_KNOWN.clear();

            for (String var2 : var7) {
               if (var2 != null && !var2.isBlank()) {
                  int var3 = var2.indexOf(9);
                  if (var3 >= 0) {
                     String var4 = var2.substring(0, var3);
                     var2 = var2.substring(var3 + 1);
                     if (!var2.isEmpty()) {
                        ALL_KNOWN.add(new String[]{var4, var2});
                     }
                  }
               }
            }

            if (ALL_KNOWN.size() > 300) {
               int var9 = ALL_KNOWN.size() - 300;

               for (int var11 = 0; var11 < var9; var11++) {
                  ALL_KNOWN.remove(0);
               }
            }
         }
      } catch (Exception var6) {
      }
   }

   private static void saveToDisk() {
      try {
         Path var0 = queueFile();
         if (var0 == null) {
            return;
         }

         ArrayList var1;
         synchronized (ALL_KNOWN) {
            var1 = new ArrayList<>(ALL_KNOWN);
         }

         Files.createDirectories(var0.getParent());

         try (BufferedWriter var11 = Files.newBufferedWriter(var0, StandardCharsets.UTF_8)) {
            for (String[] var9 : var1) {
               String var3 = clean(var9[0]);
               String var10 = clean(var9[1]);
               var11.write(var3);
               var11.write(9);
               var11.write(var10);
               var11.newLine();
            }
         }
      } catch (IOException var7) {
      }
   }

   private static String clean(String s) {
      return s == null ? "" : s.replace('\t', ' ').replace('\n', ' ').replace('\r', ' ');
   }
}
