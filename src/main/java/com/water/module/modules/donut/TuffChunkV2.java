package com.water.module.modules.donut;

import com.water.gui.ToastManager;
import com.water.module.Category;
import com.water.module.Module;
import com.water.module.modules.client.WaterPlus;
import com.water.utils.RenderUtils;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RepeaterBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;

public final class TuffChunkV2 extends Module {
   private final ConcurrentHashMap<ChunkPos, String> field_a_1 = new ConcurrentHashMap<>();
   private final AtomicBoolean field_c_1 = new AtomicBoolean(false);
   private final AtomicBoolean d = new AtomicBoolean(false);
   private ExecutorService field_c_2;
   private long o = 0L;
   private long p = 0L;
   private long q = 0L;
   private long r = 0L;
   private boolean z = false;
   private volatile TuffChunkV2.a field_a_2;

   public TuffChunkV2() {
      super("Tuff Chunk V2", Category.b);
   }

   @Override
   public void onEnable() {
      this.field_a_1.clear();
      this.o = 0L;
      this.p = 0L;
      this.z = false;
      this.field_a_2 = null;
   }

   @Override
   public void onDisable() {
      this.field_a_1.clear();
      if (this.field_c_2 != null) {
         this.field_c_2.shutdownNow();
      }

      this.field_c_1.set(false);
      this.d.set(false);
      this.z = false;
      this.field_a_2 = null;
   }

