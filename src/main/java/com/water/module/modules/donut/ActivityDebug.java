package com.water.module.modules.donut;

import com.water.gui.ToastManager;
import com.water.module.Category;
import com.water.module.Module;
import com.water.setting.Setting;
import com.water.utils.RenderUtils;
import java.awt.Color;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;

public final class ActivityDebug extends Module {
   private final Setting<Float> bh = new Setting<>("y-level", 16.0F, -64.0F, 320.0F);
   private final Setting<Boolean> bi = new Setting<>("Notification", false);
   private final Set<ChunkPos> field_b_1 = Collections.newSetFromMap(new ConcurrentHashMap<>());
   private final Map<Long, Long> field_b_2 = new ConcurrentHashMap<>();
   private static final Color field_b_3 = new Color(255, 220, 0, 180);
   private static final Color field_c_1 = new Color(255, 220, 0, 255);
   private final Map<Class<?>, List<Field>> field_c_2 = new ConcurrentHashMap<>();
   private final Map<Class<?>, List<Field>> d = new ConcurrentHashMap<>();
   private final Map<Class<?>, List<Field>> e = new ConcurrentHashMap<>();
   private final Map<Class<?>, List<Field>> f = new ConcurrentHashMap<>();
   private final ThreadLocal<Set<Integer>> a = ThreadLocal.withInitial(() -> Collections.newSetFromMap(new ConcurrentHashMap<>()));

   public ActivityDebug() {
      super("ActivityDebug", Category.d);
      this.addSetting(this.bh);
      this.addSetting(this.bi);
   }

   @Override
   public void onDisable() {
      this.field_b_1.clear();
      this.field_b_2.clear();
      this.field_c_2.clear();
      this.d.clear();
      this.e.clear();
      this.f.clear();
   }

   @Override
   public void onPacketReceive(Packet<?> packet) {
      if (packet instanceof Packet var4) {
         var4.visitUpdates((pos, state) -> this.a(pos.getX(), pos.getY(), pos.getZ()));
      } else if (packet instanceof Packet var2) {
         Packet var3 = var2.getPos();
         this.a(var3.getX(), var3.getY(), var3.getZ());
      } else {
         this.a.get().clear();
         this.a(packet, 0);
      }
   }

   private void a(Object obj, int depth) {
      if (obj != null && depth <= 3) {
         int var3 = System.identityHashCode(obj);
         if (this.a.get().add(var3)) {
            Class var16 = obj.getClass();
            this.e.computeIfAbsent(var16, c -> this.a((Class<?>)c, BlockPos.class));
            this.f.computeIfAbsent(var16, c -> this.a((Class<?>)c, Vec3d.class));
            this.field_c_2.computeIfAbsent(var16, c -> this.a((Class<?>)c, double.class));
            this.d
               .computeIfAbsent(
                  var16,
                  c -> {
                     ArrayList var1 = new ArrayList();

                     while (c != null && c != Object.class) {
                        for (Field var5 : c.getDeclaredFields()) {
                           if (!Modifier.isStatic(var5.getModifiers())
                              && !var5.getType().isPrimitive()
                              && !var5.getType().getName().startsWith("java.")
                              && !var5.getType().isEnum()) {
                              var5.setAccessible(true);
                              var1.add(var5);
                           }
                        }

                        c = c.getSuperclass();
                     }

                     return var1;
                  }
               );

            for (Field var6 : this.e.get(var16)) {
               try {
                  BlockPos var7 = (BlockPos)var6.get(obj);
                  if (var7 != null) {
                     this.a(var7.getX(), var7.getY(), var7.getZ());
                  }
               } catch (Exception var15) {
               }
            }

            for (Field var19 : this.f.get(var16)) {
               try {
                  Vec3d var22 = (Vec3d)var19.get(obj);
                  if (var22 != null) {
                     this.a(var22.x, var22.y, var22.z);
                  }
               } catch (Exception var14) {
               }
            }

            List var18 = this.field_c_2.get(var16);
            if (var18.size() >= 3) {
               try {
                  double var20 = ((Field)var18.get(0)).getDouble(obj);
                  double var8 = ((Field)var18.get(1)).getDouble(obj);
                  double var10 = ((Field)var18.get(2)).getDouble(obj);
                  if (Math.abs(var20) < 3.0E7 && Math.abs(var10) < 3.0E7 && var8 > -2048.0 && var8 < 2048.0) {
                     this.a(var20, var8, var10);
                  }
               } catch (Exception var13) {
               }
            }

            for (Field var23 : this.d.get(var16)) {
               try {
                  Object var24 = var23.get(obj);
                  if (var24 != null) {
                     this.a(var24, depth + 1);
                  }
               } catch (Exception var12) {
               }
            }
         }
      }
   }

   private List<Field> a(Class<?> clazz, Class<?> type) {
      ArrayList var3 = new ArrayList();

      while (clazz != null && clazz != Object.class) {
         for (Field var7 : clazz.getDeclaredFields()) {
            if (!Modifier.isStatic(var7.getModifiers()) && var7.getType() == type) {
               var7.setAccessible(true);
               var3.add(var7);
            }
         }

         clazz = clazz.getSuperclass();
      }

      return var3;
   }

   private void a(double x, double y, double z) {
      if (mc.player == null || !(mc.player.getY() < 0.0)) {
         if (y <= this.bh.getValue().floatValue()) {
            double var7 = new ChunkPos((int)Math.floor(x) >> 4, (int)Math.floor(z) >> 4);
            if (this.field_b_1.add(var7)) {
               this.a(var7, y);
            }
         }
      }
   }

   private void a(ChunkPos chunkPos, double y) {
      if (this.bi.getValue() && mc.player != null && mc.world != null) {
         long var4 = chunkPos.toLong();
         long var6 = System.currentTimeMillis();
         long var8 = this.field_b_2.getOrDefault(var4, 0L);
         if (var6 - var8 >= 1250L) {
            this.field_b_2.put(var4, var6);
            ToastManager.INSTANCE
               .push(
                  "Activity detected",
                  "Chunk " + chunkPos.x + ", " + chunkPos.z + "  Y " + (int)Math.floor(y),
                  new ItemStack(Items.COMPASS),
                  field_c_1.getRGB()
               );
            mc.world
               .playSound(
                  mc.player, mc.player.getX(), mc.player.getY(), mc.player.getZ(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.MASTER, 0.6F, 1.05F
               );
         }
      }
   }

   @Override
   public void onRender(MatrixStack matrices, float tickDelta) {
      if (mc.world != null) {
         float var23 = RenderUtils.getCamera();
         if (var23 != null) {
            float var24 = RenderUtils.getCameraPos(var23);
            double var5 = Math.max((double)mc.world.getBottomY(), Math.min(57.0, (double)mc.world.getTopYInclusive()));
            MatrixStack var22 = RenderUtils.beginWorldBatch(matrices);

            for (ChunkPos var4 : this.field_b_1) {
               if (RenderUtils.isWorldBoxVisible(var4.getStartX(), 57.0, var4.getStartZ(), var4.getEndX(), 57.05, var4.getEndZ())) {
                  double var10 = var4.getStartX() - var24.x;
                  double var12 = var4.getStartZ() - var24.z;
                  double var14 = var5 - var24.y;
                  double var16 = var10 + 16.0;
                  double var18 = var14 + 0.05;
                  double var20 = var12 + 16.0;
                  var22.renderFilledBox(var10, var14, var12, var16, var18, var20, field_b_3);
               }
            }

            var22.flush();
         }
      }
   }
}
