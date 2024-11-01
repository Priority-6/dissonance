package dev.hjota.dissonance.fabric.client;

import dev.hjota.dissonance.client.DissonanceClient;
import net.fabricmc.api.ClientModInitializer;

public final class DissonanceFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // This entrypoint is suitable for setting up client-specific logic, such as
        // rendering.
        DissonanceClient.init();
    }
}
