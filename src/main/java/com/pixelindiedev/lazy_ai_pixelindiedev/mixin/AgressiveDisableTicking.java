package com.pixelindiedev.lazy_ai_pixelindiedev.mixin;

import com.pixelindiedev.lazy_ai_pixelindiedev.Lazy_ai_pixelindiedev;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.pixelindiedev.lazy_ai_pixelindiedev.Lazy_ai_pixelindiedev.GetMobEntity;

@Mixin(value = MobEntity.class, priority = 1010)
public abstract class AgressiveDisableTicking extends LivingEntity {
    private final static int[] cooldowns = {1, 1, 2};  // Cooldowns from close to far, in ticks
    private final static int[] cooldownsAgressive = {1, 1, 3};
    private final static int[] cooldownsMinimal = {1, 1, 1};
    @Unique
    private int aiTickOffset;
    @Unique
    private MobEntity mob;

    protected AgressiveDisableTicking(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void assignOffset(EntityType<?> type, World world, CallbackInfo ci) {
        this.aiTickOffset = this.getId() % getCooldownList()[2];
        this.mob = GetMobEntity(this);
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void ThrottleWholeAI(CallbackInfo ci) {
        if (Lazy_ai_pixelindiedev.getNeverSlowdownDistantMobs()) return;

        if (mob == null) return;

        if ((this.age + aiTickOffset) % getCooldownList()[Lazy_ai_pixelindiedev.getDistance(mob).ordinal()] != 0)
            ci.cancel();
    }

    private int[] getCooldownList() {
        return switch (Lazy_ai_pixelindiedev.getOptimalizationType()) {
            case Minimal -> cooldownsMinimal;
            case Agressive -> cooldownsAgressive;
            default -> cooldowns;
        };
    }
}
