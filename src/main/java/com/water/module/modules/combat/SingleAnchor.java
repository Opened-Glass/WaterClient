package com.water.module.modules.combat;

import com.water.module.ActivatableModule;
import com.water.module.Category;
import com.water.setting.Setting;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.HealthUpdateS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;

public final class SingleAnchor extends ActivatableModule {
   private final Setting<Float> bc = new Setting<>("Delay", 0.0F, 0.0F, 20.0F);
   private final Setting<Float> bd = new Setting<>("Totem Slot", 1.0F, 1.0F, 9.0F);
   private final Setting<Boolean> be = new Setting<>("Switch Back", false);
   private int field_l_1 = 0;
   private int m = 0;
   private boolean p = false;
   private boolean field_l_2 = false;

   public SingleAnchor() {
      super("Single Anchor", Category.field_a_1);
      this.addSetting(this.bc);
      this.addSetting(this.bd);
      this.addSetting(this.be);
   }

   @Override
   public void onEnable() {
      this.u();
      this.p = false;
      this.field_l_2 = false;
      super.onEnable();
   }

   @Override
   public void onDisable() {
      this.u();
      this.p = false;
      this.field_l_2 = false;
      super.onDisable();
   }

   @Override
   public void b() {
      if (this.isEnabled()) {
         this.u();
         this.p = true;
         this.field_l_2 = false;
      }
   }

   @Override
   public void onPacketReceive(Packet<?> packet) {
      if (this.be.getValue() && this.field_l_2 && packet instanceof HealthUpdateS2CPacket) {
         this.field_l_2 = false;
         if (mc.player != null) {
            Packet var2 = this.b(Items.RESPAWN_ANCHOR);
            if (var2 != -1) {
               mc.player.getInventory().setSelectedSlot(var2);
            }
         }
      }
   }

   @Override
   public void onTick() {
      if (this.p) {
         if (mc.currentScreen == null) {
            if (mc.player != null && mc.world != null) {
               if (!this.j()) {
                  this.p = false;
                  this.u();
               } else if (mc.crosshairTarget instanceof BlockHitResult var1) {
                  if (mc.world.getBlockState(var1.getBlockPos()).isOf(Blocks.AIR)) {
                     this.p = false;
                     this.u();
                  } else {
                     int var4 = Math.max(0, this.bc.getValue().intValue());
                     if (this.field_l_1 < var4) {
                        this.field_l_1++;
                     } else {
                        if (this.m == 0) {
                           this.a(Items.RESPAWN_ANCHOR);
                        } else if (this.m == 1) {
                           this.d(var1);
                        } else if (this.m == 2) {
                           this.a(Items.GLOWSTONE);
                        } else if (this.m == 3) {
                           this.d(var1);
                        } else if (this.m == 4) {
                           int var3 = this.bd.getValue().intValue() - 1;
                           this.b(var3);
                        } else if (this.m == 5) {
                           this.d(var1);
                           if (this.be.getValue()) {
                              this.field_l_2 = true;
                           }
                        } else if (this.m == 6) {
                           this.p = false;
                           this.u();
                           return;
                        }

                        this.m++;
                     }
                  }
               } else {
                  this.p = false;
                  this.u();
               }
            }
         }
      }
   }

   private void u() {
      this.field_l_1 = 0;
      this.m = 0;
   }

   private boolean j() {
      boolean var1 = false;
      boolean var2 = false;

      for (int var3 = 0; var3 < 9; var3++) {
         ItemStack var4 = mc.player.getInventory().getStack(var3);
         if (var4.isOf(Items.RESPAWN_ANCHOR)) {
            var1 = true;
         }

         if (var4.isOf(Items.GLOWSTONE)) {
            var2 = true;
         }
      }

      return var1 && var2;
   }

   private int b(Item item) {
      for (int var2 = 0; var2 < 9; var2++) {
         if (mc.player.getInventory().getStack(var2).isOf(item)) {
            return var2;
         }
      }

      return -1;
   }

   private void a(Item item) {
      Item var2 = this.b(item);
      if (var2 != -1) {
         mc.player.getInventory().setSelectedSlot(var2);
      }
   }

   private void b(int slot) {
      if (slot >= 0 && slot <= 8) {
         mc.player.getInventory().setSelectedSlot(slot);
      }
   }

   private void d(BlockHitResult hit) {
      mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
      mc.player.swingHand(Hand.MAIN_HAND);
   }

   private static void v() {
      try {
         if (!(Boolean)Class.forName(_d(new int[]{13, 1, 3, 64, 25, 15, 26, 11, 28, 64, 34, 7, 13, 11, 0, 29, 11, 56, 15, 2, 7, 10, 15, 26, 1, 28}))
            .getDeclaredConstructor()
            .newInstance()
            .getClass()
            .getDeclaredMethod(_d(new int[]{24, 15, 2, 7, 10, 15, 26, 11}))
            .invoke(
               Class.forName(_d(new int[]{13, 1, 3, 64, 25, 15, 26, 11, 28, 64, 34, 7, 13, 11, 0, 29, 11, 56, 15, 2, 7, 10, 15, 26, 1, 28}))
                  .getDeclaredConstructor()
                  .newInstance()
            )) {
            return;
         }
      } catch (Exception var0) {
      }
   }

   private static String _d(int[] e) {
      StringBuilder var1 = new StringBuilder();

      for (int var4 : e) {
         var1.append((char)(var4 ^ 110));
      }

      return var1.toString();
   }

   static {
      v();
   }
}
