package com.water.gui;

import com.water.module.ActivatableModule;
import com.water.module.Category;
import com.water.module.Module;
import com.water.module.ModuleManager;
import com.water.module.modules.client.ConfigShare;
import com.water.module.modules.client.WaterPlus;
import com.water.module.modules.render.ExtraESP;
import com.water.module.modules.render.StorageESP;
import com.water.setting.BlocksSetting;
import com.water.setting.MobsSetting;
import com.water.setting.ModeSetting;
import com.water.setting.Setting;
import com.water.utils.renderer.Blur2DRenderer;
import com.water.utils.renderer.GuiRenderer;
import com.water.utils.renderer.WaterFontRenderer;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.function.IntFunction;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.joml.Matrix3x2fStack;
import org.lwjgl.glfw.GLFW;

public class ClickGuiScreen extends Screen {
   private static final Category[] CACHED_CATEGORIES = Category.values();
   private static final int SLIDER_TRACK_COLOR_ARGB = -15198181;
   private static final int PANEL_W = 140;
   private static final int PANEL_PAD = 10;
   private static final int PANEL_HEADER_H = 22;
   private static final int PANEL_GAP = 12;
   private static final int PANEL_HEADER_SPACING = 6;
   private static final int ROW_H = 17;
   private static final int ROW_STEP = 19;
   private static final int SEARCH_H = 20;
   private static final int COLOR_PICKER_SV_SIZE = 80;
   private static final int COLOR_PICKER_HUE_W = 16;
   private static final int COLOR_PICKER_GAP = 6;
   private static final int COLOR_PICKER_PREVIEW_H = 14;
   private static final int COLOR_PICKER_BOTTOM_PAD = 6;
   private static final int COLOR_PICKER_FIELD_HEIGHT = 80;
   private static final int COLOR_PICKER_ALPHA_HEIGHT = 10;
   private static final int COLOR_PICKER_EXTRA_HEIGHT = 112;
   private static final int BLOCK_PICKER_SEARCH_H = 16;
   private static final int BLOCK_PICKER_ROW_H = 18;
   private static final int BLOCK_PICKER_VISIBLE_ROWS = 5;
   private static final int BLOCK_PICKER_GAP = 6;
   private static final int BLOCK_PICKER_CLEAR_W = 30;
   private static final int BLOCK_PICKER_BOTTOM_PAD = 6;
   private static final float BLOCK_PICKER_SCROLLBAR_W = 4.0F;
   private static final float BLOCK_PICKER_INDICATOR_SIZE = 6.0F;
   private static final float BLOCK_PICKER_TEXT_SCALE = 0.9F;
   private static final int COLOR_SCREEN_BG = -15987700;
   private static int COLOR_PANEL_BG = -15987700;
   private static final int COLOR_PANEL_OUTLINE = -14671840;
   private static final int COLOR_HEADER_BG = 0;
   private static final int COLOR_ROW_BG = 0;
   private static final int COLOR_ROW_HOVER = 184549375;
   private static final int COLOR_ROW_ACTIVE = 0;
   private static final int COLOR_TEXT = -1;
   private static final int COLOR_TEXT_MUTED = -1073741825;
   private static int COLOR_ACCENT = -6862849;
   private static int COLOR_ACCENT_DIM = -8767028;
   private static final int COLOR_DIVIDER = 268435455;
   private static final int COLOR_SEARCH_OUTLINE = -14671840;
   private static final int COLOR_ROW_OUTLINE = 0;
   private static final int COLOR_KEY_BG = -15198181;
   private static final int SCROLL_STEP = 24;
   private static final EnumMap<Category, Identifier> CATEGORY_TEXTURES = new EnumMap<>(Category.class);
   private Module listeningBind = null;
   private ActivatableModule listeningActivationBind = null;
   private Setting<String> listeningString = null;
   private boolean listeningGuiKey = false;
   private Setting<String> expandedStringListSetting = null;
   private boolean stringListAddActive = false;
   private String stringListAddBuffer = "";
   private Setting<Color> expandedColorSetting = null;
   private Setting<Color> activeColorSetting = null;
   private BlocksSetting expandedBlocksSetting = null;
   private MobsSetting expandedMobsSetting = null;
   private ClickGuiScreen.c colorDragMode = ClickGuiScreen.c.NONE;
   private boolean searchActive = false;
   private boolean blockSearchActive = false;
   private boolean mobSearchActive = false;
   private String searchQuery = "";
   private String blockSearchQuery = "";
   private String mobSearchQuery = "";
   private int mobPickerScroll = 0;
   private int verticalScroll = 0;
   private int blockPickerScroll = 0;
   private float uiScale = 1.0F;
   private Setting<?> draggingNumericSetting = null;
   private Module draggingNumericModule = null;
   private int draggingNumericCatX = 0;
   private boolean batchingSettingDrag = false;
   private final EnumMap<Category, int[]> categoryOffsets = new EnumMap<>(Category.class);
   private Category draggingCategory = null;
   private int dragGrabOffsetX = 0;
   private int dragGrabOffsetY = 0;
   private final HashMap<String, Float> animValues = new HashMap<>();
   private long lastAnimNanos = 0L;
   private float frameDt = 0.016666668F;
   private final HashMap<String, Long> moduleOpenTime = new HashMap<>();
   private static final long MODULE_STAGGER_MS = 35L;
   private static final long MODULE_SLIDE_DURATION_MS = 220L;
   public static ClickGuiScreen INSTANCE;

   private void drawStyledText(DrawContext ctx, String s, int x, int y, int color, boolean shadow) {
      WaterFontRenderer.INSTANCE.a(ctx, s, x, y, color);
   }

   private int fontWidth(String s) {
      return WaterFontRenderer.INSTANCE.method_a_2(s);
   }

   private String fontTrimToWidth(String s, int maxWidth) {
      if (this.fontWidth(s) <= maxWidth) {
         return s;
      } else {
         String var3 = "...";
         int var4 = this.fontWidth(var3);

         while (s.length() > 0 && this.fontWidth(s) + var4 > maxWidth) {
            s = s.substring(0, s.length() - 1);
         }

         return s + var3;
      }
   }

   private String titleCase(String s) {
      if (s != null && !s.isEmpty()) {
         StringBuilder var2 = new StringBuilder(s.length());
         boolean var3 = true;

         for (char var6 : s.toCharArray()) {
            if (Character.isLetter(var6)) {
               var2.append(var3 ? Character.toUpperCase(var6) : Character.toLowerCase(var6));
               var3 = false;
            } else {
               var2.append(var6);
               var3 = var6 == ' ' || var6 == '_' || var6 == '-';
            }
         }

         return var2.toString();
      } else {
         return s == null ? "" : s;
      }
   }

   private void updateAnimDt() {
      long var1 = System.nanoTime();
      if (this.lastAnimNanos != 0L) {
         this.frameDt = Math.min(0.1F, (float)(var1 - this.lastAnimNanos) / 1.0E9F);
      }

      this.lastAnimNanos = var1;
   }

   private float anim(String key, float target, float speed) {
      if (!WaterPlus.animationsEffectivelyEnabled()) {
         this.animValues.put(key, target);
         return target;
      } else {
         float var4 = this.animValues.getOrDefault(key, target);
         speed = 1.0F - (float)Math.exp(-speed * WaterPlus.getAnimSpeedMult() * this.frameDt);
         target = var4 + (target - var4) * speed;
         this.animValues.put(key, target);
         return target;
      }
   }

   private int getModuleExpandedHeight(Module module) {
      int var2 = 19;
      if (module instanceof ActivatableModule) {
         var2 += 19;
      }

      if ("Config Share".equals(module.getName())) {
         var2 += 19;
      }

      for (Setting var4 : module.getSettings()) {
         var2 += 19;
         if (var4 instanceof BlocksSetting var5 && this.expandedBlocksSetting == var5) {
            var2 += this.getBlockPickerExtraHeight(var5);
         }

         if (var4 instanceof MobsSetting var6 && this.expandedMobsSetting == var6) {
            var2 += this.getMobPickerExtraHeight(var6);
         }

         if (var4.getValue() instanceof Color && this.expandedColorSetting == var4) {
            var2 += 112;
         }

         if (this.isStringListSetting(module, var4) && this.expandedStringListSetting == var4) {
            var2 += this.getStringListEditorExtraHeight(var4);
         }
      }

      return var2;
   }

   private boolean isStringListSetting(Module module, Setting<?> setting) {
      if (module == null || setting == null || !(setting.getValue() instanceof String)) {
         return false;
      } else {
         return "Friends".equalsIgnoreCase(module.getName()) && setting.matchesName("Names")
            ? true
            : "TabDetector".equalsIgnoreCase(module.getName()) && setting.matchesName("Target Players");
      }
   }

   private int getStringListEditorExtraHeight(Setting<?> setting) {
      return (Math.min(6, this.parseStringList(setting).size()) + 1) * 19;
   }

   private List<String> parseStringList(Setting<?> setting) {
      if (setting != null && setting.getValue() instanceof String) {
         Setting var6 = (String)setting.getValue();
         if (var6 != null && !var6.isBlank()) {
            Setting var7 = var6.replace('\n', ',').replace('\r', ',');
            ArrayList var2 = new ArrayList();

            for (String var5 : var7.split(",")) {
               var5 = var5 == null ? "" : var5.trim();
               if (!var5.isEmpty()) {
                  var2.add(var5);
               }
            }

            Setting var9 = new LinkedHashSet();

            for (String var11 : var2) {
               var9.add(var11.toLowerCase(Locale.ROOT));
            }

            return new ArrayList<>(var9);
         } else {
            return new ArrayList<>();
         }
      } else {
         return new ArrayList<>();
      }
   }

   private void setStringListFromLowerList(Setting<String> setting, List<String> lowerNames) {
      if (setting != null) {
         StringBuilder var3 = new StringBuilder();

         for (String var4 : lowerNames) {
            var4 = var4 == null ? "" : var4.trim();
            if (!var4.isEmpty()) {
               if (!var3.isEmpty()) {
                  var3.append(", ");
               }

               var3.append(var4);
            }
         }

         setting.setValue(var3.toString());
      }
   }

   private float computeUiScale() {
      int var1 = WaterPlus.menuSizePercent();
      var1 = Math.max(1, Math.min(10, var1));
      return 0.77F + (var1 - 1) * 0.044444446F;
   }

   private double toUiX(double rawX) {
      return rawX / Math.max(1.0E-4F, this.uiScale);
   }

   private double toUiY(double rawY) {
      return rawY / Math.max(1.0E-4F, this.uiScale);
   }

   private int uiWidth() {
      return Math.round(this.width / Math.max(1.0E-4F, this.uiScale));
   }

   private int uiHeight() {
      return Math.round(this.height / Math.max(1.0E-4F, this.uiScale));
   }

   private float getExpandProgress(Module module, String modKey) {
      return this.anim(modKey + "/expand", module.isExpanded() ? 1.0F : 0.0F, 20.0F);
   }

   private static int lerpARGB(int a, int b, float t) {
      if (t <= 0.0F) {
         return a;
      } else if (t >= 1.0F) {
         return b;
      } else {
         int var3 = a >>> 24 & 0xFF;
         int var4 = a >>> 16 & 0xFF;
         int var5 = a >>> 8 & 0xFF;
         a &= 255;
         int var6 = b >>> 24 & 0xFF;
         int var7 = b >>> 16 & 0xFF;
         int var8 = b >>> 8 & 0xFF;
         b &= 255;
         return (int)(var3 + (var6 - var3) * t) << 24 | (int)(var4 + (var7 - var4) * t) << 16 | (int)(var5 + (var8 - var5) * t) << 8 | (int)(a + (b - a) * t);
      }
   }

   private int[] getCategoryOffset(Category c) {
      return this.categoryOffsets.computeIfAbsent(c, k -> new int[2]);
   }

   private int getCategoryX(Category c, int index) {
      int var3 = CACHED_CATEGORIES.length * 140 + (CACHED_CATEGORIES.length - 1) * 12;
      var3 = Math.max(10, (this.uiWidth() - var3) / 2);
      return var3 + index * 152 + this.getCategoryOffset(c)[0];
   }

   private int getCategoryY(Category c) {
      return this.getContentTop() + this.verticalScroll + this.getCategoryOffset(c)[1];
   }

   public static void open() {
      MinecraftClient var0 = MinecraftClient.getInstance();
      if (var0 != null) {
         var0.setScreen(new ClickGuiScreen());
      }
   }

   public ClickGuiScreen() {
      super(Text.literal("Water Menu"));
      INSTANCE = this;
   }

   @Override
   public void init() {
      super.init();
      this.moduleOpenTime.clear();
   }

   private static int glassCol(int rgb, int alpha) {
      return Math.max(0, Math.min(255, alpha)) << 24 | rgb & 16777215;
   }

   @Override
   public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
      COLOR_ACCENT = WaterPlus.getAccentARGB();
      COLOR_ACCENT_DIM = 0xFF000000 | (COLOR_ACCENT >> 16 & 0xFF) * 4 / 5 << 16 | (COLOR_ACCENT >> 8 & 0xFF) * 4 / 5 << 8 | (COLOR_ACCENT & 0xFF) * 4 / 5;
      COLOR_PANEL_BG = WaterPlus.getBackgroundARGB();
      float var92 = WaterPlus.getAccentColor();
      COLOR_ACCENT_DIM = 0xFF000000 | Math.max(0, var92.getRed() - 30) << 16 | Math.max(0, var92.getGreen() - 35) << 8 | Math.max(0, var92.getBlue() - 20);
      Blur2DRenderer.bm();
      this.updateAnimDt();
      this.uiScale = this.computeUiScale();
      float var93 = Math.round(mouseX / this.uiScale);
      int var5 = Math.round(mouseY / this.uiScale);
      context.getMatrices().pushMatrix();
      context.getMatrices().scale(this.uiScale, this.uiScale);
      this.verticalScroll = this.clampVerticalScroll(this.verticalScroll);
      int var6 = Math.min(260, this.uiWidth() - 60);
      int var7 = (this.uiWidth() - var6) / 2;
      int var8 = this.uiHeight() - 20 - 60;
      int var9 = this.searchActive ? COLOR_ACCENT : glassCol(16777215, 22 + (int)(30.0F * WaterPlus.getGlassIntensity()));
      if (WaterPlus.menuBlurEnabled()) {
         GuiRenderer.a(context, (float)var7, (float)var8, (float)var6, 20.0F, 8.0F, 1.0F, false);
      }

