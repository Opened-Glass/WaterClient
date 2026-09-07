package com.water.module.modules.render;

import com.water.gui.ToastManager;
import com.water.module.Category;
import com.water.module.Module;
import com.water.module.ModuleManager;
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
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
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

public final class ExtraESP extends Module {
   private final BlocksSetting field_b_1 = new BlocksSetting("Blocks", Blocks.SPAWNER);
   private final Setting<Boolean> bz = new Setting<>("Notify", true);
   private final Setting<Boolean> bA = new Setting<>("Tracers", true);
   private final Setting<Double> bB = new Setting<>("Tracer Weight", 0.8, 0.1, 4.0);
   private final Setting<Double> bC = new Setting<>("Opacity", 190.0, 0.0, 255.0);
   private final Setting<Double> bD = new Setting<>("Fill Alpha", 35.0, 0.0, 255.0);
   private final Map<Long, Set<BlockPos>> field_m_1 = new ConcurrentHashMap<>();
   private final Map<BlockPos, Block> field_n_1 = new ConcurrentHashMap<>();
   private final Map<Long, Long> o = new ConcurrentHashMap<>();
   private final Map<Block, Color> p = new ConcurrentHashMap<>();
   private final ArrayDeque<Long> field_b_2 = new ArrayDeque<>();
   private final Set<Long> field_m_2 = new HashSet<>();
   private final Object field_b_3 = new Object();
   private volatile Set<Block> l = Collections.emptySet();
   private long x = -1L;
   private int k;
   private boolean ai = true;
   private ChunkPos c;
   private int av = -1;
   private final List<ExtraESP.a> field_e_1 = new ArrayList<>();
   private final ExecutorService d = Executors.newFixedThreadPool(Math.max(2, Math.min(4, Runtime.getRuntime().availableProcessors())), r -> {
      Runnable var1 = new Thread(r, "extraESP-scan");
      var1.setDaemon(true);
      var1.setPriority(5);
      return var1;
   });
   private final AtomicBoolean field_e_2 = new AtomicBoolean(false);
   private final Set<Long> field_n_2 = Collections.synchronizedSet(new HashSet<>());
   private int aw;
   private int ax = Integer.MIN_VALUE;
   private int ay = Integer.MIN_VALUE;

   public ExtraESP() {
      super("Extra ESP", Category.b);
      this.addSetting(this.field_b_1);
      this.addSetting(this.bz);
      this.addSetting(this.bA);
      this.addSetting(this.bB);
      this.addSetting(this.bC);
      this.addSetting(this.bD);
   }

