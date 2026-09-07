package com.water.module.modules.donut;

import com.water.module.Category;
import com.water.module.Module;
import com.water.setting.Setting;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.text.Text;

public final class AutoChunkLoader extends Module {
   private final Setting<Float> F = new Setting<>("Home Slot", 1.0F, 1.0F, 5.0F);
   private final Setting<Boolean> G = new Setting<>("Chat Feedback", true);
   private final Setting<Integer> H = new Setting<>("Cooldown", 3500, 500, 10000);
   private boolean u = false;
   private long n = 0L;

   public AutoChunkLoader() {
      super("AUTO CHUNK LOADER", Category.d);
      this.addSetting(this.F);
      this.addSetting(this.G);
      this.addSetting(this.H);
   }

   @Override
   public void onDisable() {
      this.u = false;
      this.n = 0L;
   }

   @Override
   public void onTick() {
      if (mc.world != null && mc.player != null) {
         if (!this.u) {
            if (System.currentTimeMillis() >= this.n) {
               double var1 = mc.player.getY();
               if (!(var1 >= -3.0)) {
                  this.u = true;
                  int var3 = Math.round(this.F.getValue());
                  if (this.G.getValue() && mc.inGameHud != null) {
                     mc.inGameHud.getChatHud().addMessage(Text.literal(""));
                  }

                  new Thread(() -> {
                     try {
                        mc.execute(() -> this.p("/delhome " + var3));
                        Thread.sleep(800L);
                        mc.execute(() -> {
                           this.p("/sethome " + var3);
                           if (this.G.getValue() && mc.inGameHud != null) {
                              mc.inGameHud.getChatHud().addMessage(Text.literal(""));
                           }
                        });
                        Thread.sleep(300L);
                        mc.execute(() -> {
                           this.p("/home 3");
                           if (this.G.getValue() && mc.inGameHud != null) {
                              mc.inGameHud.getChatHud().addMessage(Text.literal(""));
                           }
                        });
                        Thread.sleep(this.H.getValue().intValue());
                        mc.execute(() -> {
                           this.p("/home " + var3);
                           if (this.G.getValue() && mc.inGameHud != null) {
                              mc.inGameHud.getChatHud().addMessage(Text.literal(""));
                           }

                           this.n = System.currentTimeMillis() + 15000L;
                           this.u = false;
                        });
                     } catch (InterruptedException var2) {
                        this.u = false;
                     }
                  }, "RtpReset-Thread").start();
               }
            }
         }
      }
   }

   private void p(String command) {
      if (mc != null && mc.player != null) {
         ClientPlayNetworkHandler var2 = null;

         try {
            var2 = mc.player.networkHandler;
         } catch (Throwable var7) {
         }

         if (var2 == null) {
            try {
               var2 = mc.getNetworkHandler();
            } catch (Throwable var6) {
            }
         }

         if (var2 != null) {
            String var3 = command.startsWith("/") ? command.substring(1) : command;

            try {
               var2.sendChatCommand(var3);
            } catch (Throwable var5) {
               try {
                  var2.sendChatMessage(command);
               } catch (Throwable var4) {
               }
            }
         }
      }
   }
}
