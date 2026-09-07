package com.water.module.modules.render;

import com.water.gui.ToastManager;
import com.water.module.Category;
import com.water.module.Module;
import com.water.setting.BlocksSetting;
import com.water.setting.Setting;
import com.water.utils.RenderUtils;
import java.awt.Color;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;

public final class BlockESP extends Module {
   private final BlocksSetting field_a_1 = new BlocksSetting("Blocks", Blocks.SPAWNER);
   private final Setting<Boolean> bs = new Setting<>("Notification", true);
   private final Setting<Boolean> bt = new Setting<>("Tracers", true);
   private final Setting<Double> bu = new Setting<>("Tracer Weight", 1.0, 0.1, 5.0);
   private final Setting<Boolean> bv = new Setting<>("Filled", true);
   private final Setting<Double> bw = new Setting<>("Opacity", 220.0, 0.0, 255.0);
   private final Setting<Double> bx = new Setting<>("Fill Alpha", 100.0, 0.0, 255.0);
   private final Setting<Integer> by = new Setting<>("Max Render", 500, 10, 2000);
   private final Map<Long, Set<BlockPos>> j = new ConcurrentHashMap<>();
   private final Map<BlockPos, Block> field_k_1 = new ConcurrentHashMap<>();
   private final Map<Long, Long> field_l_1 = new ConcurrentHashMap<>();
   private final ArrayDeque<Long> field_a_2 = new ArrayDeque<>();
   private final Set<Long> field_k_2 = new HashSet<>();
   private final Object field_a_3 = new Object();
   private final ConcurrentHashMap<Block, Color> b = new ConcurrentHashMap<>();
   private volatile Set<Block> field_l_2 = Collections.emptySet();
   private long x = -1L;
   private int field_k_3 = 0;
   private boolean ai = true;
   private ChunkPos c;
   private int av = -1;
   private final List<BlockESP.a> d = new ArrayList<>();

   public BlockESP() {
      super("Block ESP", Category.b);
      this.addSetting(this.field_a_1);
      this.addSetting(this.bs);
      this.addSetting(this.bt);
      this.addSetting(this.bu);
      this.addSetting(this.bv);
      this.addSetting(this.bw);
      this.addSetting(this.bx);
      this.addSetting(this.by);
   }

   @Override
   public void onEnable() {
      this.av();
      this.x = -1L;
      this.ai = true;
      this.field_k_3 = 0;
      this.c = null;
      this.av = -1;
   }

   @Override
   public void onDisable() {
      this.av();
      this.c = null;
      this.av = -1;
   }

   @Override
   public void onTick() {
      if (mc.world != null && mc.player != null) {
         this.au();
         if (this.field_l_2.isEmpty()) {
            this.av();
         } else {
            this.field_k_3++;
            ChunkPos var1 = mc.player.getChunkPos();
            int var2 = this.p();
            int var3 = this.ai || this.field_k_3 % 200 == 0;
            if (var3 || this.c == null || !this.c.equals(var1) || this.av != var2) {
               this.b((boolean)var3);
               this.ai = false;
               this.c = var1;
               this.av = var2;
            }

            for (int var5 = 0; var5 < 6; var5++) {
               Long var6;
               synchronized (this.field_a_3) {
                  var6 = this.field_a_2.poll();
                  if (var6 != null) {
                     this.field_k_2.remove(var6);
                  }
               }

               if (var6 == null) {
                  break;
               }

               var3 = ChunkPos.getPackedX(var6);
               var2 = ChunkPos.getPackedZ(var6);
               WorldChunk var8 = mc.world.getChunkManager().getWorldChunk(var3, var2, false);
               if (var8 != null) {
                  this.a(var8);
               }
            }
         }
      }
   }

   @Override
   public void onPacketReceive(Packet<?> packet) {
      if (mc.world != null) {
         if (packet instanceof Packet var2) {
            this.a(ChunkPos.toLong(var2.getChunkX(), var2.getChunkZ()), true);
         } else if (packet instanceof Packet var3) {
            var3.visitUpdates((pos, state) -> this.a(new ChunkPos(pos).toLong(), true));
         } else if (packet instanceof Packet var4) {
            this.a(new ChunkPos(var4.getPos()).toLong(), true);
         }
      }
   }

