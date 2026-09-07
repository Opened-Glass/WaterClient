package com.water.gui;

import com.water.module.modules.client.WaterPlus;
import com.water.utils.renderer.GuiRenderer;
import com.water.utils.renderer.WaterFontRenderer;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Util;

public final class ToastManager {
   public static final ToastManager INSTANCE = new ToastManager();
   private static final long DISPLAY_MS = 3500L;
   private static final long ANIMATION_MS = 220L;
   private static final int MAX_NOTIFICATIONS = 5;
   private static final float CARD_WIDTH = 170.0F;
   private static final float CARD_HEIGHT = 36.0F;
   private static final float CARD_GAP = 5.0F;
   private static final float ICON_SIZE = 16.0F;
   private static final float ICON_PAD = 7.0F;
   private static final float ACCENT_BAR = 3.0F;
   private final List<ToastManager.a> notifications = new CopyOnWriteArrayList<>();

   private ToastManager() {
   }

   public void pushToggle(String moduleName, boolean enabled) {
      this.pushToggle(moduleName, enabled, ItemStack.EMPTY);
   }

   public void pushToggle(String moduleName, boolean enabled, ItemStack icon) {
      this.push(moduleName, enabled ? "Enabled" : "Disabled", icon, enabled ? WaterPlus.getAccentARGB() : -2076576);
   }

   public void renderToasts(DrawContext context) {
      this.render(context);
   }

   public void push(String message, String details, ItemStack stack, int accentColor) {
      this.notifications
         .add(
            0,
            new ToastManager.a(
               message == null ? "" : message,
               details == null ? "" : details,
               stack == null ? ItemStack.EMPTY : stack.copy(),
               accentColor,
               Util.getMeasuringTimeMs()
            )
         );

      while (this.notifications.size() > 5) {
         this.notifications.remove(this.notifications.size() - 1);
      }
   }

   public void render(DrawContext context) {
      if (!this.notifications.isEmpty()) {
         MinecraftClient var2 = MinecraftClient.getInstance();
         long var3 = Util.getMeasuringTimeMs();
         this.notifications.removeIf(entry -> entry.isExpired(var3));
         if (!this.notifications.isEmpty()) {
            context.createNewRootLayer();
            float var5 = var2.getWindow().getScaledWidth() - 170.0F - 8.0F;
            float var15 = var2.getWindow().getScaledHeight() - 36.0F - 8.0F;
            float var6 = WaterPlus.getGuiRoundness();

            for (int var7 = 0; var7 < this.notifications.size(); var7++) {
               ToastManager.a var8 = this.notifications.get(var7);
               float var9 = var8.getVisibility(var3);
               if (!(var9 <= 0.0F)) {
                  float var10 = var5 + (1.0F - var9) * 184.0F;
                  float var11 = var15 - var7 * 41.0F;
                  withAlpha(var8.accentColor, var9);
                  int var12 = withAlpha(WaterPlus.getBackgroundARGB(), var9 * 0.97F);
                  int var13 = withAlpha(-16777216, var9 * 0.45F);
                  int var14 = withAlpha(blendColors(WaterPlus.getBackgroundARGB(), var8.accentColor, 0.25F), var9);
                  GuiRenderer.a(context, var10 + 2.0F, var11 + 2.0F, 170.0F, 36.0F, var6, var13, false);
                  GuiRenderer.a(context, var10, var11, 170.0F, 36.0F, var6, var12, false);
                  GuiRenderer.a(context, var10, var11, 170.0F, 36.0F, var6, 1.0F, var14, false);
                  var10 = var10 + 7.0F + 3.0F;
                  float var19 = var11 + 10.0F;
                  var13 = withAlpha(-15724528, var9 * 0.7F);
                  GuiRenderer.a(context, var10 - 2.0F, var19 - 2.0F, 20.0F, 20.0F, var6 * 0.5F, var13, false);
                  if (!var8.stack.isEmpty()) {
                     context.drawItem(var8.stack, (int)var10, (int)var19);
                  }

                  WaterFontRenderer var20 = WaterFontRenderer.INSTANCE;
                  var13 = withAlpha(-1, var9);
                  int var16 = withAlpha(var8.accentColor | 0xFF000000, var9);
                  var10 = var10 + 16.0F + 6.0F;
                  var20.method_a_2("A");
                  var20.a(context, var8.message, var10, var11 + 8.0F, var13);
                  var20.a(context, var8.details, var10, var11 + 20.0F, var16);
               }
            }
         }
      }
   }

   private static int withAlpha(int color, float alphaScale) {
      float var2 = Math.max(0, Math.min(255, Math.round((color >>> 24 & 0xFF) * alphaScale)));
      return color & 16777215 | var2 << 24;
   }

   private static int blendColors(int a, int b, float t) {
      int var3 = a >> 16 & 0xFF;
      int var4 = a >> 8 & 0xFF;
      a &= 255;
      int var5 = b >> 16 & 0xFF;
      int var6 = b >> 8 & 0xFF;
      b &= 255;
      var3 = (int)(var3 + (var5 - var3) * t);
      var4 = (int)(var4 + (var6 - var4) * t);
      a = (int)(a + (b - a) * t);
      return 0xFF000000 | var3 << 16 | var4 << 8 | a;
   }

   static String _c98900d3603() {
      return "R";
   }

   private static final class a {
      private final String message;
      private final String details;
      private final ItemStack stack;
      private final int accentColor;
      private final long createdAt;

      private a(String message, String details, ItemStack stack, int accentColor, long createdAt) {
         this.message = message;
         this.details = details;
         this.stack = stack;
         this.accentColor = accentColor | 0xFF000000;
         this.createdAt = createdAt;
      }

      private boolean isExpired(long now) {
         return now - this.createdAt >= 3500L;
      }

      private float getVisibility(long now) {
         long var3 = now - this.createdAt;
         if (var3 <= 0L) {
            return 0.0F;
         } else if (var3 < 220L) {
            return this.easeOut((float)var3 / 220.0F);
         } else {
            return var3 > 3280L ? this.easeOut(Math.max(0.0F, 1.0F - (float)(var3 - 3280L) / 220.0F)) : 1.0F;
         }
      }

      private float easeOut(float t) {
         t = 1.0F - Math.max(0.0F, Math.min(1.0F, t));
         return 1.0F - t * t * t;
      }
   }
}
