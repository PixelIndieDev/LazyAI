package com.pixelindiedev.lazy_ai_pixelindiedev.mixin;

// LazyAI
// Copyright (c) 2025 PixelIndieDev
//
// Licensed under the GNU GENERAL PUBLIC LICENSE
// See the LICENSE file in the project root for full license information.

import com.pixelindiedev.lazy_ai_pixelindiedev.LazyAi$ChestCacheManager;
import com.pixelindiedev.lazy_ai_pixelindiedev.mixin.integration.MoveItemsTaskAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.TransportItemsBetweenContainers;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

@Mixin(value = TransportItemsBetweenContainers.class, priority = 1002)
public class MoveItemsTaskMixin {
    @Mutable
    @Final
    @Shadow
    private int horizontalSearchDistance;
    @Mutable
    @Final
    @Shadow
    private int verticalSearchDistance;
    @Final
    @Shadow
    private Predicate<BlockState> sourceBlockType;
    @Final
    @Shadow
    private Predicate<net.minecraft.world.level.block.state.BlockState> destinationBlockType;
    @Unique
    private BlockPos lastUsedChest = null;

    @Unique
    private Item lastCarriedItem = null;

    @Inject(method = "getTransportTarget", at = @At("HEAD"), cancellable = true)
    private void injectCachedStorage(ServerLevel world, PathfinderMob entity, CallbackInfoReturnable<Optional<TransportItemsBetweenContainers.TransportItemTarget>> cir) {
        ItemStack held = entity.getMainHandItem();
        if (held.isEmpty()) {
            cir.setReturnValue(findNearestValidStorage(world, entity));
            return;
        }

        Item currentItem = held.getItem();
        if (lastUsedChest != null && currentItem == lastCarriedItem) {
            BlockEntity be = world.getBlockEntity(lastUsedChest);
            if (be instanceof Container inv) {
                if (!isInventoryFull(inv, held) && containsMatchingItem(inv, currentItem)) {
                    TransportItemsBetweenContainers.TransportItemTarget storage = TransportItemsBetweenContainers.TransportItemTarget.tryCreatePossibleTarget(be, world);
                    if (storage != null) {
                        cir.setReturnValue(Optional.of(storage));
                        return;
                    }
                }
            }
            lastUsedChest = null;
        }

        Optional<TransportItemsBetweenContainers.TransportItemTarget> result = findNearestValidStorage(world, entity);
        cir.setReturnValue(result);
    }

    @Unique
    private boolean isInventoryFull(Container inv, ItemStack stack) {
        if (stack == null || stack.isEmpty()) return true;

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack slot = inv.getItem(i);
            if (slot.isEmpty()) return false;
            if (ItemStack.isSameItemSameComponents(slot, stack) && slot.getCount() < slot.getMaxStackSize())
                return false;
        }
        return true;
    }

    @Unique
    private boolean containsMatchingItem(Container inv, Item stack) {
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack slot = inv.getItem(i);
            if (!slot.isEmpty() && ItemStack.isSameItemSameComponents(slot, stack.getDefaultInstance())) return true;
        }
        return false;
    }

    @Inject(method = "putDownItem", at = @At("HEAD"))
    private void beforePlaceStack(PathfinderMob entity, Container inventory, CallbackInfo ci) {
        this.lastCarriedItem = inventory.getItem(0).getItem();
    }

    @Inject(method = "putDownItem", at = @At("RETURN"))
    private void afterPlaceStack(PathfinderMob entity, Container inventory, CallbackInfo ci) {
        if (entity.getMainHandItem().isEmpty())
            if (inventory instanceof BlockEntity blockEntity) this.lastUsedChest = blockEntity.getBlockPos();
    }

    @Inject(method = "markVisitedBlockPosAsUnreachable", at = @At("HEAD"))
    private void onMarkUnreachable(PathfinderMob entity, Level world, BlockPos pos, CallbackInfo ci) {
        if (lastUsedChest != null && lastUsedChest.equals(pos)) lastUsedChest = null;
    }

    @Unique
    private Optional<TransportItemsBetweenContainers.TransportItemTarget> findNearestValidStorage(ServerLevel world, PathfinderMob entity) {
        BlockPos entityPos = entity.blockPosition();
        List<BlockPos> nearbyChests = LazyAi$ChestCacheManager.getCachedStorage(world, entityPos, horizontalSearchDistance, verticalSearchDistance);
        if (nearbyChests.isEmpty()) return Optional.empty();

        double nearestDistSq = Double.MAX_VALUE;
        TransportItemsBetweenContainers.TransportItemTarget nearestStorage = null;

        for (BlockPos pos : nearbyChests) {
            double distSq = entityPos.distSqr(pos);
            if (distSq < nearestDistSq) {
                BlockEntity be = world.getBlockEntity(pos);
                if (be instanceof Container) {
                    TransportItemsBetweenContainers.TransportItemTarget storage = TransportItemsBetweenContainers.TransportItemTarget.tryCreatePossibleTarget(be, world);
                    if (storage != null && testContainerFast(world, entity, storage)) {
                        nearestDistSq = distSq;
                        nearestStorage = storage;
                    }
                }
            }
        }
        return Optional.ofNullable(nearestStorage);
    }

    @Unique
    private boolean testContainerFast(ServerLevel world, PathfinderMob entity, TransportItemsBetweenContainers.TransportItemTarget s) {
        GlobalPos currentPos = GlobalPos.of(world.dimension(), s.pos());
        Set<GlobalPos> visited = MoveItemsTaskAccessor.invokeGetVisitedPositions(entity);
        if (visited.contains(currentPos)) return false;

        Set<GlobalPos> unreachable = MoveItemsTaskAccessor.invokeGetUnreachablePositions(entity);
        if (unreachable.contains(currentPos)) return false;

        if (ChestBlock.isChestBlockedAt(world, s.pos())) return false;

        BlockEntity be = s.blockEntity();
        if (be instanceof BaseContainerBlockEntity l && l.isLocked()) return false;

        BlockState state = s.state();
        final boolean canPickUp = MoveItemsTaskAccessor.invokeCanPickUpItem(entity);
        return canPickUp ? sourceBlockType.test(state) : destinationBlockType.test(state);
    }

    @Inject(method = "setVisitedBlockPos", at = @At("HEAD"), cancellable = true)
    private void skipUnchangedVisited(PathfinderMob entity, Level world, BlockPos pos, CallbackInfo ci) {
        Set<?> visited = entity.getBrain().getMemory(MemoryModuleType.VISITED_BLOCK_POSITIONS).orElse(Set.of());
        if (visited.contains(pos)) ci.cancel();
    }

    @Inject(method = "stopInPlace", at = @At("HEAD"), cancellable = true)
    private void reset(PathfinderMob entity, CallbackInfo ci) {
        if (entity.getNavigation().isDone()) ci.cancel();
    }
}
