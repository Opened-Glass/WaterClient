package com.water.module.modules.misc;

import com.water.gui.ToastManager;
import com.water.module.Category;
import com.water.module.Module;
import com.water.module.modules.client.WaterPlus;
import com.water.setting.ModeSetting;
import com.water.setting.Setting;
import com.water.utils.RenderUtils;
import java.awt.Color;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.WorldChunk;

public final class TunnelBaseFinder extends Module {
   private static final int[][] a = new int[][]{{0, 1}, {-1, 0}, {0, -1}, {1, 0}};
   private final Setting<Integer> aT = new Setting<>("Min Y", -59, -64, 0);
   private final Setting<Integer> aU = new Setting<>("Max Y", -50, -64, 0);
   private final Setting<Integer> aV = new Setting<>("Storage Threshold", 8, 1, 500);
   private final Setting<Integer> aW = new Setting<>("Hazard Scan", 7, 3, 14);
   private final ModeSetting e = new ModeSetting("Tunnel Size", "1x1", "1x1", "3x3 (Center Only)");
   private final Setting<Boolean> aX = new Setting<>("Auto Pickaxe", true);
   private final Setting<Float> aY = new Setting<>("Turn Speed", 4.0F, 0.5F, 12.0F);
   private final Setting<Integer> aZ = new Setting<>("Mine Delay", 3, 1, 8);
   private final Setting<Boolean> bn = new Setting<>("No Pauses", false);
   private final Setting<Boolean> bo = new Setting<>("Cave Bypass", true);
   private final Setting<Boolean> bp = new Setting<>("Route Line", true);
   private final Setting<Boolean> bq = new Setting<>("Disconnect On Totem Pop", true);
   private final Set<ChunkPos> j = new HashSet<>();
   private final Random field_c_1 = new Random();
   private int ap;
   private int aq;
   private int ar;
   private int as;
   private int at;
   private int au;
   private boolean ah;
   private BlockPos field_c_2;

   public TunnelBaseFinder() {
      super("Tunnel Base Finder", Category.c);
      this.addSetting(this.aT);
      this.addSetting(this.aU);
      this.addSetting(this.aV);
      this.addSetting(this.aW);
      this.addSetting(this.e);
      this.addSetting(this.aX);
      this.addSetting(this.aY);
      this.addSetting(this.aZ);
      this.addSetting(this.bn);
      this.addSetting(this.bo);
      this.addSetting(this.bp);
      this.addSetting(this.bq);
   }

   @Override
   public void onEnable() {
      this.ap = mc.player == null ? 0 : Math.floorMod(Math.round(mc.player.getYaw() / 90.0F), 4);
      this.aq = 0;
      this.ar = 0;
      this.as = 0;
      this.at = 0;
      this.au = 0;
      this.ah = false;
      this.field_c_2 = null;
      this.j.clear();
   }

   @Override
   public void onDisable() {
      this.at();
      this.j.clear();
   }

   @Override
   public void onTick() {
      if (mc.player != null && mc.world != null && mc.interactionManager != null && mc.currentScreen == null && !this.ah) {
         this.as();
         int var1 = mc.player.getBlockY();
         if (this.ah || var1 < Math.min(this.aT.getValue(), this.aU.getValue()) || var1 > Math.max(this.aT.getValue(), this.aU.getValue())) {
            this.at();
         } else if (!this.bn.getValue() && this.aq > 0) {
            this.aq--;
            this.at();
         } else {
            if (this.bn.getValue()) {
               this.aq = 0;
               this.as = 0;
            }

            if (this.aX.getValue() && !this.x()) {
               this.t("No pickaxe found in hotbar");
               this.setEnabled(false);
            } else if (!this.bn.getValue() && ++this.as >= 4800) {
               this.as = 0;
               this.aq = 200;
               this.at();
            } else {
               if (--this.ar <= 0 || this.g(this.ap) < 0) {
                  this.ap = this.o();
                  this.ar = 12 + this.field_c_1.nextInt(18);
               }

               this.au = this.h(this.ap);
               BlockHitResult var4 = mc.crosshairTarget instanceof BlockHitResult var3 ? var3 : null;
               BlockPos var5 = this.a(var4);
               if (var5 == null) {
                  mc.interactionManager.cancelBlockBreaking();
                  mc.options.attackKey.setPressed(false);
                  mc.options.forwardKey.setPressed(true);
               } else {
                  this.b(var5);
                  if (this.bn.getValue()) {
                     mc.options.sprintKey.setPressed(true);
                     mc.options.forwardKey.setPressed(true);
                     mc.player.setSprinting(true);
                  }

                  if (!mc.world.getBlockState(var5).isAir()) {
                     if (!this.bn.getValue()) {
                        mc.options.forwardKey.setPressed(false);
                     }

                     mc.crosshairTarget = new BlockHitResult(Vec3d.ofCenter(var5), Direction.UP, var5, false);
                     if (++this.at >= this.aZ.getValue()) {
                        this.at = 0;
                        mc.interactionManager.updateBlockBreakingProgress(var5, Direction.UP);
                        mc.player.swingHand(Hand.MAIN_HAND);
                     }

                     mc.options.attackKey.setPressed(true);
                  } else {
                     mc.interactionManager.cancelBlockBreaking();
                     this.at = 0;
                     mc.options.attackKey.setPressed(false);
                     mc.options.forwardKey.setPressed(true);
                  }

                  mc.options.leftKey.setPressed(false);
                  mc.options.rightKey.setPressed(false);
               }
            }
         }
      } else {
         this.at();
      }
   }

