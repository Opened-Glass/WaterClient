package com.water.module.modules.render;

import com.water.module.Category;
import com.water.module.Module;
import com.water.module.ModuleManager;
import com.water.setting.BlocksSetting;
import com.water.setting.Setting;
import com.water.utils.RenderUtils;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.block.entity.BlastFurnaceBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.EnchantingTableBlockEntity;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.block.entity.FurnaceBlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.block.entity.SmokerBlockEntity;
import net.minecraft.block.entity.TrappedChestBlockEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;

public final class StorageESP extends Module {
   private final Setting<Boolean> cp = new Setting<>("New Style", true);
   private final Setting<Boolean> cq = new Setting<>("Chest", true);
   private final Setting<Boolean> cr = new Setting<>("Ender Chest", true);
   private final Setting<Boolean> cs = new Setting<>("Spawner", true);
   private final Setting<Boolean> ct = new Setting<>("Shulker Box", true);
   private final Setting<Boolean> cu = new Setting<>("Use Shulker Dyes", true);
   private final Setting<Boolean> cv = new Setting<>("Furnace", true);
   private final Setting<Boolean> cw = new Setting<>("Barrel", true);
   private final Setting<Boolean> cx = new Setting<>("Enchanting Table", true);
   private final Setting<Boolean> cy = new Setting<>("Moving Piston", true);
   private final Setting<Boolean> cz = new Setting<>("Hopper", true);
   private final Setting<Boolean> cA = new Setting<>("Tracers", true);
   private final Setting<Double> cB = new Setting<>("Tracer Weight", 0.3, 0.1, 2.0);
   private final Setting<Double> cC = new Setting<>("Fill Alpha", 15.0, 0.0, 255.0);
   private final Setting<Double> cD = new Setting<>("Opacity", 220.0, 0.0, 255.0);
   private final BlocksSetting c = new BlocksSetting("Blocks");
   private final Map<Block, Color> field_u_1 = new ConcurrentHashMap<>();
   private final Map<BlockPos, Block> v = new ConcurrentHashMap<>();
   private final Map<BlockPos, Color> w = new ConcurrentHashMap<>();
   private final ExecutorService e = Executors.newFixedThreadPool(Math.max(2, Math.min(4, Runtime.getRuntime().availableProcessors())), r -> {
      Runnable var1 = new Thread(r, "storageESP-scan");
      var1.setDaemon(true);
      var1.setPriority(5);
      return var1;
   });
   private final AtomicBoolean h = new AtomicBoolean(false);
   private int field_aw_1 = 0;
   private final Set<Long> field_u_2 = Collections.synchronizedSet(new HashSet<>());
   private int ax = Integer.MIN_VALUE;
   private int ay = Integer.MIN_VALUE;
   private RenderUtils.PersistentBatch a;
   private RenderUtils.PersistentBatch b;
   private boolean an;
   private boolean ao;
   private boolean ap;
   private boolean aq;
   private boolean ar;
   private boolean as;
   private boolean at;
   private boolean au;
   private boolean av;
   private boolean field_aw_2 = true;
   private int bi = 0;

   public StorageESP() {
      super("Storage ESP", Category.b);
      this.addSetting(this.cp);
      this.addSetting(this.cq);
      this.addSetting(this.cr);
      this.addSetting(this.cs);
      this.addSetting(this.ct);
      this.addSetting(this.cu);
      this.addSetting(this.cv);
      this.addSetting(this.cw);
      this.addSetting(this.cx);
      this.addSetting(this.cy);
      this.addSetting(this.cz);
      this.addSetting(this.cA);
      this.addSetting(this.cB);
      this.addSetting(this.cC);
      this.addSetting(this.cD);
      this.addSetting(this.c);
   }

