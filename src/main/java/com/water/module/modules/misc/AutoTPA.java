package com.water.module.modules.misc;

import com.water.module.Category;
import com.water.module.Module;
import com.water.setting.ModeSetting;
import com.water.setting.Setting;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.Packet;

public final class AutoTPA extends Module {
   private final Setting<String> N = new Setting<>("Player", "Player");
   private final ModeSetting b = new ModeSetting("Mode", "tpahere", "tpa", "tpahere");
   private final Setting<Float> O = new Setting<>("Min Delay", 10.0F, 1.0F, 100.0F);
   private final Setting<Float> P = new Setting<>("Max Delay", 30.0F, 1.0F, 100.0F);
   private int l = 0;
   private int ag = 0;
   private boolean ab = false;
   private int ah = -1;
   private int ai = -1;

   public AutoTPA() {
      super("AutoTPA", Category.c);
      this.addSetting(this.N);
      this.addSetting(this.b);
      this.addSetting(this.O);
      this.addSetting(this.P);
   }

   @Override
   public void onEnable() {
      this.l = 0;
      this.ag = 0;
      this.ab = false;
      this.ah = mc.player != null ? mc.player.getLastAttackedTime() : -1;
      this.ai = mc.player != null ? mc.player.getLastAttackTime() : -1;
   }

   @Override
   public void onDisable() {
      this.l = 0;
      this.ag = 0;
      this.ab = false;
      this.ah = -1;
      this.ai = -1;
   }

   @Override
   public void onPacketReceive(Packet<?> packet) {
      if (mc.player != null) {
         if (packet instanceof Packet var2 && var2.getEntityId() == mc.player.getId()) {
            this.al();
         }
      }
   }

   @Override
   public void onTick() {
      if (mc.player != null && mc.getNetworkHandler() != null) {
         int var1 = mc.player.getLastAttackedTime();
         if (var1 > 0 && var1 != this.ah) {
            this.ah = var1;
            if (mc.player.getLastAttacker() instanceof PlayerEntity var4 && var4 != mc.player && !var4.isSpectator()) {
               this.al();
            }
         }

         var1 = mc.player.getLastAttackTime();
         if (var1 > 0 && var1 != this.ai) {
            this.ai = var1;
            this.al();
         }

         if (this.ab) {
            if (this.ag > 0) {
               this.ag--;
               return;
            }

            this.ab = false;
            this.l = 0;
         }

         if (this.l > 0) {
            this.l--;
         } else {
            mc.getNetworkHandler().sendChatCommand(this.b.getValue() + " " + this.N.getValue().trim());
            var1 = this.O.getValue().intValue();
            int var2 = Math.max(var1, this.P.getValue().intValue());
            this.l = var1 + (int)(Math.random() * (var2 - var1 + 1));
         }
      }
   }

   private void al() {
      this.ab = true;
      this.ag = 400;
      this.l = 0;
   }
}