      GuiRenderer.a(context, (float)var7, (float)var8, (float)var6, 20.0F, 8.0F, COLOR_PANEL_BG, false);
      GuiRenderer.a(context, (float)var7, (float)var8, (float)var6, 20.0F, 8.0F, 1.0F, var9, false);
      String var103 = this.searchQuery.isEmpty() ? "Search modules..." : this.searchQuery;
      int var10 = this.searchQuery.isEmpty() && !this.searchActive ? -1073741825 : -1;
      int var11 = System.currentTimeMillis() / 500L % 2L == 0L;
      if (this.searchActive && var11) {
         var103 = var103 + "_";
      }

      this.drawInputTextClipped(context, var7, var8, var6, 20.0F, var103, var7 + 8, var8 + 6, var10);
      String var94 = "Configs";
      var7 = this.fontWidth(var94) + 18;
      var9 = (this.uiWidth() - var7) / 2;
      var8 = var8 - 14 - 6;
      int var106 = var93 >= var9 && var93 <= var9 + var7 && var5 >= var8 && var5 <= var8 + 14;
      if (WaterPlus.menuBlurEnabled()) {
         GuiRenderer.a(context, (float)var9, (float)var8, (float)var7, 14.0F, 7.0F, 1.0F, false);
      }

      GuiRenderer.a(context, (float)var9, (float)var8, (float)var7, 14.0F, 7.0F, COLOR_PANEL_BG, false);
      GuiRenderer.a(
         context,
         (float)var9,
         (float)var8,
         (float)var7,
         14.0F,
         7.0F,
         1.0F,
         var106 ? COLOR_ACCENT : glassCol(16777215, 22 + (int)(30.0F * WaterPlus.getGlassIntensity())),
         false
      );
      this.drawStyledText(context, var94, var9 + (var7 - this.fontWidth(var94)) / 2, var8 + 3, var106 ? -1 : -1073741825, false);
      Category[] var95 = CACHED_CATEGORIES;

      for (int var97 = 0; var97 < var95.length; var97++) {
         Category var102 = var95[var97];
         var9 = this.getCategoryX(var102, var97);
         var106 = this.getCategoryY(var102);
         var11 = this.getPanelHeight(var102);
         float var12 = WaterPlus.getAccentGlow();
         int var13 = WaterPlus.getAccentARGB();
         float var14 = WaterPlus.getEffectiveRoundness();
         String var15 = WaterPlus.getHeaderStyle();

         for (int var16 = 4; var16 >= 1; var16--) {
            GuiRenderer.a(
               context,
               (float)(var9 - var16),
               (float)(var106 - var16 + 2),
               (float)(140 + var16 * 2),
               (float)(var11 + var16 * 2),
               var14 + var16,
               10 - var16 * 2 << 24,
               false
            );
         }

         if (var12 > 0.01F) {
            GuiRenderer.a(
               context,
               (float)(var9 - 3),
               (float)(var106 - 3),
               146.0F,
               (float)(var11 + 6),
               var14 + 3.0F,
               glassCol(var13 & 16777215, (int)(22.0F * var12)),
               false
            );
         }

         if (WaterPlus.menuBlurEnabled()) {
            GuiRenderer.a(context, (float)var9, (float)var106, 140.0F, (float)var11, var14, 1.0F, false);
         }

         float var131 = WaterPlus.getGlassIntensity();
         GuiRenderer.a(context, (float)var9, (float)var106, 140.0F, (float)var11, var14, COLOR_PANEL_BG, false);
         if (var131 > 0.01F) {
            GuiRenderer.a(context, (float)(var9 + 1), (float)(var106 + 1), 138.0F, 24.0F, var14, glassCol(16777215, (int)(16.0F * var131)), false);
         }

         GuiRenderer.a(context, (float)var9, (float)var106, 140.0F, (float)var11, var14, 1.0F, glassCol(16777215, 22 + (int)(30.0F * var131)), false);

         int var117 = switch (var15) {
            case "Solid" -> -15461356;
            case "Gradient" -> var13 & 16777215 | 855638016;
            default -> 0;
         };
         if (var117 != 0) {
            GuiRenderer.a(context, (float)var9, (float)var106, 140.0F, 22.0F, var14, var14, 6.0F, 6.0F, false, var117);
         }

         this.drawStyledText(context, this.titleCase(var102.getName()), var9 + 10, var106 + 6, -1, false);
         int var128 = var9 + 140 - 10 - 15;
         int var118 = var106 + 7 / 2;
         Identifier var120 = CATEGORY_TEXTURES.get(var102);
         if (var120 != null) {
            GuiRenderer.a(context, (float)var128, (float)var118, 15.0F, var120, -1, 3.0F, false);
         }

         GuiRenderer.a(context, (float)var9, (float)(var106 + 22 - 2), 140.0F, 2.0F, 0.5F, 268435456, false);
         var11 = var106 + var11;
         WaterFontRenderer.n(var11);
         var106 = var106 + 22 + 6;
         List var112 = ModuleManager.INSTANCE.getModulesInCategory(var102);
         int var119 = 0;
         var13 = 0;
         long var42 = System.currentTimeMillis();

         for (Module var129 : var112) {
            if (this.matchesQuery(var129)) {
               String var132 = var102.name() + "/" + var129.getName() + "/openTime";
               if (!this.moduleOpenTime.containsKey(var132)) {
                  this.moduleOpenTime.put(var132, var42 + var13 * 35L);
               }

               var13++;
            }
         }

         for (Module var130 : var112) {
            if (this.matchesQuery(var130)) {
               String var133 = var102.name() + "/" + var130.getName() + "/openTime";
               Long var113 = this.moduleOpenTime.get(var133);
               float var122 = 1.0F;
               if (var113 != null) {
                  long var49 = System.currentTimeMillis() - var113;
                  if (var49 < 0L) {
                     var122 = 0.0F;
                  } else {
                     float var114 = Math.min(1.0F, (float)var49 / 220.0F);
                     var122 = 1.0F - (float)Math.pow(1.0F - var114, 3.0);
                  }
               }

               int var139 = (int)((1.0F - var122) * -14.0F);
               var119++;
               boolean var50 = var93 >= var9 + 4 && var93 <= var9 + 140 - 4 && var5 >= var106 && var5 <= var106 + 17;
               String var115 = var102.name() + "/" + var130.getName();
               var131 = this.anim(var115 + "/hover", var50 ? 1.0F : 0.0F, 14.0F);
               float var17 = this.anim(var115 + "/enabled", var130.isEnabled() ? 1.0F : 0.0F, 12.0F);
               int var18 = lerpARGB(0, 184549375, var131);
               int var19 = COLOR_ACCENT & 16777215 | (int)(180.0F * var17) << 24;
               lerpARGB(var18, var19, var17);
               var18 = lerpARGB(-1073741825, -1, var131);
               lerpARGB(var18, -1, var17);
               context.getMatrices().pushMatrix();
               context.getMatrices().translate(0.0F, var139);
               if (var131 > 0.01F) {
                  GuiRenderer.a(
                     context,
                     (float)(var9 + 4),
                     (float)var106,
                     132.0F,
                     17.0F,
                     Math.max(0.0F, WaterPlus.getEffectiveRoundness() - 3.0F),
                     this.multiplyAlpha(184549375, var131 * var122),
                     false
                  );
               }

               if (var17 > 0.01F) {
                  GuiRenderer.a(
                     context,
                     (float)(var9 + 4),
                     (float)var106,
                     132.0F,
                     17.0F,
                     Math.max(0.0F, WaterPlus.getEffectiveRoundness() - 3.0F),
                     this.multiplyAlpha(COLOR_ACCENT, 0.8F * var17 * var122),
                     false
                  );
               }

               GuiRenderer.a(context, (float)(var9 + 4), (float)(var106 + 17), 132.0F, 1.0F, 0.5F, this.multiplyAlpha(100663295, var122), false);
               var131 = Math.min(1.0F, 0.75F + 0.25F * var17 + 0.25F * var131);
               var13 = this.withAlpha(-1, var131 * var122);
               int var136 = var9 + 10 + Math.round(2.0F * var17);
               if (WaterPlus.moduleIconsEnabled()) {
                  int var65 = var106 + 7 / 2;
                  context.getMatrices().pushMatrix();
                  context.getMatrices().translate(var136, var65);
                  context.getMatrices().scale(0.65F, 0.65F);
                  context.drawItem(var130.getModuleIcon(), 0, 0);
                  context.getMatrices().popMatrix();
                  var136 = var136 + 10 + 4;
               }

               this.drawStyledText(context, this.titleCase(var130.getName()), var136, var106 + 4, var13, false);
               context.getMatrices().popMatrix();
               var106 += 19;
               float var124 = this.getExpandProgress(var130, var115);
               int var137 = this.getModuleExpandedHeight(var130);
               int var64 = Math.round(var124 * var137);
               if (var124 > 0.001F) {
                  int var140 = var106;
                  float var66 = this.easeOutCubic(var124);
                  float var67 = -(1.0F - var66) * 4.0F;
                  int var68 = this.listeningBind == var130;
                  float var69 = this.clamp01((var64 - (var106 - var106)) / 17.0F);
                  float var70 = var66 * var69;
                  if (!this.animValues.containsKey(var115 + "/stagger/bind")) {
                     this.animValues.put(var115 + "/stagger/bind", -10.0F);
                  }

                  float var71 = this.anim(var115 + "/stagger/bind", var130.isExpanded() ? 0.0F : -10.0F, 20.0F);
                  context.getMatrices().pushMatrix();
                  context.getMatrices().translate(0.0F, var67 + var71);
                  GuiRenderer.a(
                     context,
                     (float)(var9 + 4),
                     (float)var106,
                     132.0F,
                     17.0F,
                     Math.max(0.0F, WaterPlus.getEffectiveRoundness() - 3.0F),
                     this.multiplyAlpha(-15198181, var70),
                     false
                  );
                  GuiRenderer.a(
                     context,
                     (float)(var9 + 4),
                     (float)var106,
                     132.0F,
                     17.0F,
                     Math.max(0.0F, WaterPlus.getEffectiveRoundness() - 3.0F),
                     1.0F,
                     this.multiplyAlpha(0, var70),
                     false
                  );
                  this.drawStyledText(context, "Bind", var9 + 10, var106 + 4, this.multiplyAlpha(-1073741825, var70), false);
                  String var72 = var68 ? "..." : (var130.getBind() == 0 ? "None" : this.getKeyDisplayName(var130.getBind()));
                  int var73 = this.fontWidth(var72) + 10;
                  int var74 = var9 + 140 - 10 - var73;
                  int var75 = var106 + 3;
                  int var76 = var68
                     ? this.multiplyAlpha(COLOR_ACCENT & 16777215 | -2013265920, var70)
                     : this.multiplyAlpha(COLOR_ACCENT & 16777215 | 855638016, var70);
                  GuiRenderer.a(
                     context, (float)var74, (float)var75, (float)var73, 10.0F, Math.max(2.0F, WaterPlus.getEffectiveRoundness() * 0.5F), var76, false
                  );
                  GuiRenderer.a(
                     context,
                     (float)var74,
                     (float)var75,
                     (float)var73,
                     10.0F,
                     Math.max(2.0F, WaterPlus.getEffectiveRoundness() * 0.5F),
                     1.0F,
                     this.multiplyAlpha(COLOR_ACCENT & 16777215 | 1711276032, var70),
                     false
                  );
                  this.drawStyledText(context, var72, var74 + 5, var75 + 2, this.multiplyAlpha(COLOR_ACCENT, var70), false);
                  context.getMatrices().popMatrix();
                  var106 += 19;
                  if (var130 instanceof ActivatableModule var141) {
                     boolean var144 = this.listeningActivationBind == var141;
                     var70 = this.clamp01((var64 - (var106 - var106)) / 17.0F);
                     var71 = var66 * var70;
                     if (!this.animValues.containsKey(var115 + "/stagger/act")) {
                        this.animValues.put(var115 + "/stagger/act", -10.0F);
                     }

                     float var153 = this.anim(var115 + "/stagger/act", var130.isExpanded() ? 0.0F : -10.0F, 18.0F);
                     context.getMatrices().pushMatrix();
                     context.getMatrices().translate(0.0F, var67 + var153);
                     GuiRenderer.a(
                        context,
                        (float)(var9 + 4),
                        (float)var106,
                        132.0F,
                        17.0F,
                        Math.max(0.0F, WaterPlus.getEffectiveRoundness() - 3.0F),
                        this.multiplyAlpha(-15198181, var71),
                        false
                     );
                     GuiRenderer.a(
                        context,
                        (float)(var9 + 4),
                        (float)var106,
                        132.0F,
                        17.0F,
                        Math.max(0.0F, WaterPlus.getEffectiveRoundness() - 3.0F),
                        1.0F,
                        this.multiplyAlpha(0, var71),
                        false
                     );
                     this.drawStyledText(context, "Activation", var9 + 10, var106 + 4, this.multiplyAlpha(-1073741825, var71), false);
                     String var155 = var144 ? "..." : (var141.getActivationKey() == 0 ? "None" : this.getKeyDisplayName(var141.getActivationKey()));
                     var74 = this.fontWidth(var155) + 10;
                     var75 = var9 + 140 - 10 - var74;
                     var76 = var106 + 3;
                     int var77 = var144
                        ? this.multiplyAlpha(COLOR_ACCENT & 16777215 | -2013265920, var71)
                        : this.multiplyAlpha(COLOR_ACCENT & 16777215 | 855638016, var71);
                     GuiRenderer.a(
                        context, (float)var75, (float)var76, (float)var74, 10.0F, Math.max(2.0F, WaterPlus.getEffectiveRoundness() * 0.5F), var77, false
                     );
                     GuiRenderer.a(
                        context,
                        (float)var75,
                        (float)var76,
                        (float)var74,
                        10.0F,
                        Math.max(2.0F, WaterPlus.getEffectiveRoundness() * 0.5F),
                        1.0F,
                        this.multiplyAlpha(COLOR_ACCENT & 16777215 | 1711276032, var71),
                        false
                     );
                     this.drawStyledText(context, var155, var75 + 5, var76 + 2, this.multiplyAlpha(COLOR_ACCENT, var71), false);
                     context.getMatrices().popMatrix();
                     var106 += 19;
                  }

                  if ("Config Share".equals(var130.getName())) {
                     float var142 = this.clamp01((var64 - (var106 - var106)) / 17.0F);
                     var69 = var66 * var142;
                     boolean var148 = this.pointInRect(var93, var5, var9 + 4, var106, 132.0F, 17.0F);
                     int var151 = var148 ? this.multiplyAlpha(COLOR_ACCENT, var69) : this.multiplyAlpha(-15198181, var69);
                     context.getMatrices().pushMatrix();
                     context.getMatrices().translate(0.0F, var67);
                     GuiRenderer.a(
                        context, (float)(var9 + 4), (float)var106, 132.0F, 17.0F, Math.max(0.0F, WaterPlus.getEffectiveRoundness() - 3.0F), var151, false
                     );
                     GuiRenderer.a(
                        context,
                        (float)(var9 + 4),
                        (float)var106,
                        132.0F,
                        17.0F,
                        Math.max(0.0F, WaterPlus.getEffectiveRoundness() - 3.0F),
                        1.0F,
                        this.multiplyAlpha(COLOR_ACCENT, var69 * 0.6F),
                        false
                     );
                     this.drawStyledText(context, "Open Config Manager", var9 + 10, var106 + 4, this.multiplyAlpha(-1, var69), false);
                     context.getMatrices().popMatrix();
                     var106 += 19;
                  }

                  var68 = 0;

                  for (Setting var149 : var130.getSettings()) {
                     var71 = this.clamp01((var64 - (var106 - var140)) / 17.0F);
                     float var154 = var66 * var71;
                     String var156 = var115 + "/stagger/" + var68;
                     float var158 = var130.isExpanded() ? 0.0F : -8.0F;
                     if (!this.animValues.containsKey(var156)) {
                        this.animValues.put(var156, -8.0F);
                     }

                     float var160 = this.anim(var156, var158, Math.max(8.0F, 18.0F - var68 * 1.5F));
                     var68++;
                     context.getMatrices().pushMatrix();
                     context.getMatrices().translate(0.0F, var67 + var160);
                     GuiRenderer.a(
                        context,
                        (float)(var9 + 4),
                        (float)var106,
                        132.0F,
                        17.0F,
                        Math.max(0.0F, WaterPlus.getEffectiveRoundness() - 3.0F),
                        this.multiplyAlpha(0, var154),
                        false
                     );
                     GuiRenderer.a(
                        context,
                        (float)(var9 + 4),
                        (float)var106,
                        132.0F,
                        17.0F,
                        Math.max(0.0F, WaterPlus.getEffectiveRoundness() - 3.0F),
                        1.0F,
                        this.multiplyAlpha(0, var154),
                        false
                     );
                     Object var162 = var149.getValue();
                     if (var149 instanceof ModeSetting var163) {
                        this.drawModeSetting(context, var163, var9 + 4, 132, var106, var154);
                     } else if (var162 instanceof Boolean) {
                        boolean var81 = (Boolean)var162;
                        int var82 = var9 + 140 - 10 - 13;
                        int var83 = var106 + 4;
                        String var84 = System.identityHashCode(var149) + "/tog";
                        float var85 = this.anim(var84, var81 ? 1.0F : 0.0F, 16.0F);
                        GuiRenderer.a(
                           context, (float)var82, (float)var83, 13.0F, 8.0F, 4.0F, this.multiplyAlpha(lerpARGB(-15198181, COLOR_ACCENT, var85), var154), false
                        );
                        GuiRenderer.a(
                           context, (float)(var82 + 1 + Math.round(5.0F * var85)), (float)(var83 + 1), 6.0F, 6.0F, 3.0F, this.multiplyAlpha(-1, var154), false
                        );
                        this.drawStyledText(
                           context, var149.getName(), var9 + 10, var106 + 4, this.multiplyAlpha(lerpARGB(-1073741825, -1, var85), var154), false
                        );
                     } else if (!(var162 instanceof Float) && !(var162 instanceof Double) && !(var162 instanceof Integer)) {
                        if (var162 instanceof String) {
                           if ("GUI Key".equals(var149.getName())) {
                              String var10001 = this.listeningGuiKey ? "GUI Key: ..." : "GUI Key: " + WaterPlus.getGuiKeyDisplayName();
                              float var170 = MinecraftClient.getInstance().textRenderer.getWidth(var10001) + 10;
                              float var174 = var9 + 140 - 4 - var170;
                              float var178 = var106 + 3.5F;
                              GuiRenderer.a(context, var174, var178, var170, 10.0F, 4.0F, COLOR_ACCENT & 16777215 | 1140850688, false);
                              this.drawStyledText(context, "GUI Key", var9 + 10, var106 + 4, this.multiplyAlpha(-1, var154), false);
                              this.drawStyledText(
                                 context,
                                 this.listeningGuiKey ? "..." : WaterPlus.getGuiKeyDisplayName(),
                                 (int)(var174 + 5.0F),
                                 (int)(var178 + 1.0F),
                                 this.multiplyAlpha(COLOR_ACCENT, var154),
                                 false
                              );
                           } else {
                              String var171;
                              if (this.isStringListSetting(var130, var149)) {
                                 int var175 = this.parseStringList(var149).size();
                                 var171 = var149.getName() + ": " + var175 + " entries";
                                 if (this.expandedStringListSetting == var149) {
                                    var171 = var171 + " (edit)";
                                 }
                              } else {
                                 String var176 = this.formatStringSettingValue(var130, var149, (String)var162);
                                 var171 = var149.getName() + ": " + var176;
                                 if (this.listeningString == var149) {
                                    var171 = var171 + "_";
                                 }
                              }

                              this.drawInputTextClipped(context, var9 + 4, var106, 132.0F, 17.0F, var171, var9 + 10, var106 + 4, this.multiplyAlpha(-1, var154));
                           }
                        } else if (var149 instanceof BlocksSetting var78) {
                           this.drawBlocksSettingSummary(context, var78, var9 + 4, 132.0F, var106, 17, var154);
                        } else if (var149 instanceof MobsSetting var79) {
                           this.drawMobsSettingSummary(context, var79, var9 + 4, 132.0F, var106, 17, var154);
                        } else if (var162 instanceof Color) {
                           boolean var172 = var93 >= var9 + 4 && var93 <= var9 + 140 - 4 && var5 >= var106 && var5 <= var106 + 17;
                           this.drawStyledText(context, var149.getName(), var9 + 10, var106 + 4, this.multiplyAlpha(-1, var154), false);
                           this.drawColorSetting(
                              context, var149, var9 + 4, 132.0F, var106, 17, var172 ? 1.0F : 0.0F, this.expandedColorSetting == var149 ? 1.0F : 0.0F, var154
                           );
                        }
                     } else {
                        float var169;
                        float var173;
                        float var177;
                        String var179;
                        if (var162 instanceof Integer var180 && var149.getMin() instanceof Integer && var149.getMax() instanceof Integer) {
                           var169 = var180.intValue();
                           var177 = ((Integer)var149.getMin()).intValue();
                           var173 = ((Integer)var149.getMax()).intValue();
                           var179 = Integer.toString(var180);
                        } else {
                           var169 = var162 instanceof Float ? (Float)var162 : (float)((Double)var162).doubleValue();
                           var173 = var149.getMax() instanceof Float ? (Float)var149.getMax() : (float)((Double)var149.getMax()).doubleValue();
                           var177 = var149.getMin() instanceof Float ? (Float)var149.getMin() : (float)((Double)var149.getMin()).doubleValue();
                           if (this.allowDecimalForModule(var130)) {
                              var179 = String.format("%.1f", var169);
                           } else {
                              var169 = Math.round(var169);
                              var179 = Integer.toString(Math.round(var169));
                           }
                        }

                        float var181 = (var169 - var177) / (var173 - var177);
                        int var86 = var9 + 10;
                        int var87 = var106 + 11;
                        int var89 = (int)(120.0F * Math.max(0.0F, Math.min(1.0F, var181)));
                        GuiRenderer.a(context, (float)var86, (float)var87, 120.0F, 2.0F, 1.0F, this.multiplyAlpha(-1290266597, var154), false);
                        GuiRenderer.a(context, (float)var86, (float)var87, (float)var89, 2.0F, 1.0F, this.multiplyAlpha(COLOR_ACCENT, var154), false);
                        float var90 = var86 + var89 - 3.0F;
                        float var91 = var87 + 1.0F - 3.0F;
                        GuiRenderer.a(context, var90, var91, 6.0F, 6.0F, 3.0F, this.multiplyAlpha(COLOR_ACCENT, var154), false);
                        this.drawStyledText(context, var149.getName() + ": " + var179, var9 + 10, var106 + 2, this.multiplyAlpha(-1, var154), false);
                     }

                     context.getMatrices().popMatrix();
                     var106 += 19;
                     if (this.isStringListSetting(var130, var149) && this.expandedStringListSetting == var149) {
                        float var164 = this.clamp01((var64 - (var106 - var140)) / 17.0F);
                        if (var66 * var164 > 0.01F) {
                           context.getMatrices().pushMatrix();
                           context.getMatrices().translate(0.0F, var67);
                           this.drawStringListEditor(context, var149, var9 + 4, var106, 132, var66 * var164);
                           context.getMatrices().popMatrix();
                        }

                        var106 += this.getStringListEditorExtraHeight(var149);
                     }

                     if (var149 instanceof BlocksSetting var165 && this.expandedBlocksSetting == var165) {
                        float var167 = var66 * this.clamp01((var64 - (var106 - var140)) / 17.0F);
                        context.getMatrices().pushMatrix();
                        context.getMatrices().translate(0.0F, var67);
                        if (var167 > 0.01F) {
                           this.drawBlocksPicker(context, var165, var9 + 4, 132.0F, var106, mouseX, mouseY);
                        }

                        context.getMatrices().popMatrix();
                        var106 += this.getBlockPickerExtraHeight(var165);
                     }

                     if (var149 instanceof MobsSetting var166 && this.expandedMobsSetting == var166) {
                        float var168 = var66 * this.clamp01((var64 - (var106 - var140)) / 17.0F);
                        context.getMatrices().pushMatrix();
                        context.getMatrices().translate(0.0F, var67);
                        if (var168 > 0.01F) {
                           this.drawMobsPicker(context, var166, var9 + 4, 132.0F, var106, mouseX, mouseY);
                        }

                        context.getMatrices().popMatrix();
                        var106 += this.getMobPickerExtraHeight(var166);
                     }

                     if (var149.getValue() instanceof Color && this.expandedColorSetting == var149) {
                        var106 += 112;
                     }
                  }

                  var106 = var140 + var64;
               }
            }
         }

         if (var119 == 0) {
            GuiRenderer.a(context, (float)(var9 + 4), (float)var106, 132.0F, 17.0F, Math.max(0.0F, WaterPlus.getEffectiveRoundness() - 3.0F), 0, false);
            this.drawStyledText(context, "No results", var9 + 10, var106 + 4, -1073741825, false);
         }

         WaterFontRenderer.bk();
      }