   public Map<Block, Color> c() {
      LinkedHashMap var1 = new LinkedHashMap();
      if (this.cq.getValue()) {
         var1.put(Blocks.CHEST, this.field_u_1.getOrDefault(Blocks.CHEST, new Color(210, 140, 60)));
         var1.put(Blocks.TRAPPED_CHEST, this.field_u_1.getOrDefault(Blocks.TRAPPED_CHEST, new Color(220, 120, 40)));
      }

      if (this.cr.getValue()) {
         var1.put(Blocks.ENDER_CHEST, this.field_u_1.getOrDefault(Blocks.ENDER_CHEST, new Color(140, 80, 220)));
      }

      if (this.cs.getValue()) {
         var1.put(Blocks.SPAWNER, this.field_u_1.getOrDefault(Blocks.SPAWNER, new Color(160, 150, 180)));
      }

      if (this.ct.getValue()) {
         var1.put(Blocks.PURPLE_SHULKER_BOX, this.field_u_1.getOrDefault(Blocks.PURPLE_SHULKER_BOX, new Color(160, 60, 180)));
      }

      if (this.cv.getValue()) {
         var1.put(Blocks.FURNACE, this.field_u_1.getOrDefault(Blocks.FURNACE, new Color(150, 150, 150)));
      }

      if (this.cw.getValue()) {
         var1.put(Blocks.BARREL, this.field_u_1.getOrDefault(Blocks.BARREL, new Color(200, 130, 100)));
      }

      if (this.cx.getValue()) {
         var1.put(Blocks.ENCHANTING_TABLE, this.field_u_1.getOrDefault(Blocks.ENCHANTING_TABLE, new Color(100, 100, 240)));
      }

      if (this.cy.getValue()) {
         var1.put(Blocks.PISTON, this.field_u_1.getOrDefault(Blocks.PISTON, new Color(80, 200, 80)));
      }

      if (this.cz.getValue()) {
         var1.put(Blocks.HOPPER, this.field_u_1.getOrDefault(Blocks.HOPPER, new Color(120, 120, 120)));
      }

      return var1;
   }

   public void a(Block block, Color color) {
      if (block != null && color != null) {
         this.field_u_1.put(block, color);
         this.bb();
         ModuleManager.INSTANCE.c();
      }
   }

   public Map<Block, Color> d() {
      return new LinkedHashMap<>(this.field_u_1);
   }

   public void b(Map<Block, Color> colors) {
      this.field_u_1.clear();
      if (colors != null) {
         for (Entry var2 : colors.entrySet()) {
            if (var2.getKey() != null && var2.getValue() != null) {
               this.field_u_1.put((Block)var2.getKey(), (Color)var2.getValue());
            }
         }
      }

      this.bb();
      ModuleManager.INSTANCE.c();
   }

   private void bb() {
      this.w.clear();
      this.field_u_2.clear();
   }

   @Override
   public void onEnable() {
      this.v.clear();
      this.w.clear();
      this.field_u_2.clear();
      this.field_aw_1 = 0;
      this.a = RenderUtils.createPersistentBatch();
      this.b = RenderUtils.createPersistentBatch();
      this.an = this.cq.getValue();
      this.ao = this.cr.getValue();
      this.ap = this.cs.getValue();
      this.aq = this.ct.getValue();
      this.ar = this.cv.getValue();
      this.as = this.cw.getValue();
      this.at = this.cx.getValue();
      this.au = this.cy.getValue();
      this.av = this.cz.getValue();
      this.field_aw_2 = this.cp.getValue();
   }

   @Override
   public void onDisable() {
      this.v.clear();
      this.w.clear();
      this.field_u_2.clear();
      if (this.a != null) {
         this.a.close();
         this.a = null;
      }

      if (this.b != null) {
         this.b.close();
         this.b = null;
      }
   }

