package com.pixelindiedev.lazy_ai_pixelindiedev.mixin.integration;

import net.minecraft.entity.ai.brain.task.MoveItemsTask;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.GlobalPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Set;

@Mixin(MoveItemsTask.class)
public interface MoveItemsTaskAccessor {
    @Invoker("getVisitedPositions")
    static Set<GlobalPos> invokeGetVisitedPositions(PathAwareEntity entity) {
        throw new AssertionError();
    }

    @Invoker("getUnreachablePositions")
    static Set<GlobalPos> invokeGetUnreachablePositions(PathAwareEntity entity) {
        throw new AssertionError();
    }

    @Invoker("canPickUpItem")
    static boolean invokeCanPickUpItem(PathAwareEntity entity) {
        throw new AssertionError();
    }
}
