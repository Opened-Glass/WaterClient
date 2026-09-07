package com.water.module.modules.donut;

import com.water.module.Category;
import com.water.module.Module;
import com.water.setting.ModeSetting;
import com.water.setting.Setting;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;

public final class BoneDropper extends Module {
   private final ModeSetting field_a_1 = new ModeSetting("Mode", "Spawner", "Spawner", "Orders");
   private final Setting<Integer> A = new Setting<>("Delay", 300, 100, 2000);
   private BoneDropper.a field_a_2 = BoneDropper.a.b;
   private String i = this.field_a_1.getValue();
   private long k;
   private int o;
   private long l;
   private boolean t;

   public BoneDropper() {
      super("BoneDropper", Category.d);
      this.addSetting(this.field_a_1);
      this.addSetting(this.A);
   }

   @Override
   public void onEnable() {
      this.u();
   }

   @Override
   public void onDisable() {
      this.field_a_2 = BoneDropper.a.b;
      this.k = 0L;
   }

   @Override
   public void onTick() {
      if (mc.player != null && mc.world != null && mc.interactionManager != null) {
         if (mc.currentScreen == null || mc.currentScreen instanceof HandledScreen) {
            if (!this.field_a_1.getValue().equalsIgnoreCase(this.i)) {
               this.u();
            }

            long var1 = System.currentTimeMillis();
            if (var1 >= this.k) {
               if (this.field_a_1.d("Spawner")) {
                  this.x();
               } else {
                  this.y();
               }
            }
         }
      }
   }

   private void x() {
      switch (this.field_a_2) {
         case b:
            if (this.k()) {
               this.field_a_2 = BoneDropper.a.c;
               this.ab();
               return;
            }

            this.z();
            this.field_a_2 = BoneDropper.a.c;
            this.ab();
            break;
         case c:
            if (!this.k()) {
               this.field_a_2 = BoneDropper.a.b;
               this.ab();
               return;
            }

            this.field_a_2 = BoneDropper.a.d;
            this.ab();
            break;
         case d:
            ScreenHandler var7 = this.a();
            if (var7 == null) {
               this.field_a_2 = BoneDropper.a.b;
               this.ab();
               return;
            }

            if (!this.method_a_1(var7)) {
               this.ab();
               return;
            }

            this.field_a_2 = BoneDropper.a.e;
            this.ab();
            break;
         case e:
            ScreenHandler var6 = this.a();
            if (var6 == null) {
               this.field_a_2 = BoneDropper.a.b;
               this.ab();
               return;
            }

            Slot var2 = this.method_a_3(var6);
            if (var2 == null) {
               this.ab();
               return;
            }

            this.t = this.method_a_1(var6);
            if (!this.t) {
               this.field_a_2 = BoneDropper.a.d;
               this.ab();
               return;
            }

            this.o = this.c(Items.BONE);
            this.l = System.currentTimeMillis();
            this.method_a_1(var2);
            this.field_a_2 = BoneDropper.a.f;
            this.ab();
            break;
         case f:
            long var1 = System.currentTimeMillis();
            ScreenHandler var3 = this.a();
            boolean var4 = this.t && var3 != null && !this.method_a_1(var3);
            boolean var5 = this.c(Items.BONE) > this.o;
            if (var4 || var5) {
               if (var3 != null) {
                  this.aa();
               }

               this.field_a_2 = BoneDropper.a.g;
               this.k = Long.MAX_VALUE;
               return;
            }

            if (this.l > 0L && var1 - this.l > 4000L) {
               this.field_a_2 = var3 == null ? BoneDropper.a.b : BoneDropper.a.d;
               this.ab();
               return;
            }

            this.ab();
         case g:
            break;
         default:
            this.field_a_2 = BoneDropper.a.b;
            this.ab();
      }
   }

