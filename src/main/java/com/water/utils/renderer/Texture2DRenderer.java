package com.water.utils.renderer;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;
import java.nio.ByteBuffer;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.UniformType;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryUtil;

public class Texture2DRenderer {
   private static RenderPipeline field_a_1;
   private static GpuBuffer field_a_2;

   public static void a() {
      if (field_a_1 == null) {
         try {
            field_a_1 = RenderPipeline.builder()
               .withLocation(Identifier.of("water", "texture"))
               .withVertexShader(Identifier.of("water", "texture_vertex"))
               .withFragmentShader(Identifier.of("water", "texture_fragment"))
               .withVertexFormat(VertexFormat.builder().build(), DrawMode.TRIANGLES)
               .withUniform("Uniforms", UniformType.UNIFORM_BUFFER)
               .withSampler("Sampler0")
               .withBlend(BlendFunction.TRANSLUCENT)
               .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
               .withCull(false)
               .build();
            field_a_2 = RenderSystem.getDevice().createBuffer(() -> "Texture2D Uniforms", 136, 128L);
         } catch (Exception var1) {
            System.err.println("[Texture2D] Failed to init: " + var1.getMessage());
            var1.printStackTrace();
         }
      }
   }

   public static void a(Matrix4f matrix, float x, float y, float size, GpuTextureView textureView, int color, float radius, float z) {
      if (field_a_1 == null) {
         a();
      }

      if (field_a_1 != null && field_a_2 != null && textureView != null) {
         float var8 = (color >> 16 & 0xFF) / 255.0F;
         float var9 = (color >> 8 & 0xFF) / 255.0F;
         float var10 = (color & 0xFF) / 255.0F;
         int var18 = (color >> 24 & 0xFF) / 255.0F;
         ByteBuffer var11 = MemoryUtil.memAlloc(128);
         var11.putFloat(matrix.m00()).putFloat(matrix.m01()).putFloat(matrix.m02()).putFloat(matrix.m03());
         var11.putFloat(matrix.m10()).putFloat(matrix.m11()).putFloat(matrix.m12()).putFloat(matrix.m13());
         var11.putFloat(matrix.m20()).putFloat(matrix.m21()).putFloat(matrix.m22()).putFloat(matrix.m23());
         var11.putFloat(matrix.m30()).putFloat(matrix.m31()).putFloat(matrix.m32()).putFloat(matrix.m33());
         var11.position(64);
         var11.putFloat(x).putFloat(y).putFloat(size).putFloat(size);
         var11.putFloat(var8).putFloat(var9).putFloat(var10).putFloat(var18);
         var11.putFloat(radius).putFloat(0.0F).putFloat(0.0F).putFloat(0.0F);
         var11.putFloat(z).putFloat(0.0F).putFloat(0.0F).putFloat(0.0F);
         var11.flip();
         Matrix4f var14 = RenderSystem.getDevice().createCommandEncoder();
         var14.writeToBuffer(field_a_2.slice(), var11);
         MemoryUtil.memFree(var11);
         float var16 = RenderSystem.getSamplerCache().get(FilterMode.LINEAR);
         float var17 = MinecraftClient.getInstance().getFramebuffer();

         try (Matrix4f var15 = var14.createRenderPass(
               () -> "Texture2D", var17.getColorAttachmentView(), OptionalInt.empty(), var17.getDepthAttachmentView(), OptionalDouble.of(1.0)
            )) {
            GuiRenderer.a(var15);
            var15.setPipeline(field_a_1);
            var15.setUniform("Uniforms", field_a_2);
            var15.bindTexture("Sampler0", textureView, var16);
            var15.draw(0, 6);
         }
      }
   }

   public static void a(Matrix4f matrix, float x, float y, float w, float h, GpuTextureView textureView, int color, float z) {
      if (field_a_1 == null) {
         a();
      }

      if (field_a_1 != null && field_a_2 != null && textureView != null) {
         float var8 = (color >> 16 & 0xFF) / 255.0F;
         float var9 = (color >> 8 & 0xFF) / 255.0F;
         float var10 = (color & 0xFF) / 255.0F;
         int var18 = (color >> 24 & 0xFF) / 255.0F;
         ByteBuffer var11 = MemoryUtil.memAlloc(128);
         var11.putFloat(matrix.m00()).putFloat(matrix.m01()).putFloat(matrix.m02()).putFloat(matrix.m03());
         var11.putFloat(matrix.m10()).putFloat(matrix.m11()).putFloat(matrix.m12()).putFloat(matrix.m13());
         var11.putFloat(matrix.m20()).putFloat(matrix.m21()).putFloat(matrix.m22()).putFloat(matrix.m23());
         var11.putFloat(matrix.m30()).putFloat(matrix.m31()).putFloat(matrix.m32()).putFloat(matrix.m33());
         var11.position(64);
         var11.putFloat(x).putFloat(y).putFloat(w).putFloat(h);
         var11.putFloat(var8).putFloat(var9).putFloat(var10).putFloat(var18);
         var11.putFloat(0.0F).putFloat(0.0F).putFloat(0.0F).putFloat(0.0F);
         var11.putFloat(z).putFloat(0.0F).putFloat(0.0F).putFloat(0.0F);
         var11.flip();
         Matrix4f var14 = RenderSystem.getDevice().createCommandEncoder();
         var14.writeToBuffer(field_a_2.slice(), var11);
         MemoryUtil.memFree(var11);
         float var16 = RenderSystem.getSamplerCache().get(FilterMode.LINEAR);
         float var17 = MinecraftClient.getInstance().getFramebuffer();

         try (Matrix4f var15 = var14.createRenderPass(
               () -> "AwtFont", var17.getColorAttachmentView(), OptionalInt.empty(), var17.getDepthAttachmentView(), OptionalDouble.of(1.0)
            )) {
            GuiRenderer.a(var15);
            var15.setPipeline(field_a_1);
            var15.setUniform("Uniforms", field_a_2);
            var15.bindTexture("Sampler0", textureView, var16);
            var15.draw(0, 6);
         }
      }
   }
}
