package dev.hjota.dissonance.client;

import dev.hjota.dissonance.Dissonance;
import dev.hjota.dissonance.client.compat.xaero.XaeroCompat;

public class DissonanceClient {
    public static XaeroCompat xaeros = null;

    public static void init() {
        try {
            Class.forName("earth.terrarium.cadmus.Cadmus");
        } catch (ClassNotFoundException ignored) {
            Dissonance.LOGGER.error("Cadmus not found!");
        }
        try {
            Class.forName("xaero.map.WorldMap");
            xaeros = new XaeroCompat();
        } catch (ClassNotFoundException ignored) {
            Dissonance.LOGGER.error("Xaero's WorldMap not found!");
        }
    }
}