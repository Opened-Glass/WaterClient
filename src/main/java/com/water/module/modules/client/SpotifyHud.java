package com.water.module.modules.client;

import com.water.gui.ClickGuiScreen;
import com.water.gui.hud.SpotifyQueueHud;
import com.water.module.Category;
import com.water.module.Module;
import com.water.setting.Setting;
import com.water.utils.renderer.Blur2DRenderer;
import com.water.utils.renderer.GuiRenderer;
import com.water.utils.renderer.WaterFontRenderer;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

public final class SpotifyHud extends Module {
   private static SpotifyHud field_a_1;
   private final Setting<Float> aa = new Setting<>("Scale", 1.0F, 0.6F, 2.0F);
   private volatile String field_f_1 = "";
   private volatile String field_g_1 = "";
   private volatile boolean field_h_1 = false;
   private volatile boolean field_i_1 = false;
   private volatile long e = 0L;
   private volatile long field_f_2 = 0L;
   private volatile long field_g_2 = 0L;
   private ScheduledExecutorService field_a_2;
   private ExecutorService field_a_3;
   private File field_a_4;
   private File field_b_1;
   private File c;
   private static final Identifier field_a_5 = Identifier.of("water", "spotify_art");
   private static final Identifier field_b_2 = Identifier.of("water", "textures/gui/spotify_logo.png");
   private NativeImageBackedTexture field_a_6 = null;
   private volatile long field_h_2 = 0L;
   private volatile boolean j = false;
   private float field_a_7 = 0.0F;
   private long field_i_2 = 0L;
   private volatile boolean k = false;
   private final AtomicBoolean field_a_8 = new AtomicBoolean(false);
   private final AtomicInteger field_a_9 = new AtomicInteger(0);
   private volatile float field_b_3 = 0.0F;
   private volatile String field_h_3 = null;

   public SpotifyHud() {
      super("Spotify HUD", Category.e);
      this.addSetting(this.aa);
      field_a_1 = this;
   }

   public static boolean isActive() {
      return field_a_1 != null && field_a_1.isEnabled();
   }

   public static float method_b_1() {
      return field_a_1 == null ? 1.0F : field_a_1.aa.getValue();
   }

   public static void method_a_1(float v) {
      if (field_a_1 != null) {
         float var1 = field_a_1.aa.getMin() instanceof Float var5 ? var5 : 0.6F;
         float var7 = field_a_1.aa.getMax() instanceof Float var6 ? var6 : 2.0F;
         field_a_1.aa.setValue(Math.max(var1, Math.min(var7, v)));
         int[] var8 = Hud.method_a_1(Hud.a.h);
         int var9 = c(var8[0]);
         float var4 = d(var8[1]);
         if (var9 != var8[0] || var4 != var8[1]) {
            Hud.a(Hud.a.h, var9, var4);
         }
      }
   }

   public static int a() {
      return Math.round(270.0F * method_b_1());
   }

   public static int method_b_2() {
      return Math.round(60.0F * method_b_1());
   }

   private static int b(int base) {
      return Math.round(base * method_b_1());
   }

   private static float method_a_2(float base) {
      return base * method_b_1();
   }

   private static int c(int x) {
      MinecraftClient var1 = MinecraftClient.getInstance();
      return var1 == null ? x : Math.max(0, Math.min(x, var1.getWindow().getScaledWidth() - a()));
   }

   private static int d(int y) {
      MinecraftClient var1 = MinecraftClient.getInstance();
      return var1 == null ? y : Math.max(0, Math.min(y, var1.getWindow().getScaledHeight() - method_b_2()));
   }

   private static int c() {
      return c(Hud.method_a_1(Hud.a.h)[0]);
   }

   private static int method_d_1() {
      return d(Hud.method_a_1(Hud.a.h)[1]);
   }

   @Override
   public void onEnable() {
      this.n();
   }

   @Override
   public void onTick() {
      if (this.field_a_2 == null || this.field_a_2.isShutdown()) {
         this.n();
      }
   }

