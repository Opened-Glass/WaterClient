package com.water.mixin;

import com.water.WaterClient;
import com.water.module.modules.client.WaterPlus;
import net.minecraft.client.Keyboard;
import net.minecraft.client.input.KeyInput;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Keyboard.class})
public class WaterMenuKeyboardMixin {
   @Unique
   private boolean water$menuKeyDown = false;

   @Inject(
      method = {"onKey"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void water$openClickGuiFromAnyScreen(long var1, int var3, KeyInput var4, CallbackInfo var5) {
      int var6 = WaterPlus.getGuiKey();
      boolean var7 = GLFW.glfwGetKey(var1, var6) == 1;
      if (var7 && !this.water$menuKeyDown) {
         WaterClient.toggleClickGui();
         var5.cancel();
      }

      this.water$menuKeyDown = var7;
   }
}
