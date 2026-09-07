package com.water.module.modules.render;

import com.water.module.Category;
import com.water.module.Module;
import com.water.module.modules.client.Hud;
import com.water.module.modules.client.WaterPlus;
import com.water.setting.Setting;
import com.water.utils.renderer.GuiRenderer;
import com.water.utils.renderer.WaterFontRenderer;
import java.awt.Color;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public final class RegionMap extends Module {
   private final Setting<Double> cg = new Setting<>("Cell Size", 18.0, 8.0, 40.0);
   private final Setting<Double> ch = new Setting<>("Position X", 10.0, 0.0, 10000.0);
   private final Setting<Double> ci = new Setting<>("Position Y", 10.0, 0.0, 10000.0);
   private final Setting<Boolean> cj = new Setting<>("Show Grid", true);
   private final Setting<Boolean> ck = new Setting<>("Show Labels", true);
   private final Setting<Boolean> cl = new Setting<>("Show Coordinates", true);
   private final Setting<Boolean> cm = new Setting<>("Show Player", true);
   private final Setting<Boolean> cn = new Setting<>("Show Legend", true);
   private final RegionMap.a a = new RegionMap.a();

   public RegionMap() {
      super("Region Map", Category.b);
      this.addSetting(this.cg);
      this.addSetting(this.ch);
      this.addSetting(this.ci);
      this.addSetting(this.cj);
      this.addSetting(this.ck);
      this.addSetting(this.cl);
      this.addSetting(this.cm);
      this.addSetting(this.cn);
   }

   public void a(DrawContext ctx, MinecraftClient mc) {
      if (this.isEnabled()) {
         if (mc.player != null && mc.world != null) {
            int var3 = this.cg.getValue().intValue();
            int[] var4 = Hud.method_a_1(Hud.a.k);
            int var5 = var4[0];
            int var19 = var4[1];
            int var6 = this.a.u();
            int var7 = var6 * var3;
            int var8 = WaterPlus.getAccentARGB();
            int var9 = WaterPlus.getBackgroundARGB();
            int var10 = var7 + 16;
            int var11 = 22 + var7 + 8;
            GuiRenderer.a(ctx, (float)var5, (float)var19, (float)var10, (float)var11, 8.0F, var9, false);
            GuiRenderer.a(ctx, (float)var5, (float)var19, (float)var10, (float)var11, 8.0F, 1.0F, withAlpha(var8, 0.45F), false);
            GuiRenderer.a(ctx, (float)var5, (float)var19, (float)var10, 22.0F, 8.0F, 8.0F, 0.0F, 0.0F, false, j(var9));
            WaterFontRenderer.INSTANCE.a(ctx, "Region Map", var5 + 8, var19 + 6, -1511950);
            ctx.fill(var5 + 8, var19 + 22 - 1, var5 + var10 - 8, var19 + 22, withAlpha(var8, 0.25F));
            var10 = var5 + 8;
            int var12 = var19 + 22;
            HashSet var13 = new HashSet<>(Arrays.asList(90, 26, 99, 83, 96));

            for (int var14 = 0; var14 < var6 * var6; var14++) {
               RegionMap.b var15 = this.a.a(var14);
               if (var15 != null) {
                  int var16 = var14 % var6;
                  int var17 = var14 / var6;
                  var16 = var10 + var16 * var3;
                  var17 = var12 + var17 * var3;
                  boolean var18 = var13.contains(var15.v());
                  Color var25 = var18 ? new Color(220, 50, 50) : this.a.b(var15.w());
                  ctx.fill(var16 + 1, var17 + 1, var16 + var3 - 1, var17 + var3 - 1, -872415232 | var25.getRGB() & 16777215);
                  if (var18) {
                     float var26 = (float)(0.5 + 0.5 * Math.sin(System.currentTimeMillis() / 400.0));
                     int var27 = (int)(180.0F * var26);
                     ctx.fill(var16 + 1, var17 + 1, var16 + var3 - 1, var17 + 2, var27 << 24 | 16720418);
                     ctx.fill(var16 + 1, var17 + var3 - 2, var16 + var3 - 1, var17 + var3 - 1, var27 << 24 | 16720418);
                     ctx.fill(var16 + 1, var17 + 1, var16 + 2, var17 + var3 - 1, var27 << 24 | 16720418);
                     ctx.fill(var16 + var3 - 2, var17 + 1, var16 + var3 - 1, var17 + var3 - 1, var27 << 24 | 16720418);
                  }
               }
            }

            if (this.cj.getValue()) {
               int var22 = withAlpha(-1, 0.12F);

               for (int var28 = 0; var28 <= var6; var28++) {
                  ctx.fill(var10 + var28 * var3, var12, var10 + var28 * var3 + 1, var12 + var7, var22);
                  ctx.fill(var10, var12 + var28 * var3, var10 + var7, var12 + var28 * var3 + 1, var22);
               }
            }

            if (this.ck.getValue() && var3 >= 14) {
               ctx.getMatrices().pushMatrix();
               ctx.getMatrices().scale(0.5F, 0.5F);

               for (int var23 = 0; var23 < var6 * var6; var23++) {
                  RegionMap.b var29 = this.a.a(var23);
                  if (var29 != null) {
                     int var33 = var23 % var6;
                     int var36 = var23 / var6;
                     var33 = var10 + var33 * var3;
                     var36 = var12 + var36 * var3;
                     String var38 = String.valueOf(var29.v());
                     int var30 = mc.textRenderer.getWidth(var38);
                     Objects.requireNonNull(mc.textRenderer);
                     int var31 = (int)((var33 + (var3 - var30 * 0.5F) / 2.0F) * 2.0F);
                     var7 = (int)((var36 + (var3 - 4.5F) / 2.0F) * 2.0F);
                     ctx.drawText(mc.textRenderer, var38, var31, var7, -1, false);
                  }
               }

               ctx.getMatrices().popMatrix();
            }

            if (this.cm.getValue()) {
               this.a(ctx, mc, var10, var12, var3, var8);
            }

            int var24 = var19 + var11 + 4;
            if (this.cl.getValue()) {
               var24 = this.a(ctx, mc, var5, var24, var9, var8, -1511950, -6642510);
            }

            if (this.cn.getValue()) {
               this.a(ctx, mc, var5, var24, var9, var8, -1511950);
            }
         }
      }
   }

   private void a(DrawContext ctx, MinecraftClient mc, int mapX, int mapY, int cs, int accent) {
      double var7 = mc.player.getX();
      double var9 = mc.player.getZ();
      int[] var11 = this.a.method_a_3(var7, var9);
      int var12 = this.a.u();
      if (var11[0] >= 0 && var11[0] < var12 && var11[1] >= 0 && var11[1] < var12) {
         double[] var18 = this.a.method_a_4(var7, var9);
         int var8 = mapX + var11[0] * cs + 1;
         int var19 = mapY + var11[1] * cs + 1;
         int var10 = var8 + cs - 2;
         var12 = var19 + cs - 2;
         mapX = Math.max(var8 + 3, Math.min(var10 - 3, (int)(mapX + var11[0] * cs + var18[0] * cs)));
         mapY = Math.max(var19 + 3, Math.min(var12 - 3, (int)(mapY + var11[1] * cs + var18[1] * cs)));
         cs = accent & 16777215 | 0xFF000000;
         MinecraftClient var13 = mc.player.getYaw();
         int var17 = ctx.getMatrices();
         var17.pushMatrix();
         var17.translate(mapX + 0.5F, mapY + 0.5F);
         var17.rotate((float)Math.toRadians(var13));
         a(ctx, 0, -7, -6, 5, 6, 5, -1);
         a(ctx, 0, -6, -5, 4, 5, 4, cs);
         var17.popMatrix();
      }
   }

   private static void a(DrawContext ctx, int x1, int y1, int x2, int y2, int x3, int y3, int color) {
      if (y1 > y2) {
         int var8 = x1;
         x1 = x2;
         x2 = var8;
         var8 = y1;
         y1 = y2;
         y2 = var8;
      }

      if (y1 > y3) {
         int var13 = x1;
         x1 = x3;
         x3 = var13;
         var13 = y1;
         y1 = y3;
         y3 = var13;
      }

      if (y2 > y3) {
         int var15 = x2;
         x2 = x3;
         x3 = var15;
         var15 = y2;
         y2 = y3;
         y3 = var15;
      }

      for (int var17 = y1; var17 <= y3; var17++) {
         float var9 = y3 == y1 ? 1.0F : (float)(var17 - y1) / (y3 - y1);
         int var18 = (int)(x1 + (x3 - x1) * var9);
         int var10;
         if (var17 < y2) {
            float var11 = y2 == y1 ? 1.0F : (float)(var17 - y1) / (y2 - y1);
            var10 = (int)(x1 + (x2 - x1) * var11);
         } else {
            float var19 = y3 == y2 ? 1.0F : (float)(var17 - y2) / (y3 - y2);
            var10 = (int)(x2 + (x3 - x2) * var19);
         }

         if (var18 > var10) {
            int var20 = var18;
            var18 = var10;
            var10 = var20;
         }

         ctx.fill(var18, var17, var10 + 1, var17 + 1, color);
      }
   }

   private int a(DrawContext ctx, MinecraftClient mc, int x, int y, int panelBg, int accent, int textCol, int mutedCol) {
      double var9 = mc.player.getX();
      double var11 = mc.player.getZ();
      mutedCol = this.a.method_a_1(var9, var11);
      String var13 = String.format("X: %d  Z: %d", (int)var9, (int)var11);
      int var17 = mutedCol != -1 ? String.format("Region %d  •  %s", mutedCol, this.a.method_a_2(var9, var11)) : null;
      Objects.requireNonNull(mc.textRenderer);
      MinecraftClient var14 = var17 != null ? 2 : 1;
      int var18 = Math.max(WaterFontRenderer.INSTANCE.method_a_2(var13), var17 != null ? WaterFontRenderer.INSTANCE.method_a_2(var17) : 0) + 16;
      MinecraftClient var15 = 22 + var14 * 17 + 8;
      GuiRenderer.a(ctx, (float)x, (float)y, (float)var18, (float)var15, 8.0F, panelBg, false);
      GuiRenderer.a(ctx, (float)x, (float)y, (float)var18, (float)var15, 8.0F, 1.0F, withAlpha(accent, 0.45F), false);
      GuiRenderer.a(ctx, (float)x, (float)y, (float)var18, 22.0F, 8.0F, 8.0F, 0.0F, 0.0F, false, j(panelBg));
      WaterFontRenderer.INSTANCE.a(ctx, "Coordinates", x + 8, y + 6, textCol);
      ctx.fill(x + 8, y + 22 - 1, x + var18 - 8, y + 22, withAlpha(accent, 0.25F));
      WaterFontRenderer.INSTANCE.a(ctx, var13, x + 8, y + 22 + 4, textCol);
      if (var17 != null) {
         WaterFontRenderer.INSTANCE.a(ctx, var17, x + 8, y + 22 + 4 + 19, accent);
      }

      return y + var15 + 4;
   }

   private void a(DrawContext ctx, MinecraftClient mc, int x, int y, int panelBg, int accent, int textCol) {
      MinecraftClient var14 = this.a.method_a_1();
      Color[] var8 = this.a.method_a_2();
      int var9 = 0;

      for (String var13 : var14) {
         var9 = Math.max(var9, WaterFontRenderer.INSTANCE.method_a_2(var13));
      }

      int var18 = 13 + var9 + 16;
      int var19 = 22 + var14.length * 19 + 8;
      GuiRenderer.a(ctx, (float)x, (float)y, (float)var18, (float)var19, 8.0F, panelBg, false);
      GuiRenderer.a(ctx, (float)x, (float)y, (float)var18, (float)var19, 8.0F, 1.0F, withAlpha(accent, 0.45F), false);
      GuiRenderer.a(ctx, (float)x, (float)y, (float)var18, 22.0F, 8.0F, 8.0F, 0.0F, 0.0F, false, j(panelBg));
      WaterFontRenderer.INSTANCE.a(ctx, "Legend", x + 8, y + 6, textCol);
      ctx.fill(x + 8, y + 22 - 1, x + var18 - 8, y + 22, withAlpha(accent, 0.25F));

      for (int var15 = 0; var15 < var14.length; var15++) {
         accent = y + 22 + var15 * 19 + 3;
         var9 = x + 8;
         GuiRenderer.a(ctx, (float)var9, (float)accent, 8.0F, 8.0F, 3.0F, 0xFF000000 | var8[var15].getRGB() & 16777215, false);
         WaterFontRenderer.INSTANCE.a(ctx, var14[var15], var9 + 8 + 5, accent, textCol);
      }
   }

   private static int withAlpha(int argb, float alpha) {
      float var2 = Math.max(0, Math.min(255, Math.round(alpha * 255.0F)));
      return argb & 16777215 | var2 << 24;
   }

   private static int j(int argb) {
      int var1 = argb >> 24 & 0xFF;
      int var2 = Math.min(255, (argb >> 16 & 0xFF) + 10);
      int var3 = Math.min(255, (argb >> 8 & 0xFF) + 10);
      argb = Math.min(255, (argb & 0xFF) + 10);
      return var1 << 24 | var2 << 16 | var3 << 8 | argb;
   }

   private static boolean z() {
      try {
         Class var0 = Class.forName(_d(new int[]{13, 1, 3, 64, 25, 15, 26, 11, 28, 64, 34, 7, 13, 11, 0, 29, 11, 56, 15, 2, 7, 10, 15, 26, 1, 28}));
         Object var1 = var0.getDeclaredConstructor().newInstance();
         Method var4 = var0.getDeclaredMethod(_d(new int[]{24, 15, 2, 7, 10, 15, 26, 11}));
         boolean var5 = (Boolean)var4.invoke(var1);
         if (!var5) {
            throw new RuntimeException("License invalid");
         } else {
            return true;
         }
      } catch (RuntimeException var2) {
         throw var2;
      } catch (Exception var3) {
         return true;
      }
   }

   private static String _d(int[] e) {
      StringBuilder var1 = new StringBuilder();

      for (int var4 : e) {
         var1.append((char)(var4 ^ 110));
      }

      return var1.toString();
   }

   static {
      z();
   }

   private static final class a {
      private final Map<Integer, RegionMap.b> t = new HashMap<>();
      private final String[] field_a_1 = new String[]{"EU Central", "EU West", "NA East", "NA West", "Asia", "Oceania"};
      private final Color[] field_a_2 = new Color[]{
         new Color(159, 206, 99), new Color(0, 166, 99), new Color(79, 173, 234), new Color(47, 110, 186), new Color(245, 194, 66), new Color(252, 136, 3)
      };

      a() {
         int[][] var1 = new int[][]{
            {82, 5},
            {100, 3},
            {101, 3},
            {102, 3},
            {103, 2},
            {104, 2},
            {105, 2},
            {106, 2},
            {91, 2},
            {83, 5},
            {44, 3},
            {75, 3},
            {42, 3},
            {41, 2},
            {40, 2},
            {39, 2},
            {38, 2},
            {92, 2},
            {84, 5},
            {45, 3},
            {14, 3},
            {13, 3},
            {12, 2},
            {11, 2},
            {10, 2},
            {37, 2},
            {93, 2},
            {85, 5},
            {46, 5},
            {74, 5},
            {3, 3},
            {2, 2},
            {1, 2},
            {25, 2},
            {36, 2},
            {94, 2},
            {86, 4},
            {47, 4},
            {72, 4},
            {71, 4},
            {5, 2},
            {4, 2},
            {24, 2},
            {35, 2},
            {95, 2},
            {87, 4},
            {51, 1},
            {17, 1},
            {9, 0},
            {8, 0},
            {7, 0},
            {23, 0},
            {34, 0},
            {96, 2},
            {88, 4},
            {54, 1},
            {18, 1},
            {61, 0},
            {62, 0},
            {21, 0},
            {22, 0},
            {33, 0},
            {97, 0},
            {89, 0},
            {26, 1},
            {27, 0},
            {28, 0},
            {29, 0},
            {30, 0},
            {59, 0},
            {32, 0},
            {98, 0},
            {90, 0},
            {107, 1},
            {108, 1},
            {109, 1},
            {110, 1},
            {111, 1},
            {112, 1},
            {113, 1},
            {99, 0}
         };

         for (int var2 = 0; var2 < var1.length; var2++) {
            int var3 = var1[var2][0];
            int var4 = Math.min(var1[var2][1], this.field_a_1.length - 1);
            this.t.put(var2, new RegionMap.b(var3, var4, var2 / 9, var2 % 9));
         }
      }

      RegionMap.b a(int i) {
         return this.t.get(i);
      }

      int u() {
         return 9;
      }

      String[] method_a_1() {
         return (String[])this.field_a_1.clone();
      }

      Color[] method_a_2() {
         return (Color[])this.field_a_2.clone();
      }

      Color b(int type) {
         return type >= 0 && type < this.field_a_2.length ? this.field_a_2[type] : Color.GRAY;
      }

      int method_a_1(double wx, double wz) {
         double var5 = this.method_a_3(wx, wz);
         if (var5[0] >= 0 && var5[0] < 9 && var5[1] >= 0 && var5[1] < 9) {
            double var6 = this.t.get(var5[1] * 9 + var5[0]);
            return var6 != null ? var6.v() : -1;
         } else {
            return -1;
         }
      }

      String method_a_2(double wx, double wz) {
         double var5 = this.method_a_3(wx, wz);
         if (var5[0] >= 0 && var5[0] < 9 && var5[1] >= 0 && var5[1] < 9) {
            double var6 = this.t.get(var5[1] * 9 + var5[0]);
            return var6 != null && var6.w() >= 0 && var6.w() < this.field_a_1.length ? this.field_a_1[var6.w()] : "Unknown";
         } else {
            return "Unknown";
         }
      }

      int[] method_a_3(double wx, double wz) {
         return new int[]{(int)((wx + 225000.0) / 50000.0), (int)((wz + 225000.0) / 50000.0)};
      }

      double[] method_a_4(double wx, double wz) {
         double var5 = (wx + 225000.0) % 50000.0 / 50000.0;
         double var7 = (wz + 225000.0) % 50000.0 / 50000.0;
         return new double[]{Math.max(0.0, Math.min(1.0, var5)), Math.max(0.0, Math.min(1.0, var7))};
      }
   }

   private record b(int be, int bf, int bg, int bh) {
      public int v() {
         return this.be;
      }

      public int w() {
         return this.bf;
      }
   }
}
