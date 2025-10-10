package com.pixelindiedev.lazy_ai_pixelindiedev.mixin;

import com.pixelindiedev.lazy_ai_pixelindiedev.mixin.integration.MoveItemsTaskAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.MoveItemsTask;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Mixin(value = MoveItemsTask.class, priority = 1002)
public class MoveItemsTaskMixin {
    @Unique
    private static int horizontalRange;
    @Unique
    private static int verticalRange;
    @Unique
    private WeakHashMap<BlockPos, Boolean> cachedStorages;
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

    @Unique
    private WeakHashMap<BlockPos, Boolean> getCache() {
        if (cachedStorages == null) {
            cachedStorages = new WeakHashMap<>();
        }
        return cachedStorages;
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void captureMob(float speed, Predicate inputContainerPredicate, Predicate outputChestPredicate, int horizontalRange, int verticalRange, Map interactionCallbacks, Consumer travellingCallback, Predicate storagePredicate, CallbackInfo ci) {
        MoveItemsTaskMixin.horizontalRange = horizontalRange;
        MoveItemsTaskMixin.verticalRange = verticalRange;
    }

    @Inject(method = "findStorage", at = @At("HEAD"), cancellable = true)
    private void injectCachedStorage(ServerWorld world, PathAwareEntity entity, CallbackInfoReturnable<Optional<MoveItemsTask.Storage>> cir) {
        getCache().entrySet().removeIf(e -> {
            BlockEntity be = world.getBlockEntity(e.getKey());
            return be == null || be.isRemoved() || !(be instanceof Inventory);
        });

        ItemStack held = entity.getMainHandStack();
        Item currentItem = held.isEmpty() ? null : held.getItem();

        if (lastUsedChest != null && currentItem != null && currentItem == lastCarriedItem) {
            BlockEntity be = world.getBlockEntity(lastUsedChest);
            if (be instanceof Inventory inv) {
                boolean stillContains = containsMatchingItem(inv, lastCarriedItem);
                boolean full = isInventoryFull(inv, held);

                if (stillContains && !full) {
                    MoveItemsTask.Storage storage = MoveItemsTask.Storage.forContainer(be, world);
                    if (storage != null) {
                        cir.setReturnValue(Optional.of(storage));
                        return;
                    }
                } else {
                    lastUsedChest = null;
                }
            } else {
                lastUsedChest = null;
            }
        }

        Optional<MoveItemsTask.Storage> result = findValidCachedStorage(world, entity);

        if (result.isEmpty()) {
            rebuildCache(world, entity);
            result = findValidCachedStorage(world, entity);
        }

        result.ifPresent(storage -> {
            getCache().put(storage.pos(), true);
            cir.setReturnValue(Optional.of(storage));
        });
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
            if (!slot.isEmpty() && ItemStack.areItemsAndComponentsEqual(slot, stack.getDefaultStack())) {
                return true;
            }
        }
        return false;
    }

    @Inject(method = "placeStack", at = @At("HEAD"))
    private void beforePlaceStack(PathAwareEntity entity, Inventory inventory, CallbackInfo ci) {
        this.lastCarriedItem = inventory.getStack(0).getItem();
    }

    @Inject(method = "placeStack", at = @At("RETURN"))
    private void afterPlaceStack(PathAwareEntity entity, Inventory inventory, CallbackInfo ci) {
        if (entity.getMainHandStack().isEmpty()) {
            if (inventory instanceof BlockEntity blockEntity) {
                this.lastUsedChest = blockEntity.getPos();
            }
        }
    }

    @Inject(method = "markUnreachable", at = @At("HEAD"))
    private void onMarkUnreachable(PathAwareEntity entity, World world, BlockPos pos, CallbackInfo ci) {
        cachedStorages.remove(pos);

        if (lastUsedChest != null && lastUsedChest.equals(pos)) {
            lastUsedChest = null;
        }
    }

    @Unique
    private Optional<MoveItemsTask.Storage> findValidCachedStorage(ServerWorld world, PathAwareEntity entity) {
        return getCache().entrySet().stream()
                .filter(e -> !e.getValue())
                .map(Map.Entry::getKey)
                .map(pos -> {
                    BlockEntity be = world.getBlockEntity(pos);
                    return be instanceof Inventory ? MoveItemsTask.Storage.forContainer(be, world) : null;
                })
                .filter(Objects::nonNull)
                .filter(storage -> testContainerFast(world, entity, storage))
                .findFirst();
    }

    @Unique
    private boolean testContainerFast(ServerWorld world, PathAwareEntity entity, MoveItemsTask.Storage s) {
        Set<GlobalPos> visited = MoveItemsTaskAccessor.invokeGetVisitedPositions(entity);
        Set<GlobalPos> unreachable = MoveItemsTaskAccessor.invokeGetUnreachablePositions(entity);
        GlobalPos currentPos = GlobalPos.create(world.getRegistryKey(), s.pos());

        if (visited.contains(currentPos) || unreachable.contains(currentPos)) return false;
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

    @Unique
    private void rebuildCache(ServerWorld world, PathAwareEntity entity) {
        getCache().clear();
        BlockPos origin = entity.getBlockPos();
        Box area = new Box(origin).expand(horizontalRange, verticalRange, horizontalRange);

        int minChunkX = Math.floorDiv((int) area.minX, 16);
        int maxChunkX = Math.floorDiv((int) area.maxX, 16);
        int minChunkZ = Math.floorDiv((int) area.minZ, 16);
        int maxChunkZ = Math.floorDiv((int) area.maxZ, 16);

        for (int cx = minChunkX; cx <= maxChunkX; cx++) {
            for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                WorldChunk chunk = world.getChunkManager().getWorldChunk(cx, cz);
                if (chunk == null) continue;

                for (BlockEntity be : chunk.getBlockEntities().values()) {
                    if (!(be instanceof ChestBlockEntity chest)) continue;
                    BlockPos pos = chest.getPos();

                    if (area.contains(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)) {
                        getCache().put(pos, false);
                    }
                }
            }
        }
    }

    @Inject(method = "resetVisitedPositions", at = @At("RETURN"))
    private void restVisited(PathAwareEntity entity, CallbackInfo ci) {
        getCache().replaceAll((key, val) -> false);
    }
}
