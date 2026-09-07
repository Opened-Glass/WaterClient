package com.water.module.modules.combat;

import com.water.module.Category;
import com.water.module.Module;
import com.water.setting.Setting;
import java.util.Random;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

public final class AutoInventoryTotem extends Module {
   private final Setting<Float> aq = new Setting<>("Delay", 2.0F, 0.0F, 20.0F);
   private final Setting<Boolean> ar = new Setting<>("Hotbar", false);
   private final Setting<Float> as = new Setting<>("Totem Slot", 1.0F, 1.0F, 9.0F);
   private final Setting<Boolean> at = new Setting<>("Force Totem", false);
   private final Setting<Boolean> au = new Setting<>("Auto Open", false);
   private final Setting<Float> av = new Setting<>("Close Delay", 3.0F, 0.0F, 20.0F);
   private int j = 0;
   private int k = 0;
   private boolean o = true;
   private final Random b = new Random();

   public AutoInventoryTotem() {
      super("Auto Inv Totem", Category.field_a_1);
      this.addSetting(this.aq);
      this.addSetting(this.ar);
      this.addSetting(this.as);
      this.addSetting(this.at);
      this.addSetting(this.au);
      this.addSetting(this.av);
   }

   @Override
   public void onEnable() {
      this.j = 0;
      this.k = 0;
      this.o = true;
      super.onEnable();
   }

   @Override
   public void onDisable() {
      this.j = 0;
      this.k = 0;
      super.onDisable();
   }

   @Override
   public void onTick() {
      if (mc.player != null && mc.interactionManager != null) {
         PlayerInventory var1 = mc.player.getInventory();
         boolean var2 = mc.player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING;
         if (this.au.getValue()) {
            this.a(var1, var2);
            this.o = var2;
         } else {
            this.o = var2;
            if (!(mc.currentScreen instanceof InventoryScreen)) {
               this.k = 0;
            } else if (this.k < this.aq.getValue().intValue() + this.f()) {
               this.k++;
            } else if (!var2 && this.method_a_1(var1)) {
               this.k = 0;
            } else {
               if (this.ar.getValue()) {
                  this.method_b_1(var1);
               }

               this.k = 0;
            }
         }
      }
   }

   private void a(PlayerInventory inv, boolean totemInOffhand) {
      switch (this.j) {
         case 0:
            if (this.o && !totemInOffhand && this.method_a_2(inv) != -1) {
               this.j = 1;
               this.k = this.aq.getValue().intValue() <= 0 ? 1 + this.b.nextInt(2) : 1 + this.b.nextInt(3);
            }

            if (!totemInOffhand && this.j == 0 && !(mc.currentScreen instanceof InventoryScreen) && this.method_a_2(inv) != -1) {
               this.j = 1;
               this.k = this.aq.getValue().intValue() <= 0 ? 1 + this.b.nextInt(2) : 1 + this.b.nextInt(3);
            }
            break;
         case 1:
            if (this.k > 0) {
               this.k--;
               return;
            }

            if (!(mc.currentScreen instanceof InventoryScreen)) {
               mc.setScreen(new InventoryScreen(mc.player));
            }

            this.j = 2;
            this.k = this.aq.getValue().intValue() + this.f();
            break;
         case 2:
            if (!(mc.currentScreen instanceof InventoryScreen)) {
               this.j = 0;
               return;
            }

            if (this.k > 0) {
               this.k--;
               return;
            }

            boolean var3 = false;
            if (!totemInOffhand) {
               var3 = this.method_a_1(inv);
            }

            boolean var4 = false;
            if (this.ar.getValue()) {
               var4 = this.method_b_1(inv);
            }

            if (!var3 && !var4 && !totemInOffhand) {
               this.j = 3;
               this.k = 1;
            } else {
               this.j = 3;
               this.k = this.av.getValue().intValue() + this.f();
            }
            break;
         case 3:
            if (this.k > 0) {
               this.k--;
               return;
            }

            if (mc.currentScreen instanceof InventoryScreen) {
               mc.player.closeHandledScreen();
               mc.setScreen(null);
            }

            this.j = 0;
      }
   }

   private boolean method_a_1(PlayerInventory inv) {
      PlayerInventory var2 = this.method_a_2(inv);
      if (var2 == -1) {
         return false;
      } else {
         PlayerInventory var3 = e(var2);
         mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, var3, 40, SlotActionType.SWAP, mc.player);
         return true;
      }
   }

   private boolean method_b_1(PlayerInventory inv) {
      int var2 = this.as.getValue().intValue() - 1;
      if (inv.getStack(var2).getItem() == Items.TOTEM_OF_UNDYING) {
         return false;
      } else if (!inv.getStack(var2).isEmpty() && !this.at.getValue()) {
         return false;
      } else {
         PlayerInventory var3 = this.method_b_2(inv);
         if (var3 == -1) {
            return false;
         } else {
            PlayerInventory var4 = e(var3);
            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, var4, var2, SlotActionType.SWAP, mc.player);
            return true;
         }
      }
   }

   private int method_a_2(PlayerInventory inv) {
      for (int var2 = 9; var2 < 36; var2++) {
         if (inv.getStack(var2).getItem() == Items.TOTEM_OF_UNDYING) {
            return var2;
         }
      }

      for (int var3 = 0; var3 < 9; var3++) {
         if (inv.getStack(var3).getItem() == Items.TOTEM_OF_UNDYING) {
            return var3;
         }
      }

      return -1;
   }

   private int method_b_2(PlayerInventory inv) {
      for (int var2 = 9; var2 < 36; var2++) {
         if (inv.getStack(var2).getItem() == Items.TOTEM_OF_UNDYING) {
            return var2;
         }
      }

      return -1;
   }

   private static int e(int invSlot) {
      return invSlot < 9 ? 36 + invSlot : invSlot;
   }

   private int f() {
      return this.aq.getValue().intValue() <= 0 ? 0 : this.b.nextInt(2);
   }
}
