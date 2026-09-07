package com.water.module.modules.render;

import com.water.module.Category;
import com.water.module.Module;
import com.water.setting.Setting;
import com.water.utils.RenderUtils;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
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

public final class FutureDebug extends Module {
   private final Setting<Boolean> bE = new Setting<>("Smart Check", true);
   private final Setting<Integer> bF = new Setting<>("Sensitivity", 5, 1, 10);
   private final Setting<Integer> bG = new Setting<>("Scan Radius", 1, 1, 5);
   private final Setting<Color> bH = new Setting<>("Fill Color", new Color(180, 60, 60, 255));
   private final Setting<Integer> bI = new Setting<>("Fill Alpha", 30, 0, 255);
   private final Set<Long> o = ConcurrentHashMap.newKeySet();
   private final Set<ChunkPos> field_p_1 = ConcurrentHashMap.newKeySet();
   private ExecutorService field_b_1;
   private final AtomicBoolean f = new AtomicBoolean(false);
   private int field_p_2 = 0;
   private static final int[][] field_b_2 = new int[][]{{1, 3}, {1, 4}, {1, 5}, {2, 3}, {3, 3}, {4, 3}, {5, 3}, {4, 4}, {5, 5}};
   private final Map<Long, int[]> field_q_1 = new ConcurrentHashMap<>();
   private final Set<Long> field_q_2 = ConcurrentHashMap.newKeySet();

   public FutureDebug() {
      super("FutureDebug", Category.b);
      this.addSetting(this.bE);
      this.addSetting(this.bF);
      this.addSetting(this.bG);
      this.addSetting(this.bH);
      this.addSetting(this.bI);
   }

   @Override
   public void onEnable() {
      this.o.clear();
      this.field_p_1.clear();
      this.field_q_1.clear();
      this.field_q_2.clear();
      this.field_p_2 = 0;
      this.f.set(false);
   }

   @Override
   public void onDisable() {
      this.o.clear();
      this.field_p_1.clear();
      this.field_q_1.clear();
      this.field_q_2.clear();
      if (this.field_b_1 != null) {
         this.field_b_1.shutdownNow();
      }
   }

   private int q() {
      return (int)(40.0 - (this.bF.getValue() - 1) * 3.5555555555555554);
   }

   private float d() {
      return 0.015F - (this.bF.getValue() - 1) * 0.0013333333F;
   }

   private int r() {
      return (int)(15.0 - (this.bF.getValue() - 1) * 1.3333333333333333);
   }

   private float e() {
      return 0.08F + (this.bF.getValue() - 1) * 0.027F;
   }

   private float f() {
      return 6.0F + (this.bF.getValue() - 1) * 0.44444445F;
   }

