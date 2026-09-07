package com.water.utils.renderer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;

public class ProjectionUtil {
   public static final Matrix4f projectionMatrix = new Matrix4f();
   public static final Matrix4f modelViewMatrix = new Matrix4f();
   public static final Matrix4f positionMatrix = new Matrix4f();
   static MinecraftClient mc = MinecraftClient.getInstance();

   public static Vec3d worldSpaceToScreenSpace(Vec3d pos) {
      Camera var1 = mc.getEntityRenderDispatcher().camera;
      int var2 = mc.getWindow().getHeight();
      int[] var3 = new int[4];
      GL11.glGetIntegerv(2978, var3);
      Vector3f var4 = new Vector3f();
      double var5 = pos.x - var1.getCameraPos().x;
      double var7 = pos.y - var1.getCameraPos().y;
      double var9 = pos.z - var1.getCameraPos().z;
      Vec3d var11 = new Vector4f((float)var5, (float)var7, (float)var9, 1.0F).mul(positionMatrix);
      Matrix4f var12 = new Matrix4f(projectionMatrix);
      Matrix4f var13 = new Matrix4f(modelViewMatrix);
      var12.mul(var13).project(var11.x(), var11.y(), var11.z(), var3, var4);
      return new Vec3d(var4.x / mc.getWindow().getScaleFactor(), (var2 - var4.y) / mc.getWindow().getScaleFactor(), var4.z);
   }

   public static Pair<Vec3d, Boolean> project(Matrix4f modelView, Matrix4f projection, Vec3d vector) {
      if (mc.gameRenderer != null && mc.getCameraEntity() != null) {
         ProjectionUtil.ScreenProjection var3 = new ProjectionUtil.ScreenProjection();
         return !projectToScreen(modelView, projection, vector.x, vector.y, vector.z, var3)
            ? null
            : new Pair<>(new Vec3d(var3.x, var3.y, var3.z), var3.visible);
      } else {
         return null;
      }
   }

   public static boolean projectToScreen(
      Matrix4f modelView, Matrix4f projection, double worldX, double worldY, double worldZ, ProjectionUtil.ScreenProjection output
   ) {
      if (mc.gameRenderer != null && mc.getCameraEntity() != null && output != null) {
         Vec3d var9 = mc.gameRenderer.getCamera().getCameraPos();
         double var10 = worldX - var9.x;
         double var12 = worldY - var9.y;
         double var14 = worldZ - var9.z;
         double var16 = modelView.m00() * var10 + modelView.m10() * var12 + modelView.m20() * var14 + modelView.m30();
         double var18 = modelView.m01() * var10 + modelView.m11() * var12 + modelView.m21() * var14 + modelView.m31();
         double var20 = modelView.m02() * var10 + modelView.m12() * var12 + modelView.m22() * var14 + modelView.m32();
         double var22 = modelView.m03() * var10 + modelView.m13() * var12 + modelView.m23() * var14 + modelView.m33();
         double var24 = projection.m00() * var16 + projection.m10() * var18 + projection.m20() * var20 + projection.m30() * var22;
         double var26 = projection.m01() * var16 + projection.m11() * var18 + projection.m21() * var20 + projection.m31() * var22;
         double var28 = projection.m02() * var16 + projection.m12() * var18 + projection.m22() * var20 + projection.m32() * var22;
         double var30 = projection.m03() * var16 + projection.m13() * var18 + projection.m23() * var20 + projection.m33() * var22;
         Matrix4f var45 = var30 > 0.0;
         double var33 = var30 != 0.0 ? 1.0 / var30 : 0.0;
         double var35 = var24 * var33;
         double var37 = var26 * var33;
         double var39 = var28 * var33;
         double var41 = (var35 * 0.5 + 0.5) * mc.getWindow().getScaledWidth();
         double var43 = (0.5 - var37 * 0.5) * mc.getWindow().getScaledHeight();
         output.set(var41, var43, var39, var30, var45);
         return true;
      } else {
         return false;
      }
   }

   public static Vector3f projectVector(Matrix4f modelView, Matrix4f projection, Vec3d vector) {
      Matrix4f var3 = project(modelView, projection, vector);
      if (var3 == null) {
         return null;
      } else {
         Matrix4f var4 = (Vec3d)var3.getLeft();
         return new Vector3f((float)var4.x, (float)var4.y, (float)var4.z);
      }
   }

   public static Vector3f project(double x, double y, double z) {
      return mc.gameRenderer != null && mc.getCameraEntity() != null ? project(new Vec3d(x, y, z)) : null;
   }

   public static Vector3f project(Vec3d vector) {
      if (mc.gameRenderer != null && mc.getCameraEntity() != null) {
         vector = worldSpaceToScreenSpace(vector);
         return !(vector.z < 0.0) && !(vector.z > 1.0) ? new Vector3f((float)vector.x, (float)vector.y, (float)vector.z) : null;
      } else {
         return null;
      }
   }

   public static Vector3f projectWithClamp(Matrix4f modelView, Matrix4f projection, Vec3d vector) {
      if (mc.gameRenderer != null && mc.getCameraEntity() != null) {
         vector = vector.subtract(mc.gameRenderer.getCamera().getCameraPos());
         if (vector.lengthSquared() < 1.0E-4) {
            return new Vector3f(mc.getWindow().getScaledWidth() / 2.0F, mc.getWindow().getScaledHeight() / 2.0F, 0.0F);
         } else {
            Vec3d var15 = new Vector4f((float)vector.x, (float)vector.y, (float)vector.z, 1.0F);
            var15.mul(modelView);
            var15.mul(projection);
            Matrix4f var8 = var15.w() <= 0.0F;
            Matrix4f var10 = Math.abs(var15.w());
            if (var10 < 0.001F) {
               var10 = 0.001F;
            }

            float var3 = var15.x() / var10;
            Matrix4f var11 = var15.y() / var10;
            Vec3d var16 = mc.getWindow().getScaledWidth();
            float var4 = mc.getWindow().getScaledHeight();
            float var5 = var16 / 2.0F;
            float var6 = var4 / 2.0F;
            var3 = (var3 * 0.5F + 0.5F) * var16;
            Matrix4f var12 = (0.5F - var11 * 0.5F) * var4;
            if (!var8 && var3 >= 0.0F && var3 <= var16 && var12 >= 0.0F && var12 <= var4) {
               return new Vector3f(var3, var12, 0.0F);
            } else {
               Matrix4f var9 = var3 - var5;
               Matrix4f var13 = var12 - var6;
               if (var9 == 0.0F && var13 == 0.0F) {
                  var13 = 1.0F;
               }

               Vec3d var17 = var16 / 2.0F - 10.0F;
               var3 = var4 / 2.0F - 10.0F;
               var4 = Float.MAX_VALUE;
               float var7 = Float.MAX_VALUE;
               if (var9 != 0.0F) {
                  var4 = Math.abs(var17 / var9);
               }

               if (var13 != 0.0F) {
                  var7 = Math.abs(var3 / var13);
               }

               Vec3d var18 = Math.min(var4, var7);
               return new Vector3f(var5 + var9 * var18, var6 + var13 * var18, 0.0F);
            }
         }
      } else {
         return null;
      }
   }

   public static final class ScreenProjection {
      public double x;
      public double y;
      public double z;
      public double w;
      public boolean visible;

      public void set(double x, double y, double z, double w, boolean visible) {
         this.x = x;
         this.y = y;
         this.z = z;
         this.w = w;
         this.visible = visible;
      }
   }
}
