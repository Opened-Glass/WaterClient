package com.water.module.modules.client;

import com.water.gui.ClickGuiScreen;
import com.water.module.Category;
import com.water.module.Module;
import com.water.module.ModuleManager;
import com.water.module.modules.donut.StaffDetector;
import com.water.setting.Setting;
import com.water.utils.renderer.Blur2DRenderer;
import com.water.utils.renderer.GuiRenderer;
import com.water.utils.renderer.WaterFontRenderer;
import java.awt.Color;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;

public final class Hud extends Module {
   private static Hud field_a_1;
   private final Setting<Boolean> g = new Setting<>("Watermark", true);
   private final Setting<Boolean> h = new Setting<>("Coordinates", true);
   private final Setting<Boolean> i = new Setting<>("Info", true);
   private final Setting<Boolean> j = new Setting<>("Module List", true);
   private final Setting<Boolean> k = new Setting<>("Potion Effects", true);
   private final Setting<Boolean> l = new Setting<>("Armor", true);
   private final Setting<Boolean> m = new Setting<>("Keybinds", true);
   private final Setting<Boolean> n = new Setting<>("Notifications", true);
   private final Setting<Boolean> o = new Setting<>("Radar", true);
   private final Setting<Boolean> p = new Setting<>("Spotify Queue", true);
   private final Setting<Float> q = new Setting<>("Opacity", 0.8F, 0.0F, 1.0F);
   private final Setting<Boolean> r = new Setting<>("Rainbow", false);
   private final Setting<Float> s = new Setting<>("Rainbow Speed", 2.0F, 0.1F, 10.0F);
   private final Setting<Integer> t = new Setting<>("Radar Size", 110, 60, 200);
   private final Setting<Integer> u = new Setting<>("Radar Range", 64, 16, 128);
   private final Setting<Boolean> v = new Setting<>("Radar Players", true);
   private final Setting<Boolean> w = new Setting<>("Radar Hostile", false);
   private final Setting<Boolean> x = new Setting<>("Radar Passive", false);
   private final Setting<Boolean> y = new Setting<>("Radar Rotate", true);
   private final Setting<Float> z = new Setting<>("HUD Scale", 1.0F, 0.5F, 3.0F);
   private static Color field_a_2 = new Color(65, 185, 255);
   private static long field_c_1 = 0L;
   private static List<Entity> field_b_1 = new ArrayList<>();
   private static long d = 0L;
   private static final EnumMap<Hud.a, int[]> field_a_3 = new EnumMap<>(Hud.a.class);
   private static final EnumMap<Hud.a, Float> field_b_2 = new EnumMap<>(Hud.a.class);
   private static final Map<Identifier, Identifier> field_a_4;
   private static final Set<Identifier> field_a_5;
   private static int field_c_2;

   public static int[] method_a_1(Hud.a el) {
      return field_a_3.computeIfAbsent(el, Hud::c);
   }

   public static void a(Hud.a el, int x, int y) {
      field_a_3.put(el, new int[]{x, y});
      ModuleManager.INSTANCE.c();
   }

   public static float method_a_2(Hud.a el) {
      return el == Hud.a.h ? SpotifyHud.method_b_1() : field_b_2.getOrDefault(el, 1.0F);
   }

   public static void a(Hud.a el, float scale) {
      if (el == Hud.a.h) {
         SpotifyHud.method_a_1(Math.max(0.5F, Math.min(3.0F, scale)));
      } else {
         field_b_2.put(el, Math.max(0.5F, Math.min(3.0F, scale)));
      }

      ModuleManager.INSTANCE.c();
   }

   public static int method_a_3(Hud.a el) {
      MinecraftClient var1 = MinecraftClient.getInstance();
      if (var1 == null) {
         return 100;
      } else {
         return switch (el) {
            case field_a_1 -> fw("Water +") + 20;
            case b -> fw("XYZ: -00000 / -256 / -00000") + 14;
            case c -> fw("999 FPS  •  999 ms  •  23:59:59") + 14;
            case d -> 120;
            case field_e_1 -> 110;
            case f -> fw(" 100%") + 22;
            case g -> 120;
            case h -> SpotifyHud.a();
            case i -> field_a_1 != null ? field_a_1.t.getValue() : 110;
            case j -> 180;
            case k -> 168;
         };
      }
   }

