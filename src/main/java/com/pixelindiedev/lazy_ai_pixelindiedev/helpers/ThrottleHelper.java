package com.pixelindiedev.lazy_ai_pixelindiedev.helpers;

import com.pixelindiedev.lazy_ai_pixelindiedev.enums.CriticalTPSModeEnum;
import com.pixelindiedev.lazy_ai_pixelindiedev.enums.EntityCategoryEnum;
import com.pixelindiedev.lazy_ai_pixelindiedev.records.ThrottleResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;

import static com.pixelindiedev.lazy_ai_pixelindiedev.Lazy_ai_pixelindiedev.CriticalTPSMode;

public class ThrottleHelper {
    public static ThrottleResult ShouldThrottlePushing(EntityCategoryEnum cachedCategory, LivingEntity mob, int waitingForCramming, int aiTickOffset) {
        boolean isWaiting = false;

        int ordinalToCheck;
        switch (cachedCategory) {
            case Pet -> {
                if (mob instanceof TamableAnimal tameable)
                    ordinalToCheck = (tameable.isTame() && tameable.isOrderedToSit()) ? CriticalTPSModeEnum.Low.ordinal() : CriticalTPSModeEnum.Normal.ordinal();
                else ordinalToCheck = CriticalTPSModeEnum.Low.ordinal();
            }
            case Farm -> {
                ordinalToCheck = CriticalTPSModeEnum.Low.ordinal();
                isWaiting = true;
                waitingForCramming++;
            }
            case Ambient -> {
                ordinalToCheck = CriticalTPSModeEnum.Low.ordinal();
            }
            default -> {
                ordinalToCheck = CriticalTPSModeEnum.Severe.ordinal();
            }
        }

        int currentTpsMode = CriticalTPSMode.ordinal();
        if (currentTpsMode > ordinalToCheck) {
            if (!isWaiting) {
                return new ThrottleResult(true, aiTickOffset);
            }
            if (waitingForCramming < (currentTpsMode * 40)) {
                return new ThrottleResult(true, aiTickOffset);
            } else waitingForCramming = aiTickOffset;
        }
        return new ThrottleResult(false, waitingForCramming);
    }
}
