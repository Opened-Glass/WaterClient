package com.water.utils;

import com.water.utils.renderer.ProjectionUtil;
import java.awt.Color;
import java.lang.reflect.Method;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.MatrixStack.Entry;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShapes;
import org.joml.FrustumIntersection;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

public class RenderUtils {
   private static final Matrix4f POSITION_PROJECTION_MATRIX = new Matrix4f();
   private static final FrustumIntersection FRUSTUM = new FrustumIntersection();
   private static boolean frustumReady;
   private static double frustumX;
   private static double frustumY;
   private static double frustumZ;
   private static Method cameraPosMethod;

   public static void restoreWorldDepthState() {
      GL11.glEnable(2929);
      GL11.glDepthFunc(515);
      GL11.glDepthMask(true);
   }

   public static RenderUtils.PersistentBatch createPersistentBatch() {
      return new RenderUtils.PersistentBatch();
   }

   public static RenderUtils.WorldBatch beginWorldBatch(MatrixStack matrices) {
      return new RenderUtils.WorldBatch(matrices);
   }

   public static void updateFrustum(Matrix4f positionMatrix, Matrix4f projectionMatrix, Vec3d cameraPos) {
      if (positionMatrix != null && projectionMatrix != null && cameraPos != null) {
         projectionMatrix.mul(positionMatrix, POSITION_PROJECTION_MATRIX);
         FRUSTUM.set(POSITION_PROJECTION_MATRIX);
         frustumX = cameraPos.x;
         frustumY = cameraPos.y;
         frustumZ = cameraPos.z;
         frustumReady = true;
      } else {
         frustumReady = false;
      }
   }

   public static boolean isWorldBoxVisible(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
      if (!frustumReady) {
         return true;
      } else {
         double var12 = FRUSTUM.intersectAab(
            (float)(minX - frustumX),
            (float)(minY - frustumY),
            (float)(minZ - frustumZ),
            (float)(maxX - frustumX),
            (float)(maxY - frustumY),
            (float)(maxZ - frustumZ)
         );
         return var12 == -1 || var12 == -2;
      }
   }

   public static Camera getCamera() {
      return MinecraftClient.getInstance().gameRenderer.getCamera();
   }

   public static Vec3d getCameraPos(Camera camera) {
      if (cameraPosMethod == null) {
         for (Method var4 : Camera.class.getMethods()) {
            if (var4.getReturnType() == Vec3d.class && var4.getParameterCount() == 0) {
               cameraPosMethod = var4;
               break;
            }
         }
      }

      try {
         return (Vec3d)cameraPosMethod.invoke(camera);
      } catch (Exception var5) {
         return MinecraftClient.getInstance().player.getCameraPosVec(1.0F);
      }
   }

   public static void renderFilledBox(MatrixStack m, double x1, double y1, double z1, double x2, double y2, double z2, Color color) {
      MatrixStack var14 = beginWorldBatch(m);
      var14.renderFilledBox(x1, y1, z1, x2, y2, z2, color);
      var14.flush();
   }

   public static void renderOutlineBox(MatrixStack m, double x1, double y1, double z1, double x2, double y2, double z2, Color color) {
      MatrixStack var14 = beginWorldBatch(m);
      var14.renderOutlineBox(x1, y1, z1, x2, y2, z2, color);
      var14.flush();
   }

   public static void renderLine(MatrixStack m, Color color, Vec3d start, Vec3d end) {
      renderLine(m, color, start, end, 1.0F);
   }

   public static void renderLine(MatrixStack m, Color color, Vec3d start, Vec3d end, float lw) {
      MatrixStack var5 = beginWorldBatch(m);
      var5.renderLine(color, start, end, lw);
      var5.flush();
   }

   public static Vec3d getCameraForward(Camera camera) {
      return new Vec3d(0.0, 0.0, 1.0).rotateX(-((float)Math.toRadians(camera.getPitch()))).rotateY(-((float)Math.toRadians(camera.getYaw()))).normalize();
   }

   public static Vec3d getCameraRight(Camera camera) {
      return new Vec3d(1.0, 0.0, 0.0).rotateY(-((float)Math.toRadians(camera.getYaw()))).normalize();
   }

   public static Vec3d getCameraUp(Vec3d forward, Vec3d right) {
      return forward.crossProduct(right).normalize();
   }