   @Override
   public void onRender(MatrixStack matrices, float tickDelta) {
      if (mc.world != null && mc.player != null && !this.j.isEmpty()) {
         Set var3 = this.field_l_2;
         if (!var3.isEmpty()) {
            Camera var4 = RenderUtils.getCamera();
            if (var4 != null) {
               Vec3d var5 = RenderUtils.getCameraPos(var4);
               Vec3d var29 = RenderUtils.getCameraForward(var4);
               float var26 = Freecam.resolveTracerOrigin(var5, tickDelta);
               float var27 = var26.equals(var5) ? var29.multiply(0.1) : var26.subtract(var5);
               int var30 = i((int)Math.round(this.bw.getValue()));
               int var6 = i((int)Math.round(this.bx.getValue()));
               int var7 = this.by.getValue();
               double var12 = this.method_b_2();
               double var14 = mc.player.getX();
               double var16 = mc.player.getY();
               double var18 = mc.player.getZ();
               this.d.clear();

               for (Set var9 : this.j.values()) {
                  for (BlockPos var11 : var9) {
                     double var24 = var11.getSquaredDistance(var14, var16, var18);
                     if (!(var24 > var12)) {
                        Block var20 = this.field_k_1.get(var11);
                        if (var20 != null && var3.contains(var20)) {
                           this.d.add(new BlockESP.a(var11.getX() - var5.x, var11.getY() - var5.y, var11.getZ() - var5.z, this.a(var20, 255), var24));
                        }
                     }
                  }
               }

               if (!this.d.isEmpty()) {
                  this.d.sort(Comparator.comparingDouble(e -> e.field_d_2));
                  int var32 = Math.min(var7, this.d.size());
                  RenderUtils.WorldBatch var33 = RenderUtils.beginWorldBatch(matrices);

                  for (int var34 = var32 - 1; var34 >= 0; var34--) {
                     BlockESP.a var36 = this.d.get(var34);
                     Color var39 = a(var36.field_d_1, var30);
                     Color var25 = a(var36.field_d_1, 255);
                     Color var38 = a(var36.field_d_1, 70);
                     Color var28 = a(var36.field_d_1, 145);
                     var33.renderOutlineBox(
                        var36.x + 0.0625, var36.y + 0.0625, var36.z + 0.0625, var36.x + 1.0 - 0.0625, var36.y + 1.0 - 0.0625, var36.z + 1.0 - 0.0625, var39
                     );
                     if (this.bt.getValue()) {
                        var5 = new Vec3d(var36.x + 0.5, var36.y + 0.5, var36.z + 0.5);
                        var33.renderLine(var38, var27, var5, this.bu.getValue().floatValue() + 2.4F);
                        var33.renderLine(var28, var27, var5, this.bu.getValue().floatValue() + 1.4F);
                        var33.renderLine(var25, var27, var5, this.bu.getValue().floatValue());
                     }
                  }

                  var33.flush();
                  if (this.bv.getValue() && var6 > 0) {
                     RenderUtils.WorldBatch var35 = RenderUtils.beginWorldBatch(matrices);

                     for (int var37 = var32 - 1; var37 >= 0; var37--) {
                        BlockESP.a var40 = this.d.get(var37);
                        var35.renderFilledBox(
                           var40.x + 0.0625,
                           var40.y + 0.0625,
                           var40.z + 0.0625,
                           var40.x + 1.0 - 0.0625,
                           var40.y + 1.0 - 0.0625,
                           var40.z + 1.0 - 0.0625,
                           a(var40.field_d_1, var6)
                        );
                     }

                     var35.flush();
                  }
               }
            }
         }
      }
   }

   private static int i(int v) {
      return Math.max(0, Math.min(255, v));
   }

   private void au() {
      long var1 = this.field_a_1.getVersion();
      if (var1 != this.x) {
         this.x = var1;
         this.field_l_2 = Set.copyOf(this.field_a_1.getSelectedBlocks());
         this.av();
         this.ai = true;
      }
   }

