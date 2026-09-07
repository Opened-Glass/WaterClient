package com.water.module.modules.donut;

import com.water.module.Category;
import com.water.module.Module;
import com.water.setting.ModeSetting;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;

public final class FakeRoles extends Module {
   public static FakeRoles instance;
   private static final String MODE_NONE = "None";
   private static final String MODE_SRMOD = "SRMOD";
   private static final String MODE_MEDIA = "MEDIA";
   private static final String MODE_SRADMIN = "SRADMIN";
   private static final int TAG_BRACKET = 8355711;
   private static final int TAG_SRMOD = 5635925;
   private static final int TAG_MEDIA = 16733695;
   private static final int TAG_SRADMIN = 16733525;
   private static final int TAG_WHITE = 16777215;
   private final ModeSetting role = new ModeSetting("Role", "None", "None", "SRMOD", "MEDIA", "SRADMIN");

   public FakeRoles() {
      super("FakeRoles", Category.d);
      instance = this;
      this.addSetting(this.role);
   }

   public static boolean isActive() {
      return instance != null && instance.isEnabled() && !instance.role.d("None") && mc != null && mc.player != null;
   }

   public static String getActiveRole() {
      return !isActive() ? null : instance.role.getValue();
   }

   public static String getPlayerName() {
      return mc != null && mc.getSession() != null ? mc.getSession().getUsername() : null;
   }

   public static Text modifyChatText(Text original) {
      if (isActive() && original != null) {
         String var1 = getPlayerName();
         if (var1 != null && !var1.isBlank()) {
            String var2 = original.getString();
            if (!var2.contains(var1)) {
               return original;
            } else {
               original = buildPrefixedName(var1);
               int var3 = var2.indexOf(var1);
               MutableText var4 = Text.empty();
               if (var3 > 0) {
                  var4.append(Text.literal(var2.substring(0, var3)));
               }

               var4.append(original);
               Text var6 = var3 + var1.length();
               if (var6 < var2.length()) {
                  var4.append(Text.literal(var2.substring(var6)));
               }

               return var4;
            }
         } else {
            return original;
         }
      } else {
         return original;
      }
   }

   public static Text buildPrefixedDisplayName(String playerName) {
      return isActive() && playerName != null ? buildPrefixedName(playerName) : null;
   }

   public static String getPrefixedNameString() {
      if (!isActive()) {
         return null;
      } else {
         String var0 = getPlayerName();
         if (var0 == null) {
            return null;
         } else {
            Text var1 = buildPrefixedName(var0);
            return var1.getString();
         }
      }
   }

   public static Style getRolePrefixStyle() {
      if (!isActive()) {
         return Style.EMPTY;
      } else {
         String var0 = instance.role.getValue();

         return switch (var0) {
            case "SRMOD" -> roleStyle(5635925);
            case "MEDIA" -> roleStyle(16733695);
            case "SRADMIN" -> roleStyle(16733525);
            default -> Style.EMPTY;
         };
      }
   }

   public static Style getRoleBracketStyle() {
      return isActive() ? Style.EMPTY.withColor(TextColor.fromRgb(8355711)).withBold(false) : Style.EMPTY;
   }

   public static Style getRolePrefixStyleForChar(int codePoint) {
      return codePoint != 91 && codePoint != 93 && !Character.isWhitespace(codePoint) ? getRolePrefixStyle() : getRoleBracketStyle();
   }

   public static Style getRoleNameStyle() {
      if (!isActive()) {
         return Style.EMPTY;
      } else {
         String var0 = instance.role.getValue();

         return switch (var0) {
            case "SRMOD" -> roleStyle(5635925);
            case "MEDIA" -> Style.EMPTY.withColor(TextColor.fromRgb(16777215)).withBold(false);
            case "SRADMIN" -> roleStyle(16733525);
            default -> Style.EMPTY;
         };
      }
   }

   public static String getRolePrefixString() {
      if (!isActive()) {
         return null;
      } else {
         String var0 = instance.role.getValue();

         return switch (var0) {
            case "SRMOD" -> "[SR.MOD] ";
            case "MEDIA" -> "[MEDIA] ";
            case "SRADMIN" -> "[SR.ADMIN] ";
            default -> null;
         };
      }
   }

   private static Text buildPrefixedName(String playerName) {
      String var1 = instance.role.getValue();

      return (Text)(switch (var1) {
         case "SRMOD" -> buildSrmodName(playerName);
         case "MEDIA" -> buildMediaName(playerName);
         case "SRADMIN" -> buildSradminName(playerName);
         default -> Text.literal(playerName);
      });
   }

   private static Text buildSrmodName(String playerName) {
      MutableText var1 = Text.empty();
      appendTag(var1, "SR.MOD", roleStyle(5635925));
      var1.append(Text.literal(playerName).setStyle(getRoleNameStyle()));
      return var1;
   }

   private static Text buildMediaName(String playerName) {
      MutableText var1 = Text.empty();
      appendTag(var1, "MEDIA", roleStyle(16733695));
      var1.append(Text.literal(playerName).setStyle(getRoleNameStyle()));
      return var1;
   }

   private static Text buildSradminName(String playerName) {
      MutableText var1 = Text.empty();
      appendTag(var1, "SR.ADMIN", roleStyle(16733525));
      var1.append(Text.literal(playerName).setStyle(getRoleNameStyle()));
      return var1;
   }

   private static void appendTag(MutableText result, String roleText, Style roleStyle) {
      Style var3 = Style.EMPTY.withColor(TextColor.fromRgb(8355711)).withBold(false);
      result.append(Text.literal("[").setStyle(var3));
      result.append(Text.literal(roleText).setStyle(roleStyle));
      result.append(Text.literal("] ").setStyle(var3));
   }

   private static Style roleStyle(int rgb) {
      return Style.EMPTY.withColor(TextColor.fromRgb(rgb)).withBold(true);
   }
}
