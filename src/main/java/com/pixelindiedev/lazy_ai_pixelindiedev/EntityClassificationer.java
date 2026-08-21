package com.pixelindiedev.lazy_ai_pixelindiedev;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypeIds;

public class EntityClassificationer {
    public static boolean IsEntityFarmAnimal(ResourceKey<EntityType<?>> type) {
        return (type == EntityTypeIds.PIG || type == EntityTypeIds.SHEEP || type == EntityTypeIds.COW || type == EntityTypeIds.CHICKEN);
    }

    public static boolean IsAmbientAnimal(ResourceKey<EntityType<?>> type) {
        return (IsWaterAmbientAnimal(type) || type == EntityTypeIds.BAT || type == EntityTypeIds.PARROT || type == EntityTypeIds.BEE);
    }

    public static boolean IsWaterAmbientAnimal(ResourceKey<EntityType<?>> type) {
        return (type == EntityTypeIds.AXOLOTL || type == EntityTypeIds.COD || type == EntityTypeIds.GLOW_SQUID || type == EntityTypeIds.SQUID || type == EntityTypeIds.SALMON || type == EntityTypeIds.TADPOLE || type == EntityTypeIds.TROPICAL_FISH);
    }

    public static boolean CanBePet(ResourceKey<EntityType<?>> type) {
        return (type == EntityTypeIds.CAT);
    }
}
