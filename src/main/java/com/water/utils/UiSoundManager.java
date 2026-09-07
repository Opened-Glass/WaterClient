package com.water.utils;

import com.water.module.modules.client.WaterPlus;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public final class UiSoundManager {
   private static final Identifier c = Identifier.of("water", "ui.module_on");

   public static void bj() {
      a(c, 0.7F, 1.0F);
   }

   private static void a(Identifier id, float volume, float pitch) {
      if (WaterPlus.uiSoundsEnabled()) {
         MinecraftClient var3 = MinecraftClient.getInstance();
         if (var3 != null && var3.player != null) {
            try {
               var3.player.playSound(SoundEvent.of(id), volume, pitch);
            } catch (Throwable var4) {
            }
         }
      }
   }

   static {
      Identifier.of("water", "ui.gui_open");
      Identifier.of("water", "ui.gui_close");
   }
}
