package com.water.module.modules.donut;

import com.water.module.Category;
import com.water.module.Module;
import com.water.setting.Setting;
import java.util.ArrayList;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Entity.RemovalReason;

public final class AntiTrap extends Module {
   private final Setting<Boolean> bj = new Setting<>("Armor Stands", true);
   private final Setting<Boolean> bk = new Setting<>("Minecarts", true);
   private final Setting<Boolean> bl = new Setting<>("Chest Minecarts", true);
   private final Setting<Boolean> bm = new Setting<>("Hopper Minecarts", true);

   public AntiTrap() {
      super("AntiTrap", Category.d);
      this.addSetting(this.bj);
      this.addSetting(this.bk);
      this.addSetting(this.bl);
      this.addSetting(this.bm);
   }

   @Override
   public void onEnable() {
      this.w();
   }

   @Override
   public void onTick() {
      this.w();
   }

   private void w() {
      if (mc.world != null) {
         ArrayList var1 = new ArrayList();
         mc.world.getEntities().forEach(entity -> {
            if (entity != null && this.a(entity.getType())) {
               var1.add(entity);
            }
         });
         var1.forEach(e -> {
            if (!e.isRemoved()) {
               e.remove(RemovalReason.DISCARDED);
            }
         });
      }
   }

   private boolean a(EntityType<?> type) {
      if (type == null) {
         return false;
      } else if (this.bj.getValue() && type.equals(EntityType.ARMOR_STAND)) {
         return true;
      } else if (this.bk.getValue() && type.equals(EntityType.MINECART)) {
         return true;
      } else {
         return this.bl.getValue() && type.equals(EntityType.CHEST_MINECART) ? true : this.bm.getValue() && type.equals(EntityType.HOPPER_MINECART);
      }
   }
}
