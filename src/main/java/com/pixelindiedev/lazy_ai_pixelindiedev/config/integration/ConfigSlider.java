package com.pixelindiedev.lazy_ai_pixelindiedev.config.integration;

import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

public abstract class ConfigSlider extends SliderWidget {
    public ConfigSlider(int x, int y, int width, int height, Text text, double value) {
        super(x, y, width, height, text, value);
    }

    public void setSliderValue(double newValue) {
        this.value = newValue;
        this.updateMessage();
    }
}
