package com.water.gui;

import com.water.module.Category;
import com.water.module.Module;
import com.water.module.ModuleManager;
import com.water.module.modules.client.Hud;
import com.water.module.modules.client.SpotifyHud;
import com.water.module.modules.client.WaterPlus;
import com.water.utils.renderer.GuiRenderer;
import java.util.ArrayList;
import java.util.Comparator;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import org.lwjgl.glfw.GLFW;

public final class HudEditor {
   public static final HudEditor INSTANCE = new HudEditor();
   private static final int HANDLE_SIZE = 8;
   private String dragging = null;
   private int grabOffsetX;
   private int grabOffsetY;
   private boolean isDragging = false;
   private String resizing = null;
   private int resizeStartX;
   private int resizeStartY;
   private float resizeStartScale;
   public static volatile boolean isEditing = false;

   private HudEditor() {
   }

   private static float hs() {
      return Hud.method_a_2();
   }

   public boolean onMouseClick(double mouseX, double mouseY, int button) {
      if (button != 0 && button != 1) {
         return false;
      } else {
         for (Hud.a var9 : Hud.a.values()) {
            if (Hud.method_a_4(var9) && hasContent(var9)) {
               int[] var10 = handlePos(var9);
               int var11 = handleW(var9);
               int var12 = handleH(var9);
               if (button == 0 && isOnResizeHandle(mouseX, mouseY, var10, var11, var12)) {
                  this.resizing = var9.name();
                  this.resizeStartX = (int)mouseX;
                  this.resizeStartY = (int)mouseY;
                  this.resizeStartScale = elementScale(var9);
                  this.isDragging = false;
                  this.dragging = null;
                  isEditing = true;
                  return true;
               }

               if (mouseX >= var10[0] && mouseX <= var10[0] + var11 && mouseY >= var10[1] && mouseY <= var10[1] + var12) {
                  if (button == 1 && var9 == Hud.a.h) {
                     double var13 = MinecraftClient.getInstance();
                     if (var13 != null) {
                        var13.setScreen(new SpotifyBrowserScreen(var13.currentScreen));
                     }

                     return true;
                  }

                  if (button == 0) {
                     this.dragging = var9.name();
                     this.grabOffsetX = (int)(mouseX - var10[0]);
                     this.grabOffsetY = (int)(mouseY - var10[1]);
                     this.isDragging = true;
                     this.resizing = null;
                     isEditing = true;
                     return true;
                  }
               }
            }
         }

         if (button == 0) {
            this.dragging = null;
            this.isDragging = false;
            this.resizing = null;
         }

         return false;
      }
   }

   public boolean isDragging() {
      return this.dragging != null && this.isDragging || this.resizing != null;
   }

   public boolean isResizing() {
      return this.resizing != null;
   }

   public void onMouseDrag(double mouseX, double mouseY) {
      MinecraftClient var5 = MinecraftClient.getInstance();
      boolean var6 = var5 != null && var5.getWindow() != null && GLFW.glfwGetMouseButton(var5.getWindow().getHandle(), 0) == 1;
      if (!var6) {
         this.onMouseRelease();
      } else if (this.resizing != null) {
         try {
            Hud.a var20 = Hud.a.valueOf(this.resizing);
            double var21 = mouseX - this.resizeStartX;
            double var24 = mouseY - this.resizeStartY;
            double var28 = var21 + var24;
            double var18 = Math.max(0.5F, Math.min(3.0F, this.resizeStartScale + (float)(var28 * 0.006F)));
            Hud.a(var20, var18);
         } catch (Exception var14) {
         }
      } else if (this.dragging != null && this.isDragging) {
         try {
            Hud.a var19 = Hud.a.valueOf(this.dragging);
            float var8 = Hud.method_a_2(var19);
            if (var19 == Hud.a.h) {
               int var9 = (int)Math.round(mouseX - this.grabOffsetX);
               int var10 = (int)Math.round(mouseY - this.grabOffsetY);
               if (var5 != null && var5.getWindow() != null) {
                  int var11 = var5.getWindow().getScaledWidth();
                  int var12 = var5.getWindow().getScaledHeight();
                  int var13 = SpotifyHud.a();
                  double var16 = SpotifyHud.method_b_2();
                  var9 = Math.max(0, Math.min(var9, var11 - var13));
                  var10 = Math.max(0, Math.min(var10, var12 - var16));
               }

               Hud.a(var19, var9, var10);
            } else {
               int var22 = (int)Math.round((mouseX - this.grabOffsetX) / var8);
               int var23 = (int)Math.round((mouseY - this.grabOffsetY) / var8);
               if (var5 != null && var5.getWindow() != null) {
                  int var25 = var5.getWindow().getScaledWidth();
                  int var27 = var5.getWindow().getScaledHeight();
                  int[] var29 = var19 == Hud.a.d ? Hud.method_a_3() : Hud.b(var19);
                  double var17 = var29[2];
                  int var2 = var29[3];
                  if (var17 < 1) {
                     var17 = 1;
                  }

                  if (var2 < 1) {
                     var2 = 1;
                  }

                  if (var25 > 0) {
                     var22 = Math.max(0, Math.min(var22, (int)((var25 - var17 * var8) / var8)));
                  }

                  if (var27 > 0) {
                     var23 = Math.max(0, Math.min(var23, (int)((var27 - var2 * var8) / var8)));
                  }
               }

               if (var19 == Hud.a.d) {
                  int[] var26 = Hud.method_a_3();
                  Hud.a(var19, var22 + var26[2], var23);
               } else {
                  Hud.a(var19, var22, var23);
               }
            }
         } catch (Exception var15) {
         }
      }
   }

