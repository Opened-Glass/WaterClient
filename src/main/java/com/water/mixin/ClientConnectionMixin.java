package com.water.mixin;

import com.water.module.Module;
import com.water.module.ModuleManager;
import io.netty.channel.ChannelFutureListener;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ClientConnection.class})
public class ClientConnectionMixin {
   @Inject(
      method = {"handlePacket"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private static void onHandlePacket(Packet<?> var0, PacketListener var1, CallbackInfo var2) {
      try {
         Module var3 = ModuleManager.INSTANCE.getModuleByName("RTP Home Reset");
         if (var3 != null && var3.isEnabled() && containsBlockedText(var0)) {
            var2.cancel();
            return;
         }

         ModuleManager.INSTANCE.onPacketReceive(var0);
      } catch (Exception var4) {
      }
   }

   private static boolean containsBlockedText(Object var0) {
      if (var0 == null) {
         return false;
      } else {
         for (Method var4 : var0.getClass().getDeclaredMethods()) {
            if (var4.getParameterCount() == 0) {
               try {
                  var4.setAccessible(true);
                  Object var5 = var4.invoke(var0);
                  if (var5 instanceof Text var6) {
                     if (isBlocked(var6.getString())) {
                        return true;
                     }

                     if (isBlocked(var6.getString())) {
                        return true;
                     }
                  }

                  if (var5 instanceof String var15 && isBlocked(var15)) {
                     return true;
                  }
               } catch (Throwable var9) {
               }
            }
         }

         for (Class var10 = var0.getClass(); var10 != null; var10 = var10.getSuperclass()) {
            for (Field var14 : var10.getDeclaredFields()) {
               try {
                  var14.setAccessible(true);
                  Object var16 = var14.get(var0);
                  if (var16 instanceof Text var7 && isBlocked(var7.getString())) {
                     return true;
                  }

                  if (var16 instanceof String var17 && isBlocked(var17)) {
                     return true;
                  }
               } catch (Throwable var8) {
               }
            }
         }

         return false;
      }
   }

   private static boolean isBlocked(String var0) {
      if (var0 == null) {
         return false;
      } else {
         String var1 = var0.toLowerCase();
         return var1.contains("home deleted")
            || var1.contains("home set")
            || var1.contains("teleported to a random location")
            || var1.contains("teleported to your home");
      }
   }

   @Inject(
      method = {"send(Lnet/minecraft/network/packet/Packet;Lio/netty/channel/ChannelFutureListener;Z)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onSend(Packet<?> var1, ChannelFutureListener var2, boolean var3, CallbackInfo var4) {
      try {
         if (ModuleManager.INSTANCE.onPacketSend(var1)) {
            var4.cancel();
         }
      } catch (Exception var6) {
      }
   }
}
