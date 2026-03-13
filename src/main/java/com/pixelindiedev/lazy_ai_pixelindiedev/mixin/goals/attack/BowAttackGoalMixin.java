package com.pixelindiedev.lazy_ai_pixelindiedev.mixin.goals.attack;

import com.pixelindiedev.lazy_ai_pixelindiedev.interfaces.TickCancellingAware;
import com.pixelindiedev.lazy_ai_pixelindiedev.mixin.integration.LivingEntityAccessor;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.ai.goal.BowAttackGoal;
import net.minecraft.entity.mob.HostileEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BowAttackGoal.class, priority = 1001)
public class BowAttackGoalMixin<T extends HostileEntity & RangedAttackMob> {
    @Final
    @Shadow
    private T actor;
    @Shadow
    private int cooldown;

    @Inject(method = "tick", at = @At("HEAD"))
    private void compensateCooldown(CallbackInfo ci) {
        final int skipped = ((TickCancellingAware) actor).lazy_ai$getSkippedTicks();
        if (skipped <= 0) return;

        if (actor.isUsingItem()) {
            final LivingEntityAccessor accessor = (LivingEntityAccessor) actor;
            accessor.setItemUseTimeLeftAccessor(Math.max(0, accessor.getItemUseTimeLeftAccessor() - skipped));
        } else if (cooldown > 0) cooldown = Math.max(0, cooldown - skipped);
    }
}