   @Override
   public void onTick() {
      if (mc.world != null && mc.player != null) {
         if (this.z && (float)(System.currentTimeMillis() - this.r) > 800.0F) {
            this.z = false;
         }

         long var1 = System.currentTimeMillis();
         if (var1 - this.q >= 200L && !this.field_c_1.get() && this.d.compareAndSet(false, true)) {
            this.q = var1;

            try {
               ChunkPos var3 = mc.player.getChunkPos();
               int var4 = Math.min(mc.options.getClampedViewDistance(), 8);
               ArrayList var5 = new ArrayList();
               ArrayList var6 = new ArrayList();

               for (int var7 = -var4; var7 <= var4; var7++) {
                  for (int var8 = -var4; var8 <= var4; var8++) {
                     ChunkPos var9 = new ChunkPos(var3.x + var7, var3.z + var8);
                     WorldChunk var10 = mc.world.getChunkManager().getWorldChunk(var9.x, var9.z, false);
                     if (var10 != null && !var10.isEmpty()) {
                        var5.add(var9);
                        var6.add(var10);
                     }
                  }
               }

               if (this.field_c_2 == null || this.field_c_2.isShutdown()) {
                  this.field_c_2 = Executors.newSingleThreadExecutor(t -> {
                     Runnable var1x = new Thread(t, "tuff-scan");
                     var1x.setDaemon(true);
                     return var1x;
                  });
               }

               this.field_c_2.submit(() -> {
                  try {
                     for (int var3x = 0; var3x < var5.size(); var3x++) {
                        WorldChunk var4x = (WorldChunk)var6.get(var3x);
                        int var5x = 0;

                        for (ChunkSection var8x : var4x.getSectionArray()) {
                           if (var8x != null && !var8x.isEmpty() && var8x.hasAny(bs -> bs.isOf(Blocks.REPEATER))) {
                              for (int var9x = 0; var9x < 16; var9x++) {
                                 for (int var10x = 0; var10x < 16; var10x++) {
                                    for (int var11x = 0; var11x < 16; var11x++) {
                                       BlockState var12x = var8x.getBlockState(var9x, var11x, var10x);
                                       if (var12x.isOf(Blocks.REPEATER) && var12x.get(RepeaterBlock.POWERED)) {
                                          if (++var5x >= 3) {
                                             this.field_a_1.clear();
                                             this.field_a_1.put((ChunkPos)var5.get(var3x), "repeater");
                                             this.field_a_2 = this.a((ChunkPos)var5.get(var3x), var5, var6);
                                             return;
                                          }
                                       }
                                    }
                                 }
                              }
                           }
                        }
                     }
                  } finally {
                     this.d.set(false);
                  }
               });
            } catch (RejectedExecutionException var13) {
               this.d.set(false);
            } catch (Throwable var14) {
               this.d.set(false);
            }
         }

         if (var1 - this.p >= 20000L) {
            if (this.field_c_1.compareAndSet(false, true)) {
               if (this.field_c_2 == null || this.field_c_2.isShutdown()) {
                  this.field_c_2 = Executors.newSingleThreadExecutor(t -> {
                     Runnable var1x = new Thread(t, "tuff-chunk-scan");
                     var1x.setDaemon(true);
                     return var1x;
                  });
               }

               this.p = var1;
               this.z = true;
               this.r = var1;
               ChunkPos var15 = mc.player.getChunkPos();
               int var16 = Math.min(mc.options.getClampedViewDistance(), 8);
               ArrayList var17 = new ArrayList();
               ArrayList var18 = new ArrayList();

               for (int var19 = -var16; var19 <= var16; var19++) {
                  for (int var20 = -var16; var20 <= var16; var20++) {
                     ChunkPos var21 = new ChunkPos(var15.x + var19, var15.z + var20);
                     WorldChunk var22 = mc.world.getChunkManager().getWorldChunk(var21.x, var21.z, false);
                     if (var22 != null && !var22.isEmpty()) {
                        var17.add(var21);
                        var18.add(var22);
                     }
                  }
               }

               try {
                  this.field_c_2
                     .submit(
                        () -> {
                           try {
                              ChunkPos var3x = null;

                              label673:
                              for (int var4x = 0; var4x < var17.size(); var4x++) {
                                 WorldChunk var5x = (WorldChunk)var18.get(var4x);
                                 int var6x = 0;

                                 for (ChunkSection var10x : var5x.getSectionArray()) {
                                    if (var10x != null && !var10x.isEmpty() && var10x.hasAny(bs -> bs.isOf(Blocks.REPEATER))) {
                                       for (int var11x = 0; var11x < 16; var11x++) {
                                          for (int var12x = 0; var12x < 16; var12x++) {
                                             for (int var13x = 0; var13x < 16; var13x++) {
                                                BlockState var14x = var10x.getBlockState(var11x, var13x, var12x);
                                                if (var14x.isOf(Blocks.REPEATER) && var14x.get(RepeaterBlock.POWERED)) {
                                                   if (++var6x >= 3) {
                                                      var3x = (ChunkPos)var17.get(var4x);
                                                      break label673;
                                                   }
                                                }
                                             }
                                          }
                                       }
                                    }
                                 }
                              }

                              if (var3x != null) {
                                 this.o = System.currentTimeMillis();
                                 this.field_a_1.clear();
                                 this.field_a_1.put(var3x, "repeater");
                                 this.field_a_2 = this.a(var3x, var17, var18);
                                 return;
                              }

                              ChunkPos var29 = null;
                              String var30 = "normal";

                              for (int var31 = 0; var31 < var17.size(); var31++) {
                                 ChunkPos var33 = (ChunkPos)var17.get(var31);
                                 WorldChunk var35 = (WorldChunk)var18.get(var31);
                                 boolean var36 = false;

                                 for (BlockEntity var40 : var35.getBlockEntities().values()) {
                                    BlockState var43 = var35.getBlockState(var40.getPos());
                                    if ((var43.isOf(Blocks.BEEHIVE) || var43.isOf(Blocks.BEE_NEST))
                                       && var40 instanceof BeehiveBlockEntity var47
                                       && var47.getBeeCount() > 0) {
                                       var36 = true;
                                       break;
                                    }
                                 }

                                 boolean var39 = false;
                                 if (!var36) {
                                    int var41 = 0;
                                    ChunkSection[] var44 = var35.getSectionArray();
                                    int var48 = var35.getBottomY();

                                    label618:
                                    for (int var51 = 0; var51 < var44.length; var51++) {
                                       int var25 = var48 + var51 * 16;
                                       if (var25 > 20) {
                                          break;
                                       }

                                       if (var25 + 16 >= 0) {
                                          ChunkSection var15x = var44[var51];
                                          if (var15x != null && !var15x.isEmpty() && var15x.hasAny(bs -> bs.isOf(Blocks.COBBLED_DEEPSLATE))) {
                                             for (int var16x = 0; var16x < 16; var16x++) {
                                                for (int var17x = 0; var17x < 16; var17x++) {
                                                   for (int var18x = 0; var18x < 16; var18x++) {
                                                      int var19x = var25 + var18x;
                                                      if (var19x >= 0
                                                         && var19x <= 20
                                                         && var15x.getBlockState(var16x, var18x, var17x).isOf(Blocks.COBBLED_DEEPSLATE)) {
                                                         if (++var41 >= 50) {
                                                            var39 = true;
                                                            break label618;
                                                         }
                                                      }
                                                   }
                                                }
                                             }
                                          }
                                       }
                                    }
                                 }

                                 boolean var42 = false;
                                 if (!var36) {
                                    int var45 = 0;

                                    for (ChunkSection var55 : var35.getSectionArray()) {
                                       if (var55 != null && !var55.isEmpty() && var55.hasAny(bs -> bs.isOf(Blocks.VINE))) {
                                          for (int var58 = 0; var58 < 16; var58++) {
                                             for (int var61 = 0; var61 < 16; var61++) {
                                                for (int var64 = 0; var64 < 16; var64++) {
                                                   if (var55.getBlockState(var58, var64, var61).isOf(Blocks.VINE)) {
                                                      if (++var45 >= 150) {
                                                         var42 = true;
                                                         break;
                                                      }
                                                   }
                                                }
                                             }
                                          }
                                       }
                                    }
                                 }

                                 boolean var46 = false;
                                 boolean var50 = false;
                                 if (!var36 && !var42) {
                                    int var53 = 0;
                                    int var27 = 0;
                                    ChunkSection[] var56 = var35.getSectionArray();
                                    int var59 = var35.getBottomY();

                                    label543:
                                    for (int var62 = 0; var62 < var56.length && var59 + var62 * 16 <= 70; var62++) {
                                       ChunkSection var65 = var56[var62];
                                       if (var65 != null
                                          && !var65.isEmpty()
                                          && var65.hasAny(bs -> bs.isOf(Blocks.SEAGRASS) || bs.isOf(Blocks.TALL_SEAGRASS) || bs.isIn(BlockTags.FLOWERS))) {
                                          for (int var67 = 0; var67 < 16; var67++) {
                                             for (int var20x = 0; var20x < 16; var20x++) {
                                                for (int var21x = 0; var21x < 16; var21x++) {
                                                   BlockState var22x = var65.getBlockState(var67, var21x, var20x);
                                                   if (var22x.isOf(Blocks.SEAGRASS) || var22x.isOf(Blocks.TALL_SEAGRASS)) {
                                                      if (++var53 >= 70) {
                                                         var46 = true;
                                                      }
                                                   } else if (var22x.isIn(BlockTags.FLOWERS)) {
                                                      if (++var27 >= 6) {
                                                         var50 = true;
                                                      }
                                                   }

                                                   if (var46 || var50) {
                                                      break label543;
                                                   }
                                                }
                                             }
                                          }
                                       }
                                    }
                                 }

                                 boolean var54 = false;
                                 if (!var36 && !var42 && !var46 && !var39) {
                                    int var28 = 0;

                                    label497:
                                    for (ChunkSection var66 : var35.getSectionArray()) {
                                       if (var66 != null && !var66.isEmpty() && var66.hasAny(bs -> bs.isOf(Blocks.REPEATER))) {
                                          for (int var68 = 0; var68 < 16; var68++) {
                                             for (int var69 = 0; var69 < 16; var69++) {
                                                for (int var70 = 0; var70 < 16; var70++) {
                                                   if (var66.getBlockState(var68, var70, var69).isOf(Blocks.REPEATER)) {
                                                      if (++var28 >= 3) {
                                                         var54 = true;
                                                         break label497;
                                                      }
                                                   }
                                                }
                                             }
                                          }
                                       }
                                    }
                                 }

                                 if (var36 || var42 || var46 || var50 || var39 || var54) {
                                    var29 = var33;
                                    var30 = var54 ? "repeater" : "normal";
                                    break;
                                 }
                              }

                              if (var29 != null && "repeater".equals(var30)) {
                                 this.o = System.currentTimeMillis();
                                 this.field_a_1.clear();
                                 this.field_a_1.put(var29, var30);
                                 this.field_a_2 = this.a(var29, var17, var18);
                                 MinecraftClient.getInstance()
                                    .execute(
                                       () -> {
                                          MinecraftClient var0 = MinecraftClient.getInstance();
                                          if (var0.player != null) {
                                             var0.player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
                                             var0.player.sendMessage(Text.literal("§8§7Repeater chunk found!"), false);
                                          }

                                          ToastManager.INSTANCE
                                             .push("Tuff Chunk V2", "Repeater chunk found!", Items.REPEATER.getDefaultStack(), WaterPlus.getAccentARGB());
                                       }
                                    );
                              } else if (var29 != null) {
                                 boolean var32 = System.currentTimeMillis() - this.o >= 50000L;
                                 if (var32) {
                                    this.o = System.currentTimeMillis();
                                    this.field_a_1.clear();
                                    this.field_a_1.put(var29, var30);
                                    TuffChunkV2.a var34 = this.a(var29, var17, var18);
                                    this.field_a_2 = var34;
                                    int var37 = var34 != null ? var34.t : 40;
                                    MinecraftClient.getInstance()
                                       .execute(
                                          () -> {
                                             MinecraftClient var2 = MinecraftClient.getInstance();
                                             if (var2.player != null) {
                                                var2.player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
                                                var2.player
                                                   .sendMessage(
                                                      Text.literal(
                                                         "§8§7Chunk found! §aBase chance: §f"
                                                            + var37
                                                            + "%"
                                                            + (var34 != null ? " §7near chunk §f" + var34.b.x + ", " + var34.b.z : "")
                                                      ),
                                                      false
                                                   );
                                             }

                                             ToastManager.INSTANCE
                                                .push(
                                                   "Tuff Chunk V2",
                                                   var34 != null
                                                      ? "Base chance: " + var37 + "% near " + var34.b.x + ", " + var34.b.z
                                                      : "Base chance: " + var37 + "%",
                                                   Items.LIME_CONCRETE.getDefaultStack(),
                                                   WaterPlus.getAccentARGB()
                                                );
                                          }
                                       );
                                 }
                              }
                           } finally {
                              this.field_c_1.set(false);
                           }
                        }
                     );
               } catch (RejectedExecutionException var11) {
                  this.field_c_1.set(false);
               } catch (Throwable var12) {
                  this.field_c_1.set(false);
               }
            }
         }
      }
   }

