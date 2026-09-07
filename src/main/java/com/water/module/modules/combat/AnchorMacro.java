package com.water.module.modules.combat;

import com.water.module.ActivatableModule;
import com.water.module.Category;
import com.water.setting.Setting;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.ShieldItem;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.HealthUpdateS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.glfw.GLFW;

public final class AnchorMacro extends ActivatableModule {
   private final Setting<Float> ab = new Setting<>("Switch Delay", 0.0F, 0.0F, 20.0F);
   private final Setting<Float> ac = new Setting<>("Glowstone Delay", 0.0F, 0.0F, 20.0F);
   private final Setting<Float> ad = new Setting<>("Explode Delay", 0.0F, 0.0F, 20.0F);
   private final Setting<Float> ae = new Setting<>("Totem Slot", 1.0F, 1.0F, 9.0F);
   private final Setting<Boolean> af = new Setting<>("Switch Back", false);
   private int d;
   private int e;
   private int f;
   private boolean l = false;

   public AnchorMacro() {
      super("Anchor Macro", Category.field_a_1);
      this.addSetting(this.ab);
      this.addSetting(this.ac);
      this.addSetting(this.ad);
      this.addSetting(this.ae);
      this.addSetting(this.af);
   }

   @Override
   public void onEnable() {
      this.t();
      this.l = false;
      super.onEnable();
   }

   @Override
   public void onDisable() {
      this.t();
      this.l = false;
      super.onDisable();
   }

   @Override
   public void onPacketReceive(Packet<?> packet) {
      if (this.af.getValue() && this.l && packet instanceof HealthUpdateS2CPacket) {
         this.l = false;
         if (mc.player != null) {
            Packet var2 = this.method_a_1(Items.RESPAWN_ANCHOR);
            if (var2 != -1) {
               mc.player.getInventory().setSelectedSlot(var2);
            }
         }
      }
   }

   @Override
   public void onTick() {
      if (mc.player != null && mc.world != null && mc.interactionManager != null) {
         if (mc.currentScreen == null) {
            if (!this.f()) {
               if (!this.g()) {
                  this.t();
               } else {
                  this.s();
               }
            }
         }
      }
   }

   private boolean f() {
      boolean var1 = mc.player.getMainHandStack().getItem().getComponents().contains(DataComponentTypes.FOOD)
         || mc.player.getOffHandStack().getItem().getComponents().contains(DataComponentTypes.FOOD);
      boolean var2 = mc.player.getMainHandStack().getItem() instanceof ShieldItem || mc.player.getOffHandStack().getItem() instanceof ShieldItem;
      boolean var3 = this.g();
      return (var1 || var2) && var3;
   }

   private boolean g() {
      return mc.getWindow() != null && GLFW.glfwGetMouseButton(mc.getWindow().getHandle(), 1) == 1;
   }

   private void s() {
      if (mc.crosshairTarget instanceof BlockHitResult var1) {
         if (var1.getType() == Type.BLOCK) {
            BlockPos var3 = var1.getBlockPos();
            BlockState var4 = mc.world.getBlockState(var3);
            if (var4.isOf(Blocks.RESPAWN_ANCHOR)) {
               mc.options.useKey.setPressed(false);
               int var5 = var4.get(RespawnAnchorBlock.CHARGES);
               if (var5 == 0) {
                  this.a(var1);
               } else {
                  this.b(var1);
               }
            }
         }
      }
   }

   private void a(BlockHitResult blockHitResult) {
      if (!mc.player.getMainHandStack().isOf(Items.GLOWSTONE)) {
         if (this.d < this.ab.getValue().intValue()) {
            this.d++;
            return;
         }

         this.d = 0;
         if (!this.method_a_2(Items.GLOWSTONE)) {
            return;
         }
      }

      if (mc.player.getMainHandStack().isOf(Items.GLOWSTONE)) {
         if (this.e < this.ac.getValue().intValue()) {
            this.e++;
            return;
         }

         this.e = 0;
         this.c(blockHitResult);
      }
   }

   private void b(BlockHitResult blockHitResult) {
      int var2 = Math.max(0, Math.min(8, this.ae.getValue().intValue() - 1));
      if (mc.player.getInventory().getSelectedSlot() != var2) {
         if (this.d < this.ab.getValue().intValue()) {
            this.d++;
            return;
         }

         this.d = 0;
         mc.player.getInventory().setSelectedSlot(var2);
      }

      if (mc.player.getInventory().getSelectedSlot() == var2) {
         if (this.f < this.ad.getValue().intValue()) {
            this.f++;
            return;
         }

         this.f = 0;
         this.c(blockHitResult);
         if (this.af.getValue()) {
            this.l = true;
         }
      }
   }

   private int method_a_1(Item item) {
      for (int var2 = 0; var2 < 9; var2++) {
         if (mc.player.getInventory().getStack(var2).isOf(item)) {
            return var2;
         }
      }

      return -1;
   }

   private boolean method_a_2(Item item) {
      Item var2 = this.method_a_1(item);
      if (var2 != -1) {
         mc.player.getInventory().setSelectedSlot(var2);
         return true;
      } else {
         return false;
      }
   }

   private void c(BlockHitResult hit) {
      mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
      mc.player.swingHand(Hand.MAIN_HAND);
   }

   private void t() {
      this.d = 0;
      this.e = 0;
      this.f = 0;
   }
}