   private void n() {
      this.field_a_7 = 0.0F;
      this.field_i_2 = 0L;
      this.k = false;
      this.field_a_9.set(0);
      this.o();
      this.c = new File(System.getenv("TEMP"), "water_spotify_art.png");
      Thread var1 = new Thread(this::p, "water-spotify-initial");
      var1.setDaemon(true);
      var1.start();
      this.field_a_2 = Executors.newSingleThreadScheduledExecutor(r -> {
         Runnable var1x = new Thread(r, "water-spotify");
         var1x.setDaemon(true);
         return var1x;
      });
      this.field_a_3 = Executors.newSingleThreadExecutor(r -> {
         Runnable var1x = new Thread(r, "water-spotify-ctrl");
         var1x.setDaemon(true);
         return var1x;
      });
      this.field_a_2.scheduleAtFixedRate(() -> {
         int var1x = this.field_a_9.getAndIncrement();
         if (var1x < 10) {
            this.p();
            if (!this.field_f_1.isEmpty()) {
               this.field_a_9.set(Integer.MAX_VALUE);
            }
         }
      }, 1000L, 800L, TimeUnit.MILLISECONDS);
      this.field_a_2.scheduleAtFixedRate(this::p, 2L, 2L, TimeUnit.SECONDS);
   }

   @Override
   public void onDisable() {
      if (this.field_a_2 != null) {
         this.field_a_2.shutdownNow();
      }

      if (this.field_a_3 != null) {
         this.field_a_3.shutdownNow();
      }

      this.field_f_1 = "";
      this.field_g_1 = "";
      this.field_h_1 = false;
      this.e = 0L;
      this.field_f_2 = 0L;
      this.field_g_2 = 0L;
      this.field_a_7 = 0.0F;
      this.j = false;
      this.field_h_3 = null;
      if (this.field_a_6 != null) {
         try {
            MinecraftClient.getInstance().getTextureManager().destroyTexture(field_a_5);
         } catch (Exception var1) {
         }

         this.field_a_6 = null;
      }
   }

