package com.pixelindiedev.lazy_ai_pixelindiedev.config.integration;


import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return LazyAiConfigScreen::new;
    }
}
