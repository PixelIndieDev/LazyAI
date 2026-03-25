package com.pixelindiedev.lazy_ai_pixelindiedev.mixin.integration;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Villager.class)
public interface VillagerEntityAccessor {
    @Accessor("updateMerchantTimer")
    int getLevelUpTimer();

    @Accessor("updateMerchantTimer")
    void setLevelUpTimer(int timer);

    @Accessor("increaseProfessionLevelOnUpdate")
    boolean isLevelingUp();

    @Invoker("increaseMerchantCareer")
    void invokeLevelUp(ServerLevel world);

    @Accessor("lastTradedPlayer")
    Player getLastCustomer();

    @Accessor("lastTradedPlayer")
    void setLastCustomer(Player lastCustomer);
}