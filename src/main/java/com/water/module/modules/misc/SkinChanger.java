package com.water.module.modules.misc;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.water.module.Category;
import com.water.module.Module;
import com.water.setting.Setting;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Base64;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.client.texture.PlayerSkinTextureDownloader;
import net.minecraft.entity.player.PlayerSkinType;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.AssetInfo.TextureAsset;

public final class SkinChanger extends Module {
   private static final Duration HTTP_TIMEOUT = Duration.ofSeconds(10L);
   private static final long INPUT_DEBOUNCE_MS = 600L;
   private static final String PRIMARY_LOOKUP_URL = "https://api.mojang.com/users/profiles/minecraft/";
   private static final String FALLBACK_LOOKUP_URL = "https://api.minecraftservices.com/minecraft/profile/lookup/name/";
   private static final String PROFILE_LOOKUP_URL = "https://sessionserver.mojang.com/session/minecraft/profile/";
   private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder().connectTimeout(HTTP_TIMEOUT).followRedirects(Redirect.NORMAL).build();
   private static volatile SkinTextures overrideSkin;
   private static volatile TextureAsset overrideTextureAsset;
   private final Setting<String> playerName = new Setting<>("Player Name", "");
   private final AtomicInteger requestGeneration = new AtomicInteger();
   private PlayerSkinTextureDownloader skinDownloader;
   private String lastObservedName = "";
   private String lastRequestedName = "";
   private long lastNameEditAt;

   public SkinChanger() {
      super("SkinChanger", Category.c);
      this.addSetting(this.playerName);
   }

   @Override
   public void onEnable() {
      super.onEnable();
      this.lastObservedName = normalizeName(this.playerName.getValue());
      this.lastRequestedName = "";
      this.lastNameEditAt = System.currentTimeMillis();
      if (this.lastObservedName.isEmpty()) {
         clearOverride();
      } else {
         this.requestSkin(this.lastObservedName);
      }
   }

   @Override
   public void onDisable() {
      super.onDisable();
      this.requestGeneration.incrementAndGet();
      this.lastRequestedName = "";
      clearOverride();
   }

   @Override
   public void onTick() {
      String var1 = normalizeName(this.playerName.getValue());
      if (!Objects.equals(var1, this.lastObservedName)) {
         this.lastObservedName = var1;
         this.lastNameEditAt = System.currentTimeMillis();
      } else if (!var1.isEmpty()) {
         if (!Objects.equals(var1, this.lastRequestedName) && System.currentTimeMillis() - this.lastNameEditAt >= 600L) {
            this.requestSkin(var1);
         }
      } else {
         if (overrideSkin != null || overrideTextureAsset != null) {
            this.lastRequestedName = "";
            clearOverride();
         }
      }
   }

   public static SkinTextures getOverrideSkin(UUID playerUuid) {
      if (overrideSkin != null && playerUuid != null) {
         UUID var1 = getLocalPlayerUuid();
         return var1 != null && var1.equals(playerUuid) ? overrideSkin : null;
      } else {
         return null;
      }
   }

   private void requestSkin(String name) {
      this.lastRequestedName = name;
      int var2 = this.requestGeneration.incrementAndGet();
      CompletableFuture.<SkinChanger.SkinLookup>supplyAsync(() -> this.lookupSkin(name), Util.getIoWorkerExecutor().named("skinchanger-lookup"))
         .thenCompose(
            result -> this.getSkinDownloader()
               .downloadAndRegisterTexture(this.createTextureId(result), this.getCacheFile(result.uuid()), result.textureUrl(), true)
               .thenApply(textureAsset -> new SkinChanger.ResolvedSkin(result, textureAsset))
         )
         .whenComplete((resolvedSkin, throwable) -> mc.execute(() -> {
            if (var2 == this.requestGeneration.get() && this.isEnabled()) {
               if (throwable != null) {
                  this.sendFeedback("Failed to apply skin for " + name + ": " + getRootMessage(throwable));
               } else {
                  applyOverride(resolvedSkin.textureAsset(), resolvedSkin.lookup().skinType());
                  this.sendFeedback("Applied skin from " + resolvedSkin.lookup().playerName() + ".");
               }
            } else {
               if (resolvedSkin != null) {
                  destroyTexture(resolvedSkin.textureAsset());
               }
            }
         }));
   }

   private PlayerSkinTextureDownloader getSkinDownloader() {
      if (this.skinDownloader == null) {
         this.skinDownloader = new PlayerSkinTextureDownloader(mc.getNetworkProxy(), mc.getTextureManager(), mc::execute);
      }

      return this.skinDownloader;
   }

   private SkinChanger.SkinLookup lookupSkin(String playerName) {
      try {
         UUID var2 = this.lookupUuid(playerName);
         SkinChanger.TexturePayload var3 = this.lookupTexturePayload(var2);
         return new SkinChanger.SkinLookup(playerName, var2, var3.textureUrl(), var3.skinType());
      } catch (InterruptedException var4) {
         Thread.currentThread().interrupt();
         throw new IllegalStateException("Request interrupted", var4);
      } catch (IOException var5) {
         throw new IllegalStateException(var5.getMessage(), var5);
      }
   }

   private UUID lookupUuid(String playerName) throws IOException, InterruptedException {
      JsonObject var2 = this.requestJson("https://api.mojang.com/users/profiles/minecraft/" + this.encodeName(playerName));
      if (var2 == null) {
         var2 = this.requestJson("https://api.minecraftservices.com/minecraft/profile/lookup/name/" + this.encodeName(playerName));
      }

      if (var2 != null && var2.has("id")) {
         return parseUuid(var2.get("id").getAsString());
      } else {
         throw new IOException("Player not found");
      }
   }

