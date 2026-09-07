package com.water.module.modules.misc;

import com.water.module.ActivatableModule;
import com.water.module.Category;
import com.water.module.modules.client.Friends;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.network.ServerInfo.ServerType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardEntry;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Text;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult.Type;

public final class AutoLog extends ActivatableModule {
   private long s;
   private long field_t_1;
   private long field_u_1;
   private float c = -1.0F;
   private int ad = -1;
   private int ae = -1;
   private String field_t_2;
   private int af;
   private String field_u_2;
   private boolean aa = false;
   private long v = 0L;

   public AutoLog() {
      super("AutoLog", Category.c);
   }

   @Override
   public void b() {
      if (mc.player != null && mc.world != null) {
         this.aj();
         this.j();
         this.aa = true;
         this.v = System.currentTimeMillis() + 3000L;
      }
   }

   @Override
   public void onEnable() {
      this.s = 0L;
      this.field_t_1 = 0L;
      this.field_u_1 = 0L;
      this.c = this.c();
      this.ad = this.m();
      this.ae = this.n();
      if (mc.player != null && mc.world != null && (this.p() || this.q() || this.v())) {
         this.a(System.currentTimeMillis());
      }
   }

   @Override
   public void onTick() {
      if (this.aa && mc.player == null && mc.world == null) {
         if (System.currentTimeMillis() >= this.v && this.field_t_2 != null) {
            this.aa = false;
            this.ak();
         }
      } else if (mc.player != null && mc.world != null) {
         long var1 = System.currentTimeMillis();
         float var3 = this.c();
         boolean var4 = this.c >= 0.0F && var3 + 0.001F < this.c;
         this.c = var3;
         if (mc.player.hurtTime > 0 || var4 || this.r()) {
            this.s = var1;
         }

         boolean var6 = this.v();
         if (var6) {
            this.field_u_1 = var1;
         }

         if (mc.options.attackKey.isPressed()
            && mc.crosshairTarget != null
            && mc.crosshairTarget.getType() == Type.ENTITY
            && mc.crosshairTarget instanceof EntityHitResult var8
            && var8.getEntity() instanceof PlayerEntity var5
            && var5 != mc.player) {
            this.field_t_1 = var1;
         }

         if (this.s()) {
            this.field_t_1 = var1;
         }

         if (!this.a(var1, var6)) {
            for (PlayerEntity var11 : mc.world.getPlayers()) {
               if (var11 != mc.player && !var11.isSpectator() && (!Friends.c() || !Friends.method_a_1(var11.getName().getString()))) {
                  if (mc.getNetworkHandler() != null && mc.getNetworkHandler().getConnection() != null) {
                     this.aj();
                     mc.getNetworkHandler().getConnection().disconnect(Text.literal("[AutoLog] Player detected: " + var11.getName().getString()));
                     this.toggle();
                  }

                  return;
               }
            }
         }
      }
   }

   private void aj() {
      if (mc.getCurrentServerEntry() != null) {
         ServerInfo var1 = mc.getCurrentServerEntry();
         ServerAddress var2 = ServerAddress.parse(var1.address);
         this.field_t_2 = var2.getAddress();
         this.af = var2.getPort();
         this.field_u_2 = var1.name;
      }
   }

   private void j() {
      if (mc.getNetworkHandler() != null && mc.getNetworkHandler().getConnection() != null) {
         mc.getNetworkHandler().getConnection().disconnect(Text.literal("[AutoLog] Manual leave"));
      }
   }

   private void ak() {
      if (this.field_t_2 != null) {
         ServerInfo var1 = new ServerInfo(this.field_u_2 != null ? this.field_u_2 : this.field_t_2, this.field_t_2 + ":" + this.af, ServerType.OTHER);
         ConnectScreen.connect(new MultiplayerScreen(new TitleScreen()), mc, ServerAddress.parse(var1.address), var1, false, null);
      }
   }

   private boolean a(long now, boolean serverCombatTagged) {
      if (serverCombatTagged || this.q() || now - this.field_u_1 < 1500L) {
         return true;
      } else if (this.s <= 0L && this.field_t_1 <= 0L) {
         return false;
      } else {
         long var4 = Math.max(this.s, this.field_t_1);
         return now - var4 < 20000L;
      }
   }

   private boolean p() {
      if (mc.player != null && mc.world != null) {
         if (mc.player.hurtTime <= 0 && !this.q()) {
            if (mc.crosshairTarget != null
               && mc.crosshairTarget.getType() == Type.ENTITY
               && mc.crosshairTarget instanceof EntityHitResult var3
               && var3.getEntity() instanceof PlayerEntity var5
               && var5 != mc.player
               && !var5.isSpectator()) {
               return true;
            } else {
               for (PlayerEntity var2 : mc.world.getPlayers()) {
                  if (var2 != mc.player && !var2.isSpectator() && mc.player.squaredDistanceTo(var2) <= 64.0) {
                     return true;
                  }
               }

               return false;
            }
         } else {
            return true;
         }
      } else {
         return false;
      }
   }

   private void a(long now) {
      this.s = now;
      this.field_t_1 = now;
      this.field_u_1 = now;
   }