   private void o() {
      try {
         this.field_a_4 = File.createTempFile("water_smtc_poll_", ".ps1");
         this.field_a_4.deleteOnExit();

         try (FileWriter var1 = new FileWriter(this.field_a_4, StandardCharsets.UTF_8)) {
            var1.write(
               "[void][System.Reflection.Assembly]::LoadFile('C:\\Windows\\Microsoft.NET\\Framework64\\v4.0.30319\\System.Runtime.WindowsRuntime.dll')\r\n$null = [Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager,Windows.Media.Control,ContentType=WindowsRuntime]\r\n$g = ([System.WindowsRuntimeSystemExtensions].GetMethods() | Where-Object { $_.Name -eq 'AsTask' -and $_.GetParameters().Count -eq 1 -and $_.GetParameters()[0].ParameterType.Name -like 'IAsyncOperation*' })[0]\r\nfunction Aw($op,$t){$m=$g.MakeGenericMethod($t);$task=$m.Invoke($null,@($op));$task.GetAwaiter().GetResult()}\r\n$asStreamForRead = [System.IO.WindowsRuntimeStreamExtensions].GetMethods() | Where-Object { $_.Name -eq 'AsStreamForRead' -and $_.GetParameters().Count -eq 1 } | Select-Object -First 1\r\ntry {\r\n  $mgr = Aw([Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager]::RequestAsync()) ([Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager])\r\n  $s = $mgr.GetCurrentSession()\r\n  if ($s) {\r\n    $p = Aw($s.TryGetMediaPropertiesAsync()) ([Windows.Media.Control.GlobalSystemMediaTransportControlsSessionMediaProperties])\r\n    $tl = $s.GetTimelineProperties()\r\n    $pb = $s.GetPlaybackInfo()\r\n    if ($p.Title) {\r\n      if ($p.Thumbnail -and $asStreamForRead) {\r\n        try {\r\n          $stream = Aw($p.Thumbnail.OpenReadAsync()) ([Windows.Storage.Streams.IRandomAccessStreamWithContentType])\r\n          $netStream = $asStreamForRead.Invoke($null, @($stream))\r\n          $outPath = Join-Path $env:TEMP 'water_spotify_art.png'\r\n          $fs = [System.IO.File]::Create($outPath)\r\n          $netStream.CopyTo($fs)\r\n          $fs.Close()\r\n          $netStream.Close()\r\n        } catch {}\r\n      }\r\n      Write-Output ($p.Artist + '|||' + $p.Title + '|||' + [long]$tl.Position.TotalMilliseconds + '|||' + [long]$tl.EndTime.TotalMilliseconds + '|||' + ($pb.PlaybackStatus.ToString() -eq 'Playing'))\r\n    }\r\n  }\r\n} catch {}\r\n"
            );
         }

         this.field_b_1 = File.createTempFile("water_smtc_ctrl_", ".ps1");
         this.field_b_1.deleteOnExit();

         try (FileWriter var8 = new FileWriter(this.field_b_1, StandardCharsets.UTF_8)) {
            var8.write(
               "param([string]$action)\r\n[void][System.Reflection.Assembly]::LoadFile('C:\\Windows\\Microsoft.NET\\Framework64\\v4.0.30319\\System.Runtime.WindowsRuntime.dll')\r\n$null = [Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager,Windows.Media.Control,ContentType=WindowsRuntime]\r\n$g = ([System.WindowsRuntimeSystemExtensions].GetMethods() | Where-Object { $_.Name -eq 'AsTask' -and $_.GetParameters().Count -eq 1 -and $_.GetParameters()[0].ParameterType.Name -like 'IAsyncOperation*' })[0]\r\nfunction Aw($op,$t){$m=$g.MakeGenericMethod($t);$task=$m.Invoke($null,@($op));$task.GetAwaiter().GetResult()}\r\ntry {\r\n  $mgr = Aw([Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager]::RequestAsync()) ([Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager])\r\n  $s = $mgr.GetCurrentSession()\r\n  if ($s) {\r\n    switch ($action) {\r\n      'next'    { Aw($s.TrySkipNextAsync()) ([bool]) | Out-Null }\r\n      'prev'    { Aw($s.TrySkipPreviousAsync()) ([bool]) | Out-Null }\r\n      'toggle'  { Aw($s.TryTogglePlayPauseAsync()) ([bool]) | Out-Null }\r\n      'repeat'  { try { $pb2 = $s.GetPlaybackInfo(); $cur = $pb2.AutoRepeatMode; $next = if($cur -eq [Windows.Media.MediaPlaybackAutoRepeatMode]::None){'Track'} elseif($cur -eq [Windows.Media.MediaPlaybackAutoRepeatMode]::Track){'List'} else{'None'}; Aw($s.TryChangeAutoRepeatModeAsync([Windows.Media.MediaPlaybackAutoRepeatMode]::$next)) ([bool]) | Out-Null } catch {} }\r\n    }\r\n  }\r\n} catch {}\r\n"
            );
         }
      } catch (Exception var7) {
         this.field_a_4 = null;
         this.field_b_1 = null;
      }
   }

