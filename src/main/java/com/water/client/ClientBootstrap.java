package com.water.client;

import com.water.gui.ClickGuiScreen;
import com.water.gui.ToastManager;
import com.water.gui.hud.SpotifyQueueHud;
import com.water.module.ActivatableModule;
import com.water.module.Module;
import com.water.module.ModuleManager;
import com.water.module.modules.client.Hud;
import com.water.module.modules.client.SpotifyHud;
import com.water.module.modules.client.WaterPlus;
import com.water.module.modules.misc.NameTags;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.KeyBinding.Category;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.InputUtil.Type;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.LoggerFactory;

public final class ClientBootstrap {
   public static boolean isMenuKey(int keyCode, int scanCode) {
      return keyCode == WaterPlus.getGuiKey();
   }

   public static void toggleClickGui() {
      MinecraftClient var0 = MinecraftClient.getInstance();
      if (var0 != null) {
         var0.execute(() -> {
            if (var0.currentScreen instanceof ClickGuiScreen) {
               var0.setScreen(null);
            } else {
               ClickGuiScreen.open();
            }
         });
      }
   }

   public static void a() {
      ModuleManager.INSTANCE.method_a_1();
      KeyBindingHelper.registerKeyBinding(new KeyBinding("key.water.toggle_menu", Type.KEYSYM, 344, new Category(Identifier.of("water", "general"))));
      HudRenderCallback.EVENT.register((HudRenderCallback)(context, tickCounter) -> {
         NameTags.renderHud(context, tickCounter.getTickProgress(false));
         Hud.a(context);
         SpotifyHud.a(context);
         SpotifyQueueHud.visible = Hud.e();
         if (SpotifyQueueHud.visible) {
            RenderTickCounter var9 = MinecraftClient.getInstance();
            if (var9 != null) {
               double var3 = var9.getWindow().getScaleFactor();
               double var5 = var9.mouse.getX() / var3;
               double var7 = var9.mouse.getY() / var3;
               SpotifyQueueHud.render(context, var5, var7);
            }
         }

         ToastManager.INSTANCE.render(context);
      });
      ClientTickEvents.END_CLIENT_TICK.register((EndTick)client -> {
         ModuleManager.INSTANCE.onTick();
         if (client.currentScreen == null && client.getWindow() != null) {
            for (Module var2 : ModuleManager.INSTANCE.getModules()) {
               int var3 = var2.getBind();
               ActivatableModule var4 = var2 instanceof ActivatableModule var5 ? var5 : null;
               int var9 = var4 != null ? var4.getActivationKey() : 0;
               if (var3 != 0) {
                  try {
                     boolean var6 = GLFW.glfwGetKey(client.getWindow().getHandle(), var3) == 1;
                     if (var6 && !var2.wasBindPressed && var9 != var3) {
                        var2.onBindPressed();
                     }

                     var2.wasBindPressed = var6;
                  } catch (Exception var8) {
                  }
               }

               if (var4 != null && var9 != 0) {
                  try {
                     boolean var10 = GLFW.glfwGetKey(client.getWindow().getHandle(), var9) == 1;
                     if (var10 && !var4.field_a_2) {
                        var4.b();
                     }

                     var4.field_a_2 = var10;
                  } catch (Exception var7) {
                  }
               }
            }
         }
      });
   }

   static {
      LoggerFactory.getLogger("water");
   }
}
