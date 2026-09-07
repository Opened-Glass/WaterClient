package com.water.module;

import com.water.module.modules.client.ConfigShare;
import com.water.module.modules.client.DiscordRPC;
import com.water.module.modules.client.Friends;
import com.water.module.modules.client.Hud;
import com.water.module.modules.client.SpotifyHud;
import com.water.module.modules.client.WaterPlus;
import com.water.module.modules.combat.AnchorMacro;
import com.water.module.modules.combat.AutoCrystal;
import com.water.module.modules.combat.AutoDoubleHand;
import com.water.module.modules.combat.AutoInventoryTotem;
import com.water.module.modules.combat.AutoTotem;
import com.water.module.modules.combat.DoubleAnchor;
import com.water.module.modules.combat.Hitbox;
import com.water.module.modules.combat.HoverTotem;
import com.water.module.modules.combat.ShieldBreaker;
import com.water.module.modules.combat.SingleAnchor;
import com.water.module.modules.combat.SpearSwap;
import com.water.module.modules.combat.Triggerbot;
import com.water.module.modules.donut.ActivityDebug;
import com.water.module.modules.donut.AntiTrap;
import com.water.module.modules.donut.AutoChunkLoader;
import com.water.module.modules.donut.BoneDropper;
import com.water.module.modules.donut.FakeRoles;
import com.water.module.modules.donut.FakeStats;
import com.water.module.modules.donut.SpawnerProtect;
import com.water.module.modules.donut.StaffDetector;
import com.water.module.modules.donut.SuspiciousChunkFinder;
import com.water.module.modules.donut.TuffChunkV2;
import com.water.module.modules.misc.AutoLog;
import com.water.module.modules.misc.AutoMine;
import com.water.module.modules.misc.AutoRender;
import com.water.module.modules.misc.AutoTPA;
import com.water.module.modules.misc.AutoTool;
import com.water.module.modules.misc.ChatMacro;
import com.water.module.modules.misc.CoordinateSnapper;
import com.water.module.modules.misc.FastPlace;
import com.water.module.modules.misc.Freelook;
import com.water.module.modules.misc.HomeSetter;
import com.water.module.modules.misc.NameProtect;
import com.water.module.modules.misc.NameTags;
import com.water.module.modules.misc.PearlESP;
import com.water.module.modules.misc.SkinChanger;
import com.water.module.modules.misc.Sprint;
import com.water.module.modules.misc.SwingSpeed;
import com.water.module.modules.misc.TabDetector;
import com.water.module.modules.misc.TunnelBaseFinder;
import com.water.module.modules.misc.WeatherNotifier;
import com.water.module.modules.render.BlockESP;
import com.water.module.modules.render.ExtraESP;
import com.water.module.modules.render.Freecam;
import com.water.module.modules.render.FullBright;
import com.water.module.modules.render.FutureDebug;
import com.water.module.modules.render.HoleESP;
import com.water.module.modules.render.JumpCircles;
import com.water.module.modules.render.LightDebug;
import com.water.module.modules.render.NoRender;
import com.water.module.modules.render.PlayerESP;
import com.water.module.modules.render.RegionMap;
import com.water.module.modules.render.SpawnerNotifier;
import com.water.module.modules.render.StorageESP;
import com.water.setting.BlocksSetting;
import com.water.setting.MobsSetting;
import com.water.setting.MultiItemSetting;
import com.water.setting.Setting;
import com.water.setting.ToggleableIntegerSetting;
import com.water.utils.RenderUtils;
import java.awt.Color;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityType;
import net.minecraft.network.packet.Packet;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class ModuleManager {
   private String field_b_1 = "default";
   public static final ModuleManager INSTANCE = new ModuleManager();
   private final List<Module> a = new ArrayList<>();
   private boolean field_b_2 = false;
   private boolean c = false;
   private int field_b_3 = 0;
   private boolean d = false;

   public void method_a_1() {
      if (!this.field_b_2) {
         this.a.add(new WaterPlus());
         this.a.add(new Hud());
         this.a.add(new SpotifyHud());
         this.a.add(new Friends());
         this.a.add(new ConfigShare());
         this.a.add(new NamedModule("Elytra Swap", Category.field_a_1));
         this.a.add(new AutoTotem());
         this.a.add(new ShieldBreaker());
         this.a.add(new AnchorMacro());
         this.a.add(new NamedModule("Mace Swap", Category.field_a_1));
         this.a.add(new Triggerbot());
         this.a.add(new HoverTotem());
         this.a.add(new DoubleAnchor());
         this.a.add(new SingleAnchor());
         this.a.add(new AutoDoubleHand());
         this.a.add(new AutoInventoryTotem());
         this.a.add(new AutoCrystal());
         this.a.add(new Hitbox());
         this.a.add(new SpearSwap());
         this.a.add(new DiscordRPC());
         this.a.add(new StorageESP());
         this.a.add(new ExtraESP());
         this.a.add(new Freecam());
         this.a.add(new FullBright());
         this.a.add(new PlayerESP());
         this.a.add(new HoleESP());
         this.a.add(new NoRender());
         this.a.add(new JumpCircles());
         this.a.add(new LightDebug());
         this.a.add(new AutoRender());
         this.a.add(new SpawnerNotifier());
         this.a.add(new FutureDebug());
         this.a.add(new SuspiciousChunkFinder());
         this.a.add(new TuffChunkV2());
         this.a.add(new AutoMine());
         this.a.add(new PearlESP());
         this.a.add(new SwingSpeed());
         this.a.add(new NameTags());
         this.a.add(new RegionMap());
         this.a.add(new AutoTool());
         this.a.add(new Sprint());
         this.a.add(new NameProtect());
         this.a.add(new Freelook());
         this.a.add(new SkinChanger());
         this.a.add(new HomeSetter());
         this.a.add(new WeatherNotifier());
         this.a.add(new CoordinateSnapper());
         this.a.add(new FastPlace());
         this.a.add(new TabDetector());
         this.a.add(new AutoLog());
         this.a.add(new AutoTPA());
         this.a.add(new TunnelBaseFinder());
         this.a.add(new ChatMacro());
         this.a.add(new AutoChunkLoader());
         this.a.add(new StaffDetector());
         this.a.add(new FakeRoles());
         this.a.add(new AntiTrap());
         this.a.add(new ActivityDebug());
         this.a.add(new SpawnerProtect());
         this.a.add(new FakeStats());
         this.a.add(new BoneDropper());
         this.field_b_2 = true;
         this.g();
         this.h();
      }
   }

   public void c() {
      if (this.field_b_2 && !this.c) {
         if (this.field_b_3 > 0) {
            this.d = true;
         } else {
            this.f();
         }
      }
   }

   public void d() {
      this.field_b_3++;
   }

   public void e() {
      if (this.field_b_3 > 0) {
         this.field_b_3--;
      }

      if (this.field_b_3 == 0 && this.d) {
         this.d = false;
         this.f();
      }
   }

   public void f() {
      if (this.field_b_2 && !this.c) {
         Path var1 = this.b();
         Path var2 = var1.resolveSibling(var1.getFileName().toString() + ".tmp");

         try {
            Files.createDirectories(var1.getParent());

            try (BufferedWriter var3 = Files.newBufferedWriter(var2, StandardCharsets.UTF_8)) {
               var3.write("WATER_CONFIG_V2");
               var3.newLine();

               for (Module var5 : this.a) {
                  var3.write("MODULE");
                  var3.write(9);
                  var3.write(this.method_a_3(var5.getName()));
                  var3.write(9);
                  var3.write(Integer.toString(var5.getBind()));
                  var3.write(9);
                  int var6 = var5 instanceof ActivatableModule var7 ? var7.getActivationKey() : 0;
                  var3.write(Integer.toString(var6));
                  var3.write(9);
                  var3.write(Boolean.toString(var5.isEnabled()));
                  var3.newLine();

                  for (Setting var8 : var5.getSettings()) {
                     String var25 = this.a(var8);
                     if (var25 != null) {
                        var3.write("SETTING");
                        var3.write(9);
                        var3.write(this.method_a_3(var5.getName()));
                        var3.write(9);
                        var3.write(this.method_a_3(var8.getName()));
                        var3.write(9);
                        var3.write(this.method_a_3(var25));
                        var3.newLine();
                     }
                  }
               }

               for (Hud.a var37 : Hud.a.values()) {
                  int[] var41 = Hud.method_a_1(var37);
                  var3.write("HUDPOS");
                  var3.write(9);
                  var3.write(var37.name());
                  var3.write(9);
                  var3.write(Integer.toString(var41[0]));
                  var3.write(9);
                  var3.write(Integer.toString(var41[1]));
                  var3.newLine();
               }

               for (Module var19 : this.a) {
                  if (var19 instanceof BlockESP var27) {
                     Map var38 = var27.method_b_1();

                     for (Entry var28 : var38.entrySet()) {
                        Identifier var20 = Registries.BLOCK.getId((Block)var28.getKey());
                        if (var20 != null) {
                           Color var29 = (Color)var28.getValue();
                           var3.write("BLOCKCOLOR");
                           var3.write(9);
                           var3.write(this.method_a_3(var20.toString()));
                           var3.write(9);
                           var3.write(var29.getRed() + "," + var29.getGreen() + "," + var29.getBlue() + "," + var29.getAlpha());
                           var3.newLine();
                        }
                     }
                  }
               }

               for (Module var21 : this.a) {
                  if (var21 instanceof StorageESP var30) {
                     Map var39 = var30.d();

                     for (Entry var31 : var39.entrySet()) {
                        Identifier var22 = Registries.BLOCK.getId((Block)var31.getKey());
                        if (var22 != null) {
                           Color var32 = (Color)var31.getValue();
                           var3.write("STORAGECOLOR");
                           var3.write(9);
                           var3.write(this.method_a_3(var22.toString()));
                           var3.write(9);
                           var3.write(var32.getRed() + "," + var32.getGreen() + "," + var32.getBlue() + "," + var32.getAlpha());
                           var3.newLine();
                        }
                     }
                  }
               }

               for (Module var23 : this.a) {
                  if (var23 instanceof ExtraESP var33) {
                     Map var40 = var33.b();

                     for (Entry var34 : var40.entrySet()) {
                        Identifier var24 = Registries.BLOCK.getId((Block)var34.getKey());
                        if (var24 != null) {
                           Color var35 = (Color)var34.getValue();
                           var3.write("EXTRACOLOR");
                           var3.write(9);
                           var3.write(this.method_a_3(var24.toString()));
                           var3.write(9);
                           var3.write(var35.getRed() + "," + var35.getGreen() + "," + var35.getBlue() + "," + var35.getAlpha());
                           var3.newLine();
                        }
                     }
                  }
               }
            }

            try {
               Files.move(var2, var1, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException var11) {
               Files.move(var2, var1, StandardCopyOption.REPLACE_EXISTING);
            }
         } catch (Throwable var13) {
            System.err.println("[WaterConfig] save failed: " + var13.getMessage());

            try {
               Files.deleteIfExists(var2);
            } catch (IOException var9) {
            }
         }
      }
   }

   public void g() {
      Path var1 = this.b();
      if (Files.exists(var1)) {
         this.c = true;

         try {
            for (String var2 : Files.readAllLines(var1, StandardCharsets.UTF_8)) {
               if (var2 != null && !var2.isBlank() && !"WATER_CONFIG_V2".equals(var2)) {
                  if (var2.startsWith("MODULE\t")) {
                     this.f(var2);
                  } else if (var2.startsWith("SETTING\t")) {
                     this.g(var2);
                  } else if (var2.startsWith("HUDPOS\t")) {
                     this.method_a_1(var2);
                  } else if (var2.startsWith("BLOCKCOLOR\t")) {
                     this.method_b_1(var2);
                  } else if (var2.startsWith("STORAGECOLOR\t")) {
                     this.method_c_1(var2);
                  } else if (var2.startsWith("EXTRACOLOR\t")) {
                     this.d(var2);
                  } else {
                     this.h(var2);
                  }
               }
            }
         } catch (Throwable var5) {
            System.err.println("[WaterConfig] load failed: " + var5.getMessage());
         } finally {
            this.c = false;
         }
      }
   }

   private void h() {
      for (Module var2 : this.a) {
         if (var2 != null && var2.isEnabled()) {
            try {
               var2.onEnable();
            } catch (Throwable var3) {
               this.a(var2);
            }
         }
      }
   }

   private void method_a_1(String line) {
      try {
         String var5 = line.split("\t");
         if (var5.length < 4) {
            return;
         }

         Hud.a var2 = Hud.a.valueOf(var5[1]);
         int var3 = Integer.parseInt(var5[2]);
         String var6 = Integer.parseInt(var5[3]);
         Hud.a(var2, var3, var6);
      } catch (Exception var4) {
      }
   }

   private void method_b_1(String line) {
      try {
         String var10 = line.split("\t");
         if (var10.length < 3) {
            return;
         }

         String var2 = this.method_b_3(var10[1]);
         String var11 = var10[2].split(",");
         if (var11.length < 4) {
            return;
         }

         int var3 = Integer.parseInt(var11[0].trim());
         int var4 = Integer.parseInt(var11[1].trim());
         int var5 = Integer.parseInt(var11[2].trim());
         String var12 = Integer.parseInt(var11[3].trim());
         Identifier var13 = Identifier.tryParse(var2);
         if (var13 == null) {
            return;
         }

         Block var14 = Registries.BLOCK.get(var13);
         if (var14 == null || var14 == Blocks.AIR) {
            return;
         }

         for (Module var7 : this.a) {
            if (var7 instanceof BlockESP var15) {
               Map var8 = var15.method_b_1();
               var8.put(var14, new Color(var3, var4, var5, var12));
               var15.a(var8);
            }
         }
      } catch (Exception var9) {
      }
   }

   private void method_c_1(String line) {
      try {
         String var9 = line.split("\t");
         if (var9.length < 3) {
            return;
         }

         String var2 = this.method_b_3(var9[1]);
         String var10 = var9[2].split(",");
         if (var10.length < 4) {
            return;
         }

         int var3 = Integer.parseInt(var10[0].trim());
         int var4 = Integer.parseInt(var10[1].trim());
         int var5 = Integer.parseInt(var10[2].trim());
         String var11 = Integer.parseInt(var10[3].trim());
         Identifier var12 = Identifier.tryParse(var2);
         if (var12 == null) {
            return;
         }

         Block var13 = Registries.BLOCK.get(var12);
         if (var13 == null || var13 == Blocks.AIR) {
            return;
         }

         for (Module var7 : this.a) {
            if (var7 instanceof StorageESP var14) {
               var14.a(var13, new Color(var3, var4, var5, var11));
            }
         }
      } catch (Exception var8) {
      }
   }

   private void d(String line) {
      try {
         String var10 = line.split("\t");
         if (var10.length < 3) {
            return;
         }

         String var2 = this.method_b_3(var10[1]);
         String var11 = var10[2].split(",");
         if (var11.length < 4) {
            return;
         }

         int var3 = Integer.parseInt(var11[0].trim());
         int var4 = Integer.parseInt(var11[1].trim());
         int var5 = Integer.parseInt(var11[2].trim());
         String var12 = Integer.parseInt(var11[3].trim());
         Identifier var13 = Identifier.tryParse(var2);
         if (var13 == null) {
            return;
         }

         Block var14 = Registries.BLOCK.get(var13);
         if (var14 == null || var14 == Blocks.AIR) {
            return;
         }

         for (Module var7 : this.a) {
            if (var7 instanceof ExtraESP var15) {
               Map var8 = var15.b();
               var8.put(var14, new Color(var3, var4, var5, var12));
               var15.a(var8);
            }
         }
      } catch (Exception var9) {
      }
   }

   public String method_a_2() {
      return this.field_b_1;
   }

   public void e(String name) {
      this.field_b_1 = name != null && !name.isBlank() ? name.trim() : "default";
   }

   public List<Module> getModules() {
      return this.a;
   }

   public List<Module> getModulesInCategory(Category category) {
      ArrayList var2 = new ArrayList();

      for (Module var4 : this.a) {
         try {
            if (var4 != null && var4.getCategory() == category) {
               var2.add(var4);
            }
         } catch (Throwable var5) {
         }
      }

      return var2;
   }

   public Module getModuleByName(String name) {
      if (name != null && !name.isBlank()) {
         if (name.equalsIgnoreCase("Tuff Chunk Finder")) {
            name = "Tuff Chunk V2";
         }

         for (Module var3 : this.a) {
            if (var3 != null) {
               try {
                  String var4 = var3.getName();
                  if (var4 != null && var4.equalsIgnoreCase(name)) {
                     return var3;
                  }
               } catch (Throwable var5) {
               }
            }
         }

         return null;
      } else {
         return null;
      }
   }

   public Path method_a_3() {
      return this.b();
   }

   public boolean a(Path source) {
      if (source != null && Files.isRegularFile(source)) {
         Path var2 = this.b();

         try {
            Files.createDirectories(var2.getParent());
            Files.copy(source, var2, StandardCopyOption.REPLACE_EXISTING);
            this.g();
            return true;
         } catch (Throwable var3) {
            System.err.println("[WaterConfig] apply failed: " + var3.getMessage());
            return false;
         }
      } else {
         return false;
      }
   }

   public void onTick() {
      for (Module var2 : this.a) {
         if (var2.isEnabled()) {
            try {
               var2.onTick();
            } catch (Throwable var3) {
               this.a(var2);
            }
         }
      }
   }

   public void onRender(MatrixStack matrices, float tickDelta) {
      for (Module var4 : this.a) {
         if (var4.isEnabled() && !(var4 instanceof ExtraESP) && !(var4 instanceof StorageESP)) {
            try {
               var4.onRender(matrices, tickDelta);
            } catch (Throwable var7) {
               this.a(var4);
            } finally {
               RenderUtils.restoreWorldDepthState();
            }
         }
      }

      this.a(ExtraESP.class, matrices, tickDelta);
      this.a(StorageESP.class, matrices, tickDelta);
      RenderUtils.restoreWorldDepthState();
   }

   private void a(Class<? extends Module> type, MatrixStack matrices, float tickDelta) {
      for (Module var5 : this.a) {
         if (var5.isEnabled() && type.isInstance(var5)) {
            try {
               var5.onRender(matrices, tickDelta);
            } catch (Throwable var8) {
               this.a(var5);
            } finally {
               RenderUtils.restoreWorldDepthState();
            }
         }
      }
   }

   private void a(Module module) {
      try {
         module.applyEnabled(false);
         module.onDisable();
         this.f();
      } catch (Throwable var2) {
      }
   }

   public void onPacketReceive(Packet<?> packet) {
      for (Module var3 : this.a) {
         if (var3.isEnabled()) {
            var3.onPacketReceive(packet);
         }
      }
   }

   public boolean onPacketSend(Packet<?> packet) {
      boolean var2 = false;

      for (Module var4 : this.a) {
         if (var4.isEnabled()) {
            try {
               var2 |= var4.onPacketSend(packet);
            } catch (Exception var5) {
            }
         }
      }

      return var2;
   }

   private Path b() {
      String var1 = this.field_b_1 != null && !this.field_b_1.isBlank() ? this.field_b_1 : "default";
      var1 = var1.equals("default") ? "water_config.txt" : "water_config_" + var1 + ".txt";
      return MinecraftClient.getInstance().runDirectory.toPath().resolve(var1);
   }

   private void f(String line) {
      String var5 = line.split("\t", 5);
      if (var5.length >= 5) {
         Module var2 = this.getModuleByName(this.method_b_3(var5[1]));
         if (var2 != null) {
            try {
               var2.applyBind(Integer.parseInt(var5[2]));
               if (var2 instanceof ActivatableModule var3) {
                  var3.a(Integer.parseInt(var5[3]));
               }

               var2.applyEnabled(Boolean.parseBoolean(var5[4]));
            } catch (Exception var4) {
            }
         }
      }
   }

   private void g(String line) {
      String var3 = line.split("\t", 4);
      if (var3.length >= 4) {
         Module var2 = this.getModuleByName(this.method_b_3(var3[1]));
         if (var2 != null) {
            Setting var4 = this.a(var2, this.method_b_3(var3[2]));
            if (var4 != null) {
               this.a(var4, this.method_b_3(var3[3]));
            }
         }
      }
   }

   private void h(String line) {
      String var5 = line.split(":", 4);
      if (var5.length >= 2) {
         Module var2 = this.getModuleByName(var5[0]);
         if (var2 != null) {
            try {
               if (var5.length >= 2) {
                  var2.applyBind(Integer.parseInt(var5[1]));
               }

               if (var5.length >= 3 && var2 instanceof ActivatableModule var3) {
                  var3.a(Integer.parseInt(var5[2]));
               }

               if (var5.length >= 4) {
                  var2.applyEnabled(Boolean.parseBoolean(var5[3]));
               }
            } catch (Exception var4) {
            }
         }
      }
   }

   private Setting<?> a(Module module, String settingName) {
      for (Setting var3 : module.getSettings()) {
         if (var3.matchesName(settingName)) {
            return var3;
         }
      }

      return null;
   }

   private String a(Setting<?> setting) {
      Object var2 = setting.getValue();
      if (setting instanceof Setting var12) {
         return this.a(var12);
      } else if (setting instanceof Setting var11) {
         return this.a(var11);
      } else if (setting instanceof Setting var10) {
         return this.a(var10.getValue());
      } else if (setting instanceof Setting var9) {
         return var9.q();
      } else if (var2 instanceof Setting var8) {
         return Boolean.toString(var8);
      } else if (var2 instanceof Setting var7) {
         return Float.toString(var7);
      } else if (var2 instanceof Setting var6) {
         return Integer.toString(var6);
      } else if (var2 instanceof Setting var5) {
         return Double.toString(var5);
      } else if (var2 instanceof Setting var4) {
         return var4;
      } else {
         return var2 instanceof Setting var3 ? var3.getRed() + "," + var3.getGreen() + "," + var3.getBlue() + "," + var3.getAlpha() : null;
      }
   }

   private void a(Setting<?> setting, String serialized) {
      Object var3 = setting.getValue();

      try {
         if (setting instanceof BlocksSetting var9) {
            var9.setValue(this.method_a_2(serialized));
            return;
         }

         if (setting instanceof MobsSetting var8) {
            var8.setValue(this.method_b_2(serialized));
            return;
         }

         if (setting instanceof MultiItemSetting var7) {
            var7.setValue(this.method_c_2(serialized));
            return;
         }

         if (setting instanceof ToggleableIntegerSetting var6) {
            var6.u(serialized);
            return;
         }

         if (var3 instanceof Boolean) {
            setting.setValue(Boolean.parseBoolean(serialized));
            return;
         }

         if (var3 instanceof Float) {
            setting.setValue(Float.parseFloat(serialized));
            return;
         }

         if (var3 instanceof Integer) {
            setting.setValue(Math.round(Float.parseFloat(serialized)));
            return;
         }

         if (var3 instanceof Double) {
            setting.setValue(Double.parseDouble(serialized));
            return;
         }

         if (var3 instanceof String) {
            setting.setValue(serialized);
            return;
         }

         if (var3 instanceof Color) {
            var3 = this.a(serialized, (Color)var3);
            if (var3 != null) {
               setting.setValue(var3);
            }
         }
      } catch (Exception var4) {
      }
   }

   private Color a(String serialized, Color fallback) {
      if (serialized == null) {
         return fallback;
      } else {
         serialized = serialized.trim();
         if (serialized.isEmpty()) {
            return fallback;
         } else {
            try {
               if (serialized.startsWith("#")) {
                  String var3 = serialized.substring(1).trim();
                  long var5 = Long.parseLong(var3, 16);
                  if (var3.length() == 6) {
                     return new Color((int)(var5 >> 16 & 255L), (int)(var5 >> 8 & 255L), (int)(var5 & 255L), fallback == null ? 255 : fallback.getAlpha());
                  }

                  if (var3.length() == 8) {
                     return new Color((int)(var5 >> 16 & 255L), (int)(var5 >> 8 & 255L), (int)(var5 & 255L), (int)(var5 >> 24 & 255L));
                  }
               }

               if (serialized.startsWith("java.awt.Color")) {
                  serialized = serialized.replace("java.awt.Color", "")
                     .replace("[", "")
                     .replace("]", "")
                     .replace("r=", "")
                     .replace("g=", "")
                     .replace("b=", "")
                     .replace("a=", "");
               }

               String[] var10 = serialized.split(",");
               if (var10.length >= 3) {
                  int var12 = this.a(Integer.parseInt(var10[0].trim()));
                  int var6 = this.a(Integer.parseInt(var10[1].trim()));
                  String var9 = this.a(Integer.parseInt(var10[2].trim()));
                  int var11 = var10.length >= 4 ? this.a(Integer.parseInt(var10[3].trim())) : (fallback == null ? 255 : fallback.getAlpha());
                  return new Color(var12, var6, var9, var11);
               }
            } catch (Exception var7) {
            }

            return fallback;
         }
      }
   }

   private int a(int value) {
      return Math.max(0, Math.min(255, value));
   }

   private String a(BlocksSetting setting) {
      StringBuilder var2 = new StringBuilder();

      for (Block var3 : setting.getSelectedBlocks()) {
         Identifier var5 = Registries.BLOCK.getId(var3);
         if (var5 != null) {
            if (!var2.isEmpty()) {
               var2.append(',');
            }

            var2.append(var5);
         }
      }

      return var2.toString();
   }

   private Set<Block> method_a_2(String serialized) {
      LinkedHashSet var2 = new LinkedHashSet();
      if (serialized != null && !serialized.isBlank()) {
         for (String var5 : serialized.split(",")) {
            var5 = var5.trim();
            if (!var5.isEmpty()) {
               Identifier var8 = Identifier.tryParse(var5);
               if (var8 != null) {
                  Block var9 = Registries.BLOCK.get(var8);
                  if (var9 != null) {
                     var2.add(var9);
                  }
               }
            }
         }

         return var2;
      } else {
         return var2;
      }
   }

   private String a(MobsSetting setting) {
      StringBuilder var2 = new StringBuilder();

      for (EntityType var3 : setting.getSelectedMobs()) {
         Identifier var5 = Registries.ENTITY_TYPE.getId(var3);
         if (var5 != null) {
            if (!var2.isEmpty()) {
               var2.append(',');
            }

            var2.append(var5);
         }
      }

      return var2.toString();
   }

   private Set<EntityType<?>> method_b_2(String serialized) {
      LinkedHashSet var2 = new LinkedHashSet();
      if (serialized != null && !serialized.isBlank()) {
         for (String var5 : serialized.split(",")) {
            var5 = var5.trim();
            if (!var5.isEmpty()) {
               Identifier var8 = Identifier.tryParse(var5);
               if (var8 != null && Registries.ENTITY_TYPE.containsId(var8)) {
                  var2.add(Registries.ENTITY_TYPE.get(var8));
               }
            }
         }

         return var2;
      } else {
         return var2;
      }
   }

   private String a(Set<String> values) {
      StringBuilder var2 = new StringBuilder();

      for (String var3 : values) {
         if (var3 != null && !var3.isEmpty()) {
            if (!var2.isEmpty()) {
               var2.append(',');
            }

            var2.append(var3);
         }
      }

      return var2.toString();
   }

   private Set<String> method_c_2(String serialized) {
      LinkedHashSet var2 = new LinkedHashSet();
      if (serialized != null && !serialized.isBlank()) {
         for (String var5 : serialized.split(",")) {
            var5 = var5.trim();
            if (!var5.isEmpty()) {
               var2.add(var5);
            }
         }

         return var2;
      } else {
         return var2;
      }
   }

   private String method_a_3(String value) {
      return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
   }

   private String method_b_3(String value) {
      if (value != null && !value.isEmpty()) {
         try {
            return new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8);
         } catch (IllegalArgumentException var2) {
            return value;
         }
      } else {
         return "";
      }
   }
}
