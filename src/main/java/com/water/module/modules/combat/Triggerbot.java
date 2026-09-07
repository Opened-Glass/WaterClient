package com.water.module.modules.combat;

import com.water.module.Category;
import com.water.module.Module;
import com.water.module.modules.client.Friends;
import com.water.setting.Setting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult.Type;

public final class Triggerbot extends Module {
   private final Setting<Boolean> bf = new Setting<>("Only Crit", false);
   private final Setting<Boolean> bg = new Setting<>("Check Shield", false);
   private int l = 0;

   public Triggerbot() {
      super("Triggerbot", Category.field_a_1);
      this.addSetting(this.bf);
      this.addSetting(this.bg);
   }

   @Override
   public void onEnable() {
      this.l = 0;
   }

   @Override
   public void onDisable() {
      this.l = 0;
   }

   @Override
   public void onTick() {
      if (mc.player != null && mc.world != null) {
         if (mc.currentScreen == null) {
            if (this.l > 0) {
               this.l--;
            } else if (mc.crosshairTarget != null && mc.crosshairTarget.getType() == Type.ENTITY) {
               if (mc.crosshairTarget instanceof EntityHitResult var3) {
                  Entity var4 = var3.getEntity();
                  if (var4 instanceof LivingEntity) {
                     if (var4 != mc.player) {
                        if (!(var4 instanceof PlayerEntity var2 && Friends.method_a_1() && Friends.method_a_1(var2.getName().getString()))) {
                           if (!mc.options.attackKey.isPressed()) {
                              if (this.a((LivingEntity)var4)) {
                                 this.a(var4);
                                 this.l = 9;
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private boolean a(LivingEntity entity) {
      return this.bf.getValue() && !this.a((PlayerEntity)mc.player) ? false : !this.bg.getValue() || !this.b(entity);
   }

   private boolean b(LivingEntity entity) {
      ItemStack var2 = entity.getMainHandStack();
      LivingEntity var3 = entity.getOffHandStack();
      return var2.getItem() == Items.SHIELD || var3.getItem() == Items.SHIELD;
   }

   private boolean a(PlayerEntity p) {
      if (p.fallDistance <= 0.05F) {
         return false;
      } else if (p.isOnGround()) {
         return false;
      } else {
         return !p.isTouchingWater() && !p.isInLava() && !p.isClimbing() && !p.hasVehicle() ? !p.isSprinting() : false;
      }
   }

   private void a(Entity target) {
      mc.interactionManager.attackEntity(mc.player, target);
      mc.player.swingHand(Hand.MAIN_HAND);
   }
}