   @Override
   public void onPacketReceive(Packet<?> packet) {
      if (this.bq.getValue() && mc.world != null && mc.player != null && packet instanceof Packet var2 && var2.getStatus() == 35) {
         Packet var3 = var2.getEntity(mc.world);
         if (var3 == mc.player) {
            this.at();
            if (mc.getNetworkHandler() != null && mc.getNetworkHandler().getConnection() != null) {
               mc.getNetworkHandler()
                  .getConnection()
                  .disconnect(
                     Text.literal(
                        "Tunnel Base Finder: totem popped at X: " + mc.player.getBlockX() + " Y: " + mc.player.getBlockY() + " Z: " + mc.player.getBlockZ()
                     )
                  );
            }
         }
      }
   }

   @Override
   public void onRender(MatrixStack matrices, float tickDelta) {
      if (this.bp.getValue() && mc.player != null && !this.ah) {
         float var4 = RenderUtils.getCamera();
         if (var4 != null) {
            float var5 = RenderUtils.getCameraPos(var4);
            float var6 = new Vec3d(mc.player.getX(), mc.player.getY() + 0.15, mc.player.getZ()).subtract(var5);
            int[] var3 = a[this.ap];
            Vec3d var7 = var6.add(var3[0] * this.aW.getValue(), this.au, var3[1] * this.aW.getValue());
            RenderUtils.renderLine(matrices, new Color(70, 220, 255, 210), var6, var7, 2.25F);
         }
      }
   }

   private int o() {
      int var1 = this.ap;
      int var2 = Integer.MIN_VALUE;

      for (int var3 = 0; var3 < 4; var3++) {
         if (var3 != (this.ap + 2 & 3)) {
            int var4 = this.g(var3) + (var3 == this.ap ? 5 : 0) + this.field_c_1.nextInt(3);
            if (var4 > var2) {
               var2 = var4;
               var1 = var3;
            }
         }
      }

      return var1;
   }

   private int g(int d) {
      return this.method_a_1(d, 0);
   }

   private int method_a_1(int d, int yOffset) {
      BlockPos var3 = mc.player.getBlockPos();
      int var10 = a[d];
      int var4 = 0;
      int var5 = 0;

      for (int var6 = 1; var6 <= this.aW.getValue(); var6++) {
         for (int var7 = -1; var7 <= 1; var7++) {
            for (int var8 = -1; var8 <= 2; var8++) {
               BlockPos var9 = new BlockPos(
                  var3.getX() + var10[0] * var6 + var10[1] * var7, var3.getY() + yOffset + var8, var3.getZ() + var10[1] * var6 - var10[0] * var7
               );
               BlockState var12 = mc.world.getBlockState(var9);
               if (var12.isOf(Blocks.LAVA) || !var12.getFluidState().isEmpty()) {
                  return -10000;
               }

               if (var12.isAir()) {
                  var5++;
               } else {
                  var4++;
               }
            }
         }
      }

      if (var5 > this.aW.getValue() * 5) {
         var4 -= var5 * 3;
      }

      BlockPos var11 = var3.add(var10[0], yOffset - 1, var10[1]);
      if (mc.world.getBlockState(var11).isAir() || !mc.world.getFluidState(var11).isEmpty()) {
         var4 -= 200;
      }

      return var4;
   }