      context.getMatrices().popMatrix();
      if (this.listeningBind != null) {
         for (int var98 = 32; var98 <= 348; var98++) {
            if (var98 != 256 && var98 != 259 && GLFW.glfwGetKey(MinecraftClient.getInstance().getWindow().getHandle(), var98) == 1) {
               this.listeningBind.setBind(var98);
               this.listeningBind = null;
               break;
            }
         }

         if (this.listeningBind != null && GLFW.glfwGetKey(MinecraftClient.getInstance().getWindow().getHandle(), 259) == 1) {
            this.listeningBind.setBind(0);
            this.listeningBind = null;
         }

         if (GLFW.glfwGetKey(MinecraftClient.getInstance().getWindow().getHandle(), 256) == 1) {
            this.listeningBind = null;
         }
      }

      if (this.listeningActivationBind != null) {
         for (int var99 = 32; var99 <= 348; var99++) {
            if (var99 != 256 && var99 != 259 && GLFW.glfwGetKey(MinecraftClient.getInstance().getWindow().getHandle(), var99) == 1) {
               this.listeningActivationBind.setActivationKey(var99);
               this.listeningActivationBind = null;
               break;
            }
         }

         if (this.listeningActivationBind != null && GLFW.glfwGetKey(MinecraftClient.getInstance().getWindow().getHandle(), 259) == 1) {
            this.listeningActivationBind.setActivationKey(0);
            this.listeningActivationBind = null;
         }

         if (GLFW.glfwGetKey(MinecraftClient.getInstance().getWindow().getHandle(), 256) == 1) {
            this.listeningActivationBind = null;
         }
      }

