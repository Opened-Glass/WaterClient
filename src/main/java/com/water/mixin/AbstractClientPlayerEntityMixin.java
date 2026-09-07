package com.water.mixin;

import com.water.module.modules.misc.SkinChanger;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.SkinTextures;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({AbstractClientPlayerEntity.class})
public abstract class AbstractClientPlayerEntityMixin {
   @Inject(
      method = {"getSkin"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void water$overrideOwnSkin(CallbackInfoReturnable<SkinTextures> var1) {
      AbstractClientPlayerEntity var2 = (AbstractClientPlayerEntity)(Object)this;
      SkinTextures var3 = SkinChanger.getOverrideSkin(var2.getUuid());
      if (var3 != null) {
         var1.setReturnValue(var3);
      }
   }
}
