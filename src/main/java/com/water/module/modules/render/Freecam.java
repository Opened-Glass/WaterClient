package com.water.module.modules.render;

import com.water.module.Category;
import com.water.module.Module;
import com.water.setting.Setting;
import net.minecraft.client.option.Perspective;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3d;

public class Freecam extends Module {
   private static final float MIN_SCROLL_SPEED = 0.1F;
   private static final float MAX_SCROLL_SPEED = 10.0F;
   private static final float SCROLL_SPEED_STEP = 0.2F;
   public final Vector3d currentPosition = new Vector3d();
   public final Vector3d previousPosition = new Vector3d();
   private final Vector3d velocity = new Vector3d();
   public float yaw;
   public float pitch;
   public float previousYaw;
   public float previousPitch;
   public final Setting<Float> speed = new Setting<>("Speed", 1.0F, 0.1F, 10.0F);
   public final Setting<Boolean> smoothing = new Setting<>("Smooth", Boolean.TRUE);
   public final Setting<Boolean> keepSneak = new Setting<>("Keep Sneak", Boolean.FALSE);
   public final Setting<Boolean> keepMovement = new Setting<>("Keep Movement", Boolean.FALSE);
   public final Setting<Boolean> tracerStickToEye = new Setting<>("Stick to Eye", Boolean.FALSE);
   private float lookSensitivity = 0.5F;
   private float currentSpeed;
   private Perspective savedPerspective;
   private boolean savedChunkCullingEnabled;
   private long lastFrameTime;
   private float savedPlayerYaw;
   private float savedPlayerPitch;
   public boolean wasSneaking;
   private boolean heldForward;
   private boolean heldBackward;
   private boolean heldLeft;
   private boolean heldRight;
   private boolean heldJump;
   private boolean heldSneak;
   private boolean heldSprint;
   public static Freecam instance;

   public Freecam() {
      super("Freecam", Category.b);
      instance = this;
      this.addSetting(this.speed);
      this.addSetting(this.smoothing);
      this.addSetting(this.keepSneak);
      this.addSetting(this.keepMovement);
      this.addSetting(this.tracerStickToEye);
   }

   @Override
   public void onEnable() {
      if (mc.player != null && mc.world != null) {
         this.savedPerspective = mc.options.getPerspective();
         this.savedChunkCullingEnabled = mc.chunkCullingEnabled;
         mc.chunkCullingEnabled = false;
         this.savedPlayerYaw = mc.player.getYaw();
         this.savedPlayerPitch = mc.player.getPitch();
         this.wasSneaking = mc.player.isSneaking();
         this.captureHeldMovement();
         this.yaw = mc.player.getYaw();
         this.pitch = mc.player.getPitch();
         Vec3d var1 = mc.player.getCameraPosVec(1.0F);
         this.currentPosition.set(var1.x, var1.y, var1.z);
         this.previousPosition.set(var1.x, var1.y, var1.z);
         this.previousYaw = this.yaw;
         this.previousPitch = this.pitch;
         this.lastFrameTime = System.currentTimeMillis();
         this.velocity.set(0.0, 0.0, 0.0);
         this.currentSpeed = this.getConfiguredSpeed();
      } else {
         this.toggle();
      }
   }

   @Override
   public void onDisable() {
      if (mc.player != null) {
         mc.player.setYaw(this.savedPlayerYaw);
         mc.player.setPitch(this.savedPlayerPitch);
         mc.player.setHeadYaw(this.savedPlayerYaw);
         mc.player.setBodyYaw(this.savedPlayerYaw);
         mc.player.setYaw(this.savedPlayerYaw);
         mc.player.setHeadYaw(this.savedPlayerYaw);
         mc.player.setBodyYaw(this.savedPlayerYaw);
         mc.player.setSneaking(this.wasSneaking);
      }

      if (this.savedPerspective != null) {
         mc.options.setPerspective(this.savedPerspective);
      } else {
         mc.options.setPerspective(Perspective.FIRST_PERSON);
      }

      mc.chunkCullingEnabled = this.savedChunkCullingEnabled;
      this.velocity.set(0.0, 0.0, 0.0);
      this.currentSpeed = this.getConfiguredSpeed();
      this.clearHeldMovement();
   }