   private void y() {
      switch (this.field_a_2) {
         case h:
            if (!this.k()) {
               this.o("/order");
               this.field_a_2 = BoneDropper.a.i;
               this.ab();
               return;
            }

            this.field_a_2 = BoneDropper.a.j;
            this.ab();
            break;
         case i:
            if (!this.k()) {
               this.o("/order");
               this.ab();
               return;
            }

            this.field_a_2 = BoneDropper.a.j;
            this.ab();
            break;
         case j:
            this.a(BoneDropper.b.b, BoneDropper.a.k);
            break;
         case k:
            this.a(BoneDropper.b.field_a_1, BoneDropper.a.l);
            break;
         case l:
            this.a(BoneDropper.b.b, BoneDropper.a.m);
            break;
         case m:
            this.a(BoneDropper.b.c, BoneDropper.a.n);
            break;
         case n:
            this.a(BoneDropper.b.d, BoneDropper.a.o);
            break;
         case o:
            this.a(BoneDropper.b.c, BoneDropper.a.j);
            break;
         default:
            this.field_a_2 = BoneDropper.a.h;
            this.ab();
      }
   }

   private void a(BoneDropper.b target, BoneDropper.a nextState) {
      ScreenHandler var3 = this.a();
      if (var3 == null) {
         this.o("/order");
         this.ab();
      } else {
         BoneDropper.b var4 = this.a(var3, target, target == BoneDropper.b.c || target == BoneDropper.b.d);
         if (var4 == null) {
            this.ab();
         } else {
            this.method_a_1(var4);
            this.field_a_2 = nextState;
            this.ab();
         }
      }
   }

   private boolean method_a_1(ScreenHandler handler) {
      ScreenHandler var3 = this.method_a_2(handler);
      if (var3.isEmpty()) {
         return false;
      } else {
         for (Slot var2 : var3) {
            if (var2.isEnabled()) {
               ItemStack var5 = var2.getStack();
               if (var5.isEmpty() || !this.a(var5, BoneDropper.b.field_a_1)) {
                  return false;
               }
            }
         }

         return true;
      }
   }

   private List<Slot> method_a_2(ScreenHandler handler) {
      ScreenHandler var6 = this.b(handler);
      if (var6.isEmpty()) {
         return List.of();
      } else {
         int var2 = var6.stream().mapToInt(slot -> slot.y).max().orElse(Integer.MIN_VALUE);
         ArrayList var3 = new ArrayList();

         for (Slot var5 : var6) {
            if (var5.y < var2) {
               var3.add(var5);
            }
         }

         return (List<Slot>)(var3.isEmpty() ? var6 : var3);
      }
   }

   private Slot method_a_3(ScreenHandler handler) {
      ScreenHandler var4 = this.b(handler);
      if (var4.isEmpty()) {
         return null;
      } else {
         int var2 = var4.stream().mapToInt(slot -> slot.y).max().orElse(Integer.MIN_VALUE);
         Slot var5 = this.a(var4, BoneDropper.b.c, true, var2);
         if (var5 != null) {
            return var5;
         } else {
            Slot var6 = this.a(var4, BoneDropper.b.c, true, Integer.MIN_VALUE);
            if (var6 != null) {
               return var6;
            } else {
               for (int var7 = var4.size() - 1; var7 >= 0; var7--) {
                  Slot var3 = (Slot)var4.get(var7);
                  if (var3.isEnabled()) {
                     return var3;
                  }
               }

               return null;
            }
         }
      }
   }

   private Slot a(ScreenHandler handler, BoneDropper.b target, boolean preferBottomRight) {
      return this.a(this.b(handler), target, preferBottomRight, Integer.MIN_VALUE);
   }

   private Slot a(List<Slot> slots, BoneDropper.b target, boolean preferBottomRight, int yFilter) {
      Slot var5 = null;

      for (Slot var6 : slots) {
         if (var6.isEnabled() && (yFilter == Integer.MIN_VALUE || var6.y == yFilter) && this.a(var6.getStack(), target)) {
            if (var5 == null) {
               var5 = var6;
            } else if (preferBottomRight) {
               if (var6.y > var5.y || var6.y == var5.y && var6.x >= var5.x) {
                  var5 = var6;
               }
            } else if (var6.y < var5.y || var6.y == var5.y && var6.x < var5.x) {
               var5 = var6;
            }
         }
      }

      return var5;
   }

