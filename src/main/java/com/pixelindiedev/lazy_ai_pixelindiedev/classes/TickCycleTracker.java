package com.pixelindiedev.lazy_ai_pixelindiedev.classes;

import static com.pixelindiedev.lazy_ai_pixelindiedev.Lazy_ai_pixelindiedev.DynamicMode_MinTickCycles;

public class TickCycleTracker {
    private int counter = 0;

    public static <T> T applyTickCycleTrackerStuff(T currentValue, T computedValue, TickCycleTracker tracker) {
        if (computedValue.equals(currentValue)) {
            tracker.counter = 0;
            return currentValue;
        }
        tracker.counter++;
        if (tracker.counter >= DynamicMode_MinTickCycles) {
            tracker.counter = 0;
            return computedValue;
        }
        return currentValue;
    }
}
