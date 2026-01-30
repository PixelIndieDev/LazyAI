package com.pixelindiedev.lazy_ai_pixelindiedev.mixin;

import com.pixelindiedev.lazy_ai_pixelindiedev.Lazy_ai_pixelindiedev;
import com.pixelindiedev.lazy_ai_pixelindiedev.mixin.integration.VillagerEntityAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityInteraction;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.VillagerProfession;
import net.minecraft.village.VillagerType;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VillagerEntity.class)
public abstract class VillagerEntityMixin {
    @Unique
    private final static int[] cooldowns = {20, 50, 100};  // Cooldowns from close to far, in ticks
    @Unique
    private final static int[] cooldownsAgressive = {40, 75, 200};
    @Unique
    private final static int[] cooldownsMinimal = {10, 30, 60};

    @Unique
    private VillagerEntity villager;
    @Unique
    private boolean isInTradingHall;
    @Unique
    private int randomSelectedTick;

    @Shadow
    protected abstract void resetCustomer();

    @Inject(method = "<init>(Lnet/minecraft/entity/EntityType;Lnet/minecraft/world/World;Lnet/minecraft/village/VillagerType;)V", at = @At("RETURN"))
    private void captureMob(EntityType entityType, World world, VillagerType type, CallbackInfo ci) {
        villager = (VillagerEntity) (Object) this;
        isInTradingHall = false;
        randomSelectedTick = villager.getId();
    }

    @Inject(method = "mobTick", at = @At("HEAD"), cancellable = true)
    private void skipIdleTradingHallTick(CallbackInfo ci) {
        if (villager != null) {
            if (!villager.isAlive() || villager.isBaby() || isPanicking()) {
                return;
            }

            if (isInTradingCell(villager)) {

                VillagerProfession villagerprof = villager.getVillagerData().getProfession();
                if (villagerprof == VillagerProfession.NONE || villagerprof == VillagerProfession.NITWIT) {
                    return;
                }

                if (villager.hasCustomer()) {
                    return;
                }

                if (!villager.getNavigation().isIdle()) {
                    return;
                }

                if ((villager.age + randomSelectedTick) % 20 != 0) {
                    VillagerEntityAccessor accessor = (VillagerEntityAccessor) villager;

                    int tempInt = accessor.getLevelUpTimer();
                    if (!villager.hasCustomer() && tempInt > 0) {
                        accessor.setLevelUpTimer(tempInt - 1);

                        if (accessor.getLevelUpTimer() <= 0) {
                            if (accessor.isLevelingUp()) accessor.invokeLevelUp();
                            villager.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 200, 0));
                        }
                    }

                    World world = villager.getEntityWorld();
                    PlayerEntity lastcust = accessor.getLastCustomer();
                    if (lastcust != null && world instanceof ServerWorld) {
                        ((ServerWorld) world).handleInteraction(EntityInteraction.TRADE, lastcust, villager);
                        world.sendEntityStatus(villager, (byte) 14);
                        accessor.setLastCustomer(null);
                    }

                    if (villager.getVillagerData().getProfession() == VillagerProfession.NONE && villager.hasCustomer()) {
                        resetCustomer();
                    }

                    ci.cancel();
                }
            }
        }
    }

    @Unique
    private boolean isInTradingCell(VillagerEntity villager) {
        if ((villager.age + randomSelectedTick) % getCooldownList()[Lazy_ai_pixelindiedev.getDistance(villager).ordinal()] != 0) {
            return isInTradingHall;
        } else {
            final BlockPos center = villager.getBlockPos();
            final World world = villager.getEntityWorld();

            int blockedDirections = 0;

            if (isDirectionBlocked(world, center.north())) blockedDirections++;
            if (isDirectionBlocked(world, center.east())) blockedDirections++;
            if (isDirectionBlocked(world, center.south())) blockedDirections++;
            if (isDirectionBlocked(world, center.west())) blockedDirections++;

            isInTradingHall = blockedDirections >= 3;
            return isInTradingHall;
        }
    }

    @Unique
    private boolean isDirectionBlocked(World world, BlockPos pos) {
        return isSolidBlock(world, pos) && isSolidBlock(world, pos.up());
    }

    @Unique
    private boolean isSolidBlock(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        return !state.isAir() && state.isSolidBlock(world, pos);
    }

    @Unique
    private int[] getCooldownList() {
        return switch (Lazy_ai_pixelindiedev.getOptimalizationType()) {
            case Minimal -> cooldownsMinimal;
            case Agressive -> cooldownsAgressive;
            default -> cooldowns;
        };
    }

    @Unique
    public boolean isPanicking() {
        return villager.getBrain().hasActivity(Activity.PANIC);
    }
}