package com.water.mixin;

import com.water.module.modules.combat.SpearSwap;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({MinecraftClient.class})
public class MinecraftClientAttackMixin {
   @Inject(
      method = {"handleInputEvents"},
      at = {@At("HEAD")},
      require = 0
   )
   private void water$preInput(CallbackInfo var1) {
      SpearSwap var2 = SpearSwap.INSTANCE;
      if (var2 != null && var2.isEnabled()) {
         MinecraftClient var3 = MinecraftClient.getInstance();
         if (var3 != null && var3.player != null && var3.options != null) {
            if (var3.currentScreen == null) {
               if (var3.options.attackKey.isPressed()) {
                  var2.preAttack();
               } else {
                  var2.noAttack();
               }
            }
         }
      }
   }
}
