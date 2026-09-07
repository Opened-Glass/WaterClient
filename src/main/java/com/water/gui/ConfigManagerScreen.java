package com.water.gui;

import com.water.module.Module;
import com.water.module.ModuleManager;
import com.water.module.modules.client.ConfigShare;
import com.water.module.modules.client.WaterPlus;
import com.water.utils.renderer.GuiRenderer;
import com.water.utils.renderer.WaterFontRenderer;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;

public class ConfigManagerScreen extends Screen {
   private static final int W = 340;
   private static final int H = 380;
   private static final int PAD = 14;
   private static final int ROW_H = 42;
   private static final int ROWS = 4;
   private static final int MAX_CFG = 5;
   private static int BG;
   private static int SURFACE;
   private static int SURFACE2;
   private static int BORDER;
   private static int ACCENT;
   private static int ACCENT2;
   private static final int TEXT = -1117441;
   private static final int TEXT2 = -7824982;
   private static final int TEXT3 = -12298906;
   private static final int GREEN = -14498466;
   private static final int GREEN_BG = -15914984;
   private static final int RED = -1096636;
   private static final int RED_BG = -13824498;
   private static final int AMBER = -680437;
   private static final int AMBER_BG = -13821184;
   private static int SEL_BG;
   private static int SEL_BD;
   private ConfigManagerScreen.a tab = ConfigManagerScreen.a.CONFIGS;
   private final List<String> configs = new ArrayList<>();
   private int sel = 0;
   private int scroll = 0;
   private boolean newOpen = false;
   private boolean importOpen = false;
   private String newName = "";
   private String importCode = "";
   private String status = "";
   private int statusCol = -7824982;
   private long statusAt = 0L;
   private int px;
   private int py;

   private static void syncColors() {
      int var0 = WaterPlus.getBackgroundARGB();
      int var1 = WaterPlus.getAccentARGB();
      BG = darken(var0, 0.85F);
      SURFACE = darken(var0, 0.95F);
      SURFACE2 = var0;
      BORDER = blendArgb(var0, var1, 0.15F);
      ACCENT = var1;
      ACCENT2 = darken(var1, 0.75F);
      SEL_BG = blendArgb(var0, var1, 0.2F);
      SEL_BD = blendArgb(var0, var1, 0.55F);
   }

   private static int darken(int argb, float f) {
      int var2 = Math.max(0, (int)((argb >> 16 & 0xFF) * f));
      int var3 = Math.max(0, (int)((argb >> 8 & 0xFF) * f));
      float var4 = Math.max(0, (int)((argb & 0xFF) * f));
      return argb & 0xFF000000 | var2 << 16 | var3 << 8 | var4;
   }

   private static int blendArgb(int a, int b, float t) {
      int var3 = a >> 16 & 0xFF;
      int var4 = a >> 8 & 0xFF;
      a &= 255;
      int var5 = b >> 16 & 0xFF;
      int var6 = b >> 8 & 0xFF;
      b &= 255;
      return 0xFF000000 | (int)(var3 + (var5 - var3) * t) << 16 | (int)(var4 + (var6 - var4) * t) << 8 | (int)(a + (b - a) * t);
   }

   private static float R() {
      return WaterPlus.getGuiRoundness() + 6.0F;
   }

   private static float RR() {
      return Math.max(4.0F, WaterPlus.getGuiRoundness());
   }

   private void t(DrawContext ctx, String s, int x, int y, int col) {
      WaterFontRenderer.INSTANCE.a(ctx, s, x, y, col);
   }

   private void tc(DrawContext ctx, String s, int cx, int y, int col) {
      int var6 = WaterFontRenderer.INSTANCE.method_a_2(s);
      WaterFontRenderer.INSTANCE.a(ctx, s, cx - var6 / 2, y, col);
   }

   private int fw(String s) {
      return WaterFontRenderer.INSTANCE.method_a_2(s);
   }

   public ConfigManagerScreen() {
      super(Text.literal("Water Configs"));
   }

   @Override
   protected void init() {
      this.px = (this.width - 340) / 2;
      this.py = (this.height - 380) / 2;
      this.refreshConfigs();
   }