   private void bc() {
      boolean var1 = this.cq.getValue() != this.an
         || this.cr.getValue() != this.ao
         || this.cs.getValue() != this.ap
         || this.ct.getValue() != this.aq
         || this.cv.getValue() != this.ar
         || this.cw.getValue() != this.as
         || this.cx.getValue() != this.at
         || this.cy.getValue() != this.au
         || this.cz.getValue() != this.av
         || this.cp.getValue() != this.field_aw_2;
      if (var1) {
         this.v.clear();
         this.w.clear();
         this.field_u_2.clear();
         this.an = this.cq.getValue();
         this.ao = this.cr.getValue();
         this.ap = this.cs.getValue();
         this.aq = this.ct.getValue();
         this.ar = this.cv.getValue();
         this.as = this.cw.getValue();
         this.at = this.cx.getValue();
         this.au = this.cy.getValue();
         this.av = this.cz.getValue();
         this.field_aw_2 = this.cp.getValue();
      }
   }

   @Override
   public void onTick() {
      if (mc.world != null && mc.player != null) {
         this.bc();
         this.bd();
         if (++this.bi >= 5) {
            this.bi = 0;
            if (mc.player != null) {
               int var1 = mc.player.getChunkPos().x;
               int var2 = mc.player.getChunkPos().z;

               for (int var3 = -2; var3 <= 2; var3++) {
                  for (int var4 = -2; var4 <= 2; var4++) {
                     this.c(ChunkPos.toLong(var1 + var3, var2 + var4));
                  }
               }
            }
         }

         if (++this.field_aw_1 >= 1 && !this.h.get()) {
            this.field_aw_1 = 0;
            this.aw();
         }
      } else {
         this.v.clear();
         this.w.clear();
      }
   }

   @Override
   public void onPacketReceive(Packet<?> packet) {
      if (mc.world != null) {
         if (packet instanceof Packet var2) {
            this.c(ChunkPos.toLong(var2.getChunkX(), var2.getChunkZ()));
         } else if (packet instanceof Packet var3) {
            var3.visitUpdates((pos, state) -> this.c(new ChunkPos(pos).toLong()));
         } else if (packet instanceof Packet var4) {
            this.c(new ChunkPos(var4.getPos()).toLong());
         }
      }
   }

   private void aw() {
      this.h.set(true);
      if (mc.world != null && mc.player != null) {
         BlockPos var1 = mc.player.getBlockPos();
         int var2 = var1.getX() >> 4;
         int var13 = var1.getZ() >> 4;
         if (Math.abs(var2 - this.ax) > 1 || Math.abs(var13 - this.ay) > 1) {
            this.field_u_2.clear();
            this.ax = var2;
            this.ay = var13;
         }

         boolean var3 = this.c.size() > 0;
         ArrayList var4 = new ArrayList();

         try {
            for (int var5 = var2 - 20; var5 <= var2 + 20; var5++) {
               for (int var6 = var13 - 20; var6 <= var13 + 20; var6++) {
                  WorldChunk var7 = mc.world.getChunkManager().getWorldChunk(var5, var6, false);
                  if (var7 != null) {
                     long var10 = (long)var5 << 32 | var6 & 4294967295L;
                     if (!this.field_u_2.contains(var10)) {
                        var4.add(var7);
                     }
                  }
               }
            }
         } catch (Exception var12) {
            this.h.set(false);
            return;
         }

         if (var4.isEmpty()) {
            this.h.set(false);
         } else {
            this.e.execute(() -> {
               try {
                  HashMap var3x = new HashMap();
                  HashMap var4x = new HashMap();

                  for (WorldChunk var6x : var4) {
                     try {
                        for (Entry var8 : new HashMap<>(var6x.getBlockEntities()).entrySet()) {
                           BlockEntity var9 = (BlockEntity)var8.getValue();
                           if (var9 != null && this.b(var9)) {
                              var3x.put((BlockPos)var8.getKey(), var9.getCachedState().getBlock());
                              var4x.put((BlockPos)var8.getKey(), this.a(var9));
                           }
                        }
                     } catch (Exception var23) {
                     }

                     if (var3) {
                        try {
                           ChunkSection[] var29 = var6x.getSectionArray();
                           int var30 = var6x.getBottomY();
                           int var31 = var6x.getPos().x << 4;
                           int var27 = var6x.getPos().z << 4;

                           for (int var10x = 0; var10x < var29.length; var10x++) {
                              ChunkSection var11 = var29[var10x];
                              if (var11 != null && !var11.isEmpty()) {
                                 int var12x = var30 + var10x * 16;

                                 for (int var13x = 0; var13x < 16; var13x++) {
                                    for (int var14 = 0; var14 < 16; var14++) {
                                       for (int var15 = 0; var15 < 16; var15++) {
                                          Block var16 = var11.getBlockState(var13x, var15, var14).getBlock();
                                          if (this.c.contains(var16)) {
                                             BlockPos var17 = new BlockPos(var31 + var13x, var12x + var15, var27 + var14);
                                             if (!var3x.containsKey(var17)) {
                                                var3x.put(var17, var16);
                                                var4x.put(var17, this.field_u_1.getOrDefault(var16, new Color(0, 200, 255)));
                                             }
                                          }
                                       }
                                    }
                                 }
                              }
                           }
                        } catch (Exception var22) {
                        }
                     }
                  }

                  this.v.putAll(var3x);
                  this.w.putAll(var4x);

                  for (WorldChunk var28 : var4) {
                     this.field_u_2.add(var28.getPos().toLong());
                  }

                  this.bd();
               } catch (Exception var24) {
               } finally {
                  this.h.set(false);
               }
            });
         }
      } else {
         this.h.set(false);
      }
   }

