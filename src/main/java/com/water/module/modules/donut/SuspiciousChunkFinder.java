package com.water.module.modules.donut;

import com.water.module.Category;
import com.water.module.Module;
import com.water.setting.Setting;
import com.water.utils.RenderUtils;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;
import org.lwjgl.opengl.GL11;

public final class SuspiciousChunkFinder extends Module {
   private final Setting<Integer> B = new Setting<>("Scan Radius", 1, 1, 5);
   private final Setting<Integer> C = new Setting<>("Sim Chunks", 10, 1, 10);
   private final Setting<Color> D = new Setting<>("Fill Color", new Color(180, 60, 60, 40));
   private final Setting<Integer> E = new Setting<>("Fill Alpha", 40, 0, 255);
   private final Set<ChunkPos> c = ConcurrentHashMap.newKeySet();
   private final Set<ChunkPos> d = ConcurrentHashMap.newKeySet();
   private volatile Set<ChunkPos> e = Collections.emptySet();
   private volatile long m = 0L;
   private ExecutorService field_b_1;
   private final AtomicBoolean field_b_2 = new AtomicBoolean(false);
   private int p = 0;

   public SuspiciousChunkFinder() {
      super("SUS CHUNK FINDER", Category.d);
      this.addSetting(this.B);
      this.addSetting(this.C);
      this.addSetting(this.D);
      this.addSetting(this.E);
   }

   @Override
   public void onEnable() {
      this.ac();
   }

   @Override
   public void onDisable() {
      this.ac();
      if (this.field_b_1 != null) {
         this.field_b_1.shutdownNow();
      }
   }

   private void ac() {
      this.c.clear();
      this.d.clear();
      this.e = Collections.emptySet();
      this.p = 0;
      this.field_b_2.set(false);
      this.m = 0L;
   }

   @Override
   public void onTick() {
      if (mc.world != null && mc.player != null) {
         ChunkPos var1 = mc.player.getChunkPos();
         int var2 = this.B.getValue() * 5;
         this.c.removeIf(cp -> Math.abs(cp.x - var1.x) > var2 + 2 || Math.abs(cp.z - var1.z) > var2 + 2);
         this.d.removeIf(cp -> Math.abs(cp.x - var1.x) > var2 + 2 || Math.abs(cp.z - var1.z) > var2 + 2);
         if (++this.p % 5 == 0) {
            if (this.field_b_2.compareAndSet(false, true)) {
               if (this.field_b_1 == null || this.field_b_1.isShutdown()) {
                  this.field_b_1 = Executors.newSingleThreadExecutor(t -> {
                     Runnable var1x = new Thread(t, "collosionsensor-scan");
                     var1x.setDaemon(true);
                     return var1x;
                  });
               }

               ArrayList var3 = new ArrayList();
               ArrayList var4 = new ArrayList();
               int var5 = this.C.getValue();

               for (int var6 = -var2; var6 <= var2; var6++) {
                  for (int var7 = -var2; var7 <= var2; var7++) {
                     ChunkPos var8 = new ChunkPos(var1.x + var6, var1.z + var7);
                     WorldChunk var9 = mc.world.getChunkManager().getWorldChunk(var8.x, var8.z, false);
                     if (var9 != null && !var9.isEmpty()) {
                        var3.add(var8);
                        var4.add(var9);
                     }
                  }
               }

               this.field_b_1.submit(() -> {
                  try {
                     HashMap var5x = new HashMap();
                     HashSet var6x = new HashSet();

                     for (int var7x = 0; var7x < var3.size(); var7x++) {
                        ChunkPos var8x = (ChunkPos)var3.get(var7x);
                        WorldChunk var9x = (WorldChunk)var4.get(var7x);
                        int var10 = this.method_a_1(var9x);
                        if (var10 >= var5) {
                           var5x.put(var8x, var10);
                        }

                        if (this.method_a_2(var9x)) {
                           var6x.add(var8x);
                        }
                     }

                     Set var15 = this.a(var5x, var1, var5);
                     Set var16 = this.a(var15, var5);
                     boolean var17 = mc.player != null && mc.player.getY() <= -2.0;
                     if (var17) {
                        var16.removeIf(cp -> !var6x.contains(cp));
                     }

                     if (!var15.equals(this.c) || !var6x.equals(this.d) || !var16.equals(this.e)) {
                        this.e = Collections.unmodifiableSet(var16);
                     }

                     this.c.clear();
                     this.c.addAll(var15);
                     this.d.clear();
                     this.d.addAll(var6x);
                  } catch (Exception var13) {
                  } finally {
                     this.field_b_2.set(false);
                  }
               });
            }
         }
      }
   }

   private Set<ChunkPos> a(Map<ChunkPos, Integer> hitCounts, ChunkPos center, int threshold) {
      Map var5 = new ArrayList(hitCounts.entrySet());
      var5.sort((a, b) -> {
         int var4x = Integer.compare((Integer)b.getValue(), (Integer)a.getValue());
         if (var4x != 0) {
            return var4x;
         } else {
            Entry var6 = this.a(center, (ChunkPos)a.getKey());
            ChunkPos var5x = this.a(center, (ChunkPos)b.getKey());
            return Integer.compare(var6, var5x);
         }
      });
      ChunkPos var7 = Math.max(3, 33 - threshold * 3);
      int var8 = new LinkedHashSet();

      for (Entry var4 : var5) {
         if (var8.size() >= var7) {
            break;
         }

         var8.add((ChunkPos)var4.getKey());
      }

      return var8;
   }