   private void p() {
      if (this.field_a_4 == null || !this.field_a_4.exists()) {
         this.o();
      }

      if (this.field_a_4 == null) {
         this.k = true;
      } else if (this.field_a_8.compareAndSet(false, true)) {
         try {
            long var1 = System.currentTimeMillis();
            Process var3 = new ProcessBuilder(
                  "powershell", "-NoProfile", "-NonInteractive", "-ExecutionPolicy", "Bypass", "-File", this.field_a_4.getAbsolutePath()
               )
               .redirectErrorStream(true)
               .start();
            boolean var4 = var3.waitFor(5L, TimeUnit.SECONDS);
            if (!var4) {
               var3.destroyForcibly();
            }

            String var33 = null;
            BufferedReader var6 = new BufferedReader(new InputStreamReader(var3.getInputStream(), StandardCharsets.UTF_8));

            String var7;
            try {
               while ((var7 = var6.readLine()) != null) {
                  var7 = var7.trim();
                  if (var7.contains("|||")) {
                     var33 = var7;
                     break;
                  }
               }
            } catch (Throwable var27) {
               try {
                  var6.close();
               } catch (Throwable var26) {
                  var27.addSuppressed(var26);
               }

               throw var27;
            }

            var6.close();
            long var35 = System.currentTimeMillis();
            long var8 = (var1 + var35) / 2L;
            if (var33 == null) {
               return;
            }

            String[] var30 = var33.split("\\|\\|\\|", -1);
            if (var30.length < 2 || var30[1].trim().isEmpty()) {
               return;
            }

            String var2 = var30[1].trim();
            String var32 = var30[0].trim();
            long var13 = var30.length > 2 ? method_a_1(var30[2]) : 0L;
            long var15 = var30.length > 3 ? method_a_1(var30[3]) : 0L;
            boolean var31 = var30.length > 4 && var30[4].trim().equalsIgnoreCase("True");
            var4 = !var2.equals(this.field_f_1);
            boolean var5 = var31 != this.field_h_1;
            long var20 = this.field_h_1 ? this.e + Math.max(0L, var8 - this.field_f_2) : this.e;
            boolean var36 = Math.abs(var13 - var20) > 4000L;
            this.field_f_1 = var2;
            this.field_g_1 = var32;
            this.field_g_2 = var15;
            this.field_h_1 = var31;
            if (var4 || var5 || var36 || this.field_f_2 == 0L) {
               this.e = var13;
               this.field_f_2 = var8;
            }

            if (var4) {
               SpotifyQueueHud.onSongChanged(var32, var2);
            }
         } catch (Exception var28) {
            return;
         } finally {
            this.k = true;
            this.field_a_8.set(false);
         }
      }
   }

   private static long method_a_1(String s) {
      try {
         return Long.parseLong(s.trim());
      } catch (Exception var1) {
         return 0L;
      }
   }

   private static float method_a_2(String t) {
      return t == null ? 0.0F : t.hashCode() % 1000 / 1000.0F;
   }

   public static void m(String action) {
      if (field_a_1 != null && field_a_1.field_a_3 != null && field_a_1.field_b_1 != null) {
         long var1 = System.currentTimeMillis();
         if ("repeat".equals(action)) {
            field_a_1.field_i_1 = !field_a_1.field_i_1;
         }

         if ("toggle".equals(action)) {
            if (field_a_1.field_h_1) {
               field_a_1.e = field_a_1.e + Math.max(0L, var1 - field_a_1.field_f_2);
               field_a_1.field_f_2 = var1;
               field_a_1.field_h_1 = false;
            } else {
               field_a_1.field_f_2 = var1;
               field_a_1.field_h_1 = true;
            }
         }

         field_a_1.field_a_3
            .submit(
               () -> {
                  try {
                     String var2 = new ProcessBuilder(
                           "powershell",
                           "-NoProfile",
                           "-NonInteractive",
                           "-ExecutionPolicy",
                           "Bypass",
                           "-File",
                           field_a_1.field_b_1.getAbsolutePath(),
                           "-action",
                           action
                        )
                        .redirectErrorStream(true)
                        .start();
                     var2.waitFor(5L, TimeUnit.SECONDS);
                     if (field_a_1.field_a_2 != null && !field_a_1.field_a_2.isShutdown()) {
                        field_a_1.field_a_2.submit(field_a_1::p);
                     }
                  } catch (Exception var1x) {
                  }
               }
            );
      }
   }

   private void q() {
      if (this.c != null && this.c.exists() && this.c.length() >= 200L) {
         long var1 = this.c.lastModified();
         if (var1 != this.field_h_2 || this.field_a_6 == null) {
            try {
               byte[] var3 = Files.readAllBytes(this.c.toPath());

               try (ByteArrayInputStream var9 = new ByteArrayInputStream(var3)) {
                  NativeImage var4 = NativeImage.read(var9);
                  if (this.field_a_6 != null) {
                     try {
                        MinecraftClient.getInstance().getTextureManager().destroyTexture(field_a_5);
                     } catch (Exception var6) {
                     }
                  }

                  this.field_a_6 = new NativeImageBackedTexture(() -> "spotify_art", var4);
                  MinecraftClient.getInstance().getTextureManager().registerTexture(field_a_5, this.field_a_6);
                  this.field_h_2 = var1;
                  this.j = true;
               }
            } catch (Exception var8) {
            }
         }
      }
   }

