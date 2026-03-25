package com.pixelindiedev.lazy_ai_pixelindiedev.mixin.integration;

import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Entity.class)
public interface EntityAccessor {
    @Accessor("wasEyeInWater")
    void setWasEyeInWater(boolean value);

    @Invoker("isEyeInFluid")
    boolean doIsEyeInFluid(final TagKey<Fluid> type);

    @Invoker("updateFluidInteraction")
    boolean invokeUpdateWaterState();
}
