package com.water.mixin;

import com.water.module.modules.donut.FakeStats;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({PlayerListHud.class})
public class PlayerListHudMixin {
   @Shadow
   private Text field_2154;

   @Inject(
      method = {"render"},
      at = {@At("HEAD")}
   )
   private void water$fakeFooter(DrawContext var1, int var2, Scoreboard var3, ScoreboardObjective var4, CallbackInfo var5) {
      FakeStats var6 = FakeStats.getInstance();
      if (var6 != null && var6.isEnabled() && this.field_2154 != null) {
         Text var7 = var6.fakeFooterText(this.field_2154);
         if (var7 != null) {
            this.field_2154 = var7;
         }
      }
   }
}