   public void a(Map<Block, Color> colors) {
      this.b.clear();
      this.b.putAll(colors);
   }

   public Map<Block, Color> method_b_1() {
      return new LinkedHashMap<>(this.b);
   }

   private void b(boolean forceRescan) {
      if (mc.world != null && mc.player != null) {
         int var2 = this.p();
         ChunkPos var3 = mc.player.getChunkPos();
         ArrayList var4 = new ArrayList();
         HashSet var5 = new HashSet();

         for (int var6 = -var2; var6 <= var2; var6++) {
            for (int var7 = -var2; var7 <= var2; var7++) {
               WorldChunk var8 = mc.world.getChunkManager().getWorldChunk(var3.x + var6, var3.z + var7, false);
               if (var8 != null) {
                  var4.add(var8);
                  var5.add(var8.getPos().toLong());
               }
            }
         }

         var4.sort(Comparator.comparingInt(chunk -> this.b(var3, chunk.getPos())));
         synchronized (this.field_a_3) {
            this.field_a_2.removeIf(k -> !var5.contains(k));
            this.field_k_2.retainAll(var5);

            for (WorldChunk var14 : var4) {
               long var9 = var14.getPos().toLong();
               if ((forceRescan || !this.j.containsKey(var9)) && this.field_k_2.add(var9)) {
                  this.field_a_2.addLast(var9);
               }
            }
         }

         this.a(var3, var2);
      }
   }

   private void a(long chunkKey, boolean prioritized) {
      synchronized (this.field_a_3) {
         if (prioritized && this.field_k_2.contains(chunkKey)) {
            this.field_a_2.remove(chunkKey);
            this.field_a_2.addFirst(chunkKey);
         } else if (this.field_k_2.add(chunkKey)) {
            if (prioritized) {
               this.field_a_2.addFirst(chunkKey);
            } else {
               this.field_a_2.add(chunkKey);
            }
         }
      }
   }

   private void a(WorldChunk chunk) {
      Set var2 = this.field_l_2;
      if (!var2.isEmpty()) {
         int var3 = mc.world.getBottomY();
         int var4 = mc.world.getBottomY() + mc.world.getHeight();
         int var5 = mc.world.getBottomSectionCoord();
         ChunkPos var6 = chunk.getPos();
         long var7 = var6.toLong();
         Set var9 = this.j.get(var7);
         HashSet var10 = new HashSet();
         Block var11 = null;
         BlockPos var12 = null;
         WorldChunk var21 = chunk.getSectionArray();

         for (int var13 = 0; var13 < var21.length; var13++) {
            ChunkSection var14 = var21[var13];
            if (var14 != null && !var14.isEmpty()) {
               int var15 = (var5 + var13) * 16;
               if (var15 + 16 > var3 && var15 < var4 && var14.getBlockStateContainer().hasAny(state -> var2.contains(state.getBlock()))) {
                  for (int var16 = 0; var16 < 16; var16++) {
                     for (int var17 = 0; var17 < 16; var17++) {
                        for (int var18 = 0; var18 < 16; var18++) {
                           Block var19 = var14.getBlockState(var16, var18, var17).getBlock();
                           if (var2.contains(var19)) {
                              BlockPos var20 = new BlockPos(var6.getStartX() + var16, var15 + var18, var6.getStartZ() + var17);
                              var10.add(var20);
                              this.field_k_1.put(var20, var19);
                              if (var11 == null && (var9 == null || !var9.contains(var20))) {
                                 var11 = var19;
                                 var12 = var20;
                              }
                           }
                        }
                     }
                  }
               }
            }
         }

         if (var9 != null) {
            for (BlockPos var23 : var9) {
               if (!var10.contains(var23)) {
                  this.field_k_1.remove(var23);
               }
            }
         }

         if (var10.isEmpty()) {
            this.b(var7);
            this.field_l_1.remove(var7);
         } else {
            this.j.put(var7, var10);
            if (var11 != null) {
               this.a(var7, var11, var12, var6);
            }
         }
      }
   }

