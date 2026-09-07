package com.water.mixin;

import com.water.module.Module;
import com.water.module.ModuleManager;
import com.water.module.modules.donut.FakeRoles;
import com.water.module.modules.misc.NameProtect;
import java.lang.reflect.Method;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ChatHud.class})
public class ChatHudMixin {
   @Shadow
   private List<?> field_2061;

   @Inject(
      method = {"addMessage*"},
      at = {@At("RETURN")}
   )
   private void afterAddMessage(CallbackInfo var1) {
      Module var2 = ModuleManager.INSTANCE.getModuleByName("RTP Home Reset");
      if (var2 != null && var2.isEnabled()) {
         if (this.field_2061 != null && !this.field_2061.isEmpty()) {
            this.field_2061
               .removeIf(
                  var0 -> {
                     if (var0 == null) {
                        return false;
                     } else {
                        try {
                           for (Method var4 : var0.getClass().getDeclaredMethods()) {
                              if (var4.getParameterCount() == 0) {
                                 var4.setAccessible(true);
                                 if (var4.invoke(var0) instanceof Text var6) {
                                    String var7 = var6.getString().toLowerCase();
                                    return var7.contains("home deleted")
                                       || var7.contains("home set")
                                       || var7.contains("teleported to a random location")
                                       || var7.contains("teleported to your home");
                                 }
                              }
                           }
                        } catch (Throwable var8) {
                        }

                        return false;
                     }
                  }
               );
         }
      }
   }

   @ModifyVariable(
      method = {"addMessage"},
      at = @At("HEAD"),
      ordinal = 0,
      argsOnly = true
   )
   private Text modifyChatMessage(Text var1) {
      if (var1 != null && NameProtect.instance != null && NameProtect.instance.isEnabled() && MinecraftClient.getInstance().getSession() != null) {
         String var2 = MinecraftClient.getInstance().getSession().getUsername();
         if (var2 != null) {
            String var3 = var1.getString();
            if (var3.contains(var2)) {
               var1 = Text.literal(var3.replace(var2, NameProtect.instance.getFakeName()));
            }
         }
      }

      return FakeRoles.modifyChatText((Text)var1);
   }
}
