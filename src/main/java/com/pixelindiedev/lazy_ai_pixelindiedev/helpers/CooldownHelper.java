package com.pixelindiedev.lazy_ai_pixelindiedev.helpers;

import com.pixelindiedev.lazy_ai_pixelindiedev.Lazy_ai_pixelindiedev;
import com.pixelindiedev.lazy_ai_pixelindiedev.enums.DistanceType;
import com.pixelindiedev.lazy_ai_pixelindiedev.enums.OptimalizationType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;

import static com.pixelindiedev.lazy_ai_pixelindiedev.Lazy_ai_pixelindiedev.Server_TPS_NoNeed_Multiplier;
import static java.lang.Math.clamp;

public class CooldownHelper {
    private final int[] cooldowns;
    private final int[] cooldownsAgressive;
    private final int[] cooldownsMinimal;
    private final int[] cooldownsNoNeed;

    private final boolean DontUseNoNeed;

    private OptimalizationType cachedOptiType;
    private int[] cachedCooldownList;

    private int cooldown = 0;
    private DistanceType previousDistanceType = DistanceType.FarRange;

    public CooldownHelper(int[] cooldowns, int[] cooldownsAgressive, int[] cooldownsMinimal) {
        this(cooldowns, cooldownsAgressive, cooldownsMinimal, false);
    }

    public CooldownHelper(int[] cooldowns, int[] cooldownsAgressive, int[] cooldownsMinimal, boolean DontUseNoNeed) {
        this.cooldowns = cooldowns;
        this.cooldownsAgressive = cooldownsAgressive;
        this.cooldownsMinimal = cooldownsMinimal;
        this.DontUseNoNeed = DontUseNoNeed;
        if (!DontUseNoNeed) {
            this.cooldownsNoNeed = new int[cooldownsMinimal.length];
            for (int i = 0; i < cooldownsMinimal.length; i++)
                this.cooldownsNoNeed[i] = Math.max(Mth.floor(cooldownsMinimal[i] * Server_TPS_NoNeed_Multiplier), 1);
        } else cooldownsNoNeed = null;
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
        if (!DontUseNoNeed) {
            final boolean hasNoNeed = Lazy_ai_pixelindiedev.UserHasNoNeed;
            if (hasNoNeed) return cooldownsNoNeed;
        }
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