   public static void a(DrawContext ctx) {
      if (field_a_1 != null && field_a_1.isEnabled()) {
         MinecraftClient var1 = MinecraftClient.getInstance();
         if (var1 != null && var1.player != null) {
            if (!(var1.currentScreen instanceof ClickGuiScreen)) {
               if (!var1.getDebugHud().shouldShowDebugHud()) {
                  long var2 = System.nanoTime();
                  float var31 = field_a_1.field_i_2 == 0L ? 0.016F : Math.min(0.1F, (float)(var2 - field_a_1.field_i_2) / 1.0E9F);
                  field_a_1.field_i_2 = var2;
                  field_a_1.field_a_7 = field_a_1.field_a_7 + (1.0F - field_a_1.field_a_7) * (1.0F - (float)Math.exp(-12.0F * var31));
                  float var32 = field_a_1.field_a_7;
                  if (!(var32 < 0.01F)) {
                     field_a_1.q();
                     if (!field_a_1.k || !field_a_1.field_f_1.isEmpty()) {
                        if (!Objects.equals(field_a_1.field_h_3, field_a_1.field_f_1)) {
                           field_a_1.field_b_3 = method_a_2(field_a_1.field_f_1);
                           field_a_1.field_h_3 = field_a_1.field_f_1;
                        }

                        int var33 = a();
                        int var3 = method_b_2();
                        int var4 = b(52);
                        int var5 = b(10);
                        int var6 = c();
                        int var7 = method_d_1();
                        WaterFontRenderer var8 = WaterFontRenderer.INSTANCE;
                        int var9 = WaterPlus.getAccentARGB();
                        float var10 = WaterPlus.getEffectiveRoundness();
                        float var11 = WaterPlus.getGlassIntensity();
                        float var12 = WaterPlus.getAccentGlow();
                        int var13 = multiplyAlpha(WaterPlus.getBackgroundARGB(), var32);

                        for (int var14 = 4; var14 >= 1; var14--) {
                           GuiRenderer.a(
                              ctx,
                              (float)(var6 - var14),
                              (float)(var7 - var14 + 2),
                              (float)(var33 + var14 * 2),
                              (float)(var3 + var14 * 2),
                              var10 + var14,
                              (int)((10 - var14 * 2) * var32) << 24,
                              false
                           );
                        }

                        if (var12 > 0.01F) {
                           GuiRenderer.a(
                              ctx,
                              (float)(var6 - 3),
                              (float)(var7 - 3),
                              (float)(var33 + 6),
                              (float)(var3 + 6),
                              var10 + 3.0F,
                              col(var9 & 16777215, (int)(22.0F * var12 * var32)),
                              false
                           );
                        }

                        if (WaterPlus.menuBlurEnabled()) {
                           Blur2DRenderer.bm();
                           GuiRenderer.a(ctx, (float)var6, (float)var7, (float)var33, (float)var3, var10, 1.0F, false);
                        }

                        GuiRenderer.a(ctx, (float)var6, (float)var7, (float)var33, (float)var3, var10, var13, false);
                        GuiRenderer.a(
                           ctx,
                           (float)(var6 + 1),
                           (float)(var7 + 1),
                           (float)(var33 - 2),
                           (float)b(18),
                           var10,
                           col(16777215, (int)(16.0F * var11 * var32)),
                           false
                        );
                        GuiRenderer.a(
                           ctx,
                           (float)var6,
                           (float)(var7 + var3 - b(14)),
                           (float)var33,
                           (float)b(14),
                           var10,
                           col(var9 & 16777215, (int)(8.0F * var11 * var32)),
                           false
                        );
                        GuiRenderer.a(
                           ctx, (float)var6, (float)var7, (float)var33, (float)var3, var10, 1.0F, col(16777215, (int)((22.0F + 30.0F * var11) * var32)), false
                        );
                        GuiRenderer.a(
                           ctx,
                           (float)(var6 + 1),
                           (float)(var7 + 1),
                           (float)(var33 - 2),
                           (float)(var3 - 2),
                           Math.max(0.0F, var10 - 1.0F),
                           0.5F,
                           col(0, (int)(28.0F * var32)),
                           false
                        );
                        int var53 = b(16);
                        int var44 = var6 + var33 - var53 - b(5);
                        int var46 = var7 + b(4);
                        a(ctx, var44, var46, var53, var32);
                        int var45 = var6 + var5;
                        int var47 = var7 + (var3 - var4) / 2;
                        if (field_a_1.j) {
                           int var48 = Math.max(0, Math.min(255, (int)(255.0F * var32))) << 24 | 16777215;
                           GuiRenderer.a(ctx, (float)var45, (float)var47, (float)var4, field_a_5, var48, method_a_2(4.0F), false);
                           GuiRenderer.a(
                              ctx, (float)var45, (float)var47, (float)var4, (float)var4, method_a_2(4.0F), 1.0F, col(16777215, (int)(55.0F * var32)), false
                           );
                           GuiRenderer.a(
                              ctx,
                              (float)(var45 - 1),
                              (float)(var47 - 1),
                              (float)(var4 + 2),
                              (float)(var4 + 2),
                              method_a_2(5.0F),
                              0.5F,
                              col(var9 & 16777215, (int)(35.0F * var32)),
                              false
                           );
                        } else {
                           GuiRenderer.a(ctx, (float)var45, (float)var47, (float)var4, (float)var4, method_a_2(4.0F), col(16777215, (int)(8.0F * var32)), false);
                           GuiRenderer.a(
                              ctx, (float)var45, (float)var47, (float)var4, (float)var4, method_a_2(4.0F), 1.0F, col(16777215, (int)(35.0F * var32)), false
                           );
                           String var49 = "♫";
                           var13 = var8.method_a_2(var49);
                           var8.a(ctx, var49, var45 + (var4 - var13) / 2, var47 + (var4 - 9) / 2, col(var9 & 16777215, (int)(180.0F * var32)));
                        }

                        int var50 = var45 + var4 + var5;
                        var13 = var33 - (var50 - var6) - var5 - b(4);
                        long var25 = System.currentTimeMillis();
                        long var27;
                        if (field_a_1.field_h_1 && field_a_1.field_f_2 > 0L) {
                           long var29 = Math.max(0L, var25 - field_a_1.field_f_2);
                           var27 = field_a_1.e + var29;
                           if (field_a_1.field_g_2 > 0L) {
                              var27 = Math.min(field_a_1.field_g_2, var27);
                           }
                        } else {
                           var27 = field_a_1.e;
                        }

                        float var54 = field_a_1.field_g_2 > 0L ? Math.min(1.0F, (float)var27 / (float)field_a_1.field_g_2) : 0.0F;
                        int var30 = b(3);
                        int var34 = b(9) + b(3) + b(8) + b(4) + var30 + b(4);
                        int var35 = var7 + (var3 - var34) / 2 + b(5);
                        String var39 = !field_a_1.k ? "" : (field_a_1.field_f_1.isEmpty() ? "No track playing" : field_a_1.field_f_1);
                        var8.a(ctx, a(var39, var13, var8), var50, var35, col(16777215, (int)(255.0F * var32)));
                        int var36 = var35 + b(11);
                        var8.a(ctx, a(field_a_1.field_g_1, var13, var8), var50, var36, col(11184810, (int)(200.0F * var32)));
                        int var37 = var36 + b(11);
                        var4 = Math.max(0, Math.round(var13 * var54));
                        GuiRenderer.a(ctx, (float)var50, (float)var37, (float)var13, (float)var30, var30 / 2.0F, col(16777215, (int)(28.0F * var32)), false);
                        if (var4 > 0) {
                           GuiRenderer.a(
                              ctx, (float)var50, (float)var37, (float)var4, (float)var30, var30 / 2.0F, col(var9 & 16777215, (int)(230.0F * var32)), false
                           );
                        }

                        if (var4 > 0) {
                           var5 = b(5);
                           GuiRenderer.a(
                              ctx,
                              (float)(var50 + var4 - var5 / 2),
                              (float)(var37 - (var5 - var30) / 2),
                              (float)var5,
                              (float)var5,
                              var5 / 2.0F,
                              col(16777215, (int)(235.0F * var32)),
                              false
                           );
                        }

                        String var43 = a(var27);
                        String var41 = field_a_1.field_g_2 > 0L ? a(field_a_1.field_g_2) : "--:--";
                        int var38 = var37 + var30 + b(2);
                        var8.a(ctx, var43, var50, var38, col(8947848, (int)(155.0F * var32)));
                        var8.a(ctx, var41, var50 + var13 - var8.method_a_2(var41), var38, col(8947848, (int)(155.0F * var32)));
                     }
                  }
               }
            }
         }
      }
   }