   private int a(ChunkPos a, ChunkPos b) {
      int var3 = a.x - b.x;
      ChunkPos var4 = a.z - b.z;
      return var3 * var3 + var4 * var4;
   }

   private Set<ChunkPos> a(Set<ChunkPos> source, int threshold) {
      HashSet var3 = new HashSet();
      if (threshold >= 10) {
         var3.addAll(source);
         return var3;
      } else {
         int var12 = new int[][]{{1, 4}, {2, 4}, {1, 2}, {3, 1}, {5, 4}, {5, 6}};

         for (ChunkPos var4 : source) {
            Random var5 = new Random(Math.abs(var4.hashCode()));
            int[] var6 = var12[var5.nextInt(var12.length)];
            int var7 = var6[0];
            int var15 = var6[1];
            if (var5.nextBoolean()) {
               int var13 = var7;
               var7 = var15;
               var15 = var13;
            }

            int var14 = -(var7 / 2);
            int var8 = -(var15 / 2);

            for (int var9 = 0; var9 < var7; var9++) {
               for (int var10 = 0; var10 < var15; var10++) {
                  var3.add(new ChunkPos(var4.x + var14 + var9, var4.z + var8 + var10));
               }
            }
         }

         return var3;
      }
   }

   private int method_a_1(WorldChunk chunk) {
      int var2 = 0;
      ChunkSection[] var3 = chunk.getSectionArray();
      WorldChunk var9 = chunk.getBottomY();

      for (int var4 = 0; var4 < var3.length; var4++) {
         int var5 = var9 + var4 * 16;
         if (var5 > 32) {
            break;
         }

         ChunkSection var10 = var3[var4];
         if (var10 != null && !var10.isEmpty() && var10.hasAny(this::a)) {
            for (int var6 = 0; var6 < 16; var6++) {
               for (int var7 = 0; var7 < 16; var7++) {
                  for (int var8 = 0; var8 < 16; var8++) {
                     if (this.a(var10.getBlockState(var6, var7, var8))) {
                        var2++;
                     }
                  }
               }
            }
         }
      }

      return var2;
   }

   private boolean a(BlockState state) {
      return state.isOf(Blocks.AMETHYST_CLUSTER) || state.isOf(Blocks.AMETHYST_BLOCK);
   }

   private boolean method_a_2(WorldChunk chunk) {
      int var2 = 0;
      ChunkSection[] var3 = chunk.getSectionArray();
      WorldChunk var11 = chunk.getBottomY();

      for (int var4 = 0; var4 < var3.length; var4++) {
         int var5 = var11 + var4 * 16;
         if (var5 >= 0) {
            break;
         }

         ChunkSection var6 = var3[var4];
         if (var6 != null && !var6.isEmpty() && var6.hasAny(bs -> bs.isOf(Blocks.CHEST) || bs.isOf(Blocks.TRAPPED_CHEST))) {
            var5 = Math.min(15, -var5 - 1);

            for (int var7 = 0; var7 < 16; var7++) {
               for (int var8 = 0; var8 < 16; var8++) {
                  for (int var9 = 0; var9 <= var5; var9++) {
                     BlockState var10 = var6.getBlockState(var7, var9, var8);
                     if (var10.isOf(Blocks.CHEST) || var10.isOf(Blocks.TRAPPED_CHEST)) {
                        if (++var2 >= 10) {
                           return true;
                        }
                     }
                  }
               }
            }
         }
      }

      return false;
   }

   @Override
   public void onRender(MatrixStack matrices, float tickDelta) {
      if (mc.world != null && mc.player != null) {
         if (!this.e.isEmpty() || !this.d.isEmpty()) {
            float var26 = RenderUtils.getCamera();
            if (var26 != null) {
               float var27 = RenderUtils.getCameraPos(var26);
               double var5 = 47.0 - var27.y;
               double var7 = var5 + 0.1;
               Color var3 = this.D.getValue();
               int var4 = this.E.getValue();
               var3 = new Color(var3.getRed(), var3.getGreen(), var3.getBlue(), var4);
               boolean var29 = false;
               int var9 = 0;
               if (!this.d.isEmpty()) {
                  long var14 = System.currentTimeMillis();
                  if (this.m == 0L) {
                     this.m = var14;
                  }

                  long var16 = (var14 - this.m) % 400L;
                  if (var16 < 150L) {
                     var29 = true;
                     float var18 = (float)var16 / 150.0F;
                     var9 = (int)((1.0F - var18) * 200.0F);
                  }
               } else {
                  this.m = 0L;
               }

               matrices.push();
               GL11.glDisable(2929);
               RenderUtils.WorldBatch var30 = RenderUtils.beginWorldBatch(matrices);

               for (ChunkPos var32 : this.e) {
                  double var17 = (var32.x << 4) - var27.x;
                  double var19 = (var32.z << 4) - var27.z;
                  double var21 = var17 + 16.0;
                  double var23 = var19 + 16.0;
                  var30.renderFilledBox(var17, var5, var19, var21, var7, var23, var3);
               }

               if (var29 && var9 > 0) {
                  Color var31 = new Color(255, 255, 255, var9);

                  for (ChunkPos var34 : this.d) {
                     double var35 = (var34.x << 4) - var27.x;
                     double var20 = (var34.z << 4) - var27.z;
                     double var22 = var35 + 16.0;
                     double var24 = var20 + 16.0;
                     var30.renderFilledBox(var35, var5, var20, var22, var7 + 0.3, var24, var31);
                  }
               }

               var30.flush();
               GL11.glEnable(2929);
               matrices.pop();
            }
         }
      }
   }
}
