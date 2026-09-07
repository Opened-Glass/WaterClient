package com.water.utils.renderer;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import javax.imageio.ImageIO;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;

public class WaterFontRenderer {
   public static final WaterFontRenderer INSTANCE = new WaterFontRenderer();
   private static volatile WaterFontRenderer.b field_a_1 = WaterFontRenderer.b.j;
   private Font field_a_2;
   private WaterFontRenderer.b field_b_1 = null;
   private static volatile int bj = -1;
   private final AtomicInteger field_b_2 = new AtomicInteger(0);
   private final LinkedHashMap<String, WaterFontRenderer.a> field_a_3 = new LinkedHashMap<String, WaterFontRenderer.a>() {
      @Override
      protected boolean removeEldestEntry(Entry<String, WaterFontRenderer.a> eldest) {
         if (this.size() > 300) {
            MinecraftClient var2 = MinecraftClient.getInstance();
            if (var2 != null) {
               try {
                  var2.getTextureManager().destroyTexture(((WaterFontRenderer.a)eldest.getValue()).d);
               } catch (Exception var3) {
               }
            } else {
               ((WaterFontRenderer.a)eldest.getValue()).b.close();
            }

            return true;
         } else {
            return false;
         }
      }
   };

   public static void n(int y) {
      bj = y;
   }

   public static void bk() {
      bj = -1;
   }

   public static void method_a_1(WaterFontRenderer.b choice) {
      if (choice != field_a_1) {
         field_a_1 = choice;
         INSTANCE.field_a_2 = null;
         INSTANCE.field_b_1 = null;
         INSTANCE.field_a_3.clear();
      }
   }

   public static WaterFontRenderer.b a() {
      return field_a_1;
   }

   private void bl() {
      WaterFontRenderer.b var1 = WaterFontRenderer.b.j;
      field_a_1 = WaterFontRenderer.b.j;
      if (this.field_b_1 != var1 || this.field_a_2 == null) {
         if (var1 == WaterFontRenderer.b.k) {
            this.field_a_2 = null;
            this.field_b_1 = var1;
         } else {
            this.field_a_2 = this.method_a_2(WaterFontRenderer.b.j);
            if (this.field_a_2 == null) {
               this.field_a_2 = this.method_a_2(WaterFontRenderer.b.e);
            }

            if (this.field_a_2 == null) {
               this.field_a_2 = this.method_a_2(WaterFontRenderer.b.c);
            }

            if (this.field_a_2 == null) {
               this.field_a_2 = new Font("Monospaced", 1, 16);
            }

            this.field_b_1 = var1;
         }
      }
   }