   private boolean q() {
      return this.t() || this.u();
   }

   private boolean r() {
      int var1 = this.m();
      if (var1 > 0 && var1 != this.ad) {
         this.ad = var1;
         return this.t();
      } else {
         return false;
      }
   }

   private boolean s() {
      int var1 = this.n();
      if (var1 > 0 && var1 != this.ae) {
         this.ae = var1;
         return this.u();
      } else {
         return false;
      }
   }

   private boolean t() {
      if (mc.player == null) {
         return false;
      } else {
         return mc.player.getLastAttacker() instanceof PlayerEntity var2 && var2 != mc.player && !var2.isSpectator()
            ? this.a(mc.player.getLastAttackedTime())
            : false;
      }
   }

   private boolean u() {
      if (mc.player == null) {
         return false;
      } else {
         return mc.player.getAttacking() instanceof PlayerEntity var2 && var2 != mc.player && !var2.isSpectator()
            ? this.a(mc.player.getLastAttackTime())
            : false;
      }
   }

   private boolean a(int tickTimestamp) {
      if (mc.player != null && tickTimestamp > 0) {
         tickTimestamp = mc.player.age - tickTimestamp;
         return tickTimestamp >= 0 && tickTimestamp < 400;
      } else {
         return false;
      }
   }

   private int m() {
      return mc.player != null ? mc.player.getLastAttackedTime() : -1;
   }

   private int n() {
      return mc.player != null ? mc.player.getLastAttackTime() : -1;
   }

   private boolean v() {
      if (mc.world != null && this.a(mc.world.getScoreboard())) {
         return true;
      } else {
         Set var1 = Collections.newSetFromMap(new IdentityHashMap());
         return mc.inGameHud != null && this.a(mc.inGameHud, 0, var1);
      }
   }

   private boolean a(Scoreboard sb) {
      if (sb == null) {
         return false;
      } else {
         for (ScoreboardObjective var3 : sb.getObjectives()) {
            if (this.c(var3.getName()) || this.a(var3.getDisplayName())) {
               return true;
            }
         }

         for (ScoreboardDisplaySlot var5 : ScoreboardDisplaySlot.values()) {
            ScoreboardObjective var11 = sb.getObjectiveForSlot(var5);
            if (var11 != null) {
               if (this.c(var11.getName()) || this.a(var11.getDisplayName())) {
                  return true;
               }

               for (ScoreboardEntry var6 : sb.getScoreboardEntries(var11)) {
                  if (this.c(var6.owner()) || this.a(var6.name()) || this.a(var6.display())) {
                     return true;
                  }

                  Team var13 = sb.getScoreHolderTeam(var6.owner());
                  if (this.a(var13)) {
                     return true;
                  }
               }
            }
         }

         for (Team var10 : sb.getTeams()) {
            if (this.a(var10)) {
               return true;
            }
         }

         return false;
      }
   }

   private boolean a(Team team) {
      return team != null && (this.c(team.getName()) || this.a(team.getDisplayName()) || this.a(team.getPrefix()) || this.a(team.getSuffix()));
   }

   private boolean a(Object value, int depth, Set<Object> visited) {
      if (value == null || depth > 4) {
         return false;
      } else if (value instanceof Text var16) {
         return this.c(var16.getString());
      } else if (value instanceof String var15) {
         return this.c(var15);
      } else if (!visited.add(value)) {
         return false;
      } else if (value instanceof Map var13) {
         for (Entry var18 : var13.entrySet()) {
            if (this.a(var18.getKey(), depth + 1, visited) || this.a(var18.getValue(), depth + 1, visited)) {
               return true;
            }
         }

         return false;
      } else if (value instanceof Collection) {
         for (Object var17 : (Collection)value) {
            if (this.a(var17, depth + 1, visited)) {
               return true;
            }
         }

         return false;
      } else {
         Class var4 = value.getClass();
         if (!this.a(var4)) {
            return false;
         } else {
            for (Class var10 = var4; var10 != null && var10 != Object.class; var10 = var10.getSuperclass()) {
               for (Field var8 : var10.getDeclaredFields()) {
                  if (!Modifier.isStatic(var8.getModifiers()) && !var8.getType().isPrimitive() && !var8.getDeclaringClass().getName().startsWith("java.lang")) {
                     try {
                        var8.setAccessible(true);
                        if (this.a(var8.get(value), depth + 1, visited)) {
                           return true;
                        }
                     } catch (Exception var9) {
                     }
                  }
               }
            }

            return false;
         }
      }
   }

   private boolean a(Class<?> c) {
      Class var2 = c.getName();
      return var2.startsWith("net.minecraft.scoreboard.")
         || var2.startsWith("net.minecraft.text.")
         || var2.startsWith("net.minecraft.client.gui.hud.")
         || var2.startsWith("net.minecraft.client.network.")
         || var2.startsWith("java.util.");
   }

   private boolean c(String v) {
      return v != null && v.toLowerCase().contains("combat");
   }

   private boolean a(Text t) {
      return t != null && this.c(t.getString());
   }

   private float c() {
      return mc.player != null ? mc.player.getHealth() + mc.player.getAbsorptionAmount() : -1.0F;
   }
}