   private boolean a(ItemStack stack, BoneDropper.b target) {
      if (stack != null && !stack.isEmpty()) {
         Item var3 = stack.getItem();
         ItemStack var4 = stack.getName().getString().toLowerCase(Locale.ROOT);

         return switch (target) {
            case field_a_1 -> var3 == Items.BONE || var4.contains("bone");
            case b -> var3 == Items.CHEST || var3 == Items.TRAPPED_CHEST || var3 == Items.ENDER_CHEST || var4.contains("chest") || var4.contains("truhe");
            case c -> var3 == Items.DROPPER || var4.contains("dropper");
            case d -> var3 == Items.ARROW || var4.contains("arrow") || var4.contains("pfeil");
         };
      } else {
         return false;
      }
   }

   private List<Slot> b(ScreenHandler handler) {
      if (handler != null && handler.slots != null && !handler.slots.isEmpty()) {
         int var2 = Math.max(0, handler.slots.size() - 36);
         if (var2 == 0) {
            var2 = handler.slots.size();
         }

         ArrayList var3 = new ArrayList(var2);

         for (int var4 = 0; var4 < var2; var4++) {
            var3.add(handler.slots.get(var4));
         }

         return var3;
      } else {
         return List.of();
      }
   }

   private int c(Item item) {
      if (mc.player == null) {
         return 0;
      } else {
         int var2 = 0;

         for (int var3 = 0; var3 < mc.player.getInventory().size(); var3++) {
            ItemStack var4 = mc.player.getInventory().getStack(var3);
            if (!var4.isEmpty() && var4.isOf(item)) {
               var2 += var4.getCount();
            }
         }

         return var2;
      }
   }

   private void method_a_1(Slot slot) {
      if (slot != null && mc.player != null && mc.interactionManager != null) {
         mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slot.id, 0, SlotActionType.PICKUP, mc.player);
      }
   }

   private void z() {
      if (mc.crosshairTarget instanceof BlockHitResult var2 && mc.crosshairTarget.getType() == Type.BLOCK) {
         mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, var2);
         mc.player.swingHand(Hand.MAIN_HAND);
      }
   }

   private void aa() {
      if (mc.player != null) {
         if (mc.currentScreen instanceof HandledScreen) {
            mc.player.closeHandledScreen();
            mc.setScreen(null);
         }
      }
   }

   private ScreenHandler a() {
      return mc.currentScreen instanceof HandledScreen && mc.player != null ? mc.player.currentScreenHandler : null;
   }

   private boolean k() {
      return this.a() != null;
   }

   private void o(String command) {
      ClientPlayNetworkHandler var2 = mc.player != null ? mc.player.networkHandler : mc.getNetworkHandler();
      if (var2 != null) {
         String var3 = command.startsWith("/") ? command.substring(1) : command;

         try {
            var2.sendChatCommand(var3);
         } catch (Throwable var4) {
            var2.sendChatMessage(command);
         }
      }
   }

   private void u() {
      this.i = this.field_a_1.getValue();
      this.field_a_2 = this.field_a_1.d("Spawner") ? BoneDropper.a.b : BoneDropper.a.h;
      this.k = 0L;
      this.o = 0;
      this.l = 0L;
      this.t = false;
   }

   private void ab() {
      this.k = System.currentTimeMillis() + this.A.getValue().intValue();
   }

   private static enum a {
      b,
      c,
      d,
      e,
      f,
      g,
      h,
      i,
      j,
      k,
      l,
      m,
      n,
      o;
   }

   private static enum b {
      field_a_1,
      b,
      c,
      d;
   }
}