   @Override
   public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
      syncColors();
      ModuleManager.INSTANCE.getModules().stream().filter(m -> m instanceof WaterPlus).findFirst().ifPresent(m -> m.onTick());
      WaterFontRenderer.bk();
      GuiRenderer.a(context, (float)this.px, (float)this.py, 340.0F, 380.0F, R(), BG, false);
      GuiRenderer.a(context, (float)this.px, (float)this.py, 340.0F, 380.0F, R(), 1.0F, BORDER, false);
      this.renderHeader(context, mouseX, mouseY);
      this.renderTabs(context, mouseX, mouseY);
      if (this.tab == ConfigManagerScreen.a.CONFIGS) {
         this.renderConfigs(context, mouseX, mouseY);
      } else {
         this.renderShare(context, mouseX, mouseY);
      }

      this.renderFooter(context, mouseX, mouseY);
      if (!this.status.isEmpty() && System.currentTimeMillis() - this.statusAt < 3000L) {
         float var7 = this.fw(this.status) + 24;
         int var5 = this.px + 170 - var7 / 2;
         int var6 = this.py + 380 - 36;
         GuiRenderer.a(context, (float)var5, (float)var6, (float)var7, 22.0F, 6.0F, -871756268, false);
         GuiRenderer.a(context, (float)var5, (float)var6, (float)var7, 22.0F, 6.0F, 1.0F, this.statusCol & 1442840575, false);
         this.tc(context, this.status, this.px + 170, var6 + 7, this.statusCol);
      }

      if (this.newOpen) {
         this.renderNewPopup(context, mouseX, mouseY);
      }

