package com.pixelindiedev.lazy_ai_pixelindiedev.mixin.integration;

// LazyAI
// Copyright (c) 2025 PixelIndieDev
//
// Licensed under the GNU GENERAL PUBLIC LICENSE
// See the LICENSE file in the project root for full license information.

import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(VillagerEntity.class)
public interface VillagerEntityAccessor {
    @Accessor("levelUpTimer")
    int getLevelUpTimer();

    @Accessor("levelUpTimer")
    void setLevelUpTimer(int timer);

    @Accessor("levelingUp")
    boolean isLevelingUp();

    @Invoker("levelUp")
    void invokeLevelUp(ServerWorld world);

    @Accessor("lastCustomer")
    PlayerEntity getLastCustomer();

    @Accessor("lastCustomer")
    void setLastCustomer(PlayerEntity lastCustomer);
}