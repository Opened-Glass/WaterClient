package com.water.utils.renderer;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.RenderSystem;
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

public class Outline2DRenderer {
   private static RenderPipeline field_a_1;
   private static GpuBuffer field_a_2;

   public static void a() {
      if (field_a_1 == null) {
         try {
            field_a_1 = RenderPipeline.builder()
               .withLocation(Identifier.of("water", "outline"))
               .withVertexShader(Identifier.of("water", "outline_vertex"))
               .withFragmentShader(Identifier.of("water", "outline_fragment"))
               .withVertexFormat(VertexFormat.builder().build(), DrawMode.TRIANGLES)
               .withUniform("Uniforms", UniformType.UNIFORM_BUFFER)
               .withBlend(BlendFunction.TRANSLUCENT)
               .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
               .withCull(false)
               .build();
            field_a_2 = RenderSystem.getDevice().createBuffer(() -> "Outline2D Uniforms", 136, 256L);
         } catch (Exception var1) {
            System.err.println("[Outline2D] Failed to init: " + var1.getMessage());
            var1.printStackTrace();
         }
      }
   }

   public static void a(
      Matrix4f matrix, float x, float y, float width, float height, float tl, float tr, float br, float bl, float thickness, float z, int... colors
   ) {
      if (field_a_1 == null) {
         a();
      }

      if (field_a_1 != null && field_a_2 != null) {
         colors = a(colors);
         ByteBuffer var12 = MemoryUtil.memAlloc(256);
         var12.putFloat(matrix.m00()).putFloat(matrix.m01()).putFloat(matrix.m02()).putFloat(matrix.m03());
         var12.putFloat(matrix.m10()).putFloat(matrix.m11()).putFloat(matrix.m12()).putFloat(matrix.m13());
         var12.putFloat(matrix.m20()).putFloat(matrix.m21()).putFloat(matrix.m22()).putFloat(matrix.m23());
         var12.putFloat(matrix.m30()).putFloat(matrix.m31()).putFloat(matrix.m32()).putFloat(matrix.m33());
         var12.position(64);
         var12.putFloat(x).putFloat(y).putFloat(width).putFloat(height);
         var12.putFloat(tr).putFloat(br).putFloat(tl).putFloat(bl);
         var12.position(96);
         var12.putFloat(thickness).putFloat(z).putFloat(0.0F).putFloat(0.0F);
         var12.position(112);

         for (Matrix4f var15 = 0; var15 < 9; var15++) {
            float var18 = colors[var15];
            var12.putFloat((var18 >> 16 & 0xFF) / 255.0F);
            var12.putFloat((var18 >> 8 & 0xFF) / 255.0F);
            var12.putFloat((var18 & 0xFF) / 255.0F);
            var12.putFloat((var18 >> 24 & 0xFF) / 255.0F);
         }

         var12.flip();
         Matrix4f var16 = RenderSystem.getDevice().createCommandEncoder();
         var16.writeToBuffer(field_a_2.slice(), var12);
         MemoryUtil.memFree(var12);
         float var19 = MinecraftClient.getInstance().getFramebuffer();

         try (Matrix4f var17 = var16.createRenderPass(
               () -> "Outline2D", var19.getColorAttachmentView(), OptionalInt.empty(), var19.getDepthAttachmentView(), OptionalDouble.of(1.0)
            )) {
            GuiRenderer.a(var17);
            var17.setPipeline(field_a_1);
            var17.setUniform("Uniforms", field_a_2);
            var17.draw(0, 6);
         }
      }
   }

   private static int[] a(int[] colors) {
      if (colors.length == 1) {
         int var3 = colors[0];
         return new int[]{var3, var3, var3, var3, var3, var3, var3, var3, var3};
      } else if (colors.length >= 9) {
         return colors;
      } else {
         int[] var1 = new int[9];

         for (int var2 = 0; var2 < 9; var2++) {
            var1[var2] = var2 < colors.length ? colors[var2] : colors[colors.length - 1];
         }

         return var1;
      }
   }
}