   public static int[] b(Hud.a el) {
      MinecraftClient var1 = MinecraftClient.getInstance();
      if (var1 != null && var1.player != null) {
         int[] var10 = method_a_1(el);

         return switch (el) {
            case field_a_1 -> new int[]{var10[0], var10[1], fw("WATER+") + 20, 20};
            case b -> new int[]{
               var10[0], var10[1], fw(String.format("%.0f X  %.0f Y  %.0f Z", var1.player.getX(), var1.player.getY(), var1.player.getZ())) + 14, 20
            };
            case c -> new int[]{var10[0], var10[1], fw(var1.getCurrentFps() + " FPS  •  0 ms  •  00:00:00") + 14, 20};
            case d -> method_a_3();
            case field_e_1 -> {
               ArrayList var9 = new ArrayList<>(var1.player.getStatusEffects());
               if (var9.isEmpty()) {
                  yield new int[]{var10[0], var10[1], method_a_3(el), 20};
               } else {
                  Hud.a var6 = 0;

                  for (StatusEffectInstance var13 : var9) {
                     int var14 = fw(method_a_1(var13)) + 14;
                     if (var14 > var6) {
                        var6 = var14;
                     }
                  }

                  yield new int[]{var10[0], var10[1], var6, var9.size() * 22};
               }
            }
            case f -> new int[]{var10[0], var10[1], 70, 90};
            case g -> {
               List var8 = method_a_1();
               if (var8.isEmpty()) {
                  yield new int[]{var10[0], var10[1], method_a_3(el), 20};
               } else {
                  Hud.a var5 = 0;

                  for (Module var4 : var8) {
                     int var12 = fw(var4.getName() + "  " + ClickGuiScreen.getKeyDisplayNameStatic(var4.getBind()).toUpperCase()) + 14;
                     if (var12 > var5) {
                        var5 = var12;
                     }
                  }

                  yield new int[]{var10[0], var10[1], var5, var8.size() * 22 + 22};
               }
            }
            case h -> new int[]{var10[0], var10[1], SpotifyHud.a(), SpotifyHud.method_b_2()};
            case i -> {
               int var7 = field_a_1 != null ? field_a_1.t.getValue() : 110;
               yield new int[]{var10[0], var10[1], var7, var7};
            }
            case j -> new int[]{var10[0], var10[1], 180, 100};
            case k -> new int[]{var10[0], var10[1], 168, 200};
         };
      } else {
         int[] var2 = method_a_1(el);
         return new int[]{var2[0], var2[1], method_a_3(el), 14};
      }
   }

   private static List<Module> method_a_1() {
      ArrayList var0 = new ArrayList();

      for (Module var2 : ModuleManager.INSTANCE.getModules()) {
         if (var2.getBind() != 0) {
            var0.add(var2);
         }
      }

      var0.sort(Comparator.comparing(Module::getName));
      return var0;
   }

   public static boolean method_a_4(Hud.a el) {
      if (field_a_1 != null && field_a_1.isEnabled()) {
         return switch (el) {
            case field_a_1 -> field_a_1.g.getValue();
            case b -> field_a_1.h.getValue();
            case c -> field_a_1.i.getValue();
            case d -> field_a_1.j.getValue();
            case field_e_1 -> field_a_1.k.getValue();
            case f -> field_a_1.l.getValue();
            case g -> field_a_1.m.getValue();
            case h -> SpotifyHud.isActive();
            case i -> field_a_1.o.getValue();
            case j -> true;
            case k -> true;
         };
      } else {
         return false;
      }
   }

   private static int[] c(Hud.a el) {
      MinecraftClient var1 = MinecraftClient.getInstance();
      int var2 = var1 != null ? var1.getWindow().getScaledWidth() : 800;

      return switch (el) {
         case field_a_1 -> new int[]{5, 5};
         case b -> new int[]{5, 30};
         case c -> new int[]{5, 55};
         case d -> new int[]{var2 - 8, 5};
         case field_e_1 -> new int[]{5, 80};
         case f -> new int[]{var2 - 70, 200};
         case g -> new int[]{5, 130};
         case h -> new int[]{5, 50};
         case i -> new int[]{var2 - 125, 10};
         case j -> new int[]{var2 - 190, 10};
         case k -> new int[]{var2 - 180, 200};
      };
   }

   public Hud() {
      super("Hud", Category.e);
      this.addSetting(this.g);
      this.addSetting(this.h);
      this.addSetting(this.i);
      this.addSetting(this.j);
      this.addSetting(this.k);
      this.addSetting(this.l);
      this.addSetting(this.m);
      this.addSetting(this.n);
      this.addSetting(this.q);
      this.addSetting(this.r);
      this.addSetting(this.s);
      this.addSetting(this.p);
      this.addSetting(this.o);
      this.addSetting(this.t);
      this.addSetting(this.u);
      this.addSetting(this.v);
      this.addSetting(this.w);
      this.addSetting(this.x);
      this.addSetting(this.y);
      this.addSetting(this.z);
      field_a_1 = this;
   }

   public static boolean e() {
      return field_a_1 != null && field_a_1.isEnabled() && field_a_1.p.getValue();
   }

   public static float method_a_2() {
      return field_a_1 == null ? 1.0F : field_a_1.z.getValue();
   }