   private TuffChunkV2.a a(ChunkPos source, List<ChunkPos> positions, List<WorldChunk> chunks) {
      if (source != null && positions != null && chunks != null && !positions.isEmpty()) {
         ChunkPos var4 = source;
         int var5 = -1;
         int var6 = 0;
         TuffChunkV2.b var7 = null;
         int var8 = Math.min(positions.size(), chunks.size());

         for (int var9 = 0; var9 < var8; var9++) {
            ChunkPos var10 = (ChunkPos)positions.get(var9);
            if (Math.abs(var10.x - source.x) <= 7 && Math.abs(var10.z - source.z) <= 7) {
               TuffChunkV2.b var11 = this.a((WorldChunk)chunks.get(var9));
               int var12 = var11.score();
               int var13 = Math.abs(var10.x - source.x) + Math.abs(var10.z - source.z);
               var13 = Math.max(0, var12 - var13 * 3);
               if (var13 > var5) {
                  var5 = var13;
                  var6 = var12;
                  var4 = var10;
                  var7 = var11;
               }
            }
         }

         int var14 = var7 == null ? 35 : Math.max(20, Math.min(99, 25 + var6 / 2));
         return new TuffChunkV2.a(source, var4, var14, var6);
      } else {
         return null;
      }
   }

   private TuffChunkV2.b a(WorldChunk chunk) {
      TuffChunkV2.b var2 = new TuffChunkV2.b();
      if (chunk != null && !chunk.isEmpty()) {
         for (BlockEntity var4 : chunk.getBlockEntities().values()) {
            BlockState var5 = chunk.getBlockState(var4.getPos());
            if (var5.isOf(Blocks.BEEHIVE) || var5.isOf(Blocks.BEE_NEST)) {
               var2.u++;
               if (var4 instanceof BeehiveBlockEntity var6) {
                  var2.v = var2.v + Math.max(0, var6.getBeeCount());
               }
            } else if (a(var5.getBlock())) {
               var2.ac++;
            }
         }

         ChunkSection[] var13 = chunk.getSectionArray();
         int var14 = chunk.getBottomY();

         for (int var15 = 0; var15 < var13.length; var15++) {
            ChunkSection var16 = var13[var15];
            if (var16 != null && !var16.isEmpty()) {
               WorldChunk var12 = var14 + var15 * 16;
               boolean var7 = var16.hasAny(
                  bs -> bs.isOf(Blocks.SEAGRASS)
                     || bs.isOf(Blocks.TALL_SEAGRASS)
                     || bs.isIn(BlockTags.FLOWERS)
                     || bs.isOf(Blocks.VINE)
                     || bs.isOf(Blocks.COBBLED_DEEPSLATE)
                     || bs.isOf(Blocks.REPEATER)
                     || a(bs.getBlock())
               );
               if (var7) {
                  for (int var17 = 0; var17 < 16; var17++) {
                     for (int var8 = 0; var8 < 16; var8++) {
                        for (int var9 = 0; var9 < 16; var9++) {
                           int var10 = var12 + var9;
                           BlockState var11 = var16.getBlockState(var17, var9, var8);
                           if ((var11.isOf(Blocks.SEAGRASS) || var11.isOf(Blocks.TALL_SEAGRASS)) && var10 <= 70) {
                              var2.y++;
                           } else if (var11.isIn(BlockTags.FLOWERS) && var10 <= 70) {
                              var2.z++;
                           } else if (var11.isOf(Blocks.VINE)) {
                              var2.x++;
                           } else if (var11.isOf(Blocks.COBBLED_DEEPSLATE) && var10 >= 0 && var10 <= 20) {
                              var2.w++;
                           } else if (var11.isOf(Blocks.REPEATER)) {
                              var2.aa++;
                              if (var11.get(RepeaterBlock.POWERED)) {
                                 var2.ab++;
                              }
                           } else if (a(var11.getBlock())) {
                              var2.ac++;
                           }
                        }
                     }
                  }
               }
            }
         }

         return var2;
      } else {
         return var2;
      }
   }

