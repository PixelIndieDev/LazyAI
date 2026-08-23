package com.pixelindiedev.lazy_ai_pixelindiedev.mixin.integration;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
    @Accessor("useItemRemaining")
    int getItemUseTimeLeftAccessor();

    @Accessor("useItemRemaining")
    void setItemUseTimeLeftAccessor(int value);
}
