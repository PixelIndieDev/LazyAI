package com.pixelindiedev.lazy_ai_pixelindiedev.helpers;

import com.pixelindiedev.lazy_ai_pixelindiedev.Lazy_ai_pixelindiedev;
import com.pixelindiedev.lazy_ai_pixelindiedev.enums.DistanceType;
import com.pixelindiedev.lazy_ai_pixelindiedev.enums.OptimalizationType;
import net.minecraft.world.entity.Mob;

import static java.lang.Math.clamp;

public class CooldownHelper {
    private final int[] cooldowns;
    private final int[] cooldownsAgressive;
    private final int[] cooldownsMinimal;

    private OptimalizationType cachedOptiType;
    private int[] cachedCooldownList;

    private int cooldown = 0;
    private DistanceType previousDistanceType = DistanceType.FarRange;

    public CooldownHelper(int[] cooldowns, int[] cooldownsAgressive, int[] cooldownsMinimal) {
        this.cooldowns = cooldowns;
        this.cooldownsAgressive = cooldownsAgressive;
        this.cooldownsMinimal = cooldownsMinimal;
    }

    public boolean shouldThrottle(Mob mob) {
        final DistanceType newDistanceType = Lazy_ai_pixelindiedev.getDistance(mob);
        final int[] cooldownList = getCooldownList();

        if (newDistanceType != previousDistanceType) {
            final int target = cooldownList[newDistanceType.ordinal()];
            final int prevTarget = cooldownList[previousDistanceType.ordinal()];
            cooldown = clamp(target - (prevTarget - cooldown), 0, target);
            previousDistanceType = newDistanceType;
            if (cooldown > 0) {
                cooldown--;
                return true;
            }
            cooldown = target;
            return false;
        }

        if (cooldown > 0) {
            cooldown--;
            return true;
        }

        cooldown = cooldownList[newDistanceType.ordinal()];
        return false;
    }

    public int[] getCurrentCooldownList() {
        return getCooldownList();
    }

    private int[] getCooldownList() {
        final OptimalizationType current = Lazy_ai_pixelindiedev.getOptimalizationType();
        if (current != cachedOptiType) {
            cachedOptiType = current;
            cachedCooldownList = switch (current) {
                case Minimal -> cooldownsMinimal;
                case Agressive -> cooldownsAgressive;
                case null, default -> cooldowns;
            };
        }
        return cachedCooldownList;
    }
}
