package com.water.utils.renderer;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;
import java.nio.ByteBuffer;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.GpuSampler;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gl.UniformType;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

public class Blur2DRenderer {
   private static final RenderPipeline field_b_1 = RenderPipelines.register(
      RenderPipeline.builder(RenderPipelines.TRANSFORMS_AND_PROJECTION_SNIPPET)
         .withLocation(Identifier.of("water", "pipeline/blur_pass"))
         .withVertexShader(Identifier.of("water", "blur_pass_vertex"))
         .withFragmentShader(Identifier.of("water", "blur_pass_fragment"))
         .withVertexFormat(VertexFormats.EMPTY, DrawMode.TRIANGLES)
         .withUniform("BlurData", UniformType.UNIFORM_BUFFER)
         .withSampler("Sampler0")
         .withBlend(BlendFunction.TRANSLUCENT)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .build()
   );
   private static final RenderPipeline c = RenderPipelines.register(
      RenderPipeline.builder(RenderPipelines.TRANSFORMS_AND_PROJECTION_SNIPPET)
         .withLocation(Identifier.of("water", "pipeline/blur_final"))
         .withVertexShader(Identifier.of("water", "blur_final_vertex"))
         .withFragmentShader(Identifier.of("water", "blur_final_fragment"))
         .withVertexFormat(VertexFormats.EMPTY, DrawMode.TRIANGLES)
         .withUniform("BlurData", UniformType.UNIFORM_BUFFER)
         .withSampler("Sampler0")
         .withBlend(BlendFunction.TRANSLUCENT)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .build()
   );
   private static final Vector4f field_a_1 = new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);
   private static final Vector3f field_a_2 = new Vector3f(0.0F, 0.0F, 0.0F);
   private static final Matrix4f field_a_3 = new Matrix4f();
   private static GpuBuffer field_a_4;
   private static GpuBuffer field_b_2;
   private static ByteBuffer field_a_5;
   private static final GpuBuffer[] field_a_6 = new GpuBuffer[24];
   private static final ByteBuffer[] field_a_7 = new ByteBuffer[24];
   private static int A = 0;
   private static GpuTexture field_a_8;
   private static GpuTextureView field_a_9;
   private static GpuTexture[] field_a_10 = new GpuTexture[2];
   private static GpuTextureView[] field_a_11 = new GpuTextureView[2];
   private static int B = 0;
   private static int C = 0;
   private static boolean field_b_3 = false;
   private static int D = 0;
   private static float e = 0.0F;
   private static boolean ay = true;

   public static void bm() {
      ay = true;
   }

   public static void a() {
      if (!field_b_3) {
         ByteBuffer var0 = MemoryUtil.memAlloc(4);
         var0.putInt(0);
         var0.flip();
         field_b_2 = RenderSystem.getDevice().createBuffer(() -> "water:blur_dummy_vertex", 32, var0);
         MemoryUtil.memFree(var0);
         field_b_3 = true;
      }
   }

   private static void a(int fbWidth, int fbHeight) {
      int var2 = fbWidth / 2;
      int var3 = fbHeight / 2;
      if (field_a_8 == null || fbWidth != B || fbHeight != C) {
         if (field_a_9 != null) {
            field_a_9.close();
            field_a_9 = null;
         }

         if (field_a_8 != null) {
            field_a_8.close();
            field_a_8 = null;
         }

         field_a_8 = RenderSystem.getDevice().createTexture(() -> "water:blur_copy", 5, TextureFormat.RGBA8, fbWidth, fbHeight, 1, 1);
         field_a_9 = RenderSystem.getDevice().createTextureView(field_a_8);

         for (int var4 = 0; var4 < 2; var4++) {
            if (field_a_11[var4] != null) {
               field_a_11[var4].close();
               field_a_11[var4] = null;
            }

            if (field_a_10[var4] != null) {
               field_a_10[var4].close();
               field_a_10[var4] = null;
            }

            int var5 = var4;
            field_a_10[var4] = RenderSystem.getDevice().createTexture(() -> "water:blur_pp_" + var5, 13, TextureFormat.RGBA8, var2, var3, 1, 1);
            field_a_11[var4] = RenderSystem.getDevice().createTextureView(field_a_10[var4]);
         }

         B = fbWidth;
         C = fbHeight;
      }
   }

   public static void a(Matrix4f matrix, float x, float y, float width, float height, float radius, float strength, float z) {
      MinecraftClient var8 = MinecraftClient.getInstance();
      if (var8.getFramebuffer() != null) {
         if (var8.getFramebuffer().getColorAttachment() != null) {
            a();
            int var9 = var8.getFramebuffer().textureWidth;
            int var10 = var8.getFramebuffer().textureHeight;
            int var11 = var9 / 2;
            int var12 = var10 / 2;
            a(var9, var10);
            long var10000 = System.nanoTime() / 16666666L;
            int var13 = ay || Math.abs(strength - e) > 0.01F;
            GpuSampler var14 = RenderSystem.getSamplerCache().get(FilterMode.LINEAR);
            GpuBufferSlice var15 = RenderSystem.getDynamicUniforms().write(RenderSystem.getModelViewMatrix(), field_a_1, field_a_2, field_a_3);
            CommandEncoder var16 = RenderSystem.getDevice().createCommandEncoder();
            if (var13) {
               var16.copyTextureToTexture(var8.getFramebuffer().getColorAttachment(), field_a_8, 0, 0, 0, 0, 0, var9, var10);
               a(var9, var10, var11, var12, 1.0F, strength);
               var16.writeToBuffer(field_a_4.slice(), field_a_5);

               try (RenderPass var27 = var16.createRenderPass(() -> "water:blur_downsample", field_a_11[0], OptionalInt.empty(), null, OptionalDouble.empty())) {
                  var27.setPipeline(field_b_1);
                  var27.setVertexBuffer(0, field_b_2);
                  var27.bindTexture("Sampler0", field_a_9, var14);
                  RenderSystem.bindDefaultUniforms(var27);
                  var27.setUniform("DynamicTransforms", var15);
                  var27.setUniform("BlurData", field_a_4);
                  var27.draw(0, 6);
               }

               var9 = Math.min(10, Math.max(2, (int)(5.0F * strength)));
               float[] var30 = new float[]{1.0F, 2.0F, 2.0F, 3.0F};

               for (int var32 = 0; var32 < var9; var32++) {
                  int var17 = var32 % 2;
                  int var18 = (var32 + 1) % 2;
                  float var19 = var32 < var30.length ? var30[var32] : 3.0F;
                  int var20 = var32;
                  a(var11, var12, var11, var12, var19, 1.0F);
                  var16.writeToBuffer(field_a_4.slice(), field_a_5);

                  try (RenderPass var35 = var16.createRenderPass(
                        () -> "water:blur_" + var20, field_a_11[var18], OptionalInt.empty(), null, OptionalDouble.empty()
                     )) {
                     var35.setPipeline(field_b_1);
                     var35.setVertexBuffer(0, field_b_2);
                     var35.bindTexture("Sampler0", field_a_11[var17], var14);
                     RenderSystem.bindDefaultUniforms(var35);
                     var35.setUniform("DynamicTransforms", var15);
                     var35.setUniform("BlurData", field_a_4);
                     var35.draw(0, 6);
                  }
               }

               D = var9 % 2;
               e = strength;
               ay = false;
            }

            float[] var29 = new float[]{radius, radius, radius, radius};
            var10 = GuiRenderer.x();
            var13 = GuiRenderer.y();
            a(matrix, x, y, width, height, var10, var13, var29, z);
            var16.writeToBuffer(field_a_4.slice(), field_a_5);

            try (RenderPass var34 = var16.createRenderPass(
                  () -> "water:blur_final",
                  var8.getFramebuffer().getColorAttachmentView(),
                  OptionalInt.empty(),
                  var8.getFramebuffer().getDepthAttachmentView(),
                  OptionalDouble.of(1.0)
               )) {
               GuiRenderer.a(var34);
               var34.setPipeline(c);
               var34.setVertexBuffer(0, field_b_2);
               var34.bindTexture("Sampler0", field_a_11[D], var14);
               RenderSystem.bindDefaultUniforms(var34);
               var34.setUniform("DynamicTransforms", var15);
               var34.setUniform("BlurData", field_a_4);
               var34.draw(0, 6);
            }
         }
      }
   }

   private static void A() {
      A = (A + 1) % 24;
      if (field_a_7[A] == null) {
         field_a_7[A] = MemoryUtil.memAlloc(256);
      }

      field_a_5 = field_a_7[A];
   }

   private static void a(int screenW, int screenH, int texW, int texH, float offset, float strength) {
      A();
      field_a_5.clear();

      for (int var6 = 0; var6 < 16; var6++) {
         field_a_5.putFloat(0.0F);
      }

      field_a_5.putFloat(0.0F).putFloat(0.0F).putFloat(screenW).putFloat(screenH);
      field_a_5.putFloat(screenW).putFloat(screenH).putFloat(1.0F).putFloat(offset);
      field_a_5.putFloat(texW).putFloat(texH).putFloat(1.0F).putFloat(strength);
      field_a_5.putFloat(0.0F).putFloat(0.0F).putFloat(0.0F).putFloat(0.0F);
      field_a_5.putFloat(0.0F).putFloat(0.0F).putFloat(0.0F).putFloat(0.0F);
      field_a_5.flip();
      B();
   }

   private static void a(Matrix4f matrix, float x, float y, float w, float h, int fbW, int fbH, float[] r, float z) {
      A();
      field_a_5.clear();
      field_a_5.putFloat(matrix.m00()).putFloat(matrix.m01()).putFloat(matrix.m02()).putFloat(matrix.m03());
      field_a_5.putFloat(matrix.m10()).putFloat(matrix.m11()).putFloat(matrix.m12()).putFloat(matrix.m13());
      field_a_5.putFloat(matrix.m20()).putFloat(matrix.m21()).putFloat(matrix.m22()).putFloat(matrix.m23());
      field_a_5.putFloat(matrix.m30()).putFloat(matrix.m31()).putFloat(matrix.m32()).putFloat(matrix.m33());
      field_a_5.putFloat(x).putFloat(y).putFloat(w).putFloat(h);
      field_a_5.putFloat(fbW).putFloat(fbH).putFloat(0.0F).putFloat(0.0F);
      field_a_5.putFloat(fbW).putFloat(fbH).putFloat(1.0F).putFloat(0.0F);
      field_a_5.putFloat(r[0]).putFloat(r[1]).putFloat(r[2]).putFloat(r[3]);
      field_a_5.putFloat(z).putFloat(0.0F).putFloat(0.0F).putFloat(0.0F);
      field_a_5.flip();
      B();
   }

   private static void B() {
      int var0 = field_a_5.remaining();
      if (field_a_6[A] == null || field_a_6[A].size() < var0) {
         if (field_a_6[A] != null) {
            field_a_6[A].close();
         }

         int var1 = A;
         field_a_6[A] = RenderSystem.getDevice().createBuffer(() -> "water:blur_uniform_" + var1, 136, var0);
      }

      field_a_4 = field_a_6[A];
   }
}
