package com.water.mixin;

import com.water.module.ModuleManager;
import com.water.module.modules.misc.AutoMine;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({MinecraftClient.class})
public class AutoMineMixin {
   private BlockPos autoMineLastPos = null;
   private boolean autoMineActive = false;

   @Inject(
      method = {"tick"},
      at = {@At("HEAD")}
   )
   private void onTick(CallbackInfo var1) {
      MinecraftClient var2 = (MinecraftClient)(Object)this;
      AutoMine var3 = (AutoMine)ModuleManager.INSTANCE.getModuleByName("AutoMine");
      if (var3 != null && var3.isEnabled()) {
         if (var2.player != null && var2.world != null && var2.interactionManager != null) {
            if (var2.currentScreen == null) {
               boolean var4 = GLFW.glfwGetMouseButton(var2.getWindow().getHandle(), 0) == 1;
               if (var4) {
                  this.autoMineActive = false;
                  this.autoMineLastPos = null;
               } else {
                  HitResult var5 = var2.crosshairTarget;
                  if (var5 != null && var5.getType() == Type.BLOCK) {
                     BlockHitResult var6 = (BlockHitResult)var5;
                     BlockPos var7 = var6.getBlockPos();
                     BlockState var8 = var2.world.getBlockState(var7);
                     if (!var8.isAir() && !(var8.getHardness(var2.world, var7) < 0.0F)) {
                        var2.options.attackKey.setPressed(true);
                        this.autoMineActive = true;
                        this.autoMineLastPos = var7;
                     } else {
                        this.autoMineLastPos = null;
                        this.autoMineActive = false;
                     }
                  } else {
                     this.autoMineLastPos = null;
                     this.autoMineActive = false;
                  }
               }
            }
         }
      } else {
         if (this.autoMineActive) {
            this.autoMineActive = false;
            this.autoMineLastPos = null;
         }
      }
   }
}
