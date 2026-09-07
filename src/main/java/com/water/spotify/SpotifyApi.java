package com.water.spotify;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class SpotifyApi {
   public static List<SpotifyApi.a> b(String playlistId) {
      ArrayList var1 = new ArrayList();
      playlistId = "https://api.spotify.com/v1/playlists/" + playlistId + "/tracks?limit=50&fields=items(track(id,name,artists,uri)),next";

      while (playlistId != null) {
         playlistId = i(playlistId);
         if (playlistId == null) {
            break;
         }

         var1.addAll(c(playlistId));
         playlistId = method_b_2(playlistId, "next");
         playlistId = playlistId != null && !playlistId.equals("null") ? playlistId : null;
      }

      return var1;
   }

   public static List<SpotifyApi.a> b() {
      String var0 = i("https://api.spotify.com/v1/me/player/queue");
      if (var0 == null) {
         return new ArrayList<>();
      } else {
         ArrayList var1 = new ArrayList();
         String var2 = method_a_2(var0, "currently_playing", "id");
         String var3 = method_a_2(var0, "currently_playing", "name");
         String var4 = j(c(var0, "currently_playing"));
         String var5 = method_a_2(var0, "currently_playing", "uri");
         if (var3 != null) {
            var1.add(new SpotifyApi.a(var2, var3, var4 != null ? var4 : "", var5 != null ? var5 : ""));
         }

         var0 = d(var0, "queue");
         if (var0 != null) {
            var1.addAll(d(var0));
         }

         return var1;
      }
   }

   public static List<String[]> c() {
      String var0 = i("https://api.spotify.com/v1/me/playlists?limit=50");
      if (var0 == null) {
         return new ArrayList<>();
      } else {
         ArrayList var1 = new ArrayList();
         var0 = d(var0, "items");
         if (var0 == null) {
            return var1;
         } else {
            for (String var2 : method_e_2(var0)) {
               String var3 = method_b_2(var2, "id");
               var2 = method_b_2(var2, "name");
               if (var3 != null && var2 != null) {
                  var1.add(new String[]{var3, var2});
               }
            }

            return var1;
         }
      }
   }

   public static boolean method_e_1(String uri) {
      uri = "{\"uris\":[\"" + uri + "\"]}";
      return method_a_2("https://api.spotify.com/v1/me/player/play", uri) == 204;
   }

   public static boolean method_a_1(String contextUri, String trackUri) {
      contextUri = "{\"context_uri\":\"" + contextUri + "\",\"offset\":{\"uri\":\"" + trackUri + "\"}}";
      return method_a_2("https://api.spotify.com/v1/me/player/play", contextUri) == 204;
   }

   public static boolean aa() {
      return method_b_1("https://api.spotify.com/v1/me/player/pause", "") == 204;
   }

   public static boolean ab() {
      return method_b_1("https://api.spotify.com/v1/me/player/next", "") == 204;
   }

   public static boolean ac() {
      return method_b_1("https://api.spotify.com/v1/me/player/previous", "") == 204;
   }

   private static String i(String urlStr) {
      String var1 = SpotifyAuth.t();
      if (var1 == null) {
         return null;
      } else {
         try {
            String var6 = new URL(urlStr);
            String var7 = (HttpURLConnection)var6.openConnection();
            var7.setRequestMethod("GET");
            var7.setRequestProperty("Authorization", "Bearer " + var1);
            var7.setConnectTimeout(8000);
            var7.setReadTimeout(8000);
            if (var7.getResponseCode() == 401) {
               return null;
            } else if (var7.getResponseCode() == 204) {
               return "";
            } else if (var7.getResponseCode() != 200) {
               return null;
            } else {
               try (String var8 = new BufferedReader(new InputStreamReader(var7.getInputStream(), StandardCharsets.UTF_8))) {
                  StringBuilder var9 = new StringBuilder();

                  String var2;
                  while ((var2 = var8.readLine()) != null) {
                     var9.append(var2);
                  }

                  var1 = var9.toString();
               }

               return var1;
            }
         } catch (Exception var5) {
            return null;
         }
      }
   }

   private static int method_a_2(String urlStr, String body) {
      return method_a_1(urlStr, "PUT", body);
   }

   private static int method_b_1(String urlStr, String body) {
      return method_a_1(urlStr, "POST", body);
   }

   private static int method_a_1(String urlStr, String method, String body) {
      String var3 = SpotifyAuth.t();
      if (var3 == null) {
         return -1;
      } else {
         try {
            String var7 = new URL(urlStr);
            String var8 = (HttpURLConnection)var7.openConnection();
            var8.setRequestMethod(method);
            var8.setRequestProperty("Authorization", "Bearer " + var3);
            var8.setRequestProperty("Content-Type", "application/json");
            var8.setConnectTimeout(8000);
            var8.setReadTimeout(8000);
            if (!body.isEmpty()) {
               var8.setDoOutput(true);

               try (String var9 = var8.getOutputStream()) {
                  var9.write(body.getBytes(StandardCharsets.UTF_8));
               }
            }

            return var8.getResponseCode();
         } catch (Exception var6) {
            return -1;
         }
      }
   }

   private static List<SpotifyApi.a> c(String json) {
      ArrayList var1 = new ArrayList();
      json = d(json, "items");
      if (json == null) {
         return var1;
      } else {
         for (String var2 : method_e_2(json)) {
            var2 = c(var2, "track");
            if (var2 != null) {
               String var3 = method_b_2(var2, "id");
               String var4 = method_b_2(var2, "name");
               String var5 = j(var2);
               var2 = method_b_2(var2, "uri");
               if (var4 != null) {
                  var1.add(new SpotifyApi.a(var3, var4, var5 != null ? var5 : "", var2 != null ? var2 : ""));
               }
            }
         }

         return var1;
      }
   }

   private static List<SpotifyApi.a> d(String arr) {
      ArrayList var1 = new ArrayList();

      for (String var2 : method_e_2(arr)) {
         String var3 = method_b_2(var2, "id");
         String var4 = method_b_2(var2, "name");
         String var5 = j(var2);
         var2 = method_b_2(var2, "uri");
         if (var4 != null) {
            var1.add(new SpotifyApi.a(var3, var4, var5 != null ? var5 : "", var2 != null ? var2 : ""));
         }
      }

      return var1;
   }

   private static String j(String obj) {
      if (obj == null) {
         return null;
      } else {
         obj = d(obj, "artists");
         if (obj == null) {
            return null;
         } else {
            String var2 = method_e_2(obj);
            return var2.isEmpty() ? null : method_b_2((String)var2.get(0), "name");
         }
      }
   }

   private static String method_b_2(String json, String key) {
      if (json == null) {
         return null;
      } else {
         key = "\"" + key + "\":";
         int var2 = json.indexOf(key);
         if (var2 < 0) {
            return null;
         } else {
            var2 += key.length();

            while (var2 < json.length() && json.charAt(var2) == ' ') {
               var2++;
            }

            if (var2 >= json.length()) {
               return null;
            } else if (json.charAt(var2) == '"') {
               for (var5 = ++var2; var5 < json.length() && json.charAt(var5) != '"'; var5++) {
                  if (json.charAt(var5) == '\\') {
                     var5++;
                  }
               }

               return json.substring(var2, Math.min(var5, json.length()));
            } else if (json.charAt(var2) == 'n') {
               return null;
            } else {
               String var4 = var2;

               while (var4 < json.length() && ",}]".indexOf(json.charAt(var4)) < 0) {
                  var4++;
               }

               return json.substring(var2, var4).trim();
            }
         }
      }
   }

   private static String method_a_2(String json, String key1, String key2) {
      json = c(json, key1);
      return json != null ? method_b_2(json, key2) : null;
   }

   private static String c(String json, String key) {
      if (json == null) {
         return null;
      } else {
         key = "\"" + key + "\":";
         int var2 = json.indexOf(key);
         if (var2 < 0) {
            return null;
         } else {
            var2 += key.length();

            while (var2 < json.length() && json.charAt(var2) == ' ') {
               var2++;
            }

            return var2 < json.length() && json.charAt(var2) == '{' ? a(json, var2, '{', '}') : null;
         }
      }
   }

   private static String d(String json, String key) {
      if (json == null) {
         return null;
      } else {
         key = "\"" + key + "\":";
         int var2 = json.indexOf(key);
         if (var2 < 0) {
            return null;
         } else {
            var2 += key.length();

            while (var2 < json.length() && json.charAt(var2) == ' ') {
               var2++;
            }

            if (var2 < json.length() && json.charAt(var2) == '[') {
               json = a(json, var2, '[', ']');
               return json != null ? json.substring(1, json.length() - 1) : null;
            } else {
               return null;
            }
         }
      }
   }

   private static String a(String json, int start, char open, char close) {
      int var4 = 0;
      int var5 = start;

      for (boolean var6 = false; var5 < json.length(); var5++) {
         char var7 = json.charAt(var5);
         if (var7 == '"' && (var5 == 0 || json.charAt(var5 - 1) != '\\')) {
            var6 = !var6;
         }

         if (!var6) {
            if (var7 == open) {
               var4++;
            }

            if (var7 == close) {
               if (--var4 == 0) {
                  return json.substring(start, var5 + 1);
               }
            }
         }
      }

      return null;
   }

   private static List<String> method_e_2(String arr) {
      ArrayList var1 = new ArrayList();
      if (arr != null && !arr.isBlank()) {
         int var2 = 0;
         int var3 = -1;
         boolean var4 = false;

         for (int var5 = 0; var5 < arr.length(); var5++) {
            char var6 = arr.charAt(var5);
            if (var6 == '"' && (var5 == 0 || arr.charAt(var5 - 1) != '\\')) {
               var4 = !var4;
            }

            if (!var4) {
               if (var6 == '{') {
                  if (var2++ == 0) {
                     var3 = var5;
                  }
               } else if (var6 == '}') {
                  if (--var2 == 0 && var3 >= 0) {
                     var1.add(arr.substring(var3, var5 + 1));
                     var3 = -1;
                  }
               }
            }
         }

         return var1;
      } else {
         return var1;
      }
   }

   public static final class a {
      private String ac;
      public final String ad;
      private String g;
      public final String ae;

      public a(String id, String name, String artist, String uri) {
         this.ac = id;
         this.ad = name;
         this.g = artist;
         this.ae = uri;
      }

      public String r() {
         return this.g.isEmpty() ? this.ad : this.g + " - " + this.ad;
      }
   }
}