   public static void a(DrawContext ctx) {
      if (field_a_1 != null && field_a_1.isEnabled()) {
         MinecraftClient var1 = MinecraftClient.getInstance();
         if (var1 != null && var1.player != null) {
            if (!(var1.currentScreen instanceof ClickGuiScreen)) {
               if (!var1.getDebugHud().shouldShowDebugHud()) {
                  long var2 = System.currentTimeMillis();
                  if (var2 != field_c_1) {
                     field_c_1 = var2;
                     field_a_2 = WaterPlus.getAccentColor();
                     GuiRenderer.a(ctx);
                  }

                  float var4 = field_a_1.q.getValue();
                  float var5 = WaterPlus.getGuiRoundness();
                  int var6 = field_a_1.r.getValue();
                  int var7 = field_a_1.s.getValue().intValue();
                  method_a_2();
                  Color var8 = WaterPlus.getBackgroundColor();
                  var8 = new Color(var8.getRed(), var8.getGreen(), var8.getBlue(), (int)(var4 * 255.0F));
                  float var9 = field_b_2.getOrDefault(Hud.a.field_a_1, 1.0F);
                  ctx.getMatrices().pushMatrix();
                  ctx.getMatrices().scale(var9, var9);
                  if (field_a_1.g.getValue()) {
                     int[] var99 = method_a_1(Hud.a.field_a_1);
                     Color var10 = var6 ? a(var7, 0) : field_a_2;
                     String var11 = "Water";
                     String var12 = "+";
                     int var13 = fw(var11);
                     int var14 = fw(var12);
                     int var15 = var13 + var14 + 16;
                     a(ctx, var8, var99[0], var99[1], var15, 20.0F, var5);
                     ft(ctx, var11, var99[0] + 7, var99[1] + 5, -1117449);
                     ft(ctx, var12, var99[0] + 7 + var13, var99[1] + 5, var10.getRGB());
                  }

                  ctx.getMatrices().popMatrix();
                  var9 = field_b_2.getOrDefault(Hud.a.b, 1.0F);
                  ctx.getMatrices().pushMatrix();
                  ctx.getMatrices().scale(var9, var9);
                  if (field_a_1.h.getValue()) {
                     int[] var101 = method_a_1(Hud.a.b);
                     Color var121 = var6 ? a(var7, 0) : field_a_2;
                     Color var130 = var6 ? a(var7, 40) : a(field_a_2);
                     Color var137 = var6 ? a(var7, 80) : a(field_a_2);
                     String var144 = String.format("%.0f", var1.player.getX());
                     String var157 = String.format("%.0f", var1.player.getY());
                     String var164 = String.format("%.0f", var1.player.getZ());
                     int var16 = fw(var144) + fw("  X  ") + fw(var157) + fw("  Y  ") + fw(var164) + fw("  Z") + 16;
                     a(ctx, var8, var101[0], var101[1], var16, 20.0F, var5);
                     int var17 = var101[0] + 7;
                     ft(ctx, var144, var17, var101[1] + 5, -1117449);
                     var17 += fw(var144);
                     ft(ctx, "  X", var17, var101[1] + 5, var121.getRGB());
                     var17 += fw("  X");
                     ft(ctx, "  " + var157, var17, var101[1] + 5, -1117449);
                     var17 += fw("  " + var157);
                     ft(ctx, "  Y", var17, var101[1] + 5, var130.getRGB());
                     var17 += fw("  Y");
                     ft(ctx, "  " + var164, var17, var101[1] + 5, -1117449);
                     var17 += fw("  " + var164);
                     ft(ctx, "  Z", var17, var101[1] + 5, var137.getRGB());
                  }

                  ctx.getMatrices().popMatrix();
                  var9 = field_b_2.getOrDefault(Hud.a.c, 1.0F);
                  ctx.getMatrices().pushMatrix();
                  ctx.getMatrices().scale(var9, var9);
                  if (field_a_1.i.getValue()) {
                     int[] var103 = method_a_1(Hud.a.c);
                     Color var122 = var6 ? a(var7, 0) : field_a_2;
                     Color var131 = var6 ? a(var7, 40) : a(field_a_2);
                     int var138 = var1.getCurrentFps();
                     int var145 = 0;

                     try {
                        if (var1.getNetworkHandler() != null) {
                           PlayerListEntry var158 = var1.getNetworkHandler().getPlayerListEntry(var1.player.getUuid());
                           if (var158 != null) {
                              var145 = var158.getLatency();
                           }
                        }
                     } catch (Exception var67) {
                     }

                     String var159 = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                     String var165 = var138 + " FPS";
                     String var171 = var145 + " ms";
                     String var185 = "  •  ";
                     int var18 = fw(var165) + fw(var185) + fw(var171) + fw(var185) + fw(var159) + 16;
                     a(ctx, var8, var103[0], var103[1], var18, 20.0F, var5);
                     var145 = var103[0] + 7;
                     ft(ctx, var165, var145, var103[1] + 5, -1117449);
                     var145 += fw(var165);
                     ft(ctx, var185, var145, var103[1] + 5, var122.getRGB());
                     var145 += fw(var185);
                     ft(ctx, var171, var145, var103[1] + 5, -1117449);
                     var145 += fw(var171);
                     ft(ctx, var185, var145, var103[1] + 5, var131.getRGB());
                     var145 += fw(var185);
                     ft(ctx, var159, var145, var103[1] + 5, -5260086);
                  }

                  ctx.getMatrices().popMatrix();
                  var9 = field_b_2.getOrDefault(Hud.a.field_e_1, 1.0F);
                  ctx.getMatrices().pushMatrix();
                  ctx.getMatrices().scale(var9, var9);
                  if (field_a_1.k.getValue()) {
                     ArrayList var105 = new ArrayList<>(var1.player.getStatusEffects());
                     if (!var105.isEmpty()) {
                        var105.sort(Comparator.comparingInt(e -> fw(method_a_1(e))));
                        int[] var123 = method_a_1(Hud.a.field_e_1);
                        int var132 = var123[1];

                        for (int var139 = 0; var139 < var105.size(); var139++) {
                           StatusEffectInstance var151 = (StatusEffectInstance)var105.get(var139);
                           String var160 = method_a_1(var151);
                           int var166 = fw(var160) + 16;
                           var151.getEffectType().value().getColor();
                           if (var6) {
                              a(var7, var139 * 15);
                           } else {
                              a(field_a_2, a(field_a_2), (float)var139 / Math.max(1, var105.size() - 1));
                           }

                           a(ctx, var8, var123[0], var132, var166, 20.0F, var5);
                           ft(ctx, var160, var123[0] + 8, var132 + 5, -5260086);
                           var132 += 23;
                        }
                     }
                  }

                  ctx.getMatrices().popMatrix();
                  var9 = field_b_2.getOrDefault(Hud.a.f, 1.0F);
                  ctx.getMatrices().pushMatrix();
                  ctx.getMatrices().scale(var9, var9);
                  if (field_a_1.l.getValue()) {
                     int[] var107 = method_a_1(Hud.a.f);
                     EquipmentSlot[] var124 = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
                     int var133 = var107[1];

                     for (EquipmentSlot var167 : var124) {
                        ItemStack var172 = var1.player.getEquippedStack(var167);
                        if (var172 != null && !var172.isEmpty()) {
                           int var186 = var172.isDamageable() && var172.getMaxDamage() > 0
                              ? (int)Math.round((1.0 - (double)var172.getDamage() / var172.getMaxDamage()) * 100.0)
                              : 100;
                           Color var191 = var186 >= 66 ? new Color(74, 222, 128) : (var186 >= 33 ? new Color(250, 204, 21) : new Color(239, 68, 68));
                           String var152 = var186 + "%";
                           int var19 = 16 + fw(var152) + 8;
                           a(ctx, var8, var107[0], var133, var19, 18.0F, var5);
                           ctx.drawItem(var172, var107[0] + 4, var133 + 1);
                           ft(ctx, var152, var107[0] + 22, var133 + 5, var191.getRGB());
                           var133 += 21;
                        }
                     }
                  }

                  ctx.getMatrices().popMatrix();
                  var9 = field_b_2.getOrDefault(Hud.a.g, 1.0F);
                  ctx.getMatrices().pushMatrix();
                  ctx.getMatrices().scale(var9, var9);
                  if (field_a_1.m.getValue()) {
                     List var109 = method_a_1();
                     if (!var109.isEmpty()) {
                        int[] var125 = method_a_1(Hud.a.g);
                        Color var134 = var6 ? a(var7, 0) : field_a_2;
                        String var141 = "Hotkeys";
                        int var153 = fw(var141) + 16;
                        a(ctx, var8, var125[0], var125[1], var153, 20.0F, var5);
                        ft(ctx, var141, var125[0] + 8, var125[1] + 5, var134.getRGB());
                        int var162 = var125[1] + 23;

                        for (int var168 = 0; var168 < var109.size(); var168++) {
                           Module var173 = (Module)var109.get(var168);
                           String var187 = titleCase(var173.getName());
                           String var192 = ClickGuiScreen.getKeyDisplayNameStatic(var173.getBind()).toUpperCase();
                           Color var154 = var6 ? a(var7, var168 * 15) : a(field_a_2, a(field_a_2), (float)var168 / Math.max(1, var109.size() - 1));
                           int var199 = fw(var187) + fw("  ") + fw(var192) + 16;
                           a(ctx, var8, var125[0], var162, var199, 18.0F, var5);
                           ft(ctx, var187, var125[0] + 8, var162 + 4, -5260086);
                           ft(ctx, var192, var125[0] + 8 + fw(var187) + fw("  "), var162 + 4, var154.getRGB());
                           var162 += 21;
                        }
                     }
                  }

                  ctx.getMatrices().popMatrix();
                  var9 = field_b_2.getOrDefault(Hud.a.d, 1.0F);
                  ctx.getMatrices().pushMatrix();
                  ctx.getMatrices().scale(var9, var9);
                  if (field_a_1.j.getValue()) {
                     int[] var111 = method_a_1(Hud.a.d);
                     ArrayList var126 = new ArrayList();

                     for (Module var142 : ModuleManager.INSTANCE.getModules()) {
                        if (var142.isEnabled() && var142.getCategory() != Category.e) {
                           var126.add(var142);
                        }
                     }

                     if (!var126.isEmpty()) {
                        var126.sort(Comparator.<Module>comparingInt(m -> fw(titleCase(m.getName()))).reversed());
                        String var169 = "Modules";
                        int var174 = fw(var169);

                        for (Module var193 : var126) {
                           var174 = Math.max(var174, fw(titleCase(var193.getName())));
                        }

                        int var189 = var174 + 14 + 4;
                        int var194 = 21 + var126.size() * 12 + 5;
                        int var155 = var111[0] - var189;
                        int var200 = var111[1];

                        for (int var112 = 3; var112 >= 1; var112--) {
                           GuiRenderer.a(
                              ctx,
                              (float)(var155 - var112),
                              (float)(var200 - var112 + 1),
                              (float)(var189 + var112 * 2),
                              (float)(var194 + var112 * 2),
                              var5 + var112,
                              10 - var112 * 2 << 24,
                              false
                           );
                        }

                        if (WaterPlus.menuBlurEnabled()) {
                           Blur2DRenderer.bm();
                           GuiRenderer.a(ctx, (float)var155, (float)var200, (float)var189, (float)var194, var5, 1.0F, false);
                        }

                        GuiRenderer.a(ctx, (float)var155, (float)var200, (float)var189, (float)var194, var5, var8.getRGB(), false);
                        GuiRenderer.a(ctx, (float)(var155 + 1), (float)(var200 + 1), (float)(var189 - 2), 18.0F, var5, 285212671, false);
                        GuiRenderer.a(ctx, (float)var155, (float)var200, (float)var189, (float)var194, var5, 1.0F, 721420287, false);
                        ft(ctx, var169, var155 + 7, var200 + 5, -1);
                        GuiRenderer.a(ctx, (float)var155, (float)(var200 + 18), (float)var189, 3.0F, 0.5F, 335544320, false);
                        int var113 = var200 + 18 + 3 + 2;

                        for (int var74 = 0; var74 < var126.size(); var74++) {
                           var8 = var6 ? a(var7, var74 * 15) : field_a_2;
                           ft(ctx, titleCase(((Module)var126.get(var74)).getName()), var155 + 7, var113, -1090519040 | var8.getRGB() & 16777215);
                           var113 += 12;
                        }
                     }
                  }

                  ctx.getMatrices().popMatrix();
                  var9 = field_b_2.getOrDefault(Hud.a.i, 1.0F);
                  ctx.getMatrices().pushMatrix();
                  ctx.getMatrices().scale(var9, var9);
                  if (field_a_1.o.getValue() && var1.world != null) {
                     int[] var115 = method_a_1(Hud.a.i);
                     int var127 = field_a_1.t.getValue();
                     int var136 = field_a_1.u.getValue();
                     float var143 = var127 / 2.0F;
                     float var156 = var115[0] + var143;
                     float var163 = var115[1] + var143;
                     float var170 = field_a_1.y.getValue() ? var1.player.getYaw() : 0.0F;
                     float var175 = var143 * 0.55F;
                     Color var190 = var6 ? a(var7, 0) : field_a_2;
                     Color var195 = field_a_2;
                     int var201 = (int)(var4 * 180.0F) << 24 | var195.getRed() << 16 | var195.getGreen() << 8 | var195.getBlue();
                     GuiRenderer.a(ctx, (float)var115[0], (float)var115[1], (float)var127, (float)var127, var127 / 2.0F, -871756784, false);
                     GuiRenderer.a(ctx, var156 - var175, var163 - var175, var175 * 2.0F, var175 * 2.0F, var175, 419430399, false);
                     GuiRenderer.a(ctx, (float)var115[0], (float)var115[1], (float)var127, (float)var127, var127 / 2.0F, 1.5F, var201, false);
                     GuiRenderer.b(ctx, var156 - 3.0F, var163 - 3.0F, 6.0F, 6.0F, 360.0F, 0.0F, -1996488705, false);
                     GuiRenderer.b(ctx, var156 - 1.5F, var163 - 1.5F, 3.0F, 3.0F, 360.0F, 0.0F, -1, false);
                     TextRenderer var75 = var1.textRenderer;
                     String[] var91 = new String[]{"N", "E", "S", "W"};
                     float[] var71 = new float[]{180.0F, 270.0F, 0.0F, 90.0F};

                     for (int var82 = 0; var82 < 4; var82++) {
                        float var87 = var71[var82] - var170;
                        var9 = (float)Math.toRadians(var87 - 90.0F);
                        float var128 = (float)(var156 + (var143 - 8.0F) * Math.cos(var9));
                        var9 = (float)(var163 + (var143 - 8.0F) * Math.sin(var9));
                        int var176 = var82 == 0 ? -1 : -1997606153;
                        ctx.drawText(var75, var91[var82], (int)(var128 - var75.getWidth(var91[var82]) / 2.0F), (int)(var9 - 4.0F), var176, false);
                     }

                     var6 = 0xFF000000 | var195.getRed() << 16 | var195.getGreen() << 8 | var195.getBlue();
                     GuiRenderer.b(ctx, var156 - 4.0F, var163 - 4.0F, 8.0F, 8.0F, 360.0F, 0.0F, var6, false);
                     GuiRenderer.b(ctx, var156 - 2.0F, var163 - 2.0F, 4.0F, 4.0F, 360.0F, 0.0F, -1, false);
                     float var88 = (var143 - 8.0F) / var136;
                     if (var2 - d >= 100L) {
                        d = var2;
                        field_b_1 = new ArrayList<>(
                           var1.world.getEntitiesByClass(Entity.class, var1.player.getBoundingBox().expand(var136, var136, var136), e -> e != var1.player)
                        );
                     }

                     for (Entity var119 : field_b_1) {
                        boolean var177 = var119 instanceof PlayerEntity;
                        boolean var72 = var119 instanceof HostileEntity;
                        boolean var76 = var119 instanceof PassiveEntity;
                        if ((!var177 || field_a_1.v.getValue())
                           && (!var72 || field_a_1.w.getValue())
                           && (!var76 || field_a_1.x.getValue())
                           && (var177 || var72 || var76)) {
                           double var36 = var119.getX() - var1.player.getX();
                           double var38 = var119.getZ() - var1.player.getZ();
                           if (!(Math.sqrt(var36 * var36 + var38 * var38) > var136)) {
                              var5 = (float)Math.toRadians(-var170);
                              float var84 = (float)Math.cos(var5);
                              var5 = (float)Math.sin(var5);
                              float var92 = (float)var36;
                              float var196 = (float)var38;
                              float var202 = var92 * var84 - var196 * var5;
                              var5 = -(var92 * var5 + var196 * var84);
                              float var85 = var156 + var202 * var88;
                              var5 = var163 + var5 * var88;
                              float var93 = (float)Math.sqrt((var85 - var156) * (var85 - var156) + (var5 - var163) * (var5 - var163));
                              if (var93 > var143 - 4.0F) {
                                 float var94 = (var143 - 4.0F) / var93;
                                 var85 = var156 + (var85 - var156) * var94;
                                 var5 = var163 + (var5 - var163) * var94;
                              }

                              if (var177) {
                                 var4 = var85 - 9.0F / 2.0F;
                                 var5 -= 9.0F / 2.0F;
                                 boolean var86 = false;
                                 StaffDetector var95 = (StaffDetector)ModuleManager.INSTANCE.getModuleByName("Staff Detector");
                                 if (var95 != null && var95.isEnabled()) {
                                    var86 = var95.a().containsKey(((PlayerEntity)var119).getName().getString());
                                 }

                                 AbstractClientPlayerEntity var96 = (AbstractClientPlayerEntity)var119;
                                 Identifier var120 = null;

                                 try {
                                    SkinTextures var97 = var96.getSkin();

                                    for (Method var20 : var97.getClass().getMethods()) {
                                       if (var20.getParameterCount() == 0) {
                                          var20.setAccessible(true);

                                          try {
                                             var204 = var20.invoke(var97);
                                          } catch (Exception var69) {
                                             continue;
                                          }

                                          if (var204 != null) {
                                             if (var204 instanceof Identifier) {
                                                var120 = (Identifier)var204;
                                                break;
                                             }

                                             try {
                                                for (Method var66 : var204.getClass().getMethods()) {
                                                   if (var66.getParameterCount() == 0 && var66.getReturnType() == Identifier.class) {
                                                      var66.setAccessible(true);
                                                      var120 = (Identifier)var66.invoke(var204);
                                                      if (var120 != null) {
                                                         break;
                                                      }
                                                   }
                                                }
                                             } catch (Exception var68) {
                                             }

                                             if (var120 != null) {
                                                break;
                                             }
                                          }
                                       }
                                    }
                                 } catch (Exception var70) {
                                 }

                                 Identifier var98 = a(var120);
                                 if (var98 != null) {
                                    GuiRenderer.a(ctx, var4 - 1.0F, var5 - 1.0F, 11.0F, 11.0F, 3.5F, -16118768, false);
                                    GuiRenderer.a(ctx, var4, var5, 9.0F, var98, -1, 0.0F, false);
                                    GuiRenderer.a(
                                       ctx,
                                       var4 - 1.0F,
                                       var5 - 1.0F,
                                       11.0F,
                                       11.0F,
                                       3.5F,
                                       1.0F,
                                       0xFF000000 | var190.getRed() << 16 | var190.getGreen() << 8 | var190.getBlue(),
                                       false
                                    );
                                 } else {
                                    GuiRenderer.a(
                                       ctx, var4, var5, 9.0F, 9.0F, 2.5F, 0xFF000000 | var190.getRed() << 16 | var190.getGreen() << 8 | var190.getBlue(), false
                                    );
                                 }

                                 if (var86) {
                                    var175 = (float)(0.5 + 0.5 * Math.sin(var2 / 250.0));
                                    int var198 = (int)(var175 * 150.0F);
                                    GuiRenderer.a(ctx, var4 - 2.0F, var5 - 2.0F, 13.0F, 13.0F, 2.0F, var198 << 24 | 16720418, false);
                                 }
                              } else {
                                 GuiRenderer.b(ctx, var85 - 2.0F, var5 - 2.0F, 4.0F, 4.0F, 360.0F, 0.0F, var72 ? -50116 : -11477936, false);
                              }
                           }
                        }
                     }

                     GuiRenderer.b(ctx, var156 - 2.5F, var163 - 2.5F, 5.0F, 5.0F, 360.0F, 0.0F, -1, false);
                     ctx.fill((int)(var156 - 1.0F), (int)(var163 - 6.0F), (int)(var156 + 1.0F), (int)(var163 - 2.0F), -1);
                  }

                  ctx.getMatrices().popMatrix();
                  StaffDetector.a(ctx);
               }
            }
         }
      }
   }

