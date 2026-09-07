package com.water.client;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.LoggerFactory;

public class WaterInit implements ClientModInitializer {
   public static boolean isMenuKey(int k, int s) {
      return ClientBootstrap.isMenuKey(k, s);
   }

   public static void toggleClickGui() {
      ClientBootstrap.toggleClickGui();
   }

   public void onInitializeClient() {
      ClientBootstrap.a();
   }

   static {
      LoggerFactory.getLogger("water");
   }
}