   private void bd() {
      if (mc.player != null) {
         int var1 = mc.player.getChunkPos().x;
         int var2 = mc.player.getChunkPos().z;
         this.v.keySet().removeIf(pos -> Math.max(Math.abs((pos.getX() >> 4) - var1), Math.abs((pos.getZ() >> 4) - var2)) > 20);
         this.w.keySet().removeIf(pos -> Math.max(Math.abs((pos.getX() >> 4) - var1), Math.abs((pos.getZ() >> 4) - var2)) > 20);
         this.field_u_2.removeIf(chunkKey -> Math.max(Math.abs(ChunkPos.getPackedX(chunkKey) - var1), Math.abs(ChunkPos.getPackedZ(chunkKey) - var2)) > 20);
      }
   }

   private void c(long chunkKey) {
      this.field_u_2.remove(chunkKey);
   }

   private boolean b(BlockEntity be) {
      if (be instanceof ChestBlockEntity && this.cq.getValue()) {
         return true;
      } else if (be instanceof TrappedChestBlockEntity && this.cq.getValue()) {
         return true;
      } else if (be instanceof EnderChestBlockEntity && this.cr.getValue()) {
         return true;
      } else if (be instanceof MobSpawnerBlockEntity && this.cs.getValue()) {
         return true;
      } else if (be instanceof ShulkerBoxBlockEntity && this.ct.getValue()) {
         return true;
      } else if (be instanceof FurnaceBlockEntity && this.cv.getValue()) {
         return true;
      } else if (be instanceof BlastFurnaceBlockEntity && this.cv.getValue()) {
         return true;
      } else if (be instanceof SmokerBlockEntity && this.cv.getValue()) {
         return true;
      } else if (be instanceof BarrelBlockEntity && this.cw.getValue()) {
         return true;
      } else if (be instanceof EnchantingTableBlockEntity && this.cx.getValue()) {
         return true;
      } else {
         return be instanceof PistonBlockEntity && this.cy.getValue() ? true : be instanceof HopperBlockEntity && this.cz.getValue();
      }
   }