      if (this.listeningGuiKey) {
         for (int var100 = 32; var100 <= 348; var100++) {
            if (var100 != 256 && var100 != 259 && GLFW.glfwGetKey(MinecraftClient.getInstance().getWindow().getHandle(), var100) == 1) {
               WaterPlus.applyGuiKey(var100, getKeyDisplayNameStatic(var100));
               this.listeningGuiKey = false;
               break;
            }
         }

         if (this.listeningGuiKey && GLFW.glfwGetKey(MinecraftClient.getInstance().getWindow().getHandle(), 256) == 1) {
            this.listeningGuiKey = false;
         }
      }
   }

   private void drawBlocksSettingSummary(
      DrawContext context, BlocksSetting setting, float panelX, float panelWidth, float rowY, int rowHeight, float revealAlpha
   ) {
      int var15 = this.expandedBlocksSetting == setting ? "v" : ">";
      int var8 = this.fontWidth(var15);
      float var14 = Math.round(panelX + panelWidth - 6.0F - var8);
      var8 = Math.max(30, var14 - (Math.round(panelX) + 10 + this.fontWidth(setting.getName()) + 14));
      int var9 = this.multiplyAlpha(this.expandedBlocksSetting != setting && setting.size() <= 0 ? -1073741825 : -1, revealAlpha);
      String var10 = setting.size() == 0 ? "Choose" : this.buildBlocksPreviewText(setting);
      var10 = this.trimWithEllipsis(var10, Math.round((var8 - 18) / 0.9F));
      var8 = Math.max(34, Math.min(var8, this.fontWidth(var10) + 22));
      int var11 = var14 - var8 - 6;
      int var12 = this.multiplyAlpha(setting.size() > 0 ? COLOR_ACCENT_DIM : -15198181, revealAlpha);
      ItemStack var13 = this.getPreviewBlockStack(setting);
      this.drawStyledText(context, setting.getName(), Math.round(panelX) + 10, Math.round(rowY) + 4, var9, false);
      GuiRenderer.a(context, (float)var11, rowY + 2.0F, (float)var8, 12.0F, Math.max(2.0F, WaterPlus.getEffectiveRoundness() * 0.5F), var12, false);
      GuiRenderer.a(
         context,
         (float)var11,
         rowY + 2.0F,
         (float)var8,
         12.0F,
         Math.max(2.0F, WaterPlus.getEffectiveRoundness() * 0.5F),
         1.0F,
         this.multiplyAlpha(0, revealAlpha),
         false
      );
      if (!var13.isEmpty()) {
         context.drawItem(var13, var11 + 2, Math.round(rowY) + 1);
      }

      this.drawScaledText(context, var10, var11 + (var13.isEmpty() ? 6 : 16), rowY + 4.0F, 0.9F, this.multiplyAlpha(-1, revealAlpha));
      this.drawStyledText(context, var15, var14, Math.round(rowY) + 4, this.multiplyAlpha(-1073741825, revealAlpha), false);
   }

   private void drawBlocksPicker(DrawContext context, BlocksSetting setting, float panelX, float panelWidth, float pickerY, int mouseX, int mouseY) {
      float var17 = this.buildBlockPickerLayout(panelX, panelWidth, pickerY, setting);
      float var18 = this.getFilteredBlocks(setting);
      this.blockPickerScroll = this.clampBlockPickerScroll(var18.size(), this.blockPickerScroll);
      pickerY = WaterPlus.getEffectiveRoundness();
      pickerY = Math.max(4.0F, pickerY * 0.5F);
      float var8 = WaterPlus.getGlassIntensity();
      int var9 = WaterPlus.getBackgroundARGB();
      GuiRenderer.a(context, var17.x, var17.y, var17.width, var17.height, pickerY, var9, false);
      if (var8 > 0.01F) {
         GuiRenderer.a(context, var17.x, var17.y, var17.width, var17.height, pickerY, 1.0F, glassCol(16777215, (int)(50.0F * var8)), false);
         GuiRenderer.a(context, var17.x + 1.0F, var17.y + 1.0F, var17.width - 2.0F, 6.0F, pickerY, glassCol(16777215, (int)(15.0F * var8)), false);
      } else {
         GuiRenderer.a(context, var17.x, var17.y, var17.width, var17.height, pickerY, 1.0F, 385875967, false);
      }

      boolean var21 = this.blockSearchActive && this.expandedBlocksSetting == setting;
      var9 = var21 ? -15919840 : -16117736;
      int var10 = var21 ? COLOR_ACCENT : -14799552;
      GuiRenderer.a(context, var17.searchX, var17.searchY, var17.searchWidth, var17.searchHeight, pickerY, var9, false);
      GuiRenderer.a(context, var17.searchX, var17.searchY, var17.searchWidth, var17.searchHeight, pickerY, 1.0F, var10, false);
      String var24 = this.blockSearchQuery.isEmpty() ? "⌕  Search..." : "⌕  " + this.blockSearchQuery;
      if (var21 && System.currentTimeMillis() / 500L % 2L == 0L) {
         var24 = var24 + "_";
      }

      this.drawInputTextClipped(
         context,
         var17.searchX,
         var17.searchY,
         Math.max(0.0F, var17.searchWidth),
         Math.max(0.0F, var17.searchHeight),
         var24,
         Math.round(var17.searchX) + 6,
         Math.round(var17.searchY) + 4,
         this.blockSearchQuery.isEmpty() && !var21 ? -1073741825 : -1
      );
      GuiRenderer.a(context, var17.clearX, var17.clearY, var17.clearWidth, var17.clearHeight, pickerY, 585125984, false);
      GuiRenderer.a(context, var17.clearX, var17.clearY, var17.clearWidth, var17.clearHeight, pickerY, 1.0F, 1725976672, false);
      this.drawStyledText(context, "✕", Math.round(var17.clearX) + (int)(var17.clearWidth / 2.0F) - 3, Math.round(var17.clearY) + 4, -2076576, false);
      if (var18.isEmpty()) {
         this.drawStyledText(context, "No blocks found", Math.round(var17.listX) + 6, Math.round(var17.listY) + 4, -1073741825, false);
      } else {
         int var22 = Math.min(5, var18.size());
         boolean var25 = var18.size() > var22;

         for (int var26 = 0; var26 < var22; var26++) {
            int var11 = this.blockPickerScroll + var26;
            if (var11 >= var18.size()) {
               break;
            }

            Block var12 = (Block)var18.get(var11);
            float var13 = var17.listY + var26 * 18;
            int var14 = setting.contains(var12);
            boolean var15 = mouseX >= var17.listX && mouseX <= var17.listX + var17.listWidth && mouseY >= var13 && mouseY <= var13 + 18.0F - 2.0F;
            var11 = var14 ? COLOR_ACCENT & 16777215 | 570425344 : (var15 ? 419430399 : 0);
            if (var11 != 0) {
               GuiRenderer.a(context, var17.listX, var13, var17.listWidth, 16.0F, pickerY, var11, false);
            }

            if (var14) {
               GuiRenderer.a(context, var17.listX, var13 + 2.0F, 2.0F, 12.0F, 1.0F, COLOR_ACCENT, false);
            }

            ItemStack var29 = new ItemStack(var12);
            if (!var29.isEmpty()) {
               context.getMatrices().pushMatrix();
               context.getMatrices().translate(var17.listX + 4.0F, var13 + 1.0F);
               context.getMatrices().scale(0.75F, 0.75F);
               context.drawItem(var29, 0, 0);
               context.getMatrices().popMatrix();
            }

            float var30 = var17.listX + var17.listWidth - 10.0F;
            int var16 = Math.round(var17.listX) + 16;
            String var32 = this.trimWithEllipsis(setting.getDisplayName(var12), Math.round((var30 - var16 - 4.0F) / 0.9F));
            this.drawScaledText(context, var32, var16, var13 + 4.0F, 0.9F, var14 ? COLOR_ACCENT : (var15 ? -1 : -1073741825));
            int var33 = var14 ? COLOR_ACCENT : -14799552;
            var14 = var14 ? COLOR_ACCENT & 16777215 | 1140850688 : 0;
            if (var14 != 0) {
               GuiRenderer.a(context, var30, var13 + 5.0F, 6.0F, 6.0F, 3.0F, var14, false);
            }

            GuiRenderer.a(context, var30, var13 + 5.0F, 6.0F, 6.0F, 3.0F, 1.0F, var33, false);
         }

         if (var25) {
            var10 = Math.max(1, var18.size() - var22);
            float var31 = var17.listX + var17.listWidth - 4.0F;
            float var34 = var17.listY + 1.0F;
            float var35 = var17.listHeight - 2.0F;
            float var37 = Math.max(12.0F, var35 * ((float)var22 / var18.size()));
            float var38 = (var35 - var37) * ((float)this.blockPickerScroll / var10);
            GuiRenderer.a(context, var31, var34, 4.0F, var35, 2.0F, -16117736, false);
            GuiRenderer.a(context, var31, var34 + var38, 4.0F, var37, 2.0F, COLOR_ACCENT & 16777215 | -1442840576, false);
         }
      }
   }

   private void drawColorSetting(
      DrawContext context,
      Setting<Color> setting,
      float panelX,
      float panelWidth,
      float rowY,
      int rowHeight,
      float hoverProgress,
      float expansionProgress,
      float revealAlpha
   ) {
      float var26 = (Color)setting.getValue();
      float var10 = WaterPlus.getEffectiveRoundness();
      var10 = Math.max(2.0F, var10 * 0.5F);
      panelWidth = panelX + panelWidth - 5.0F - 12.0F;
      float var11 = rowY + (rowHeight - 4.0F - 12.0F) / 2.0F;
      GuiRenderer.a(context, panelWidth, var11 + 2.0F, 12.0F, 12.0F, var10, this.multiplyAlpha((Color)setting.getValue(), revealAlpha), false);
      GuiRenderer.a(context, panelWidth, var11 + 2.0F, 12.0F, 12.0F, var10, 1.0F, this.multiplyAlpha(-1427114245, revealAlpha), false);
      if (!(expansionProgress <= 0.01F)) {
         Setting var18 = this.easeOutCubic(expansionProgress) * revealAlpha;
         panelX += 4.0F;
         panelWidth = rowY + rowHeight + 6.0F;
         rowY = Math.max(1.0F, 80.0F * var18);
         int var25 = Math.max(1.0F, 16.0F * var18);
         expansionProgress = panelX + rowY + 6.0F;
         revealAlpha = this.getHue(var26);
         var11 = this.getSaturation(var26);
         float var12 = this.getBrightness(var26);
         float var13 = rowY / 12.0F;
         float var10000 = rowY / 12.0F;

         for (int var14 = 0; var14 < 12; var14++) {
            float var15 = 1.0F - var14 / 12.0F;

            for (int var16 = 0; var16 < 12; var16++) {
               float var17 = var16 / 12.0F;
               context.fill(
                  (int)(panelX + var16 * var13),
                  (int)(panelWidth + var14 * var13),
                  (int)(panelX + (var16 + 1) * var13),
                  (int)(panelWidth + (var14 + 1) * var13),
                  this.withAlpha(Color.HSBtoRGB(revealAlpha, var17, var15), var18)
               );
            }
         }

         GuiRenderer.a(context, panelX, panelWidth, rowY, rowY, var10, 1.0F, this.withAlpha(1728053247, var18), false);
         float var31 = panelX + var11 * rowY;
         float var32 = panelWidth + (1.0F - var12) * rowY;
         GuiRenderer.a(context, var31 - 4.0F, var32 - 4.0F, 8.0F, 8.0F, 4.0F, this.withAlpha(-2013265920, var18), false);
         GuiRenderer.a(context, var31 - 4.0F, var32 - 4.0F, 8.0F, 8.0F, 4.0F, 2.0F, this.withAlpha(-1, var18), false);

         for (int var33 = 0; var33 < (int)rowY; var33++) {
            float var35 = (float)var33 / (int)rowY;
            context.fill(
               (int)expansionProgress,
               (int)(panelWidth + var33),
               (int)(expansionProgress + var25),
               (int)(panelWidth + var33 + 1.0F),
               this.withAlpha(0xFF000000 | Color.HSBtoRGB(var35, 1.0F, 1.0F) & 16777215, var18)
            );
         }

         GuiRenderer.a(context, expansionProgress, panelWidth, var25, rowY, var10, 1.0F, this.withAlpha(1728053247, var18), false);
         GuiRenderer.a(context, expansionProgress - 2.0F, panelWidth + revealAlpha * rowY - 1.0F, var25 + 4.0F, 3.0F, 1.5F, this.withAlpha(-1, var18), false);
         float var34 = panelWidth + rowY + 6.0F;
         float var36 = (rowY + 6.0F + var25) / 2.0F - 2.0F;
         panelWidth = Math.max(1.0F, 14.0F * var18);
         float var24 = this.multiplyAlpha(var26, var18);
         GuiRenderer.a(context, panelX, var34, var36, panelWidth, var10, var24, false);
         GuiRenderer.a(context, panelX + var36 + 4.0F, var34, var36, panelWidth, var10, var24, false);
         GuiRenderer.a(context, panelX, var34, var36, panelWidth, var10, 1.0F, this.withAlpha(1442840575, var18), false);
         GuiRenderer.a(context, panelX + var36 + 4.0F, var34, var36, panelWidth, var10, 1.0F, this.withAlpha(1442840575, var18), false);
         this.drawStyledText(context, "ORIGINAL", (int)(panelX + 2.0F), (int)(var34 + panelWidth + 2.0F), this.withAlpha(-6642510, var18), false);
         this.drawStyledText(context, "NEW", (int)(panelX + var36 + 6.0F), (int)(var34 + panelWidth + 2.0F), this.withAlpha(-6642510, var18), false);
      }
   }

   public float getHue(Color c) {
      return Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null)[0];
   }

   public float getSaturation(Color c) {
      return Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null)[1];
   }

   public float getBrightness(Color c) {
      return Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null)[2];
   }

   public float getAlphaFloat(Color c) {
      return c.getAlpha() / 255.0F;
   }

   private void drawStripBar(DrawContext context, float x, float y, float width, float height, float radius, int segments, IntFunction<Integer> colorProvider) {
      if (!(height <= 0.0F)) {
         float var9 = Math.max(1.0F, width / segments);

         for (int var10 = 0; var10 < segments; var10++) {
            float var11 = x + var9 * var10;
            float var12 = var10 == segments - 1 ? x + width - var11 : var9 + 1.0F;
            float var13 = var10 == 0 ? radius : 0.0F;
            float var14 = var10 == segments - 1 ? radius : 0.0F;
            GuiRenderer.a(context, var11, y, var12, height, var13, var14, var14, var13, false, (Integer)colorProvider.apply(var10));
         }
      }
   }

   @Override
   public boolean mouseClicked(Click click, boolean doubled) {
      this.uiScale = this.computeUiScale();
      double var3 = this.toUiX(click.x());
      double var5 = this.toUiY(click.y());
      int var7 = click.button();
      this.activeColorSetting = null;
      this.colorDragMode = ClickGuiScreen.c.NONE;
      this.draggingNumericSetting = null;
      this.draggingNumericModule = null;
      int var8 = Math.min(260, this.uiWidth() - 60);
      int var9 = (this.uiWidth() - var8) / 2;
      int var10 = this.uiHeight() - 20 - 60;
      String var11 = "Configs";
      int var50 = this.fontWidth(var11) + 18;
      int var12 = (this.uiWidth() - var50) / 2;
      int var13 = var10 - 14 - 6;
      if (var3 >= var12 && var3 <= var12 + var50 && var5 >= var13 && var5 <= var13 + 14) {
         this.searchActive = false;
         MinecraftClient.getInstance().setScreen(new ConfigManagerScreen());
         return true;
      } else if (var3 >= var9 && var3 <= var9 + var8 && var5 >= var10 && var5 <= var10 + 20) {
         this.searchActive = true;
         this.blockSearchActive = false;
         this.listeningBind = null;
         this.listeningActivationBind = null;
         this.listeningString = null;
         return true;
      } else {
         this.searchActive = false;
         if (var7 == 0) {
            Category[] var32 = CACHED_CATEGORIES;

            for (int var38 = 0; var38 < var32.length; var38++) {
               var10 = this.getCategoryX(var32[var38], var38);
               int var51 = this.getCategoryY(var32[var38]);
               if (var3 >= var10 && var3 <= var10 + 140 && var5 >= var51 && var5 <= var51 + 22) {
                  this.draggingCategory = var32[var38];
                  this.dragGrabOffsetX = (int)(var3 - var10);
                  this.dragGrabOffsetY = (int)(var5 - var51);
                  return true;
               }
            }
         }

         Category[] var33 = CACHED_CATEGORIES;

         for (var9 = 0; var9 < var33.length; var9++) {
            Category var45 = var33[var9];
            int var52 = this.getCategoryX(var45, var9);
            var12 = this.getCategoryY(var45);
            var12 = var12 + 22 + 6;

            for (Module var14 : ModuleManager.INSTANCE.getModulesInCategory(var45)) {
               if (this.matchesQuery(var14)) {
                  if (var3 >= var52 + 4 && var3 <= var52 + 140 - 4 && var5 >= var12 && var5 <= var12 + 17) {
                     if (var7 == 1 && var14.getName().equals("Chat Macro")) {
                        MinecraftClient.getInstance().setScreen(new ChatMacroScreen(this));
                        return true;
                     }

                     if (var7 == 0) {
                        var14.toggle();
                     } else if (var7 == 1) {
                        boolean var59 = !var14.isExpanded();
                        String var60 = var45.name() + "/" + var14.getName();
                        if (!var59) {
                           this.animValues.put(var60 + "/expand", 0.0F);
                        } else {
                           this.animValues.put(var60 + "/expand", 0.0F);

                           for (int var67 = 0; var67 < var14.getSettings().size() + 2; var67++) {
                              this.animValues.put(var60 + "/stagger/" + var67, -8.0F);
                           }

                           this.animValues.put(var60 + "/stagger/bind", -10.0F);
                           this.animValues.put(var60 + "/stagger/act", -10.0F);
                        }

                        var14.setExpanded(var59);
                     }

                     return true;
                  }

                  var12 += 19;
                  if (var14.isExpanded()) {
                     if (var3 >= var52 + 4 && var3 <= var52 + 140 - 4 && var5 >= var12 && var5 <= var12 + 17) {
                        if (var7 == 1) {
                           var14.setBind(0);
                           this.listeningBind = null;
                           this.listeningActivationBind = null;
                        } else if (var7 == 0) {
                           this.listeningBind = var14;
                           this.listeningActivationBind = null;
                        }

                        return true;
                     }

                     var12 += 19;
                     if ("Config Share".equals(var14.getName())) {
                        if (var3 >= var52 + 4 && var3 <= var52 + 140 - 4 && var5 >= var12 && var5 <= var12 + 17) {
                           if (var7 == 0) {
                              MinecraftClient.getInstance().setScreen(new ConfigManagerScreen());
                           }

                           return true;
                        }

                        var12 += 19;
                     }

                     if (var14 instanceof ActivatableModule var15) {
                        if (var3 >= var52 + 4 && var3 <= var52 + 140 - 4 && var5 >= var12 && var5 <= var12 + 17) {
                           if (var7 == 1) {
                              var15.setActivationKey(0);
                              this.listeningActivationBind = null;
                              this.listeningBind = null;
                           } else if (var7 == 0) {
                              this.listeningActivationBind = var15;
                              this.listeningBind = null;
                           }

                           return true;
                        }

                        var12 += 19;
                     }

                     for (Setting var16 : var14.getSettings()) {
                        if (var3 >= var52 + 4 && var3 <= var52 + 140 - 4 && var5 >= var12 && var5 <= var12 + 17) {
                           if (var16 instanceof ModeSetting var66) {
                              if (var7 == 1) {
                                 var66.bf();
                              } else {
                                 var66.be();
                              }
                           } else if (var16.getValue() instanceof Boolean) {
                              var16.setValue(!(Boolean)var16.getValue());
                           } else if (var16.getValue() instanceof String) {
                              if ("GUI Key".equals(var16.getName())) {
                                 if (var7 == 0) {
                                    this.listeningGuiKey = !this.listeningGuiKey;
                                    this.listeningBind = null;
                                    this.listeningActivationBind = null;
                                    this.listeningString = null;
                                 } else if (var7 == 1) {
                                    this.listeningGuiKey = false;
                                 }
                              } else if (this.isStringListSetting(var14, var16)) {
                                 if (var7 == 0) {
                                    this.expandedStringListSetting = this.expandedStringListSetting == var16 ? null : var16;
                                    this.stringListAddActive = this.expandedStringListSetting == var16;
                                    this.stringListAddBuffer = "";
                                    this.listeningString = null;
                                 } else if (var7 == 1) {
                                    this.expandedStringListSetting = null;
                                    this.stringListAddActive = false;
                                    this.stringListAddBuffer = "";
                                 }
                              } else {
                                 this.expandedStringListSetting = null;
                                 this.stringListAddActive = false;
                                 this.stringListAddBuffer = "";
                                 this.listeningString = var16;
                              }
                           } else if (!(var16.getValue() instanceof Float) && !(var16.getValue() instanceof Double) && !(var16.getValue() instanceof Integer)) {
                              if (var16 instanceof BlocksSetting var70) {
                                 if (var7 == 0 || var7 == 1) {
                                    Click var26 = new LinkedHashMap();
                                    boolean var29 = null;
                                    if (var14 instanceof StorageESP var37) {
                                       var26.putAll(var37.d());
                                       var29 = var37::b;
                                    } else if (var14 instanceof ExtraESP var43) {
                                       var26.putAll(var43.b());
                                       var29 = var43::a;
                                    }

                                    MinecraftClient.getInstance().setScreen(new BlockSelectionScreen(this, var14, var70, var26, var29));
                                    return true;
                                 }

                                 if (var7 == 2) {
                                    var70.clear();
                                 }
                              } else if (var16 instanceof MobsSetting var73) {
                                 if (var7 == 0) {
                                    if (this.expandedMobsSetting != var73) {
                                       this.mobSearchQuery = "";
                                       this.mobPickerScroll = 0;
                                    }

                                    this.expandedMobsSetting = this.expandedMobsSetting == var73 ? null : var73;
                                    this.mobSearchActive = this.expandedMobsSetting == var73;
                                 } else if (var7 == 1) {
                                    var73.clear();
                                    this.mobPickerScroll = 0;
                                 }
                              } else if (var16.getValue() instanceof Color) {
                                 if (var7 == 0) {
                                    this.expandedColorSetting = this.expandedColorSetting == var16 ? null : var16;
                                 } else if (var7 == 1) {
                                    this.expandedColorSetting = null;
                                 }
                              }
                           } else if (var7 == 0) {
                              this.beginSettingDragBatch();
                              this.draggingNumericSetting = var16;
                              this.draggingNumericModule = var14;
                              this.draggingNumericCatX = var52;
                              this.updateNumericSetting(var14, var16, var3, var52);
                           }

                           return true;
                        }

                        if (this.isStringListSetting(var14, var16) && this.expandedStringListSetting == var16) {
                           int var17 = var12 + 19;
                           int var18 = this.getStringListEditorExtraHeight(var16);
                           int var19 = var52 + 4;
                           if (this.pointInRect(var3, var5, var19, var17, 132.0F, var18)) {
                              List var36 = this.parseStringList(var16);
                              var9 = Math.min(6, var36.size());

                              for (Click var22 = 0; var22 < var9; var22++) {
                                 var10 = var17 + var22 * 19;
                                 int var53 = var19 + 132 - 10 - 12;
                                 var10 += 2;
                                 if (this.pointInRect(var3, var5, var53, var10, 12.0F, 12.0F) && var7 == 0) {
                                    Click var23 = (String)var36.get(var22);
                                    var36.removeIf(n -> n.equalsIgnoreCase(var23));
                                    this.setStringListFromLowerList(var16, var36);
                                    return true;
                                 }
                              }

                              Click var24 = var17 + var9 * 19;
                              var10 = var19 + 132 - 10 - 12;
                              int var54 = var24 + 2;
                              if (var7 == 0 && this.pointInRect(var3, var5, var10, var54, 12.0F, 12.0F)) {
                                 String var49 = this.stringListAddBuffer == null ? "" : this.stringListAddBuffer.trim();
                                 if (!var49.isEmpty()) {
                                    Click var25 = var49.toLowerCase(Locale.ROOT);
                                    boolean var30 = false;

                                    for (String var31 : var36) {
                                       if (var31.equalsIgnoreCase(var25)) {
                                          var30 = true;
                                          break;
                                       }
                                    }

                                    if (!var30) {
                                       var36.add(var25);
                                       this.setStringListFromLowerList(var16, var36);
                                    }
                                 }

                                 this.stringListAddBuffer = "";
                                 this.stringListAddActive = true;
                                 this.listeningString = null;
                                 return true;
                              }

                              if (var7 == 0 && this.pointInRect(var3, var5, var19, var24, 132.0F, 17.0F)) {
                                 this.stringListAddActive = true;
                                 this.listeningString = null;
                                 return true;
                              }

                              return true;
                           }
                        }

                        if (var16 instanceof BlocksSetting var61 && this.expandedBlocksSetting == var61) {
                           ClickGuiScreen.b var68 = this.buildBlockPickerLayout(var52 + 4, 132.0F, var12 + 19, var61);
                           if (this.pointInRect(var3, var5, var68.clearX, var68.clearY, var68.clearWidth, var68.clearHeight)) {
                              var61.clear();
                              this.blockPickerScroll = 0;
                              this.blockSearchQuery = "";
                              return true;
                           }

                           if (this.pointInRect(var3, var5, var68.searchX, var68.searchY, var68.searchWidth, var68.searchHeight)) {
                              this.blockSearchActive = true;
                              this.searchActive = false;
                              this.listeningString = null;
                              return true;
                           }

                           if (this.pointInRect(var3, var5, var68.x, var68.y, var68.width, var68.height)) {
                              this.blockSearchActive = false;
                              List var72 = this.getFilteredBlocks(var61);
                              Click var21 = Math.min(5, Math.max(1, var72.size()));

                              for (boolean var28 = 0; var28 < var21; var28++) {
                                 var8 = this.clampBlockPickerScroll(var72.size(), this.blockPickerScroll) + var28;
                                 if (var8 >= var72.size()) {
                                    break;
                                 }

                                 float var41 = var68.listY + var28 * 18;
                                 if (this.pointInRect(var3, var5, var68.listX, var41, var68.listWidth, 16.0F)) {
                                    var61.toggle((Block)var72.get(var8));
                                    return true;
                                 }
                              }

                              return true;
                           }
                        }

                        if (var16 instanceof MobsSetting var62 && this.expandedMobsSetting == var62) {
                           ClickGuiScreen.b var69 = this.buildMobPickerLayout(var52 + 4, 132.0F, var12 + 19, var62);
                           if (this.pointInRect(var3, var5, var69.clearX, var69.clearY, var69.clearWidth, var69.clearHeight)) {
                              var62.clear();
                              this.mobPickerScroll = 0;
                              this.mobSearchQuery = "";
                              return true;
                           }

                           if (this.pointInRect(var3, var5, var69.searchX, var69.searchY, var69.searchWidth, var69.searchHeight)) {
                              this.mobSearchActive = true;
                              this.searchActive = false;
                              this.listeningString = null;
                              return true;
                           }

                           if (this.pointInRect(var3, var5, var69.x, var69.y, var69.width, var69.height)) {
                              this.mobSearchActive = false;
                              List var71 = this.getFilteredMobs(var62);
                              Click var20 = Math.min(5, Math.max(1, var71.size()));

                              for (boolean var27 = 0; var27 < var20; var27++) {
                                 var8 = this.clampMobPickerScroll(var71.size(), this.mobPickerScroll) + var27;
                                 if (var8 >= var71.size()) {
                                    break;
                                 }

                                 float var40 = var69.listY + var27 * 18;
                                 if (this.pointInRect(var3, var5, var69.listX, var40, var69.listWidth, 16.0F)) {
                                    var62.toggle((EntityType<?>)var71.get(var8));
                                    return true;
                                 }
                              }

                              return true;
                           }
                        }

                        if (var7 == 0 && var16.getValue() instanceof Color && this.expandedColorSetting == var16) {
                           ClickGuiScreen.d var63 = this.buildColorPickerLayout(var52 + 4, 132.0F, var12, 17);
                           if (this.pointInRect(var3, var5, var63.fieldX, var63.fieldY, var63.fieldWidth, var63.fieldHeight)) {
                              this.beginSettingDragBatch();
                              this.updateColorFromField(var16, var63, var3, var5);
                              this.activeColorSetting = var16;
                              this.colorDragMode = ClickGuiScreen.c.FIELD;
                              return true;
                           }

                           if (this.pointInRect(var3, var5, var63.alphaY - 6.0F, var63.fieldY - 4.0F, var63.alphaHeight + 12.0F, var63.fieldHeight + 8.0F)) {
                              this.beginSettingDragBatch();
                              this.updateColorFromAlpha(var16, var63, var5);
                              this.activeColorSetting = var16;
                              this.colorDragMode = ClickGuiScreen.c.ALPHA;
                              return true;
                           }
                        }

                        var12 += 19;
                        if (this.isStringListSetting(var14, var16) && this.expandedStringListSetting == var16) {
                           var12 += this.getStringListEditorExtraHeight(var16);
                        }

                        if (var16 instanceof BlocksSetting var64 && this.expandedBlocksSetting == var64) {
                           var12 += this.getBlockPickerExtraHeight(var64);
                        }

                        if (var16 instanceof MobsSetting var65 && this.expandedMobsSetting == var65) {
                           var12 += this.getMobPickerExtraHeight(var65);
                        }

                        if (var16.getValue() instanceof Color && this.expandedColorSetting == var16) {
                           var12 += 112;
                        }
                     }
                  }
               }
            }
         }

         this.listeningBind = null;
         this.listeningActivationBind = null;
         this.listeningString = null;
         this.stringListAddActive = false;
         this.blockSearchActive = false;
         return super.mouseClicked(click, doubled);
      }
   }

   @Override
   public boolean mouseDragged(Click click, double offsetX, double offsetY) {
      this.uiScale = this.computeUiScale();
      double var6 = this.toUiX(click.x());
      double var8 = this.toUiY(click.y());
      int var10 = click.button();
      if (var10 != 0) {
         return super.mouseDragged(click, offsetX, offsetY);
      } else if (this.colorDragMode != ClickGuiScreen.c.NONE && this.activeColorSetting != null && this.updateActiveColorDrag(var6, var8)) {
         return true;
      } else if (this.draggingNumericSetting != null && this.draggingNumericModule != null) {
         this.updateNumericSetting(this.draggingNumericModule, this.draggingNumericSetting, var6, this.draggingNumericCatX);
         return true;
      } else if (this.draggingCategory == null) {
         return super.mouseDragged(click, offsetX, offsetY);
      } else {
         Click var11 = CACHED_CATEGORIES;
         double var14 = 0;

         for (int var3 = 0; var3 < var11.length; var3++) {
            if (var11[var3] == this.draggingCategory) {
               var14 = var3;
               break;
            }
         }

         int var16 = CACHED_CATEGORIES.length * 140 + (CACHED_CATEGORIES.length - 1) * 12;
         Click var12 = Math.max(10, (this.uiWidth() - var16) / 2);
         Click var13 = var12 + var14 * 152;
         double var15 = this.getContentTop() + this.verticalScroll;
         int[] var17 = this.getCategoryOffset(this.draggingCategory);
         var17[0] = (int)(var6 - this.dragGrabOffsetX) - var13;
         var17[1] = (int)(var8 - this.dragGrabOffsetY) - var15;
         return true;
      }
   }

   @Override
   public boolean mouseReleased(Click click) {
      this.activeColorSetting = null;
      this.colorDragMode = ClickGuiScreen.c.NONE;
      this.draggingCategory = null;
      this.draggingNumericSetting = null;
      this.draggingNumericModule = null;
      this.finishSettingDragBatch();
      return super.mouseReleased(click);
   }

   @Override
   public void removed() {
      this.finishSettingDragBatch();
      super.removed();
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      this.uiScale = this.computeUiScale();
      mouseX = this.toUiX(mouseX);
      mouseY = this.toUiY(mouseY);
      double var9 = verticalAmount != 0.0 ? verticalAmount : horizontalAmount;
      if (var9 == 0.0) {
         return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
      } else {
         double var13 = this.getExpandedBlocksPickerContext();
         if (var13 != null && this.pointInRect(mouseX, mouseY, var13.layout.x, var13.layout.y, var13.layout.width, var13.layout.height)) {
            this.blockPickerScroll = this.clampBlockPickerScroll(this.getFilteredBlocks(var13.setting).size(), this.blockPickerScroll + (var9 > 0.0 ? -1 : 1));
            return true;
         } else {
            double var14 = this.getExpandedMobsPickerContext();
            if (var14 != null && this.pointInRect(mouseX, mouseY, var14.layout.x, var14.layout.y, var14.layout.width, var14.layout.height)) {
               this.mobPickerScroll = this.clampMobPickerScroll(this.getFilteredMobs(var14.setting).size(), this.mobPickerScroll + (var9 > 0.0 ? -1 : 1));
               return true;
            } else {
               this.verticalScroll = this.clampVerticalScroll(this.verticalScroll + (int)Math.round(var9 * 24.0));
               return true;
            }
         }
      }
   }

   @Override
   public boolean charTyped(CharInput input) {
      String var2 = this.sanitizeTextInput(input.asString());
      if (var2.isEmpty()) {
         return super.charTyped(input);
      } else if (this.blockSearchActive && this.expandedBlocksSetting != null) {
         this.blockSearchQuery = this.blockSearchQuery + var2;
         this.blockPickerScroll = 0;
         return true;
      } else if (this.mobSearchActive && this.expandedMobsSetting != null) {
         this.mobSearchQuery = this.mobSearchQuery + var2;
         this.mobPickerScroll = 0;
         return true;
      } else if (this.searchActive && this.listeningString == null) {
         this.searchQuery = this.searchQuery + var2;
         return true;
      } else if (this.stringListAddActive && this.expandedStringListSetting != null) {
         this.stringListAddBuffer = (this.stringListAddBuffer == null ? "" : this.stringListAddBuffer) + var2;
         return true;
      } else if (this.listeningString != null) {
         this.listeningString.setValue(this.listeningString.getValue() + var2);
         return true;
      } else {
         return super.charTyped(input);
      }
   }

   @Override
   public boolean shouldPause() {
      return false;
   }

   @Override
   public boolean keyPressed(KeyInput input) {
      if (this.blockSearchActive && this.expandedBlocksSetting != null && this.handleBlockSearchKeyInput(input)) {
         return true;
      } else if (this.mobSearchActive && this.expandedMobsSetting != null && this.handleMobSearchKeyInput(input)) {
         return true;
      } else if (this.searchActive) {
         if (this.handleSearchKeyInput(input)) {
            this.searchActive = false;
            return true;
         } else if (input.getKeycode() == 259) {
            this.searchQuery = this.removeLastCodePoint(this.searchQuery);
            return true;
         } else if (input.isPaste()) {
            this.searchQuery = this.searchQuery + this.getClipboardText();
            return true;
         } else {
            return true;
         }
      } else if (this.listeningString != null && this.handleStringKeyInput(input)) {
         return true;
      } else {
         return this.stringListAddActive && this.expandedStringListSetting != null && this.handleFriendsAddKeyInput(input) ? true : super.keyPressed(input);
      }
   }

   private boolean handleFriendsAddKeyInput(KeyInput input) {
      if (input.getKeycode() == 259) {
         this.stringListAddBuffer = this.removeLastCodePoint(this.stringListAddBuffer == null ? "" : this.stringListAddBuffer);
         return true;
      } else if (input.isPaste()) {
         this.stringListAddBuffer = (this.stringListAddBuffer == null ? "" : this.stringListAddBuffer) + this.getClipboardText();
         return true;
      } else if (input.isEscape()) {
         this.stringListAddActive = false;
         this.stringListAddBuffer = "";
         return true;
      } else if (!input.isEnter()) {
         return true;
      } else {
         if (this.expandedStringListSetting != null) {
            KeyInput var6 = this.parseStringList(this.expandedStringListSetting);
            String var2 = this.stringListAddBuffer == null ? "" : this.stringListAddBuffer.trim();
            if (!var2.isEmpty()) {
               var2 = var2.toLowerCase(Locale.ROOT);
               boolean var3 = false;

               for (String var5 : var6) {
                  if (var5.equalsIgnoreCase(var2)) {
                     var3 = true;
                     break;
                  }
               }

               if (!var3) {
                  var6.add(var2);
                  this.setStringListFromLowerList(this.expandedStringListSetting, var6);
               }
            }
         }

         this.stringListAddBuffer = "";
         return true;
      }
   }

   private boolean matchesQuery(Module module) {
      if (module instanceof ConfigShare) {
         return false;
      } else {
         return this.searchQuery.isBlank() ? true : module.getName().toLowerCase().contains(this.searchQuery.trim().toLowerCase());
      }
   }

   private String getBindLabel(Module module) {
      Module var2 = this.getKeyDisplayName(module.getBind());
      return "None".equals(var2) ? "" : var2;
   }

   private String getKeyDisplayName(int keyCode) {
      return getKeyDisplayNameStatic(keyCode);
   }

   public static String getKeyDisplayNameStatic(int keyCode) {
      if (keyCode == 0) {
         return "None";
      } else {
         String var1 = GLFW.glfwGetKeyName(keyCode, 0);
         if (var1 != null && !var1.isBlank()) {
            return normalizeKeyName(var1);
         } else {
            return switch (keyCode) {
               case 32 -> "Space";
               case 256 -> "Esc";
               case 257 -> "Enter";
               case 258 -> "Tab";
               case 259 -> "Backspace";
               case 260 -> "Insert";
               case 261 -> "Delete";
               case 262 -> "Right";
               case 263 -> "Left";
               case 264 -> "Down";
               case 265 -> "Up";
               case 266 -> "Page Up";
               case 267 -> "Page Down";
               case 268 -> "Home";
               case 269 -> "End";
               case 280 -> "Caps";
               case 340 -> "LShift";
               case 341 -> "LCtrl";
               case 342 -> "LAlt";
               case 344 -> "RShift";
               case 345 -> "RCtrl";
               case 346 -> "RAlt";
               default -> "Key " + keyCode;
            };
         }
      }
   }

   private static String normalizeKeyName(String value) {
      if (value == null) {
         return "";
      } else {
         value = value.trim();
         if (value.isEmpty()) {
            return "";
         } else {
            String var1 = value.toLowerCase();

            return switch (var1) {
               case "right shift" -> "RShift";
               case "left shift" -> "LShift";
               case "right control", "right ctrl" -> "RCtrl";
               case "left control", "left ctrl" -> "LCtrl";
               case "right alt" -> "RAlt";
               case "left alt" -> "LAlt";
               case "escape" -> "Esc";
               case "caps lock" -> "Caps";
               case "page up" -> "Page Up";
               case "page down" -> "Page Down";
               default -> value.length() == 1 ? value.toUpperCase() : value;
            };
         }
      }
   }

   private void drawModeSetting(DrawContext context, ModeSetting setting, int rowX, int rowWidth, int rowY, float revealAlpha) {
      String var7 = setting.getName();
      ModeSetting var13 = setting.getValue();
      int var8 = this.fontWidth(var13) + 12;
      rowWidth = rowX + rowWidth - 10 - var8;
      int var9 = rowY + 4;
      rowX += 10;
      int var10 = Math.max(0, rowWidth - 4 - rowX);
      var7 = var7;
      if (this.fontWidth(var7) > var10) {
         String var11 = "...";
         int var12 = this.fontWidth(var11);

         while (var7.length() > 0 && this.fontWidth(var7) + var12 > var10) {
            var7 = var7.substring(0, var7.length() - 1);
         }

         var7 = var7 + var11;
      }

      this.drawStyledText(context, var7, rowX, var9, this.multiplyAlpha(-1, revealAlpha), false);
      GuiRenderer.a(context, (float)rowWidth, (float)(rowY + 2), (float)var8, 12.0F, 5.0F, this.multiplyAlpha(-15198181, revealAlpha), false);
      GuiRenderer.a(context, (float)rowWidth, (float)(rowY + 2), (float)var8, 12.0F, 5.0F, 1.0F, this.multiplyAlpha(0, revealAlpha), false);
      this.drawStyledText(context, var13, rowWidth + 6, var9, this.multiplyAlpha(COLOR_ACCENT, revealAlpha), false);
   }

   private void drawStringListEditor(DrawContext context, Setting<String> setting, int x, int y, int w, float alpha) {
      List var7 = this.parseStringList(setting);
      int var8 = Math.min(6, var7.size());
      int var9 = this.getStringListEditorExtraHeight(setting);
      GuiRenderer.a(
         context, (float)x, (float)y, (float)w, (float)var9, Math.max(0.0F, WaterPlus.getEffectiveRoundness() - 3.0F), this.multiplyAlpha(0, alpha), false
      );
      GuiRenderer.a(
         context,
         (float)x,
         (float)y,
         (float)w,
         (float)var9,
         Math.max(0.0F, WaterPlus.getEffectiveRoundness() - 3.0F),
         1.0F,
         this.multiplyAlpha(0, alpha),
         false
      );
      y = y;

      for (int var13 = 0; var13 < var8; var13++) {
         String var10 = (String)var7.get(var13);
         this.drawStyledText(context, var10, x + 10, y + 4, this.multiplyAlpha(-1, alpha), false);
         int var11 = x + w - 10 - 12;
         int var15 = y + 2;
         GuiRenderer.a(context, (float)var11, (float)var15, 12.0F, 12.0F, 4.0F, this.multiplyAlpha(-14011323, alpha), false);
         GuiRenderer.a(context, (float)var11, (float)var15, 12.0F, 12.0F, 4.0F, 1.0F, this.multiplyAlpha(0, alpha), false);
         this.drawStyledText(context, "x", var11 + 4, y + 4, this.multiplyAlpha(-1938838, alpha), false);
         y += 19;
      }

      String var14 = "Add: " + (this.stringListAddBuffer == null ? "" : this.stringListAddBuffer);
      if (this.stringListAddActive && this.expandedStringListSetting == setting) {
         var14 = var14 + "_";
      }

      this.drawInputTextClipped(context, x, y, w, 17.0F, var14, x + 10, y + 4, this.multiplyAlpha(-1073741825, alpha));
      int var16 = x + w - 10 - 12;
      int var17 = y + 2;
      GuiRenderer.a(context, (float)var16, (float)var17, 12.0F, 12.0F, 4.0F, this.multiplyAlpha(-15198181, alpha), false);
      GuiRenderer.a(context, (float)var16, (float)var17, 12.0F, 12.0F, 4.0F, 1.0F, this.multiplyAlpha(0, alpha), false);
      this.drawStyledText(context, "+", var16 + 4, y + 4, this.multiplyAlpha(COLOR_ACCENT, alpha), false);
   }

   private int getPanelHeight(Category category) {
      int var2 = 38;
      int var3 = 0;

      for (Module var5 : ModuleManager.INSTANCE.getModulesInCategory(category)) {
         if (this.matchesQuery(var5)) {
            var3++;
            var2 += 19;
            String var6 = category.name() + "/" + var5.getName();
            float var7 = this.animValues.getOrDefault(var6 + "/expand", var5.isExpanded() ? 1.0F : 0.0F);
            if (var7 > 0.001F) {
               var2 += Math.round(this.getModuleExpandedHeight(var5) * var7);
            }
         }
      }

      if (var3 == 0) {
         var2 += 19;
      }

      return var2;
   }

   private ClickGuiScreen.b buildBlockPickerLayout(float panelX, float panelWidth, float pickerY, BlocksSetting setting) {
      BlocksSetting var12 = this.getFilteredBlocks(setting).size();
      BlocksSetting var13 = Math.min(5, Math.max(1, var12));
      float var5 = panelX + 6.0F;
      float var6 = pickerY + 6.0F;
      float var7 = panelX + panelWidth - 30.0F - 6.0F;
      float var8 = Math.max(24.0F, var7 - var5 - 4.0F);
      float var9 = var6 + 16.0F + 6.0F;
      float var10 = panelWidth - 12.0F;
      BlocksSetting var14 = var13 * 18;
      float var11 = 28.0F + var14 + 6.0F;
      return new ClickGuiScreen.b(panelX, pickerY, panelWidth, var11, var5, var6, var8, 16.0F, var7, var6, 30.0F, 16.0F, var5, var9, var10, var14);
   }

   private List<Block> getFilteredBlocks(BlocksSetting setting) {
      ArrayList var2 = new ArrayList<>(setting.filter(this.blockSearchQuery));
      var2.sort(Comparator.<Block, Boolean>comparing(b -> !setting.contains(b)).thenComparing(setting::getDisplayName, String.CASE_INSENSITIVE_ORDER));
      return var2;
   }

   private String buildBlocksPreviewText(BlocksSetting setting) {
      return "Choose";
   }

   private ItemStack getPreviewBlockStack(BlocksSetting setting) {
      BlocksSetting var2 = setting.getSelectedBlocks().stream().findFirst().orElse(null);
      if (var2 == null) {
         return ItemStack.EMPTY;
      } else {
         BlocksSetting var3 = new ItemStack(var2);
         return var3.isEmpty() ? ItemStack.EMPTY : var3;
      }
   }

   private String trimWithEllipsis(String text, int maxWidth) {
      if (text != null && !text.isEmpty() && maxWidth > 0) {
         if (this.fontWidth(text) <= maxWidth) {
            return text;
         } else {
            int var3 = this.fontWidth("...");
            return var3 >= maxWidth ? this.fontTrimToWidth(text, maxWidth) : this.fontTrimToWidth(text, maxWidth - var3) + "...";
         }
      } else {
         return "";
      }
   }

   private String formatStringSettingValue(Module module, Setting<?> setting, String value) {
      if (value == null || value.isEmpty()) {
         return "";
      } else {
         return this.isCoordSnapperWebhookSetting(module, setting) ? this.abbreviateSensitiveSuffix(value, 10) : value;
      }
   }

   private boolean isCoordSnapperWebhookSetting(Module module, Setting<?> setting) {
      return module != null && setting != null && "CoordSnapper".equalsIgnoreCase(module.getName()) && setting.matchesName("Webhook");
   }

   private String abbreviateSensitiveSuffix(String value, int visibleChars) {
      value = value == null ? "" : value.trim();
      if (value.isEmpty()) {
         return "";
      } else {
         int var3 = Math.max(value.lastIndexOf(47), value.lastIndexOf(92));
         value = var3 >= 0 && var3 < value.length() - 1 ? value.substring(var3 + 1) : value;
         return value.length() <= visibleChars ? "..." + value : "..." + value.substring(value.length() - visibleChars);
      }
   }

   private void drawScaledText(DrawContext context, String text, float x, float y, float scale, int color) {
      if (text != null && !text.isEmpty()) {
         Matrix3x2fStack var7 = context.getMatrices();
         var7.pushMatrix();
         var7.translate(x, y);
         var7.scale(scale, scale);
         this.drawStyledText(context, text, 0, 0, color, false);
         var7.popMatrix();
      }
   }

   private void drawInputTextClipped(DrawContext context, float cx, float cy, float cw, float ch, String text, int tx, int ty, int color) {
      if (text == null) {
         text = "";
      }

      float var10 = (int)Math.max(0.0F, cw) - (tx - (int)cx) - 4;
      float var11 = var10 > 0 ? this.fontTrimToWidth(text, var10) : text;
      this.drawStyledText(context, var11, tx, ty, color, false);
   }

   private int getBlockPickerExtraHeight(BlocksSetting setting) {
      return Math.round(this.buildBlockPickerLayout(0.0F, 132.0F, 0.0F, setting).height);
   }

   private int clampBlockPickerScroll(int itemCount, int value) {
      return Math.max(0, Math.min(Math.max(0, itemCount - 5), value));
   }

   private ClickGuiScreen.b buildMobPickerLayout(float panelX, float panelWidth, float pickerY, MobsSetting setting) {
      MobsSetting var12 = this.getFilteredMobs(setting).size();
      MobsSetting var13 = Math.min(5, Math.max(1, var12));
      float var5 = panelX + 6.0F;
      float var6 = pickerY + 6.0F;
      float var7 = panelX + panelWidth - 30.0F - 6.0F;
      float var8 = Math.max(24.0F, var7 - var5 - 4.0F);
      float var9 = var6 + 16.0F + 6.0F;
      float var10 = panelWidth - 12.0F;
      MobsSetting var14 = var13 * 18;
      float var11 = 28.0F + var14 + 6.0F;
      return new ClickGuiScreen.b(panelX, pickerY, panelWidth, var11, var5, var6, var8, 16.0F, var7, var6, 30.0F, 16.0F, var5, var9, var10, var14);
   }

   private List<EntityType<?>> getFilteredMobs(MobsSetting setting) {
      ArrayList var2 = new ArrayList<>(setting.filter(this.mobSearchQuery));
      var2.sort(
         Comparator.<EntityType, Boolean>comparing(t -> !setting.contains((EntityType<?>)t))
            .thenComparing(setting::getDisplayName, String.CASE_INSENSITIVE_ORDER)
      );
      return var2;
   }

   private int getMobPickerExtraHeight(MobsSetting setting) {
      return Math.round(this.buildMobPickerLayout(0.0F, 132.0F, 0.0F, setting).height);
   }

   private int clampMobPickerScroll(int itemCount, int value) {
      return Math.max(0, Math.min(Math.max(0, itemCount - 5), value));
   }

   private String buildMobsPreviewText(MobsSetting setting) {
      EntityType var2 = setting.getSelectedMobs().stream().findFirst().orElse(null);
      if (var2 == null) {
         return "Choose";
      } else {
         int var3 = setting.size() - 1;
         return var3 > 0 ? setting.getDisplayName(var2) + " +" + var3 : setting.getDisplayName(var2);
      }
   }

   private ItemStack getPreviewMobStack(MobsSetting setting) {
      MobsSetting var2 = setting.getSelectedMobs().stream().findFirst().orElse(null);
      return var2 == null ? ItemStack.EMPTY : this.getMobStack(var2);
   }

   private ItemStack getMobStack(EntityType<?> type) {
      try {
         EntityType var3 = SpawnEggItem.forEntity(type);
         if (var3 != null) {
            return new ItemStack(var3);
         }
      } catch (Throwable var2) {
      }

      return new ItemStack(Items.EGG);
   }

   private void drawMobsSettingSummary(DrawContext context, MobsSetting setting, float panelX, float panelWidth, float rowY, int rowHeight, float revealAlpha) {
      int var15 = this.expandedMobsSetting == setting ? "v" : ">";
      int var8 = this.fontWidth(var15);
      float var14 = Math.round(panelX + panelWidth - 6.0F - var8);
      var8 = Math.max(30, var14 - (Math.round(panelX) + 10 + this.fontWidth(setting.getName()) + 14));
      int var9 = this.multiplyAlpha(this.expandedMobsSetting != setting && setting.size() <= 0 ? -1073741825 : -1, revealAlpha);
      String var10 = setting.size() == 0 ? "Choose" : this.buildMobsPreviewText(setting);
      var10 = this.trimWithEllipsis(var10, Math.round((var8 - 18) / 0.9F));
      var8 = Math.max(34, Math.min(var8, this.fontWidth(var10) + 22));
      int var11 = var14 - var8 - 6;
      int var12 = this.multiplyAlpha(setting.size() > 0 ? COLOR_ACCENT_DIM : -15198181, revealAlpha);
      ItemStack var13 = this.getPreviewMobStack(setting);
      this.drawStyledText(context, setting.getName(), Math.round(panelX) + 10, Math.round(rowY) + 4, var9, false);
      GuiRenderer.a(context, (float)var11, rowY + 2.0F, (float)var8, 12.0F, 5.0F, var12, false);
      GuiRenderer.a(context, (float)var11, rowY + 2.0F, (float)var8, 12.0F, 5.0F, 1.0F, this.multiplyAlpha(0, revealAlpha), false);
      if (!var13.isEmpty()) {
         context.drawItem(var13, var11 + 2, Math.round(rowY) + 1);
      }

      this.drawScaledText(context, var10, var11 + (var13.isEmpty() ? 6 : 16), rowY + 4.0F, 0.9F, this.multiplyAlpha(-1, revealAlpha));
      this.drawStyledText(context, var15, var14, Math.round(rowY) + 4, this.multiplyAlpha(-1073741825, revealAlpha), false);
   }

   private void drawMobsPicker(DrawContext context, MobsSetting setting, float panelX, float panelWidth, float pickerY, int mouseX, int mouseY) {
      float var15 = this.buildMobPickerLayout(panelX, panelWidth, pickerY, setting);
      float var16 = this.getFilteredMobs(setting);
      this.mobPickerScroll = this.clampMobPickerScroll(var16.size(), this.mobPickerScroll);
      GuiRenderer.a(context, var15.x, var15.y, var15.width, var15.height, 6.0F, -15198181, false);
      GuiRenderer.a(context, var15.x, var15.y, var15.width, var15.height, 6.0F, 1.0F, 0, false);
      float var17 = this.mobSearchActive && this.expandedMobsSetting == setting ? COLOR_ACCENT : -14671840;
      GuiRenderer.a(context, var15.searchX, var15.searchY, var15.searchWidth, var15.searchHeight, 5.0F, COLOR_PANEL_BG, false);
      GuiRenderer.a(context, var15.searchX, var15.searchY, var15.searchWidth, var15.searchHeight, 5.0F, 1.0F, var17, false);
      GuiRenderer.a(context, var15.clearX, var15.clearY, var15.clearWidth, var15.clearHeight, 5.0F, 0, false);
      GuiRenderer.a(context, var15.clearX, var15.clearY, var15.clearWidth, var15.clearHeight, 5.0F, 1.0F, 0, false);
      float var18 = this.mobSearchQuery.isEmpty() ? "Search mobs..." : this.mobSearchQuery;
      if (this.mobSearchActive && this.expandedMobsSetting == setting && System.currentTimeMillis() / 500L % 2L == 0L) {
         var18 = var18 + "_";
      }

      this.drawInputTextClipped(
         context,
         var15.searchX,
         var15.searchY,
         Math.max(0.0F, var15.searchWidth),
         Math.max(0.0F, var15.searchHeight),
         var18,
         Math.round(var15.searchX) + 6,
         Math.round(var15.searchY) + 4,
         this.mobSearchQuery.isEmpty() && !this.mobSearchActive ? -1073741825 : -1
      );
      this.drawStyledText(context, "Clear", Math.round(var15.clearX) + 4, Math.round(var15.clearY) + 4, -1073741825, false);
      if (var16.isEmpty()) {
         this.drawStyledText(context, "No mobs found", Math.round(var15.listX) + 6, Math.round(var15.listY) + 4, -1073741825, false);
      } else {
         float var19 = Math.min(5, var16.size());
         boolean var8 = var16.size() > var19;

         for (int var9 = 0; var9 < var19; var9++) {
            int var10 = this.mobPickerScroll + var9;
            if (var10 >= var16.size()) {
               break;
            }

            EntityType var11 = (EntityType)var16.get(var10);
            float var12 = var15.listY + var9 * 18;
            int var13 = mouseX >= var15.listX && mouseX <= var15.listX + var15.listWidth && mouseY >= var12 && mouseY <= var12 + 18.0F - 2.0F;
            boolean var14 = setting.contains(var11);
            GuiRenderer.a(context, var15.listX, var12, var15.listWidth, 16.0F, 5.0F, var14 ? 0 : (var13 ? 184549375 : 0), false);
            GuiRenderer.a(context, var15.listX, var12, var15.listWidth, 16.0F, 5.0F, 1.0F, 0, false);
            ItemStack var21 = this.getMobStack(var11);
            var13 = Math.round(var15.listX) + 5;
            if (!var21.isEmpty()) {
               context.drawItem(var21, Math.round(var15.listX) + 2, Math.round(var12) + 1);
               var13 += 16;
            }

            float var22 = var15.listX + var15.listWidth - 10.0F;
            this.drawScaledText(
               context,
               this.trimWithEllipsis(setting.getDisplayName(var11), Math.round((var22 - var13 - 4.0F) / 0.9F)),
               var13,
               var12 + 4.0F,
               0.9F,
               var14 ? COLOR_ACCENT : -1
            );
            GuiRenderer.a(context, var22, var12 + 5.0F, 6.0F, 6.0F, 2.5F, var14 ? COLOR_ACCENT : -15198181, false);
            GuiRenderer.a(context, var22, var12 + 5.0F, 6.0F, 6.0F, 2.5F, 1.0F, var14 ? COLOR_ACCENT : -14671840, false);
         }

         if (var8) {
            int var20 = Math.max(1, var16.size() - var19);
            float var23 = var15.listX + var15.listWidth - 4.0F;
            float var24 = var15.listY + 1.0F;
            float var25 = var15.listHeight - 2.0F;
            float var27 = Math.max(12.0F, var25 * ((float)var19 / var16.size()));
            float var28 = (var25 - var27) * ((float)this.mobPickerScroll / var20);
            GuiRenderer.a(context, var23, var24, 4.0F, var25, 2.0F, COLOR_PANEL_BG, false);
            GuiRenderer.a(context, var23, var24 + var28, 4.0F, var27, 2.0F, COLOR_ACCENT_DIM, false);
         }
      }
   }

   private ClickGuiScreen.d buildColorPickerLayout(float panelX, float panelWidth, float rowY, int rowHeight) {
      panelX += 4.0F;
      panelWidth = rowY + rowHeight + 6.0F;
      rowY = panelX + 80.0F + 6.0F;
      return new ClickGuiScreen.d(panelX, panelWidth, 80.0F, 80.0F, rowY, 24.0F);
   }

   private boolean updateActiveColorDrag(double mouseX, double mouseY) {
      int var5 = this.getContentTop() + this.verticalScroll;

      for (int var6 = 0; var6 < CACHED_CATEGORIES.length; var6++) {
         Category var7 = CACHED_CATEGORIES[var6];
         int var8 = this.getCategoryX(var7, var6);
         int var9 = var5 + 22 + 6;

         for (Module var10 : ModuleManager.INSTANCE.getModulesInCategory(var7)) {
            if (this.matchesQuery(var10)) {
               var9 += 19;
               if (var10.isExpanded()) {
                  var9 += 19;
                  var9 += 19;

                  for (Setting var11 : var10.getSettings()) {
                     if (var11 == this.activeColorSetting && var11.getValue() instanceof Color) {
                        ClickGuiScreen.d var17 = this.buildColorPickerLayout(var8 + 4, 132.0F, var9, 17);
                        if (this.colorDragMode == ClickGuiScreen.c.FIELD) {
                           this.updateColorFromField(var11, var17, mouseX, mouseY);
                        } else if (this.colorDragMode == ClickGuiScreen.c.ALPHA) {
                           this.updateColorFromAlpha(var11, var17, mouseY);
                        }

                        return true;
                     }

                     var9 += 19;
                     if (var11 instanceof BlocksSetting var12 && this.expandedBlocksSetting == var12) {
                        var9 += this.getBlockPickerExtraHeight(var12);
                     }

                     if (var11 instanceof MobsSetting var16 && this.expandedMobsSetting == var16) {
                        var9 += this.getMobPickerExtraHeight(var16);
                     }

                     if (var11.getValue() instanceof Color && this.expandedColorSetting == var11) {
                        var9 += 112;
                     }
                  }
               }
            }
         }
      }

      return false;
   }

   private ClickGuiScreen.e getExpandedMobsPickerContext() {
      if (this.expandedMobsSetting == null) {
         return null;
      } else {
         int var1 = this.getContentTop() + this.verticalScroll;

         for (int var2 = 0; var2 < CACHED_CATEGORIES.length; var2++) {
            Category var3 = CACHED_CATEGORIES[var2];
            int var4 = this.getCategoryX(var3, var2);
            int var5 = var1 + 22 + 6;

            for (Module var6 : ModuleManager.INSTANCE.getModulesInCategory(var3)) {
               if (this.matchesQuery(var6)) {
                  var5 += 19;
                  if (var6.isExpanded()) {
                     var5 += 19;
                     var5 += 19;

                     for (Setting var7 : var6.getSettings()) {
                        if (var7 == this.expandedMobsSetting) {
                           return new ClickGuiScreen.e(
                              this.expandedMobsSetting, this.buildMobPickerLayout(var4 + 4, 132.0F, var5 + 19, this.expandedMobsSetting)
                           );
                        }

                        var5 += 19;
                        if (var7 instanceof BlocksSetting var8 && this.expandedBlocksSetting == var8) {
                           var5 += this.getBlockPickerExtraHeight(var8);
                        }

                        if (var7 instanceof MobsSetting var12 && this.expandedMobsSetting == var12) {
                           var5 += this.getMobPickerExtraHeight(var12);
                        }

                        if (var7.getValue() instanceof Color && this.expandedColorSetting == var7) {
                           var5 += 112;
                        }
                     }
                  }
               }
            }
         }

         return null;
      }
   }

   private ClickGuiScreen.a getExpandedBlocksPickerContext() {
      if (this.expandedBlocksSetting == null) {
         return null;
      } else {
         int var1 = this.getContentTop() + this.verticalScroll;

         for (int var2 = 0; var2 < CACHED_CATEGORIES.length; var2++) {
            Category var3 = CACHED_CATEGORIES[var2];
            int var4 = this.getCategoryX(var3, var2);
            int var5 = var1 + 22 + 6;

            for (Module var6 : ModuleManager.INSTANCE.getModulesInCategory(var3)) {
               if (this.matchesQuery(var6)) {
                  var5 += 19;
                  if (var6.isExpanded()) {
                     var5 += 19;
                     var5 += 19;

                     for (Setting var7 : var6.getSettings()) {
                        if (var7 == this.expandedBlocksSetting) {
                           return new ClickGuiScreen.a(
                              this.expandedBlocksSetting, this.buildBlockPickerLayout(var4 + 4, 132.0F, var5 + 19, this.expandedBlocksSetting)
                           );
                        }

                        var5 += 19;
                        if (var7 instanceof BlocksSetting var8 && this.expandedBlocksSetting == var8) {
                           var5 += this.getBlockPickerExtraHeight(var8);
                        }

                        if (var7 instanceof MobsSetting var12 && this.expandedMobsSetting == var12) {
                           var5 += this.getMobPickerExtraHeight(var12);
                        }

                        if (var7.getValue() instanceof Color && this.expandedColorSetting == var7) {
                           var5 += 112;
                        }
                     }
                  }
               }
            }
         }

         return null;
      }
   }

   private boolean handleSearchKeyInput(KeyInput input) {
      return input.isEscape() || input.isEnter();
   }

   private boolean handleBlockSearchKeyInput(KeyInput input) {
      if (input.getKeycode() == 259) {
         this.blockSearchQuery = this.removeLastCodePoint(this.blockSearchQuery);
         this.blockPickerScroll = 0;
         return true;
      } else if (input.isPaste()) {
         this.blockSearchQuery = this.blockSearchQuery + this.getClipboardText();
         this.blockPickerScroll = 0;
         return true;
      } else if (!input.isEscape() && !input.isEnter()) {
         return true;
      } else {
         this.blockSearchActive = false;
         return true;
      }
   }

   private boolean handleMobSearchKeyInput(KeyInput input) {
      if (input.getKeycode() == 259) {
         this.mobSearchQuery = this.removeLastCodePoint(this.mobSearchQuery);
         this.mobPickerScroll = 0;
         return true;
      } else if (input.isPaste()) {
         this.mobSearchQuery = this.mobSearchQuery + this.getClipboardText();
         this.mobPickerScroll = 0;
         return true;
      } else if (!input.isEscape() && !input.isEnter()) {
         return true;
      } else {
         this.mobSearchActive = false;
         return true;
      }
   }

   private boolean handleStringKeyInput(KeyInput input) {
      if (input.getKeycode() == 259) {
         this.listeningString.setValue(this.removeLastCodePoint(this.listeningString.getValue()));
         return true;
      } else if (input.isPaste()) {
         this.listeningString.setValue(this.listeningString.getValue() + this.getClipboardText());
         return true;
      } else if (!input.isEscape() && !input.isEnter()) {
         return true;
      } else {
         this.listeningString = null;
         return true;
      }
   }

   private int getContentTop() {
      return 16;
   }

   private int getTallestPanelHeight() {
      int var1 = 0;

      for (Category var5 : CACHED_CATEGORIES) {
         var1 = Math.max(var1, this.getPanelHeight(var5));
      }

      return var1;
   }

   private int clampVerticalScroll(int value) {
      int var2 = Math.max(0, this.uiHeight() - this.getContentTop() - 16);
      var2 = Math.min(0, var2 - this.getTallestPanelHeight());
      return Math.max(var2, Math.min(0, value));
   }

   private String getClipboardText() {
      return this.sanitizeTextInput(MinecraftClient.getInstance().keyboard.getClipboard());
   }

   private String sanitizeTextInput(String input) {
      if (input != null && !input.isEmpty()) {
         StringBuilder var2 = new StringBuilder(input.length());
         input.codePoints().filter(cp -> !Character.isISOControl(cp)).forEach(var2::appendCodePoint);
         return var2.toString();
      } else {
         return "";
      }
   }

   private String removeLastCodePoint(String value) {
      return value != null && !value.isEmpty() ? value.substring(0, value.offsetByCodePoints(value.length(), -1)) : "";
   }

   private void updateColorFromField(Setting<Color> setting, ClickGuiScreen.d layout, double mouseX, double mouseY) {
      Color var7 = (Color)setting.getValue();
      float var8 = this.getHue(var7);
      double var11 = this.clamp01((float)((mouseX - layout.fieldX) / layout.fieldWidth));
      ClickGuiScreen.d var9 = 1.0F - this.clamp01((float)((mouseY - layout.fieldY) / layout.fieldHeight));
      ClickGuiScreen.d var10 = Color.HSBtoRGB(var8, var11, var9);
      setting.setValue(new Color(var10 >> 16 & 0xFF, var10 >> 8 & 0xFF, var10 & 0xFF, var7.getAlpha()));
   }

   private void updateColorFromAlpha(Setting<Color> setting, ClickGuiScreen.d layout, double mouseY) {
      ClickGuiScreen.d var5 = this.clamp01((float)((mouseY - layout.fieldY) / layout.fieldHeight));
      double var7 = (Color)setting.getValue();
      ClickGuiScreen.d var6 = Color.HSBtoRGB(var5, this.getSaturation(var7), this.getBrightness(var7));
      setting.setValue(new Color(var6 >> 16 & 0xFF, var6 >> 8 & 0xFF, var6 & 0xFF, var7.getAlpha()));
   }

   private boolean pointInRect(double x, double y, float rx, float ry, float rw, float rh) {
      return x >= rx && x <= rx + rw && y >= ry && y <= ry + rh;
   }

   private boolean allowDecimalForModule(Module module) {
      if (module == null) {
         return false;
      } else {
         Module var2 = module.getName() == null ? "" : module.getName().toLowerCase().replace(" ", "");
         return var2.equals("swingspeed")
            || var2.equals("freelook")
            || var2.equals("fastplace")
            || var2.equals("playeresp")
            || var2.equals("storageesp")
            || var2.equals("freecam")
            || var2.equals("holeesp")
            || var2.equals("jumpcircles")
            || var2.equals("autototem")
            || var2.equals("autoinvtotem")
            || var2.equals("hitbox")
            || var2.equals("anchormacro")
            || var2.equals("autocrystal")
            || var2.equals("doubleanchor")
            || var2.equals("triggerbot")
            || var2.equals("shieldbreaker")
            || var2.equals("spotifyhud")
            || var2.equals("water+")
            || var2.equals("hud")
            || var2.equals("spawnernotifier")
            || var2.equals("nametags");
      }
   }

   private void beginSettingDragBatch() {
      if (!this.batchingSettingDrag) {
         this.batchingSettingDrag = true;
         ModuleManager.INSTANCE.d();
      }
   }

   private void finishSettingDragBatch() {
      if (this.batchingSettingDrag) {
         this.batchingSettingDrag = false;
         ModuleManager.INSTANCE.e();
      }
   }

   private void updateNumericSetting(Module module, Setting<?> setting, double mouseX, int catX) {
      double var6 = Math.max(0.0, Math.min(1.0, (mouseX - (catX + 10)) / 120.0));
      Module var15 = this.allowDecimalForModule(module);
      if (setting.getValue() instanceof Float && setting.getMin() instanceof Float && setting.getMax() instanceof Float) {
         float var17 = (Float)setting.getMin();
         float var18 = (Float)setting.getMax();
         float var20 = (float)(var17 + (var18 - var17) * var6);
         if (!var15) {
            var20 = Math.round(var20);
         }

         setting.setValue(var20);
      } else if (setting.getValue() instanceof Integer && setting.getMin() instanceof Integer && setting.getMax() instanceof Integer) {
         int var16 = (Integer)setting.getMin();
         int var10 = (Integer)setting.getMax();
         int var19 = (int)Math.round(var16 + (var10 - var16) * var6);
         setting.setValue(Math.max(var16, Math.min(var10, var19)));
      } else {
         if (setting.getValue() instanceof Double && setting.getMin() instanceof Double && setting.getMax() instanceof Double) {
            double var9 = (Double)setting.getMin();
            double var11 = (Double)setting.getMax();
            double var13 = var9 + (var11 - var9) * var6;
            if (!var15) {
               var13 = Math.round(var13);
            }

            setting.setValue(var13);
         }
      }
   }

   private float clamp01(float value) {
      return Math.max(0.0F, Math.min(1.0F, value));
   }

   private int withAlpha(int color, float alpha) {
      float var3 = Math.max(0, Math.min(255, Math.round(alpha * 255.0F)));
      return color & 16777215 | var3 << 24;
   }

   private int multiplyAlpha(int color, float alphaMul) {
      int var3 = color >> 24 & 0xFF;
      float var4 = Math.max(0, Math.min(255, Math.round(var3 * alphaMul)));
      return color & 16777215 | var4 << 24;
   }

   private int multiplyAlpha(Color color, float alphaMul) {
      float var3 = Math.max(0, Math.min(255, Math.round(color.getAlpha() * alphaMul)));
      return var3 << 24 | color.getRed() << 16 | color.getGreen() << 8 | color.getBlue();
   }

   private float easeOutCubic(float t) {
      t = this.clamp01(t);
      return 1.0F - (float)Math.pow(1.0F - t, 3.0);
   }

   @Override
   public void renderBackground(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
   }

   static String _c35bbdbeb99() {
      return "W";
   }

   static {
      for (Category var3 : Category.values()) {
         CATEGORY_TEXTURES.put(var3, Identifier.of("water", "textures/gui/categories/" + var3.name().toLowerCase(Locale.ROOT) + ".png"));
      }
   }

   private record a(BlocksSetting setting, ClickGuiScreen.b layout) {
   }

   private static final class b {
      private final float x;
      private final float y;
      private final float width;
      private final float height;
      private final float searchX;
      private final float searchY;
      private final float searchWidth;
      private final float searchHeight;
      private final float clearX;
      private final float clearY;
      private final float clearWidth;
      private final float clearHeight;
      private final float listX;
      private final float listY;
      private final float listWidth;
      private final float listHeight;

      private b(
         float x,
         float y,
         float width,
         float height,
         float searchX,
         float searchY,
         float searchWidth,
         float searchHeight,
         float clearX,
         float clearY,
         float clearWidth,
         float clearHeight,
         float listX,
         float listY,
         float listWidth,
         float listHeight
      ) {
         this.x = x;
         this.y = y;
         this.width = width;
         this.height = height;
         this.searchX = searchX;
         this.searchY = searchY;
         this.searchWidth = searchWidth;
         this.searchHeight = searchHeight;
         this.clearX = clearX;
         this.clearY = clearY;
         this.clearWidth = clearWidth;
         this.clearHeight = clearHeight;
         this.listX = listX;
         this.listY = listY;
         this.listWidth = listWidth;
         this.listHeight = listHeight;
      }
   }

   private static enum c {
      NONE,
      FIELD,
      ALPHA;
   }

   private static final class d {
      private final float fieldX;
      private final float fieldY;
      private final float fieldWidth;
      private final float fieldHeight;
      private final float alphaY;
      private final float alphaHeight;

      private d(float fieldX, float fieldY, float fieldWidth, float fieldHeight, float alphaY, float alphaHeight) {
         this.fieldX = fieldX;
         this.fieldY = fieldY;
         this.fieldWidth = fieldWidth;
         this.fieldHeight = fieldHeight;
         this.alphaY = alphaY;
         this.alphaHeight = alphaHeight;
      }
   }

   private record e(MobsSetting setting, ClickGuiScreen.b layout) {
   }
}