   private SkinChanger.TexturePayload lookupTexturePayload(UUID uuid) throws IOException, InterruptedException {
      UUID var4 = this.requestJson("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid.toString().replace("-", ""));
      if (var4 != null && var4.has("properties")) {
         for (JsonElement var2 : var4.getAsJsonArray("properties")) {
            if (var2.isJsonObject()) {
               JsonObject var11 = var2.getAsJsonObject();
               if ("textures".equalsIgnoreCase(getString(var11, "name")) && var11.has("value")) {
                  UUID var7 = new String(Base64.getDecoder().decode(var11.get("value").getAsString()), StandardCharsets.UTF_8);
                  UUID var8 = JsonParser.parseString(var7).getAsJsonObject();
                  UUID var9 = var8.getAsJsonObject("textures");
                  UUID var10 = var9 != null ? var9.getAsJsonObject("SKIN") : null;
                  if (var10 != null && var10.has("url")) {
                     String var12 = null;
                     JsonObject var3 = var10.getAsJsonObject("metadata");
                     if (var3 != null && var3.has("model")) {
                        var12 = var3.get("model").getAsString();
                     }

                     PlayerSkinType var13 = "slim".equalsIgnoreCase(var12) ? PlayerSkinType.SLIM : PlayerSkinType.WIDE;
                     return new SkinChanger.TexturePayload(var10.get("url").getAsString(), var13);
                  }
                  break;
               }
            }
         }

         throw new IOException("No usable skin texture found");
      } else {
         throw new IOException("Skin profile not found");
      }
   }

   private JsonObject requestJson(String url) throws IOException, InterruptedException {
      String var3 = HttpRequest.newBuilder(URI.create(url))
         .timeout(HTTP_TIMEOUT)
         .header("Accept", "application/json")
         .header("User-Agent", "Water-SkinChanger")
         .GET()
         .build();
      String var4 = HTTP_CLIENT.send(var3, BodyHandlers.ofString());
      int var2 = var4.statusCode();
      if (var2 == 404 || var2 == 204) {
         return null;
      } else if (var2 >= 200 && var2 < 300) {
         url = (String)var4.body();
         return url != null && !url.isBlank() ? JsonParser.parseString(url).getAsJsonObject() : null;
      } else {
         throw new IOException("HTTP " + var2);
      }
   }

   private static synchronized void applyOverride(TextureAsset textureAsset, PlayerSkinType skinType) {
      destroyTexture(overrideTextureAsset);
      overrideTextureAsset = textureAsset;
      overrideSkin = SkinTextures.create(textureAsset, null, null, skinType);
   }

   private static synchronized void clearOverride() {
      destroyTexture(overrideTextureAsset);
      overrideTextureAsset = null;
      overrideSkin = null;
   }

   private static void destroyTexture(TextureAsset textureAsset) {
      if (textureAsset != null && mc != null) {
         try {
            mc.getTextureManager().destroyTexture(textureAsset.texturePath());
         } catch (Throwable var2) {
         }

         try {
            if (!textureAsset.id().equals(textureAsset.texturePath())) {
               mc.getTextureManager().destroyTexture(textureAsset.id());
            }
         } catch (Throwable var1) {
         }
      }
   }

   private void sendFeedback(String message) {
      if (mc != null && mc.inGameHud != null) {
         try {
            mc.inGameHud.getChatHud().addMessage(Text.literal("[SkinChanger] " + message));
         } catch (Throwable var2) {
         }
      }
   }

   private Identifier createTextureId(SkinChanger.SkinLookup lookup) {
      String var2 = lookup.playerName().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_]", "");
      if (var2.isEmpty()) {
         var2 = "player";
      }

      return Identifier.of("water", "skins/" + var2 + "_" + lookup.uuid().toString().replace("-", ""));
   }

   private Path getCacheFile(UUID uuid) {
      return mc.runDirectory.toPath().resolve("water-cache").resolve("skins").resolve(uuid.toString().replace("-", "") + ".png");
   }

   private String encodeName(String playerName) {
      return URLEncoder.encode(playerName, StandardCharsets.UTF_8);
   }

   private static UUID parseUuid(String rawUuid) {
      rawUuid = rawUuid.replace("-", "");
      return UUID.fromString(rawUuid.replaceFirst("(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5"));
   }

   private static String normalizeName(String value) {
      return value == null ? "" : value.trim();
   }

   private static String getString(JsonObject object, String key) {
      return object.has(key) ? object.get(key).getAsString() : "";
   }

   private static String getRootMessage(Throwable throwable) {
      throwable = throwable;

      while (throwable.getCause() != null) {
         throwable = throwable.getCause();
      }

      String var1 = throwable.getMessage();
      return var1 != null && !var1.isBlank() ? var1 : throwable.getClass().getSimpleName();
   }

   private static UUID getLocalPlayerUuid() {
      if (mc == null) {
         return null;
      } else if (mc.player != null) {
         return mc.player.getUuid();
      } else {
         return mc.getSession() != null ? mc.getSession().getUuidOrNull() : null;
      }
   }

   private record ResolvedSkin(SkinChanger.SkinLookup lookup, TextureAsset textureAsset) {
   }

   private record SkinLookup(String playerName, UUID uuid, String textureUrl, PlayerSkinType skinType) {
   }

   private record TexturePayload(String textureUrl, PlayerSkinType skinType) {
   }
}
