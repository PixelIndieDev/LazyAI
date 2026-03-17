package com.pixelindiedev.lazy_ai_pixelindiedev.mixin.integration;

// LazyAI
// Copyright (c) 2025 PixelIndieDev
//
// Licensed under the GNU GENERAL PUBLIC LICENSE
// See the LICENSE file in the project root for full license information.

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
