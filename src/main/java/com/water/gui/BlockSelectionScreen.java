package com.water.gui;

import com.water.module.Module;
import com.water.module.ModuleManager;
import com.water.module.modules.client.WaterPlus;
import com.water.module.modules.render.StorageESP;
import com.water.setting.BlocksSetting;
import com.water.utils.renderer.GuiRenderer;
import com.water.utils.renderer.WaterFontRenderer;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Consumer;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public final class BlockSelectionScreen extends Screen {
   private static final int W = 320;
   private static final int H = 300;
   private static final int PAD = 10;
   private static final int HEAD_H = 34;
   private static final int FOOT_H = 36;
   private static final int TAB_H = 24;
   private static final int SRCH_H = 22;
   private static final int CELL = 22;
   private static final int GAP = 3;
   private static final int COLS = 9;
   private static final int CP_W = 140;
   private static final int CP_H = 130;
   private static final int CP_SV = 90;
   private static final int CP_HUE = 12;
   private static final int CP_GAP = 6;
   private static final int C_BD_IN = -15327184;
   private static final int C_TEXT = -2234128;
   private static final int C_TEXT_DIM = -7824982;
   private static final int C_MUTED = -12298906;
   private static final int C_RED = -2539435;
   private static final int C_GRID_BG = -16183270;
   private static final int C_CELL_BG = -15656928;
   private static final int C_CELL_HOV = -15063498;
   private static final int C_CELL_SEL = -15785936;
   private static final int C_WHITE_10 = 285212671;
   private final Screen parent;
   private final Module module;
   private final BlocksSetting setting;
   private final Map<Block, Color> colorMap;
   private final Consumer<Map<Block, Color>> colorSaveCallback;
   private final Map<Block, Color> builtinColors = new LinkedHashMap<>();
   private final Set<Block> builtinBlocks = new LinkedHashSet<>();
   private final Set<Block> sel = new LinkedHashSet<>();
   private final List<Block> all = new ArrayList<>();
   private List<Block> filtered = new ArrayList<>();
   private String search = "";
   private boolean showSel = false;
   private int scroll = 0;
   private long openNs = 0L;
   private Block cpBlock = null;
   private boolean cpBuiltin = false;
   private int cpX;
   private int cpY;
   private boolean cpDragSV = false;
   private boolean cpDragHue = false;

   public BlockSelectionScreen(Screen parent, Module module, BlocksSetting setting) {
      this(parent, module, setting, new LinkedHashMap<>(), null);
   }

   public BlockSelectionScreen(
      Screen parent, Module module, BlocksSetting setting, Map<Block, Color> existingColors, Consumer<Map<Block, Color>> colorSaveCallback
   ) {
      super(Text.literal(""));
      this.parent = parent;
      this.module = module;
      this.setting = setting;
      this.colorMap = new LinkedHashMap<>(existingColors);
      this.colorSaveCallback = colorSaveCallback;
      if (module instanceof Screen var6) {
         this.builtinColors.putAll(var6.c());
         this.builtinBlocks.addAll(this.builtinColors.keySet());
      }

      this.sel.addAll(setting.getSelectedBlocks());
      this.all.addAll(setting.getAvailableBlocks());
      this.all.removeIf(b -> b == null || b == Blocks.AIR || new ItemStack(b).isEmpty());
      this.all.sort(Comparator.comparing(setting::getDisplayName, String.CASE_INSENSITIVE_ORDER));
      this.rebuild();
   }

   private int px() {
      return (this.width - 320) / 2;
   }

   private int py() {
      return (this.height - 300) / 2;
   }

   private int gx() {
      return this.px() + 10;
   }

   private int gy() {
      return this.py() + 34 + 24 + 22 + 6;
   }

   private int gw() {
      return 300;
   }

   private int gh() {
      return 172;
   }

   private int mxR() {
      return Math.max(1, this.gh() / 25);
   }

   private int totR() {
      return (int)Math.ceil(this.visibleList().size() / 9.0);
   }

   private int mxSc() {
      return Math.max(0, this.totR() - this.mxR());
   }

   private List<Block> visibleList() {
      return this.showSel ? this.selectedList() : this.filtered;
   }

   private List<Block> selectedList() {
      ArrayList var1 = new ArrayList<>(this.builtinBlocks);

      for (Block var3 : this.sel) {
         if (!this.builtinBlocks.contains(var3)) {
            var1.add(var3);
         }
      }

      return var1;
   }

   private void rebuild() {
      this.rebuild(false);
   }

   private void rebuild(boolean keep) {
      if (this.search.isBlank()) {
         this.filtered = new ArrayList<>(this.all);
      } else {
         String var2 = this.search.trim().toLowerCase();
         this.filtered = new ArrayList<>();

         for (Block var4 : this.all) {
            if (this.setting.getDisplayName(var4).toLowerCase().contains(var2)) {
               this.filtered.add(var4);
            }
         }
      }

      if (!keep) {
         this.scroll = 0;
      } else {
         this.scroll = Math.max(0, Math.min(this.mxSc(), this.scroll));
      }
   }

   private Color getColor(Block b) {
      return this.builtinColors.containsKey(b) ? this.builtinColors.get(b) : this.colorMap.computeIfAbsent(b, k -> this.randomColor());
   }

   private void setColor(Block b, Color c) {
      if (this.builtinColors.containsKey(b)) {
         this.builtinColors.put(b, c);
      } else {
         this.colorMap.put(b, c);
      }
   }

   private Color randomColor() {
      int var1 = Color.HSBtoRGB((float)Math.random(), 0.75F, 1.0F);
      return new Color(var1 >> 16 & 0xFF, var1 >> 8 & 0xFF, var1 & 0xFF, 200);
   }

   private boolean isSelected(Block b) {
      return this.builtinBlocks.contains(b) || this.sel.contains(b);
   }

   @Override
   public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
      if (this.openNs == 0L) {
         this.openNs = System.nanoTime();
      }

      deltaTicks = this.easeOut(Math.min(1.0F, (float)(System.nanoTime() - this.openNs) / 1.6E8F));
      int var5 = this.px();
      int var6 = this.py();
      int var7 = WaterPlus.getAccentARGB();
      int var8 = WaterPlus.getBackgroundARGB();
      float var9 = WaterPlus.getGuiRoundness();
      float var10 = Math.max(5.0F, var9 * 0.6F);
      float var11 = Math.max(4.0F, var9 * 0.4F);
      float var12 = WaterPlus.getGlassIntensity();
      context.fill(0, 0, this.width, this.height, this.as(-2013265920, deltaTicks));
      GuiRenderer.a(
         context, (float)(var5 - 4), (float)(var6 - 4), 328.0F, 308.0F, var9 + 3.0F, this.as(gc(var7 & 16777215, (int)(28.0F * deltaTicks)), 1.0F), false
      );
      GuiRenderer.a(context, (float)var5, (float)var6, 320.0F, 300.0F, var9, this.as(var8, deltaTicks), false);
      if (var12 > 0.01F) {
         GuiRenderer.a(context, (float)(var5 + 1), (float)(var6 + 1), 318.0F, 36.0F, var9, this.as(gc(16777215, (int)(16.0F * var12)), deltaTicks), false);
         GuiRenderer.a(context, (float)var5, (float)var6, 320.0F, 300.0F, var9, 1.0F, this.as(gc(16777215, (int)(55.0F * var12)), deltaTicks), false);
      } else {
         GuiRenderer.a(context, (float)var5, (float)var6, 320.0F, 300.0F, var9, 1.0F, this.as(var7 & 16777215 | 1426063360, deltaTicks), false);
      }

      GuiRenderer.a(context, (float)var5, (float)var6, 320.0F, 34.0F, var9, var9, 0.0F, 0.0F, false, this.as(gc(0, 55), deltaTicks));
      context.fill(var5, var6 + 34, var5 + 320, var6 + 34 + 1, this.as(-15327184, deltaTicks));
      GuiRenderer.a(context, (float)var5, (float)(var6 + 8), 3.0F, 18.0F, 1.5F, this.as(var7, deltaTicks), false);
      String var30 = (this.setting.getName() + " — " + (this.module == null ? "" : this.module.getName())).toUpperCase();
      this.ft(context, var30, var5 + 10 + 8, var6 + 10, this.as(-2234128, deltaTicks));
      var8 = this.builtinBlocks.size() + this.sel.size();
      this.ft(context, var8 + " selected", var5 + 10 + 8, var6 + 22, this.as(-7824982, deltaTicks));
      var8 = var6 + 34 + 4;
      int var37 = var5 + 10;
      int var13 = var37 + 145 + 10;
      this.drawTab(context, var37, var8, 145, 18, "ALL", !this.showSel, this.hov(mouseX, mouseY, var37, var8, 145, 18), deltaTicks, var7);
      this.drawTab(
         context,
         var13,
         var8,
         145,
         18,
         "SELECTED (" + (this.builtinBlocks.size() + this.sel.size()) + ")",
         this.showSel,
         this.hov(mouseX, mouseY, var13, var8, 145, 18),
         deltaTicks,
         var7
      );
      if (!this.showSel) {
         var8 = var6 + 34 + 24 + 2;
         boolean var38 = !this.search.isEmpty();
         GuiRenderer.a(context, (float)var37, (float)var8, 300.0F, 20.0F, var10, this.as(-16183270, deltaTicks), false);
         GuiRenderer.a(
            context, (float)var37, (float)var8, 300.0F, 20.0F, var10, 1.0F, this.as(var38 ? var7 & 16777215 | 1711276032 : -15327184, deltaTicks), false
         );
         this.ft(
            context, var38 ? "⌕  " + this.search + "_" : "⌕  Search blocks...", var37 + 8, var8 + 11 - 5, this.as(var38 ? -2234128 : -12298906, deltaTicks)
         );
      } else {
         var8 = var6 + 34 + 24 + 2;
         this.ft(context, "Right-click a block to change its color", var37 + 4, var8 + 11 - 5, this.as(-12298906, deltaTicks));
      }

      List var35 = this.visibleList();
      var13 = this.gx();
      int var14 = this.gy();
      int var15 = this.gw();
      int var16 = this.gh();
      GuiRenderer.a(context, (float)(var13 - 3), (float)(var14 - 3), (float)(var15 + 6), (float)(var16 + 6), var10, this.as(-16183270, deltaTicks), false);
      String var36 = null;
      int var17 = 0;
      int var18 = 0;
      int var19 = this.mxR();

      for (int var20 = 0; var20 < var19; var20++) {
         for (int var21 = 0; var21 < 9; var21++) {
            int var22 = (var20 + this.scroll) * 9 + var21;
            if (var22 >= var35.size()) {
               break;
            }

            Block var23 = (Block)var35.get(var22);
            var22 = var13 + var21 * 25;
            int var24 = var14 + var20 * 25;
            boolean var25 = this.isSelected(var23);
            boolean var26 = this.builtinBlocks.contains(var23);
            boolean var27 = this.hov(mouseX, mouseY, var22, var24, 22, 22);
            GuiRenderer.a(
               context, (float)var22, (float)var24, 22.0F, 22.0F, var11, this.as(var25 ? -15785936 : (var27 ? -15063498 : -15656928), deltaTicks), false
            );
            if (var25) {
               Color var53 = this.getColor(var23);
               int var28 = (int)(50.0F * deltaTicks) << 24 | var53.getRed() << 16 | var53.getGreen() << 8 | var53.getBlue();
               GuiRenderer.a(context, (float)var22, (float)var24, 22.0F, 22.0F, var11, var28, false);
               var28 = (int)(255.0F * deltaTicks) << 24 | var53.getRed() << 16 | var53.getGreen() << 8 | var53.getBlue();
               GuiRenderer.a(context, (float)var22, (float)var24, 22.0F, 22.0F, var11, var23 == this.cpBlock ? 2.0F : 1.5F, var28, false);
               GuiRenderer.a(context, (float)(var22 + 22 - 7), (float)(var24 + 22 - 7), 6.0F, 6.0F, 3.0F, this.as(-16777216, deltaTicks), false);
               GuiRenderer.a(
                  context,
                  (float)(var22 + 22 - 6),
                  (float)(var24 + 22 - 6),
                  4.0F,
                  4.0F,
                  2.0F,
                  this.as(0xFF000000 | var53.getRed() << 16 | var53.getGreen() << 8 | var53.getBlue(), deltaTicks),
                  false
               );
               if (var26) {
                  GuiRenderer.a(context, (float)(var22 + 1), (float)(var24 + 1), 5.0F, 5.0F, 2.0F, this.as(var7 & 16777215 | -1442840576, deltaTicks), false);
               }
            } else if (var27) {
               GuiRenderer.a(context, (float)var22, (float)var24, 22.0F, 22.0F, var11, 1.0F, this.as(var7 & 16777215 | 1140850688, deltaTicks), false);
            }

            ItemStack var54 = new ItemStack(var23);
            if (!var54.isEmpty()) {
               context.drawItem(var54, var22 + 3, var24 + 3);
            }

            if (var27) {
               var36 = this.setting.getDisplayName(var23) + (var26 ? " [built-in]" : "");
               var17 = var22;
               var18 = var24;
            }
         }
      }

      if (var36 != null) {
         String var40 = var36 + "  [RMB: color]";
         int var43 = this.fw(var40) + 10;
         int var47 = Math.min(var17, var13 + var15 - var43);
         int var50 = var18 - 15;
         if (var50 < var14) {
            var50 = var18 + 22 + 2;
         }

         GuiRenderer.a(context, (float)var47, (float)var50, (float)var43, 13.0F, var11, this.as(-16117736, deltaTicks), false);
         GuiRenderer.a(context, (float)var47, (float)var50, (float)var43, 13.0F, var11, 1.0F, this.as(-15327184, deltaTicks), false);
         this.ft(context, var40, var47 + 5, var50 + 2, this.as(-2234128, deltaTicks));
      }

      if (var35.isEmpty()) {
         this.ft(context, "No blocks", var13 + var15 / 2 - this.fw("No blocks") / 2, var14 + var16 / 2 - 5, this.as(-12298906, deltaTicks));
      }

      if (this.totR() > this.mxR()) {
         int var41 = var13 + var15 + 2;
         float var44 = Math.max(16.0F, (float)(var16 * this.mxR()) / this.totR());
         float var48 = var14 + (var16 - var44) * ((float)this.scroll / Math.max(1, this.mxSc()));
         GuiRenderer.a(context, (float)var41, (float)var14, 3.0F, (float)var16, 1.5F, this.as(-15327184, deltaTicks), false);
         GuiRenderer.a(context, (float)var41, (float)((int)var48), 3.0F, (float)((int)var44), 1.5F, this.as(var7 & 16777215 | -1442840576, deltaTicks), false);
      }

      int var42 = var6 + 300 - 36;
      context.fill(var5, var42, var5 + 320, var42 + 1, this.as(-15327184, deltaTicks));
      GuiRenderer.a(context, (float)var5, (float)var42, 320.0F, 36.0F, 0.0F, 0.0F, var9, var9, false, this.as(gc(0, 50), deltaTicks));
      int var45 = var42 + 7;
      boolean var51 = this.hov(mouseX, mouseY, var37, var45, 76, 22);
      GuiRenderer.a(context, (float)var37, (float)var45, 76.0F, 22.0F, 11.0F, this.as(var51 ? 869875797 : 349782101, deltaTicks), false);
      GuiRenderer.a(context, (float)var37, (float)var45, 76.0F, 22.0F, 11.0F, 1.0F, this.as(-2539435, deltaTicks * (var51 ? 0.9F : 0.4F)), false);
      this.ft(context, "CLEAR ALL", var37 + (76 - this.fw("CLEAR ALL")) / 2, var45 + 7, this.as(-2539435, deltaTicks));
      int var49 = var5 + 320 - 10 - 66 - 10 - 66;
      boolean var52 = this.hov(mouseX, mouseY, var49, var45, 66, 22);
      GuiRenderer.a(context, (float)var49, (float)var45, 66.0F, 22.0F, 11.0F, this.as(var52 ? 419430399 : 150994943, deltaTicks), false);
      GuiRenderer.a(context, (float)var49, (float)var45, 66.0F, 22.0F, 11.0F, 1.0F, this.as(-15327184, deltaTicks), false);
      this.ft(context, "CANCEL", var49 + (66 - this.fw("CANCEL")) / 2, var45 + 7, this.as(-7824982, deltaTicks));
      int var55 = var5 + 320 - 10 - 66;
      boolean var56 = this.hov(mouseX, mouseY, var55, var45, 66, 22);
      GuiRenderer.a(context, (float)var55, (float)var45, 66.0F, 22.0F, 11.0F, this.as(var56 ? var7 : var7 & 16777215 | 570425344, deltaTicks), false);
      GuiRenderer.a(context, (float)var55, (float)var45, 66.0F, 22.0F, 11.0F, 1.5F, this.as(var7, deltaTicks * (var56 ? 1.0F : 0.5F)), false);
      this.ft(context, "SAVE", var55 + (66 - this.fw("SAVE")) / 2, var45 + 7, this.as(var56 ? -16777216 : var7, deltaTicks));
      if (this.cpBlock != null) {
         this.drawColorPicker(context, mouseX, mouseY, deltaTicks);
      }
   }

   private void drawColorPicker(DrawContext ctx, int mx, int my, float a) {
      mx = Math.min(this.cpX, this.px() + 320 - 140 - 6);
      if (mx < this.px() + 4) {
         mx = this.px() + 4;
      }

      my = Math.min(this.cpY, this.py() + 300 - 130 - 6);
      if (my < this.py() + 34 + 4) {
         my = this.py() + 34 + 4;
      }

      Color var5 = this.getColor(this.cpBlock);
      float var6 = this.hsb(var5)[0];
      float var7 = this.hsb(var5)[1];
      float var8 = this.hsb(var5)[2];
      float var9 = WaterPlus.getGuiRoundness();
      int var10 = WaterPlus.getAccentARGB();
      GuiRenderer.a(ctx, (float)(mx - 2), (float)(my - 2), 144.0F, 134.0F, var9, this.as(gc(0, 80), a), false);
      GuiRenderer.a(ctx, (float)mx, (float)my, 140.0F, 130.0F, var9, this.as(WaterPlus.getBackgroundARGB(), a), false);
      GuiRenderer.a(ctx, (float)mx, (float)my, 140.0F, 130.0F, var9, 1.0F, this.as(var10 & 16777215 | -2013265920, a), false);
      int var19 = mx + 6;
      var10 = my + 6;
      int var11 = var19 + 90 + 6;
      float var10000 = 90.0F / 12.0F;
      var10000 = 90.0F / 12.0F;

      for (int var12 = 0; var12 < 12; var12++) {
         float var13 = 1.0F - var12 / 12.0F;

         for (int var14 = 0; var14 < 12; var14++) {
            float var15 = var14 / 12.0F;
            ctx.fill(
               (int)(var19 + var14 * 7.5F),
               (int)(var10 + var12 * 7.5F),
               (int)(var19 + (var14 + 1) * 7.5F),
               (int)(var10 + (var12 + 1) * 7.5F),
               0xFF000000 | Color.HSBtoRGB(var6, var15, var13) & 16777215
            );
         }
      }

      GuiRenderer.a(ctx, (float)var19, (float)var10, 90.0F, 90.0F, 3.0F, 1.0F, this.as(1157627903, a), false);
      int var21 = var19 + (int)(var7 * 90.0F);
      int var22 = var10 + (int)((1.0F - var8) * 90.0F);
      GuiRenderer.a(ctx, (float)(var21 - 4), (float)(var22 - 4), 8.0F, 8.0F, 4.0F, this.as(-2013265920, a), false);
      GuiRenderer.a(ctx, (float)(var21 - 4), (float)(var22 - 4), 8.0F, 8.0F, 4.0F, 2.0F, this.as(-1, a), false);

      for (int var23 = 0; var23 < 90; var23++) {
         float var25 = var23 / 90.0F;
         ctx.fill(var11, var10 + var23, var11 + 12, var10 + var23 + 1, 0xFF000000 | Color.HSBtoRGB(var25, 1.0F, 1.0F) & 16777215);
      }

      GuiRenderer.a(ctx, (float)var11, (float)var10, 12.0F, 90.0F, 3.0F, 1.0F, this.as(1157627903, a), false);
      int var24 = var10 + (int)(var6 * 90.0F);
      GuiRenderer.a(ctx, (float)(var11 - 2), (float)(var24 - 1), 16.0F, 3.0F, 1.5F, this.as(-1, a), false);
      int var26 = var10 + 90 + 6;
      int var18 = 0xFF000000 | var5.getRed() << 16 | var5.getGreen() << 8 | var5.getBlue();
      GuiRenderer.a(ctx, (float)var19, (float)var26, 52.0F, 10.0F, 3.0F, this.as(var18, a), false);
      GuiRenderer.a(ctx, (float)(var19 + 52 + 4), (float)var26, 52.0F, 10.0F, 3.0F, this.as(var18, a), false);
      GuiRenderer.a(ctx, (float)var19, (float)var26, 52.0F, 10.0F, 3.0F, 1.0F, this.as(1157627903, a), false);
      GuiRenderer.a(ctx, (float)(var19 + 52 + 4), (float)var26, 52.0F, 10.0F, 3.0F, 1.0F, this.as(1157627903, a), false);
      this.ft(ctx, this.setting.getDisplayName(this.cpBlock), mx + 6, my + 130 - 14, this.as(-7824982, a));
   }

   private float[] hsb(Color c) {
      return Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
   }

   private int cpSvX() {
      int var1 = Math.min(this.cpX, this.px() + 320 - 140 - 6);
      if (var1 < this.px() + 4) {
         var1 = this.px() + 4;
      }

      return var1 + 6;
   }

   private int cpSvY() {
      int var1 = Math.min(this.cpY, this.py() + 300 - 130 - 6);
      if (var1 < this.py() + 34 + 4) {
         var1 = this.py() + 34 + 4;
      }

      return var1 + 6;
   }

   private int cpHueX() {
      return this.cpSvX() + 90 + 6;
   }

   private void updateSV(int mx, int my) {
      if (this.cpBlock != null) {
         Color var3 = this.getColor(this.cpBlock);
         int var4 = Math.max(0.0F, Math.min(1.0F, (mx - this.cpSvX()) / 90.0F));
         int var6 = 1.0F - Math.max(0.0F, Math.min(1.0F, (my - this.cpSvY()) / 90.0F));
         mx = Color.HSBtoRGB(this.hsb(var3)[0], var4, var6);
         this.setColor(this.cpBlock, new Color(mx >> 16 & 0xFF, mx >> 8 & 0xFF, mx & 0xFF, var3.getAlpha()));
      }
   }

   private void updateHue(int my) {
      if (this.cpBlock != null) {
         Color var2 = this.getColor(this.cpBlock);
         int var3 = Math.max(0.0F, Math.min(1.0F, (my - this.cpSvY()) / 90.0F));
         my = Color.HSBtoRGB(var3, this.hsb(var2)[1], this.hsb(var2)[2]);
         this.setColor(this.cpBlock, new Color(my >> 16 & 0xFF, my >> 8 & 0xFF, my & 0xFF, var2.getAlpha()));
      }
   }

   @Override
   public boolean mouseClicked(Click click, boolean doubled) {
      int var3 = (int)click.x();
      int var4 = (int)click.y();
      int var5 = click.button();
      int var6 = this.px();
      int var7 = this.py();
      if (this.cpBlock != null) {
         int var22 = this.cpSvX();
         Click var13 = this.cpSvY();
         int var24 = this.cpHueX();
         if (var5 == 0) {
            if (this.hov(var3, var4, var22, var13, 90, 90)) {
               this.updateSV(var3, var4);
               this.cpDragSV = true;
               return true;
            }

            if (this.hov(var3, var4, var24, var13, 12, 90)) {
               this.updateHue(var4);
               this.cpDragHue = true;
               return true;
            }
         }

         int var25 = Math.min(this.cpX, var6 + 320 - 140 - 6);
         if (var25 < var6 + 4) {
            var25 = var6 + 4;
         }

         var22 = Math.min(this.cpY, var7 + 300 - 130 - 6);
         if (var22 < var7 + 34 + 4) {
            var22 = var7 + 34 + 4;
         }

         if (!this.hov(var3, var4, var25 - 2, var22 - 2, 144, 134)) {
            this.cpBlock = null;
            this.cpDragSV = false;
            this.cpDragHue = false;
         }

         return true;
      } else {
         int var8 = var7 + 34 + 4;
         int var9 = var6 + 10;
         int var10 = var9 + 145 + 10;
         if (this.hov(var3, var4, var9, var8, 145, 18)) {
            this.showSel = false;
            this.scroll = 0;
            this.rebuild();
            return true;
         } else if (this.hov(var3, var4, var10, var8, 145, 18)) {
            this.showSel = true;
            this.scroll = 0;
            this.rebuild();
            return true;
         } else if (var3 >= this.gx() && var3 < this.gx() + this.gw() && var4 >= this.gy() && var4 < this.gy() + this.gh()) {
            List var21 = this.visibleList();
            var7 = (var3 - this.gx()) / 25;
            Click var11 = (var4 - this.gy()) / 25 + this.scroll;
            doubled = (boolean)(var11 * 9 + var7);
            if (var7 < 9 && doubled >= 0 && doubled < var21.size()) {
               boolean var15 = (Block)var21.get(doubled);
               boolean var16 = this.builtinBlocks.contains(var15);
               if (var5 == 0 && !var16) {
                  if (this.sel.contains(var15)) {
                     this.sel.remove(var15);
                  } else {
                     this.sel.add(var15);
                     this.getColor(var15);
                  }

                  this.rebuild(true);
               } else if (var5 == 1 && this.isSelected(var15)) {
                  var4 = this.gx() + var7 * 25;
                  Click var12 = this.gy() + (var11 - this.scroll) * 25;
                  this.cpX = var4 + 22 + 4;
                  this.cpY = var12 - 4;
                  this.cpBlock = var15;
                  this.cpBuiltin = var16;
                  this.cpDragSV = false;
                  this.cpDragHue = false;
               }
            }

            return true;
         } else {
            var8 = var7 + 300 - 36;
            var7 = var8 + 7;
            if (this.hov(var3, var4, var9, var7, 76, 22)) {
               this.sel.clear();
               this.colorMap.clear();
               this.rebuild();
               return true;
            } else if (this.hov(var3, var4, var6 + 320 - 10 - 66 - 10 - 66, var7, 66, 22)) {
               this.client.setScreen(this.parent);
               return true;
            } else if (this.hov(var3, var4, var6 + 320 - 10 - 66, var7, 66, 22)) {
               this.saveAndClose();
               return true;
            } else {
               return super.mouseClicked(click, doubled);
            }
         }
      }
   }

   @Override
   public boolean mouseDragged(Click click, double offsetX, double offsetY) {
      int var6 = (int)click.x();
      int var7 = (int)click.y();
      if (this.cpDragSV) {
         this.updateSV(var6, var7);
         return true;
      } else if (this.cpDragHue) {
         this.updateHue(var7);
         return true;
      } else {
         return super.mouseDragged(click, offsetX, offsetY);
      }
   }

   @Override
   public boolean mouseReleased(Click click) {
      this.cpDragSV = false;
      this.cpDragHue = false;
      return super.mouseReleased(click);
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      if (mouseX >= this.gx() && mouseX < this.gx() + this.gw() && mouseY >= this.gy() && mouseY < this.gy() + this.gh()) {
         this.scroll = Math.max(0, Math.min(this.mxSc(), this.scroll + (verticalAmount > 0.0 ? -1 : 1)));
         return true;
      } else {
         return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
      }
   }

   @Override
   public boolean charTyped(CharInput input) {
      if (this.cpBlock != null) {
         return true;
      } else {
         String var2 = input.asString();
         if (var2 != null && !var2.isEmpty() && !this.showSel) {
            this.search = this.search + var2;
            this.rebuild();
            return true;
         } else {
            return super.charTyped(input);
         }
      }
   }

   @Override
   public boolean keyPressed(KeyInput input) {
      if (this.cpBlock != null) {
         if (input.isEscape()) {
            this.cpBlock = null;
         }

         return true;
      } else if (input.getKeycode() == 259 && !this.search.isEmpty()) {
         this.search = this.search.substring(0, this.search.length() - 1);
         this.rebuild();
         return true;
      } else if (input.isEscape()) {
         this.saveAndClose();
         return true;
      } else {
         return super.keyPressed(input);
      }
   }

   @Override
   public boolean shouldPause() {
      return false;
   }

   @Override
   public void renderBackground(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
   }

   private void drawTab(DrawContext ctx, int x, int y, int w, int h, String lbl, boolean active, boolean hov, float a, int acc) {
      hov = (boolean)(active ? this.as(acc & 16777215 | 436207616, a) : (hov ? this.as(285212671, a) : this.as(150994943, a)));
      int var11 = active ? this.as(acc & 16777215 | 1426063360, a) : this.as(-15327184, a);
      active = (boolean)(active ? this.as(acc, a) : this.as(-7824982, a));
      GuiRenderer.a(ctx, (float)x, (float)y, (float)w, (float)h, h / 2.0F, hov, false);
      GuiRenderer.a(ctx, (float)x, (float)y, (float)w, (float)h, h / 2.0F, 1.0F, var11, false);
      this.ft(ctx, lbl, x + (w - this.fw(lbl)) / 2, y + h / 2 - 5, active);
   }

   private void ft(DrawContext ctx, String s, int x, int y, int c) {
      WaterFontRenderer.INSTANCE.a(ctx, s, x, y, c);
   }

   private int fw(String s) {
      return WaterFontRenderer.INSTANCE.method_a_2(s);
   }

   private int as(int c, float a) {
      return Math.max(0, Math.min(255, (int)((c >> 24 & 0xFF) * a))) << 24 | c & 16777215;
   }

   private static int gc(int rgb, int alpha) {
      return Math.max(0, Math.min(255, alpha)) << 24 | rgb & 16777215;
   }

   private float easeOut(float t) {
      return 1.0F - (float)Math.pow(1.0F - Math.min(1.0F, t), 3.0);
   }

   private boolean hov(int mx, int my, int x, int y, int w, int h) {
      return mx >= x && mx <= x + w && my >= y && my <= y + h;
   }

   private void saveAndClose() {
      this.setting.setValue(new LinkedHashSet<>(this.sel));
      if (this.module instanceof StorageESP var5) {
         LinkedHashMap var8 = new LinkedHashMap<>(this.colorMap);

         for (Entry var4 : this.builtinColors.entrySet()) {
            var8.put((Block)var4.getKey(), (Color)var4.getValue());
         }

         var5.b(var8);
         ModuleManager.INSTANCE.f();
         this.client.setScreen(this.parent);
      } else {
         if (this.colorSaveCallback != null) {
            this.colorSaveCallback.accept(new LinkedHashMap<>(this.colorMap));
         }

         if (this.module instanceof StorageESP var1) {
            for (Entry var3 : this.builtinColors.entrySet()) {
               var1.a((Block)var3.getKey(), (Color)var3.getValue());
            }
         }

         ModuleManager.INSTANCE.f();
         this.client.setScreen(this.parent);
      }
   }
}
