package com.water.module.modules.combat;

import com.water.module.Category;
import com.water.module.Module;
import com.water.setting.Setting;
import java.util.Iterator;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;

public final class SpearSwap extends Module {
   public static SpearSwap INSTANCE;
   private final Setting<Boolean> lunge = new Setting<>("Lunge", true);
   private final Setting<Boolean> sharpness = new Setting<>("Sharpness", false);
   private final Setting<Boolean> onlySword = new Setting<>("Only Sword", false);
   private final Setting<Boolean> onlyAxe = new Setting<>("Only Axe", false);
   private final Setting<Boolean> switchBack = new Setting<>("Switch Back", true);
   private final Setting<Float> switchDelay = new Setting<>("Switch Delay", 1.0F, 1.0F, 20.0F);
   private int previousSlot = -1;
   private int countdown = 0;
   private boolean attackHeldLastCheck = false;

   public SpearSwap() {
      super("SpearSwap", Category.field_a_1);
      this.addSetting(this.lunge);
      this.addSetting(this.sharpness);
      this.addSetting(this.onlySword);
      this.addSetting(this.onlyAxe);
      this.addSetting(this.switchBack);
      this.addSetting(this.switchDelay);
      INSTANCE = this;
   }

   public void preAttack() {
      if (mc.player != null) {
         boolean var1 = this.attackHeldLastCheck;
         this.attackHeldLastCheck = true;
         if (!var1) {
            if (this.countdown <= 0) {
               PlayerInventory var3 = mc.player.getInventory();
               int var2 = this.findBestWeaponSlot();
               if (var2 >= 0) {
                  if (var3.getSelectedSlot() != var2) {
                     this.previousSlot = var3.getSelectedSlot();
                     var3.setSelectedSlot(var2);
                     this.countdown = Math.max(1, this.switchDelay.getValue().intValue());
                  }
               }
            }
         }
      }
   }

   public void noAttack() {
      this.attackHeldLastCheck = false;
   }

   @Override
   public void onEnable() {
      this.previousSlot = -1;
      this.countdown = 0;
   }

   @Override
   public void onDisable() {
      this.previousSlot = -1;
      this.countdown = 0;
   }

   @Override
   public void onTick() {
      if (mc.player != null && mc.options != null) {
         PlayerInventory var1 = mc.player.getInventory();
         if (this.countdown > 0) {
            this.countdown--;
            if (this.countdown == 0) {
               if (this.switchBack.getValue() && this.previousSlot >= 0 && this.previousSlot < 9 && var1.getSelectedSlot() != this.previousSlot) {
                  var1.setSelectedSlot(this.previousSlot);
               }

               this.previousSlot = -1;
            }
         }

         if (mc.options.attackKey.isPressed()) {
            this.preAttack();
         } else {
            this.noAttack();
         }
      }
   }

   private int findBestWeaponSlot() {
      PlayerInventory var1 = mc.player.getInventory();
      boolean var2 = this.onlySword.getValue();
      boolean var3 = this.onlyAxe.getValue();
      boolean var4 = this.lunge.getValue();
      boolean var5 = this.sharpness.getValue();
      int var6 = -1;
      int var7 = Integer.MIN_VALUE;

      for (int var8 = 0; var8 < 9; var8++) {
         ItemStack var9 = var1.getStack(var8);
         if (!var9.isEmpty()) {
            String var10 = Registries.ITEM.getId(var9.getItem()).getPath();
            String var11 = "";

            try {
               var11 = var9.getName().getString().toLowerCase();
            } catch (Throwable var15) {
            }

            boolean var12 = var10.endsWith("_sword");
            boolean var13 = var9.getItem() instanceof AxeItem;
            boolean var16 = var9.getItem() == Items.TRIDENT || var9.getItem() == Items.MACE || var10.contains("spear") || var11.contains("spear");
            boolean var17 = this.hasLungeEnchant(var9);
            boolean var18 = var4 && var17;
            if ((!var2 || var12) && (!var3 || var13) && (var2 || var3 || var12 || var13 || var16 || var18)) {
               int var14 = 0;
               if (var18) {
                  var14 += 500;
               }

               if (var16) {
                  var14 += 300;
               } else if (var12) {
                  var14 += 200;
               } else if (var13) {
                  var14 += 100;
               }

               if (var5) {
                  var14 += this.sharpnessLevel(var9) * 60;
               }

               if (var14 > var7) {
                  var7 = var14;
                  var6 = var8;
               }
            }
         }
      }

      return var6;
   }

   // $VF: Handled exception range with multiple entry points by splitting it
   // $VF: Duplicated exception handlers to handle obfuscated exceptions
   private boolean hasLungeEnchant(ItemStack s) {
      try {
         var7 = s.get(DataComponentTypes.ENCHANTMENTS);
         if (var7 == null) {
            return false;
         }
      } catch (Throwable var6) {
         return false;
      }

      try {
         var8 = var7.getEnchantments().iterator();
      } catch (Throwable var4) {
         return false;
      }

      while (true) {
         RegistryKey var9;
         try {
            if (!var8.hasNext()) {
               break;
            }

            RegistryEntry var2 = (RegistryEntry)var8.next();
            var9 = (RegistryKey)var2.getKey().orElse(null);
            if (var9 == null) {
               continue;
            }

            if (var9.equals(Enchantments.WIND_BURST) || var9.equals(Enchantments.DENSITY) || var9.equals(Enchantments.RIPTIDE)) {
               return true;
            }
         } catch (Throwable var5) {
            break;
         }

         try {
            String var10 = var9.getValue().toString().toLowerCase();
            if (var10.contains("lunge")) {
               return true;
            }
         } catch (Throwable var3) {
            break;
         }
      }

      return false;
   }

   // $VF: Handled exception range with multiple entry points by splitting it
   // $VF: Duplicated exception handlers to handle obfuscated exceptions
   private int sharpnessLevel(ItemStack s) {
      try {
         var8 = s.get(DataComponentTypes.ENCHANTMENTS);
         if (var8 == null) {
            return 0;
         }
      } catch (Throwable var7) {
         return 0;
      }

      Iterator var2;
      try {
         var2 = var8.getEnchantments().iterator();
      } catch (Throwable var5) {
         return 0;
      }

      while (true) {
         try {
            if (!var2.hasNext()) {
               break;
            }

            RegistryEntry var3 = (RegistryEntry)var2.next();
            RegistryKey var4 = (RegistryKey)var3.getKey().orElse(null);
            if (var4 != null && var4.equals(Enchantments.SHARPNESS)) {
               return var8.getLevel(var3);
            }
         } catch (Throwable var6) {
            break;
         }
      }

      return 0;
   }
}
