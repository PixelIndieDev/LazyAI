package com.pixelindiedev.lazy_ai_pixelindiedev.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.pixelindiedev.lazy_ai_pixelindiedev.Lazy_ai_pixelindiedev;
import net.minecraft.entity.ai.goal.TemptGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = TemptGoal.class, priority = 600)
public class TemptGoalmixin {
    @Unique
    private final static float[] cooldowns = {0.001f, 0.15f, 0.5f, 1.0f};

    @ModifyExpressionValue(method = "stop", at = @At(value = "CONSTANT", args = "intValue=100"))
    private int replaceInt(int original) {
        return (int) (original * cooldowns[Lazy_ai_pixelindiedev.getTemptGoal()]);
    }
}