   private Color a(BlockEntity be) {
      Block var2 = be.getCachedState().getBlock();
      if (this.field_u_1.containsKey(var2)) {
         return this.field_u_1.get(var2);
      } else if (be instanceof ShulkerBoxBlockEntity && this.ct.getValue()) {
         if (this.cu.getValue()) {
            BlockEntity var3 = this.a(var2);
            if (var3 != null) {
               return var3;
            }
         }

         return new Color(160, 60, 180);
      } else if (be instanceof TrappedChestBlockEntity) {
         return new Color(220, 120, 40);
      } else if (be instanceof ChestBlockEntity) {
         return new Color(210, 140, 60);
      } else if (be instanceof EnderChestBlockEntity) {
         return new Color(140, 80, 220);
      } else if (be instanceof MobSpawnerBlockEntity) {
         return new Color(160, 150, 180);
      } else if (be instanceof FurnaceBlockEntity || be instanceof BlastFurnaceBlockEntity || be instanceof SmokerBlockEntity) {
         return new Color(150, 150, 150);
      } else if (be instanceof BarrelBlockEntity) {
         return new Color(200, 130, 100);
      } else if (be instanceof EnchantingTableBlockEntity) {
         return new Color(100, 100, 240);
      } else if (be instanceof PistonBlockEntity) {
         return new Color(80, 200, 80);
      } else {
         return be instanceof HopperBlockEntity ? new Color(120, 120, 120) : new Color(100, 200, 255);
      }
   }

   private Color a(Block block) {
      if (block == Blocks.WHITE_SHULKER_BOX) {
         return new Color(15789802);
      } else if (block == Blocks.ORANGE_SHULKER_BOX) {
         return new Color(15242794);
      } else if (block == Blocks.MAGENTA_SHULKER_BOX) {
         return new Color(12274357);
      } else if (block == Blocks.LIGHT_BLUE_SHULKER_BOX) {
         return new Color(3845832);
      } else if (block == Blocks.YELLOW_SHULKER_BOX) {
         return new Color(15253016);
      } else if (block == Blocks.LIME_SHULKER_BOX) {
         return new Color(6991400);
      } else if (block == Blocks.PINK_SHULKER_BOX) {
         return new Color(14708912);
      } else if (block == Blocks.GRAY_SHULKER_BOX) {
         return new Color(4738128);
      } else if (block == Blocks.LIGHT_GRAY_SHULKER_BOX) {
         return new Color(10000544);
      } else if (block == Blocks.CYAN_SHULKER_BOX) {
         return new Color(2263176);
      } else if (block == Blocks.PURPLE_SHULKER_BOX) {
         return new Color(7878832);
      } else if (block == Blocks.BLUE_SHULKER_BOX) {
         return new Color(2767016);
      } else if (block == Blocks.BROWN_SHULKER_BOX) {
         return new Color(7883824);
      } else if (block == Blocks.GREEN_SHULKER_BOX) {
         return new Color(5005344);
      } else if (block == Blocks.RED_SHULKER_BOX) {
         return new Color(9969696);
      } else {
         return block == Blocks.BLACK_SHULKER_BOX ? new Color(1710626) : null;
      }
   }

