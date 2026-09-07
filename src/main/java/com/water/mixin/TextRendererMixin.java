package com.water.mixin;

import com.water.utils.FakeRolesUtil;
import com.water.utils.NameProtectUtil;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin({TextRenderer.class})
public class TextRendererMixin {
   @ModifyVariable(
      method = {"prepare(Ljava/lang/String;FFIZI)Lnet/minecraft/client/font/TextRenderer$GlyphDrawable;"},
      at = @At("HEAD"),
      ordinal = 0,
      argsOnly = true
   )
   private String water$replacePreparedString(String var1) {
      return FakeRolesUtil.replace(NameProtectUtil.replace(var1));
   }

   @ModifyVariable(
      method = {"prepare(Lnet/minecraft/text/OrderedText;FFIZZI)Lnet/minecraft/client/font/TextRenderer$GlyphDrawable;"},
      at = @At("HEAD"),
      ordinal = 0,
      argsOnly = true
   )
   private OrderedText water$replacePreparedOrderedText(OrderedText var1) {
      return FakeRolesUtil.replace(NameProtectUtil.replace(var1));
   }

   @ModifyVariable(
      method = {"drawWithOutline(Lnet/minecraft/text/OrderedText;FFIILorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"},
      at = @At("HEAD"),
      ordinal = 0,
      argsOnly = true
   )
   private OrderedText water$replaceOutlinedOrderedText(OrderedText var1) {
      return FakeRolesUtil.replace(NameProtectUtil.replace(var1));
   }

   @ModifyVariable(
      method = {"getWidth(Ljava/lang/String;)I"},
      at = @At("HEAD"),
      ordinal = 0,
      argsOnly = true
   )
   private String water$replaceWidthString(String var1) {
      return FakeRolesUtil.replace(NameProtectUtil.replace(var1));
   }

   @ModifyVariable(
      method = {"getWidth(Lnet/minecraft/text/StringVisitable;)I"},
      at = @At("HEAD"),
      ordinal = 0,
      argsOnly = true
   )
   private StringVisitable water$replaceWidthVisitable(StringVisitable var1) {
      return FakeRolesUtil.replace(NameProtectUtil.replace(var1));
   }

   @ModifyVariable(
      method = {"getWidth(Lnet/minecraft/text/OrderedText;)I"},
      at = @At("HEAD"),
      ordinal = 0,
      argsOnly = true
   )
   private OrderedText water$replaceWidthOrderedText(OrderedText var1) {
      return FakeRolesUtil.replace(NameProtectUtil.replace(var1));
   }
}
