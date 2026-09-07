package com.water.module.modules.render;

import com.water.module.Category;
import com.water.module.Module;
import com.water.setting.Setting;
import com.water.utils.RenderUtils;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;

public final class JumpCircles extends Module {
   private final Setting<Float> bN = new Setting<>("Lifetime (s)", 1.5F, 0.1F, 5.0F);
   private final Setting<Float> bO = new Setting<>("Start Radius", 0.55F, 0.1F, 3.0F);
   private final Setting<Float> bP = new Setting<>("End Radius", 1.8F, 0.2F, 6.0F);
   private final Setting<Float> bQ = new Setting<>("Line Width", 2.0F, 0.5F, 6.0F);
   private final Setting<Color> bR = new Setting<>("Color", new Color(120, 220, 255, 255));
   private final Setting<Boolean> bS = new Setting<>("Glow Mode", false);
   private final Setting<Boolean> bT = new Setting<>("Glow Filled", false);
   private final List<JumpCircles.a> f = new ArrayList<>();
   private boolean am = true;
   private double j = 0.0;
   private double k = 0.0;
   private double l = 0.0;

   public JumpCircles() {
      super("JumpCircles", Category.b);
      this.addSetting(this.bN);
      this.addSetting(this.bO);
      this.addSetting(this.bP);
      this.addSetting(this.bQ);
      this.addSetting(this.bR);
      this.addSetting(this.bS);
      this.addSetting(this.bT);
   }

   @Override
   public void onEnable() {
      this.f.clear();
      this.am = true;
   }

   @Override
   public void onDisable() {
      this.f.clear();
   }

   @Override
   public void onTick() {
      if (mc.player != null) {
         boolean var1 = mc.player.isOnGround();
         if (var1) {
            this.j = mc.player.getX();
            this.k = mc.player.getY();
            this.l = mc.player.getZ();
         }

         if (this.am && !var1 && mc.player.getVelocity().y > 0.0) {
            this.f.add(new JumpCircles.a(this.j, this.k + 0.02, this.l, System.currentTimeMillis()));
         }

         this.am = var1;
         long var2 = System.currentTimeMillis();
         long var4 = (long)(this.bN.getValue() * 1000.0F);
         Iterator var6 = this.f.iterator();

         while (var6.hasNext()) {
            if (var2 - ((JumpCircles.a)var6.next()).z >= var4) {
               var6.remove();
            }
         }
      }
   }

   @Override
   public void onRender(MatrixStack matrices, float tickDelta) {
      if (mc.world != null && mc.player != null && !this.f.isEmpty()) {
         float var48 = RenderUtils.getCamera();
         if (var48 != null) {
            float var49 = RenderUtils.getCameraPos(var48);
            long var5 = System.currentTimeMillis();
            long var7 = (long)(this.bN.getValue() * 1000.0F);
            double var9 = this.bO.getValue().floatValue();
            double var11 = this.bP.getValue().floatValue();
            float var3 = this.bQ.getValue();
            Color var4 = this.bR.getValue();
            int var13 = var4.getAlpha();
            matrices.push();
            RenderUtils.WorldBatch var14 = RenderUtils.beginWorldBatch(matrices);

            for (JumpCircles.a var16 : this.f) {
               float var17 = (float)(var5 - var16.z) / (float)var7;
               if (var17 < 0.0F) {
                  var17 = 0.0F;
               }

               if (var17 > 1.0F) {
                  var17 = 1.0F;
               }

               var17 = 1.0F - (1.0F - var17) * (1.0F - var17) * (1.0F - var17);
               double var21 = var9 + (var11 - var9) * var17;
               int var52 = (int)(var13 * (1.0F - var17));
               if (var52 > 0) {
                  Color var18 = new Color(var4.getRed(), var4.getGreen(), var4.getBlue(), var52);
                  double var25 = var16.m - var49.x;
                  double var27 = var16.n - var49.y;
                  double var29 = var16.o - var49.z;
                  Vec3d[] var50 = new Vec3d[129];

                  for (int var19 = 0; var19 <= 128; var19++) {
                     double var33 = (Math.PI * 2) * (var19 / 128.0);
                     var50[var19] = new Vec3d(var25 + Math.cos(var33) * var21, var27, var29 + Math.sin(var33) * var21);
                  }

                  if (this.bT.getValue()) {
                     int var54 = var4.getRed();
                     int var60 = var4.getGreen();
                     int var34 = var4.getBlue();
                     float var20 = var3 * 8.0F;

                     for (int var23 = 1; var23 <= 32; var23++) {
                        double var38 = var23 / 32.0;
                        double var40 = var21 * (1.0 - var38);
                        if (!(var40 < 0.05)) {
                           int var24 = Math.max(1, (int)(var52 * (1.0 - var38 * 0.6) * 0.55));
                           Color var58 = new Color(var54, var60, var34, var24);
                           Vec3d[] var31 = new Vec3d[129];

                           for (int var32 = 0; var32 <= 128; var32++) {
                              double var46 = (Math.PI * 2) * (var32 / 128.0);
                              var31[var32] = new Vec3d(var25 + Math.cos(var46) * var40, var27, var29 + Math.sin(var46) * var40);
                           }

                           a(var14, var31, var58, var20);
                        }
                     }
                  }

                  if (this.bS.getValue()) {
                     int var55 = var4.getRed();
                     int var61 = var4.getGreen();
                     int var62 = var4.getBlue();
                     var18 = new Color(var55, var61, var62, Math.max(1, var52 / 16));
                     Color var56 = new Color(var55, var61, var62, Math.max(1, var52 / 12));
                     Color var57 = new Color(var55, var61, var62, Math.max(1, var52 / 9));
                     Color var63 = new Color(var55, var61, var62, Math.max(1, var52 / 6));
                     Color var39 = new Color(var55, var61, var62, Math.max(1, var52 / 4));
                     Color var64 = new Color(var55, var61, var62, Math.max(1, var52 / 2));
                     Color var41 = new Color(var55, var61, var62, Math.min(255, (int)(var52 * 1.0F)));
                     Color var59 = new Color(255, 255, 255, Math.min(255, (int)(var52 * 1.4F)));
                     a(var14, var50, var18, var3 * 18.0F);
                     a(var14, var50, var56, var3 * 14.0F);
                     a(var14, var50, var57, var3 * 11.0F);
                     a(var14, var50, var63, var3 * 8.5F);
                     a(var14, var50, var39, var3 * 6.0F);
                     a(var14, var50, var64, var3 * 4.0F);
                     a(var14, var50, var41, var3 * 2.8F);
                     a(var14, var50, var59, var3 * 1.6F);
                  } else {
                     a(var14, var50, var18, var3);
                  }
               }
            }

            var14.flush();
            matrices.pop();
         }
      }
   }

   private static void a(RenderUtils.WorldBatch batch, Vec3d[] points, Color col, float width) {
      for (int var4 = 1; var4 < points.length; var4++) {
         batch.renderLine(col, points[var4 - 1], points[var4], width);
      }
   }

   private static final class a {
      final double m;
      final double n;
      final double o;
      final long z;

      a(double x, double y, double z, long bornMs) {
         this.m = x;
         this.n = y;
         this.o = z;
         this.z = bornMs;
      }
   }
}
