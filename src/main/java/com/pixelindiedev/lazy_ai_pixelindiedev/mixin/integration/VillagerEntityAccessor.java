package com.pixelindiedev.lazy_ai_pixelindiedev.mixin.integration;

import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Villager.class)
public interface VillagerEntityAccessor {
    @Accessor("lastTradedPlayer")
    Player getLastCustomer();

    @Accessor("lastTradedPlayer")
    void setLastCustomer(Player lastCustomer);
}