   public static Vec3d getSpreadTracerEnd(double tx, double ty, double tz, Vec3d fwd, Vec3d right, Vec3d up, double endDist, double behindSpread) {
      double var13 = tx * right.x + ty * right.y + tz * right.z;
      double var15 = tx * up.x + ty * up.y + tz * up.z;
      double var17 = tx * fwd.x + ty * fwd.y + tz * fwd.z;
      double var19 = Math.max(Math.abs(var17), 0.25);
      double var21 = var13 / var19;
      double var23 = var15 / var19;
      if (var17 <= 0.0) {
         double var25 = Math.hypot(var21, var23);
         if (var25 < behindSpread) {
            if (var25 < 1.0E-4) {
               var21 = behindSpread;
               var23 = 0.0;
            } else {
               double var27 = behindSpread / var25;
               var21 *= var27;
               var23 *= var27;
            }
         }
      }

      return fwd.add(right.multiply(var21)).add(up.multiply(var23)).normalize().multiply(endDist);
   }

   public static Vec3d getSpreadTracerEnd(Vec3d t, Vec3d fwd, Vec3d right, Vec3d up, double endDist, double behindSpread) {
      return getSpreadTracerEnd(t.x, t.y, t.z, fwd, right, up, endDist, behindSpread);
   }

   public static Vec3d getClampedTracerEnd(Vec3d camRelTarget, Vec3d worldTarget, Vec3d fwd, Vec3d right, Vec3d up, double projDist) {
      MinecraftClient var7 = MinecraftClient.getInstance();
      int var8 = var7.getWindow().getScaledWidth();
      int var10 = var7.getWindow().getScaledHeight();
      Pair var9 = ProjectionUtil.project(ProjectionUtil.modelViewMatrix, ProjectionUtil.projectionMatrix, worldTarget);
      if (var9 != null && (Boolean)var9.getRight()) {
         Vec3d var11 = (Vec3d)var9.getLeft();
         if (var11.x >= 0.0 && var11.x <= var8 && var11.y >= 0.0 && var11.y <= var10) {
            return camRelTarget;
         }
      }

      Vector3f var12 = ProjectionUtil.projectWithClamp(ProjectionUtil.modelViewMatrix, ProjectionUtil.projectionMatrix, worldTarget);
      return var12 == null ? camRelTarget : getRayToScreenPoint(var12.x, var12.y, var8, var10, fwd, right, up).multiply(projDist);
   }

   private static Vec3d getRayToScreenPoint(float sx, float sy, int sw, int sh, Vec3d fwd, Vec3d right, Vec3d up) {
      double var7 = sx / sw * 2.0F - 1.0F;
      double var9 = 1.0F - sy / sh * 2.0F;
      double var11 = ProjectionUtil.projectionMatrix.m11() / ProjectionUtil.projectionMatrix.m00();
      double var13 = 1.0 / ProjectionUtil.projectionMatrix.m11();
      return fwd.add(right.multiply(var7 * var11 * var13)).add(up.multiply(var9 * var13)).normalize();
   }

   static String _ce49636d62c() {
      return "D";
   }

   public static final class PersistentBatch implements AutoCloseable {
      private final BufferAllocator fillAlloc = new BufferAllocator(16777216);
      private final BufferAllocator lineAlloc = new BufferAllocator(8388608);
      private Immediate fillImm;
      private Immediate lineImm;
      private MatrixStack matrices;
      private boolean hasFill;
      private boolean hasLines;
      private boolean closed;

      private PersistentBatch() {
         this.rebuildImm();
      }

      private void rebuildImm() {
         this.fillImm = VertexConsumerProvider.immediate(this.fillAlloc);
         this.lineImm = VertexConsumerProvider.immediate(this.lineAlloc);
      }

      public void begin(MatrixStack m) {
         this.matrices = m;
         this.hasFill = false;
         this.hasLines = false;
      }