   @Override
   public void onEnable() {
      this.av();
      this.x = -1L;
      this.ai = true;
      this.k = 0;
      this.aw = 0;
      this.c = null;
      this.av = -1;
      this.ax = Integer.MIN_VALUE;
      this.ay = Integer.MIN_VALUE;
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
         if (this.l.isEmpty()) {
            this.av();
         } else {
            this.k++;
            if (this.ai || this.k % 120 == 0) {
               this.field_n_2.clear();
               this.ai = false;
            }

            if (++this.aw >= 1 && !this.field_e_2.get()) {
               this.aw = 0;
               this.aw();
            }
         }
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

   @Override
   public void onRender(MatrixStack matrices, float tickDelta) {
      if (mc.world != null && mc.player != null && !this.field_m_1.isEmpty()) {
         Set var3 = this.l;
         if (!var3.isEmpty()) {
            Camera var4 = RenderUtils.getCamera();
            if (var4 != null) {
               Vec3d var5 = RenderUtils.getCameraPos(var4);
               Vec3d var29 = RenderUtils.getCameraForward(var4);
               float var27 = Freecam.resolveTracerOrigin(var5, tickDelta);
               float var28 = var27.equals(var5) ? var29.multiply(0.1) : var27.subtract(var5);
               double var13 = mc.player.getX();
               double var15 = mc.player.getZ();
               this.field_e_1.clear();

               for (Set var6 : this.field_m_1.values()) {
                  for (BlockPos var8 : var6) {
                     double var21 = var8.getX() - var13;
                     double var23 = var8.getZ() - var15;
                     double var25 = var21 * var21 + var23 * var23;
                     if (!(var25 > 73984.0)) {
                        Block var9 = this.field_n_1.get(var8);
                        if (var9 != null && var3.contains(var9)) {
                           this.field_e_1.add(new ExtraESP.a(var8.getX() - var5.x, var8.getY() - var5.y, var8.getZ() - var5.z, var25, var9));
                        }
                     }
                  }
               }

               if (!this.field_e_1.isEmpty()) {
                  this.field_e_1.sort(Comparator.comparingDouble(e -> e.h));
                  int var31 = Math.min(this.field_e_1.size(), 5000);
                  int var32 = i((int)Math.round(this.bC.getValue()));
                  int var33 = i((int)Math.round(this.bD.getValue()));
                  if (this.bA.getValue() || var32 > 0) {
                     RenderUtils.WorldBatch var34 = RenderUtils.beginWorldBatch(matrices);

                     for (int var37 = var31 - 1; var37 >= 0; var37--) {
                        ExtraESP.a var22 = this.field_e_1.get(var37);
                        Color var40 = this.a(var22.a, var32);
                        Color var24 = this.a(var22.a, 255);
                        Color var41 = this.a(var22.a, 70);
                        Color var26 = this.a(var22.a, 145);
                        var34.renderOutlineBox(
                           var22.e + 0.0, var22.f + 0.0, var22.g + 0.0, var22.e + 1.0 - 0.0, var22.f + 1.0 - 0.0, var22.g + 1.0 - 0.0, var40
                        );
                        if (this.bA.getValue()) {
                           Vec3d var36 = new Vec3d(var22.e + 0.5, var22.f + 0.5, var22.g + 0.5);
                           var34.renderLine(var41, var28, var36, this.bB.getValue().floatValue() + 2.4F);
                           var34.renderLine(var26, var28, var36, this.bB.getValue().floatValue() + 1.4F);
                           var34.renderLine(var24, var28, var36, this.bB.getValue().floatValue());
                        }
                     }

                     var34.flush();
                  }

                  if (var33 > 0) {
                     RenderUtils.WorldBatch var35 = RenderUtils.beginWorldBatch(matrices);

                     for (int var38 = var31 - 1; var38 >= 0; var38--) {
                        ExtraESP.a var39 = this.field_e_1.get(var38);
                        var35.renderFilledBox(
                           var39.e + 0.0, var39.f + 0.0, var39.g + 0.0, var39.e + 1.0 - 0.0, var39.f + 1.0 - 0.0, var39.g + 1.0 - 0.0, this.a(var39.a, var33)
                        );
                     }

                     var35.flush();
                  }
               }
            }
         }
      }
   }

   private void au() {
      long var1 = this.field_b_1.getVersion();
      if (var1 != this.x) {
         this.x = var1;
         this.l = Set.copyOf(this.field_b_1.getSelectedBlocks());
         this.av();
         this.ai = true;
      }
   }

   private void aw() {
      if (this.field_e_2.compareAndSet(false, true)) {
         if (mc.world != null && mc.player != null) {
            ChunkPos var1 = mc.player.getChunkPos();
            int var2 = this.p();
            if (Math.abs(var1.x - this.ax) > 1 || Math.abs(var1.z - this.ay) > 1) {
               this.field_n_2.clear();
               this.ax = var1.x;
               this.ay = var1.z;
            }

            this.c = var1;
            this.av = var2;
            ArrayList var3 = new ArrayList();
            HashSet var4 = new HashSet();

            try {
               for (int var5 = var1.x - var2; var5 <= var1.x + var2; var5++) {
                  for (int var6 = var1.z - var2; var6 <= var1.z + var2; var6++) {
                     WorldChunk var7 = mc.world.getChunkManager().getWorldChunk(var5, var6, false);
                     if (var7 != null) {
                        long var8 = var7.getPos().toLong();
                        var4.add(var8);
                        if (!this.field_n_2.contains(var8)) {
                           var3.add(var7);
                        }
                     }
                  }
               }
            } catch (Throwable var10) {
               this.field_e_2.set(false);
               return;
            }

            this.d(var4);
            this.a(var1, var2);
            if (var3.isEmpty()) {
               this.field_e_2.set(false);
            } else {
               var3.sort(Comparator.comparingInt(chunk -> this.b(var1, chunk.getPos())));
               this.d.execute(() -> {
                  try {
                     for (WorldChunk var2x : var3) {
                        this.a(var2x);
                        this.field_n_2.add(var2x.getPos().toLong());
                     }
                  } finally {
                     this.field_e_2.set(false);
                  }
               });
            }
         } else {
            this.field_e_2.set(false);
         }
      }
   }

   private void c(long chunkKey) {
      this.field_n_2.remove(chunkKey);
   }

   private void d(Set<Long> loadedChunkKeys) {
      ArrayList var2 = new ArrayList();

      for (Long var4 : this.field_m_1.keySet()) {
         if (!loadedChunkKeys.contains(var4)) {
            var2.add(var4);
         }
      }

      for (Long var6 : var2) {
         this.b(var6);
         this.o.remove(var6);
         this.field_n_2.remove(var6);
      }
   }

   private void a(WorldChunk chunk) {
      Set var2 = this.l;
      if (!var2.isEmpty() && mc.world != null) {
         int var3 = mc.world.getBottomY();
         int var4 = mc.world.getBottomY() + mc.world.getHeight();
         int var5 = mc.world.getBottomSectionCoord();
         ChunkPos var6 = chunk.getPos();
         long var7 = var6.toLong();
         Set var9 = this.field_m_1.get(var7);
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
                              this.field_n_1.put(var20, var19);
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

         if (var9 != null && !var10.isEmpty()) {
            for (BlockPos var23 : var9) {
               if (!var10.contains(var23)) {
                  this.field_n_1.remove(var23);
               }
            }
         }

         if (!var10.isEmpty()) {
            this.field_m_1.put(var7, var10);
            if (var11 != null) {
               this.a(var7, var11, var12);
            }
         }
      }
   }

   private void a(long chunkKey, Block block, BlockPos pos) {
      if (this.bz.getValue() && mc.player != null && mc.world != null && pos != null) {
         long var5 = System.currentTimeMillis();
         long var7 = this.o.getOrDefault(chunkKey, 0L);
         if (var5 - var7 >= 900L) {
            this.o.put(chunkKey, var5);
            ToastManager.INSTANCE
               .push(
                  this.method_a_2(block) + " found",
                  "X " + pos.getX() + "  Y " + pos.getY() + "  Z " + pos.getZ(),
                  this.method_a_1(block),
                  this.a(block, 255).getRGB()
               );
            mc.world
               .playSound(
                  mc.player, mc.player.getX(), mc.player.getY(), mc.player.getZ(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.MASTER, 0.45F, 1.05F
               );
         }
      }
   }

   private void av() {
      this.field_m_1.clear();
      this.field_n_1.clear();
      this.o.clear();
      this.field_n_2.clear();
      synchronized (this.field_b_3) {
         this.field_b_2.clear();
         this.field_m_2.clear();
      }
   }

   private void a(ChunkPos center, int chunkRadius) {
      ArrayList var3 = new ArrayList();

      for (Long var5 : this.field_m_1.keySet()) {
         ChunkPos var6 = new ChunkPos(ChunkPos.getPackedX(var5), ChunkPos.getPackedZ(var5));
         if (Math.abs(var6.x - center.x) > chunkRadius || Math.abs(var6.z - center.z) > chunkRadius) {
            var3.add(var5);
         }
      }

      for (Long var8 : var3) {
         this.b(var8);
         this.o.remove(var8);
      }
   }

   private void b(long chunkKey) {
      long var3 = this.field_m_1.remove(chunkKey);
      if (var3 != null) {
         var3.forEach(this.field_n_1::remove);
      }
   }

   private ItemStack method_a_1(Block block) {
      Block var2 = new ItemStack(block.asItem());
      return var2.isEmpty() ? ItemStack.EMPTY : var2;
   }

   private Color a(Block block, int alpha) {
      Color var3 = this.p.get(block);
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
         } else if (var5.contains("lapis")) {
            return new Color(70, 110, 255, alpha);
         } else if (var5.contains("chest")) {
            return new Color(210, 140, 60, alpha);
         } else {
            return var5.contains("barrel") ? new Color(200, 130, 100, alpha) : new Color(177, 92, 255, alpha);
         }
      }
   }

