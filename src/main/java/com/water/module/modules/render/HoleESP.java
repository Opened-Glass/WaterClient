package com.water.module.modules.render;

import com.water.module.Category;
import com.water.module.Module;
import com.water.setting.Setting;
import com.water.utils.RenderUtils;
import java.awt.Color;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.MatrixStack.Entry;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;
import org.lwjgl.opengl.GL11;

public final class HoleESP extends Module {
   private final Setting<Double> bJ = new Setting<>("Fill Alpha", 60.0, 0.0, 255.0);
   private final Setting<Color> bK = new Setting<>("Color", new Color(255, 100, 0));
   private final Setting<Double> bL = new Setting<>("Range", 64.0, 16.0, 128.0);
   private final Setting<Boolean> bM = new Setting<>("Gradient Fill", true);
   private final Map<Long, HoleESP.b> field_r_1 = new ConcurrentHashMap<>();
   private final Queue<Long> field_a_1 = new ArrayDeque<>();
   private final Set<Long> field_r_2 = ConcurrentHashMap.newKeySet();
   private final Set<HoleESP.a> s = ConcurrentHashMap.newKeySet();
   private ExecutorService c;
   private ClientWorld field_a_2;

   public HoleESP() {
      super("Hole ESP", Category.b);
      this.addSetting(this.bJ);
      this.addSetting(this.bK);
      this.addSetting(this.bL);
      this.addSetting(this.bM);
   }

   @Override
   public void onEnable() {
      this.field_a_2 = mc.world;
      this.az();
      this.clear();
   }

   @Override
   public void onDisable() {
      this.ba();
      this.clear();
      this.field_a_2 = null;
   }

   @Override
   public void onTick() {
      if (mc.world != null && mc.player != null) {
         if (mc.world != this.field_a_2) {
            this.field_a_2 = mc.world;
            this.clear();
         }

         this.az();
         this.ax();
      }
   }

   @Override
   public void onRender(MatrixStack matrices, float tickDelta) {
      if (mc.world != null && mc.player != null && !this.s.isEmpty()) {
         float var15 = RenderUtils.getCamera();
         if (var15 != null) {
            float var16 = RenderUtils.getCameraPos(var15);
            int var3 = this.a(this.bJ.getValue());
            boolean var4 = this.bM.getValue();
            BufferAllocator var5 = new BufferAllocator(2097152);
            Immediate var6 = VertexConsumerProvider.immediate(var5);
            VertexConsumer var7 = var6.getBuffer(RenderLayers.debugFilledBox());
            MatrixStack var14 = matrices.peek();
            boolean var8 = false;

            for (HoleESP.a var10 : this.s) {
               if (var10.y()) {
                  Box var19 = var10.a;
                  if (RenderUtils.isWorldBoxVisible(var19.minX, var19.minY, var19.minZ, var19.maxX, var19.maxY, var19.maxZ)) {
                     Color var17 = this.bK.getValue();
                     Color var11 = this.a(var17, var3);
                     Box var20 = new Box(
                        var19.minX - var16.x, var19.minY - var16.y, var19.minZ - var16.z, var19.maxX - var16.x, var19.maxY - var16.y, var19.maxZ - var16.z
                     );
                     if (var4) {
                        this.a(var7, var14, var20, var17, var3);
                     } else {
                        this.a(var7, var14, var20, this.toArgb(var11));
                     }

                     var8 = true;
                  }
               }
            }

            if (!var8) {
               var5.close();
            } else {
               boolean var18 = GL11.glIsEnabled(2929);
               GL11.glDisable(2929);
               GL11.glDepthMask(false);

               try {
                  var6.draw();
               } finally {
                  GL11.glDepthMask(true);
                  if (var18) {
                     GL11.glEnable(2929);
                  }

                  var5.close();
               }
            }
         }
      }
   }