   private int h(int d) {
      if (!this.bo.getValue()) {
         return 0;
      } else if (!this.method_a_2(d, 0)) {
         return 0;
      } else {
         int var2 = this.method_a_1(d, -2);
         int var3 = this.method_a_1(d, 2);
         d = this.method_a_1(d, 0) - 120;
         if (var2 >= var3 && var2 > d) {
            return -2;
         } else {
            return var3 > d ? 2 : 0;
         }
      }
   }

   private boolean method_a_2(int d, int yOffset) {
      BlockPos var3 = mc.player.getBlockPos();
      int var10 = a[d];
      int var4 = 0;
      int var5 = 0;

      for (int var6 = 1; var6 <= Math.min(5, this.aW.getValue()); var6++) {
         for (int var7 = -1; var7 <= 1; var7++) {
            for (int var8 = -1; var8 <= 2; var8++) {
               BlockPos var9 = new BlockPos(
                  var3.getX() + var10[0] * var6 + var10[1] * var7, var3.getY() + yOffset + var8, var3.getZ() + var10[1] * var6 - var10[0] * var7
               );
               BlockState var11 = mc.world.getBlockState(var9);
               if (var11.isOf(Blocks.LAVA) || !var11.getFluidState().isEmpty()) {
                  return true;
               }

               if (var11.isAir()) {
                  var4++;
               }

               var5++;
            }
         }
      }

      return var5 > 0 && var4 > var5 * 0.42F;
   }

   private BlockPos a(BlockHitResult currentHit) {
      if (this.field_c_2 != null && mc.world.getBlockState(this.field_c_2).isAir()) {
         mc.interactionManager.cancelBlockBreaking();
         this.field_c_2 = null;
      }

      if (this.field_c_2 != null) {
         return this.field_c_2;
      } else {
         int[] var2 = a[this.ap];
         BlockPos var3 = mc.player.getBlockPos();
         int var4 = this.au;
         if (this.e.d("3x3 (Center Only)")) {
            var4++;
         }

         BlockPos var5 = new BlockPos(var3.getX() + var2[0], var3.getY() + var4, var3.getZ() + var2[1]);
         if (!mc.world.getBlockState(var5).isAir()) {
            this.field_c_2 = var5.toImmutable();
            mc.interactionManager.cancelBlockBreaking();
            return this.field_c_2;
         } else if (currentHit != null && !mc.world.getBlockState(currentHit.getBlockPos()).isAir()) {
            this.field_c_2 = currentHit.getBlockPos().toImmutable();
            mc.interactionManager.cancelBlockBreaking();
            return this.field_c_2;
         } else {
            return null;
         }
      }
   }

   private void b(BlockPos target) {
      Vec3d var2 = mc.player.getEyePos();
      BlockPos var12 = Vec3d.ofCenter(target);
      double var4 = var12.x - var2.x;
      double var6 = var12.y - var2.y;
      double var8 = var12.z - var2.z;
      double var10 = Math.sqrt(var4 * var4 + var8 * var8);
      BlockPos var13 = MathHelper.wrapDegrees((float)(Math.toDegrees(Math.atan2(var8, var4)) - 90.0));
      float var16 = MathHelper.clamp((float)(-Math.toDegrees(Math.atan2(var6, var10))), -75.0F, 75.0F);
      BlockPos var14 = MathHelper.wrapDegrees(var13 - mc.player.getYaw());
      float var17 = var16 - mc.player.getPitch();
      float var3 = Math.min(Math.abs(var14), this.aY.getValue() * (0.62F + this.field_c_1.nextFloat() * 0.18F));
      float var19 = Math.min(Math.abs(var17), Math.max(0.35F, this.aY.getValue() * 0.42F));
      BlockPos var15 = mc.player.getYaw() + Math.copySign(var3, var14);
      float var18 = mc.player.getPitch() + Math.copySign(var19, var17);
      mc.player.setYaw(var15);
      mc.player.setPitch(var18);
      mc.player.setHeadYaw(var15);
      mc.player.setBodyYaw(var15);
   }