   private static void a(DrawContext ctx, int x, int y, int size, float alpha) {
      if (size >= 0) {
         int var19 = Math.max(0, Math.min(255, (int)(alpha * 255.0F))) << 24 | 16777215;
         GuiRenderer.a(ctx, (float)x, (float)y, (float)size, field_b_2, var19, 0.0F, false);
      } else {
         int var5 = (int)(alpha * 255.0F);
         GuiRenderer.a(ctx, (float)x, (float)y, (float)size, (float)size, size / 2.0F, col(1947988, var5), false);
         GuiRenderer.a(ctx, (float)x, (float)y, (float)size, (float)size, size / 2.0F, 0.8F, col(16777215, (int)(50.0F * alpha)), false);
         alpha = size * 0.18F;
         float var6 = size * 0.09F;
         float var7 = size * 0.115F;
         float var8 = var6 / 2.0F;
         var5 = col(16777215, var5);
         float var9 = x + alpha * 0.7F;
         int var15 = y + size * 0.3F;
         float var10 = size - alpha * 1.4F;
         float var11 = x + alpha * 1.1F;
         float var12 = var15 + var6 + var7;
         float var13 = size - alpha * 2.2F;
         int var14 = x + alpha * 1.55F;
         var7 = var12 + var6 + var7;
         int var16 = size - alpha * 3.1F;
         GuiRenderer.a(ctx, (float)((int)var9), (float)((int)var15), (float)((int)var10), (float)((int)var6), var8, var5, false);
         GuiRenderer.a(ctx, (float)((int)var11), (float)((int)var12), (float)((int)var13), (float)((int)var6), var8, var5, false);
         GuiRenderer.a(ctx, (float)((int)var14), (float)((int)var7), (float)((int)var16), (float)((int)var6), var8, var5, false);
      }
   }

