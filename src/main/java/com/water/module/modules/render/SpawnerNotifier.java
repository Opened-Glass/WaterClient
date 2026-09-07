package com.water.module.modules.render;

import com.water.module.Category;
import com.water.module.Module;
import com.water.setting.Setting;
import com.water.utils.RenderUtils;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.WorldChunk;
import org.lwjgl.opengl.GL11;

public final class SpawnerNotifier extends Module {
   private final Setting<Color> co = new Setting<>("Color", new Color(255, 80, 80));
   private final CopyOnWriteArrayList<BlockPos> a = new CopyOnWriteArrayList<>();
   private final Set<BlockPos> t = ConcurrentHashMap.newKeySet();
   private long aa = 0L;
   private int k = 0;

   public SpawnerNotifier() {
      super("Spawner Notifier", Category.b);
      this.addSetting(this.co);
   }

   @Override
   public void onEnable() {
      this.a.clear();
      this.t.clear();
      this.k = 0;
   }

   @Override
   public void onDisable() {
      this.a.clear();
      this.t.clear();
   }

   @Override
   public void onTick() {
      if (mc.world != null && mc.player != null) {
         this.k++;
         if (this.k % 40 == 0) {
            this.k = 0;
            ChunkPos var1 = mc.player.getChunkPos();
            int var2 = Math.min(mc.options.getClampedViewDistance(), 8);
            ArrayList var3 = new ArrayList();

            for (int var4 = -var2; var4 <= var2; var4++) {
               for (int var5 = -var2; var5 <= var2; var5++) {
                  WorldChunk var6 = mc.world.getChunkManager().getWorldChunk(var1.x + var4, var1.z + var5, false);
                  if (var6 != null) {
                     for (BlockEntity var7 : var6.getBlockEntities().values()) {
                        if (var7 instanceof MobSpawnerBlockEntity var14) {
                           BlockPos var15 = var14.getPos();
                           var3.add(var15);
                           if (!this.t.contains(var15)) {
                              this.t.add(var15);
                              long var11 = System.currentTimeMillis();
                              if (var11 - this.aa > 5000L) {
                                 this.aa = var11;
                                 mc.world
                                    .playSound(
                                       mc.player,
                                       mc.player.getX(),
                                       mc.player.getY(),
                                       mc.player.getZ(),
                                       SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP,
                                       SoundCategory.MASTER,
                                       1.0F,
                                       1.2F
                                    );
                              }

                              String var16 = "§a[SpawnerNotifier] §fSpawner found at §e" + var15.getX() + ", " + var15.getY() + ", " + var15.getZ();

                              for (int var8 = 0; var8 < 4; var8++) {
                                 mc.inGameHud.getChatHud().addMessage(Text.literal(var16));
                              }
                           }
                        }
                     }
                  }
               }
            }

            this.t.retainAll(new HashSet(var3));
            this.a.clear();
            this.a.addAll(var3);
         }
      }
   }

   @Override
   public void onRender(MatrixStack matrices, float tickDelta) {
      if (mc.world != null && mc.player != null && !this.a.isEmpty()) {
         float var16 = RenderUtils.getCamera();
         if (var16 != null) {
            float var17 = RenderUtils.getCameraPos(var16);
            ArrayList var3 = new ArrayList<>(this.a);
            matrices.push();
            GL11.glDisable(2929);

            try {
               for (BlockPos var4 : var3) {
                  double var8 = var4.getX() + 0.5 - var17.x;
                  double var10 = var4.getY() + 0.5 - var17.y;
                  double var12 = var4.getZ() + 0.5 - var17.z;
                  this.a(matrices, var8, var10, var12);
               }
            } finally {
               GL11.glEnable(2929);
               matrices.pop();
            }
         }
      }
   }

   private void a(MatrixStack matrices, double dx, double dy, double dz) {
      Color var8 = this.co.getValue();
      Vec3d var9 = new Vec3d(dx, dy, dz);
      double var10 = new Vec3d(dx, dy + 500.0, dz);
      RenderUtils.renderLine(matrices, var8, var9, var10, 8.0F);
   }
}
