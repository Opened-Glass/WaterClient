package com.water.setting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public final class MobsSetting extends Setting<Set<EntityType<?>>> {
   private final List<EntityType<?>> availableMobs = Registries.ENTITY_TYPE
      .stream()
      .filter(MobsSetting::isLivingMob)
      .sorted(Comparator.comparing(this::getDisplayName, String.CASE_INSENSITIVE_ORDER))
      .toList();
   private long version;

   public MobsSetting(String name, EntityType<?>... defaults) {
      super(name, createDefaultSet(defaults));
   }

   private static boolean isLivingMob(EntityType<?> type) {
      EntityType var1 = type.getSpawnGroup();
      return var1 == SpawnGroup.MONSTER
         || var1 == SpawnGroup.CREATURE
         || var1 == SpawnGroup.AMBIENT
         || var1 == SpawnGroup.AXOLOTLS
         || var1 == SpawnGroup.UNDERGROUND_WATER_CREATURE
         || var1 == SpawnGroup.WATER_CREATURE
         || var1 == SpawnGroup.WATER_AMBIENT;
   }

   public void setValue(Set<EntityType<?>> value) {
      LinkedHashSet var2 = new LinkedHashSet();
      if (value != null) {
         for (EntityType var3 : value) {
            if (var3 != null) {
               var2.add(var3);
            }
         }
      }

      super.setValue(var2);
      this.version++;
   }

   public boolean contains(EntityType<?> type) {
      return type != null && this.getValue().contains(type);
   }

   public void toggle(EntityType<?> type) {
      if (type != null) {
         LinkedHashSet var2 = new LinkedHashSet<>(this.getValue());
         if (!var2.add(type)) {
            var2.remove(type);
         }

         this.setValue(var2);
      }
   }

   public void clear() {
      if (!this.getValue().isEmpty()) {
         this.setValue(Collections.emptySet());
      }
   }

   public int size() {
      return this.getValue().size();
   }

   public long getVersion() {
      return this.version;
   }

   public Set<EntityType<?>> getSelectedMobs() {
      return Collections.unmodifiableSet(this.getValue());
   }

   public List<EntityType<?>> getAvailableMobs() {
      return this.availableMobs;
   }

   public List<EntityType<?>> filter(String query) {
      query = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
      if (query.isEmpty()) {
         return this.availableMobs;
      } else {
         ArrayList var2 = new ArrayList();

         for (EntityType var4 : this.availableMobs) {
            String var5 = this.getDisplayName(var4).toLowerCase(Locale.ROOT);
            Identifier var6 = Registries.ENTITY_TYPE.getId(var4);
            String var8 = var6 == null ? "" : var6.toString().toLowerCase(Locale.ROOT);
            if (var5.contains(query) || var8.contains(query)) {
               var2.add(var4);
            }
         }

         return var2;
      }
   }

   public String getDisplayName(EntityType<?> type) {
      try {
         return type.getName().getString();
      } catch (Exception var2) {
         EntityType var3 = Registries.ENTITY_TYPE.getId(type);
         return var3 == null ? "Mob" : var3.getPath();
      }
   }

   public String getSummary() {
      if (this.getValue().isEmpty()) {
         return "None";
      } else {
         EntityType var1 = this.getValue().iterator().next();
         String var3 = this.getDisplayName(var1);
         int var2 = this.getValue().size() - 1;
         return var2 > 0 ? var3 + " +" + var2 : var3;
      }
   }

   private static Set<EntityType<?>> createDefaultSet(EntityType<?>... defaults) {
      LinkedHashSet var1 = new LinkedHashSet();
      if (defaults != null) {
         Collections.addAll(var1, defaults);
         var1.remove(null);
      }

      return var1;
   }
}