   private Font method_a_2(WaterFontRenderer.b choice) {
      if (choice != null && choice.aj != null) {
         byte[] var2 = this.method_a_3(choice);
         if (var2 != null && var2.length != 0) {
            Font var3 = this.a(var2, 0, choice.field_d_2);
            if (var3 == null) {
               var3 = this.a(var2, 1, choice.field_d_2);
            }

            if (var3 == null) {
               return null;
            } else {
               try {
                  GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(var3);
               } catch (Throwable var4) {
               }

               return var3.deriveFont(0, choice.field_d_2);
            }
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   private Font a(byte[] data, int type, float size) {
      try {
         try (byte[] var7 = new ByteArrayInputStream(data)) {
            var8 = Font.createFont(type, var7).deriveFont(0, size);
         }

         return var8;
      } catch (Throwable var6) {
         return null;
      }
   }

   private byte[] method_a_3(WaterFontRenderer.b choice) {
      WaterFontRenderer.b var14 = choice.aj;

      try {
         label138: {
            byte[] var3;
            try (InputStream var2 = this.getClass().getResourceAsStream(var14)) {
               if (var2 == null) {
                  break label138;
               }

               var3 = var2.readAllBytes();
            }

            return var3;
         }
      } catch (Throwable var13) {
      }

      try {
         ClassLoader var16 = Thread.currentThread().getContextClassLoader();
         String var20 = var14.startsWith("/") ? var14.substring(1) : var14;
         if (var16 != null) {
            try (InputStream var4 = var16.getResourceAsStream(var20)) {
               if (var4 != null) {
                  return var4.readAllBytes();
               }
            }
         }
      } catch (Throwable var11) {
      }

      try {
         MinecraftClient var17 = MinecraftClient.getInstance();
         if (var17 != null && var17.getResourceManager() != null) {
            String var21 = var14.startsWith("/assets/water/") ? var14.substring("/assets/water/".length()) : var14;
            if (var21.startsWith("/")) {
               var21 = var21.substring(1);
            }

            Identifier var23 = Identifier.of("water", var21);
            Optional var18 = var17.getResourceManager().getResource(var23);
            if (var18.isPresent()) {
               try (InputStream var22 = ((Resource)var18.get()).getInputStream()) {
                  var15 = var22.readAllBytes();
               }

               return var15;
            }
         }
      } catch (Throwable var9) {
      }

      return null;
   }

   private WaterFontRenderer.a method_a_1(String text) {
      String var2 = field_a_1.name() + ":" + text;
      WaterFontRenderer.a var3 = this.field_a_3.get(var2);
      if (var3 != null) {
         return var3;
      } else {
         var3 = this.b(text);
         if (var3 != null) {
            this.field_a_3.put(var2, var3);
         }

         return var3;
      }
   }

   private WaterFontRenderer.a b(String text) {
      this.bl();
      if (this.field_a_2 == null) {
         return null;
      } else {
         BufferedImage var2 = new BufferedImage(1, 1, 2);
         Graphics2D var10 = var2.createGraphics();
         var10.setFont(this.field_a_2);
         FontRenderContext var3 = var10.getFontRenderContext();
         Rectangle2D var12 = this.field_a_2.getStringBounds(text, var3);
         var10.dispose();
         int var11 = Math.max(1, (int)Math.ceil(var12.getWidth()) + 6);
         int var13 = Math.max(1, (int)Math.ceil(this.field_a_2.getSize() * 1.3F));
         BufferedImage var4 = new BufferedImage(var11, var13, 2);
         Graphics2D var5 = var4.createGraphics();
         var5.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
         var5.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         var5.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
         var5.setFont(this.field_a_2);
         var5.setColor(Color.WHITE);
         var5.drawString(text, 1, this.field_a_2.getSize() - 1);
         var5.dispose();

         try {
            String var7 = new ByteArrayOutputStream();
            ImageIO.write(var4, "png", var7);
            String var8 = NativeImage.read(new ByteArrayInputStream(var7.toByteArray()));
            String var9 = new NativeImageBackedTexture(() -> "water_font_cache", var8);
            Identifier var14 = Identifier.of("water", "font_cache_" + this.field_b_2.getAndIncrement());
            MinecraftClient var15 = MinecraftClient.getInstance();
            if (var15 != null) {
               var15.getTextureManager().registerTexture(var14, var9);
            }

            return new WaterFontRenderer.a(var9, var14, var11 / 2, var13 / 2);
         } catch (Exception var6) {
            return null;
         }
      }
   }

   public void a(DrawContext ctx, String text, float x, float y, int argbColor) {
      if (text != null && !text.isEmpty()) {
         if (bj < 0 || !(y >= bj)) {
            if (field_a_1 == WaterFontRenderer.b.k) {
               MinecraftClient var8 = MinecraftClient.getInstance();
               if (var8 != null) {
                  ctx.drawText(var8.textRenderer, text, (int)x, (int)y, argbColor, false);
               }
            } else {
               WaterFontRenderer.a var6 = this.method_a_1(text);
               if (var6 != null && var6.b.getGlTextureView() != null) {
                  DrawContext var7 = GuiRenderer.a(ctx);
                  Texture2DRenderer.a(var7, x, y, var6.bk, var6.bl, var6.b.getGlTextureView(), argbColor, 0.0F);
               }
            }
         }
      }
   }

   public int method_a_2(String text) {
      if (text == null || text.isEmpty()) {
         return 0;
      } else if (field_a_1 == WaterFontRenderer.b.k) {
         MinecraftClient var5 = MinecraftClient.getInstance();
         return var5 != null ? var5.textRenderer.getWidth(text) : text.length() * 6;
      } else {
         this.bl();
         if (this.field_a_2 == null) {
            return text.length() * 6;
         } else {
            BufferedImage var2 = new BufferedImage(1, 1, 2);
            Graphics2D var4 = var2.createGraphics();
            var4.setFont(this.field_a_2);
            String var3 = var4.getFontMetrics().stringWidth(text) / 2;
            var4.dispose();
            return var3;
         }
      }
   }

   private static class a {
      NativeImageBackedTexture b;
      Identifier d;
      int bk;
      int bl;

      a(NativeImageBackedTexture t, Identifier id, int dw, int dh) {
         this.b = t;
         this.d = id;
         this.bk = dw;
         this.bl = dh;
      }
   }

   public static enum b {
      c("Inter", "/assets/water/font/inter.ttf", 16.0F, 0),
      field_d_1("Dekatron", "/assets/water/font/dekatron.otf", 16.0F, 0),
      e("Minecraft Ten", "/assets/water/font/minecraft_ten.ttf", 16.0F, 0),
      f("Basketball", "/assets/water/font/basketball.otf", 16.0F, 0),
      g("Second Time", "/assets/water/font/second_time_demo.ttf", 16.0F, 0),
      h("Under Trained", "/assets/water/font/under_trained.ttf", 16.0F, 0),
      i("Belash", "/assets/water/font/belash.ttf", 16.0F, 0),
      j("Bliss Bloom", "/assets/water/font/bliss_bloom.otf", 16.0F, 0),
      k("Vanilla", null, 16.0F, -1);

      private String ai;
      public final String aj;
      public final float field_d_2;
      private int bm;

      private b(String displayName, String resourcePath, float size, int fontType) {
         this.ai = displayName;
         this.aj = resourcePath;
         this.field_d_2 = size;
         this.bm = fontType;
      }
   }
}
