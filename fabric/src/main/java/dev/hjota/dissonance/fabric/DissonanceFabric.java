package dev.hjota.dissonance.fabric;

import net.fabricmc.api.ModInitializer;

import dev.hjota.dissonance.Dissonance;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

public final class DissonanceFabric implements ModInitializer {
    public static final String MOD_ID = "dissonance";

    @Override
    public void onInitialize() {
        Dissonance.LOGGER.info("-- Dissonance v" + getModInfoVersion() + " --");
    }

    private String getModInfoVersion() {
        ModContainer modContainer = FabricLoader.getInstance().getModContainer(MOD_ID).get();
        return modContainer.getMetadata().getVersion().getFriendlyString();
    }
}
