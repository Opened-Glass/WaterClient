package com.water.module.modules.combat;

import com.water.module.ActivatableModule;
import com.water.module.Category;
import com.water.setting.Setting;
import java.util.Random;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import org.lwjgl.glfw.GLFW;

public final class AutoCrystal extends ActivatableModule {
   private final Setting<Float> ag = new Setting<>("Place Delay", 2.0F, 0.0F, 20.0F);
   private final Setting<Float> ah = new Setting<>("Break Delay", 2.0F, 0.0F, 20.0F);
   private final Setting<Float> ai = new Setting<>("Place Chance", 100.0F, 0.0F, 100.0F);
   private final Setting<Float> aj = new Setting<>("Break Chance", 100.0F, 0.0F, 100.0F);
   private final Setting<Boolean> ak = new Setting<>("Fake Punch", false);
   private final Setting<Boolean> al = new Setting<>("Anti-Weakness", false);
   private final Random a = new Random();
   private boolean m = false;
   private int g = 0;
   private int h = 0;

   public AutoCrystal() {
      super("Auto Crystal", Category.field_a_1);
      this.addSetting(this.ag);
      this.addSetting(this.ah);
      this.addSetting(this.ai);
      this.addSetting(this.aj);
      this.addSetting(this.ak);
      this.addSetting(this.al);
   }

   @Override
   public void onEnable() {
      this.g = 0;
      this.h = 0;
      this.m = false;
      super.onEnable();
   }

   @Override
   public void onDisable() {
      this.g = 0;
      this.h = 0;
      this.m = false;
   }

   @Override
   public void b() {
   }

   private boolean h() {
      int var1 = this.getActivationKey();
      if (var1 == 0) {
         return true;
      } else if (mc.getWindow() == null) {
         return false;
      } else {
         try {
            return GLFW.glfwGetKey(mc.getWindow().getHandle(), var1) == 1;
         } catch (Exception var2) {
            return false;
         }
      }
   }

   @Override
   public void onTick() {
      if (mc.player != null && mc.world != null && mc.currentScreen == null) {
         boolean var1 = this.g != 0;
         boolean var2 = this.h != 0;
         if (var1) {
            this.g--;
         }

         if (var2) {
            this.h--;
         }

         if (!mc.player.isDead()) {
            if (!this.h()) {
               this.g = 0;
               this.h = 0;
               this.m = false;
            } else {
               this.m = true;
               if (mc.player.getMainHandStack().getItem() == Items.END_CRYSTAL) {
                  HitResult var3 = mc.crosshairTarget;
                  int var4 = this.a.nextInt(100) + 1;
                  if (var3 instanceof BlockHitResult var5 && var5.getType() == Type.BLOCK) {
                     BlockPos var6 = var5.getBlockPos();
                     boolean var7 = mc.world.getBlockState(var6).isOf(Blocks.OBSIDIAN);
                     boolean var8 = mc.world.getBlockState(var6).isOf(Blocks.BEDROCK);
                     if (var7 || var8) {
                        boolean var9 = this.a(var6);
                        if (!var1 && var4 <= this.ai.getValue()) {
                           if (var9) {
                              mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, var5);
                              mc.player.swingHand(Hand.MAIN_HAND);
                              this.g = this.ag.getValue().intValue();
                           }

                           if (this.ak.getValue() && !var2 && var4 <= this.aj.getValue()) {
                              mc.interactionManager.attackBlock(var6, var5.getSide());
                              mc.player.swingHand(Hand.MAIN_HAND);
                              this.h = this.ah.getValue().intValue();
                           }
                        }
                     }
                  }

                  var4 = this.a.nextInt(100) + 1;
                  if (var3 instanceof EntityHitResult var12) {
                     Entity var13 = var12.getEntity();
                     if (var13 instanceof EndCrystalEntity && !var2 && var4 <= this.aj.getValue()) {
                        int var14 = mc.player.getInventory().getSelectedSlot();
                        if (this.al.getValue() && this.i()) {
                           for (int var15 = 0; var15 < 9; var15++) {
                              ItemStack var16 = mc.player.getInventory().getStack(var15);
                              String var10 = Registries.ITEM.getId(var16.getItem()).getPath();
                              if (var10.endsWith("_sword")) {
                                 mc.player.getInventory().setSelectedSlot(var15);
                                 break;
                              }
                           }
                        }

                        mc.interactionManager.attackEntity(mc.player, var13);
                        mc.player.swingHand(Hand.MAIN_HAND);
                        this.h = this.ah.getValue().intValue();
                        if (this.al.getValue()) {
                           mc.player.getInventory().setSelectedSlot(var14);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private boolean a(BlockPos pos) {
      pos = pos.up();
      if (!mc.world.isAir(pos)) {
         return false;
      } else {
         BlockPos var3 = new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1.0, pos.getY() + 2.0, pos.getZ() + 1.0);
         return mc.world.getOtherEntities(null, var3).isEmpty();
      }
   }

   private boolean i() {
      if (mc.player == null) {
         return false;
      } else {
         int var1 = mc.player.hasStatusEffect(StatusEffects.WEAKNESS);
         if (!var1) {
            return false;
         } else {
            var1 = mc.player.hasStatusEffect(StatusEffects.STRENGTH);
            int var2 = mc.player.getStatusEffect(StatusEffects.WEAKNESS).getAmplifier();
            var1 = var1 ? mc.player.getStatusEffect(StatusEffects.STRENGTH).getAmplifier() : -1;
            return var1 <= var2;
         }
      }
   }
}
