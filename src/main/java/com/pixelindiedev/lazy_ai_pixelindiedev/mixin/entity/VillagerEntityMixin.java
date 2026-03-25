package com.pixelindiedev.lazy_ai_pixelindiedev.mixin.entity;

// LazyAI
// Copyright (c) 2025 PixelIndieDev
//
// Licensed under the GNU GENERAL PUBLIC LICENSE
// See the LICENSE file in the project root for full license information.

import com.pixelindiedev.lazy_ai_pixelindiedev.Lazy_ai_pixelindiedev;
import com.pixelindiedev.lazy_ai_pixelindiedev.interfaces.VillagerCacheAccessor;
import com.pixelindiedev.lazy_ai_pixelindiedev.mixin.integration.VillagerEntityAccessor;
import it.unimi.dsi.fastutil.longs.Long2BooleanOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.village.ReputationEventType;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.pixelindiedev.lazy_ai_pixelindiedev.LazyAI$BlockChecker.hasSolidCollision;
import static com.pixelindiedev.lazy_ai_pixelindiedev.Lazy_ai_pixelindiedev.getOptimalizationType;

@Mixin(Villager.class)
public abstract class VillagerEntityMixin implements VillagerCacheAccessor {
    @Unique
    private final static int[] cooldowns = {50, 90, 150};  // Cooldowns from close to far, in ticks
    @Unique
    private final static int[] cooldownsAgressive = {70, 115, 250};
    @Unique
    private final static int[] cooldownsMinimal = {30, 70, 110};
    @Unique
    private final Long2BooleanOpenHashMap cachedBlockPos = new Long2BooleanOpenHashMap(9);
    @Unique
    private final Direction[] directionsDirections = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
    @Unique
    private BlockPos.MutableBlockPos reusableSide = new BlockPos.MutableBlockPos();
    @Unique
    private Villager villager;
    @Unique
    private boolean isInTradingHall;
    @Unique
    private boolean shouldRefreshTradingHall;
    @Unique
    private BlockPos lastStandingLocation;
    @Unique
    private int randomSelectedTick;
    @Unique
    private Holder<VillagerProfession> cachedProfessionEntry;
    @Unique
    private ResourceKey<VillagerProfession> cachedProfessionKey;

    @Shadow
    protected abstract void stopTrading();

    @Inject(method = "<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/Holder;)V", at = @At("RETURN"))
    private void captureMob(EntityType entityType, Level world, Holder type, CallbackInfo ci) {
        villager = (Villager) (Object) this;
        isInTradingHall = false;
        shouldRefreshTradingHall = false;
        randomSelectedTick = villager.getId();
        cachedProfessionEntry = null;
        cachedProfessionKey = null;
        reusableSide = new BlockPos.MutableBlockPos();
    }

    @Override
    public void lazyai$invalidateBlockCache(BlockPos pos) {
        final long key = pos.asLong();
        if (cachedBlockPos.containsKey(key)) {
            cachedBlockPos.remove(key);
            shouldRefreshTradingHall = true;
        }
    }

    @Inject(method = "customServerAiStep", at = @At("HEAD"), cancellable = true)
    private void skipIdleTradingHallTick(ServerLevel world, CallbackInfo ci) {
        if (villager == null || !villager.isAlive() || villager.isBaby() || villager.isPanicking()) return;
        if (!isInTradingCell(villager)) return;

        final ResourceKey<VillagerProfession> villagerprof = getCachedProfession();
        if (villagerprof == VillagerProfession.NONE || villagerprof == VillagerProfession.NITWIT) return;

        if (villager.isTrading()) return;

        if (((villager.tickCount + randomSelectedTick) & 31) != 0) {
            final VillagerEntityAccessor accessor = (VillagerEntityAccessor) villager;

            final int tempInt = accessor.getLevelUpTimer();
            if (!villager.isTrading() && tempInt > 0) {
                accessor.setLevelUpTimer(tempInt - 1);

                if (accessor.getLevelUpTimer() <= 0) {
                    if (accessor.isLevelingUp()) accessor.invokeLevelUp(world);
                    villager.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 0));
                }
            }

            final Player lastcust = accessor.getLastCustomer();
            if (lastcust != null) {
                world.onReputationEvent(ReputationEventType.TRADE, lastcust, villager);
                world.broadcastEntityEvent(villager, (byte) 14);
                accessor.setLastCustomer(null);
            }

            if (villager.getVillagerData().profession().is(VillagerProfession.NONE) && villager.isTrading())
                stopTrading();

            ci.cancel();
        }
    }

    @Unique
    private boolean isInTradingCell(Villager villager) {
        final int[] cooldownList = getCooldownList();
        final int distanceOrdinal = Lazy_ai_pixelindiedev.getDistance(villager).ordinal();

        if ((villager.tickCount + randomSelectedTick) % cooldownList[distanceOrdinal] != 0) return isInTradingHall;

        final BlockPos center = villager.blockPosition();
        //if block was changed near, or villager is no longer standing in the same spot
        if (shouldRefreshTradingHall || lastStandingLocation != center) {
            shouldRefreshTradingHall = false;
            lastStandingLocation = center;

            final Level world = villager.level();

            int fullyBlockedDirections = 0;
            int halfBlockedDirections = 0;
            for (Direction direction : directionsDirections) {
                reusableSide.setWithOffset(center, direction);
                boolean baseSolid = getCachedSolidBlock(world, reusableSide);
                reusableSide.move(Direction.UP);
                boolean upperSolid = getCachedSolidBlock(world, reusableSide);

                if (baseSolid) {
                    if (upperSolid) fullyBlockedDirections++;
                    else halfBlockedDirections++;
                } else {
                    if (upperSolid) fullyBlockedDirections++;
                    else return isInTradingHall = false; //open wall found
                }
            }

            //fully closed off
            if (fullyBlockedDirections >= 4) return isInTradingHall = true;

            if (halfBlockedDirections > 0) {
                //check ceiling, if villager can jump to get out of hole
                reusableSide.set(center).move(Direction.UP, 2);
                return isInTradingHall = getCachedSolidBlock(world, reusableSide);
            }

            //should not reach this, but just in case
            return isInTradingHall = false;
        }

        //trading hall check does not need to refresh, so return the saved value
        return isInTradingHall;
    }

    @Unique
    private ResourceKey<VillagerProfession> getCachedProfession() {
        final Holder<VillagerProfession> current = villager.getVillagerData().profession();
        if (current != cachedProfessionEntry) {
            cachedProfessionEntry = current;
            cachedProfessionKey = current.unwrapKey().get();
        }
        return cachedProfessionKey;
    }

    @Unique
    private boolean getCachedSolidBlock(Level world, BlockPos pos) {
        final long key = pos.asLong();
        if (cachedBlockPos.containsKey(key)) return cachedBlockPos.get(key);
        else {
            final BlockState state = world.getBlockState(pos);
            final boolean isSolid = hasSolidCollision(state);
            cachedBlockPos.put(key, isSolid);
            return isSolid;
        }
    }

    @Unique
    private int[] getCooldownList() {
        return switch (getOptimalizationType()) {
            case Minimal -> cooldownsMinimal;
            case Agressive -> cooldownsAgressive;
            case null, default -> cooldowns;
        };
    }
}