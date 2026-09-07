package com.water.mixin;

import com.water.module.modules.misc.Freelook;
import com.water.module.modules.render.Freecam;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Camera.class})
public abstract class CameraMixin {
   @Shadow
   protected abstract void method_19327(double var1, double var3, double var5);

   @Shadow
   protected abstract void method_19325(float var1, float var2);

   @Shadow
   protected abstract float method_19318(float var1);

   @Shadow
   protected abstract void method_19324(float var1, float var2, float var3);

   @Inject(
      method = {"update"},
      at = {@At("TAIL")}
   )
   private void onUpdate(World var1, Entity var2, boolean var3, boolean var4, float var5, CallbackInfo var6) {
      if (Freecam.instance != null && Freecam.instance.isEnabled()) {
         Freecam.instance.updateCameraMovement();
         double var15 = Freecam.instance.getInterpolatedX(var5);
         double var16 = Freecam.instance.getInterpolatedY(var5);
         double var11 = Freecam.instance.getInterpolatedZ(var5);
         float var13 = Freecam.instance.getInterpolatedYaw(var5);
         float var14 = Freecam.instance.getInterpolatedPitch(var5);
         this.method_19327(var15, var16, var11);
         this.method_19325(var13, var14);
      } else {
         Freelook var7 = Freelook.instance;
         if (var7 != null && var7.isCameraActive() && var2 != null) {
            Vec3d var8 = var2.getCameraPosVec(var5);
            this.method_19325(var7.getCameraYaw(), var7.getCameraPitch());
            this.method_19327(var8.x, var8.y, var8.z);
            float var9 = var7.getDistance();
            if (!var7.shouldWallClip()) {
               var9 = this.method_19318(var9);
            }

            this.method_19324(-var9, 0.0F, 0.0F);
         }
      }
   }

   @Inject(
      method = {"isThirdPerson"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onIsThirdPerson(CallbackInfoReturnable<Boolean> var1) {
      if (Freecam.instance != null && Freecam.instance.isEnabled()) {
         var1.setReturnValue(true);
      } else {
         if (Freelook.instance != null && Freelook.instance.isCameraActive()) {
            var1.setReturnValue(true);
         }
      }
   }
}
