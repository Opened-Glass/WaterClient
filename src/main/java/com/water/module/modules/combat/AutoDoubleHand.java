package com.water.module.modules.combat;

import com.water.module.Category;
import com.water.module.Module;
import com.water.setting.Setting;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Items;

public final class AutoDoubleHand extends Module {
   private final Setting<Boolean> am = new Setting<>("On Totem Pop", true);
   private final Setting<Boolean> an = new Setting<>("On Health", true);
   private final Setting<Float> ao = new Setting<>("Health Threshold", 6.0F, 1.0F, 20.0F);
   private final Setting<Float> ap = new Setting<>("Cooldown", 5.0F, 0.0F, 40.0F);
   private boolean n = false;
   private int i = 0;
   private int previousSlot = -1;

   public AutoDoubleHand() {
      super("AutoDoubleHand", Category.field_a_1);
      this.addSetting(this.am);
      this.addSetting(this.an);
      this.addSetting(this.ao);
      this.addSetting(this.ap);
   }

   @Override
   public void onEnable() {
      this.n = false;
      this.i = 0;
      this.previousSlot = -1;
   }

   @Override
   public void onDisable() {
      this.n = false;
      this.i = 0;
      this.previousSlot = -1;
   }

   @Override
   public void onTick() {
      if (mc.player != null && mc.interactionManager != null) {
         if (this.i > 0) {
            this.i--;
         }

         PlayerInventory var1 = mc.player.getInventory();
         int var2 = mc.player.getMainHandStack().getItem() == Items.TOTEM_OF_UNDYING;
         boolean var3 = mc.player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING;
         var3 = var2 || var3;
         boolean var4 = this.n && !var3;
         this.n = var3;
         if (this.i <= 0) {
            var3 = false;
            if (this.am.getValue() && var4) {
               var3 = true;
            }

            if (this.an.getValue() && mc.player.getHealth() <= this.ao.getValue()) {
               var3 = true;
            }

            if (var3) {
               if (!var2) {
                  var2 = this.e();
                  if (var2 >= 0) {
                     if (var1.getSelectedSlot() != var2) {
                        this.previousSlot = var1.getSelectedSlot();
                        var1.setSelectedSlot(var2);
                        this.i = this.ap.getValue().intValue();
                     }
                  }
               }
            }
         }
      }
   }

   private int e() {
      PlayerInventory var1 = mc.player.getInventory();

      for (int var2 = 0; var2 < 9; var2++) {
         if (var1.getStack(var2).isOf(Items.TOTEM_OF_UNDYING)) {
            return var2;
         }
      }

      return -1;
   }
}
