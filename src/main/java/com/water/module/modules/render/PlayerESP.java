package com.water.module.modules.render;

import com.mojang.authlib.GameProfile;
import com.water.module.Category;
import com.water.module.Module;
import com.water.module.modules.client.Friends;
import com.water.setting.Setting;
import com.water.utils.RenderUtils;
import java.awt.Color;
import java.util.ArrayList;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class PlayerESP extends Module {
   private final Setting<Double> ca = new Setting<>("Fill Alpha", 180.0, 0.0, 255.0);
   private final Setting<Double> cb = new Setting<>("Range", 256.0, 16.0, 512.0);
   private final Setting<Boolean> cc = new Setting<>("Tracers", false);
   private final Setting<Double> cd = new Setting<>("Tracer Width", 0.5, 0.1, 2.0);
   private final Setting<Color> ce = new Setting<>("Fill color", new Color(255, 0, 0));
   private final Setting<Color> cf = new Setting<>("Tracer color", new Color(255, 0, 0));
   private RenderUtils.PersistentBatch a;
   private RenderUtils.PersistentBatch b;

   public PlayerESP() {
      super("Player ESP", Category.b);
      this.addSetting(this.ca);
      this.addSetting(this.cb);
      this.addSetting(this.cc);
      this.addSetting(this.cd);
      this.addSetting(this.ce);
      this.addSetting(this.cf);
   }

   @Override
   public void onEnable() {
      this.a = RenderUtils.createPersistentBatch();
      this.b = RenderUtils.createPersistentBatch();
   }

   @Override
   public void onDisable() {
      if (this.a != null) {
         this.a.close();
         this.a = null;
      }

      if (this.b != null) {
         this.b.close();
         this.b = null;
      }
   }

   @Override
   public void onRender(MatrixStack matrices, float tickDelta) {
      if (mc.world != null && mc.player != null) {
         Camera var3 = RenderUtils.getCamera();
         if (var3 != null) {
            if (this.a == null) {
               this.a = RenderUtils.createPersistentBatch();
            }

            if (this.b == null) {
               this.b = RenderUtils.createPersistentBatch();
            }

            Vec3d var4 = RenderUtils.getCameraPos(var3);
            Vec3d var29 = RenderUtils.getCameraForward(var3);
            double var6 = var4.x;
            double var8 = var4.y;
            double var10 = var4.z;
            double var12 = this.cb.getValue() * this.cb.getValue();
            int var5 = this.a(this.ca.getValue());
            boolean var14 = this.cc.getValue();
            Vec3d var15 = Freecam.resolveTracerOrigin(var4, tickDelta);
            Vec3d var30 = var15.equals(var4) ? var29.multiply(0.1) : var15.subtract(var4);
            ArrayList var31 = new ArrayList();

            for (PlayerEntity var16 : mc.world.getPlayers()) {
               if (var16 != mc.player && var16.isAlive() && !var16.isSpectator()) {
                  boolean var17 = Friends.b() && Friends.method_a_1(this.a(var16));
                  Vec3d var18 = this.a(var16, tickDelta);
                  double var23 = var18.x - var6;
                  double var25 = var18.y - var8;
                  double var27 = var18.z - var10;
                  if (!(var23 * var23 + var25 * var25 + var27 * var27 > var12)) {
                     Color var39 = var17 ? Friends.method_a_2() : this.ce.getValue();
                     Color var37 = var17 ? Friends.method_a_2() : this.cf.getValue();
                     var31.add(
                        new PlayerESP.a(
                           var23,
                           var25,
                           var27,
                           var25 + var16.getHeight() * 0.5,
                           var16.getWidth() / 2.0,
                           var16.getHeight(),
                           this.b(var39, var5),
                           this.b(var37, 255)
                        )
                     );
                  }
               }
            }

            if (!var31.isEmpty()) {
               this.a.begin(matrices);

               for (PlayerESP.a var35 : var31) {
                  this.a.addFilledBox(var35.p - var35.t, var35.q, var35.r - var35.t, var35.p + var35.t, var35.q + var35.u, var35.r + var35.t, var35.e);
               }

               this.a.flush();
               if (var14) {
                  float var34 = this.cd.getValue().floatValue();
                  this.b.begin(matrices);

                  for (PlayerESP.a var38 : var31) {
                     Vec3d var40 = new Vec3d(var38.p, var38.s, var38.r);
                     Color var41 = var38.f;
                     this.b.addLine(var30, var40, a(var41, 25), var34 + 0.8F);
                     this.b.addLine(var30, var40, a(var41, 200), var34);
                     this.b.addLine(var30, var40, a(a(var41, 0.6F), 150), Math.max(var34 - 0.2F, 0.2F));
                  }

                  this.b.flush();
               }
            }
         }
      }
   }

   private static Color a(Color c, int a) {
      return new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.max(0, Math.min(255, a)));
   }

   private static Color a(Color c, float t) {
      int var2 = (int)(c.getRed() + (255 - c.getRed()) * t);
      int var3 = (int)(c.getGreen() + (255 - c.getGreen()) * t);
      Color var4 = (int)(c.getBlue() + (255 - c.getBlue()) * t);
      return new Color(var2, var3, var4);
   }

   private int a(double v) {
      return Math.max(0, Math.min(255, (int)Math.round(v)));
   }

   private Color b(Color base, int a) {
      a = Math.max(0, Math.min(255, Math.round(base.getAlpha() / 255.0F * a)));
      return new Color(base.getRed(), base.getGreen(), base.getBlue(), a);
   }

   private Vec3d a(PlayerEntity player, float tickDelta) {
      try {
         return player.getLerpedPos(tickDelta);
      } catch (Throwable var3) {
         return new Vec3d(
            MathHelper.lerp((double)tickDelta, player.lastRenderX, player.getX()),
            MathHelper.lerp((double)tickDelta, player.lastRenderY, player.getY()),
            MathHelper.lerp((double)tickDelta, player.lastRenderZ, player.getZ())
         );
      }
   }

   private String a(PlayerEntity player) {
      if (player == null) {
         return "";
      } else {
         try {
            GameProfile var2 = player.getGameProfile();
            if (var2 != null) {
               try {
                  if (var2.getClass().getMethod("getName").invoke(var2) instanceof String var7 && !var7.isBlank()) {
                     return var7;
                  }
               } catch (Throwable var5) {
               }

               try {
                  if (var2.getClass().getMethod("name").invoke(var2) instanceof String var9 && !var9.isBlank()) {
                     return var9;
                  }
               } catch (Throwable var4) {
               }
            }
         } catch (Throwable var6) {
         }

         return player.getName().getString();
      }
   }

   private static final class a {
      final double p;
      final double q;
      final double r;
      final double s;
      final double t;
      final double u;
      final Color e;
      final Color f;

      a(double dx, double dy, double dz, double tracerY, double hw, double height, Color fill, Color tracer) {
         this.p = dx;
         this.q = dy;
         this.r = dz;
         this.s = tracerY;
         this.t = hw;
         this.u = height;
         this.e = fill;
         this.f = tracer;
      }
   }
}
