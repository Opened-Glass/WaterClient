package com.water.utils;

import com.water.module.modules.donut.FakeRoles;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;

public final class FakeRolesUtil {
   private FakeRolesUtil() {
   }

   public static String replace(String text) {
      if (text != null && FakeRoles.isActive()) {
         String var1 = getRealName();
         if (var1 == null || var1.isBlank()) {
            return text;
         } else if (!text.contains(var1)) {
            return text;
         } else {
            String var2 = FakeRoles.getPrefixedNameString();
            return var2 != null && !var2.equals(var1) ? text.replace(var1, var2) : text;
         }
      } else {
         return text;
      }
   }

   public static OrderedText replace(OrderedText orderedText) {
      if (orderedText != null && FakeRoles.isActive()) {
         List var1 = replaceChunks(collectOrdered(orderedText));
         if (var1 == null) {
            return orderedText;
         } else if (var1.isEmpty()) {
            return OrderedText.empty();
         } else {
            OrderedText var3 = new ArrayList(var1.size());

            for (FakeRolesUtil.StyledChar var2 : var1) {
               var3.add(OrderedText.styledForwardsVisitedString(var2.text(), var2.style()));
            }

            return OrderedText.concat(var3);
         }
      } else {
         return orderedText;
      }
   }

   public static StringVisitable replace(StringVisitable visitable) {
      if (visitable != null && FakeRoles.isActive()) {
         List var1 = replaceChunks(collectVisitable(visitable));
         if (var1 == null) {
            return visitable;
         } else if (var1.isEmpty()) {
            return StringVisitable.EMPTY;
         } else {
            StringVisitable var3 = new ArrayList(var1.size());

            for (FakeRolesUtil.StyledChar var2 : var1) {
               var3.add(StringVisitable.styled(var2.text(), var2.style()));
            }

            return StringVisitable.concat(var3);
         }
      } else {
         return visitable;
      }
   }

   private static String getRealName() {
      MinecraftClient var0 = MinecraftClient.getInstance();
      return var0 != null && var0.getSession() != null ? var0.getSession().getUsername() : null;
   }

   private static List<FakeRolesUtil.StyledChar> collectOrdered(OrderedText orderedText) {
      ArrayList var1 = new ArrayList();
      orderedText.accept((index, style, codePoint) -> {
         var1.add(new FakeRolesUtil.StyledChar(new String(Character.toChars(codePoint)), style));
         return true;
      });
      return var1;
   }

   private static List<FakeRolesUtil.StyledChar> collectVisitable(StringVisitable visitable) {
      ArrayList var1 = new ArrayList();
      visitable.visit((style, text) -> {
         int var3 = 0;

         while (var3 < text.length()) {
            int var4 = text.codePointAt(var3);
            var1.add(new FakeRolesUtil.StyledChar(new String(Character.toChars(var4)), style));
            var3 += Character.charCount(var4);
         }

         return Optional.empty();
      }, Style.EMPTY);
      return var1;
   }

   private static List<FakeRolesUtil.StyledChar> replaceChunks(List<FakeRolesUtil.StyledChar> chars) {
      String var1 = getRealName();
      if (var1 != null && !var1.isBlank()) {
         String var2 = FakeRoles.getRolePrefixString();
         if (var2 == null) {
            return null;
         } else {
            StringBuilder var3 = new StringBuilder();
            ArrayList var4 = new ArrayList(chars.size());

            for (FakeRolesUtil.StyledChar var6 : chars) {
               var4.add(var3.length());
               var3.append(var6.text());
            }

            String var12 = var3.toString();
            if (!var12.contains(var1)) {
               return null;
            } else {
               Style var13 = FakeRoles.getRoleNameStyle();
               ArrayList var11 = new ArrayList();
               int var7 = 0;
               int var8 = 0;

               while ((var8 = var12.indexOf(var1, var8)) >= 0) {
                  while (var7 < chars.size() && var4.get(var7) < var8) {
                     var11.add((FakeRolesUtil.StyledChar)chars.get(var7++));
                  }

                  int var9 = 0;

                  while (var9 < var2.length()) {
                     int var10 = var2.codePointAt(var9);
                     var11.add(new FakeRolesUtil.StyledChar(new String(Character.toChars(var10)), FakeRoles.getRolePrefixStyleForChar(var10)));
                     var9 += Character.charCount(var10);
                  }

                  var9 = 0;

                  while (var9 < var1.length()) {
                     int var17 = var1.codePointAt(var9);
                     var11.add(new FakeRolesUtil.StyledChar(new String(Character.toChars(var17)), var13));
                     var9 += Character.charCount(var17);
                  }

                  var9 = var8 + var1.length();

                  while (var7 < chars.size() && var4.get(var7) < var9) {
                     var7++;
                  }

                  var8 = var9;
               }

               while (var7 < chars.size()) {
                  var11.add((FakeRolesUtil.StyledChar)chars.get(var7++));
               }

               return merge(var11);
            }
         }
      } else {
         return null;
      }
   }

   private static List<FakeRolesUtil.StyledChar> merge(List<FakeRolesUtil.StyledChar> chars) {
      if (chars.isEmpty()) {
         return chars;
      } else {
         ArrayList var1 = new ArrayList(chars.size());
         FakeRolesUtil.StyledChar var2 = (FakeRolesUtil.StyledChar)chars.getFirst();

         for (int var3 = 1; var3 < chars.size(); var3++) {
            FakeRolesUtil.StyledChar var4 = (FakeRolesUtil.StyledChar)chars.get(var3);
            if (Objects.equals(var2.style(), var4.style())) {
               var2 = new FakeRolesUtil.StyledChar(var2.text() + var4.text(), var2.style());
            } else {
               var1.add(var2);
               var2 = var4;
            }
         }

         var1.add(var2);
         return var1;
      }
   }

   private record StyledChar(String text, Style style) {
      private StyledChar(String text, Style style) {
         style = style == null ? Style.EMPTY : style;
         this.text = text;
         this.style = style;
      }
   }
}
