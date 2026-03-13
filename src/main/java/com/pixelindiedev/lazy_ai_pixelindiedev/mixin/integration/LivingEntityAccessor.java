package com.pixelindiedev.lazy_ai_pixelindiedev.mixin.integration;

import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
    @Accessor("itemUseTimeLeft")
    int getItemUseTimeLeftAccessor();

    @Accessor("itemUseTimeLeft")
    void setItemUseTimeLeftAccessor(int value);
}
