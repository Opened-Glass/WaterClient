package com.water.mixin;

import com.water.module.modules.combat.Hitbox;
import com.water.module.modules.misc.NameTags;
import com.water.utils.NametagRenderState;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({EntityRenderer.class})
public class EntityRendererMixin {
   @Inject(
      method = {"updateRenderState"},
      at = {@At("TAIL")}
   )
   private void water$updateNametagState(Entity var1, EntityRenderState var2, float var3, CallbackInfo var4) {
      if (var1 instanceof LivingEntity var5 && NameTags.isActive()) {
         NameTags var6 = NameTags.instance;
         if (var6 != null && var6.shouldRenderForState(var5, var2.squaredDistanceToCamera) && !var2.invisible) {
            NametagRenderState.mark(var2);
         } else {
            NametagRenderState.clear(var2);
         }
      } else {
         NametagRenderState.clear(var2);
      }
   }

   @Inject(
      method = {"renderLabelIfPresent"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void water$renderCustomNametag(EntityRenderState var1, MatrixStack var2, OrderedRenderCommandQueue var3, CameraRenderState var4, CallbackInfo var5) {
      if (NametagRenderState.hasEntry(var1)) {
         var5.cancel();
      }
   }

   @Inject(
      method = {"getShadowRadius"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void water$hitbox(EntityRenderState var1, CallbackInfoReturnable<Float> var2) {
      Hitbox var3 = Hitbox.INSTANCE;
      if (var3 != null && var3.isEnabled()) {
         var2.setReturnValue((Float)var2.getReturnValue() + var3.size.getValue());
      }
   }
}