   public void onMouseRelease() {
      this.dragging = null;
      this.isDragging = false;
      this.resizing = null;
      isEditing = false;
   }

   public boolean onMouseScroll(double mouseX, double mouseY, double amount) {
      if (amount == 0.0) {
         return false;
      } else if (!Hud.method_a_4(Hud.a.h)) {
         return false;
      } else {
         int[] var7 = Hud.method_a_1(Hud.a.h);
         int var8 = SpotifyHud.a();
         int var9 = SpotifyHud.method_b_2();
         if (!(mouseX < var7[0]) && !(mouseX > var7[0] + var8) && !(mouseY < var7[1]) && !(mouseY > var7[1] + var9)) {
            SpotifyHud.method_a_1(SpotifyHud.method_b_1() + (float)(amount * 0.08));
            return true;
         } else {
            return false;
         }
      }
   }

   public void render(DrawContext context, int mouseX, int mouseY) {
      MinecraftClient var4 = MinecraftClient.getInstance();
      if (var4 != null) {
         int var5 = WaterPlus.getAccentARGB();
         float var6 = WaterPlus.getGuiRoundness();

         for (Hud.a var10 : Hud.a.values()) {
            if (Hud.method_a_4(var10) && (hasContent(var10) || var10 == Hud.a.h)) {
               int[] var11 = handlePos(var10);
               int var12 = handleW(var10);
               int var13 = handleH(var10);
               if (var10 == Hud.a.d) {
                  int[] var14 = Hud.method_a_3();
                  GuiRenderer.a(context, (float)var14[0], (float)var14[1], (float)var14[2], (float)var14[3], 3.0F, 1.0F, var5, false);
               } else if (var10 == Hud.a.field_e_1) {
                  if (var4.player == null) {
                     continue;
                  }

                  TextRenderer var19 = var4.textRenderer;
                  ArrayList var15 = new ArrayList<>(var4.player.getStatusEffects());
                  var15.sort(Comparator.comparingInt(e -> var19.getWidth(Hud.method_a_1(e))));
                  int[] var16 = Hud.method_a_1(Hud.a.field_e_1);
                  int var17 = var16[1];

                  for (StatusEffectInstance var18 : var15) {
                     String var26 = Hud.method_a_1(var18);
                     int var27 = var19.getWidth(var26) + 14;
                     GuiRenderer.a(context, (float)var16[0], (float)var17, (float)var27, 14.0F, 5.0F, 1.0F, var5, false);
                     var17 += 17;
                  }
               } else {
                  GuiRenderer.a(context, (float)var11[0], (float)var11[1], (float)var12, (float)var13, var6, 1.0F, var5, false);
               }

               if (var10 != Hud.a.d && var10 != Hud.a.field_e_1) {
                  boolean var20 = isOnResizeHandle(mouseX, mouseY, var11, var12, var13);
                  boolean var22 = this.resizing != null && this.resizing.equals(var10.name());
                  int var24 = var22 ? var5 : (var20 ? blendArgb(var5, -1, 0.3F) : var5 & 16777215 | -1728053248);
                  float var25 = var11[0] + var12 - 8;
                  float var23 = var11[1] + var13 - 8;
                  GuiRenderer.a(context, var25, var23, 8.0F, 8.0F, Math.min(var6, 4.0F), var24, false);
                  context.fill((int)(var25 + 2.0F), (int)(var23 + 8.0F - 3.0F), (int)(var25 + 8.0F - 1.0F), (int)(var23 + 8.0F - 2.0F), -855638017);
                  context.fill((int)(var25 + 2.0F), (int)(var23 + 8.0F - 5.0F), (int)(var25 + 8.0F - 3.0F), (int)(var23 + 8.0F - 4.0F), -855638017);
               }
            }
         }
      }
   }

