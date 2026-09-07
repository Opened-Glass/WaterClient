package com.water.utils;

import com.water.module.modules.misc.NameProtect;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;

public final class NameProtectUtil {
   private NameProtectUtil() {
   }

   public static String replace(String text) {
      if (text != null && isActive()) {
         String var1 = getRealName();
         String var2 = getFakeName();
         if (var1 != null && var2 != null && !var1.isBlank() && !var1.equals(var2)) {
            return text.contains(var1) ? text.replace(var1, var2) : text;
         } else {
            return text;
         }
      } else {
         return text;
      }
   }

   public static StringVisitable replace(StringVisitable visitable) {
      if (visitable != null && isActive()) {
         List var1 = replaceChunks(collect(visitable));
         if (var1 == null) {
            return visitable;
         } else if (var1.isEmpty()) {
            return StringVisitable.EMPTY;
         } else {
            StringVisitable var3 = new ArrayList(var1.size());

            for (NameProtectUtil.StyledChunk var2 : var1) {
               var3.add(StringVisitable.styled(var2.text(), var2.style()));
            }

            return StringVisitable.concat(var3);
         }
      } else {
         return visitable;
      }
   }

   public static OrderedText replace(OrderedText orderedText) {
      if (orderedText != null && isActive()) {
         List var1 = replaceChunks(collect(orderedText));
         if (var1 == null) {
            return orderedText;
         } else if (var1.isEmpty()) {
            return OrderedText.empty();
         } else {
            OrderedText var3 = new ArrayList(var1.size());

            for (NameProtectUtil.StyledChunk var2 : var1) {
               var3.add(OrderedText.styledForwardsVisitedString(var2.text(), var2.style()));
            }

            return OrderedText.concat(var3);
         }
      } else {
         return orderedText;
      }
   }

   private static boolean isActive() {
      return NameProtect.instance != null && NameProtect.instance.isEnabled() && getRealName() != null && !getRealName().isBlank();
   }

   private static String getRealName() {
      MinecraftClient var0 = MinecraftClient.getInstance();
      return var0 != null && var0.getSession() != null ? var0.getSession().getUsername() : null;
   }

   private static String getFakeName() {
      return NameProtect.instance == null ? null : NameProtect.instance.getFakeName();
   }

   private static List<NameProtectUtil.StyledChunk> collect(StringVisitable visitable) {
      ArrayList var1 = new ArrayList();
      visitable.visit((style, text) -> {
         appendCodePoints(var1, text, style);
         return Optional.empty();
      }, Style.EMPTY);
      return var1;
   }

   private static List<NameProtectUtil.StyledChunk> collect(OrderedText orderedText) {
      ArrayList var1 = new ArrayList();
      orderedText.accept((index, style, codePoint) -> {
         var1.add(new NameProtectUtil.StyledChunk(new String(Character.toChars(codePoint)), style));
         return true;
      });
      return var1;
   }

   private static void appendCodePoints(List<NameProtectUtil.StyledChunk> chunks, String text, Style style) {
      if (text != null && !text.isEmpty()) {
         int var3 = 0;

         while (var3 < text.length()) {
            int var4 = text.codePointAt(var3);
            chunks.add(new NameProtectUtil.StyledChunk(new String(Character.toChars(var4)), style));
            var3 += Character.charCount(var4);
         }
      }
   }

   private static List<NameProtectUtil.StyledChunk> replaceChunks(List<NameProtectUtil.StyledChunk> chunks) {
      String var1 = getRealName();
      String var2 = getFakeName();
      if (var1 != null && var2 != null && !var1.isBlank() && !var1.equals(var2)) {
         StringBuilder var3 = new StringBuilder();
         ArrayList var4 = new ArrayList(chunks.size());

         for (NameProtectUtil.StyledChunk var6 : chunks) {
            var4.add(var3.length());
            var3.append(var6.text());
         }

         String var10 = var3.toString();
         if (!var10.contains(var1)) {
            return null;
         } else {
            ArrayList var11 = new ArrayList();
            int var9 = 0;
            int var7 = 0;

            while ((var7 = var10.indexOf(var1, var7)) >= 0) {
               while (var9 < chunks.size() && var4.get(var9) < var7) {
                  var11.add((NameProtectUtil.StyledChunk)chunks.get(var9++));
               }

               Style var8 = var9 < chunks.size() ? ((NameProtectUtil.StyledChunk)chunks.get(var9)).style() : Style.EMPTY;
               var11.add(new NameProtectUtil.StyledChunk(var2, var8));
               var7 += var1.length();

               while (var9 < chunks.size() && var4.get(var9) < var7) {
                  var9++;
               }

               var7 = var7;
            }

            while (var9 < chunks.size()) {
               var11.add((NameProtectUtil.StyledChunk)chunks.get(var9++));
            }

            return merge(var11);
         }
      } else {
         return null;
      }
   }

   private static List<NameProtectUtil.StyledChunk> merge(List<NameProtectUtil.StyledChunk> chunks) {
      if (chunks.isEmpty()) {
         return chunks;
      } else {
         ArrayList var1 = new ArrayList(chunks.size());
         NameProtectUtil.StyledChunk var2 = (NameProtectUtil.StyledChunk)chunks.getFirst();

         for (int var3 = 1; var3 < chunks.size(); var3++) {
            NameProtectUtil.StyledChunk var4 = (NameProtectUtil.StyledChunk)chunks.get(var3);
            if (Objects.equals(var2.style(), var4.style())) {
               var2 = new NameProtectUtil.StyledChunk(var2.text() + var4.text(), var2.style());
            } else {
               var1.add(var2);
               var2 = var4;
            }
         }

         var1.add(var2);
         return var1;
      }
   }

   private record StyledChunk(String text, Style style) {
      private StyledChunk(String text, Style style) {
         style = style == null ? Style.EMPTY : style;
         this.text = text;
         this.style = style;
      }
   }
}