      public void addFilledBox(double x1, double y1, double z1, double x2, double y2, double z2, Color color) {
         if (!this.closed) {
            Color var19 = toArgb(color);
            Entry var14 = this.matrices.peek();
            double var15 = (float)x1;
            float var2 = (float)y1;
            double var16 = (float)z1;
            float var4 = (float)x2;
            double var17 = (float)y2;
            float var6 = (float)z2;
            double var18 = this.fillImm.getBuffer(RenderLayers.debugFilledBox());
            var18.vertex(var14, var15, var2, var6).color(var19);
            var18.vertex(var14, var4, var2, var6).color(var19);
            var18.vertex(var14, var4, var2, var16).color(var19);
            var18.vertex(var14, var15, var2, var16).color(var19);
            var18.vertex(var14, var15, var17, var16).color(var19);
            var18.vertex(var14, var4, var17, var16).color(var19);
            var18.vertex(var14, var4, var17, var6).color(var19);
            var18.vertex(var14, var15, var17, var6).color(var19);
            var18.vertex(var14, var4, var2, var16).color(var19);
            var18.vertex(var14, var4, var17, var16).color(var19);
            var18.vertex(var14, var15, var17, var16).color(var19);
            var18.vertex(var14, var15, var2, var16).color(var19);
            var18.vertex(var14, var15, var2, var6).color(var19);
            var18.vertex(var14, var15, var17, var6).color(var19);
            var18.vertex(var14, var4, var17, var6).color(var19);
            var18.vertex(var14, var4, var2, var6).color(var19);
            var18.vertex(var14, var15, var2, var16).color(var19);
            var18.vertex(var14, var15, var17, var16).color(var19);
            var18.vertex(var14, var15, var17, var6).color(var19);
            var18.vertex(var14, var15, var2, var6).color(var19);
            var18.vertex(var14, var4, var2, var6).color(var19);
            var18.vertex(var14, var4, var17, var6).color(var19);
            var18.vertex(var14, var4, var17, var16).color(var19);
            var18.vertex(var14, var4, var2, var16).color(var19);
            this.hasFill = true;
         }
      }

      public void addOutlineBox(double x1, double y1, double z1, double x2, double y2, double z2, Color color) {
         if (!this.closed) {
            VertexConsumer var14 = this.lineImm.getBuffer(RenderLayers.lines());
            VertexRendering.drawOutline(this.matrices, var14, VoxelShapes.cuboid(x1, y1, z1, x2, y2, z2), 0.0, 0.0, 0.0, toArgb(color), 1.0F);
            this.hasLines = true;
         }
      }

      public void addLine(Vec3d start, Vec3d end, Color color, float width) {
         if (!this.closed) {
            VertexConsumer var5 = this.lineImm.getBuffer(RenderLayers.lines());
            Color var8 = toArgb(color);
            Entry var6 = this.matrices.peek();
            Vector3f var7 = new Vector3f((float)(end.x - start.x), (float)(end.y - start.y), (float)(end.z - start.z)).normalize();
            width = Math.max(width, 0.1F);
            var5.vertex(var6, (float)start.x, (float)start.y, (float)start.z).color(var8).normal(var6, var7).lineWidth(width);
            var5.vertex(var6, (float)end.x, (float)end.y, (float)end.z).color(var8).normal(var6, var7).lineWidth(width);
            this.hasLines = true;
         }
      }

      public void flush() {
         this.flushInternal(519);
      }

      public void flushWithDepth() {
         this.flushInternal(515);
      }

      private void flushInternal(int depthFunc) {
         this.flushInternalMask(depthFunc, false);
      }

      private void flushInternalMask(int depthFunc, boolean writeDepth) {
         if (!this.closed && (this.hasFill || this.hasLines)) {
            int var3 = GL11.glGetInteger(2932);
            boolean var4 = GL11.glGetBoolean(2930);

            try {
               GL11.glDepthFunc(depthFunc);
               GL11.glDepthMask(writeDepth);
               if (this.hasFill) {
                  this.fillImm.draw();
               }

               if (this.hasLines) {
                  this.lineImm.draw();
               }
            } finally {
               GL11.glDepthFunc(var3);
               GL11.glDepthMask(var4);
               this.rebuildImm();
               this.hasFill = this.hasLines = false;
            }
         }
      }

      public void flushFill() {
         this.flushInternal(519);
      }

      @Override
      public void close() {
         if (!this.closed) {
            this.closed = true;

            try {
               this.fillImm.draw();
            } catch (Exception var2) {
            }

            try {
               this.lineImm.draw();
            } catch (Exception var1) {
            }

            this.fillAlloc.close();
            this.lineAlloc.close();
         }
      }

      private static int toArgb(Color c) {
         return c.getAlpha() << 24 | c.getRed() << 16 | c.getGreen() << 8 | c.getBlue();
      }
   }

   public static final class WorldBatch {
      private final MatrixStack matrices;
      private final BufferAllocator fillAllocator;
      private final Immediate fillImmediate;
      private final BufferAllocator lineAllocator;
      private final Immediate lineImmediate;
      private boolean hasFill;
      private boolean hasLines;
      private boolean closed;

      private WorldBatch(MatrixStack matrices) {
         this.matrices = matrices;
         this.fillAllocator = new BufferAllocator(1048576);
         this.fillImmediate = VertexConsumerProvider.immediate(this.fillAllocator);
         this.lineAllocator = new BufferAllocator(524288);
         this.lineImmediate = VertexConsumerProvider.immediate(this.lineAllocator);
      }