   private void a(long chunkKey, Block block, BlockPos pos, ChunkPos chunkPos) {
      if (this.bs.getValue() && mc.player != null) {
         long var6 = System.currentTimeMillis();
         long var8 = this.field_l_1.getOrDefault(chunkKey, 0L);
         if (var6 - var8 >= 750L) {
            this.field_l_1.put(chunkKey, var6);
            ToastManager.INSTANCE
               .push(
                  this.method_a_2(block) + " found",
                  "X " + pos.getX() + "  Y " + pos.getY() + "  Z " + pos.getZ(),
                  this.method_a_1(block),
                  this.a(block, 255).getRGB()
               );
            mc.world
               .playSound(
                  mc.player, mc.player.getX(), mc.player.getY(), mc.player.getZ(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.MASTER, 0.6F, 0.95F
               );
         }
      }
   }

   private ItemStack method_a_1(Block block) {
      Block var2 = new ItemStack(block.asItem());
      return var2.isEmpty() ? ItemStack.EMPTY : var2;
   }

   private int b(ChunkPos o, ChunkPos t) {
      int var3 = t.x - o.x;
      ChunkPos var4 = t.z - o.z;
      return var3 * var3 + var4 * var4;
   }

   private int p() {
      return mc.options.getClampedViewDistance();
   }

   private double method_b_2() {
      double var1 = this.p() * 16.0 + 16.0;
      return var1 * var1;
   }

   private void a(ChunkPos center, int chunkRadius) {
      ArrayList var3 = new ArrayList();

      for (Long var5 : this.j.keySet()) {
         ChunkPos var6 = new ChunkPos(ChunkPos.getPackedX(var5), ChunkPos.getPackedZ(var5));
         if (Math.abs(var6.x - center.x) > chunkRadius || Math.abs(var6.z - center.z) > chunkRadius) {
            var3.add(var5);
         }
      }

      for (Long var8 : var3) {
         this.b(var8);
         this.field_l_1.remove(var8);
      }
   }

   private void b(long chunkKey) {
      long var3 = this.j.remove(chunkKey);
      if (var3 != null) {
         var3.forEach(this.field_k_1::remove);
      }
   }

   private Color a(Block block, int alpha) {
      Color var3 = this.b.get(block);
      if (var3 != null) {
         return new Color(var3.getRed(), var3.getGreen(), var3.getBlue(), alpha);
      } else {
         Identifier var4 = Registries.BLOCK.getId(block);
         String var5 = var4 == null ? "" : var4.getPath();
         if (block == Blocks.SPAWNER) {
            return new Color(138, 126, 166, alpha);
         } else if (var5.contains("diamond")) {
            return new Color(0, 255, 255, alpha);
         } else if (var5.contains("ancient_debris")) {
            return new Color(196, 120, 72, alpha);
         } else if (var5.contains("emerald")) {
            return new Color(0, 255, 127, alpha);
         } else if (var5.contains("gold")) {
            return new Color(255, 215, 0, alpha);
         } else if (var5.contains("iron")) {
            return new Color(213, 213, 213, alpha);
         } else if (var5.contains("redstone")) {
            return new Color(255, 70, 70, alpha);
         } else {
            return var5.contains("lapis") ? new Color(70, 110, 255, alpha) : new Color(255, 255, 0, alpha);
         }
      }
   }

   private static Color a(Color c, int a) {
      return new Color(c.getRed(), c.getGreen(), c.getBlue(), a);
   }

   private String method_a_2(Block block) {
      try {
         return block.getName().getString();
      } catch (Exception var2) {
         Block var3 = Registries.BLOCK.getId(block);
         return var3 == null ? "Block" : var3.toString();
      }
   }

   private void av() {
      this.j.clear();
      this.field_k_1.clear();
      this.field_l_1.clear();
      synchronized (this.field_a_3) {
         this.field_a_2.clear();
         this.field_k_2.clear();
      }
   }

   private static final class a {
      final double x;
      final double y;
      final double z;
      final Color field_d_1;
      final double field_d_2;

      a(double x, double y, double z, Color color, double distSq) {
         this.x = x;
         this.y = y;
         this.z = z;
         this.field_d_1 = color;
         this.field_d_2 = distSq;
      }
   }
}