   private void a(VertexConsumer consumer, Entry entry, Box box, int color) {
      float var5 = (float)box.minX;
      float var6 = (float)box.minY;
      float var7 = (float)box.minZ;
      float var8 = (float)box.maxX;
      float var9 = (float)box.maxY;
      Box var10 = (float)box.maxZ;
      this.a(consumer, entry, var5, var6, var7, var8, var6, var7, var8, var6, var10, var5, var6, var10, color);
      this.a(consumer, entry, var5, var9, var7, var5, var9, var10, var8, var9, var10, var8, var9, var7, color);
      this.a(consumer, entry, var5, var6, var7, var5, var9, var7, var8, var9, var7, var8, var6, var7, color);
      this.a(consumer, entry, var5, var6, var10, var8, var6, var10, var8, var9, var10, var5, var9, var10, color);
      this.a(consumer, entry, var5, var6, var7, var5, var6, var10, var5, var9, var10, var5, var9, var7, color);
      this.a(consumer, entry, var8, var6, var7, var8, var9, var7, var8, var9, var10, var8, var6, var10, color);
   }

   private void a(VertexConsumer consumer, Entry entry, Box box, Color baseColor, int maxAlpha) {
      double var6 = Math.max(0.001, box.maxY - box.minY);
      int var21 = Math.max(1, MathHelper.ceil(var6));
      int var7 = Math.max(6, Math.round(maxAlpha * 0.18F));
      float var8 = (float)box.minX;
      float var9 = (float)box.minZ;
      float var10 = (float)box.maxX;
      float var11 = (float)box.maxZ;
      int var12 = 0;
      int var13 = 0;

      for (int var14 = 0; var14 < var21; var14++) {
         double var17 = (double)var14 / var21;
         double var19 = (double)(var14 + 1) / var21;
         float var15 = (float)MathHelper.lerp(var17, box.minY, box.maxY);
         float var16 = (float)MathHelper.lerp(var19, box.minY, box.maxY);
         float var22 = 1.0F - (float)var14 / Math.max(1, var21 - 1);
         float var18 = 1.0F - (float)(var14 + 1) / Math.max(1, var21);
         int var23 = this.toArgb(this.a(baseColor, Math.max(var7, Math.round(maxAlpha * var22))));
         int var24 = this.toArgb(this.a(baseColor, Math.max(var7, Math.round(maxAlpha * var18))));
         if (var14 == 0) {
            var13 = var23;
         }

         if (var14 == var21 - 1) {
            var12 = var24;
         }

         this.a(consumer, entry, var8, var15, var9, var8, var16, var9, var10, var16, var9, var10, var15, var9, var23, var24);
         this.a(consumer, entry, var8, var15, var11, var10, var15, var11, var10, var16, var11, var8, var16, var11, var23, var24);
         this.a(consumer, entry, var8, var15, var9, var8, var15, var11, var8, var16, var11, var8, var16, var9, var23, var24);
         this.a(consumer, entry, var10, var15, var9, var10, var16, var9, var10, var16, var11, var10, var15, var11, var23, var24);
      }

      this.a(consumer, entry, var8, (float)box.maxY, var9, var8, (float)box.maxY, var11, var10, (float)box.maxY, var11, var10, (float)box.maxY, var9, var12);
      this.a(consumer, entry, var8, (float)box.minY, var9, var10, (float)box.minY, var9, var10, (float)box.minY, var11, var8, (float)box.minY, var11, var13);
   }

   private void a(
      VertexConsumer consumer,
      Entry entry,
      float x1,
      float y1,
      float z1,
      float x2,
      float y2,
      float z2,
      float x3,
      float y3,
      float z3,
      float x4,
      float y4,
      float z4,
      int bottomColor,
      int topColor
   ) {
      consumer.vertex(entry, x1, y1, z1).color(bottomColor);
      consumer.vertex(entry, x2, y2, z2).color(topColor);
      consumer.vertex(entry, x3, y3, z3).color(topColor);
      consumer.vertex(entry, x4, y4, z4).color(bottomColor);
   }

   private void a(
      VertexConsumer consumer,
      Entry entry,
      float x1,
      float y1,
      float z1,
      float x2,
      float y2,
      float z2,
      float x3,
      float y3,
      float z3,
      float x4,
      float y4,
      float z4,
      int color
   ) {
      consumer.vertex(entry, x1, y1, z1).color(color);
      consumer.vertex(entry, x2, y2, z2).color(color);
      consumer.vertex(entry, x3, y3, z3).color(color);
      consumer.vertex(entry, x4, y4, z4).color(color);
   }

