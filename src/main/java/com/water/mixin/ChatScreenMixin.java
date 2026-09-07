package com.water.mixin;

import com.water.gui.HudEditor;
import com.water.gui.hud.SpotifyQueueHud;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({ChatScreen.class})
public class ChatScreenMixin {
   @Inject(
      method = {"mouseClicked"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void water$mouseClicked(Click var1, boolean var2, CallbackInfoReturnable<Boolean> var3) {
      double var4 = var1.x();
      double var6 = var1.y();
      int var8 = var1.button();
      if (var8 == 0) {
         if (SpotifyQueueHud.onMouseClick(var4, var6, var8)) {
            var3.setReturnValue(true);
            return;
         }

         if (HudEditor.INSTANCE.onMouseClick(var4, var6, var8)) {
            HudEditor.isEditing = true;
            var3.setReturnValue(true);
         }
      }
   }

   @Inject(
      method = {"render"},
      at = {@At("TAIL")}
   )
   private void water$render(DrawContext var1, int var2, int var3, float var4, CallbackInfo var5) {
      MinecraftClient var6 = MinecraftClient.getInstance();
      if (var6 != null) {
         boolean var7 = GLFW.glfwGetMouseButton(var6.getWindow().getHandle(), 0) == 1;
         if (!var7) {
            SpotifyQueueHud.onMouseRelease();
            HudEditor.INSTANCE.onMouseRelease();
            HudEditor.isEditing = false;
         }
      }

      HudEditor.INSTANCE.render(var1, var2, var3);
   }
}