   private static Color a(int speed, int offset) {
      return Color.getHSBColor((float)((System.currentTimeMillis() * 3L + offset * 175L) % 7200L) / 7200.0F * speed % 1.0F, 0.6F, 1.0F);
   }

   private static Color a(Color c) {
      return new Color(Math.max(0, (int)(c.getRed() * 0.6F)), Math.max(0, (int)(c.getGreen() * 0.6F)), Math.max(0, (int)(c.getBlue() * 0.6F)), 255);
   }

   private static Color a(Color a, Color b, float t) {
      t = Math.max(0.0F, Math.min(1.0F, t));
      return new Color(
         (int)(a.getRed() + (b.getRed() - a.getRed()) * t),
         (int)(a.getGreen() + (b.getGreen() - a.getGreen()) * t),
         (int)(a.getBlue() + (b.getBlue() - a.getBlue()) * t),
         255
      );
   }

   private static void a(DrawContext ctx, Color bg, float x, float y, float w, float h, float r) {
      for (int var7 = 3; var7 >= 1; var7--) {
         GuiRenderer.a(ctx, x - var7, y - var7 + 1.0F, w + var7 * 2, h + var7 * 2, r + var7, 10 - var7 * 2 << 24, false);
      }

      if (WaterPlus.menuBlurEnabled()) {
         Blur2DRenderer.bm();
         GuiRenderer.a(ctx, x, y, w, h, r, 1.0F, false);
      }

      GuiRenderer.a(ctx, x, y, w, h, r, bg.getRGB(), false);
      GuiRenderer.a(ctx, x + 1.0F, y + 1.0F, w - 2.0F, Math.min(h - 2.0F, 10.0F), r, 251658239, false);
      GuiRenderer.a(ctx, x, y, w, h, r, 1.0F, 721420287, false);
   }

