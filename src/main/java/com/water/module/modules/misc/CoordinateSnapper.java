package com.water.module.modules.misc;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.water.WaterClient;
import com.water.gui.ToastManager;
import com.water.module.ActivatableModule;
import com.water.module.Category;
import com.water.setting.Setting;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Util;

public final class CoordinateSnapper extends ActivatableModule {
   private static final Duration field_b_1 = Duration.ofSeconds(8L);
   private static final ItemStack field_a_1 = new ItemStack(Items.RECOVERY_COMPASS);
   private static final DateTimeFormatter field_b_2 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z", Locale.ROOT);
   private static final HttpClient field_b_3 = HttpClient.newBuilder().connectTimeout(field_b_1).followRedirects(Redirect.NORMAL).build();
   private final Setting<String> field_a_2 = new Setting<String>("Webhook", "") {
      @Override
      public boolean matchesName(String settingName) {
         return super.matchesName(settingName) || "Webhook URL".equalsIgnoreCase(settingName);
      }
   };
   private final Setting<Boolean> aF = new Setting<>("Notification", true);
   private volatile long w;

   public CoordinateSnapper() {
      super("CoordSnapper", Category.c);
      this.addSetting(this.field_a_2);
      this.addSetting(this.aF);
   }

   @Override
   public void b() {
      if (this.isEnabled() && mc.player != null) {
         long var1 = System.currentTimeMillis();
         if (var1 - this.w >= 250L) {
            this.w = var1;
            String var3 = this.e(this.field_a_2.getValue());
            if (!this.b(var3)) {
               this.a("Webhook invalid", "Set a Discord webhook URL.", -1002662);
            } else {
               CoordinateSnapper.a var4 = this.method_a_1(var3);
               CompletableFuture.runAsync(() -> this.a(var4), Util.getIoWorkerExecutor().named("coordsnapper-send"))
                  .whenComplete((ignored, throwable) -> mc.execute(() -> {
                     if (throwable != null) {
                        this.a("Send failed", this.getRootMessage(throwable), -1938838);
                     } else {
                        this.a("Coords sent", var4.p(), -11152222);
                     }
                  }));
            }
         }
      }
   }

   private CoordinateSnapper.a method_a_1(String webhook) {
      int var2 = mc.player.getBlockX();
      int var3 = mc.player.getBlockY();
      int var4 = mc.player.getBlockZ();
      String var5 = mc.player.getName().getString();
      String var6 = this.e();
      String var7 = field_b_2.format(ZonedDateTime.now());
      String var8 = this.f(var5);
      var8 = "https://mc-heads.net/body/" + var8;
      return new CoordinateSnapper.a(webhook, var5, var2, var3, var4, var6, var7, var8);
   }

   private void a(CoordinateSnapper.a snapshot) {
      JsonObject var2 = new JsonObject();
      var2.addProperty("username", "CoordSnapper");
      JsonObject var3 = new JsonObject();
      var3.addProperty("title", "CoordSnapper");
      var3.addProperty("color", 5624994);
      JsonArray var4 = new JsonArray();
      var4.add(this.a("Name", snapshot.playerName(), false));
      var4.add(this.a("Coords", snapshot.o(), false));
      var4.add(this.a("IP", snapshot.k(), true));
      var4.add(this.a("Time", snapshot.l(), true));
      var3.add("fields", var4);
      JsonObject var10 = new JsonObject();
      var10.addProperty("url", snapshot.m());
      var3.add("thumbnail", var10);
      var4 = new JsonArray();
      var4.add(var3);
      var2.add("embeds", var4);
      HttpRequest var7 = HttpRequest.newBuilder(this.method_a_2(snapshot.f()))
         .timeout(field_b_1)
         .header("Content-Type", "application/json")
         .header("Accept", "application/json")
         .header("User-Agent", "Water-CoordSnapper")
         .POST(BodyPublishers.ofString(var2.toString()))
         .build();

      try {
         var8 = field_b_3.send(var7, BodyHandlers.ofString());
      } catch (Exception var5) {
         WaterClient.a.error("CoordSnapper webhook request failed", var5);
         throw new IllegalStateException("Webhook request failed", var5);
      }

      int var9 = var8.statusCode();
      if (var9 >= 200 && var9 < 300) {
         WaterClient.a.info("CoordSnapper sent coords for {}", snapshot.playerName());
      } else {
         CoordinateSnapper.a var6 = (String)var8.body();
         if (var6 != null && !var6.isBlank()) {
            WaterClient.a.warn("CoordSnapper webhook rejected with status {} and body {}", var9, this.a(var6, 240));
            throw new IllegalStateException("HTTP " + var9 + ": " + this.a(var6, 120));
         } else {
            WaterClient.a.warn("CoordSnapper webhook rejected with status {}", var9);
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

   private URI method_a_2(String webhook) {
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

   private void a(String message, String details, int accentColor) {
      if (mc != null && this.aF.getValue()) {
         mc.execute(() -> ToastManager.INSTANCE.push(message, details, field_a_1, accentColor));
      }
   }

   private String getRootMessage(Throwable throwable) {
      throwable = throwable;

      while (throwable.getCause() != null) {
         throwable = throwable.getCause();
      }

      String var2 = throwable.getMessage();
      return var2 != null && !var2.isBlank() ? this.a(var2, 120) : throwable.getClass().getSimpleName();
   }

   private String a(String value, int maxLength) {
      if (value == null) {
         return "";
      } else {
         value = value.replace('\n', ' ').replace('\r', ' ').trim();
         return value.length() <= maxLength ? value : value.substring(0, Math.max(0, maxLength - 3)) + "...";
      }
   }

   private record a(String v, String w, int am, int an, int ao, String x, String y, String z) {
      private String o() {
         return "X: " + this.am + " Y: " + this.an + " Z: " + this.ao;
      }

      private String p() {
         return this.am + ", " + this.an + ", " + this.ao;
      }

      public String f() {
         return this.v;
      }

      public String playerName() {
         return this.w;
      }

      public String k() {
         return this.x;
      }

      public String l() {
         return this.y;
      }

      public String m() {
         return this.z;
      }
   }
}
