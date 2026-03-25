package com.pixelindiedev.lazy_ai_pixelindiedev.mixin.integration;

import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.TransportItemsBetweenContainers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Set;

@Mixin(TransportItemsBetweenContainers.class)
public interface MoveItemsTaskAccessor {
    @Invoker("getVisitedPositions")
    static Set<GlobalPos> invokeGetVisitedPositions(PathfinderMob entity) {
        throw new AssertionError();
    }

    @Invoker("getUnreachablePositions")
    static Set<GlobalPos> invokeGetUnreachablePositions(PathfinderMob entity) {
        throw new AssertionError();
    }

    @Invoker("isPickingUpItems")
    static boolean invokeCanPickUpItem(PathfinderMob entity) {
        throw new AssertionError();
    }
}
