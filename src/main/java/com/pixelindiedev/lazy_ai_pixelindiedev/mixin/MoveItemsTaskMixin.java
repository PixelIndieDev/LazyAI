package com.pixelindiedev.lazy_ai_pixelindiedev.mixin;

import com.pixelindiedev.lazy_ai_pixelindiedev.LazyAi$ChestCacheManager;
import com.pixelindiedev.lazy_ai_pixelindiedev.mixin.integration.MoveItemsTaskAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.MoveItemsTask;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

@Mixin(value = MoveItemsTask.class, priority = 1002)
public class MoveItemsTaskMixin {
    @Mutable
    @Final
    @Shadow
    private int horizontalRange;
    @Mutable
    @Final
    @Shadow
    private int verticalRange;
    @Final
    @Shadow
    private Predicate<BlockState> inputContainerPredicate;
    @Final
    @Shadow
    private Predicate<net.minecraft.block.BlockState> outputContainerPredicate;
    @Unique
    private BlockPos lastUsedChest = null;

    @Unique
    private Item lastCarriedItem = null;

    @Inject(method = "findStorage", at = @At("HEAD"), cancellable = true)
    private void injectCachedStorage(ServerWorld world, PathAwareEntity entity, CallbackInfoReturnable<Optional<MoveItemsTask.Storage>> cir) {
        ItemStack held = entity.getMainHandStack();
        if (held.isEmpty()) {
            cir.setReturnValue(findNearestValidStorage(world, entity));
            return;
        }

        Item currentItem = held.getItem();

        if (lastUsedChest != null && currentItem == lastCarriedItem) {
            BlockEntity be = world.getBlockEntity(lastUsedChest);
            if (be instanceof Inventory inv) {
                if (!isInventoryFull(inv, held) && containsMatchingItem(inv, currentItem)) {
                    MoveItemsTask.Storage storage = MoveItemsTask.Storage.forContainer(be, world);
                    if (storage != null) {
                        cir.setReturnValue(Optional.of(storage));
                        return;
                    }
                }
            }
            lastUsedChest = null;
        }

        Optional<MoveItemsTask.Storage> result = findNearestValidStorage(world, entity);
        cir.setReturnValue(result);
    }

    @Unique
    private boolean isInventoryFull(Inventory inv, ItemStack stack) {
        if (stack == null || stack.isEmpty()) return true;

        for (int i = 0; i < inv.size(); i++) {
            ItemStack slot = inv.getStack(i);
            if (slot.isEmpty()) return false;
            if (ItemStack.areItemsAndComponentsEqual(slot, stack) && slot.getCount() < slot.getMaxCount()) return false;
        }
        return true;
    }

    @Unique
    private boolean containsMatchingItem(Inventory inv, Item stack) {
        for (int i = 0; i < inv.size(); i++) {
            ItemStack slot = inv.getStack(i);
            if (!slot.isEmpty() && ItemStack.areItemsAndComponentsEqual(slot, stack.getDefaultStack())) return true;
        }
        return false;
    }

    @Inject(method = "placeStack", at = @At("HEAD"))
    private void beforePlaceStack(PathAwareEntity entity, Inventory inventory, CallbackInfo ci) {
        this.lastCarriedItem = inventory.getStack(0).getItem();
    }

    @Inject(method = "placeStack", at = @At("RETURN"))
    private void afterPlaceStack(PathAwareEntity entity, Inventory inventory, CallbackInfo ci) {
        if (entity.getMainHandStack().isEmpty())
            if (inventory instanceof BlockEntity blockEntity) this.lastUsedChest = blockEntity.getPos();
    }

    @Inject(method = "markUnreachable", at = @At("HEAD"))
    private void onMarkUnreachable(PathAwareEntity entity, World world, BlockPos pos, CallbackInfo ci) {
        if (lastUsedChest != null && lastUsedChest.equals(pos)) lastUsedChest = null;
    }

    @Unique
    private Optional<MoveItemsTask.Storage> findNearestValidStorage(ServerWorld world, PathAwareEntity entity) {
        BlockPos entityPos = entity.getBlockPos();
        List<BlockPos> nearbyChests = LazyAi$ChestCacheManager.getCachedStorage(world, entityPos, horizontalRange, verticalRange);
        if (nearbyChests.isEmpty()) {
            return Optional.empty();
        }

        double nearestDistSq = Double.MAX_VALUE;
        MoveItemsTask.Storage nearestStorage = null;

        for (BlockPos pos : nearbyChests) {
            double distSq = entityPos.getSquaredDistance(pos);

            if (distSq < nearestDistSq) {
                BlockEntity be = world.getBlockEntity(pos);
                if (be instanceof Inventory) {
                    MoveItemsTask.Storage storage = MoveItemsTask.Storage.forContainer(be, world);
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
    private boolean testContainerFast(ServerWorld world, PathAwareEntity entity, MoveItemsTask.Storage s) {
        GlobalPos currentPos = GlobalPos.create(world.getRegistryKey(), s.pos());
        Set<GlobalPos> visited = MoveItemsTaskAccessor.invokeGetVisitedPositions(entity);
        if (visited.contains(currentPos)) return false;

        Set<GlobalPos> unreachable = MoveItemsTaskAccessor.invokeGetUnreachablePositions(entity);
        if (unreachable.contains(currentPos)) return false;

        if (ChestBlock.isChestBlocked(world, s.pos())) return false;

        BlockEntity be = s.blockEntity();
        if (be instanceof LockableContainerBlockEntity l && l.isLocked()) return false;

        BlockState state = s.state();
        boolean canPickUp = MoveItemsTaskAccessor.invokeCanPickUpItem(entity);
        return canPickUp ? inputContainerPredicate.test(state) : outputContainerPredicate.test(state);
    }

    @Inject(method = "markVisited", at = @At("HEAD"), cancellable = true)
    private void skipUnchangedVisited(PathAwareEntity entity, World world, BlockPos pos, CallbackInfo ci) {
        Set<?> visited = entity.getBrain().getOptionalRegisteredMemory(MemoryModuleType.VISITED_BLOCK_POSITIONS).orElse(Set.of());
        if (visited.contains(pos)) ci.cancel();
    }

    @Inject(method = "resetNavigation", at = @At("HEAD"), cancellable = true)
    private void reset(PathAwareEntity entity, CallbackInfo ci) {
        if (entity.getNavigation().isIdle()) ci.cancel();
    }
}
