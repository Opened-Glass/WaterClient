package com.water.module.modules.misc;

import com.water.module.Category;
import com.water.module.Module;
import java.util.function.Predicate;
import net.minecraft.block.BambooBlock;
import net.minecraft.block.BambooShootBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.AttributeModifiersComponent.Entry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShearsItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;

public final class AutoTool extends Module {
   public AutoTool() {
      super("Auto Tool", Category.c);
   }

   @Override
   public void onTick() {
      if (mc.player != null && mc.world != null && mc.interactionManager != null) {
         if (mc.options.attackKey.isPressed() && mc.crosshairTarget != null) {
            HitResult var1 = mc.crosshairTarget;
            if (var1.getType() == Type.ENTITY && var1 instanceof EntityHitResult) {
               this.am();
            } else {
               if (var1.getType() == Type.BLOCK && var1 instanceof BlockHitResult var2) {
                  this.a(var2.getBlockPos());
               }
            }
         }
      }
   }

   private void a(BlockPos blockPos) {
      BlockPos var11 = mc.world.getBlockState(blockPos);
      ItemStack var2 = mc.player.getMainHandStack();
      int var3 = -1;
      double var5 = -1.0;

      for (int var7 = 0; var7 < 9; var7++) {
         ItemStack var8 = mc.player.getInventory().getStack(var7);
         double var9 = a(var8, var11, itemStack -> true);
         if (var9 > var5) {
            var5 = var9;
            var3 = var7;
         }
      }

      if (var3 != -1) {
         double var12 = a(var2, var11, itemStack -> true);
         if (var5 > var12 || !b(var2)) {
            this.c(var3);
         }
      }
   }

   private void am() {
      int var1 = -1;
      double var2 = Double.NEGATIVE_INFINITY;

      for (int var4 = 0; var4 < 9; var4++) {
         ItemStack var5 = mc.player.getInventory().getStack(var4);
         if (!var5.isEmpty()) {
            double var6 = this.a(var5);
            if (var6 > var2) {
               var2 = var6;
               var1 = var4;
            }
         }
      }

      if (var1 != -1) {
         this.c(var1);
      }
   }

   private double a(ItemStack stack) {
      AttributeModifiersComponent var2 = stack.get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
      double var3 = 0.0;
      if (var2 != null) {
         for (Entry var5 : var2.modifiers()) {
            if (var5.attribute().toString().contains("attack_damage")) {
               var3 += var5.modifier().value();
            }
         }
      }

      if (var3 > 0.0) {
         return var3;
      } else {
         String var7 = Registries.ITEM.getId(stack.getItem()).getPath();
         if (var7.endsWith("_sword")) {
            return 10.0 + this.a(var7);
         } else {
            return var7.endsWith("_axe") ? 5.0 + this.a(var7) : 0.0;
         }
      }
   }

   private void c(int slot) {
      if (slot >= 0 && slot <= 8) {
         if (mc.player.getInventory().getSelectedSlot() != slot) {
            mc.player.getInventory().setSelectedSlot(slot);
         }
      }
   }

   public static double a(ItemStack itemStack, BlockState blockState, Predicate<ItemStack> predicate) {
      if (predicate.test(itemStack) && b(itemStack)) {
         Predicate var3 = Registries.ITEM.getId(itemStack.getItem()).getPath();
         Predicate var4 = var3.endsWith("_sword");
         return !itemStack.isSuitableFor(blockState)
               && (!var4 || !(blockState.getBlock() instanceof BambooBlock) && !(blockState.getBlock() instanceof BambooShootBlock))
               && (!(itemStack.getItem() instanceof ShearsItem) || !(blockState.getBlock() instanceof LeavesBlock))
               && !blockState.isIn(BlockTags.WOOL)
            ? -1.0
            : itemStack.getMiningSpeedMultiplier(blockState) * 1000.0F;
      } else {
         return -1.0;
      }
   }

   public static boolean b(ItemStack itemStack) {
      return b(itemStack.getItem());
   }

   public static boolean b(Item item) {
      if (item instanceof ShearsItem) {
         return true;
      } else {
         Item var1 = Registries.ITEM.getId(item).getPath();
         return var1.endsWith("_pickaxe") || var1.endsWith("_axe") || var1.endsWith("_shovel") || var1.endsWith("_hoe") || var1.endsWith("_sword");
      }
   }

   private double a(String itemPath) {
      if (itemPath.startsWith("netherite_")) {
         return 6.0;
      } else if (itemPath.startsWith("diamond_")) {
         return 5.0;
      } else if (itemPath.startsWith("iron_")) {
         return 4.0;
      } else if (itemPath.startsWith("golden_")) {
         return 3.0;
      } else if (itemPath.startsWith("stone_")) {
         return 2.0;
      } else {
         return itemPath.startsWith("wooden_") ? 1.0 : 0.0;
      }
   }
}
