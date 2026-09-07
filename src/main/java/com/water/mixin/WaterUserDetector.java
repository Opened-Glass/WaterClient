package com.water.mixin;

import com.water.module.modules.misc.NameTags;
import java.util.UUID;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.PlayerRemoveS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ClientPlayNetworkHandler.class})
public class WaterUserDetector {
   @Inject(
      method = {"onPlayerRemove"},
      at = {@At("HEAD")}
   )
   private void onPlayerRemove(PlayerRemoveS2CPacket var1, CallbackInfo var2) {
      try {
         for (UUID var4 : var1.profileIds()) {
            NameTags.removeWaterUser(var4);
         }
      } catch (Exception var5) {
      }
   }
}
