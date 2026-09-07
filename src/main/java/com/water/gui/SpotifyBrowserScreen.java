package com.water.gui;

import com.water.module.modules.client.WaterPlus;
import com.water.spotify.SpotifyApi;
import com.water.spotify.SpotifyAuth;
import com.water.utils.renderer.GuiRenderer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;

public final class SpotifyBrowserScreen extends Screen {
   private static final int W = 420;
   private static final int H = 360;
   private static final int R = 10;
   private static final int PAD = 10;
   private static final int HEAD_H = 44;
   private static final int FOOT_H = 44;
   private static final int ROW_H = 22;
   private static final int SRCH_H = 24;
   private static final int TABS_H = 28;
   private final Screen parent;
   private String search = "";
   private int scroll = 0;
   private int hoverRow = -1;
   private long openNs = 0L;
   private int tab = 0;
   private static final List<SpotifyApi.a> QUEUE_CACHE = new ArrayList<>();
   private static final List<String[]> PLAYLISTS_CACHE = new ArrayList<>();
   private static final List<SpotifyApi.a> TRACKS_CACHE = new ArrayList<>();
   private static volatile String selectedPlaylist = null;
   private static volatile String selectedPlaylistName = "";
   private List<Object> filtered = new ArrayList<>();
   private volatile boolean loading = false;
   private volatile String status = "";
   private final ExecutorService exec = Executors.newSingleThreadExecutor(r -> {
      Runnable var1 = new Thread(r, "spotify-browser");
      var1.setDaemon(true);
      return var1;
   });
   private boolean enteringClientId = false;
   private String clientIdInput = "";

   public SpotifyBrowserScreen(Screen parent) {
      super(Text.literal(""));
      this.parent = parent;
   }

   private int px() {
      return (this.width - 420) / 2;
   }

   private int py() {
      return (this.height - 360) / 2;
   }

   private int listY() {
      return this.py() + 44 + 28 + 24 + 10;
   }

   private int listH() {
      return 200;
   }

   private int maxRows() {
      return this.listH() / 22;
   }

   private int maxScroll() {
      return Math.max(0, this.filtered.size() - this.maxRows());
   }

   @Override
   public void init() {
      String var1 = WaterPlus.getSpotifyClientId();
      if (var1 != null && !var1.isBlank()) {
         SpotifyAuth.v(var1);
      }

      if (!SpotifyAuth.ad()) {
         this.enteringClientId = true;
         this.clientIdInput = "";
         this.status = "Enter your Spotify Client ID";
      } else if (!SpotifyAuth.ae()) {
         this.status = "Click CONNECT to login";
      } else {
         this.loadQueue();
      }
   }

   private void loadQueue() {
      this.loading = true;
      this.status = "Loading queue...";
      this.exec.submit(() -> {
         List var1 = SpotifyApi.b();
         synchronized (QUEUE_CACHE) {
            QUEUE_CACHE.clear();
            QUEUE_CACHE.addAll(var1);
         }

         this.status = var1.isEmpty() ? "Queue is empty" : var1.size() + " tracks";
         this.loading = false;
         this.client.execute(this::rebuild);
      });
   }

   private void loadPlaylists() {
      this.loading = true;
      this.status = "Loading playlists...";
      this.exec.submit(() -> {
         List var1 = SpotifyApi.c();
         synchronized (PLAYLISTS_CACHE) {
            PLAYLISTS_CACHE.clear();
            PLAYLISTS_CACHE.addAll(var1);
         }

         this.status = var1.size() + " playlists";
         this.loading = false;
         this.client.execute(this::rebuild);
      });
   }

   private void loadPlaylistTracks(String id, String name) {
      selectedPlaylist = id;
      selectedPlaylistName = name;
      this.loading = true;
      this.status = "Loading " + name + "...";
      this.tab = 2;
      this.exec.submit(() -> {
         String var5 = SpotifyApi.b(id);
         synchronized (TRACKS_CACHE) {
            TRACKS_CACHE.clear();
            TRACKS_CACHE.addAll(var5);
         }

         this.status = var5.size() + " tracks in " + name;
         this.loading = false;
         this.client.execute(this::rebuild);
      });
   }