   public void a(Map<Block, Color> colors) {
      this.p.clear();
      if (colors != null) {
         for (Entry var2 : colors.entrySet()) {
            if (var2.getKey() != null && var2.getValue() != null) {
               this.p.put((Block)var2.getKey(), (Color)var2.getValue());
            }
         }
      }

      ModuleManager.INSTANCE.c();
   }

   public Map<Block, Color> b() {
      return new LinkedHashMap<>(this.p);
   }

   private String method_a_2(Block block) {
      try {
         return block.getName().getString();
      } catch (Throwable var2) {
         Block var3 = Registries.BLOCK.getId(block);
         return var3 == null ? "Block" : var3.getPath();
      }
   }

   private int b(ChunkPos origin, ChunkPos target) {
      int var3 = target.x - origin.x;
      ChunkPos var4 = target.z - origin.z;
      return var3 * var3 + var4 * var4;
   }

   private int p() {
      return 20;
   }

   private static int i(int v) {
      return Math.max(0, Math.min(255, v));
   }

   private static final class a {
      final double e;
      final double f;
      final double g;
      final double h;
      final Block a;

      a(double x, double y, double z, double distSq, Block block) {
         this.e = x;
         this.f = y;
         this.g = z;
         this.h = distSq;
         this.a = block;
      }
   }
}