   private static String a(String text, int maxWidth, WaterFontRenderer font) {
      if (text != null && !text.isEmpty()) {
         if (font.method_a_2(text) <= maxWidth) {
            return text;
         } else {
            String var3 = "...";
            int var4 = font.method_a_2(var3);
            if (var4 >= maxWidth) {
               return var3;
            } else {
               StringBuilder var11 = new StringBuilder();
               String var9 = text.codePoints().toArray();

               for (int var7 : var9) {
                  String var8 = var11.toString() + new String(Character.toChars(var7));
                  if (font.method_a_2(var8 + var3) > maxWidth) {
                     break;
                  }

                  var11.appendCodePoint(var7);
               }

               return var11.toString() + var3;
            }
         }
      } else {
         return "";
      }
   }

   private static String a(long ms) {
      long var2 = ms / 1000L;
      return var2 / 60L + ":" + String.format("%02d", var2 % 60L);
   }

   private static int col(int rgb, int alpha) {
      return Math.max(0, Math.min(255, alpha)) << 24 | rgb & 16777215;
   }

   private static int multiplyAlpha(int color, float alphaMul) {
      int var2 = color >>> 24 & 0xFF;
      float var3 = Math.max(0, Math.min(255, Math.round(var2 * alphaMul)));
      return color & 16777215 | var3 << 24;
   }
}
