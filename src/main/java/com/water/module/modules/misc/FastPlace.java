package com.water.module.modules.misc;

import com.water.mixin.MinecraftClientAccessor;
import com.water.module.Category;
import com.water.module.Module;
import com.water.setting.Setting;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.RangedWeaponItem;

public final class FastPlace extends Module {
   private final Setting<Boolean> aG = new Setting<>("Only XP", false);
   private final Setting<Boolean> aH = new Setting<>("Blocks", true);
   private final Setting<Boolean> aI = new Setting<>("Items", true);
   private final Setting<Float> aJ = new Setting<>("Delay", 0.0F, 0.0F, 10.0F);

   public FastPlace() {
      super("Fast Place", Category.c);
      this.addSetting(this.aG);
      this.addSetting(this.aH);
      this.addSetting(this.aI);
      this.addSetting(this.aJ);
   }

   @Override
   public void onTick() {
      if (mc.player != null && mc.currentScreen == null) {
         if (mc.options.useKey.isPressed()) {
            ItemStack var1 = mc.player.getMainHandStack();
            ItemStack var2 = mc.player.getOffHandStack();
            if (this.a(var1, var2)) {
               MinecraftClientAccessor var3 = (MinecraftClientAccessor)mc;
               int var4 = Math.max(0, this.aJ.getValue().intValue());
               if (var3.water$getItemUseCooldown() != var4) {
                  var3.water$setItemUseCooldown(var4);
               }
            }
         }
      }
   }

   private boolean a(ItemStack mainHand, ItemStack offHand) {
      boolean var3 = mainHand.isOf(Items.EXPERIENCE_BOTTLE);
      boolean var4 = offHand.isOf(Items.EXPERIENCE_BOTTLE);
      if (this.aG.getValue()) {
         return var3 || var4;
      } else {
         Item var6 = mainHand.getItem();
         Item var7 = offHand.getItem();
         if (this.e(mainHand) || this.e(offHand)) {
            return false;
         } else if (mainHand.isOf(Items.RESPAWN_ANCHOR)
            || mainHand.isOf(Items.GLOWSTONE)
            || offHand.isOf(Items.RESPAWN_ANCHOR)
            || offHand.isOf(Items.GLOWSTONE)) {
            return false;
         } else if (!(var6 instanceof RangedWeaponItem) && !(var7 instanceof RangedWeaponItem)) {
            ItemStack var5 = var6 instanceof BlockItem || var7 instanceof BlockItem;
            return var5 ? this.aH.getValue() : this.aI.getValue();
         } else {
            return false;
         }
      }
   }

   private boolean e(ItemStack stack) {
      return stack.getComponents().contains(DataComponentTypes.FOOD);
   }
}