   private static boolean isOnResizeHandle(double mouseX, double mouseY, int[] pos, int w, int h) {
      return mouseX >= pos[0] + w - 8 && mouseX <= pos[0] + w && mouseY >= pos[1] + h - 8 && mouseY <= pos[1] + h;
   }

   private static float elementScale(Hud.a el) {
      return Hud.method_a_2(el);
   }

   private static int blendArgb(int a, int b, float t) {
      int var3 = a >> 16 & 0xFF;
      int var4 = a >> 8 & 0xFF;
      int var5 = a & 0xFF;
      a = a >> 24 & 0xFF;
      int var6 = b >> 16 & 0xFF;
      int var7 = b >> 8 & 0xFF;
      int var8 = b & 0xFF;
      b = b >> 24 & 0xFF;
      return (int)(a + (b - a) * t) << 24 | (int)(var3 + (var6 - var3) * t) << 16 | (int)(var4 + (var7 - var4) * t) << 8 | (int)(var5 + (var8 - var5) * t);
   }

   private static boolean hasContent(Hud.a el) {
      MinecraftClient var1 = MinecraftClient.getInstance();
      if (var1 == null) {
         return false;
      } else {
         return switch (el) {
            case f -> {
               if (var1.player == null) {
                  yield false;
               } else {
                  Hud.a var6 = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

                  for (EquipmentSlot var3 : var6) {
                     ItemStack var10 = var1.player.getEquippedStack(var3);
                     if (var10 != null && !var10.isEmpty()) {
                        yield true;
                     }
                  }

                  yield false;
               }
            }
            case field_e_1 -> var1.player == null ? false : !var1.player.getStatusEffects().isEmpty();
            case d -> {
               for (Module var8 : ModuleManager.INSTANCE.getModules()) {
                  if (var8.isEnabled() && var8.getCategory() != Category.e) {
                     yield true;
                  }
               }

               yield false;
            }
            case g -> {
               for (Module var2 : ModuleManager.INSTANCE.getModules()) {
                  if (var2.getBind() != 0) {
                     yield true;
                  }
               }

               yield false;
            }
            default -> true;
         };
      }
   }

   private static int[] handlePos(Hud.a el) {
      float var1 = Hud.method_a_2(el);
      if (el == Hud.a.d) {
         Hud.a var5 = Hud.method_a_3();
         return new int[]{var5[0], var5[1]};
      } else if (el == Hud.a.h) {
         Hud.a var4 = Hud.method_a_1(el);
         MinecraftClient var6 = MinecraftClient.getInstance();
         if (var6 != null && var6.getWindow() != null) {
            int var2 = Math.max(0, var6.getWindow().getScaledWidth() - SpotifyHud.a());
            int var7 = Math.max(0, var6.getWindow().getScaledHeight() - SpotifyHud.method_b_2());
            return new int[]{Math.max(0, Math.min(var4[0], var2)), Math.max(0, Math.min(var4[1], var7))};
         } else {
            return new int[]{var4[0], var4[1]};
         }
      } else {
         Hud.a var3 = Hud.method_a_1(el);
         return new int[]{Math.round(var3[0] * var1), Math.round(var3[1] * var1)};
      }
   }

   private static int handleW(Hud.a el) {
      float var1 = Hud.method_a_2(el);
      if (el == Hud.a.d) {
         return Math.round(Hud.method_a_3()[2] * var1);
      } else {
         return el == Hud.a.h ? SpotifyHud.a() : Math.round(Hud.b(el)[2] * var1);
      }
   }

   private static int handleH(Hud.a el) {
      float var1 = Hud.method_a_2(el);
      if (el == Hud.a.d) {
         return Math.round(Hud.method_a_3()[3] * var1);
      } else {
         return el == Hud.a.h ? SpotifyHud.method_b_2() : Math.round(Hud.b(el)[3] * var1);
      }
   }

   static String _c4e6cf034db() {
      return "A";
   }
}
