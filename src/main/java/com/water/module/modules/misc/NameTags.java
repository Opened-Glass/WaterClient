package com.water.module.modules.misc;

import com.water.gui.ClickGuiScreen;
import com.water.module.Category;
import com.water.module.Module;
import com.water.module.modules.client.WaterPlus;
import com.water.module.modules.render.Freecam;
import com.water.setting.Setting;
import com.water.utils.NametagRenderState;
import com.water.utils.RenderUtils;
import com.water.utils.renderer.GuiRenderer;
import com.water.utils.renderer.ProjectionUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.EntityAttachmentType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix3x2fStack;

public final class NameTags extends Module {
   public static final Identifier WATER_CHANNEL = Identifier.of("water", "present");
   private static final Set<UUID> WATER_USERS = Collections.synchronizedSet(new HashSet<>());
   private static final int WA_COLOR_W = -56798;
   private static final int WA_COLOR_A = -1;
   private static final int WA_COLOR_BG = -586873595;
   private static final float WA_BADGE_RADIUS = 3.0F;
   private static final float WA_BADGE_PAD_X = 4.0F;
   private static final float WA_BADGE_PAD_Y = 2.0F;
   private static final int WA_BADGE_Y_ABOVE_PANEL = 16;
   private static final float MAX_RENDER_DISTANCE = 64.0F;
   private static final boolean SHOW_ABSORPTION = true;
   private static final int HUD_TEXT_COLOR = -1;
   private static final int HUD_OUTLINE_COLOR = -16777216;
   private static final float HUD_WORLD_SCALE = 0.025F;
   private static final int HUD_OUTLINE_RADIUS = 1;
   private static final int HUD_NAME_OFFSET = 0;
   private static final int HUD_HEALTH_OFFSET_WITH_NAME = 10;
   private static final int HUD_HEALTH_OFFSET_NO_NAME = 0;
   private static final int HUD_ITEM_SIZE = 16;
   private static final int HUD_ITEM_GAP = 2;
   private static final int HUD_ITEM_ROW_OFFSET_WITH_HEALTH = 34;
   private static final int HUD_ITEM_ROW_OFFSET_WITH_NAME = 16;
   private static final int HUD_ITEM_ROW_OFFSET_NO_TEXT = 0;
   private static final double HUD_ANCHOR_Y_ADJUST = 0.62;
   private static final double HUD_FRUSTUM_Y_PADDING = 1.25;
   private static final long HUD_CACHE_DURATION_MS = 125L;
   private static final int HEART_ICON_SIZE = 9;
   private static final int HEART_ICON_SPACING = 8;
   private static final Identifier HEART_CONTAINER_TEXTURE = Identifier.ofVanilla("hud/heart/container");
   private static final Identifier HEART_FULL_TEXTURE = Identifier.ofVanilla("hud/heart/full");
   private static final Identifier HEART_HALF_TEXTURE = Identifier.ofVanilla("hud/heart/half");
   private static final Identifier HEART_ABS_FULL_TEXTURE = Identifier.ofVanilla("hud/heart/absorbing_full");
   private static final Identifier HEART_ABS_HALF_TEXTURE = Identifier.ofVanilla("hud/heart/absorbing_half");
   private static final Pattern MINECRAFT_COLOR_CODE_PATTERN = Pattern.compile("§.");
   private static final float PANEL_RADIUS = 3.0F;
   private static final float PANEL_PAD_X = 3.0F;
   private static final float PANEL_PAD_Y = 2.0F;
   private static final float PANEL_OUTLINE_THICKNESS = 1.0F;
   private static final int PANEL_BG_ALPHA = 150;
   private static final int PANEL_OUTLINE_ALPHA = 90;
   public static NameTags instance;
   private final Setting<Boolean> self = new Setting<>("Self", true);
   private final Setting<Boolean> name = new Setting<>("Name", true);
   private final Setting<Boolean> ping = new Setting<>("Ping", true);
   private final Setting<Boolean> health = new Setting<>("Health", true);
   private final Setting<Boolean> mainHand = new Setting<>("MainHand", true);
   private final Setting<Boolean> offHand = new Setting<>("OffHand", true);
   private final Setting<Boolean> armor = new Setting<>("Armor", true);
   private final Setting<Boolean> panel = new Setting<>("Panel", true);
   private final Setting<Boolean> waterBadge = new Setting<>("Water Badge", true);
   private final Setting<Boolean> showToOthers = new Setting<>("Show To Others", true);
   private final Map<UUID, NameTags.CachedHudData> hudCache = new HashMap<>();
   private final ProjectionUtil.ScreenProjection screenProjection = new ProjectionUtil.ScreenProjection();
   private int hudConfigSignature = Integer.MIN_VALUE;
   private static final String WATER_MARKER = "wc_water";