   @Override
   public void onTick() {
      if (mc.player != null) {
         mc.player.setYaw(this.savedPlayerYaw);
         mc.player.setPitch(this.savedPlayerPitch);
         mc.player.setHeadYaw(this.savedPlayerYaw);
         mc.player.setBodyYaw(this.savedPlayerYaw);
         if (this.keepMovement.getValue()) {
            mc.player.setSneaking(this.heldSneak);
            mc.player.setSprinting(this.heldSprint);
         } else if (this.keepSneak.getValue() || this.wasSneaking) {
            mc.player.setSneaking(this.wasSneaking || this.keepSneak.getValue());
         }
      }
   }

   private void captureHeldMovement() {
      if (mc.options == null) {
         this.clearHeldMovement();
      } else {
         this.heldForward = mc.options.forwardKey.isPressed();
         this.heldBackward = mc.options.backKey.isPressed();
         this.heldLeft = mc.options.leftKey.isPressed();
         this.heldRight = mc.options.rightKey.isPressed();
         this.heldJump = mc.options.jumpKey.isPressed();
         this.heldSneak = mc.options.sneakKey.isPressed() || this.wasSneaking;
         this.heldSprint = mc.options.sprintKey.isPressed() || mc.player.isSprinting();
      }
   }

   private void clearHeldMovement() {
      this.heldForward = false;
      this.heldBackward = false;
      this.heldLeft = false;
      this.heldRight = false;
      this.heldJump = false;
      this.heldSneak = false;
      this.heldSprint = false;
   }

   public void updateCameraMovement() {
      if (mc.player != null) {
         this.previousPosition.set(this.currentPosition);
         this.previousYaw = this.yaw;
         this.previousPitch = this.pitch;
         long var1 = System.currentTimeMillis();
         float var3 = (float)(var1 - this.lastFrameTime) / 1000.0F;
         this.lastFrameTime = var1;
         var3 = Math.min(var3, 0.1F);
         if (var3 < 0.001F) {
            var3 = 0.016F;
         }

         float var25 = (float)Math.toRadians(this.yaw);
         double var5 = -Math.sin(var25);
         double var7 = Math.cos(var25);
         double var9 = -Math.cos(var25);
         double var11 = -Math.sin(var25);
         double var13 = 0.0;
         double var15 = 0.0;
         double var17 = 0.0;
         double var19 = this.currentSpeed * 2.0;
         if (mc.options != null && mc.options.sprintKey.isPressed()) {
            var19 *= 2.0;
         }

         if (mc.options.forwardKey.isPressed()) {
            var13 = 0.0 + var5 * var19;
            var17 = 0.0 + var7 * var19;
         }

         if (mc.options.backKey.isPressed()) {
            var13 -= var5 * var19;
            var17 -= var7 * var19;
         }

         if (mc.options.rightKey.isPressed()) {
            var13 += var9 * var19;
            var17 += var11 * var19;
         }

         if (mc.options.leftKey.isPressed()) {
            var13 -= var9 * var19;
            var17 -= var11 * var19;
         }

         if (mc.options.jumpKey.isPressed()) {
            var15 = 0.0 + var19;
         }

         if (mc.options.sneakKey.isPressed()) {
            var15 -= var19;
         }

         if (this.smoothing.getValue()) {
            double var23 = 1.0 - Math.pow(0.001, var3);
            this.velocity.x = MathHelper.lerp(var23, this.velocity.x, var13 * 5.0);
            this.velocity.y = MathHelper.lerp(var23, this.velocity.y, var15 * 5.0);
            this.velocity.z = MathHelper.lerp(var23, this.velocity.z, var17 * 5.0);
         } else {
            this.velocity.set(var13 * 5.0, var15 * 5.0, var17 * 5.0);
         }

         this.currentPosition.x = this.currentPosition.x + this.velocity.x * var3;
         this.currentPosition.y = this.currentPosition.y + this.velocity.y * var3;
         this.currentPosition.z = this.currentPosition.z + this.velocity.z * var3;
      }
   }