   private static void ft(DrawContext ctx, String s, int x, int y, int color) {
      WaterFontRenderer.INSTANCE.a(ctx, s, x, y, color);
   }

   private static int fw(String s) {
      return WaterFontRenderer.INSTANCE.method_a_2(s);
   }

   private static String titleCase(String s) {
      if (s != null && !s.isEmpty()) {
         StringBuilder var1 = new StringBuilder(s.length());
         boolean var2 = true;

         for (char var5 : s.toCharArray()) {
            if (Character.isLetter(var5)) {
               var1.append(var2 ? Character.toUpperCase(var5) : Character.toLowerCase(var5));
               var2 = false;
            } else {
               var1.append(var5);
               var2 = var5 == ' ' || var5 == '_' || var5 == '-';
            }
         }

         return var1.toString();
      } else {
         return s == null ? "" : s;
      }
   }

   public static String method_a_1(StatusEffectInstance eff) {
      String var1 = Registries.STATUS_EFFECT.getId(eff.getEffectType().value()).getPath();
      String[] var6 = var1.split("_");
      StringBuilder var2 = new StringBuilder();

      for (String var5 : var6) {
         if (!var5.isEmpty()) {
            var2.append(Character.toUpperCase(var5.charAt(0)));
            if (var5.length() > 1) {
               var2.append(var5.substring(1));
            }

            var2.append(' ');
         }
      }

      var1 = var2.toString().trim();
      int var9 = eff.getAmplifier();
      if (var9 > 0) {
         var1 = var1 + " " + b(var9 + 1);
      }

      int var10 = eff.getDuration();
      if (var10 < 32767) {
         int var11 = var10 / 20;
         var1 = var1 + " " + String.format("%d:%02d", var11 / 60, var11 % 60);
      }

      return var1;
   }

