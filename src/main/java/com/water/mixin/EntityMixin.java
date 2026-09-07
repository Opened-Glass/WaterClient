package com.water.mixin;

import com.water.module.modules.donut.FakeRoles;
import com.water.module.modules.misc.Freelook;
import com.water.module.modules.misc.NameProtect;
import com.water.module.modules.render.Freecam;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Entity.class})
public class EntityMixin {
   @Inject(
      method = {"changeLookDirection"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onChangeLookDirection(double var1, double var3, CallbackInfo var5) {
      Entity var6 = (Entity)(Object)this;
      if (var6 == MinecraftClient.getInstance().player) {
         if (Freecam.instance != null && Freecam.instance.isEnabled()) {
            Freecam.instance.updateRotation(var1 * 0.15 * Freecam.instance.getLookSensitivity(), var3 * 0.15 * Freecam.instance.getLookSensitivity());
            var5.cancel();
         } else {
            if (Freelook.instance != null && Freelook.instance.isCameraActive()) {
               Freelook.instance.consumeMouseDelta(var1, var3);
               var5.cancel();
            }
         }
      }
   }

   @Inject(
      method = {"isSneaking"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onIsSneaking(CallbackInfoReturnable<Boolean> var1) {
      if (Freecam.instance != null && Freecam.instance.isEnabled() && (Object)this == MinecraftClient.getInstance().player) {
         var1.setReturnValue(Freecam.instance.shouldRenderSneaking());
      }
   }

   @Inject(
      method = {"shouldRender(D)Z"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onShouldRender(double var1, CallbackInfoReturnable<Boolean> var3) {
      if (Freecam.instance != null && Freecam.instance.isEnabled() && var1 < 25600.0) {
         var3.setReturnValue(true);
      }
   }

   @Inject(
      method = {"getDisplayName"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void onGetDisplayName(CallbackInfoReturnable<Text> var1) {
      if (FakeRoles.isActive()) {
         Text var2 = FakeRoles.modifyChatText((Text)var1.getReturnValue());
         if (var2 != var1.getReturnValue()) {
            var1.setReturnValue(var2);
            return;
         }
      }

      if (NameProtect.instance != null && NameProtect.instance.isEnabled() && MinecraftClient.getInstance().getSession() != null) {
         String var5 = MinecraftClient.getInstance().getSession().getUsername();
         if (var5 != null) {
            Text var3 = (Text)var1.getReturnValue();
            if (var3 != null) {
               String var4 = var3.getString();
               if (var4.contains(var5)) {
                  var1.setReturnValue(Text.literal(var4.replace(var5, NameProtect.instance.getFakeName())));
               }
            }
         }
      }
   }

   @Inject(
      method = {"getName"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void onGetName(CallbackInfoReturnable<Text> var1) {
      if (FakeRoles.isActive()) {
         Text var2 = FakeRoles.modifyChatText((Text)var1.getReturnValue());
         if (var2 != var1.getReturnValue()) {
            var1.setReturnValue(var2);
            return;
         }
      }

      if (NameProtect.instance != null && NameProtect.instance.isEnabled() && MinecraftClient.getInstance().getSession() != null) {
         String var5 = MinecraftClient.getInstance().getSession().getUsername();
         if (var5 != null) {
            Text var3 = (Text)var1.getReturnValue();
            if (var3 != null) {
               String var4 = var3.getString();
               if (var4.contains(var5)) {
                  var1.setReturnValue(Text.literal(var4.replace(var5, NameProtect.instance.getFakeName())));
               }
            }
         }
      }
   }
}
