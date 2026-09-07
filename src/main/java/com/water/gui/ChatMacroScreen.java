package com.water.gui;

import com.water.module.Module;
import com.water.module.ModuleManager;
import com.water.module.modules.client.WaterPlus;
import com.water.module.modules.misc.ChatMacro;
import com.water.setting.Setting;
import com.water.utils.renderer.GuiRenderer;
import com.water.utils.renderer.WaterFontRenderer;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class ChatMacroScreen extends Screen {
   private static final int W = 420;
   private static final int H = 380;
   private static final int PAD = 12;
   private static final int HEAD_H = 36;
   private static final int FOOT_H = 44;
   private static final int ROW_H = 52;
   private static final int ROW_GAP = 6;
   private static final int KEY_W = 72;
   private static final float R = 14.0F;
   private static final float R_SM = 8.0F;
   private static final float R_XS = 5.0F;
   private static final int C_BD_IN = -15327184;
   private static final int C_TEXT = -2234128;
   private static final int C_TEXT_DIM = -7824982;
   private static final int C_MUTED = -12298906;
   private static final int C_RED = -2539435;
   private static final int C_GREEN = -11751558;
   private static final int C_WHITE_10 = 285212671;
   private static final int C_GRID_BG = -16183270;
   private final Screen parent;
   private final ChatMacro module;
   private long openNs = 0L;
   private final List<String[]> macros = new ArrayList<>();
   private int editingText = -1;
   private int listeningKey = -1;
   private int scroll = 0;
   private int px;
   private int py;

   public ChatMacroScreen(Screen parent) {
      super(Text.literal(""));
      this.parent = parent;
      this.module = this.find();
      this.load();
   }

   private ChatMacro find() {
      for (Module var2 : ModuleManager.INSTANCE.getModules()) {
         if (var2 instanceof ChatMacro var3) {
            return var3;
         }
      }

      return null;
   }

   private void load() {
      this.macros.clear();
      if (this.module == null) {
         this.macros.add(new String[]{"", "0"});
      } else {
         List var1 = this.module.getSettings();

         for (byte var2 = 0; var2 + 1 < var1.size(); var2 += 2) {
            String var3 = (String)((Setting)var1.get(var2)).getValue();
            String var4 = String.valueOf(((Setting)var1.get(var2 + 1)).getValue());
            this.macros.add(new String[]{var3, var4});
         }

         if (this.macros.isEmpty()) {
            this.macros.add(new String[]{"", "0"});
         }
      }
   }

   private void save() {
      if (this.module != null) {
         List var1 = this.module.getSettings();

         for (int var2 = 0; var2 < this.macros.size() && var2 * 2 + 1 < var1.size(); var2++) {
            ((Setting)var1.get(var2 * 2)).setValue(this.macros.get(var2)[0]);
            ((Setting)var1.get(var2 * 2 + 1)).setValue(this.parseKey(this.macros.get(var2)[1]));
         }
      }
   }

   private int parseKey(String s) {
      try {
         return Integer.parseInt(s);
      } catch (Exception var2) {
         return 0;
      }
   }

   private int px() {
      return (this.width - 420) / 2;
   }

   private int py() {
      return (this.height - 380) / 2;
   }

   private int listY() {
      return this.py() + 36 + 12;
   }

   private int listH() {
      return 276;
   }

   private int visRows() {
      return Math.max(1, this.listH() / 58);
   }

   private int maxScroll() {
      return Math.max(0, this.macros.size() - this.visRows());
   }

   @Override
   protected void init() {
      this.px = this.px();
      this.py = this.py();
   }

   @Override
   public void renderBackground(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
   }

   @Override
   public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
      if (this.openNs == 0L) {
         this.openNs = System.nanoTime();
      }

      float var5 = this.easeOut(Math.min(1.0F, (float)(System.nanoTime() - this.openNs) / 1.6E8F));
      this.px = this.px();
      this.py = this.py();
      int var6 = WaterPlus.getAccentARGB();
      int var7 = WaterPlus.getBackgroundARGB();
      float var8 = WaterPlus.getGuiRoundness();
      float var9 = Math.max(5.0F, var8 * 0.6F);
      float var10 = Math.max(4.0F, var8 * 0.4F);
      float var11 = WaterPlus.getGlassIntensity();
      GuiRenderer.a(
         context, (float)(this.px - 4), (float)(this.py - 4), 428.0F, 388.0F, var8 + 3.0F, this.as(gc(var6 & 16777215, (int)(25.0F * var5)), 1.0F), false
      );
      GuiRenderer.a(context, (float)this.px, (float)this.py, 420.0F, 380.0F, var8, this.as(var7, var5), false);
      if (var11 > 0.01F) {
         GuiRenderer.a(context, (float)(this.px + 1), (float)(this.py + 1), 418.0F, 38.0F, var8, this.as(gc(16777215, (int)(15.0F * var11)), var5), false);
         GuiRenderer.a(context, (float)this.px, (float)this.py, 420.0F, 380.0F, var8, 1.0F, this.as(gc(16777215, (int)(50.0F * var11)), var5), false);
      } else {
         GuiRenderer.a(context, (float)this.px, (float)this.py, 420.0F, 380.0F, var8, 1.0F, this.as(var6 & 16777215 | 1426063360, var5), false);
      }

      GuiRenderer.a(context, (float)this.px, (float)this.py, 420.0F, 36.0F, var8, var8, 0.0F, 0.0F, false, this.as(gc(0, 55), var5));
      context.fill(this.px, this.py + 36, this.px + 420, this.py + 36 + 1, this.as(-15327184, var5));
      GuiRenderer.a(context, (float)this.px, (float)(this.py + 8), 3.0F, 20.0F, 1.5F, this.as(var6, var5), false);
      this.ft(context, "CHAT MACROS", this.px + 12 + 8, this.py + 11, this.as(-2234128, var5));
      this.ft(context, this.macros.size() + " macros", this.px + 12 + 8, this.py + 23, this.as(-7824982, var5));
      var7 = this.px + 420 - 12 - 60;
      int var25 = this.py + 9;
      int var12 = this.hov(mouseX, mouseY, var7, var25, 60, 18);
      GuiRenderer.a(
         context, (float)var7, (float)var25, 60.0F, 18.0F, 9.0F, this.as(var12 ? var6 & 16777215 | 855638016 : var6 & 16777215 | 402653184, var5), false
      );
      GuiRenderer.a(context, (float)var7, (float)var25, 60.0F, 18.0F, 9.0F, 1.0F, this.as(var6 & 16777215 | 1711276032, var5), false);
      this.ft(context, "+ ADD", var7 + (60 - this.fw("+ ADD")) / 2, var25 + 4, this.as(var6, var5));
      var7 = this.px + 12;
      int var26 = this.listY();
      var12 = this.visRows();
      this.scroll = Math.max(0, Math.min(this.maxScroll(), this.scroll));

      for (int var13 = 0; var13 < var12; var13++) {
         int var14 = var13 + this.scroll;
         if (var14 >= this.macros.size()) {
            break;
         }

         String[] var15 = this.macros.get(var14);
         String var16 = var15[0];
         int var17 = this.parseKey(var15[1]);
         int var18 = var26 + var13 * 58;
         boolean var35 = this.editingText == var14;
         boolean var19 = this.listeningKey == var14;
         int var20 = var35 || var19;
         int var21 = var20 ? var6 & 16777215 | 369098752 : this.as(419430399, var5);
         int var22 = var20 ? var6 & 16777215 | 1426063360 : this.as(587202559, var5);
         GuiRenderer.a(context, (float)var7, (float)var18, 396.0F, 52.0F, var9, var21, false);
         GuiRenderer.a(context, (float)var7, (float)var18, 396.0F, 52.0F, var9, 1.0F, var22, false);
         this.ft(context, "#" + (var14 + 1), var7 + 8, var18 + 6, this.as(var20 ? var6 : -12298906, var5));
         var14 = var7 + 8;
         var20 = var35 && System.currentTimeMillis() / 500L % 2L == 0L;
         String var50 = var16 + (var20 ? "|" : "");
         if (var50.isEmpty()) {
            var50 = var35 ? "|" : "";
         }

         var21 = var35 ? this.as(var6 & 16777215 | 570425344, var5) : this.as(570425344, var5);
         var22 = var35 ? this.as(var6 & 16777215 | -2013265920, var5) : this.as(872415231, var5);
         GuiRenderer.a(context, (float)(var14 - 2), (float)(var18 + 22), 280.0F, 20.0F, var10, var21, false);
         GuiRenderer.a(context, (float)(var14 - 2), (float)(var18 + 22), 280.0F, 20.0F, var10, 1.0F, var22, false);
         String var53 = "Type message or /command...";
         var22 = var16.isEmpty() && !var35 ? this.as(-12298906, var5) : this.as(-2234128, var5);
         String var36 = var16.isEmpty() && !var35 ? var53 : var50;
         if (this.fw(var36) > 272) {
            while (var36.length() > 1 && this.fw(var36) > 272) {
               var36 = var36.substring(1);
            }
         }

         this.ft(context, var36, var14 + 2, var18 + 28, var22);
         var14 = var7 + 396 - 72 - 34;
         int var37 = var18 + 22;
         this.hov(mouseX, mouseY, var14, var37, 72, 20);
         var16 = var19 ? "PRESS..." : (var17 <= 0 ? "NO KEY" : this.keyName(var17));
         var20 = var19 ? this.as(var6 & 16777215 | 1140850688, var5) : (var17 > 0 ? this.as(var6 & 16777215 | 671088640, var5) : this.as(570425344, var5));
         var21 = var19 ? this.as(var6, var5) : (var17 > 0 ? this.as(var6 & 16777215 | 1996488704, var5) : this.as(872415231, var5));
         GuiRenderer.a(context, (float)var14, (float)var37, 72.0F, 20.0F, var10, var20, false);
         GuiRenderer.a(context, (float)var14, (float)var37, 72.0F, 20.0F, var10, 1.0F, var21, false);
         this.ft(context, var16, var14 + (72 - this.fw(var16)) / 2, var37 + 5, this.as(var19 ? var6 : (var17 > 0 ? -2234128 : -12298906), var5));
         var14 = var7 + 396 - 26;
         int var38 = var18 + 22;
         boolean var42 = this.hov(mouseX, mouseY, var14, var38, 20, 20);
         GuiRenderer.a(context, (float)var14, (float)var38, 20.0F, 20.0F, var10, this.as(var42 ? 1155088469 : 584663125, var5), false);
         GuiRenderer.a(context, (float)var14, (float)var38, 20.0F, 20.0F, var10, 1.0F, this.as(-2539435, var5 * (var42 ? 0.9F : 0.4F)), false);
         this.ft(context, "✕", var14 + (20 - this.fw("✕")) / 2, var38 + 5, this.as(-2539435, var5));
      }

      if (this.macros.isEmpty()) {
         this.ft(
            context, "No macros yet — click + ADD", this.px + 210 - this.fw("No macros yet — click + ADD") / 2, this.py + 190 - 5, this.as(-12298906, var5)
         );
      }

      if (this.macros.size() > this.visRows()) {
         int var28 = this.px + 420 - 8;
         int var33 = this.listY();
         int var39 = this.listH();
         float var43 = (float)this.visRows() / this.macros.size();
         float var45 = Math.max(20.0F, var39 * var43);
         float var47 = var33 + (var39 - var45) * ((float)this.scroll / Math.max(1, this.maxScroll()));
         GuiRenderer.a(context, (float)var28, (float)var33, 4.0F, (float)var39, 2.0F, this.as(-15327184, var5), false);
         GuiRenderer.a(context, (float)var28, (float)((int)var47), 4.0F, (float)((int)var45), 2.0F, this.as(var6 & 16777215 | -1442840576, var5), false);
      }

      int var29 = this.py + 380 - 44;
      context.fill(this.px, var29, this.px + 420, var29 + 1, this.as(-15327184, var5));
      GuiRenderer.a(context, (float)this.px, (float)var29, 420.0F, 44.0F, 0.0F, 0.0F, var8, var8, false, this.as(gc(0, 50), var5));
      int var34 = var29 + 11;
      int var40 = this.px + 12;
      boolean var44 = this.hov(mouseX, mouseY, var40, var34, 80, 22);
      GuiRenderer.a(context, (float)var40, (float)var34, 80.0F, 22.0F, 11.0F, this.as(var44 ? 419430399 : 150994943, var5), false);
      GuiRenderer.a(context, (float)var40, (float)var34, 80.0F, 22.0F, 11.0F, 1.0F, this.as(-15327184, var5), false);
      this.ft(context, "CANCEL", var40 + (80 - this.fw("CANCEL")) / 2, var34 + 7, this.as(-7824982, var5));
      int var46 = this.px + 420 - 12 - 80;
      boolean var48 = this.hov(mouseX, mouseY, var46, var34, 80, 22);
      GuiRenderer.a(context, (float)var46, (float)var34, 80.0F, 22.0F, 11.0F, this.as(var48 ? var6 : var6 & 16777215 | 570425344, var5), false);
      GuiRenderer.a(context, (float)var46, (float)var34, 80.0F, 22.0F, 11.0F, 1.5F, this.as(var6, var5 * (var48 ? 1.0F : 0.5F)), false);
      this.ft(context, "SAVE", var46 + (80 - this.fw("SAVE")) / 2, var34 + 7, this.as(var48 ? -16777216 : var6, var5));
      super.render(context, mouseX, mouseY, deltaTicks);
   }

   @Override
   public boolean mouseClicked(Click click, boolean doubled) {
      int var3 = (int)click.x();
      int var4 = (int)click.y();
      click.button();
      this.px = this.px();
      this.py = this.py();
      WaterPlus.getAccentARGB();
      int var5 = this.py + 380 - 44;
      var5 += 11;
      if (this.hov(var3, var4, this.px + 12, var5, 80, 22)) {
         MinecraftClient.getInstance().setScreen(this.parent);
         return true;
      } else if (this.hov(var3, var4, this.px + 420 - 12 - 80, var5, 80, 22)) {
         this.save();
         MinecraftClient.getInstance().setScreen(this.parent);
         return true;
      } else {
         var5 = this.px + 420 - 12 - 60;
         int var6 = this.py + 9;
         if (this.hov(var3, var4, var5, var6, 60, 18)) {
            this.macros.add(new String[]{"", "0"});
            this.scroll = this.maxScroll();
            this.editingText = this.macros.size() - 1;
            this.listeningKey = -1;
            return true;
         } else {
            this.editingText = -1;
            this.listeningKey = -1;
            var5 = this.px + 12;
            var6 = this.listY();
            int var7 = this.visRows();

            for (int var8 = 0; var8 < var7; var8++) {
               int var9 = var8 + this.scroll;
               if (var9 >= this.macros.size()) {
                  break;
               }

               int var10 = var6 + var8 * 58;
               int var11 = var5 + 8;
               if (this.hov(var3, var4, var11 - 2, var10 + 22, 280, 20)) {
                  this.editingText = var9;
                  return true;
               }

               var11 = var5 + 396 - 72 - 34;
               int var12 = var10 + 22;
               if (this.hov(var3, var4, var11, var12, 72, 20)) {
                  this.listeningKey = var9;
                  return true;
               }

               var11 = var5 + 396 - 26;
               var10 += 22;
               if (this.hov(var3, var4, var11, var10, 20, 20)) {
                  this.macros.remove(var9);
                  this.scroll = Math.max(0, Math.min(this.maxScroll(), this.scroll));
                  this.editingText = -1;
                  this.listeningKey = -1;
                  return true;
               }
            }

            return super.mouseClicked(click, doubled);
         }
      }
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      this.scroll = Math.max(0, Math.min(this.maxScroll(), this.scroll + (verticalAmount > 0.0 ? -1 : 1)));
      return true;
   }

   @Override
   public boolean keyPressed(KeyInput input) {
      int var2 = input.getKeycode();
      if (this.listeningKey < 0) {
         if (this.editingText >= 0) {
            if (var2 != 256 && var2 != 257) {
               if (var2 == 259 && !this.macros.get(this.editingText)[0].isEmpty()) {
                  KeyInput var3 = this.macros.get(this.editingText)[0];
                  this.macros.get(this.editingText)[0] = var3.substring(0, var3.length() - 1);
                  return true;
               } else if (input.isPaste()) {
                  StringBuilder var10000 = new StringBuilder();
                  String[] var10002 = this.macros.get(this.editingText);
                  var10002[0] = var10000.append(var10002[0]).append(MinecraftClient.getInstance().keyboard.getClipboard().trim()).toString();
                  return true;
               } else {
                  return true;
               }
            } else {
               this.editingText = -1;
               return true;
            }
         } else if (var2 == 256) {
            MinecraftClient.getInstance().setScreen(this.parent);
            return true;
         } else {
            return super.keyPressed(input);
         }
      } else {
         if (var2 != 256 && var2 != 259) {
            this.macros.get(this.listeningKey)[1] = String.valueOf(var2);
         } else {
            this.macros.get(this.listeningKey)[1] = "0";
         }

         this.listeningKey = -1;
         return true;
      }
   }

   @Override
   public boolean charTyped(CharInput input) {
      if (this.editingText >= 0) {
         StringBuilder var10000 = new StringBuilder();
         String[] var10002 = this.macros.get(this.editingText);
         var10002[0] = var10000.append(var10002[0]).append(input.asString()).toString();
         return true;
      } else {
         return super.charTyped(input);
      }
   }

   private void ft(DrawContext ctx, String s, int x, int y, int c) {
      WaterFontRenderer.INSTANCE.a(ctx, s, x, y, c);
   }

   private int fw(String s) {
      return WaterFontRenderer.INSTANCE.method_a_2(s);
   }

   private int as(int c, float a) {
      return Math.max(0, Math.min(255, (int)((c >> 24 & 0xFF) * a))) << 24 | c & 16777215;
   }

   private static int gc(int rgb, int alpha) {
      return Math.max(0, Math.min(255, alpha)) << 24 | rgb & 16777215;
   }

   private float easeOut(float t) {
      return 1.0F - (float)Math.pow(1.0F - Math.min(1.0F, t), 3.0);
   }

   private boolean hov(int mx, int my, int x, int y, int w, int h) {
      return mx >= x && mx <= x + w && my >= y && my <= y + h;
   }

   private String keyName(int kc) {
      if (kc <= 0) {
         return "None";
      } else {
         String var2 = GLFW.glfwGetKeyName(kc, 0);
         return var2 != null && !var2.isBlank() ? var2.toUpperCase() : ClickGuiScreen.getKeyDisplayNameStatic(kc);
      }
   }

   @Override
   public boolean shouldPause() {
      return false;
   }
}
