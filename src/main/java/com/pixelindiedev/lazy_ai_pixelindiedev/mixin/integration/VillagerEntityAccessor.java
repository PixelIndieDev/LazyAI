package com.pixelindiedev.lazy_ai_pixelindiedev.mixin.integration;

import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
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
    void invokeLevelUp();

    @Accessor("lastCustomer")
    PlayerEntity getLastCustomer();

    @Accessor("lastCustomer")
    void setLastCustomer(PlayerEntity lastCustomer);
}