   private static boolean a(Block block) {
      return block == Blocks.CHEST
         || block == Blocks.TRAPPED_CHEST
         || block == Blocks.BARREL
         || block == Blocks.ENDER_CHEST
         || block == Blocks.FURNACE
         || block == Blocks.BLAST_FURNACE
         || block == Blocks.SMOKER
         || block == Blocks.CRAFTING_TABLE
         || block == Blocks.ENCHANTING_TABLE
         || block == Blocks.ANVIL
         || block == Blocks.CHIPPED_ANVIL
         || block == Blocks.DAMAGED_ANVIL;
   }

   @Override
   public void onRender(MatrixStack matrices, float tickDelta) {
      if (mc.world != null && mc.player != null) {
         float var20 = RenderUtils.getCamera();
         if (var20 != null) {
            float var21 = RenderUtils.getCameraPos(var20);
            double var5 = 63.0 - var21.y;
            double var7 = var5 + 0.01;
            matrices.push();

            try {
               RenderUtils.WorldBatch var3 = RenderUtils.beginWorldBatch(matrices);
               if (this.z) {
                  float var4 = Math.min(1.0F, (float)(System.currentTimeMillis() - this.r) / 800.0F);
                  float var9 = 1.0F - (1.0F - var4) * (1.0F - var4);
                  float var10 = var9 * 140.0F;
                  int var24 = (int)(120.0F * (1.0F - var4));
                  if (var24 > 0 && var10 > 0.0F) {
                     double var14 = mc.player.getX() - var21.x;
                     double var16 = mc.player.getZ() - var21.z;
                     Color var22 = new Color(255, 255, 255, var24);
                     var3.renderFilledBox(var14 - var10, var5, var16 - var10, var14 + var10, var7, var16 - var10 + 5.0, var22);
                     var3.renderFilledBox(var14 - var10, var5, var16 + var10 - 5.0, var14 + var10, var7, var16 + var10, var22);
                     var3.renderFilledBox(var14 - var10, var5, var16 - var10, var14 - var10 + 5.0, var7, var16 + var10, var22);
                     var3.renderFilledBox(var14 + var10 - 5.0, var5, var16 - var10, var14 + var10, var7, var16 + var10, var22);
                  }
               }

               for (Entry var25 : this.field_a_1.entrySet()) {
                  ChunkPos var27 = (ChunkPos)var25.getKey();
                  String var26 = (String)var25.getValue();
                  double var28 = (var27.x << 4) + 8.0 - var21.x;
                  double var29 = (var27.z << 4) + 8.0 - var21.z;
                  if ("repeater".equals(var26)) {
                     var3.renderFilledBox(var28 - 32.0, var5, var29 - 32.0, var28 + 32.0, var7, var29 + 32.0, a(40));
                     var3.renderOutlineBox(var28 - 32.0, var5, var29 - 32.0, var28 + 32.0, var7, var29 + 32.0, a(255));
                     var3.renderFilledBox(var28 - 8.0, var5, var29 - 8.0, var28 + 8.0, var7, var29 + 8.0, a(100));
                     var3.renderOutlineBox(var28 - 8.0, var5, var29 - 8.0, var28 + 8.0, var7, var29 + 8.0, a(255));
                  } else {
                     var3.renderFilledBox(var28 - 32.0, var5, var29 - 32.0, var28 + 32.0, var7, var29 + 32.0, a(120));
                     var3.renderOutlineBox(var28 - 32.0, var5, var29 - 32.0, var28 + 32.0, var7, var29 + 32.0, a(255));
                  }
               }

               var3.flush();
            } finally {
               matrices.pop();
            }
         }
      }
   }

   private static Color a(int alpha) {
      return new Color(0, 255, 0, Math.max(0, Math.min(255, alpha)));
   }

   private static final class a {
      private ChunkPos a;
      final ChunkPos b;
      final int t;
      private int score;

      a(ChunkPos source, ChunkPos chunk, int chance, int score) {
         this.a = source;
         this.b = chunk;
         this.t = chance;
         this.score = score;
      }
   }

   private static final class b {
      int u;
      int v;
      int w;
      int x;
      int y;
      int z;
      int aa;
      int ab;
      int ac;

      int score() {
         int var1 = 0 + Math.min(90, this.u * 22 + this.v * 10);
         var1 += Math.min(65, this.w / 2);
         var1 += Math.min(55, this.x / 4);
         var1 += Math.min(70, this.y);
         var1 += Math.min(75, this.z * 12);
         var1 += Math.min(90, this.aa * 16 + this.ab * 28);
         return var1 + Math.min(100, this.ac * 18);
      }
   }
}