   private void rebuild() {
      String var1 = this.search.trim().toLowerCase();
      this.filtered = new ArrayList<>();

      Object var2 = switch (this.tab) {
         case 0 -> QUEUE_CACHE;
         case 1 -> PLAYLISTS_CACHE;
         case 2 -> TRACKS_CACHE;
         default -> new ArrayList();
      };
      synchronized (var2) {
         for (Object var4 : var2) {
            String var9 = var4 instanceof SpotifyApi.a var5 ? var5.r() : (var4 instanceof String[] var8 ? var8[1] : "");
            if (var1.isEmpty() || var9.toLowerCase().contains(var1)) {
               this.filtered.add(var4);
            }
         }
      }

      this.scroll = Math.max(0, Math.min(this.maxScroll(), this.scroll));
   }

   @Override
   public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
      if (this.openNs == 0L) {
         this.openNs = System.nanoTime();
      }

      deltaTicks = this.easeOut(Math.min(1.0F, (float)(System.nanoTime() - this.openNs) / 1.8E8F));
      int var5 = this.px();
      int var6 = this.py();
      int var7 = WaterPlus.getAccentARGB();
      var7 &= 16777215;
      context.fill(0, 0, this.width, this.height, this.alphaScale(-2013265920, deltaTicks));
      GuiRenderer.a(context, (float)var5, (float)var6, 420.0F, 360.0F, 10.0F, this.alphaScale(-234353645, deltaTicks), false);
      GuiRenderer.a(context, (float)var5, (float)var6, 420.0F, 360.0F, 10.0F, 1.5F, this.col(var7, (int)(80.0F * deltaTicks)), false);
      GuiRenderer.a(context, (float)var5, (float)var6, 420.0F, 44.0F, 10.0F, 10.0F, 0.0F, 0.0F, false, this.alphaScale(-871756264, deltaTicks));
      context.fill(var5, var6 + 44, var5 + 420, var6 + 44 + 1, this.alphaScale(553648127, deltaTicks));
      context.fill(var5, var6 + 10, var5 + 3, var6 + 44 - 10, this.col(var7, (int)(255.0F * deltaTicks)));
      context.drawText(this.textRenderer, "SPOTIFY", var5 + 10 + 8, var6 + 10, this.col(15659767, (int)(255.0F * deltaTicks)), false);
      String var8 = this.enteringClientId
         ? "Enter Client ID to connect"
         : (!SpotifyAuth.ad() ? "Not configured" : (!SpotifyAuth.ae() ? (SpotifyAuth.af() ? "Connecting..." : "Click CONNECT") : this.status));
      context.drawText(this.textRenderer, var8, var5 + 10 + 8, var6 + 25, this.col(6320256, (int)(180.0F * deltaTicks)), false);
      if (!SpotifyAuth.ae() && !this.enteringClientId) {
         int var21 = var5 + 420 - 10 - 80;
         int var9 = var6 + 22 - 9;
         boolean var10 = this.hov(mouseX, mouseY, var21, var9, 80, 18);
         GuiRenderer.a(
            context, (float)var21, (float)var9, 80.0F, 18.0F, 4.0F, this.col(var7, var10 ? (int)(50.0F * deltaTicks) : (int)(20.0F * deltaTicks)), false
         );
         GuiRenderer.a(context, (float)var21, (float)var9, 80.0F, 18.0F, 4.0F, 1.0F, this.col(var7, (int)(150.0F * deltaTicks)), false);
         String var25 = SpotifyAuth.ad() ? "CONNECT" : "SETUP";
         context.drawText(
            this.textRenderer, var25, var21 + (80 - this.textRenderer.getWidth(var25)) / 2, var9 + 5, this.col(var7, (int)(220.0F * deltaTicks)), false
         );
      }

