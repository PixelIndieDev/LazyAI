package com.pixelindiedev.lazy_ai_pixelindiedev;

import com.pixelindiedev.lazy_ai_pixelindiedev.enums.EntityCategoryEnum;
import net.minecraft.world.entity.EntityType;

public class EntityClassificationer {
    public static EntityCategoryEnum GetEntityCategory(EntityType<?> type) {
        if (EntityClassificationer.CanBePet(type)) return EntityCategoryEnum.Pet;
        else if (EntityClassificationer.IsEntityFarmAnimal(type)) return EntityCategoryEnum.Farm;
        else if (EntityClassificationer.IsAmbientAnimal(type)) return EntityCategoryEnum.Ambient;
        else return EntityCategoryEnum.Other;
    }

    private static boolean IsEntityFarmAnimal(EntityType<?> type) {
        return (type == EntityType.PIG || type == EntityType.SHEEP || type == EntityType.COW || type == EntityType.CHICKEN);
    }

    private static boolean IsAmbientAnimal(EntityType<?> type) {
        return (IsWaterAmbientAnimal(type) || type == EntityType.BAT || type == EntityType.PARROT || type == EntityType.BEE);
    }

    private static boolean IsWaterAmbientAnimal(EntityType<?> type) {
        return (type == EntityType.AXOLOTL || type == EntityType.COD || type == EntityType.GLOW_SQUID || type == EntityType.SQUID || type == EntityType.SALMON || type == EntityType.TADPOLE || type == EntityType.TROPICAL_FISH);
    }

    private static boolean CanBePet(EntityType<?> type) {
        return (type == EntityType.CAT || type == EntityType.WOLF);
    }
}