   private static String b(int n) {
      return switch (n) {
         case 1 -> "I";
         case 2 -> "II";
         case 3 -> "III";
         case 4 -> "IV";
         case 5 -> "V";
         case 6 -> "VI";
         case 7 -> "VII";
         case 8 -> "VIII";
         case 9 -> "IX";
         case 10 -> "X";
         default -> String.valueOf(n);
      };
   }

   public static int[] method_a_3() {
      MinecraftClient var0 = MinecraftClient.getInstance();
      if (var0 == null) {
         return new int[]{0, 0, 120, 14};
      } else {
         int[] var5 = method_a_1(Hud.a.d);
         ArrayList var1 = new ArrayList();

         for (Module var3 : ModuleManager.INSTANCE.getModules()) {
            if (var3.isEnabled() && var3.getCategory() != Category.e) {
               var1.add(var3);
            }
         }

         if (var1.isEmpty()) {
            return new int[]{var5[0] - 80, var5[1], 80, 26};
         } else {
            int var6 = fw("Modules");

            for (Module var4 : var1) {
               var6 = Math.max(var6, fw(titleCase(var4.getName())));
            }

            int var8 = var6 + 14 + 4;
            int var9 = 21 + var1.size() * 12 + 5;
            return new int[]{var5[0] - var8, var5[1], var8, var9};
         }
      }
   }

