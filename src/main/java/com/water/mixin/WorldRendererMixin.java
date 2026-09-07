package com.water.mixin;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.water.module.ModuleManager;
import com.water.module.modules.render.NoRender;
import com.water.utils.RenderUtils;
import com.water.utils.renderer.ProjectionUtil;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.FrameGraphBuilder;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.ObjectAllocator;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({WorldRenderer.class})
public class WorldRendererMixin {
   private static final Matrix4f capturedMatrix = new Matrix4f();
   private static boolean hasCapturedMatrix;
   private static float capturedTickDelta = 1.0F;

   @Inject(
      method = {"render"},
      at = {@At("HEAD")}
   )
   private void captureRenderState(
      ObjectAllocator var1,
      RenderTickCounter var2,
      boolean var3,
      Camera var4,
      Matrix4f var5,
      Matrix4f var6,
      Matrix4f var7,
      GpuBufferSlice var8,
      Vector4f var9,
      boolean var10,
      CallbackInfo var11
   ) {
      capturedMatrix.set(var5);
      ProjectionUtil.modelViewMatrix.set(var5);
      ProjectionUtil.positionMatrix.set(var5);
      ProjectionUtil.projectionMatrix.set(var6);
      RenderUtils.updateFrustum(var5, var6, var4.getCameraPos());
      hasCapturedMatrix = true;
      capturedTickDelta = var2.getTickProgress(false);
   }

   @Inject(
      method = {"render"},
      at = {@At("RETURN")}
   )
   private void onRender(CallbackInfo var1) {
      if (hasCapturedMatrix) {
         MatrixStack var2 = new MatrixStack();
         var2.multiplyPositionMatrix(capturedMatrix);

         try {
            ModuleManager.INSTANCE.onRender(var2, capturedTickDelta);
         } finally {
            RenderUtils.restoreWorldDepthState();
         }
      }
   }

   @Inject(
      method = {"renderWeather"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void water$skipWeatherPass(FrameGraphBuilder var1, GpuBufferSlice var2, CallbackInfo var3) {
      if (NoRender.hideAllPrecipitation()) {
         var3.cancel();
      }
   }
}
