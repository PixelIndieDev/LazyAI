package com.pixelindiedev.lazy_ai_pixelindiedev;

import com.pixelindiedev.lazy_ai_pixelindiedev.config.EntityCategoryEnum;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypeIds;

public class EntityClassificationer {
    public static EntityCategoryEnum GetEntityCategory(ResourceKey<EntityType<?>> type) {
        if (EntityClassificationer.CanBePet(type))  return EntityCategoryEnum.Pet;
        else if (EntityClassificationer.IsEntityFarmAnimal(type))  return EntityCategoryEnum.Farm;
        else if (EntityClassificationer.IsAmbientAnimal(type)) return EntityCategoryEnum.Ambient;
        else return EntityCategoryEnum.Other;
    }

    private static boolean IsEntityFarmAnimal(ResourceKey<EntityType<?>> type) {
        return (type == EntityTypeIds.PIG || type == EntityTypeIds.SHEEP || type == EntityTypeIds.COW || type == EntityTypeIds.CHICKEN);
    }

    private static boolean IsAmbientAnimal(ResourceKey<EntityType<?>> type) {
        return (IsWaterAmbientAnimal(type) || type == EntityTypeIds.BAT || type == EntityTypeIds.PARROT || type == EntityTypeIds.BEE);
    }

    private static boolean IsWaterAmbientAnimal(ResourceKey<EntityType<?>> type) {
        return (type == EntityTypeIds.AXOLOTL || type == EntityTypeIds.COD || type == EntityTypeIds.GLOW_SQUID || type == EntityTypeIds.SQUID || type == EntityTypeIds.SALMON || type == EntityTypeIds.TADPOLE || type == EntityTypeIds.TROPICAL_FISH);
    }

    private static boolean CanBePet(ResourceKey<EntityType<?>> type) {
        return (type == EntityTypeIds.CAT || type == EntityTypeIds.WOLF);
    }
}