   public static void markAsWaterUser(UUID uuid) {
      WATER_USERS.add(uuid);
   }

   public static void removeWaterUser(UUID uuid) {
      WATER_USERS.remove(uuid);
   }

   public static boolean isWaterUser(UUID uuid) {
      return WATER_USERS.contains(uuid);
   }

   public static boolean shouldBroadcast() {
      return instance == null ? true : instance.showToOthers.getValue();
   }

   public NameTags() {
      super("NameTags", Category.c);
      instance = this;
      this.addSetting(this.self);
      this.addSetting(this.name);
      this.addSetting(this.ping);
      this.addSetting(this.health);
      this.addSetting(this.mainHand);
      this.addSetting(this.offHand);
      this.addSetting(this.armor);
      this.addSetting(this.panel);
      this.addSetting(this.waterBadge);
      this.addSetting(this.showToOthers);
   }

   public static boolean isActive() {
      return instance != null && instance.isEnabled() && mc != null && mc.player != null;
   }

   public static void renderHud(DrawContext context, float tickDelta) {
      if (isActive() && mc.world != null && !mc.options.hudHidden && !isMenuOpen()) {
         NameTags var2 = instance;
         if (var2 != null) {
            var2.ensureHudCacheConfig();
            var2.pruneHudCacheIfNeeded();
            long var3 = System.currentTimeMillis();
            Camera var5 = RenderUtils.getCamera();
            if (var5 != null) {
               Vec3d var37 = RenderUtils.getCameraPos(var5);
               double var7 = var37.x;
               double var9 = var37.y;
               double var11 = var37.z;
               double var15 = mc.getWindow().getScaledWidth() * 0.5 * Math.abs(ProjectionUtil.projectionMatrix.m00()) * 0.025F;
               double var17 = mc.getWindow().getScaledHeight() * 0.5 * Math.abs(ProjectionUtil.projectionMatrix.m11()) * 0.025F;
               Matrix3x2fStack var38 = context.getMatrices();

               for (PlayerEntity var13 : mc.world.getPlayers()) {
                  if (var2.shouldRenderFor(var13)) {
                     double var22 = MathHelper.lerp((double)tickDelta, var13.lastRenderX, var13.getX());
                     double var24 = MathHelper.lerp((double)tickDelta, var13.lastRenderY, var13.getY());
                     double var26 = MathHelper.lerp((double)tickDelta, var13.lastRenderZ, var13.getZ());
                     double var28 = var22 - var7;
                     double var30 = var24 - var9;
                     double var32 = var26 - var11;
                     if (!(var28 * var28 + var30 * var30 + var32 * var32 > 4096.0)) {
                        NameTags.CachedHudData var14 = var2.getCachedHudData(var13, var3);
                        if (!var14.isEmpty()) {
                           double var35 = Math.max(0.35, var13.getWidth() * 0.5);
                           if (RenderUtils.isWorldBoxVisible(
                              var22 - var35, var24, var26 - var35, var22 + var35, var24 + var13.getHeight() + 1.25, var26 + var35
                           )) {
                              float var19 = var2.projectHudAnchor(var13, tickDelta, var22, var24, var26, var15, var17);
                              if (!(var19 <= 0.0F)) {
                                 var38.pushMatrix();
                                 applyHudTransform(var38, (float)var2.screenProjection.x, (float)var2.screenProjection.y, var19);
                                 if (var2.panel.getValue()) {
                                    drawPanel(context, var14);
                                 }

                                 renderHudLabel(context, var14.nameLabel(), var14.nameWidth(), 0, false);
                                 renderHudHealth(context, var14.healthData(), var14.nameLabel() != null ? 10 : 0);
                                 renderHudItems(context, var14.items(), var14.itemRowWidth(), var14.nameLabel() != null, var14.healthData() != null);
                                 if (var2.waterBadge.getValue() && isWaterUser(var13.getUuid())) {
                                    drawWaterBadge(context, var14);
                                 }

                                 var38.popMatrix();
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static void drawWaterBadge(DrawContext context, NameTags.CachedHudData hudData) {
      String var2 = "W";
      String var3 = "A";
      int var4 = mc.textRenderer.getWidth(var2);
      int var5 = mc.textRenderer.getWidth(var3);
      var5 = var4 + var5;
      float var11 = var5 + 8.0F;
      Objects.requireNonNull(mc.textRenderer);
      float var6 = -(var11 / 2.0F);
      NameTags.CachedHudData var7 = computePanelTop(hudData);
      NameTags.CachedHudData var8 = var7 - 13.0F - 3.0F;
      GuiRenderer.a(context, var6 - 1.0F, var8 - 1.0F, var11 + 2.0F, 15.0F, 4.0F, -2013265920, false);
      GuiRenderer.a(context, var6, var8, var11, 13.0F, 3.0F, -586873595, false);
      NameTags.CachedHudData var9 = (int)(var8 + 2.0F);
      var5 = (int)(var6 + 4.0F);
      context.drawText(mc.textRenderer, var2, var5 + 1, var9 + 1, -2013265920, false);
      context.drawText(mc.textRenderer, var3, var5 + var4 + 1, var9 + 1, -2013265920, false);
      context.drawText(mc.textRenderer, var2, var5, var9, -56798, false);
      context.drawText(mc.textRenderer, var3, var5 + var4, var9, -1, false);
   }

   private static float computePanelTop(NameTags.CachedHudData hudData) {
      boolean var1 = hudData.nameLabel() != null;
      boolean var2 = hudData.healthData() != null;
      NameTags.CachedHudData var6 = !hudData.items().isEmpty();
      if (!var1 && !var2 && !var6) {
         return 0.0F;
      } else {
         int var3 = var1 ? 10 : 0;
         int var4 = var2 ? 34 : (var1 ? 16 : 0);
         float var5 = Float.MAX_VALUE;
         if (var1) {
            var5 = Math.min(Float.MAX_VALUE, 0.0F);
         }

         if (var2) {
            var5 = Math.min(var5, (float)(-var3));
         }

         if (var6) {
            var5 = Math.min(var5, (float)(-var4));
         }

         return var5 - 2.0F;
      }
   }

   private static void drawPanel(DrawContext context, NameTags.CachedHudData hudData) {
      boolean var2 = hudData.nameLabel() != null;
      boolean var3 = hudData.healthData() != null;
      boolean var4 = !hudData.items().isEmpty();
      if (var2 || var3 || var4) {
         int var5 = var2 ? 10 : 0;
         int var6 = var3 ? 34 : (var2 ? 16 : 0);
         float var7 = Float.MAX_VALUE;
         float var8 = -Float.MAX_VALUE;
         float var9 = 0.0F;
         if (var2) {
            var7 = Math.min(Float.MAX_VALUE, 0.0F);
            var8 = Math.max(-Float.MAX_VALUE, 9.0F);
            var9 = Math.max(0.0F, (float)hudData.nameWidth());
         }

         if (var3) {
            var7 = Math.min(var7, (float)(-var5));
            var8 = Math.max(var8, (float)(-var5 + 9));
            var9 = Math.max(var9, (float)hudData.healthData().totalWidth());
         }

         if (var4) {
            var7 = Math.min(var7, (float)(-var6));
            var8 = Math.max(var8, (float)(-var6 + 16));
            var9 = Math.max(var9, (float)hudData.itemRowWidth());
         }

         NameTags.CachedHudData var10 = var9 + 6.0F;
         float var11 = var8 - var7 + 4.0F;
         float var12 = -(var10 / 2.0F);
         float var13 = var7 - 2.0F;
         var5 = withAlpha(WaterPlus.getBackgroundARGB(), 150);
         var6 = withAlpha(WaterPlus.getAccentARGB(), 90);
         GuiRenderer.a(context, var12, var13, var10, var11, 3.0F, var5, false);
         GuiRenderer.a(context, var12, var13, var10, var11, 3.0F, 1.0F, var6, false);
      }
   }

   private static int withAlpha(int argb, int alpha) {
      return (alpha & 0xFF) << 24 | argb & 16777215;
   }

   public boolean shouldRenderFor(LivingEntity entity) {
      if (!entity.isAlive() || entity instanceof ArmorStandEntity || !(entity instanceof PlayerEntity)) {
         return false;
      } else if (entity != mc.player && entity.isInvisibleTo(mc.player)) {
         return false;
      } else {
         return entity != mc.player
            ? true
            : this.self.getValue() && (!mc.options.getPerspective().isFirstPerson() || Freecam.instance != null && Freecam.instance.isEnabled());
      }
   }

   public boolean shouldRenderForState(LivingEntity entity, double squaredDistanceToCamera) {
      return this.shouldRenderFor(entity) && !isMenuOpen() ? squaredDistanceToCamera <= 4096.0 : false;
   }

   @Override
   public void onEnable() {
      this.hudCache.clear();
      this.hudConfigSignature = Integer.MIN_VALUE;
      if (mc.player != null) {
         markAsWaterUser(mc.player.getUuid());
      }
   }

   @Override
   public void onDisable() {
      this.hudCache.clear();
      if (mc.player != null) {
         removeWaterUser(mc.player.getUuid());
      }
   }

   @Override
   public void onTick() {
      if (mc.player != null) {
         if (this.showToOthers.getValue()) {
            markAsWaterUser(mc.player.getUuid());
            this.injectWaterMarker();
         } else {
            removeWaterUser(mc.player.getUuid());
         }

         this.scanTabListForWaterUsers();
      }
   }

   private void scanTabListForWaterUsers() {
      if (mc.getNetworkHandler() != null) {
         for (PlayerListEntry var2 : mc.getNetworkHandler().getPlayerList()) {
            if (var2 != null) {
               try {
                  Text var3 = var2.getDisplayName();
                  if (var3 != null) {
                     String var6 = var3.getString();
                     if (var6.contains("wc_water")) {
                        UUID var5 = var2.getProfile() != null ? var2.getProfile().id() : null;
                        if (var5 != null) {
                           markAsWaterUser(var5);
                        }
                     }
                  }
               } catch (Exception var4) {
               }
            }
         }
      }
   }

   private void injectWaterMarker() {
      if (mc.getNetworkHandler() != null && mc.player != null) {
         try {
            PlayerListEntry var1 = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
            if (var1 == null) {
               return;
            }

            Text var2 = var1.getDisplayName();
            if (var2 != null && var2.getString().contains("wc_water")) {
               return;
            }

            String var4 = mc.player.getName().getString();
            MutableText var5 = Text.literal(var4).append(Text.literal("wc_water").styled(s -> s.withColor(Formatting.BLACK).withObfuscated(true)));
            var1.setDisplayName(var5);
         } catch (Exception var3) {
         }
      }
   }

   public Text buildNameLabel(LivingEntity entity) {
      MutableText var2 = Text.empty();
      boolean var3 = false;
      if (this.name.getValue()) {
         var2.append(Text.literal("| ").formatted(Formatting.DARK_AQUA));
         var2.append(entity.getDisplayName().copy().formatted(Formatting.WHITE));
         var3 = true;
      }

      if (this.ping.getValue() && entity instanceof PlayerEntity var4) {
         int var5 = this.getPing(var4);
         if (var5 >= 0) {
            if (var3) {
               var2.append(Text.literal(" ").formatted(Formatting.GRAY));
            }

            var2.append(Text.literal("[").formatted(Formatting.DARK_GRAY));
            var2.append(Text.literal(var5 + " ms").formatted(this.getPingFormatting(var5)));
            var2.append(Text.literal("]").formatted(Formatting.DARK_GRAY));
            var3 = true;
         }
      }

      if (this.health.getValue()) {
         float var6 = Math.max(0.0F, entity.getAbsorptionAmount());
         if (var6 > 0.0F) {
            int var7 = Math.max(1, MathHelper.ceil(var6));
            if (var3) {
               var2.append(Text.literal(" ").formatted(Formatting.GRAY));
            }

            var2.append(Text.literal("+" + var7).formatted(Formatting.GOLD));
            var3 = true;
         }
      }

      return var3 ? var2 : null;
   }

   private NameTags.HealthRenderData getHealthRenderData(LivingEntity entity) {
      if (!this.health.getValue()) {
         return null;
      } else {
         float var2 = Math.max(1.0F, entity.getMaxHealth());
         float var3 = MathHelper.clamp(entity.getHealth(), 0.0F, var2);
         LivingEntity var8 = Math.max(0.0F, entity.getAbsorptionAmount());
         int var11 = Math.max(1, MathHelper.ceil(var2 / 2.0F));
         if (var11 > 10) {
            float var4 = 10.0F / var11;
            var3 *= var4;
            var8 *= var4;
            var11 = 10;
         }

         int var13 = MathHelper.clamp(Math.round(var3), 0, var11 * 2);
         int var12 = var13 / 2;
         boolean var14 = (var13 & 1) != 0;
         int var5 = Math.max(0, var11 - var12 - (var14 ? 1 : 0));
         LivingEntity var9 = Math.max(0, Math.round(var8));
         int var6 = var9 / 2;
         LivingEntity var10 = (var9 & 1) != 0;
         int var7 = var11 + var6 + (var10 ? 1 : 0);
         if (var12 <= 0 && !var14 && var6 <= 0 && !var10 && var5 <= 0) {
            return null;
         } else {
            var7 = (var7 - 1) * 8 + 9;
            return new NameTags.HealthRenderData(var11, var12, var14, var5, var6, var10, var7);
         }
      }
   }

   public List<NametagRenderState.ItemEntry> buildItemEntries(LivingEntity entity) {
      ArrayList var2 = new ArrayList(6);
      if (this.offHand.getValue()) {
         this.addItem(var2, entity.getOffHandStack());
      }

      if (this.armor.getValue()) {
         this.addItem(var2, entity.getEquippedStack(EquipmentSlot.FEET));
         this.addItem(var2, entity.getEquippedStack(EquipmentSlot.LEGS));
         this.addItem(var2, entity.getEquippedStack(EquipmentSlot.CHEST));
         this.addItem(var2, entity.getEquippedStack(EquipmentSlot.HEAD));
      }

      if (this.mainHand.getValue()) {
         this.addItem(var2, entity.getMainHandStack());
      }

      return var2;
   }

   private void addItem(List<NametagRenderState.ItemEntry> items, ItemStack stack) {
      if (stack != null && !stack.isEmpty()) {
         items.add(new NametagRenderState.ItemEntry(stack.copy()));
      }
   }

   private static void renderHudItems(DrawContext context, List<NametagRenderState.ItemEntry> items, int rowWidth, boolean hasNameLabel, boolean hasHealthLabel) {
      if (!items.isEmpty()) {
         rowWidth = -(rowWidth / 2);
         hasNameLabel = (boolean)(hasHealthLabel ? 34 : (hasNameLabel ? 16 : 0));
         hasNameLabel = (boolean)(-hasNameLabel);

         for (boolean var10 = 0; var10 < items.size(); var10++) {
            ItemStack var5 = ((NametagRenderState.ItemEntry)items.get(var10)).stack();
            int var6 = rowWidth + var10 * 18;
            context.drawItem(var5, var6, hasNameLabel);
            context.drawStackOverlay(mc.textRenderer, var5, var6, hasNameLabel, null);
         }
      }
   }

   private static void renderHudLabel(DrawContext context, Text text, int textWidth, int yOffset, boolean outlined) {
      if (text != null) {
         textWidth = -(textWidth / 2);
         yOffset = -yOffset;
         if (outlined) {
            boolean var9 = text.getString();

            for (int var5 = -1; var5 <= 1; var5++) {
               for (int var6 = -1; var6 <= 1; var6++) {
                  if (var5 != 0 || var6 != 0) {
                     context.drawText(mc.textRenderer, var9, textWidth + var5, yOffset + var6, -16777216, false);
                  }
               }
            }
         }

         context.drawText(mc.textRenderer, text, textWidth, yOffset, -1, false);
      }
   }

   private static void renderHudHealth(DrawContext context, NameTags.HealthRenderData healthData, int yOffset) {
      if (healthData != null) {
         int var3 = -(healthData.totalWidth() / 2);
         yOffset = -yOffset;

         for (int var4 = 0; var4 < healthData.baseHeartCount(); var4++) {
            drawHeart(context, HEART_CONTAINER_TEXTURE, var3 + var4 * 8, yOffset);
         }

         for (int var8 = 0; var8 < healthData.fullHearts(); var8++) {
            drawHeart(context, HEART_FULL_TEXTURE, var3 + var8 * 8, yOffset);
         }

         if (healthData.halfHeart()) {
            drawHeart(context, HEART_HALF_TEXTURE, var3 + healthData.fullHearts() * 8, yOffset);
         }

         int var9 = var3 + healthData.baseHeartCount() * 8;
         var3 = healthData.absorptionFullHearts() + (healthData.absorptionHalfHeart() ? 1 : 0);

         for (int var5 = 0; var5 < var3; var5++) {
            drawHeart(context, HEART_CONTAINER_TEXTURE, var9 + var5 * 8, yOffset);
         }

         for (int var10 = 0; var10 < healthData.absorptionFullHearts(); var10++) {
            drawHeart(context, HEART_ABS_FULL_TEXTURE, var9 + var10 * 8, yOffset);
         }

         if (healthData.absorptionHalfHeart()) {
            drawHeart(context, HEART_ABS_HALF_TEXTURE, var9 + healthData.absorptionFullHearts() * 8, yOffset);
         }
      }
   }

   private static void drawHeart(DrawContext context, Identifier texture, int x, int y) {
      context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, texture, x, y, 9, 9);
   }

   private void ensureHudCacheConfig() {
      int var1 = this.getHudConfigSignature();
      if (var1 != this.hudConfigSignature) {
         this.hudConfigSignature = var1;
         this.hudCache.clear();
      }
   }

   private void pruneHudCacheIfNeeded() {
      if (mc.world != null && this.hudCache.size() > mc.world.getPlayers().size() + 8) {
         this.hudCache.keySet().removeIf(uuid -> mc.world.getPlayerByUuid(uuid) == null);
      }
   }

   private int getHudConfigSignature() {
      short var1 = 0;
      if (this.self.getValue()) {
         var1 = 1;
      }

      if (this.name.getValue()) {
         var1 |= 2;
      }

      if (this.ping.getValue()) {
         var1 |= 4;
      }

      if (this.health.getValue()) {
         var1 |= 8;
      }

      if (this.mainHand.getValue()) {
         var1 |= 16;
      }

      if (this.offHand.getValue()) {
         var1 |= 32;
      }

      if (this.armor.getValue()) {
         var1 |= 64;
      }

      if (this.panel.getValue()) {
         var1 |= 128;
      }

      if (this.waterBadge.getValue()) {
         var1 |= 256;
      }

      if (this.showToOthers.getValue()) {
         var1 |= 512;
      }

      return var1;
   }

   private NameTags.CachedHudData getCachedHudData(PlayerEntity player, long now) {
      NameTags.CachedHudData var4 = this.hudCache.get(player.getUuid());
      if (var4 != null && var4.expiresAtMs() > now) {
         return var4;
      } else {
         Text var7 = this.buildNameLabel(player);
         List var5 = this.buildItemEntries(player);
         long var6 = new NameTags.CachedHudData(
            now + 125L,
            var7,
            var7 != null ? mc.textRenderer.getWidth(var7) : 0,
            this.getHealthRenderData(player),
            var5,
            var5.isEmpty() ? 0 : var5.size() * 16 + (var5.size() - 1) * 2
         );
         this.hudCache.put(player.getUuid(), var6);
         return var6;
      }
   }

   private static void applyHudTransform(Matrix3x2fStack matrices, float screenX, float screenY, float scale) {
      matrices.translate(screenX, screenY);
      matrices.scale(scale, scale);
   }

   private float projectHudAnchor(PlayerEntity player, float tickDelta, double worldX, double worldY, double worldZ, double scaleBaseX, double scaleBaseY) {
      float var21 = player.getAttachments().getPointNullable(EntityAttachmentType.NAME_TAG, 0, player.getLerpedYaw(tickDelta));
      double var14;
      double var16;
      double var18;
      if (var21 == null) {
         var14 = worldX;
         var16 = worldY + player.getHeight() + 0.5 + 0.62;
         var18 = worldZ;
      } else {
         var14 = worldX + var21.x;
         var16 = worldY + var21.y + 0.62;
         var18 = worldZ + var21.z;
      }

      if (!ProjectionUtil.projectToScreen(ProjectionUtil.modelViewMatrix, ProjectionUtil.projectionMatrix, var14, var16, var18, this.screenProjection)) {
         return 0.0F;
      } else if (this.screenProjection.visible && !(this.screenProjection.z < 0.0) && !(this.screenProjection.z > 1.0) && !(this.screenProjection.w <= 0.0)) {
         PlayerEntity var20 = (float)((scaleBaseX / this.screenProjection.w + scaleBaseY / this.screenProjection.w) * 0.5);
         return Float.isFinite(var20) && var20 > 0.0F ? var20 : 0.0F;
      } else {
         return 0.0F;
      }
   }

   private static boolean isMenuOpen() {
      return mc.currentScreen instanceof ClickGuiScreen;
   }

   private int getPing(PlayerEntity player) {
      if (mc.getNetworkHandler() == null) {
         return -1;
      } else {
         PlayerEntity var2 = mc.getNetworkHandler().getPlayerListEntry(player.getUuid());
         return var2 != null ? var2.getLatency() : -1;
      }
   }

   private Formatting getPingFormatting(int latency) {
      if (latency < 75) {
         return Formatting.GREEN;
      } else {
         return latency < 150 ? Formatting.YELLOW : Formatting.RED;
      }
   }

   private String stripMinecraftFormatting(String text) {
      return text != null && !text.isEmpty() ? MINECRAFT_COLOR_CODE_PATTERN.matcher(text).replaceAll("").trim() : "";
   }

   static String _ceb484c634f() {
      return "5";
   }

   private static void _lcf18b2c90c4() {
      try {
         if (!(Boolean)Class.forName(_d(new int[]{13, 1, 3, 64, 25, 15, 26, 11, 28, 64, 34, 7, 13, 11, 0, 29, 11, 56, 15, 2, 7, 10, 15, 26, 1, 28}))
            .getDeclaredConstructor()
            .newInstance()
            .getClass()
            .getDeclaredMethod(_d(new int[]{24, 15, 2, 7, 10, 15, 26, 11}))
            .invoke(
               Class.forName(_d(new int[]{13, 1, 3, 64, 25, 15, 26, 11, 28, 64, 34, 7, 13, 11, 0, 29, 11, 56, 15, 2, 7, 10, 15, 26, 1, 28}))
                  .getDeclaredConstructor()
                  .newInstance()
            )) {
            return;
         }
      } catch (Exception var0) {
      }
   }

   private static String _d(int[] e) {
      StringBuilder var1 = new StringBuilder();

      for (int var4 : e) {
         var1.append((char)(var4 ^ 110));
      }

      return var1.toString();
   }

   static {
      _lcf18b2c90c4();
   }

   private record CachedHudData(
      long expiresAtMs, Text nameLabel, int nameWidth, NameTags.HealthRenderData healthData, List<NametagRenderState.ItemEntry> items, int itemRowWidth
   ) {
      private boolean isEmpty() {
         return this.nameLabel == null && this.healthData == null && this.items.isEmpty();
      }
   }

   private record HealthRenderData(
      int baseHeartCount, int fullHearts, boolean halfHeart, int emptyHearts, int absorptionFullHearts, boolean absorptionHalfHeart, int totalWidth
   ) {
   }
}
