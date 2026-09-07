package com.water.module.modules.misc;

import com.water.gui.ToastManager;
import com.water.module.Category;
import com.water.module.Module;
import com.water.setting.ModeSetting;
import com.water.setting.Setting;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public final class TabDetector extends Module {
   private final ModeSetting c = new ModeSetting("Detect", "List", "Any", "List");
   private final Setting<String> aR = new Setting<>("Target Players", "");
   private final ModeSetting d = new ModeSetting("Notification Mode", "Both", "Chat", "Toast", "Both");
   private final Setting<Boolean> aS = new Setting<>("Log Offline", true);
   private final Set<String> h = new HashSet<>();
   private final Set<String> i = new HashSet<>();

   public TabDetector() {
      super("TabDetector", Category.c);
      this.aR.visibleWhen(() -> this.c.d("List"));
      this.addSetting(this.c);
      this.addSetting(this.aR);
      this.addSetting(this.d);
      this.addSetting(this.aS);
   }

   @Override
   public void onEnable() {
      this.h.clear();
      this.i.clear();
      this.c(this.i);
   }

   @Override
   public void onDisable() {
      this.h.clear();
      this.i.clear();
   }

   @Override
   public void onTick() {
      if (mc.player != null && mc.world != null && mc.getNetworkHandler() != null) {
         boolean var1 = this.c.d("Any");
         Set var2 = var1 ? Set.of() : this.d(this.aR.getValue());
         if (!var1 && var2.isEmpty()) {
            this.h.clear();
            this.i.clear();
         } else {
            this.h.clear();

            for (PlayerListEntry var4 : mc.getNetworkHandler().getPlayerList()) {
               String var6 = this.a(var4);
               if (!var6.isEmpty() && (mc.player == null || !var6.equalsIgnoreCase(mc.player.getName().getString())) && (var1 || this.a(var2, var6))) {
                  this.h.add(var6);
               }
            }

            HashSet var5 = new HashSet<>(this.h);
            var5.removeAll(this.i);
            if (!var5.isEmpty()) {
               this.a(var5);
            }

            if (this.aS.getValue()) {
               HashSet var7 = new HashSet<>(this.i);
               var7.removeAll(this.h);
               if (!var7.isEmpty()) {
                  this.b(var7);
               }
            }

            this.i.clear();
            this.i.addAll(this.h);
         }
      }
   }

   private void a(Set<String> players) {
      String var2 = String.join(", ", players);
      var2 = players.size() == 1 ? "Target player joined: " + var2 : "Target players joined: " + var2;
      this.c(var2, players.size() == 1 ? "Target Player Joined!" : "Target Players Joined!", -1938838);
   }

   private void b(Set<String> players) {
      String var2 = String.join(", ", players);
      var2 = players.size() == 1 ? "Target player left: " + var2 : "Target players left: " + var2;
      this.c(var2, players.size() == 1 ? "Target Player Left!" : "Target Players Left!", -11152222);
   }

   private void c(String chatMessage, String toastTitle, int accent) {
      String var4 = this.d.getValue();
      boolean var5 = "Chat".equalsIgnoreCase(var4) || "Both".equalsIgnoreCase(var4);
      boolean var7 = "Toast".equalsIgnoreCase(var4) || "Both".equalsIgnoreCase(var4);
      if (var5) {
         try {
            mc.inGameHud.getChatHud().addMessage(Text.literal("[TabDetector] " + chatMessage));
         } catch (Throwable var6) {
         }
      }

      if (var7) {
         ToastManager.INSTANCE.push("TabDetector", toastTitle, ItemStack.EMPTY, accent);
      }
   }

   private void c(Set<String> out) {
      out.clear();
      if (mc.getNetworkHandler() != null) {
         boolean var2 = this.c.d("Any");
         Set var3 = var2 ? Set.of() : this.d(this.aR.getValue());
         if (var2 || !var3.isEmpty()) {
            for (PlayerListEntry var5 : mc.getNetworkHandler().getPlayerList()) {
               String var6 = this.a(var5);
               if (!var6.isEmpty() && (mc.player == null || !var6.equalsIgnoreCase(mc.player.getName().getString())) && (var2 || this.a(var3, var6))) {
                  out.add(var6);
               }
            }
         }
      }
   }

   private String a(PlayerListEntry entry) {
      try {
         if (entry != null && entry.getProfile() != null) {
            try {
               PlayerListEntry var4 = entry.getProfile().name();
               return var4 == null ? "" : var4;
            } catch (Throwable var2) {
               return "";
            }
         } else {
            return "";
         }
      } catch (Throwable var3) {
         return "";
      }
   }

   private boolean a(Set<String> loweredSet, String value) {
      return loweredSet.contains(value.toLowerCase(Locale.ROOT));
   }

   private Set<String> d(String raw) {
      if (raw != null && !raw.isBlank()) {
         raw = raw.replace('\n', ',').replace('\r', ',');
         LinkedHashSet var2 = new LinkedHashSet();

         for (String var5 : raw.split(",")) {
            var5 = var5 == null ? "" : var5.trim();
            if (!var5.isEmpty()) {
               var2.add(var5.toLowerCase(Locale.ROOT));
            }
         }

         return var2;
      } else {
         return Set.of();
      }
   }
}