   private void ax() {
      if (mc.world != null && mc.player != null) {
         for (HoleESP.b var2 : this.field_r_1.values()) {
            var2.al = false;
         }

         int var9 = Math.max(1, this.s() / 16);
         int var10 = mc.player.getChunkPos().x;
         int var3 = mc.player.getChunkPos().z;

         for (int var4 = var10 - var9; var4 <= var10 + var9; var4++) {
            for (int var5 = var3 - var9; var5 <= var3 + var9; var5++) {
               WorldChunk var6 = mc.world.getChunkManager().getWorldChunk(var4, var5, false);
               if (var6 != null) {
                  long var7 = ChunkPos.toLong(var4, var5);
                  HoleESP.b var12 = this.field_r_1.get(var7);
                  if (var12 != null) {
                     var12.al = true;
                  } else if (this.field_r_2.add(var7)) {
                     this.field_a_1.add(var7);
                  }
               }
            }
         }

         this.ay();
         this.field_r_1.entrySet().removeIf(entry -> !entry.getValue().al);
         Set var11 = this.field_r_1.keySet();
         this.s.removeIf(hole -> !this.a(hole.a, var11));
      }
   }

   private boolean a(Box box, Set<Long> activeKeys) {
      int var3 = (int)Math.floor(box.getCenter().x) >> 4;
      Box var4 = (int)Math.floor(box.getCenter().z) >> 4;
      return activeKeys.contains(ChunkPos.toLong(var3, var4));
   }

   private void ay() {
      if (this.c != null && mc.world != null) {
         int var1 = 0;

         while (!this.field_a_1.isEmpty() && var1 < 200) {
            Long var2 = this.field_a_1.poll();
            if (var2 != null) {
               this.field_r_2.remove(var2);
               int var3 = ChunkPos.getPackedX(var2);
               int var4 = ChunkPos.getPackedZ(var2);
               WorldChunk var5 = mc.world.getChunkManager().getWorldChunk(var3, var4, false);
               if (var5 != null) {
                  this.field_r_1.put(var2, new HoleESP.b(var3, var4));
                  this.c.execute(() -> this.b(var5));
                  var1++;
               }
            }
         }
      }
   }

   private void b(WorldChunk chunk) {
      ClientWorld var2 = mc.world;
      if (var2 != null && var2 == this.field_a_2 && this.isEnabled()) {
         ChunkSection[] var3 = chunk.getSectionArray();
         int var4 = var2.getBottomY();
         int var12 = var2.getBottomY() + var2.getHeight();
         int var5 = var4;

         for (ChunkSection var8 : var3) {
            if (var8 != null && !var8.isEmpty()) {
               for (int var14 = 0; var14 < 16; var14++) {
                  for (int var9 = 0; var9 < 16; var9++) {
                     for (int var10 = 0; var10 < 16; var10++) {
                        int var11 = var5 + var10;
                        if (var11 > var4 && var11 < var12) {
                           BlockPos var15 = new BlockPos(chunk.getPos().getStartX() + var9, var11, chunk.getPos().getStartZ() + var14);
                           this.method_c_1(var15);
                           this.method_d_1(var15);
                        }
                     }
                  }
               }
            }

            var5 += 16;
         }
      }
   }

