package dev.hjota.forge;

import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import dev.hjota.dissonance.Dissonance;

@Mod(Dissonance.MOD_ID)
public final class DissonanceForge {
    public DissonanceForge() {
        // Submit our event bus to let Architectury API register our content on the
        // right time.
        EventBuses.registerModEventBus(Dissonance.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());

        // Run our common setup.
        Dissonance.init();
    }
}