      if (this.importOpen) {
         this.renderImportPopup(context, mouseX, mouseY);
      }
   }

   private void renderHeader(DrawContext ctx, int mx, int my) {
      int var4 = this.py + 18;
      this.t(ctx, "WATER CONFIGS", this.px + 14, var4, -1117441);
      String var5 = this.configs.size() + " / " + 5;
      int var6 = this.fw(var5) + 16;
      GuiRenderer.a(ctx, (float)(this.px + 340 - 14 - var6), (float)(var4 - 4), (float)var6, 20.0F, 5.0F, SURFACE2, false);
      this.tc(ctx, var5, this.px + 340 - 14 - var6 / 2, var4, -7824982);
      int var7 = this.hov(mx, my, this.px + 340 - 28, var4 - 4, 20, 20);
      this.t(ctx, "x", this.px + 340 - 22, var4, var7 ? -1117441 : -7824982);
      ctx.fill(this.px + 14, this.py + 46, this.px + 340 - 14, this.py + 47, BORDER);
   }

   private void renderTabs(DrawContext ctx, int mx, int my) {
      mx = this.py + 56;
      int var9 = "MY CONFIGS";
      String var4 = "SHARE / IMPORT";
      boolean var5 = this.tab == ConfigManagerScreen.a.CONFIGS;
      int var6 = this.px + 14;
      int var7 = this.px + 14 + 130;
      GuiRenderer.a(ctx, (float)(var6 - 6), (float)(mx - 4), (float)(this.fw(var9) + 12), 24.0F, 5.0F, var5 ? SURFACE2 : 0, false);
      GuiRenderer.a(ctx, (float)(var7 - 6), (float)(mx - 4), (float)(this.fw(var4) + 12), 24.0F, 5.0F, !var5 ? SURFACE2 : 0, false);
      this.t(ctx, var9, var6, mx + 3, var5 ? -1117441 : -7824982);
      this.t(ctx, var4, var7, mx + 3, !var5 ? -1117441 : -7824982);
      var6 = var5 ? var6 : var7;
      my = var5 ? this.fw(var9) : this.fw(var4);
      GuiRenderer.a(ctx, (float)var6, (float)(mx + 22), (float)my, 3.0F, 2.0F, ACCENT, false);
      ctx.fill(this.px + 14, mx + 28, this.px + 340 - 14, mx + 29, BORDER);
   }

   private void renderConfigs(DrawContext ctx, int mx, int my) {
      int var4 = this.py + 100;
      if (this.configs.isEmpty()) {
         this.tc(ctx, "No configs yet — create one below", this.px + 170, var4 + 168 / 2 - 6, -12298906);
      } else {
         for (int var5 = 0; var5 < 4; var5++) {
            int var6 = var5 + this.scroll;
            if (var6 >= this.configs.size()) {
               if (var6 == this.configs.size() && this.configs.size() < 5) {
                  int var7 = var4 + var5 * 42 + 4;
                  GuiRenderer.a(ctx, (float)(this.px + 14), (float)var7, 312.0F, 34.0F, RR(), 587202559, false);
                  GuiRenderer.a(ctx, (float)(this.px + 14), (float)var7, 312.0F, 34.0F, RR(), 1.0F, 587202559, false);
                  this.tc(ctx, "+ new config slot", this.px + 170, var7 + 17 - 5, -12298906);
               }
            } else {
               int var12 = var4 + var5 * 42 + 4;
               int var8 = var6 == this.sel;
               int var9 = this.hov(mx, my, this.px + 14, var12, 312, 34);
               var9 = var8 ? SEL_BG : (var9 ? SURFACE2 : SURFACE);
               int var10 = var8 ? SEL_BD : BORDER;
               GuiRenderer.a(ctx, (float)(this.px + 14), (float)var12, 312.0F, 34.0F, RR(), var9, false);
               GuiRenderer.a(ctx, (float)(this.px + 14), (float)var12, 312.0F, 34.0F, RR(), 1.0F, var10, false);
               if (var8) {
                  GuiRenderer.a(ctx, (float)(this.px + 14), (float)(var12 + 8), 3.0F, 18.0F, 2.0F, ACCENT, false);
               }

               this.t(ctx, this.configs.get(var6), this.px + 14 + (var8 ? 14 : 10), var12 + 17 - 5, var8 ? -1117441 : -7824982);
               var6 = var12 + 17 - 10;
               var12 = this.px + 340 - 14 - 70;
               var8 = var12 - 80;
               boolean var17 = this.hov(mx, my, var8, var6, 70, 20);
               GuiRenderer.a(ctx, (float)var8, (float)var6, 70.0F, 20.0F, 5.0F, var17 ? -15914984 : SURFACE, false);
               GuiRenderer.a(ctx, (float)var8, (float)var6, 70.0F, 20.0F, 5.0F, 1.0F, var17 ? -14498466 : 872415231, false);
               this.tc(ctx, "Reload", var8 + 35, var6 + 5, -14498466);
               boolean var15 = this.hov(mx, my, var12, var6, 70, 20);
               GuiRenderer.a(ctx, (float)var12, (float)var6, 70.0F, 20.0F, 5.0F, var15 ? -13824498 : SURFACE, false);
               GuiRenderer.a(ctx, (float)var12, (float)var6, 70.0F, 20.0F, 5.0F, 1.0F, var15 ? -1096636 : 872415231, false);
               this.tc(ctx, "Delete", var12 + 35, var6 + 5, -1096636);
            }
         }
      }
   }

   private void renderShare(DrawContext ctx, int mx, int my) {
      ConfigShare var4 = ConfigShare.a();
      int var5 = var4 != null && (var4.e || var4.f);
      int var6 = this.px + 14;
      int var7 = this.py + 105;
      var7 += 8;
      boolean var8 = !var5 && this.hov(mx, my, var6, var7, 312, 36);
      GuiRenderer.a(ctx, (float)var6, (float)var7, 312.0F, 36.0F, RR(), var8 ? ACCENT2 : SURFACE2, false);
      GuiRenderer.a(ctx, (float)var6, (float)var7, 312.0F, 36.0F, RR(), 1.0F, var8 ? ACCENT : BORDER, false);
      String var17 = var4 != null && var4.e ? "Uploading..." : "Share Config";
      this.tc(ctx, var17, this.px + 170, var7 + 11, -1117441);
      var7 += 44;
      int var9 = !var5 && this.hov(mx, my, var6, var7, 312, 36);
      GuiRenderer.a(ctx, (float)var6, (float)var7, 312.0F, 36.0F, RR(), var9 ? SURFACE2 : SURFACE, false);
      GuiRenderer.a(ctx, (float)var6, (float)var7, 312.0F, 36.0F, RR(), 1.0F, var9 ? ACCENT : BORDER, false);
      int var11 = var4 != null && var4.f ? "Importing..." : "Import Config";
      this.tc(ctx, var11, this.px + 170, var7 + 11, var9 ? -1117441 : -7824982);
      var7 += 48;
      if (var4 != null && !var4.c.isEmpty()) {
         int var10 = "Code: " + var4.c + "  (copied)";
         my = this.fw(var10) + 24;
         var5 = this.px + 170 - my / 2;
         GuiRenderer.a(ctx, (float)var5, (float)var7, (float)my, 26.0F, 6.0F, SURFACE2, false);
         GuiRenderer.a(ctx, (float)var5, (float)var7, (float)my, 26.0F, 6.0F, 1.0F, ACCENT, false);
         this.tc(ctx, var10, this.px + 170, var7 + 8, ACCENT);
      }

      if (var4 != null && !var4.d.isEmpty()) {
         this.tc(ctx, var4.d, this.px + 170, this.py + 380 - 70, -1096636);
      }
   }

   private void renderFooter(DrawContext ctx, int mx, int my) {
      int var4 = this.py + 380 - 54;
      ctx.fill(this.px + 14, var4, this.px + 340 - 14, var4 + 1, BORDER);
      var4 += 14;
      if (this.tab == ConfigManagerScreen.a.CONFIGS) {
         int var5 = this.configs.size() >= 5;
         boolean var6 = !var5 && this.hov(mx, my, this.px + 14, var4, 110, 18);
         this.t(ctx, "+ New config", this.px + 14, var4 + 2, var6 ? ACCENT : (var5 ? -12298906 : -7824982));
         var5 = this.px + 340 - 14 - 90;
         int var7 = this.hov(mx, my, var5, var4 - 4, 90, 26);
         GuiRenderer.a(ctx, (float)var5, (float)(var4 - 4), 90.0F, 26.0F, 6.0F, var7 ? -13821184 : SURFACE, false);
         GuiRenderer.a(ctx, (float)var5, (float)(var4 - 4), 90.0F, 26.0F, 6.0F, 1.0F, var7 ? -680437 : BORDER, false);
         this.tc(ctx, "Reset all", var5 + 90 / 2, var4 + 2, -680437);
      }
   }

   private void renderNewPopup(DrawContext ctx, int mx, int my) {
      int var4 = this.px + 170 - 340 / 2;
      int var5 = this.py + 190 - 130 / 2;
      GuiRenderer.a(ctx, (float)var4, (float)var5, 340.0F, 130.0F, 12.0F, BG, false);
      GuiRenderer.a(ctx, (float)var4, (float)var5, 340.0F, 130.0F, 12.0F, 1.0F, BORDER, false);
      this.tc(ctx, "New Config Name", var4 + 340 / 2, var5 + 14, -1117441);
      GuiRenderer.a(ctx, (float)(var4 + 16), (float)(var5 + 40), 308.0F, 30.0F, 6.0F, SURFACE2, false);
      GuiRenderer.a(ctx, (float)(var4 + 16), (float)(var5 + 40), 308.0F, 30.0F, 6.0F, 1.0F, this.newName.isEmpty() ? BORDER : ACCENT, false);
      String var6 = this.newName.isEmpty() ? "Enter name..." : this.newName;
      this.t(ctx, var6, var4 + 26, var5 + 50, this.newName.isEmpty() ? -12298906 : -1117441);
      var4 = var4 + 340 / 2 - 55;
      int var7 = this.hov(mx, my, var4, var5 + 88, 110, 28);
      GuiRenderer.a(ctx, (float)var4, (float)(var5 + 88), 110.0F, 28.0F, 7.0F, var7 ? ACCENT2 : SURFACE2, false);
      GuiRenderer.a(ctx, (float)var4, (float)(var5 + 88), 110.0F, 28.0F, 7.0F, 1.0F, var7 ? ACCENT : BORDER, false);
      this.tc(ctx, "Create", var4 + 110 / 2, var5 + 97, -1117441);
   }

   private void renderImportPopup(DrawContext ctx, int mx, int my) {
      int var4 = this.px + 170 - 380 / 2;
      int var5 = this.py + 190 - 130 / 2;
      GuiRenderer.a(ctx, (float)var4, (float)var5, 380.0F, 130.0F, 12.0F, BG, false);
      GuiRenderer.a(ctx, (float)var4, (float)var5, 380.0F, 130.0F, 12.0F, 1.0F, BORDER, false);
      this.tc(ctx, "Enter Config Code", var4 + 380 / 2, var5 + 14, -1117441);
      GuiRenderer.a(ctx, (float)(var4 + 16), (float)(var5 + 40), 348.0F, 30.0F, 6.0F, SURFACE2, false);
      GuiRenderer.a(ctx, (float)(var4 + 16), (float)(var5 + 40), 348.0F, 30.0F, 6.0F, 1.0F, this.importCode.isEmpty() ? BORDER : ACCENT, false);
      String var6 = this.importCode.isEmpty() ? "Paste code here (Ctrl+V)..." : this.importCode;
      this.t(ctx, var6, var4 + 26, var5 + 50, this.importCode.isEmpty() ? -12298906 : -1117441);
      var4 = var4 + 380 / 2 - 55;
      int var7 = this.hov(mx, my, var4, var5 + 88, 110, 28);
      GuiRenderer.a(ctx, (float)var4, (float)(var5 + 88), 110.0F, 28.0F, 7.0F, var7 ? ACCENT2 : SURFACE2, false);
      GuiRenderer.a(ctx, (float)var4, (float)(var5 + 88), 110.0F, 28.0F, 7.0F, 1.0F, var7 ? ACCENT : BORDER, false);
      this.tc(ctx, "Import", var4 + 110 / 2, var5 + 97, -1117441);
   }

   @Override
   public boolean mouseClicked(Click click, boolean doubled) {
      int var3 = (int)click.x();
      int var4 = (int)click.y();
      int var5 = this.px;
      int var6 = this.py;
      if (this.newOpen) {
         int var19 = var5 + 170 - 340 / 2;
         int var21 = var6 + 190 - 130 / 2;
         int var24 = var19 + 340 / 2 - 55;
         if (this.hov(var3, var4, var24, var21 + 88, 110, 28)) {
            this.doCreate();
            return true;
         } else if (!this.hov(var3, var4, var19, var21, 340, 130)) {
            this.newOpen = false;
            return true;
         } else {
            return true;
         }
      } else if (this.importOpen) {
         int var18 = var5 + 170 - 380 / 2;
         int var20 = var6 + 190 - 130 / 2;
         int var23 = var18 + 380 / 2 - 55;
         if (this.hov(var3, var4, var23, var20 + 88, 110, 28)) {
            this.doImport();
            return true;
         } else if (!this.hov(var3, var4, var18, var20, 380, 130)) {
            this.importOpen = false;
            return true;
         } else {
            return true;
         }
      } else if (this.hov(var3, var4, var5 + 340 - 28, var6 + 14, 20, 20)) {
         this.close();
         return true;
      } else if (this.hov(var3, var4, var5 + 14 - 6, var6 + 52, this.fw("MY CONFIGS") + 12, 28)) {
         this.tab = ConfigManagerScreen.a.CONFIGS;
         return true;
      } else if (this.hov(var3, var4, var5 + 14 + 124, var6 + 52, this.fw("SHARE / IMPORT") + 12, 28)) {
         this.tab = ConfigManagerScreen.a.SHARE;
         return true;
      } else {
         if (this.tab == ConfigManagerScreen.a.CONFIGS) {
            int var7 = var6 + 100;

            for (int var8 = 0; var8 < 4; var8++) {
               int var9 = var8 + this.scroll;
               if (var9 >= this.configs.size()) {
                  break;
               }

               int var10 = var7 + var8 * 42 + 4;
               int var11 = var10 + 17 - 10;
               int var12 = var5 + 340 - 14 - 70;
               int var13 = var12 - 80;
               if (this.hov(var3, var4, var13, var11, 70, 20)) {
                  this.sel = var9;
                  this.doReload();
                  return true;
               }

               if (this.hov(var3, var4, var12, var11, 70, 20)) {
                  this.sel = var9;
                  this.doDelete();
                  return true;
               }

               if (this.hov(var3, var4, var5 + 14, var10, 312, 34)) {
                  this.sel = var9;
                  return true;
               }
            }

            int var15 = var6 + 380 - 54 + 14;
            boolean var17 = this.configs.size() >= 5;
            if (!var17 && this.hov(var3, var4, var5 + 14, var15, 110, 18)) {
               this.newOpen = true;
               this.newName = "";
               return true;
            }

            if (var17 && this.hov(var3, var4, var5 + 14, var15, 110, 18)) {
               this.setStatus("Max 5 configs!", -1096636);
               return true;
            }

            int var22 = var5 + 340 - 14 - 90;
            if (this.hov(var3, var4, var22, var15 - 4, 90, 26)) {
               this.doReset();
               return true;
            }
         } else {
            ConfigShare var14 = ConfigShare.a();
            boolean var16 = var14 != null && (var14.e || var14.f);
            int var25 = var5 + 14;
            int var26 = var6 + 127;
            if (!var16 && this.hov(var3, var4, var25, var26, 312, 36)) {
               this.doShare();
               return true;
            }

            if (!var16 && this.hov(var3, var4, var25, var26 + 36 + 8, 312, 36)) {
               this.importOpen = true;
               this.importCode = "";
               return true;
            }
         }

         return super.mouseClicked(click, doubled);
      }
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      double var9 = Math.max(0, this.configs.size() - 4);
      this.scroll = (int)Math.max(0.0, Math.min((double)var9, this.scroll - verticalAmount));
      return true;
   }

   @Override
   public boolean keyPressed(KeyInput input) {
      int var2 = input.getKeycode();
      if (this.newOpen) {
         if (var2 == 256) {
            this.newOpen = false;
            return true;
         } else if (var2 == 257) {
            this.doCreate();
            return true;
         } else if (var2 == 259 && !this.newName.isEmpty()) {
            this.newName = this.newName.substring(0, this.newName.length() - 1);
            return true;
         } else {
            return true;
         }
      } else if (this.importOpen) {
         if (var2 == 256) {
            this.importOpen = false;
            return true;
         } else if (var2 == 257) {
            this.doImport();
            return true;
         } else if (var2 == 259 && !this.importCode.isEmpty()) {
            this.importCode = this.importCode.substring(0, this.importCode.length() - 1);
            return true;
         } else if (input.isPaste()) {
            KeyInput var3 = MinecraftClient.getInstance().keyboard.getClipboard();
            if (var3 != null) {
               this.importCode = var3.trim();
            }

            return true;
         } else {
            return true;
         }
      } else if (var2 == 256) {
         this.close();
         return true;
      } else {
         return super.keyPressed(input);
      }
   }

   @Override
   public boolean charTyped(CharInput input) {
      String var2 = input.asString();
      if (this.newOpen && this.newName.length() < 24) {
         this.newName = this.newName + var2;
         return true;
      } else if (this.importOpen && this.importCode.length() < 64) {
         this.importCode = this.importCode + var2;
         return true;
      } else {
         return super.charTyped(input);
      }
   }

   private void doReload() {
      Map var1 = this.snapshotEnabled();
      if (!this.configs.isEmpty() && this.sel < this.configs.size()) {
         ModuleManager.INSTANCE.e(this.configs.get(this.sel));
      }

      ModuleManager.INSTANCE.g();
      this.syncModuleLifecycle(var1, this.snapshotEnabled());
      this.setStatus("Config loaded: " + ModuleManager.INSTANCE.method_a_2(), -14498466);
   }

   private void doDelete() {
      if (!this.configs.isEmpty()) {
         try {
            MinecraftClient var1 = MinecraftClient.getInstance();
            if (var1 != null) {
               File var3 = new File(var1.runDirectory, "water_config_" + this.configs.get(this.sel) + ".txt");
               if (var3.exists()) {
                  var3.delete();
               }
            }
         } catch (Exception var2) {
         }

         this.configs.remove(this.sel);
         if (this.sel >= this.configs.size()) {
            this.sel = Math.max(0, this.configs.size() - 1);
         }

         this.setStatus("Config deleted.", -1096636);
      }
   }

   private void doReset() {
      ModuleManager.INSTANCE.getModules().forEach(m -> {
         if (m.isEnabled()) {
            m.toggle();
         }
      });
      this.setStatus("All modules disabled!", -680437);
   }

   private void doShare() {
      ConfigShare var1 = ConfigShare.a();
      if (var1 != null) {
         if (!this.configs.isEmpty() && this.sel < this.configs.size()) {
            ModuleManager.INSTANCE.e(this.configs.get(this.sel));
         }

         ModuleManager.INSTANCE.f();
         this.setStatus("Uploading...", -680437);
         var1.a(() -> {
            if (var1.d.isEmpty()) {
               this.setStatus("Code copied: " + var1.c, -14498466);
            } else {
               this.setStatus(var1.d, -1096636);
            }
         });
      }
   }

   private void doImport() {
      if (this.importCode.trim().isEmpty()) {
         this.setStatus("Paste a code first!", -1096636);
      } else {
         ConfigShare var1 = ConfigShare.a();
         if (var1 != null) {
            this.setStatus("Loading...", -680437);
            var1.a(this.importCode.trim(), () -> {
               this.importOpen = false;
               if (var1.d.isEmpty()) {
                  this.setStatus("Config imported!", -14498466);
               } else {
                  this.setStatus(var1.d, -1096636);
               }
            });
         }
      }
   }

   private void doCreate() {
      if (this.configs.size() >= 5) {
         this.setStatus("Max 5 configs!", -1096636);
      } else if (this.newName.trim().isEmpty()) {
         this.setStatus("Enter a name first!", -1096636);
      } else {
         String var1 = this.newName.trim();
         ModuleManager.INSTANCE.e(var1);
         ModuleManager.INSTANCE.f();
         if (!this.configs.contains(var1)) {
            this.configs.add(var1);
         }

         this.newOpen = false;
         this.newName = "";
         this.sel = this.configs.indexOf(var1);
         this.setStatus("\"" + var1 + "\" saved!", -14498466);
      }
   }

   private void refreshConfigs() {
      this.configs.clear();

      try {
         MinecraftClient var1 = MinecraftClient.getInstance();
         if (var1 != null) {
            File[] var6 = var1.runDirectory.listFiles(f -> f.isFile() && f.getName().startsWith("water_config_") && f.getName().endsWith(".txt"));
            if (var6 != null) {
               for (File var4 : var6) {
                  this.configs.add(var4.getName().replace("water_config_", "").replace(".txt", ""));
               }
            }
         }
      } catch (Exception var5) {
      }

      this.sel = 0;
   }

   private void setStatus(String m, int c) {
      this.status = m;
      this.statusCol = c;
      this.statusAt = System.currentTimeMillis();
   }

   private boolean hov(int mx, int my, int x, int y, int w, int h) {
      return mx >= x && mx <= x + w && my >= y && my <= y + h;
   }

   private Map<String, Boolean> snapshotEnabled() {
      HashMap var1 = new HashMap();

      for (Module var3 : ModuleManager.INSTANCE.getModules()) {
         var1.put(var3.getName(), var3.isEnabled());
      }

      return var1;
   }

   private void syncModuleLifecycle(Map<String, Boolean> before, Map<String, Boolean> after) {
      for (Module var4 : ModuleManager.INSTANCE.getModules()) {
         boolean var5 = before.getOrDefault(var4.getName(), false);
         boolean var6 = after.getOrDefault(var4.getName(), false);
         if (var5 != var6) {
            try {
               if (var6) {
                  var4.onEnable();
               } else {
                  var4.onDisable();
               }
            } catch (Throwable var7) {
            }
         }
      }
   }

   @Override
   public boolean shouldPause() {
      return false;
   }

   @Override
   public void renderBackground(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
   }

   private static enum a {
      CONFIGS,
      SHARE;
   }
}
