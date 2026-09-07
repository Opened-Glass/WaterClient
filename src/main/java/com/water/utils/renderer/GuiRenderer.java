package com.water.utils.renderer;

import com.mojang.blaze3d.systems.RenderPass;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.Window;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;

public class GuiRenderer {
   private static final List<Runnable> j = new ArrayList<>();

   public static int x() {
      Window var0 = MinecraftClient.getInstance().getWindow();
      return (int)Math.ceil(var0.getWidth() / 1.0);
   }

   public static int y() {
      Window var0 = MinecraftClient.getInstance().getWindow();
      return (int)Math.ceil(var0.getHeight() / 1.0);
   }

   public static Matrix4f a(DrawContext context) {
      Window var1 = MinecraftClient.getInstance().getWindow();
      DrawContext var2 = context.getMatrices();
      return new Matrix4f()
         .ortho(0.0F, var1.getScaledWidth(), var1.getScaledHeight(), 0.0F, -1000.0F, 1000.0F)
         .mul(new Matrix4f(var2.m00, var2.m01, 0.0F, 0.0F, var2.m10, var2.m11, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, var2.m20, var2.m21, 0.0F, 1.0F));
   }

   public static void a(DrawContext context, float x, float y, float width, float height, float radius, int color, boolean overrideContext) {
      a(a(context), x, y, width, height, radius, color, overrideContext);
   }

   public static void a(
      DrawContext context, float x, float y, float width, float height, float tl, float tr, float br, float bl, boolean overrideContext, int... colors
   ) {
      a(a(context), x, y, width, height, tl, tr, br, bl, overrideContext, colors);
   }

   public static void a(Matrix4f matrix, float x, float y, float width, float height, float radius, int color, boolean overrideContext) {
      a(matrix, x, y, width, height, radius, radius, radius, radius, overrideContext, color);
   }

   public static void a(
      Matrix4f matrix, float x, float y, float width, float height, float tl, float tr, float br, float bl, boolean overrideContext, int... colors
   ) {
      if (overrideContext) {
         j.add(() -> Rectangle2DRenderer.a(matrix, x, y, width, height, tl, tr, br, bl, 0.0F, colors));
      } else {
         Rectangle2DRenderer.a(matrix, x, y, width, height, tl, tr, br, bl, 0.0F, colors);
      }
   }

   public static void a(DrawContext context, float x, float y, float width, float height, float radius, float thickness, int color, boolean overrideContext) {
      a(a(context), x, y, width, height, radius, radius, radius, radius, thickness, color, overrideContext);
   }

   public static void a(
      Matrix4f matrix, float x, float y, float width, float height, float tl, float tr, float br, float bl, float thickness, int color, boolean overrideContext
   ) {
      if (overrideContext) {
         j.add(() -> Outline2DRenderer.a(matrix, x, y, width, height, tl, tr, br, bl, thickness, 0.0F, color));
      } else {
         Outline2DRenderer.a(matrix, x, y, width, height, tl, tr, br, bl, thickness, 0.0F, color);
      }
   }

   public static void a(DrawContext context, float x, float y, float width, float height, float radius, float strength, boolean overrideContext) {
      DrawContext var8 = a(context);
      if (overrideContext) {
         j.add(() -> Blur2DRenderer.a(var8, x, y, width, height, radius, strength, 0.0F));
      } else {
         Blur2DRenderer.a(var8, x, y, width, height, radius, strength, 0.0F);
      }
   }

   public static void b(DrawContext context, float x, float y, float size, float thickness, float degree, float rotation, int color, boolean overrideContext) {
      a(a(context), x, y, size, thickness, degree, rotation, color, overrideContext);
   }

   public static void a(Matrix4f matrix, float x, float y, float size, float thickness, float degree, float rotation, int color, boolean overrideContext) {
      if (overrideContext) {
         j.add(() -> Arc2DRenderer.a(matrix, x, y, size, thickness, degree, rotation, 0.0F, color));
      } else {
         Arc2DRenderer.a(matrix, x, y, size, thickness, degree, rotation, 0.0F, color);
      }
   }

   public static void a(DrawContext context, float x, float y, float size, Identifier texture, int color, float radius, boolean overrideContext) {
      a(a(context), x, y, size, texture, color, radius, overrideContext);
   }

   public static void a(Matrix4f matrix, float x, float y, float size, Identifier texture, int color, float radius, boolean overrideContext) {
      MinecraftClient var8 = MinecraftClient.getInstance();
      Identifier var9 = var8.getTextureManager().getTexture(texture);
      if (var9 != null) {
         if (overrideContext) {
            j.add(() -> Texture2DRenderer.a(matrix, x, y, size, var9.getGlTextureView(), color, radius, 0.0F));
            return;
         }

         Texture2DRenderer.a(matrix, x, y, size, var9.getGlTextureView(), color, radius, 0.0F);
      }
   }

   public static void a(RenderPass pass) {
   }
}
