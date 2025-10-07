package com.pixelindiedev.lazy_ai_pixelindiedev.mixin;

import com.pixelindiedev.lazy_ai_pixelindiedev.Lazy_ai_pixelindiedev;
import net.minecraft.entity.ai.goal.TemptGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(value = TemptGoal.class, priority = 1005)
public class TemptGoalmixin {
    private final static int[] cooldowns = {0, 15, 50, 100};

    @ModifyConstant(method = "stop", constant = @Constant(intValue = 100))
    private int replaceInt(int constant) {
        return cooldowns[Lazy_ai_pixelindiedev.getTemptGoal()];
    }
}
