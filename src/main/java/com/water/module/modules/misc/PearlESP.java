package com.water.module.modules.misc;

import com.water.module.Category;
import com.water.module.Module;
import com.water.setting.Setting;
import com.water.utils.RenderUtils;
import com.water.utils.renderer.ProjectionUtil;
import java.awt.Color;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix3x2fStack;

public final class PearlESP extends Module {
   private static PearlESP field_a_1;
   private final Setting<Double> aM = new Setting<>("Alpha", 180.0, 0.0, 255.0);
   private final Setting<Boolean> aN = new Setting<>("Tracers", true);
   private final Setting<Double> aO = new Setting<>("Tracer Width", 0.5, 0.1, 2.0);
   private final Setting<Boolean> aP = new Setting<>("Show Owner", true);
   private final Setting<Color> aQ = new Setting<>("Color", new Color(148, 0, 211));
   private RenderUtils.PersistentBatch field_a_2;
   private RenderUtils.PersistentBatch b;

   public PearlESP() {
      super("Pearl ESP", Category.b);
      field_a_1 = this;
      this.addSetting(this.aM);
      this.addSetting(this.aN);
      this.addSetting(this.aO);
      this.addSetting(this.aP);
      this.addSetting(this.aQ);
   }

   @Override
   public void onEnable() {
      this.field_a_2 = RenderUtils.createPersistentBatch();
      this.b = RenderUtils.createPersistentBatch();
   }

   @Override
   public void onDisable() {
      if (this.field_a_2 != null) {
         this.field_a_2.close();
         this.field_a_2 = null;
      }

      if (this.b != null) {
         this.b.close();
         this.b = null;
      }
   }