   @Override
   public void onRender(MatrixStack matrices, float tickDelta) {
      if (mc.world != null && mc.player != null && !this.v.isEmpty()) {
         if (this.a == null) {
            this.a = RenderUtils.createPersistentBatch();
         }

         if (this.b == null) {
            this.b = RenderUtils.createPersistentBatch();
         }

         Camera var3 = RenderUtils.getCamera();
         if (var3 != null) {
            Vec3d var4 = RenderUtils.getCameraPos(var3);
            Vec3d var39 = RenderUtils.getCameraForward(var3);
            float var37 = Freecam.resolveTracerOrigin(var4, tickDelta);
            float var38 = var37.equals(var4) ? var39.multiply(0.1) : var37.subtract(var4);
            double var8 = mc.player.getX();
            mc.player.getY();
            double var12 = mc.player.getZ();
            if (this.cp.getValue()) {
               int var40 = this.k((int)Math.round(this.cC.getValue()));
               if (this.cA.getValue()) {
                  float var5 = this.cB.getValue().floatValue();
                  this.b.begin(matrices);

                  for (Entry var7 : this.v.entrySet()) {
                     BlockPos var10 = (BlockPos)var7.getKey();
                     Color var23 = this.w.get(var10);
                     if (var23 != null) {
                        double var24 = var10.getX() - var8;
                        double var26 = var10.getZ() - var12;
                        if (!(var24 * var24 + var26 * var26 > 73984.0)) {
                           double var28 = var10.getX() - var4.x;
                           double var30 = var10.getY() - var4.y;
                           double var32 = var10.getZ() - var4.z;
                           if (Double.isFinite(var28) && Double.isFinite(var30) && Double.isFinite(var32)) {
                              Vec3d var34 = new Vec3d(var28 + 0.5, var30 + 0.5, var32 + 0.5);
                              this.b.addLine(var38, var34, a(var23, 70), var5 + 2.4F);
                              this.b.addLine(var38, var34, a(var23, 145), var5 + 1.4F);
                              this.b.addLine(var38, var34, a(var23, 255), var5);
                           }
                        }
                     }
                  }

                  this.b.flush();
               }

               this.a.begin(matrices);

               for (Entry var44 : this.v.entrySet()) {
                  BlockPos var46 = (BlockPos)var44.getKey();
                  Color var48 = this.w.get(var46);
                  if (var48 != null) {
                     double var50 = var46.getX() - var8;
                     double var25 = var46.getZ() - var12;
                     if (!(var50 * var50 + var25 * var25 > 73984.0)) {
                        double var27 = var46.getX() - var4.x;
                        double var29 = var46.getY() - var4.y;
                        double var31 = var46.getZ() - var4.z;
                        if (Double.isFinite(var27) && Double.isFinite(var29) && Double.isFinite(var31) && var40 > 0) {
                           this.a.addFilledBox(var27 + 0.1, var29 + 0.1, var31 + 0.1, var27 + 0.9, var29 + 0.9, var31 + 0.9, a(var48, var40));
                        }
                     }
                  }
               }

               this.a.flushFill();
            } else {
               int var41 = this.k((int)Math.round(this.cD.getValue()));
               int var43 = this.k((int)Math.round(this.cC.getValue()));
               RenderUtils.WorldBatch var45 = RenderUtils.beginWorldBatch(matrices);
               RenderUtils.WorldBatch var47 = var43 > 0 ? RenderUtils.beginWorldBatch(matrices) : null;

               for (Entry var51 : this.v.entrySet()) {
                  BlockPos var52 = (BlockPos)var51.getKey();
                  Color var53 = this.w.get(var52);
                  if (var53 != null) {
                     double var54 = var52.getX() - var8;
                     double var55 = var52.getZ() - var12;
                     if (!(var54 * var54 + var55 * var55 > 73984.0)) {
                        double var56 = var52.getX() - var4.x;
                        double var57 = var52.getY() - var4.y;
                        double var58 = var52.getZ() - var4.z;
                        if (Double.isFinite(var56) && Double.isFinite(var57) && Double.isFinite(var58)) {
                           var45.renderOutlineBox(var56 + 0.0625, var57, var58 + 0.0625, var56 + 0.9375, var57 + 1.0, var58 + 0.9375, a(var53, var41));
                           if (var47 != null) {
                              var47.renderFilledBox(var56 + 0.0625, var57, var58 + 0.0625, var56 + 0.9375, var57 + 1.0, var58 + 0.9375, a(var53, var43));
                           }

                           if (this.cA.getValue()) {
                              MatrixStack var36 = new Vec3d(var56 + 0.5, var57 + 0.5, var58 + 0.5);
                              var45.renderLine(a(var53, 70), var38, var36, this.cB.getValue().floatValue() + 2.4F);
                              var45.renderLine(a(var53, 145), var38, var36, this.cB.getValue().floatValue() + 1.4F);
                              var45.renderLine(a(var53, 255), var38, var36, this.cB.getValue().floatValue());
                           }
                        }
                     }
                  }
               }

               if (var47 != null) {
                  var47.flush();
               }

               var45.flush();
            }
         }
      }
   }

   private static Color a(Color c, int a) {
      return new Color(c.getRed(), c.getGreen(), c.getBlue(), a);
   }

   private int k(int v) {
      return Math.max(0, Math.min(255, v));
   }
}