   public void onScrollWheel(double scrollDelta) {
      float var3 = this.currentSpeed;
      double var4 = var3 + (float)scrollDelta * 0.5F;
      this.currentSpeed = MathHelper.clamp(var4, 0.1F, 10.0F);
   }

   public void updateRotation(double deltaYaw, double deltaPitch) {
      this.yaw += (float)deltaYaw;
      this.pitch += (float)deltaPitch;
      this.yaw = MathHelper.wrapDegrees(this.yaw);
      this.pitch = MathHelper.clamp(this.pitch, -90.0F, 90.0F);
   }

   public double getInterpolatedX(float partialTicks) {
      return MathHelper.lerp((double)partialTicks, this.previousPosition.x, this.currentPosition.x);
   }

   public double getInterpolatedY(float partialTicks) {
      return MathHelper.lerp((double)partialTicks, this.previousPosition.y, this.currentPosition.y);
   }

   public double getInterpolatedZ(float partialTicks) {
      return MathHelper.lerp((double)partialTicks, this.previousPosition.z, this.currentPosition.z);
   }

   public float getInterpolatedYaw(float partialTicks) {
      return MathHelper.lerp(partialTicks, this.previousYaw, this.yaw);
   }

   public float getInterpolatedPitch(float partialTicks) {
      return MathHelper.lerp(partialTicks, this.previousPitch, this.pitch);
   }

   public float getLookSensitivity() {
      return this.lookSensitivity;
   }

   public void adjustSpeed(double scrollAmount) {
      if (scrollAmount != 0.0) {
         double var3 = this.currentSpeed + (float)Math.signum(scrollAmount) * 0.2F;
         this.currentSpeed = MathHelper.clamp(var3, 0.1F, 10.0F);
      }
   }

   private float getConfiguredSpeed() {
      return MathHelper.clamp(this.speed.getValue(), 0.1F, 10.0F);
   }

   public boolean shouldKeepMovement() {
      return this.keepMovement.getValue();
   }

   public PlayerInput getHeldMovementInput() {
      return new PlayerInput(this.heldForward, this.heldBackward, this.heldLeft, this.heldRight, this.heldJump, this.heldSneak, this.heldSprint);
   }

   public Vec2f getHeldMovementVector() {
      float var1 = 0.0F;
      float var2 = 0.0F;
      if (this.heldForward) {
         var2 = 1.0F;
      }

      if (this.heldBackward) {
         var2--;
      }

      if (this.heldLeft) {
         var1 = 1.0F;
      }

      if (this.heldRight) {
         var1--;
      }

      if (var1 != 0.0F && var2 != 0.0F) {
         float var3 = (float)(1.0 / Math.sqrt(var1 * var1 + var2 * var2));
         var1 *= var3;
         var2 *= var3;
      }

      return new Vec2f(var1, var2);
   }

   public boolean shouldRenderSneaking() {
      return this.keepMovement.getValue() ? this.heldSneak : this.keepSneak.getValue() || this.wasSneaking;
   }

   public boolean shouldTracersStickToEye() {
      return this.tracerStickToEye.getValue();
   }

   public static Vec3d resolveTracerOrigin(Vec3d fallbackCameraPos, float tickDelta) {
      return instance != null && instance.isEnabled() && instance.shouldTracersStickToEye() && mc.player != null
         ? mc.player.getCameraPosVec(tickDelta)
         : fallbackCameraPos;
   }
}