   private boolean b(WorldChunk chunk) {
      ChunkSection[] var2 = chunk.getSectionArray();
      WorldChunk var22 = chunk.getBottomY();
      int var3 = 0;
      int var4 = 0;
      int var5 = 0;
      int var6 = 0;
      int var7 = 0;
      int var8 = 0;
      int var9 = 0;
      int var10 = 0;
      ArrayList var11 = new ArrayList();
      byte[][][] var12 = new byte[16][128][16];

      for (int var13 = 0; var13 < var2.length; var13++) {
         int var14 = var22 + var13 * 16;
         if (var14 <= 48 && var14 + 16 >= -64) {
            ChunkSection var15 = var2[var13];
            if (var15 != null && !var15.isEmpty()) {
               for (int var16 = 0; var16 < 16; var16++) {
                  for (int var17 = 0; var17 < 16; var17++) {
                     int var18 = var14 + var17;
                     if (var18 >= -64 && var18 <= 48) {
                        int var19 = var18 + 64;
                        if (var19 >= 0 && var19 < 128) {
                           for (int var20 = 0; var20 < 16; var20++) {
                              BlockState var21 = var15.getBlockState(var16, var17, var20);
                              if (var21.isOf(Blocks.BUDDING_AMETHYST)) {
                                 return false;
                              }

                              if (this.j(var21)) {
                                 var12[var16][var19][var20] = 1;
                                 var3++;
                                 var8 += var16;
                                 var9 += var18;
                                 var10 += var20;
                                 var11.add(new int[]{var16, var18, var20});
                              } else if (var21.isOf(Blocks.CALCITE)) {
                                 var12[var16][var19][var20] = 2;
                                 var4++;
                              } else if (var21.isOf(Blocks.SMOOTH_BASALT)) {
                                 var12[var16][var19][var20] = 3;
                                 var5++;
                              } else if (!var21.isAir() && !var21.isOf(Blocks.CAVE_AIR) && !var21.isOf(Blocks.VOID_AIR)) {
                                 var12[var16][var19][var20] = 5;
                              } else {
                                 var12[var16][var19][var20] = 4;
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      if (var3 == 0) {
         return false;
      } else if (!this.bE.getValue()) {
         return var3 >= this.q();
      } else {
         int var35 = Integer.MAX_VALUE;
         int var38 = Integer.MIN_VALUE;

         for (int[] var42 : var11) {
            if (var42[1] < var35) {
               var35 = var42[1];
            }

            if (var42[1] > var38) {
               var38 = var42[1];
            }
         }

         float var41 = var3 / (256.0F * Math.max(1, var38 - var35 + 1));
         float var43 = (float)var8 / var3;
         float var45 = (float)var9 / var3;
         float var46 = (float)var10 / var3;
         float var47 = 0.0F;

         for (int[] var51 : var11) {
            WorldChunk var23 = var51[0] - var43;
            float var25 = var51[1] - var45;
            float var28 = var51[2] - var46;
            var47 += (float)Math.sqrt(var23 * var23 + var25 * var25 + var28 * var28);
         }

         var47 /= var3;
         int[] var50 = new int[]{1, -1, 0, 0, 0, 0};
         int[] var52 = new int[]{0, 0, 1, -1, 0, 0};
         WorldChunk var24 = new int[]{0, 0, 0, 0, 1, -1};

         for (int[] var29 : var11) {
            var9 = var29[0];
            var10 = var29[1] + 64;
            var8 = var29[2];

            for (int var34 = 0; var34 < 6; var34++) {
               var35 = var9 + var50[var34];
               var38 = var10 + var52[var34];
               int var44 = var8 + var24[var34];
               if (var35 >= 0 && var35 <= 15 && var38 >= 0 && var38 < 128 && var44 >= 0 && var44 <= 15) {
                  byte var37 = var12[var35][var38][var44];
                  if (var37 == 4) {
                     var6++;
                  } else if (var37 >= 2) {
                     var7++;
                  }
               }
            }
         }

         int var27 = var6 + var7;
         float var31 = var27 > 0 ? (float)var6 / var27 : 0.0F;
         return var3 >= this.q() && var41 >= this.d() && var4 + var5 >= this.r() && var47 <= this.f() && var31 <= this.e();
      }
   }

   private boolean j(BlockState bs) {
      return bs.isOf(Blocks.AMETHYST_CLUSTER)
         || bs.isOf(Blocks.LARGE_AMETHYST_BUD)
         || bs.isOf(Blocks.MEDIUM_AMETHYST_BUD)
         || bs.isOf(Blocks.SMALL_AMETHYST_BUD)
         || bs.isOf(Blocks.AMETHYST_BLOCK);
   }

   private boolean a(WorldChunk chunk) {
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
   public void onTick() {
      if (mc.world != null && mc.player != null) {
         ChunkPos var1 = mc.player.getChunkPos();
         int var2 = this.bG.getValue() * 5;
         this.o.removeIf(key -> {
            int var3x = ChunkPos.getPackedX(key);
            Long var4x = ChunkPos.getPackedZ(key);
            return Math.abs(var3x - var1.x) > var2 + 3 || Math.abs(var4x - var1.z) > var2 + 3;
         });
         this.field_p_1.removeIf(cp -> Math.abs(cp.x - var1.x) > var2 + 2 || Math.abs(cp.z - var1.z) > var2 + 2);
         if (++this.field_p_2 % 5 == 0) {
            if (this.f.compareAndSet(false, true)) {
               if (this.field_b_1 == null || this.field_b_1.isShutdown()) {
                  this.field_b_1 = Executors.newSingleThreadExecutor(t -> {
                     Runnable var1x = new Thread(t, "futuredebug-scan");
                     var1x.setDaemon(true);
                     return var1x;
                  });
               }

               ArrayList var3 = new ArrayList();
               ArrayList var4 = new ArrayList();

               for (int var5 = -var2; var5 <= var2; var5++) {
                  for (int var6 = -var2; var6 <= var2; var6++) {
                     ChunkPos var7 = new ChunkPos(var1.x + var5, var1.z + var6);
                     WorldChunk var8 = mc.world.getChunkManager().getWorldChunk(var7.x, var7.z, false);
                     if (var8 != null && !var8.isEmpty()) {
                        var3.add(var7);
                        var4.add(var8);
                     }
                  }
               }

               this.field_b_1.submit(() -> {
                  try {
                     for (int var3x = 0; var3x < var3.size(); var3x++) {
                        ChunkPos var4x = (ChunkPos)var3.get(var3x);
                        WorldChunk var5x = (WorldChunk)var4.get(var3x);
                        long var6x = var4x.toLong();
                        if (this.b(var5x)) {
                           this.o.add(var6x);
                        }

                        if (this.a(var5x)) {
                           this.field_p_1.add(var4x);
                        }
                     }
                  } catch (Exception var10) {
                  } finally {
                     this.f.set(false);
                  }
               });
            }
         }
      }
   }

   private int[] a(long key) {
      if (this.field_q_1.containsKey(key)) {
         return this.field_q_1.get(key);
      } else {
         int var3 = ChunkPos.getPackedX(key);
         int var4 = ChunkPos.getPackedZ(key);
         ArrayList var5 = new ArrayList<>(Arrays.asList(field_b_2));
         Collections.shuffle(var5);

         for (int[] var6 : var5) {
            int var7 = var6[0];
            int var16 = var6[1];
            boolean var8 = true;

            label52:
            for (int var9 = 0; var9 < var7; var9++) {
               for (int var10 = 0; var10 < var16; var10++) {
                  long var13 = ChunkPos.toLong(var3 + var9, var4 + var10);
                  if (this.field_q_2.contains(var13)) {
                     var8 = false;
                     break label52;
                  }
               }
            }

            if (var8) {
               for (int var17 = 0; var17 < var7; var17++) {
                  for (int var18 = 0; var18 < var16; var18++) {
                     this.field_q_2.add(ChunkPos.toLong(var3 + var17, var4 + var18));
                  }
               }

               this.field_q_1.put(key, new int[]{var7, var16});
               return new int[]{var7, var16};
            }
         }

         this.field_q_2.add(key);
         this.field_q_1.put(key, new int[]{1, 1});
         return new int[]{1, 1};
      }
   }

   @Override
   public void onRender(MatrixStack matrices, float tickDelta) {
      if (mc.world != null && mc.player != null) {
         if (!this.o.isEmpty() || !this.field_p_1.isEmpty()) {
            float var29 = RenderUtils.getCamera();
            if (var29 != null) {
               float var30 = RenderUtils.getCameraPos(var29);
               double var5 = 63.0 - var30.y;
               double var7 = var5 + 0.1;
               Color var3 = this.bH.getValue();
               var3 = new Color(var3.getRed(), var3.getGreen(), var3.getBlue(), this.bI.getValue());
               MatrixStack var28 = RenderUtils.beginWorldBatch(matrices);

               for (long var13 : this.o) {
                  int var9 = ChunkPos.getPackedX(var13);
                  int var10 = ChunkPos.getPackedZ(var13);
                  int[] var11 = this.a(var13);
                  int var12 = var11[0];
                  int var32 = var11[1];
                  double var20 = (var9 << 4) - var30.x;
                  double var22 = (var10 << 4) - var30.z;
                  double var24 = var20 + var12 * 16.0;
                  double var26 = var22 + var32 * 16.0;
                  var28.renderFilledBox(var20, var5, var22, var24, var7, var26, var3);
               }

               var28.flush();
            }
         }
      }
   }
}
