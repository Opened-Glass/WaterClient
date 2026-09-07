package com.water.module.modules.client;

import com.water.gui.ConfigManagerScreen;
import com.water.module.Category;
import com.water.module.Module;
import com.water.module.ModuleManager;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import net.minecraft.client.MinecraftClient;

public final class ConfigShare extends Module {
   private static ConfigShare a;
   public volatile String c = "";
   public volatile String d = "";
   public volatile boolean e = false;
   public volatile boolean f = false;

   public ConfigShare() {
      super("Config Share", Category.e);
      a = this;
   }

   @Override
   public void onEnable() {
      MinecraftClient var1 = MinecraftClient.getInstance();
      if (var1 != null) {
         var1.execute(() -> {
            var1.setScreen(new ConfigManagerScreen());
            this.setEnabled(false);
         });
      }
   }

   public static ConfigShare a() {
      return a;
   }

   public void a(Runnable onDone) {
      this.e = true;
      this.c = "";
      this.d = "";
      new Thread(() -> {
         try {
            String var2 = this.b();
            ByteArrayOutputStream var19 = new ByteArrayOutputStream();

            try (GZIPOutputStream var4 = new GZIPOutputStream(var19)) {
               var4.write(var2.getBytes(StandardCharsets.UTF_8));
            }

            String var22 = Base64.getUrlEncoder().withoutPadding().encodeToString(var19.toByteArray());
            HttpURLConnection var17 = (HttpURLConnection)new URI("https://dpaste.com/api/v2/").toURL().openConnection();
            var17.setRequestMethod("POST");
            var17.setDoOutput(true);
            var17.setConnectTimeout(6000);
            var17.setReadTimeout(6000);
            var17.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            String var20 = "content=" + URLEncoder.encode(var22, StandardCharsets.UTF_8) + "&syntax=text&expiry_days=365";

            try (OutputStream var23 = var17.getOutputStream()) {
               var23.write(var20.getBytes(StandardCharsets.UTF_8));
            }

            var22 = new String(var17.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
            var17.disconnect();
            var2 = var22.replaceAll("https?://dpaste\\.com/", "").replace("/", "").trim();
            this.c = var2;
            MinecraftClient var21 = MinecraftClient.getInstance();
            if (var21 != null) {
               var21.execute(() -> var21.keyboard.setClipboard(var2));
            }
         } catch (Exception var15) {
            this.d = "Upload failed: " + var15.getMessage();
         } finally {
            this.e = false;
            MinecraftClient var3 = MinecraftClient.getInstance();
            if (onDone != null && var3 != null) {
               var3.execute(onDone);
            }
         }
      }, "water-config-export").start();
   }

   public void a(String code, Runnable onDone) {
      this.f = true;
      this.d = "";
      new Thread(() -> {
         try {
            try {
               code = code.trim().replaceAll("\\s+", "");
               if (code.isEmpty()) {
                  this.d = "Enter a code first!";
                  return;
               }

               String var19 = (HttpURLConnection)new URI("https://dpaste.com/" + code + ".txt").toURL().openConnection();
               var19.setConnectTimeout(6000);
               var19.setReadTimeout(6000);
               String var22 = new String(var19.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
               var19.disconnect();

               try {
                  var20 = Base64.getUrlDecoder().decode(var22);
               } catch (Exception var15) {
                  try {
                     var20 = Base64.getDecoder().decode(var22);
                  } catch (Exception var14) {
                     this.d = "Invalid code!";
                     return;
                  }
               }

               try (GZIPInputStream var23 = new GZIPInputStream(new ByteArrayInputStream(var20))) {
                  code = new String(var23.readAllBytes(), StandardCharsets.UTF_8);
               } catch (Exception var13) {
                  this.d = "Invalid code!";
                  return;
               }

               this.i(code);
               ModuleManager.INSTANCE.f();
            } catch (Exception var16) {
               this.d = "Load error: " + var16.getMessage();
            }
         } finally {
            this.f = false;
            MinecraftClient var3 = MinecraftClient.getInstance();
            if (onDone != null && var3 != null) {
               var3.execute(onDone);
            }
         }
      }, "water-config-import").start();
   }

   private String b() {
      ModuleManager.INSTANCE.f();

      try {
         Path var1 = ModuleManager.INSTANCE.method_a_3();
         if (var1 != null && Files.isRegularFile(var1)) {
            return Files.readString(var1, StandardCharsets.UTF_8);
         }
      } catch (IOException var2) {
      }

      return "";
   }

   private void i(String raw) {
      if (raw != null && !raw.isBlank()) {
         Map var2 = this.snapshotEnabled();
         Path var3 = null;

         try {
            Path var4 = ModuleManager.INSTANCE.method_a_3();
            var3 = var4.resolveSibling(var4.getFileName().toString() + ".import");
            Files.writeString(var3, raw, StandardCharsets.UTF_8);
            if (ModuleManager.INSTANCE.a(var3)) {
               this.syncModuleLifecycle(var2, this.snapshotEnabled());
               return;
            }
         } catch (IOException var12) {
            return;
         } finally {
            if (var3 != null) {
               try {
                  Files.deleteIfExists(var3);
               } catch (IOException var11) {
               }
            }
         }
      }
   }

   private Map<String, Boolean> snapshotEnabled() {
      HashMap var1 = new HashMap();

      for (Module var3 : ModuleManager.INSTANCE.getModules()) {
         var1.put(var3.getName(), var3.isEnabled());
      }

      return var1;
   }

   private void syncModuleLifecycle(Map<String, Boolean> before, Map<String, Boolean> after) {
      for (Module var4 : ModuleManager.INSTANCE.getModules()) {
         boolean var5 = before.getOrDefault(var4.getName(), false);
         boolean var6 = after.getOrDefault(var4.getName(), false);
         if (var5 != var6) {
            try {
               if (var6) {
                  var4.onEnable();
               } else {
                  var4.onDisable();
               }
            } catch (Throwable var7) {
            }
         }
      }
   }
}