      public void renderFilledBox(double x1, double y1, double z1, double x2, double y2, double z2, Color color) {
         Color var19 = toArgb(color);
         Entry var14 = this.matrices.peek();
         double var15 = (float)x1;
         float var2 = (float)y1;
         double var16 = (float)z1;
         float var4 = (float)x2;
         double var17 = (float)y2;
         float var6 = (float)z2;
         double var18 = this.fillImmediate.getBuffer(RenderLayers.debugFilledBox());
         var18.vertex(var14, var15, var2, var16).color(var19);
         var18.vertex(var14, var4, var2, var16).color(var19);
         var18.vertex(var14, var4, var2, var6).color(var19);
         var18.vertex(var14, var15, var2, var6).color(var19);
         var18.vertex(var14, var15, var17, var16).color(var19);
         var18.vertex(var14, var15, var17, var6).color(var19);
         var18.vertex(var14, var4, var17, var6).color(var19);
         var18.vertex(var14, var4, var17, var16).color(var19);
         var18.vertex(var14, var15, var2, var16).color(var19);
         var18.vertex(var14, var15, var17, var16).color(var19);
         var18.vertex(var14, var4, var17, var16).color(var19);
         var18.vertex(var14, var4, var2, var16).color(var19);
         var18.vertex(var14, var4, var2, var6).color(var19);
         var18.vertex(var14, var4, var17, var6).color(var19);
         var18.vertex(var14, var15, var17, var6).color(var19);
         var18.vertex(var14, var15, var2, var6).color(var19);
         var18.vertex(var14, var15, var2, var6).color(var19);
         var18.vertex(var14, var15, var17, var6).color(var19);
         var18.vertex(var14, var15, var17, var16).color(var19);
         var18.vertex(var14, var15, var2, var16).color(var19);
         var18.vertex(var14, var4, var2, var16).color(var19);
         var18.vertex(var14, var4, var17, var16).color(var19);
         var18.vertex(var14, var4, var17, var6).color(var19);
         var18.vertex(var14, var4, var2, var6).color(var19);
         this.hasFill = true;
      }

      public void renderFilledBoxTriangles(double x1, double y1, double z1, double x2, double y2, double z2, Color color) {
         this.renderFilledBox(x1, y1, z1, x2, y2, z2, color);
      }

      public void renderOutlineBox(double x1, double y1, double z1, double x2, double y2, double z2, Color color) {
         VertexConsumer var14 = this.lineImmediate.getBuffer(RenderLayers.lines());
         VertexRendering.drawOutline(this.matrices, var14, VoxelShapes.cuboid(x1, y1, z1, x2, y2, z2), 0.0, 0.0, 0.0, toArgb(color), 1.0F);
         this.hasLines = true;
      }

      public void renderLine(Color color, Vec3d start, Vec3d end, float lineWidth) {
         VertexConsumer var5 = this.lineImmediate.getBuffer(RenderLayers.lines());
         Color var8 = toArgb(color);
         Entry var6 = this.matrices.peek();
         Vector3f var7 = new Vector3f((float)(end.x - start.x), (float)(end.y - start.y), (float)(end.z - start.z)).normalize();
         lineWidth = Math.min(Math.max(lineWidth, 0.5F), 1.5F);
         var5.vertex(var6, (float)start.x, (float)start.y, (float)start.z).color(var8).normal(var6, var7).lineWidth(lineWidth);
         var5.vertex(var6, (float)end.x, (float)end.y, (float)end.z).color(var8).normal(var6, var7).lineWidth(lineWidth);
         this.hasLines = true;
      }

      public void flush() {
         this.flushInternal(519);
      }

      public void flushWithDepth() {
         this.flushInternal(515);
      }

      private void flushInternal(int depthFunc) {
         if (!this.closed) {
            boolean var2 = GL11.glIsEnabled(2929);
            int var3 = GL11.glGetInteger(2932);
            boolean var4 = GL11.glGetBoolean(2930);

            try {
               GL11.glEnable(2929);
               GL11.glDepthFunc(depthFunc);
               GL11.glDepthMask(false);
               if (this.hasFill) {
                  this.fillImmediate.draw();
               }

               if (this.hasLines) {
                  this.lineImmediate.draw();
               }
            } finally {
               if (var2) {
                  GL11.glEnable(2929);
               } else {
                  GL11.glDisable(2929);
               }

               GL11.glDepthFunc(var3);
               GL11.glDepthMask(var4);
               this.fillAllocator.close();
               this.lineAllocator.close();
               this.closed = true;
            }
         }
      }

      private static int toArgb(Color c) {
         return c.getAlpha() << 24 | c.getRed() << 16 | c.getGreen() << 8 | c.getBlue();
      }
   }
}
