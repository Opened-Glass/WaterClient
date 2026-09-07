package com.water.module.modules.render;

import com.water.module.Category;
import com.water.module.Module;
import com.water.setting.Setting;
import com.water.utils.RenderUtils;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.LightType;
import net.minecraft.world.chunk.WorldChunk;
import org.lwjgl.opengl.GL11;

public final class LightDebug extends Module {
   private final Setting<Integer> bU = new Setting<>("Min Light", 0, 0, 15);
   private final Setting<Integer> bV = new Setting<>("Max Light", 7, 0, 15);
   private final Setting<Integer> bW = new Setting<>("Min Y", -51, -64, 20);
   private final Setting<Integer> bX = new Setting<>("Max Y", 20, -64, 20);
   private final Setting<Color> bY = new Setting<>("Color Low", new Color(50, 200, 50, 60));
   private final Setting<Color> bZ = new Setting<>("Color High", new Color(255, 50, 50, 60));
   private final Map<ChunkPos, List<LightDebug.a>> s = new ConcurrentHashMap<>();
   private ChunkPos d = null;
   private int bc = 0;
   private ExecutorService b;
   private final AtomicBoolean g = new AtomicBoolean(false);

   public LightDebug() {
      super("LightDebug", Category.b);
      this.addSetting(this.bU);
      this.addSetting(this.bV);
      this.addSetting(this.bW);
      this.addSetting(this.bX);
      this.addSetting(this.bY);
      this.addSetting(this.bZ);
   }

   @Override
   public void onEnable() {
      this.s.clear();
      this.d = null;
      this.bc = 0;
      this.g.set(false);
   }

   @Override
   public void onDisable() {
      this.s.clear();
      if (this.b != null) {
         this.b.shutdownNow();
      }
   }

   @Override
   public void onTick() {
      if (mc.world != null && mc.player != null) {
         ChunkPos var1 = mc.player.getChunkPos();
         int var2 = !var1.equals(this.d);
         this.d = var1;
         if (++this.bc % 10 == 0 || var2) {
            if (this.g.compareAndSet(false, true)) {
               if (this.b == null || this.b.isShutdown()) {
                  this.b = Executors.newSingleThreadExecutor(t -> {
                     Runnable var1x = new Thread(t, "lightdebug-scan");
                     var1x.setDaemon(true);
                     return var1x;
                  });
               }

               var2 = this.bU.getValue();
               int var3 = this.bV.getValue();
               int var4 = Math.min(this.bW.getValue(), this.bX.getValue());
               int var5 = Math.max(this.bW.getValue(), this.bX.getValue());
               ArrayList var6 = new ArrayList();
               ArrayList var7 = new ArrayList();

               for (int var8 = -3; var8 <= 3; var8++) {
                  for (int var9 = -3; var9 <= 3; var9++) {
                     ChunkPos var10 = new ChunkPos(var1.x + var8, var1.z + var9);
                     WorldChunk var11 = mc.world.getChunkManager().getWorldChunk(var10.x, var10.z, false);
                     if (var11 != null && !var11.isEmpty()) {
                        var6.add(var10);
                        var7.add(var11);
                     }
                  }
               }

               this.s.keySet().removeIf(cp -> Math.abs(cp.x - var1.x) > r + 1 || Math.abs(cp.z - var1.z) > r + 1);
               this.b.submit(() -> {
                  try {
                     for (int var7x = 0; var7x < var6.size(); var7x++) {
                        ChunkPos var8x = (ChunkPos)var6.get(var7x);
                        List var9x = this.a((WorldChunk)var7.get(var7x), var8x, var2, var3, var4, var5);
                        if (var9x.isEmpty()) {
                           this.s.remove(var8x);
                        } else {
                           this.s.put(var8x, var9x);
                        }
                     }
                  } catch (Exception var12x) {
                  } finally {
                     this.g.set(false);
                  }
               });
            }
         }
      }
   }

   private List<LightDebug.a> a(WorldChunk chunk, ChunkPos cp, int minL, int maxL, int yMin, int yMax) {
      WorldChunk var12 = new ArrayList();
      int var7 = cp.x << 4;
      ChunkPos var13 = cp.z << 4;

      for (int var14 = yMin; var14 <= yMax; var14++) {
         for (int var8 = 0; var8 < 16; var8++) {
            for (int var9 = 0; var9 < 16; var9++) {
               BlockPos var10 = new BlockPos(var7 + var8, var14, var13 + var9);
               int var11 = mc.world.getLightLevel(LightType.BLOCK, var10);
               if (var11 >= minL && var11 <= maxL) {
                  var12.add(new LightDebug.a(var10, var11));
               }
            }
         }
      }

      return var12;
   }

   @Override
   public void onRender(MatrixStack matrices, float tickDelta) {
      if (mc.world != null && mc.player != null) {
         if (!this.s.isEmpty()) {
            float var22 = RenderUtils.getCamera();
            if (var22 != null) {
               float var23 = RenderUtils.getCameraPos(var22);
               int var3 = this.bV.getValue();
               int var4 = this.bU.getValue();
               matrices.push();
               GL11.glDisable(2929);

               try {
                  RenderUtils.WorldBatch var5 = RenderUtils.beginWorldBatch(matrices);
                  int var6 = 0;

                  label65:
                  for (List var8 : this.s.values()) {
                     for (LightDebug.a var9 : var8) {
                        if (var6++ >= 50000) {
                           break label65;
                        }

                        Color var10 = this.a(var9.bd, var4, var3);
                        double var14 = var9.d.getX() - var23.x;
                        double var16 = var9.d.getY() - var23.y;
                        double var18 = var9.d.getZ() - var23.z;
                        var5.renderFilledBox(var14, var16, var18, var14 + 1.0, var16 + 1.0, var18 + 1.0, var10);
                     }
                  }

                  var5.flush();
               } finally {
                  GL11.glEnable(2929);
                  matrices.pop();
               }
            }
         }
      }
   }

   private Color a(int level, int minL, int maxL) {
      int var10 = Math.max(1, maxL - minL);
      int var7 = Math.max(0.0F, Math.min(1.0F, (level - minL) / var10));
      int var9 = this.bY.getValue();
      int var11 = this.bZ.getValue();
      int var4 = (int)(var9.getRed() + (var11.getRed() - var9.getRed()) * var7);
      int var5 = (int)(var9.getGreen() + (var11.getGreen() - var9.getGreen()) * var7);
      int var6 = (int)(var9.getBlue() + (var11.getBlue() - var9.getBlue()) * var7);
      level = (int)(var9.getAlpha() + (var11.getAlpha() - var9.getAlpha()) * var7);
      return new Color(var4, var5, var6, level);
   }

   private static final class a {
      final BlockPos d;
      final int bd;

      a(BlockPos pos, int level) {
         this.d = pos;
         this.bd = level;
      }
   }
}