      if (this.enteringClientId) {
         this.renderClientIdSetup(context, var5, var6, mouseX, mouseY, deltaTicks, var7);
      } else if (!SpotifyAuth.ae()) {
         context.drawText(
            this.textRenderer,
            "Click CONNECT to login with Spotify",
            var5 + (420 - this.textRenderer.getWidth("Click CONNECT to login with Spotify")) / 2,
            var6 + 180 - 4,
            this.col(6320256, (int)(180.0F * deltaTicks)),
            false
         );
         this.renderFooter(context, var5, var6, mouseX, mouseY, deltaTicks, var7);
      } else {
         this.renderTabs(context, var5, var6, mouseX, mouseY, deltaTicks, var7);
         int var22 = var6 + 44 + 28 + 4;
         int var24 = var5 + 10;
         int var26 = !this.search.isEmpty();
         GuiRenderer.a(context, (float)var24, (float)var22, 400.0F, 24.0F, 6.0F, this.alphaScale(-16183784, deltaTicks), false);
         GuiRenderer.a(
            context,
            (float)var24,
            (float)var22,
            400.0F,
            24.0F,
            6.0F,
            1.0F,
            this.col(var26 ? var7 : 822083583, var26 ? (int)(120.0F * deltaTicks) : (int)(50.0F * deltaTicks)),
            false
         );
         context.drawText(
            this.textRenderer,
            var26 ? "⌕  " + this.search + "_" : "⌕  Search...",
            var24 + 8,
            var22 + 12 - 4,
            this.col(var26 ? 15659767 : 6320256, (int)(200.0F * deltaTicks)),
            false
         );
         int var23 = this.listY();
         var26 = this.listH();
         GuiRenderer.a(context, (float)var24, (float)(var23 - 2), 400.0F, (float)(var26 + 4), 6.0F, this.alphaScale(-1442444784, deltaTicks), false);
         this.hoverRow = -1;
         if (this.loading && this.filtered.isEmpty()) {
            context.drawText(
               this.textRenderer,
               this.status,
               var24 + (400 - this.textRenderer.getWidth(this.status)) / 2,
               var23 + var26 / 2 - 4,
               this.col(6320256, (int)(180.0F * deltaTicks)),
               false
            );
         } else if (this.filtered.isEmpty()) {
            String var11 = this.search.isEmpty() ? "Nothing here yet" : "No results for \"" + this.search + "\"";
            context.drawText(
               this.textRenderer,
               var11,
               var24 + (400 - this.textRenderer.getWidth(var11)) / 2,
               var23 + var26 / 2 - 4,
               this.col(6320256, (int)(180.0F * deltaTicks)),
               false
            );
         } else {
            int var28 = Math.min(this.scroll + this.maxRows(), this.filtered.size());

            for (int var12 = this.scroll; var12 < var28; var12++) {
               Object var13 = this.filtered.get(var12);
               int var14 = var23 + (var12 - this.scroll) * 22;
               boolean var15 = this.hov(mouseX, mouseY, var24, var14, 400, 22);
               if (var15) {
                  this.hoverRow = var12;
               }

               boolean var16 = var13 instanceof SpotifyApi.a var17 && var17 == this.filtered.get(0) && this.tab == 0;
               if (var16) {
                  GuiRenderer.a(context, (float)var24, (float)var14, 400.0F, 21.0F, 4.0F, this.col(var7, (int)(25.0F * deltaTicks)), false);
                  GuiRenderer.a(context, (float)var24, (float)var14, 400.0F, 21.0F, 4.0F, 1.0F, this.col(var7, (int)(80.0F * deltaTicks)), false);
               } else if (var15) {
                  GuiRenderer.a(context, (float)var24, (float)var14, 400.0F, 21.0F, 4.0F, this.alphaScale(369098751, deltaTicks), false);
               }

               int var35 = var24 + 8;
               if (var16) {
                  context.fill(var24 + 3, var14 + 5, var24 + 5, var14 + 22 - 5, this.col(var7, (int)(255.0F * deltaTicks)));
                  var35 = var24 + 10;
               }

               String var18 = var12 + 1 + ".";
               context.drawText(this.textRenderer, var18, var35, var14 + 11 - 4, this.col(5267568, (int)(160.0F * deltaTicks)), false);
               var35 += this.textRenderer.getWidth(var18) + 4;
               if (var13 instanceof String[]) {
                  var18 = ">";
                  context.drawText(this.textRenderer, var18, var24 + 400 - 14, var14 + 11 - 4, this.col(var7, (int)(160.0F * deltaTicks)), false);
               }

               var18 = var13 instanceof SpotifyApi.a var30
                  ? this.textRenderer.trimToWidth(var30.r(), 400 - var35 - 18)
                  : (var13 instanceof String[] var31 ? this.textRenderer.trimToWidth(var31[1], 400 - var35 - 18) : "");
               int var32 = var16 ? this.col(var7, (int)(255.0F * deltaTicks)) : this.col(var15 ? 15659767 : 10531008, (int)(220.0F * deltaTicks));
               context.drawText(this.textRenderer, var18, var35, var14 + 11 - 4, var32, false);
            }

            if (this.filtered.size() > this.maxRows()) {
               int var29 = var24 + 400 + 2;
               float var33 = Math.max(20.0F, var26 * ((float)this.maxRows() / this.filtered.size()));
               float var34 = var23 + (var26 - var33) * ((float)this.scroll / Math.max(1, this.maxScroll()));
               GuiRenderer.a(context, (float)var29, (float)var23, 3.0F, (float)var26, 1.5F, this.alphaScale(318767103, deltaTicks), false);
               GuiRenderer.a(context, (float)var29, (float)((int)var34), 3.0F, (float)((int)var33), 1.5F, this.col(var7, (int)(150.0F * deltaTicks)), false);
            }
         }

         this.renderFooter(context, var5, var6, mouseX, mouseY, deltaTicks, var7);
      }
   }

   private void renderTabs(DrawContext ctx, int px, int py, int mx, int my, float a, int accentRgb) {
      py = py + 44 + 4;
      String[] var8 = new String[]{"QUEUE", "PLAYLISTS", this.tab == 2 ? selectedPlaylistName : "TRACKS"};

      for (int var9 = 0; var9 < 3; var9++) {
         int var10 = px + 10 + var9 * 135;
         boolean var11 = this.tab == var9;
         int var12 = this.hov(mx, my, var10, py, 133, 22);
         var12 = var11 ? this.col(accentRgb, (int)(35.0F * a)) : this.col(16777215, var12 ? (int)(18.0F * a) : (int)(8.0F * a));
         int var13 = var11 ? this.col(accentRgb, (int)(150.0F * a)) : this.col(16777215, (int)(30.0F * a));
         GuiRenderer.a(ctx, (float)var10, (float)py, 133.0F, 22.0F, 5.0F, var12, false);
         GuiRenderer.a(ctx, (float)var10, (float)py, 133.0F, 22.0F, 5.0F, 1.0F, var13, false);
         String var16 = this.textRenderer.trimToWidth(var8[var9], 125);
         ctx.drawText(
            this.textRenderer,
            var16,
            var10 + (133 - this.textRenderer.getWidth(var16)) / 2,
            py + 11 - 4,
            var11 ? this.col(accentRgb, (int)(255.0F * a)) : this.col(10531008, (int)(200.0F * a)),
            false
         );
      }
   }

   private void renderClientIdSetup(DrawContext ctx, int px, int py, int mx, int my, float a, int accentRgb) {
      py = py + 180 - 40;
      ctx.drawText(
         this.textRenderer,
         "Setup Spotify (free on developer.spotify.com)",
         px + (420 - this.textRenderer.getWidth("Setup Spotify (free on developer.spotify.com)")) / 2,
         py - 20,
         this.col(10531008, (int)(200.0F * a)),
         false
      );
      ctx.drawText(this.textRenderer, "Enter Client ID:", px + 10, py, this.col(15659767, (int)(255.0F * a)), false);
      GuiRenderer.a(ctx, (float)(px + 10), (float)(py + 14), 400.0F, 22.0F, 5.0F, this.alphaScale(-16183784, a), false);
      GuiRenderer.a(ctx, (float)(px + 10), (float)(py + 14), 400.0F, 22.0F, 5.0F, 1.0F, this.col(accentRgb, (int)(150.0F * a)), false);
      String var8 = this.clientIdInput.isEmpty() ? "Paste Client ID here..." : this.clientIdInput + "_";
      ctx.drawText(this.textRenderer, var8, px + 10 + 6, py + 19, this.col(this.clientIdInput.isEmpty() ? 6320256 : 15659767, (int)(200.0F * a)), false);
      py += 46;
      boolean var11 = this.hov(mx, my, px + 10, py, 80, 22);
      this.hov(mx, my, px + 420 - 10 - 80, py, 80, 22);
      GuiRenderer.a(ctx, (float)(px + 10), (float)py, 80.0F, 22.0F, 5.0F, this.col(accentRgb, var11 ? (int)(50.0F * a) : (int)(20.0F * a)), false);
      GuiRenderer.a(ctx, (float)(px + 10), (float)py, 80.0F, 22.0F, 5.0F, 1.0F, this.col(accentRgb, (int)(150.0F * a)), false);
      ctx.drawText(this.textRenderer, "SAVE & CONNECT", px + 10 + 6, py + 7, this.col(accentRgb, (int)(220.0F * a)), false);
      ctx.drawText(
         this.textRenderer,
         "developer.spotify.com →",
         px + 420 - 10 - this.textRenderer.getWidth("developer.spotify.com →"),
         py + 7,
         this.col(6320256, (int)(160.0F * a)),
         false
      );
   }

   private void renderFooter(DrawContext ctx, int px, int py, int mx, int my, float a, int accentRgb) {
      GuiRenderer.a(ctx, (float)px, (float)(py + 360 - 44), 420.0F, 44.0F, 0.0F, 0.0F, 10.0F, 10.0F, false, this.alphaScale(-871756264, a));
      ctx.fill(px, py + 360 - 44, px + 420, py + 360 - 44 + 1, this.alphaScale(553648127, a));
      py = py + 360 - 44 + 9;
      this.drawBtn(ctx, px + 10, py, 64, 26, "⏮ PREV", mx, my, a, accentRgb);
      this.drawBtn(ctx, px + 10 + 68, py, 80, 26, "⏸ PAUSE", mx, my, a, accentRgb);
      this.drawBtn(ctx, px + 10 + 152, py, 64, 26, "NEXT ⏭", mx, my, a, accentRgb);
      px = px + 420 - 10 - 64;
      int var10 = this.hov(mx, my, px, py, 64, 26);
      GuiRenderer.a(ctx, (float)px, (float)py, 64.0F, 26.0F, 6.0F, this.alphaScale(var10 ? 637534207 : 285212671, a), false);
      GuiRenderer.a(ctx, (float)px, (float)py, 64.0F, 26.0F, 6.0F, 1.0F, this.alphaScale(553648127, a), false);
      ctx.drawText(this.textRenderer, "CLOSE", px + (64 - this.textRenderer.getWidth("CLOSE")) / 2, py + 9, this.col(10531008, (int)(200.0F * a)), false);
   }

   private void drawBtn(DrawContext ctx, int x, int y, int w, int h, String lbl, int mx, int my, float a, int rgb) {
      int var11 = this.hov(mx, my, x, y, w, h);
      GuiRenderer.a(ctx, (float)x, (float)y, (float)w, (float)h, 6.0F, this.col(rgb, var11 ? (int)(45.0F * a) : (int)(18.0F * a)), false);
      GuiRenderer.a(ctx, (float)x, (float)y, (float)w, (float)h, 6.0F, 1.0F, this.col(rgb, var11 ? (int)(180.0F * a) : (int)(60.0F * a)), false);
      ctx.drawText(
         this.textRenderer, lbl, x + (w - this.textRenderer.getWidth(lbl)) / 2, y + h / 2 - 4, this.col(var11 ? rgb : 10531008, (int)(220.0F * a)), false
      );
   }

   @Override
   public boolean mouseClicked(Click click, boolean doubled) {
      int var3 = (int)click.x();
      int var4 = (int)click.y();
      int var5 = this.px();
      int var6 = this.py();
      if (this.enteringClientId) {
         int var17 = var6 + 180 - 40;
         Click var15 = var17 + 46;
         if (this.hov(var3, var4, var5 + 10, var15, 80, 22) && !this.clientIdInput.isBlank()) {
            SpotifyAuth.v(this.clientIdInput);
            this.enteringClientId = false;
            this.init();
         }

         return true;
      } else if (!SpotifyAuth.ae()) {
         int var16 = var5 + 420 - 10 - 80;
         Click var14 = var6 + 22 - 9;
         if (this.hov(var3, var4, var16, var14, 80, 18)) {
            if (!SpotifyAuth.ad()) {
               this.enteringClientId = true;
               this.clientIdInput = "";
            } else {
               SpotifyAuth.a(() -> {
                  this.status = "Connected!";
                  this.loadQueue();
               }, () -> this.status = "Auth failed.");
            }
         }

         this.renderFooterClick(var3, var4, var5, var6);
         return true;
      } else {
         int var7 = var6 + 44 + 4;

         for (int var8 = 0; var8 < 3; var8++) {
            int var9 = var5 + 10 + var8 * 135;
            if (this.hov(var3, var4, var9, var7, 133, 22)) {
               if (var8 == 0 && this.tab != 0) {
                  this.tab = 0;
                  this.search = "";
                  this.loadQueue();
               } else if (var8 == 1 && this.tab != 1) {
                  this.tab = 1;
                  this.search = "";
                  if (PLAYLISTS_CACHE.isEmpty()) {
                     this.loadPlaylists();
                  } else {
                     this.rebuild();
                  }
               } else if (var8 == 2 && this.tab != 2 && selectedPlaylist != null) {
                  this.tab = 2;
                  this.search = "";
                  this.rebuild();
               }

               return true;
            }
         }

         if (this.renderFooterClick(var3, var4, var5, var6)) {
            return true;
         } else {
            int var18 = var5 + 10;
            int var19 = this.listY();
            if (var3 >= var18 && var3 < var18 + 400 && var4 >= var19 && var4 < var19 + this.listH()) {
               Click var10 = (var4 - var19) / 22 + this.scroll;
               if (var10 >= 0 && var10 < this.filtered.size()) {
                  Click var11 = this.filtered.get(var10);
                  if (var11 instanceof Click var12) {
                     this.exec.submit(() -> {
                        boolean var2 = SpotifyApi.method_e_1(var12.ae);
                        if (!var2 && selectedPlaylist != null) {
                           SpotifyApi.method_a_1("spotify:playlist:" + selectedPlaylist, var12.ae);
                        }

                        this.client.execute(() -> {
                           this.status = "Playing: " + var12.ad;
                           this.loadQueue();
                        });
                     });
                  } else if (var11 instanceof Click var13) {
                     this.loadPlaylistTracks(var13[0], var13[1]);
                  }
               }

               return true;
            } else {
               return super.mouseClicked(click, doubled);
            }
         }
      }
   }

   private boolean renderFooterClick(int mx, int my, int px, int py) {
      py = py + 360 - 44 + 9;
      if (this.hov(mx, my, px + 420 - 10 - 64, py, 64, 26)) {
         this.client.setScreen(this.parent);
         return true;
      } else if (this.hov(mx, my, px + 10, py, 64, 26)) {
         this.exec.submit(SpotifyApi::ac);
         return true;
      } else if (this.hov(mx, my, px + 10 + 68, py, 80, 26)) {
         this.exec.submit(SpotifyApi::aa);
         return true;
      } else if (this.hov(mx, my, px + 10 + 152, py, 64, 26)) {
         this.exec.submit(SpotifyApi::ab);
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean charTyped(CharInput input) {
      String var2 = input.asString();
      if (var2 != null && !var2.isEmpty()) {
         if (this.enteringClientId) {
            this.clientIdInput = this.clientIdInput + var2;
         } else {
            this.search = this.search + var2;
            this.rebuild();
         }

         return true;
      } else {
         return super.charTyped(input);
      }
   }

   @Override
   public boolean keyPressed(KeyInput input) {
      if (input.getKeycode() != 259) {
         if (input.isEscape()) {
            this.client.setScreen(this.parent);
            return true;
         } else {
            return super.keyPressed(input);
         }
      } else {
         if (this.enteringClientId && !this.clientIdInput.isEmpty()) {
            this.clientIdInput = this.clientIdInput.substring(0, this.clientIdInput.length() - 1);
         } else if (!this.search.isEmpty()) {
            this.search = this.search.substring(0, this.search.length() - 1);
            this.rebuild();
         }

         return true;
      }
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      int var9 = this.px() + 10;
      int var10 = this.listY();
      if (mouseX >= var9 && mouseX < var9 + 400 && mouseY >= var10 && mouseY < var10 + this.listH()) {
         this.scroll = Math.max(0, Math.min(this.maxScroll(), this.scroll + (verticalAmount > 0.0 ? -1 : 1)));
         return true;
      } else {
         return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
      }
   }

   @Override
   public boolean shouldPause() {
      return false;
   }

   @Override
   public void renderBackground(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
   }

   private int alphaScale(int c, float a) {
      return Math.max(0, Math.min(255, (int)((c >> 24 & 0xFF) * a))) << 24 | c & 16777215;
   }

   private int col(int rgb, int alpha) {
      return Math.max(0, Math.min(255, alpha)) << 24 | rgb & 16777215;
   }

   private float easeOut(float t) {
      return 1.0F - (float)Math.pow(1.0F - Math.min(1.0F, t), 3.0);
   }

   private boolean hov(int mx, int my, int x, int y, int w, int h) {
      return mx >= x && mx <= x + w && my >= y && my <= y + h;
   }
}
