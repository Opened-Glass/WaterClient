package com.water.module.modules.donut;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.water.WaterClient;
import com.water.module.Category;
import com.water.module.Module;
import com.water.module.modules.client.Friends;
import com.water.setting.Setting;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public final class SpawnerProtect extends Module {
   private static final Duration field_a_1 = Duration.ofSeconds(8L);
   private static final DateTimeFormatter field_a_2 = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ROOT);
   private static final HttpClient field_a_3 = HttpClient.newBuilder().connectTimeout(field_a_1).followRedirects(Redirect.NORMAL).build();
   private final Setting<Integer> I = new Setting<>("Critical Distance", 5, 1, 20);
   private final Setting<String> field_a_4 = new Setting<String>("Webhook", "") {
      @Override
      public boolean matchesName(String settingName) {
         return super.matchesName(settingName) || "Webhook URL".equalsIgnoreCase(settingName);
      }
   };
   private BlockPos b;
   private boolean v;
   private int q;

   public SpawnerProtect() {
      super("SpawnerProtect", Category.d);
      this.addSetting(this.I);
      this.addSetting(this.field_a_4);
   }

   @Override
   public void onEnable() {
      this.ag();
      if (!this.l()) {
         this.q("Need a Silk Touch pickaxe in hotbar");
      }
   }

   @Override
   public void onDisable() {
      this.af();
   }

   @Override
   public void onTick() {
      if (mc.player != null && mc.world != null && mc.interactionManager != null) {
         if (!this.v) {
            if (!this.l()) {
               this.q("Need a Silk Touch pickaxe in hotbar");
            } else {
               SpawnerProtect.a var1 = this.method_a_1();
               if (var1.n()) {
                  this.a(this.a(var1.b(), var1.a()));
               } else if (!var1.m()) {
                  this.af();
               } else {
                  int var2 = this.i();
                  if (var2 == -1) {
                     this.q("Need a Silk Touch pickaxe in hotbar");
                  } else {
                     this.h(var2);
                     this.ae();
                     if (this.b == null || !this.b(this.b) || !mc.player.canInteractWithBlockAt(this.b, 5.0)) {
                        this.b = this.method_a_2();
                        if (this.b == null) {
                           this.a(this.method_a_3());
                           return;
                        }
                     }

                     Direction var3 = this.method_a_1(this.b);
                     this.a(this.b, var3);
                     mc.interactionManager.updateBlockBreakingProgress(this.b, var3);
                     mc.world.spawnBlockBreakingParticle(this.b, var3);
                     mc.player.swingHand(Hand.MAIN_HAND);
                     if (!this.b(this.b)) {
                        this.q++;
                        this.b = null;
                     }
                  }
               }
            }
         }
      }
   }

   private SpawnerProtect.a method_a_1() {
      double var1 = a(this.I.getValue());
      boolean var3 = false;
      boolean var4 = false;
      PlayerEntity var5 = null;
      double var6 = -1.0;

      for (PlayerEntity var9 : mc.world.getPlayers()) {
         if (var9 != mc.player && !var9.isSpectator() && !var9.isTeammate(mc.player) && (!Friends.d() || !Friends.method_a_1(var9.getName().getString()))) {
            var3 = true;
            double var10 = mc.player.squaredDistanceTo(var9);
            if (var10 <= var1) {
               var4 = true;
               double var12 = Math.sqrt(var10);
               var5 = var9;
               var6 = var12;
               break;
            }
         }
      }

      return new SpawnerProtect.a(var3, var4, var5, var6);
   }

   private BlockPos method_a_2() {
      ArrayList var1 = new ArrayList();
      BlockPos var2 = mc.player.getBlockPos();

      for (int var3 = -32; var3 <= 32; var3++) {
         for (int var4 = -32; var4 <= 32; var4++) {
            for (int var5 = -32; var5 <= 32; var5++) {
               int var6 = var3 * var3 + var4 * var4 + var5 * var5;
               if (var6 <= 1024) {
                  BlockPos var7 = var2.add(var3, var4, var5);
                  if (this.b(var7) && mc.player.canInteractWithBlockAt(var7, 5.0)) {
                     var1.add(var7.toImmutable());
                  }
               }
            }
         }
      }

      return var1.stream().min(Comparator.comparingDouble(this::method_a_2)).orElse(null);
   }

   private boolean l() {
      return this.i() != -1;
   }

   private int i() {
      for (int var1 = 0; var1 < 9; var1++) {
         if (this.a(mc.player.getInventory().getStack(var1))) {
            return var1;
         }
      }

      return -1;
   }

   private boolean a(ItemStack stack) {
      if (stack != null && !stack.isEmpty()) {
         String var2 = Registries.ITEM.getId(stack.getItem()).getPath();
         if (!var2.endsWith("_pickaxe")) {
            return false;
         } else {
            RegistryEntry var3 = mc.world
               .getRegistryManager()
               .getOrThrow(RegistryKeys.ENCHANTMENT)
               .getEntry(mc.world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT).get(Enchantments.SILK_TOUCH));
            return var3 != null && EnchantmentHelper.getLevel(var3, stack) > 0;
         }
      } else {
         return false;
      }
   }

   private boolean b(BlockPos pos) {
      return mc.world != null && mc.world.getBlockState(pos).isOf(Blocks.SPAWNER);
   }

   private Direction method_a_1(BlockPos pos) {
      BlockPos var2 = mc.player.getEyePos().subtract(Vec3d.ofCenter(pos));
      return Direction.getFacing(var2.x, var2.y, var2.z);
   }

   private void a(BlockPos pos, Direction direction) {
      Vec3d var3 = mc.player.getEyePos();
      Vec3d var4 = Vec3d.ofCenter(pos);
      double var5 = var4.x - var3.x;
      double var7 = var4.y - var3.y;
      double var9 = var4.z - var3.z;
      double var11 = Math.sqrt(var5 * var5 + var9 * var9);
      float var13 = (float)(Math.toDegrees(Math.atan2(var9, var5)) - 90.0);
      float var14 = (float)(-Math.toDegrees(Math.atan2(var7, var11)));
      mc.player.setYaw(var13);
      mc.player.setPitch(var14);
      mc.crosshairTarget = new BlockHitResult(var4, direction, pos, false);
   }

   private void h(int slot) {
      if (slot >= 0 && slot < 9 && mc.player.getInventory().getSelectedSlot() != slot) {
         mc.player.getInventory().setSelectedSlot(slot);
      }
   }

   private void ae() {
      mc.options.sneakKey.setPressed(true);
      mc.player.setSneaking(true);
   }

   private void af() {
      this.b = null;
      if (mc.interactionManager != null && mc.interactionManager.isBreakingBlock()) {
         mc.interactionManager.cancelBlockBreaking();
      }

      if (mc.player != null) {
         mc.options.sneakKey.setPressed(false);
         mc.player.setSneaking(false);
      }
   }

   private void a(SpawnerProtect.b snapshot) {
      this.af();
      String var2 = this.e(this.field_a_4.getValue());
      if (!this.b(var2)) {
         this.v = false;
         this.q(snapshot.n());
      } else {
         this.v = true;
         CompletableFuture.runAsync(() -> this.b(snapshot), Util.getIoWorkerExecutor().named("coordsnapper-send"))
            .whenComplete((ignored, throwable) -> mc.execute(() -> {
               if (throwable != null) {
                  WaterClient.a.error("SpawnerProtect webhook failed", throwable);
               }

               this.v = false;
               this.q(snapshot.n());
            }));
      }
   }

   private SpawnerProtect.b method_a_3() {
      String var1 = this.e(this.field_a_4.getValue());
      return new SpawnerProtect.b(
         var1,
         "[SpawnerProtect]",
         "All your spawners have been collected.",
         5624994,
         mc.player.getName().getString(),
         "",
         "",
         this.j(),
         true,
         this.e(),
         field_a_2.format(LocalTime.now()),
         "https://mc-heads.net/body/" + this.f(mc.player.getName().getString()),
         "SpawnerProtect finished"
      );
   }

   private SpawnerProtect.b a(PlayerEntity threat, double distance) {
      String var4 = this.e(this.field_a_4.getValue());
      PlayerEntity var5 = threat != null ? threat.getName().getString() : "Unknown";
      return new SpawnerProtect.b(
         var4,
         "[SpawnerProtect]",
         var5 + " came too close.",
         14838378,
         mc.player.getName().getString(),
         var5,
         String.format(Locale.ROOT, "%.1f", distance),
         this.j(),
         false,
         this.e(),
         field_a_2.format(LocalTime.now()),
         "https://mc-heads.net/body/" + this.f(var5),
         "Enemy within critical distance"
      );
   }

   private void b(SpawnerProtect.b snapshot) {
      JsonObject var2 = new JsonObject();
      var2.addProperty("username", "SpawnerProtect");
      JsonObject var3 = new JsonObject();
      var3.addProperty("title", snapshot.g());
      var3.addProperty("description", snapshot.h());
      var3.addProperty("color", snapshot.method_k_1());
      JsonArray var4 = new JsonArray();
      var4.add(this.a("Player", snapshot.playerName(), false));
      var4.add(this.a("Time", snapshot.method_l_2(), true));
      var4.add(this.a("Server", snapshot.method_k_2(), true));
      var4.add(this.a("All spawners mined", snapshot.o() ? "✅ Yes" : "❌ No", false));
      var4.add(this.a("Spawners in bag", snapshot.method_l_1() + " spawners", false));
      if (!snapshot.i().isBlank()) {
         var4.add(this.a("Threat", snapshot.i() + " (" + snapshot.j() + " blocks)", false));
      }

      var3.add("fields", var4);
      JsonObject var10 = new JsonObject();
      var10.addProperty("url", snapshot.m());
      var3.add("thumbnail", var10);
      var4 = new JsonArray();
      var4.add(var3);
      var2.add("embeds", var4);
      SpawnerProtect.b var6 = HttpRequest.newBuilder(this.a(snapshot.f()))
         .timeout(field_a_1)
         .header("Content-Type", "application/json")
         .header("Accept", "application/json")
         .header("User-Agent", "Water-CoordSnapper")
         .POST(BodyPublishers.ofString(var2.toString()))
         .build();

      try {
         var7 = field_a_3.send(var6, BodyHandlers.ofString());
      } catch (Exception var5) {
         WaterClient.a.error("SpawnerProtect webhook request failed", var5);
         throw new IllegalStateException("Webhook request failed", var5);
      }

      int var9 = var7.statusCode();
      if (var9 >= 200 && var9 < 300) {
         WaterClient.a.info("SpawnerProtect webhook sent");
      } else {
         SpawnerProtect.b var8 = (String)var7.body();
         if (var8 != null && !var8.isBlank()) {
            WaterClient.a.warn("SpawnerProtect webhook rejected with status {} and body {}", var9, this.a(var8, 240));
            throw new IllegalStateException("HTTP " + var9 + ": " + this.a(var8, 120));
         } else {
            WaterClient.a.warn("SpawnerProtect webhook rejected with status {}", var9);
            throw new IllegalStateException("HTTP " + var9);
         }
      }
   }

   private JsonObject a(String name, String value, boolean inline) {
      JsonObject var4 = new JsonObject();
      var4.addProperty("name", name);
      var4.addProperty("value", value != null && !value.isBlank() ? value : "-");
      var4.addProperty("inline", inline);
      return var4;
   }

   private String e() {
      ServerInfo var1 = mc.getCurrentServerEntry();
      if (var1 != null && var1.address != null && !var1.address.isBlank()) {
         String var2 = this.d(var1.address);
         return var2.isEmpty() ? "Singleplayer" : var2;
      } else {
         return "Singleplayer";
      }
   }

   private String d(String address) {
      address = address == null ? "" : address.trim().toLowerCase(Locale.ROOT);
      int var2 = address.indexOf(47);
      if (var2 >= 0) {
         address = address.substring(0, var2);
      }

      var2 = address.indexOf(58);
      if (var2 >= 0) {
         address = address.substring(0, var2);
      }

      return address;
   }

   private String e(String value) {
      return value == null ? "" : value.trim();
   }

   private String f(String playerName) {
      return playerName != null && !playerName.isBlank() ? playerName.trim() : "Steve";
   }

   private URI a(String webhook) {
      URI var2 = URI.create(webhook);
      String var3 = var2.getQuery();
      if (var3 == null || var3.isBlank()) {
         return URI.create(webhook + "?wait=true");
      } else {
         return var3.contains("wait=") ? var2 : URI.create(webhook + "&wait=true");
      }
   }

   private boolean b(String webhook) {
      if (webhook.isEmpty()) {
         return false;
      } else {
         try {
            String var5 = URI.create(webhook);
            String var2 = var5.getScheme();
            String var3 = var5.getHost();
            webhook = var5.getPath();
            return ("https".equalsIgnoreCase(var2) || "http".equalsIgnoreCase(var2))
               && var3 != null
               && !var3.isBlank()
               && webhook != null
               && webhook.contains("/api/webhooks/");
         } catch (Exception var4) {
            return false;
         }
      }
   }

   private String a(String value, int maxLength) {
      if (value == null) {
         return "";
      } else {
         value = value.replace('\n', ' ').replace('\r', ' ').trim();
         return value.length() <= maxLength ? value : value.substring(0, Math.max(0, maxLength - 3)) + "...";
      }
   }

   private int j() {
      int var1 = 0;

      for (int var2 = 0; var2 < mc.player.getInventory().size(); var2++) {
         ItemStack var3 = mc.player.getInventory().getStack(var2);
         if (!var3.isEmpty() && var3.isOf(Blocks.SPAWNER.asItem())) {
            var1 += var3.getCount();
         }
      }

      return var1;
   }

   private void q(String reason) {
      if (mc.getNetworkHandler() != null && mc.getNetworkHandler().getConnection() != null) {
         mc.getNetworkHandler().getConnection().disconnect(Text.literal(reason));
      }

      this.setEnabled(false);
   }

   private double method_a_2(BlockPos pos) {
      return mc.player.squaredDistanceTo(Vec3d.ofCenter(pos));
   }

   private static double a(int value) {
      return (double)value * value;
   }

   private void ag() {
      this.b = null;
      this.v = false;
      this.q = 0;
   }

   private record a(boolean w, boolean x, PlayerEntity field_a_1, double field_a_2) {
      public boolean m() {
         return this.w;
      }

      public boolean n() {
         return this.x;
      }

      public PlayerEntity b() {
         return this.field_a_1;
      }

      public double a() {
         return this.field_a_2;
      }
   }

   private record b(
      String j,
      String k,
      String l,
      int field_r_1,
      String m,
      String n,
      String o,
      int field_s_1,
      boolean y,
      String p,
      String q,
      String field_r_2,
      String field_s_2
   ) {
      public String f() {
         return this.j;
      }

      public String g() {
         return this.k;
      }

      public String h() {
         return this.l;
      }

      public int method_k_1() {
         return this.field_r_1;
      }

      public String playerName() {
         return this.m;
      }

      public String i() {
         return this.n;
      }

      public String j() {
         return this.o;
      }

      public int method_l_1() {
         return this.field_s_1;
      }

      public boolean o() {
         return this.y;
      }

      public String method_k_2() {
         return this.p;
      }

      public String method_l_2() {
         return this.q;
      }

      public String m() {
         return this.field_r_2;
      }

      public String n() {
         return this.field_s_2;
      }
   }
}
