package com.pixelindiedev.lazy_ai_pixelindiedev.mixin;

import com.pixelindiedev.lazy_ai_pixelindiedev.Lazy_ai_pixelindiedev;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.entity.mob.GhastEntity$ShootFireballGoal", priority = 1002)
public class GhastShootFireballGoalMixin {

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void throttleFireballTick(CallbackInfo ci) {
        int cooldown = 5; //orignal value divided by the cooldown
        if ((Lazy_ai_pixelindiedev.getServerTick() % cooldown) != 0) ci.cancel();
    }

    @ModifyConstant(method = "tick", constant = @Constant(intValue = 10))
    private int adjustSightDelay2(int original) {
        return 2;
    }

    @ModifyConstant(method = "tick", constant = @Constant(intValue = 20))
    private int adjustSightDelay1(int original) {
        return 4;
    }

    @ModifyConstant(method = "tick", constant = @Constant(intValue = -40))
    private int adjustSightDelay(int original) {
        return -8;
    }
}