   private void as() {
      ChunkPos var1 = mc.player.getChunkPos();
      int var2 = Math.min(mc.options.getClampedViewDistance(), 12);

      for (int var3 = var1.x - var2; var3 <= var1.x + var2; var3++) {
         for (int var4 = var1.z - var2; var4 <= var1.z + var2; var4++) {
            ChunkPos var5 = new ChunkPos(var3, var4);
            if (this.j.add(var5)) {
               WorldChunk var6 = mc.world.getChunkManager().getWorldChunk(var3, var4, false);
               if (var6 != null) {
                  int var7 = 0;

                  for (BlockEntity var8 : var6.getBlockEntities().values()) {
                     if (this.a(var8)) {
                        var7++;
                     }
                  }

                  if (var7 >= this.aV.getValue()) {
                     this.ah = true;
                     this.at();
                     String var10 = "Found " + var7 + " storages near X: " + var5.getCenterX() + " Z: " + var5.getCenterZ();
                     this.t(var10);
                     if (mc.player != null) {
                        mc.player.sendMessage(Text.literal("§8§7[Tunnel Base Finder] " + var10), false);
                     }

                     return;
                  }
               }
            }
         }
      }
   }

   private boolean x() {
      if (this.c(mc.player.getMainHandStack().getItem())) {
         return true;
      } else {
         for (int var1 = 0; var1 < 9; var1++) {
            ItemStack var2 = mc.player.getInventory().getStack(var1);
            if (!var2.isEmpty() && this.c(var2.getItem())) {
               mc.player.getInventory().setSelectedSlot(var1);
               return true;
            }
         }

         return false;
      }
   }

   private boolean c(Item item) {
      return Registries.ITEM.getId(item).getPath().endsWith("_pickaxe");
   }

   private boolean a(BlockEntity be) {
      if (be == null) {
         return false;
      } else {
         BlockEntity var2 = be.getCachedState();
         return var2.isOf(Blocks.CHEST)
            || var2.isOf(Blocks.TRAPPED_CHEST)
            || var2.isOf(Blocks.BARREL)
            || var2.isOf(Blocks.ENDER_CHEST)
            || var2.isOf(Blocks.SHULKER_BOX)
            || var2.isOf(Blocks.WHITE_SHULKER_BOX)
            || var2.isOf(Blocks.ORANGE_SHULKER_BOX)
            || var2.isOf(Blocks.MAGENTA_SHULKER_BOX)
            || var2.isOf(Blocks.LIGHT_BLUE_SHULKER_BOX)
            || var2.isOf(Blocks.YELLOW_SHULKER_BOX)
            || var2.isOf(Blocks.LIME_SHULKER_BOX)
            || var2.isOf(Blocks.PINK_SHULKER_BOX)
            || var2.isOf(Blocks.GRAY_SHULKER_BOX)
            || var2.isOf(Blocks.LIGHT_GRAY_SHULKER_BOX)
            || var2.isOf(Blocks.CYAN_SHULKER_BOX)
            || var2.isOf(Blocks.PURPLE_SHULKER_BOX)
            || var2.isOf(Blocks.BLUE_SHULKER_BOX)
            || var2.isOf(Blocks.BROWN_SHULKER_BOX)
            || var2.isOf(Blocks.GREEN_SHULKER_BOX)
            || var2.isOf(Blocks.RED_SHULKER_BOX)
            || var2.isOf(Blocks.BLACK_SHULKER_BOX);
      }
   }

   private void t(String message) {
      try {
         ToastManager.INSTANCE.push("Tunnel Base Finder", message, Items.DIAMOND_PICKAXE.getDefaultStack(), WaterPlus.getAccentARGB());
      } catch (Throwable var2) {
      }
   }

   private void at() {
      if (mc.options != null) {
         mc.options.forwardKey.setPressed(false);
         mc.options.leftKey.setPressed(false);
         mc.options.rightKey.setPressed(false);
         mc.options.sprintKey.setPressed(false);
         mc.options.attackKey.setPressed(false);
      }

      if (mc.interactionManager != null) {
         mc.interactionManager.cancelBlockBreaking();
      }

      this.at = 0;
      this.field_c_2 = null;
   }
}
