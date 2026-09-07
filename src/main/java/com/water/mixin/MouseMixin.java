package com.water.mixin;

import com.water.gui.HudEditor;
import com.water.gui.hud.SpotifyQueueHud;
import com.water.module.modules.render.Freecam;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.screen.ChatScreen;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Mouse.class})
public class MouseMixin {
   private static double toScaledX(MinecraftClient var0, double var1) {
      if (var0 != null && var0.getWindow() != null) {
         double var3 = var0.getWindow().getWidth();
         return var3 <= 0.0 ? var1 : var1 * (var0.getWindow().getScaledWidth() / var3);
      } else {
         return var1;
      }
   }

   private static double toScaledY(MinecraftClient var0, double var1) {
      if (var0 != null && var0.getWindow() != null) {
         double var3 = var0.getWindow().getHeight();
         return var3 <= 0.0 ? var1 : var1 * (var0.getWindow().getScaledHeight() / var3);
      } else {
         return var1;
      }
   }

   @Inject(
      method = {"onMouseButton"},
      at = {@At("HEAD")},
      cancellable = true,
      require = 0
   )
   private void water$hudEditorMouseButtonClick(long var1, @Coerce Object var3, int var4, CallbackInfo var5) {
      MinecraftClient var6 = MinecraftClient.getInstance();
      if (var6 != null && var6.currentScreen instanceof ChatScreen) {
         if (var3 instanceof Click var7) {
            if (var4 == 1) {
               double var8 = toScaledX(var6, var7.x());
               double var10 = toScaledY(var6, var7.y());
               if (SpotifyQueueHud.onMouseClick(var8, var10, var7.button())) {
                  var5.cancel();
                  return;
               }

               if (HudEditor.INSTANCE.onMouseClick(var7.x(), var7.y(), var7.button())) {
                  var5.cancel();
               }
            } else if (var4 == 0) {
               SpotifyQueueHud.onMouseRelease();
               HudEditor.INSTANCE.onMouseRelease();
            }
         }
      }
   }

   @Inject(
      method = {"onCursorPos"},
      at = {@At("HEAD")},
      require = 0
   )
   private void water$hudEditorCursorPos(long var1, double var3, double var5, CallbackInfo var7) {
      MinecraftClient var8 = MinecraftClient.getInstance();
      if (var8 != null && var8.player != null) {
         if (var8.currentScreen instanceof ChatScreen) {
            boolean var9 = SpotifyQueueHud.isDragging();
            boolean var10 = HudEditor.INSTANCE.isDragging();
            if (var9 || var10) {
               if (GLFW.glfwGetMouseButton(var1, 0) != 1) {
                  SpotifyQueueHud.onMouseRelease();
                  HudEditor.INSTANCE.onMouseRelease();
               } else {
                  if (var9) {
                     SpotifyQueueHud.onMouseDrag(toScaledX(var8, var3), toScaledY(var8, var5));
                  } else {
                     HudEditor.INSTANCE.onMouseDrag(toScaledX(var8, var3), toScaledY(var8, var5));
                  }
               }
            }
         }
      }
   }

   @Inject(
      method = {"onMouseScroll"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void water$useScrollForFreecamSpeed(long var1, double var3, double var5, CallbackInfo var7) {
      if (Freecam.instance != null && Freecam.instance.isEnabled()) {
         if (MinecraftClient.getInstance().currentScreen == null) {
            double var8 = var5 != 0.0 ? var5 : var3;
            if (var8 != 0.0) {
               Freecam.instance.adjustSpeed(var8);
               var7.cancel();
            }
         }
      }
   }

   @Inject(
      method = {"onMouseScroll"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void water$hudEditorMouseScroll(long var1, double var3, double var5, CallbackInfo var7) {
      MinecraftClient var8 = MinecraftClient.getInstance();
      if (var8 != null && var8.currentScreen instanceof ChatScreen) {
         double var9 = var5 != 0.0 ? var5 : var3;
         if (var9 != 0.0) {
            double var11 = toScaledX(var8, var8.mouse.getX());
            double var13 = toScaledY(var8, var8.mouse.getY());
            if (SpotifyQueueHud.onScroll(var11, var13, var9)) {
               var7.cancel();
            } else {
               if (HudEditor.INSTANCE.onMouseScroll(var11, var13, var9)) {
                  var7.cancel();
               }
            }
         }
      }
   }
}