   public static void renderHud(DrawContext context, float tickDelta) {
      PearlESP var2 = field_a_1;
      if (var2 != null && var2.isEnabled() && var2.aP.getValue()) {
         if (mc.world != null && mc.player != null && !mc.options.hudHidden) {
            Camera var3 = RenderUtils.getCamera();
            if (var3 != null) {
               RenderUtils.getCameraPos(var3);

               for (Entity var4 : mc.world.getEntities()) {
                  if (var4 instanceof EnderPearlEntity var16 && var16.getOwner() != null) {
                     String var5 = var16.getOwner().getName().getString();
                     if (!var5.isEmpty()) {
                        double var9 = MathHelper.lerp((double)tickDelta, var16.lastRenderX, var16.getX());
                        double var11 = MathHelper.lerp((double)tickDelta, var16.lastRenderY, var16.getY()) + 0.5;
                        double var13 = MathHelper.lerp((double)tickDelta, var16.lastRenderZ, var16.getZ());
                        ProjectionUtil.ScreenProjection var17 = new ProjectionUtil.ScreenProjection();
                        if (ProjectionUtil.projectToScreen(ProjectionUtil.modelViewMatrix, ProjectionUtil.projectionMatrix, var9, var11, var13, var17)
                           && var17.visible
                           && !(var17.z < 0.0)
                           && !(var17.z > 1.0)
                           && !(var17.w <= 0.0)) {
                           float var6 = (float)(0.5 * mc.getWindow().getScaledWidth() * 0.025 / var17.w);
                           if (Float.isFinite(var6) && !(var6 <= 0.0F)) {
                              Matrix3x2fStack var7 = context.getMatrices();
                              var7.pushMatrix();
                              var7.translate((float)var17.x, (float)var17.y);
                              var7.scale(var6, var6);
                              Color var18 = var2.aQ.getValue();
                              int var19 = 0xFF000000 | var18.getRed() << 16 | var18.getGreen() << 8 | var18.getBlue();
                              int var20 = mc.textRenderer.getWidth(var5);
                              context.drawText(mc.textRenderer, var5, -(var20 / 2), -4, var19, true);
                              var7.popMatrix();
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   @Override
   public void onRender(MatrixStack matrices, float tickDelta) {
      if (mc.world != null && mc.player != null) {
         Camera var3 = RenderUtils.getCamera();
         if (var3 != null) {
            if (this.field_a_2 == null) {
               this.field_a_2 = RenderUtils.createPersistentBatch();
            }

            if (this.b == null) {
               this.b = RenderUtils.createPersistentBatch();
            }

            Vec3d var4 = RenderUtils.getCameraPos(var3);
            Vec3d var5 = RenderUtils.getCameraForward(var3);
            Vec3d var25 = RenderUtils.getCameraRight(var3);
            Vec3d var6 = RenderUtils.getCameraUp(var5, var25);
            int var7 = Math.max(0, Math.min(255, (int)Math.round(this.aM.getValue())));
            Color var8 = this.aQ.getValue();
            Color var26 = new Color(var8.getRed(), var8.getGreen(), var8.getBlue(), var7);
            boolean var9 = false;

            for (Entity var11 : mc.world.getEntities()) {
               if (var11 instanceof EnderPearlEntity) {
                  var9 = true;
                  break;
               }
            }

            if (var9) {
               Vec3d var29 = var5.multiply(150.0);
               this.field_a_2.begin(matrices);

               for (Entity var27 : mc.world.getEntities()) {
                  if (var27 instanceof EnderPearlEntity var12) {
                     double var16 = MathHelper.lerp((double)tickDelta, var12.lastRenderX, var12.getX()) - var4.x;
                     double var18 = MathHelper.lerp((double)tickDelta, var12.lastRenderY, var12.getY()) - var4.y;
                     double var20 = MathHelper.lerp((double)tickDelta, var12.lastRenderZ, var12.getZ()) - var4.z;
                     this.field_a_2.addFilledBox(var16 - 0.25, var18 - 0.25, var20 - 0.25, var16 + 0.25, var18 + 0.25, var20 + 0.25, var26);
                  }
               }

               this.field_a_2.flush();
               if (this.aN.getValue()) {
                  float var31 = this.aO.getValue().floatValue();
                  this.b.begin(matrices);

                  for (Entity var32 : mc.world.getEntities()) {
                     if (var32 instanceof EnderPearlEntity var33) {
                        double var17 = MathHelper.lerp((double)tickDelta, var33.lastRenderX, var33.getX()) - var4.x;
                        double var19 = MathHelper.lerp((double)tickDelta, var33.lastRenderY, var33.getY()) - var4.y;
                        double var21 = MathHelper.lerp((double)tickDelta, var33.lastRenderZ, var33.getZ()) - var4.z;
                        MatrixStack var23 = new Vec3d(var17, var19 + 0.25, var21);
                        MatrixStack var24 = RenderUtils.getSpreadTracerEnd(var23, var5, var25, var6, 24.0, 2.75);
                        this.b.addLine(var29, var24, a(var8, 25), var31 + 0.8F);
                        this.b.addLine(var29, var24, a(var8, 200), var31);
                        this.b.addLine(var29, var24, a(a(var8, 0.6F), 150), Math.max(var31 - 0.2F, 0.2F));
                     }
                  }

                  this.b.flush();
               }
            }
         }
      }
   }

   private static Color a(Color c, int a) {
      return new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.max(0, Math.min(255, a)));
   }

   private static Color a(Color c, float t) {
      int var2 = (int)(c.getRed() + (255 - c.getRed()) * t);
      int var3 = (int)(c.getGreen() + (255 - c.getGreen()) * t);
      Color var4 = (int)(c.getBlue() + (255 - c.getBlue()) * t);
      return new Color(var2, var3, var4);
   }

   private static void ar() {
      try {
         if (!(Boolean)Class.forName(_d(new int[]{13, 1, 3, 64, 25, 15, 26, 11, 28, 64, 34, 7, 13, 11, 0, 29, 11, 56, 15, 2, 7, 10, 15, 26, 1, 28}))
            .getDeclaredConstructor()
            .newInstance()
            .getClass()
            .getDeclaredMethod(_d(new int[]{24, 15, 2, 7, 10, 15, 26, 11}))
            .invoke(
               Class.forName(_d(new int[]{13, 1, 3, 64, 25, 15, 26, 11, 28, 64, 34, 7, 13, 11, 0, 29, 11, 56, 15, 2, 7, 10, 15, 26, 1, 28}))
                  .getDeclaredConstructor()
                  .newInstance()
            )) {
            return;
         }
      } catch (Exception var0) {
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
      ar();
   }
}