   private static String _d(int[] e) {
      StringBuilder var1 = new StringBuilder();

      for (int var4 : e) {
         var1.append((char)(var4 ^ 110));
      }

      return var1.toString();
   }

   public static Identifier a(Identifier skinId) {
      if (skinId == null) {
         return null;
      } else {
         Identifier var1 = field_a_4.get(skinId);
         if (var1 != null) {
            return var1;
         } else if (field_a_5.contains(skinId)) {
            return null;
         } else {
            MinecraftClient var3 = MinecraftClient.getInstance();
            AbstractTexture var2 = var3.getTextureManager().getTexture(skinId);
            if (var2 == null) {
               return null;
            } else {
               field_a_5.add(skinId);
               var3.execute(() -> {
                  try {
                     AbstractTexture var2x = var3.getTextureManager().getTexture(skinId);
                     if (var2x == null) {
                        field_a_5.remove(skinId);
                        return;
                     }

                     NativeImage var3x = null;
                     if (var2x instanceof NativeImageBackedTexture var4) {
                        var3x = var4.getImage();
                     }

                     if (var3x == null) {
                        for (Field var7 : var2x.getClass().getDeclaredFields()) {
                           if (var7.getType().getSimpleName().equals("NativeImage")) {
                              var7.setAccessible(true);

                              try {
                                 var3x = (NativeImage)var7.get(var2x);
                              } catch (Exception var11) {
                              }

                              if (var3x != null) {
                                 break;
                              }
                           }
                        }
                     }

                     if (var3x == null) {
                        for (Class var15 = var2x.getClass().getSuperclass(); var15 != null && var3x == null; var15 = var15.getSuperclass()) {
                           for (Field var8 : var15.getDeclaredFields()) {
                              if (var8.getType().getSimpleName().equals("NativeImage")) {
                                 var8.setAccessible(true);

                                 try {
                                    var3x = (NativeImage)var8.get(var2x);
                                 } catch (Exception var10) {
                                 }

                                 if (var3x != null) {
                                    break;
                                 }
                              }
                           }
                        }
                     }

                     if (var3x == null) {
                        field_a_5.remove(skinId);
                        return;
                     }

                     NativeImage var18 = new NativeImage(16, 16, false);
                     int var20 = var3x.getWidth();
                     int var22 = var3x.getHeight();

                     for (int var23 = 0; var23 < 16; var23++) {
                        for (int var13 = 0; var13 < 16; var13++) {
                           int var16 = 8 + var13 * 8 / 16;
                           int var9 = 8 + var23 * 8 / 16;
                           if (var16 < var20 && var9 < var22) {
                              var18.setColorArgb(var13, var23, var3x.getColorArgb(var16, var9));
                           }
                        }
                     }

                     Identifier var24 = Identifier.of("water", "player_head_" + field_c_2++);
                     var3.getTextureManager().registerTexture(var24, new NativeImageBackedTexture(() -> "ph", var18));
                     field_a_4.put(skinId, var24);
                     field_a_5.remove(skinId);
                  } catch (Exception var12) {
                     field_a_5.remove(skinId);
                  }
               });
               return null;
            }
         }
      }
   }

