package com.water.mixin;

import com.mojang.authlib.GameProfile;
import com.water.module.modules.donut.FakeRoles;
import com.water.module.modules.misc.SkinChanger;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({PlayerListEntry.class})
public abstract class PlayerListEntryMixin {
   @Shadow
   public abstract GameProfile method_2966();

   @Inject(
      method = {"getSkinTextures"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void water$overrideListEntrySkin(CallbackInfoReturnable<SkinTextures> var1) {
      GameProfile var2 = this.method_2966();
      if (var2 != null && var2.id() != null) {
         SkinTextures var3 = SkinChanger.getOverrideSkin(var2.id());
         if (var3 != null) {
            var1.setReturnValue(var3);
         }
      }
   }

   @Inject(
      method = {"getDisplayName"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void water$fakeRoleDisplayName(CallbackInfoReturnable<Text> var1) {
      if (FakeRoles.isActive()) {
         GameProfile var2 = this.method_2966();
         MinecraftClient var3 = MinecraftClient.getInstance();
         if (var2 != null && var2.id() != null && var3 != null && var3.player != null) {
            if (var2.id().equals(var3.player.getUuid())) {
               Text var4 = FakeRoles.buildPrefixedDisplayName(var2.name());
               if (var4 != null) {
                  var1.setReturnValue(var4);
               }
            }
         }
      }
   }
}
