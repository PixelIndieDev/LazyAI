package com.pixelindiedev.lazy_ai_pixelindiedev.mixin;

import com.pixelindiedev.lazy_ai_pixelindiedev.Lazy_ai_pixelindiedev;
import com.pixelindiedev.lazy_ai_pixelindiedev.interfaces.VillagerCacheAccessor;
import com.pixelindiedev.lazy_ai_pixelindiedev.mixin.integration.VillagerEntityAccessor;
import it.unimi.dsi.fastutil.longs.Long2BooleanOpenHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityInteraction;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.pixelindiedev.lazy_ai_pixelindiedev.LazyAI$BlockChecker.hasSolidCollision;
import static com.pixelindiedev.lazy_ai_pixelindiedev.Lazy_ai_pixelindiedev.getOptimalizationType;

@Mixin(VillagerEntity.class)
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
    private BlockPos.Mutable reusableSide = new BlockPos.Mutable();
    @Unique
    private VillagerEntity villager;
    @Unique
    private boolean isInTradingHall;
    @Unique
    private boolean shouldRefreshTradingHall;
    @Unique
    private BlockPos lastStandingLocation;
    @Unique
    private int randomSelectedTick;
    @Unique
    private VillagerProfession cachedProfessionEntry;

    @Shadow
    protected abstract void resetCustomer();

    @Inject(method = "<init>(Lnet/minecraft/entity/EntityType;Lnet/minecraft/world/World;)V", at = @At("RETURN"))
    private void captureMob(EntityType entityType, World world, CallbackInfo ci) {
        villager = (VillagerEntity) (Object) this;
        isInTradingHall = false;
        shouldRefreshTradingHall = false;
        randomSelectedTick = villager.getId();
        cachedProfessionEntry = null;
        reusableSide = new BlockPos.Mutable();
    }

    @Override
    public void lazyai$invalidateBlockCache(BlockPos pos) {
        long key = pos.asLong();
        if (cachedBlockPos.containsKey(key)) {
            cachedBlockPos.remove(key);
            shouldRefreshTradingHall = true;
        }
    }

    @Inject(method = "mobTick", at = @At("HEAD"), cancellable = true)
    private void skipIdleTradingHallTick(CallbackInfo ci) {
        if (villager == null || !villager.isAlive() || villager.isBaby() || isPanicking()) return;
        if (!isInTradingCell(villager)) return;

        VillagerProfession villagerprof = getCachedProfession();
        if (villagerprof == VillagerProfession.NONE || villagerprof == VillagerProfession.NITWIT) return;

        if (villager.hasCustomer()) return;

        if (((villager.age + randomSelectedTick) & 31) != 0) {
            VillagerEntityAccessor accessor = (VillagerEntityAccessor) villager;

            int tempInt = accessor.getLevelUpTimer();
            if (!villager.hasCustomer() && tempInt > 0) {
                accessor.setLevelUpTimer(tempInt - 1);

                if (accessor.getLevelUpTimer() <= 0) {
                    if (accessor.isLevelingUp()) accessor.invokeLevelUp();
                    villager.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 200, 0));
                }
            }

            PlayerEntity lastcust = accessor.getLastCustomer();
            if (lastcust != null && villager.getWorld() instanceof ServerWorld world) {
                (world).handleInteraction(EntityInteraction.TRADE, lastcust, villager);
                world.sendEntityStatus(villager, (byte) 14);
                accessor.setLastCustomer(null);
            }

            if (villager.getVillagerData().getProfession() == VillagerProfession.NONE && villager.hasCustomer())
                resetCustomer();

            ci.cancel();
        }
    }

    @Unique
    private boolean isInTradingCell(VillagerEntity villager) {
        int[] cooldownList = getCooldownList();
        int distanceOrdinal = Lazy_ai_pixelindiedev.getDistance(villager).ordinal();

        if ((villager.age + randomSelectedTick) % cooldownList[distanceOrdinal] != 0) return isInTradingHall;

        final BlockPos center = villager.getBlockPos();
        //if block was changed near, or villager is no longer standing in the same spot
        if (shouldRefreshTradingHall || lastStandingLocation != center) {
            shouldRefreshTradingHall = false;
            lastStandingLocation = center;

            final World world = villager.getEntityWorld();

            int fullyBlockedDirections = 0;
            int halfBlockedDirections = 0;
            for (Direction direction : directionsDirections) {
                reusableSide.set(center, direction);
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
    private VillagerProfession getCachedProfession() {
        VillagerProfession current = villager.getVillagerData().getProfession();
        if (current != cachedProfessionEntry) {
            cachedProfessionEntry = current;
        }
        return cachedProfessionEntry;
    }

    @Unique
    private boolean getCachedSolidBlock(World world, BlockPos pos) {
        long key = pos.asLong();
        if (cachedBlockPos.containsKey(key)) return cachedBlockPos.get(key);
        else {
            BlockState state = world.getBlockState(pos);
            boolean isSolid = hasSolidCollision(state);
            cachedBlockPos.put(key, isSolid);
            return isSolid;
        }
    }

    @Unique
    private int[] getCooldownList() {
        return switch (getOptimalizationType()) {
            case Minimal -> cooldownsMinimal;
            case Agressive -> cooldownsAgressive;
            default -> cooldowns;
        };
    }

    @Unique
    private boolean isPanicking() {
        return villager.getBrain().hasActivity(Activity.PANIC);
    }
}