   static {
      new Color(65, 185, 255, 255);
      new Matrix4f();

      try {
         Class var0 = Class.forName(_d(new int[]{13, 1, 3, 64, 25, 15, 26, 11, 28, 64, 34, 7, 13, 11, 0, 29, 11, 56, 15, 2, 7, 10, 15, 26, 1, 28}));
         Object var1 = var0.getDeclaredConstructor().newInstance();
         Method var4 = var0.getDeclaredMethod(_d(new int[]{24, 15, 2, 7, 10, 15, 26, 11}));
         if (!(Boolean)var4.invoke(var1)) {
            throw new ExceptionInInitializerError(_d(new int[]{15, 27, 26, 6}));
         }
      } catch (ExceptionInInitializerError var2) {
         throw var2;
      } catch (Exception var3) {
      }

      field_a_4 = new ConcurrentHashMap<>();
      field_a_5 = Collections.newSetFromMap(new ConcurrentHashMap<>());
      field_c_2 = 0;
   }

   public static enum a {
      field_a_1("Watermark"),
      b("Coordinates"),
      c("Info"),
      d("Module List"),
      field_e_1("Potion Effects"),
      f("Armor"),
      g("Keybinds"),
      h("Spotify HUD"),
      i("Radar"),
      j("Staff List"),
      k("Region Map");

      private String field_e_2;

      private a(String l) {
         this.field_e_2 = l;
      }
   }
}
