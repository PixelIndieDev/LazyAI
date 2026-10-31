package com.pixelindiedev.lazy_ai_pixelindiedev.mixin.integration;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Entity.class)
public interface EntityAccessor {
    @Invoker("updateFluidInteraction")
    boolean invokeUpdateWaterState();
}