   private void method_c_1(BlockPos pos) {
      if (this.method_d_2(pos) && !this.method_d_2(pos.up())) {
         Mutable var2 = pos.mutableCopy();

         while (this.method_d_2(var2)) {
            var2.move(Direction.DOWN);
         }

         int var3 = pos.getY() - var2.getY();
         if (var3 >= this.t()) {
            BlockPos var4 = new Box(pos.getX(), var2.getY() + 1, pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
            if (!this.a(var4)) {
               this.s.add(new HoleESP.a(var4, var3, true));
            }
         }
      }
   }

   private void method_d_1(BlockPos pos) {
      if (this.e(pos) && !this.e(pos.up())) {
         Mutable var2 = pos.mutableCopy();

         while (this.e(var2)) {
            var2.move(Direction.DOWN);
         }

         int var3 = pos.getY() - var2.getY();
         if (var3 >= this.t()) {
            Box var4 = new Box(pos.getX(), var2.getY() + 1, pos.getZ(), pos.getX() + 3, pos.getY() + 1, pos.getZ() + 1);
            if (!this.a(var4)) {
               this.s.add(new HoleESP.a(var4, var3, false));
            }
         }
      }

      if (this.f(pos) && !this.f(pos.up())) {
         Mutable var5 = pos.mutableCopy();

         while (this.f(var5)) {
            var5.move(Direction.DOWN);
         }

         int var7 = pos.getY() - var5.getY();
         if (var7 >= this.t()) {
            Box var6 = new Box(pos.getX(), var5.getY() + 1, pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 3);
            if (!this.a(var6)) {
               this.s.add(new HoleESP.a(var6, var7, false));
            }
         }
      }
   }

   private boolean a(Box box) {
      for (HoleESP.a var3 : this.s) {
         if (var3.a.equals(box) || var3.a.intersects(box)) {
            return true;
         }
      }

      return false;
   }

   private boolean l(BlockState state) {
      return state.getBlock() == Blocks.OAK_LEAVES
         || state.getBlock() == Blocks.SPRUCE_LEAVES
         || state.getBlock() == Blocks.BIRCH_LEAVES
         || state.getBlock() == Blocks.JUNGLE_LEAVES
         || state.getBlock() == Blocks.ACACIA_LEAVES
         || state.getBlock() == Blocks.DARK_OAK_LEAVES
         || state.getBlock() == Blocks.CHERRY_LEAVES
         || state.getBlock() == Blocks.MANGROVE_LEAVES
         || state.getBlock() == Blocks.AZALEA_LEAVES
         || state.getBlock() == Blocks.FLOWERING_AZALEA_LEAVES
         || state.getBlock() == Blocks.GLASS
         || state.getBlock() == Blocks.GLASS_PANE
         || state.getBlock() == Blocks.VINE
         || state.getBlock() == Blocks.CAVE_VINES
         || state.getBlock() == Blocks.CAVE_VINES_PLANT
         || state.getBlock() == Blocks.WEEPING_VINES
         || state.getBlock() == Blocks.WEEPING_VINES_PLANT
         || state.getBlock() == Blocks.TWISTING_VINES
         || state.getBlock() == Blocks.TWISTING_VINES_PLANT
         || state.getBlock() == Blocks.GLOW_LICHEN
         || state.getBlock() == Blocks.HANGING_ROOTS
         || state.getBlock() == Blocks.SPORE_BLOSSOM
         || state.getBlock() == Blocks.BAMBOO
         || state.getBlock() == Blocks.BAMBOO_SAPLING
         || state.getBlock() == Blocks.KELP
         || state.getBlock() == Blocks.KELP_PLANT
         || state.getBlock() == Blocks.SEAGRASS
         || state.getBlock() == Blocks.TALL_SEAGRASS
         || state.getBlock() == Blocks.SHORT_GRASS
         || state.getBlock() == Blocks.TALL_GRASS
         || state.getBlock() == Blocks.FERN
         || state.getBlock() == Blocks.LARGE_FERN
         || state.getBlock() == Blocks.SUGAR_CANE
         || state.getBlock() == Blocks.DEAD_BUSH
         || state.getBlock() == Blocks.SWEET_BERRY_BUSH;
   }

   private boolean method_c_2(BlockPos pos) {
      if (mc.world == null) {
         return false;
      } else {
         BlockPos var2 = mc.world.getBlockState(pos);
         return !var2.isAir() && !this.l(var2);
      }
   }

   private boolean method_d_2(BlockPos pos) {
      return this.g(pos) && this.method_c_2(pos.north()) && this.method_c_2(pos.south()) && this.method_c_2(pos.east()) && this.method_c_2(pos.west());
   }

   private boolean e(BlockPos pos) {
      return this.g(pos)
         && this.g(pos.east())
         && this.g(pos.east(2))
         && this.method_c_2(pos.north())
         && this.method_c_2(pos.south())
         && this.method_c_2(pos.west())
         && this.method_c_2(pos.east(3));
   }

   private boolean f(BlockPos pos) {
      return this.g(pos)
         && this.g(pos.south())
         && this.g(pos.south(2))
         && this.method_c_2(pos.east())
         && this.method_c_2(pos.west())
         && this.method_c_2(pos.north())
         && this.method_c_2(pos.south(3));
   }

   private boolean g(BlockPos pos) {
      if (mc.world == null) {
         return false;
      } else {
         BlockState var2 = mc.world.getBlockState(pos);
         if (!var2.isAir()) {
            return false;
         } else {
            var2 = mc.world.getBlockState(pos.down());
            BlockPos var3 = mc.world.getBlockState(pos.up());
            return !this.m(var2) && !this.m(var3) && !this.n(var2) && !this.n(var3);
         }
      }
   }

   private boolean m(BlockState state) {
      return state.getBlock() == Blocks.KELP
         || state.getBlock() == Blocks.KELP_PLANT
         || state.getBlock() == Blocks.SEAGRASS
         || state.getBlock() == Blocks.TALL_SEAGRASS
         || state.getBlock() == Blocks.VINE
         || state.getBlock() == Blocks.CAVE_VINES
         || state.getBlock() == Blocks.CAVE_VINES_PLANT
         || state.getBlock() == Blocks.WEEPING_VINES
         || state.getBlock() == Blocks.WEEPING_VINES_PLANT
         || state.getBlock() == Blocks.TWISTING_VINES
         || state.getBlock() == Blocks.TWISTING_VINES_PLANT
         || state.getBlock() == Blocks.GLOW_LICHEN
         || state.getBlock() == Blocks.HANGING_ROOTS
         || state.getBlock() == Blocks.SPORE_BLOSSOM;
   }

   private boolean n(BlockState state) {
      return state.getBlock() == Blocks.RAIL
         || state.getBlock() == Blocks.POWERED_RAIL
         || state.getBlock() == Blocks.DETECTOR_RAIL
         || state.getBlock() == Blocks.ACTIVATOR_RAIL
         || state.getBlock() == Blocks.OAK_FENCE
         || state.getBlock() == Blocks.DARK_OAK_FENCE
         || state.getBlock() == Blocks.SPRUCE_FENCE
         || state.getBlock() == Blocks.COBWEB;
   }

   private void clear() {
      this.field_r_1.clear();
      this.field_a_1.clear();
      this.field_r_2.clear();
      this.s.clear();
   }

   private void az() {
      if (this.c == null || this.c.isShutdown()) {
         this.c = Executors.newFixedThreadPool(2, task -> {
            Runnable var1 = new Thread(task, "water-hole-esp");
            var1.setDaemon(true);
            return var1;
         });
      }
   }

   private void ba() {
      ExecutorService var1 = this.c;
      this.c = null;
      if (var1 != null) {
         var1.shutdown();

         try {
            if (!var1.awaitTermination(500L, TimeUnit.MILLISECONDS)) {
               var1.shutdownNow();
            }
         } catch (InterruptedException var2) {
            var1.shutdownNow();
            Thread.currentThread().interrupt();
         }
      }
   }

   private int s() {
      return MathHelper.clamp((int)Math.round(this.bL.getValue()), 16, 128);
   }

   private int t() {
      return 7;
   }

   private int a(double value) {
      return MathHelper.clamp((int)Math.round(value), 0, 255);
   }

   private Color a(Color base, int alphaValue) {
      return new Color(base.getRed(), base.getGreen(), base.getBlue(), MathHelper.clamp(alphaValue, 0, 255));
   }

   private int toArgb(Color color) {
      return color.getAlpha() << 24 | color.getRed() << 16 | color.getGreen() << 8 | color.getBlue();
   }

   private static final class a {
      private final Box a;
      private final int az;
      private final boolean ak;
      private final long y;

      private a(Box box, int depth, boolean is1x1) {
         this.a = box;
         this.az = depth;
         this.ak = is1x1;
         this.y = System.currentTimeMillis();
      }

      private boolean y() {
         return true;
      }

      @Override
      public boolean equals(Object obj) {
         if (this == obj) {
            return true;
         } else {
            return obj instanceof Object var2 ? Objects.equals(this.a, var2.a) : false;
         }
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.a);
      }
   }

   private static final class b {
      private final int ba;
      private final int bb;
      private boolean al;

      private b(int x, int z) {
         this.ba = x;
         this.bb = z;
         this.al = true;
      }
   }
}
