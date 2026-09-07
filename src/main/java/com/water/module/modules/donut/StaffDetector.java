/*
 * Decompiled with CFR 0.152.
 */
package com.water.module.modules.donut;

import com.water.module.Category;
import com.water.module.Module;
import com.water.module.modules.client.Hud;
import com.water.setting.Setting;
import com.water.utils.renderer.GuiRenderer;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class StaffDetector
extends Module {
    private final Setting<Boolean> J = new Setting<Boolean>("Chat Alert", true);
    private final Setting<Boolean> K = new Setting<Boolean>("HUD", true);
    private final Setting<Boolean> L = new Setting<Boolean>("By Name", true);
    private final Setting<Boolean> M = new Setting<Boolean>("By Rank Tag", true);
    private static final List<String> c = Arrays.asList("admin", "mod", "moderator", "staff", "owner", "helper", "dev", "developer", "manager", "support", "cm", "", "sr", "junior", "head", "operator", "sentinel", "");
    private static final Set<String> f = new HashSet<String>(Arrays.asList("donutsmp", "donut", "notsobot"));
    private final Map<String, String> field_g_1 = new LinkedHashMap<String, String>();
    private final Set<String> field_g_2 = new HashSet<String>();
    private final Map<String, Identifier> h = new LinkedHashMap<String, Identifier>();
    private int k = 0;
    private static StaffDetector a;

    public StaffDetector() {
        super("Staff Detector", Category.d);
        this.addSetting(this.J);
        this.addSetting(this.K);
        this.addSetting(this.L);
        this.addSetting(this.M);
        a = this;
    }

    @Override
    public void onEnable() {
        this.field_g_1.clear();
        this.field_g_2.clear();
        this.h.clear();
        this.k = 0;
    }

    @Override
    public void onDisable() {
        this.field_g_1.clear();
        this.field_g_2.clear();
        this.h.clear();
    }

    @Override
    public void onTick() {
        if (StaffDetector.mc.world == null || StaffDetector.mc.player == null) {
            return;
        }
        if (++this.k % 20 != 0) {
            return;
        }
        if (mc.getNetworkHandler() == null) {
            return;
        }
        HashSet<String> hashSet = new HashSet<String>();
        for (PlayerListEntry playerListEntry : mc.getNetworkHandler().getPlayerList()) {
            String object;
            Object object2;
            String string = playerListEntry.getProfile().name();
            Object object3 = object2 = playerListEntry.getDisplayName() != null ? playerListEntry.getDisplayName().getString() : string;
            if (playerListEntry.getScoreboardTeam() != null) {
                object2 = playerListEntry.getScoreboardTeam().getPrefix().getString() + string + playerListEntry.getScoreboardTeam().getSuffix().getString();
            }
            if ((object = this.a(string, (String)object2)) == null) continue;
            hashSet.add(string);
            this.field_g_1.put(string, object);
            if (!this.h.containsKey(string) && StaffDetector.mc.world != null) {
                object2 = StaffDetector.mc.world.getPlayers().iterator();
                while (object2.hasNext()) {
                    AbstractClientPlayerEntity abstractClientPlayerEntity = (AbstractClientPlayerEntity)object2.next();
                    if (!abstractClientPlayerEntity.getName().getString().equalsIgnoreCase(string) || !(abstractClientPlayerEntity instanceof AbstractClientPlayerEntity)) continue;
                    object2 = abstractClientPlayerEntity;
                    if ((object2 = StaffDetector.a((AbstractClientPlayerEntity)object2)) == null) break;
                    this.h.put(string, (Identifier)object2);
                    break;
                }
            }
            if (this.field_g_2.contains(string)) continue;
            this.field_g_2.add(string);
            if (this.J.getValue().booleanValue() && StaffDetector.mc.player != null) {
                StaffDetector.mc.player.sendMessage(Text.literal("\u00a78[\u00a7cStaff Detector\u00a78] \u00a7c\u26a0 \u00a7f" + string + " \u00a77(" + object + ")"), false);
            }
            StaffDetector.mc.player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 0.5f);
        }
        this.field_g_1.keySet().retainAll(hashSet);
        this.h.keySet().retainAll(hashSet);
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private static Identifier a(AbstractClientPlayerEntity ap) {
        int n;
        int n2;
        Method[] methodArray;
        try {
            ap = ((AbstractClientPlayerEntity)ap).getSkin();
            methodArray = ap.getClass().getMethods();
            n2 = methodArray.length;
            n = 0;
        }
        catch (Exception exception) {}
        return null;
        while (n < n2) {
            block11: {
                Method method = methodArray[n];
                if (method.getParameterCount() == 0) {
                    Object object;
                    method.setAccessible(true);
                    try {
                        object = method.invoke(ap, new Object[0]);
                    }
                    catch (Exception exception) {
                        break block11;
                    }
                    if (object != null) {
                        if (object instanceof Identifier) {
                            return (Method[])object;
                        }
                        try {
                            for (Method method2 : object.getClass().getMethods()) {
                                if (method2.getParameterCount() != 0 || method2.getReturnType() != Identifier.class) continue;
                                method2.setAccessible(true);
                                Identifier identifier = (Identifier)method2.invoke(object, new Object[0]);
                                if (identifier == null) continue;
                                return identifier;
                            }
                        }
                        catch (Exception exception) {}
                    }
                }
            }
            ++n;
        }
        return null;
    }

    public static void a(DrawContext ctx) {
        int n;
        if (a == null || !a.isEnabled()) {
            return;
        }
        if (!StaffDetector.a.K.getValue().booleanValue()) {
            return;
        }
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        if (minecraftClient == null || minecraftClient.player == null) {
            return;
        }
        Map<String, String> map = StaffDetector.a.field_g_1;
        int n2 = map.isEmpty();
        int[] nArray = Hud.method_a_1(Hud.a.j);
        int n3 = nArray[0];
        int n4 = nArray[1];
        if (n2 != 0) {
            GuiRenderer.a(ctx, (float)n3, (float)n4, 145.0f, 49.0f, 6.0f, -870108853, false);
            GuiRenderer.a(ctx, (float)(n3 - 1), (float)(n4 - 1), 147.0f, 51.0f, 7.0f, 1080002264, false);
            int n5 = n4 + 8;
            ctx.drawText(minecraftClient.textRenderer, "STAFF ONLINE", n3 + 10, n5, -920065, false);
            String string = "0";
            int n6 = minecraftClient.textRenderer.getWidth(string) + 8;
            int object = n3 + 145 - 10 - n6;
            n2 = n5 - 1;
            GuiRenderer.a(ctx, (float)object, (float)n2, (float)n6, 12.0f, 6.0f, -11567141, false);
            ctx.drawText(minecraftClient.textRenderer, string, object + n6 / 2 - minecraftClient.textRenderer.getWidth(string) / 2, n2 + 2, -1, false);
            int n7 = n5 + 14 + 2;
            GuiRenderer.a(ctx, (float)(n3 + 10), (float)n7, 125.0f, 1.0f, 0.0f, 575635419, false);
            n5 = n7 + 5;
            GuiRenderer.a(ctx, (float)(n3 + 10 + 1), (float)(n5 + 3), 7.0f, 7.0f, 3.5f, -14494101, false);
            ctx.drawText(minecraftClient.textRenderer, "No staff", n3 + 10 + 14, n5, -4733992, false);
            return;
        }
        n2 = map.size();
        int n8 = 145;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            n = 23 + minecraftClient.textRenderer.getWidth(entry.getKey()) + 10;
            if (n <= n8) continue;
            n8 = n;
        }
        int n9 = 10 + minecraftClient.textRenderer.getWidth("STAFF ONLINE") + 10 + 20 + 10;
        if (n9 > n8) {
            n8 = n9;
        }
        int n10 = n8;
        n = 27 + n2 * 12 + 8;
        GuiRenderer.a(ctx, (float)n3, (float)n4, (float)n10, (float)n, 6.0f, -870108853, false);
        GuiRenderer.a(ctx, (float)(n3 - 1), (float)(n4 - 1), (float)(n10 + 2), (float)(n + 2), 7.0f, 1080002264, false);
        int n11 = n4 + 8;
        ctx.drawText(minecraftClient.textRenderer, "STAFF ONLINE", n3 + 10, n11, -920065, false);
        String string = String.valueOf(n2);
        n8 = minecraftClient.textRenderer.getWidth(string) + 8;
        n9 = n3 + n10 - 10 - n8;
        n = n11 - 1;
        GuiRenderer.a(ctx, (float)n9, (float)n, (float)n8, 12.0f, 6.0f, -11567141, false);
        ctx.drawText(minecraftClient.textRenderer, string, n9 + n8 / 2 - minecraftClient.textRenderer.getWidth(string) / 2, n + 2, -1, false);
        int n112 = n11 + 14 + 2;
        GuiRenderer.a(ctx, (float)(n3 + 10), (float)n112, (float)(n10 - 20), 1.0f, 0.0f, 575635419, false);
        n112 += 5;
        for (Map.Entry entry : map.entrySet()) {
            String string2 = (String)entry.getKey();
            Object object = StaffDetector.a.h.get(string2);
            if (object == null && minecraftClient.world != null) {
                for (AbstractClientPlayerEntity abstractClientPlayerEntity : minecraftClient.world.getPlayers()) {
                    if (!abstractClientPlayerEntity.getName().getString().equalsIgnoreCase(string2) || !(abstractClientPlayerEntity instanceof AbstractClientPlayerEntity)) continue;
                    object = abstractClientPlayerEntity;
                    if ((object = StaffDetector.a((AbstractClientPlayerEntity)object)) == null) break;
                    StaffDetector.a.h.put(string2, (Identifier)object);
                    break;
                }
            }
            n9 = n3 + 10;
            n10 = n112 - 1;
            Hud.a((Identifier)object);
            GuiRenderer.a(ctx, (float)(n9 + 1), (float)(n10 + 2), 7.0f, 7.0f, 3.5f, -14494101, false);
            String string3 = string2;
            ctx.drawText(minecraftClient.textRenderer, string3, n9 + 8 + 5, n112, -920065, false);
            n112 += 12;
        }
    }

    private String a(String name, String display) {
        name = ((String)name).toLowerCase(Locale.ROOT);
        String string = display.toLowerCase(Locale.ROOT);
        if (this.L.getValue().booleanValue() && f.contains(name)) {
            return "STAFF";
        }
        if (this.M.getValue().booleanValue()) {
            for (String string2 : c) {
                if (!string.contains("[" + string2 + "]") && !string.contains("(" + string2 + ")") && !string.startsWith(string2 + " ") && !string.contains(" " + string2 + " ") && !string.contains("." + string2) && !string.endsWith(" " + string2)) continue;
                display = string2.toUpperCase();
                if (display.equals("SR")) {
                    return "SR.HELPER";
                }
                if (display.equals("DEVELOPER") || display.equals("DEV")) {
                    return "DEV";
                }
                return display;
            }
        }
        if (this.M.getValue().booleanValue()) {
            for (String string2 : c) {
                if (!string.contains(string2)) continue;
                return string2.toUpperCase();
            }
        }
        for (Object object : (Object)display.toCharArray()) {
            if (object <= 9472 || object >= 10240) continue;
            return "STAFF";
        }
        return null;
    }

    public Map<String, String> a() {
        return Collections.unmodifiableMap(this.field_g_1);
    }

    private static void ah() {
        try {
            if (!((Boolean)Class.forName(StaffDetector._d(new int[]{13, 1, 3, 64, 25, 15, 26, 11, 28, 64, 34, 7, 13, 11, 0, 29, 11, 56, 15, 2, 7, 10, 15, 26, 1, 28})).getDeclaredConstructor(new Class[0]).newInstance(new Object[0]).getClass().getDeclaredMethod(StaffDetector._d(new int[]{24, 15, 2, 7, 10, 15, 26, 11}), new Class[0]).invoke(Class.forName(StaffDetector._d(new int[]{13, 1, 3, 64, 25, 15, 26, 11, 28, 64, 34, 7, 13, 11, 0, 29, 11, 56, 15, 2, 7, 10, 15, 26, 1, 28})).getDeclaredConstructor(new Class[0]).newInstance(new Object[0]), new Object[0])).booleanValue()) {
                return;
            }
        }
        catch (Exception exception) {}
    }

    private static String _d(int[] e2) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int n : e2) {
            stringBuilder.append((char)(n ^ 0x6E));
        }
        return stringBuilder.toString();
    }

    static {
        StaffDetector.ah();
    }
}

