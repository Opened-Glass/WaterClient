package com.water.mixin;

import com.water.module.modules.render.Freecam;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.math.Vec2f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({KeyboardInput.class})
public class KeyboardInputMixin {
   @Inject(
      method = {"tick()V"},
      at = {@At("RETURN")}
   )
   private void onTickReturn(CallbackInfo var1) {
      if (Freecam.instance != null && Freecam.instance.isEnabled()) {
         InputAccessor var2 = (InputAccessor)this;
         if (Freecam.instance.shouldKeepMovement()) {
            var2.water$setPlayerInput(Freecam.instance.getHeldMovementInput());
            var2.water$setMovementVector(Freecam.instance.getHeldMovementVector());
         } else {
            var2.water$setPlayerInput(new PlayerInput(false, false, false, false, false, false, false));
            var2.water$setMovementVector(Vec2f.ZERO);
         }
      }
   }
}
