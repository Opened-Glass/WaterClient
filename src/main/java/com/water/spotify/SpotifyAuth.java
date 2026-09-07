package com.water.spotify;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class SpotifyAuth {
   private static String af = "";
   private static final Path a = Paths.get(System.getProperty("user.home"), ".water_spotify_token");
   private static volatile String ag = null;
   private static volatile String ah = null;
   private static volatile long ab = 0L;
   private static volatile boolean ax = false;
   private static final ExecutorService f = Executors.newSingleThreadExecutor(r -> {
      Runnable var1 = new Thread(r, "spotify-auth");
      var1.setDaemon(true);
      return var1;
   });

   public static void v(String id) {
      if (id != null && !id.isBlank() && !id.equals(af)) {
         af = id.trim();

         try {
            Files.deleteIfExists(a);
         } catch (Exception var1) {
         }

         ag = null;
         ah = null;
         ab = 0L;
      }
   }

   public static String s() {
      return af;
   }

   public static boolean ad() {
      return !af.isBlank();
   }

   public static boolean ae() {
      if (ag != null && System.currentTimeMillis() < ab - 30000L) {
         return true;
      } else {
         bi();
         return ag != null && System.currentTimeMillis() < ab - 30000L;
      }
   }

   public static String t() {
      if (!ae()) {
         return null;
      } else {
         if (System.currentTimeMillis() > ab - 60000L) {
            f.submit(SpotifyAuth::bg);
         }

         return ag;
      }
   }

   public static void a(Runnable onSuccess, Runnable onFail) {
      if (ad() && !ax) {
         ax = true;
         f.submit(
            () -> {
               try {
                  byte[] var2 = new byte[64];
                  new SecureRandom().nextBytes(var2);
                  String var9 = Base64.getUrlEncoder().withoutPadding().encodeToString(var2);
                  MessageDigest var3 = MessageDigest.getInstance("SHA-256");
                  byte[] var11 = var3.digest(var9.getBytes(StandardCharsets.US_ASCII));
                  String var12 = Base64.getUrlEncoder().withoutPadding().encodeToString(var11);
                  byte[] var4 = new byte[16];
                  new SecureRandom().nextBytes(var4);
                  String var15 = Base64.getUrlEncoder().withoutPadding().encodeToString(var4);
                  String var13 = "https://accounts.spotify.com/authorize?client_id="
                     + URLEncoder.encode(af, StandardCharsets.UTF_8)
                     + "&response_type=code&redirect_uri="
                     + URLEncoder.encode("http://localhost:8888/callback", StandardCharsets.UTF_8)
                     + "&scope="
                     + URLEncoder.encode(
                        "user-read-playback-state user-modify-playback-state user-read-currently-playing playlist-read-private playlist-read-collaborative",
                        StandardCharsets.UTF_8
                     )
                     + "&state="
                     + var15
                     + "&code_challenge_method=S256&code_challenge="
                     + var12;
                  Desktop.getDesktop().browse(new URI(var13));
                  String var14 = k(var15);
                  if (var14 == null) {
                     if (onFail != null) {
                        onFail.run();
                     }

                     return;
                  }

                  String var10 = e(var14, var9);
                  if (var10 != null) {
                     w(var10);
                     if (onSuccess != null) {
                        onSuccess.run();
                     }

                     return;
                  }

                  if (onFail != null) {
                     onFail.run();
                  }
               } catch (Exception var7) {
                  if (onFail != null) {
                     onFail.run();
                  }

                  return;
               } finally {
                  ax = false;
               }
            }
         );
      }
   }

   private static String k(String expectedState) throws Exception {
      try (ServerSocket var1 = new ServerSocket(8888)) {
         var1.setSoTimeout(120000);

         try (Socket var2 = var1.accept()) {
            BufferedReader var3 = new BufferedReader(new InputStreamReader(var2.getInputStream(), StandardCharsets.UTF_8));
            String var4 = var3.readLine();
            if (var4 == null) {
               return null;
            }

            String var13 = var4.split(" ")[1];
            String var14 = var13.contains("?") ? var13.split("\\?", 2)[1] : "";
            Object var17 = null;
            String var5 = null;

            for (String var8 : var14.split("&")) {
               String[] var19 = var8.split("=", 2);
               if (var19.length == 2) {
                  if (var19[0].equals("code")) {
                     var17 = URLDecoder.decode(var19[1], StandardCharsets.UTF_8);
                  }

                  if (var19[0].equals("state")) {
                     var5 = URLDecoder.decode(var19[1], StandardCharsets.UTF_8);
                  }
               }
            }

            String var16 = "HTTP/1.1 200 OK\r\nContent-Type: text/html\r\n\r\n<html><body style='background:#0a0e18;color:#5be6d0;font-family:sans-serif;text-align:center;padding:50px'><h2>✓ Water Client connected to Spotify</h2><p>You can close this tab and return to Minecraft.</p></body></html>";
            var2.getOutputStream().write(var16.getBytes(StandardCharsets.UTF_8));
            if (!expectedState.equals(var5)) {
               return null;
            }
         }
      }

      Object var18;
      return (String)var18;
   }

   private static String e(String code, String codeVerifier) throws Exception {
      code = "grant_type=authorization_code&code="
         + URLEncoder.encode(code, StandardCharsets.UTF_8)
         + "&redirect_uri="
         + URLEncoder.encode("http://localhost:8888/callback", StandardCharsets.UTF_8)
         + "&client_id="
         + URLEncoder.encode(af, StandardCharsets.UTF_8)
         + "&code_verifier="
         + URLEncoder.encode(codeVerifier, StandardCharsets.UTF_8);
      String var10 = new URL("https://accounts.spotify.com/api/token");
      String var11 = (HttpURLConnection)var10.openConnection();
      var11.setRequestMethod("POST");
      var11.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
      var11.setDoOutput(true);
      var11.setConnectTimeout(10000);
      var11.setReadTimeout(10000);

      try (OutputStream var2 = var11.getOutputStream()) {
         var2.write(code.getBytes(StandardCharsets.UTF_8));
      }

      if (var11.getResponseCode() != 200) {
         return null;
      } else {
         try (BufferedReader var13 = new BufferedReader(new InputStreamReader(var11.getInputStream(), StandardCharsets.UTF_8))) {
            String var8 = new StringBuilder();

            while ((codeVerifier = var13.readLine()) != null) {
               var8.append(codeVerifier);
            }

            code = var8.toString();
         }

         return code;
      }
   }

   private static void bg() {
      if (ah != null) {
         try {
            String var0 = "grant_type=refresh_token&refresh_token="
               + URLEncoder.encode(ah, StandardCharsets.UTF_8)
               + "&client_id="
               + URLEncoder.encode(af, StandardCharsets.UTF_8);
            URL var1 = new URL("https://accounts.spotify.com/api/token");
            HttpURLConnection var9 = (HttpURLConnection)var1.openConnection();
            var9.setRequestMethod("POST");
            var9.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            var9.setDoOutput(true);
            var9.setConnectTimeout(10000);
            var9.setReadTimeout(10000);

            try (OutputStream var2 = var9.getOutputStream()) {
               var2.write(var0.getBytes(StandardCharsets.UTF_8));
            }

            if (var9.getResponseCode() != 200) {
               return;
            }

            try (BufferedReader var11 = new BufferedReader(new InputStreamReader(var9.getInputStream(), StandardCharsets.UTF_8))) {
               StringBuilder var8 = new StringBuilder();

               while ((var10 = var11.readLine()) != null) {
                  var8.append(var10);
               }

               w(var8.toString());
            }
         } catch (Exception var7) {
         }
      }
   }

   private static void w(String json) {
      ag = f(json, "access_token");
      String var1 = f(json, "refresh_token");
      if (var1 != null) {
         ah = var1;
      }

      json = f(json, "expires_in");
      long var3 = json != null ? Long.parseLong(json) : 3600L;
      ab = System.currentTimeMillis() + var3 * 1000L;
      bh();
   }

   private static void bh() {
      try {
         String var0 = ag + "\n" + ah + "\n" + ab;
         Files.writeString(a, var0, StandardCharsets.UTF_8);
      } catch (Exception var1) {
      }
   }

   private static void bi() {
      try {
         if (!Files.exists(a)) {
            return;
         }

         String[] var0 = Files.readString(a, StandardCharsets.UTF_8).split("\n");
         if (var0.length >= 3) {
            ag = var0[0].trim();
            ah = var0[1].trim();
            ab = Long.parseLong(var0[2].trim());
            if (System.currentTimeMillis() > ab - 30000L && ah != null) {
               bg();
            }
         }
      } catch (Exception var1) {
      }
   }

   private static String f(String json, String key) {
      key = "\"" + key + "\":";
      int var2 = json.indexOf(key);
      if (var2 < 0) {
         return null;
      } else {
         var2 += key.length();

         while (var2 < json.length() && (json.charAt(var2) == ' ' || json.charAt(var2) == '"')) {
            var2++;
         }

         String var4 = var2;

         while (var4 < json.length() && json.charAt(var4) != '"' && json.charAt(var4) != ',' && json.charAt(var4) != '}') {
            var4++;
         }

         return json.substring(var2, var4).trim();
      }
   }

   public static boolean af() {
      return ax;
   }
}
