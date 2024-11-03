package dev.hjota.dissonance.client;

import com.mojang.datafixers.util.Pair;

import dev.hjota.dissonance.claims.ClaimType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public final class ClientClaims {
    public static String ID = null;
    public static final Map<ResourceKey<Level>, ClientClaims> CLAIMS = new HashMap<>();
    private final Map<ChunkPos, ClientClaims.Entry> claims = new HashMap<>();
    private final Map<String, Consumer<Map<ChunkPos, ClientClaims.Entry>>> listeners = new HashMap<>();

    public ClientClaims(ResourceKey<Level> dimension) {
    }

    public void addListener(String id, Consumer<Map<ChunkPos, ClientClaims.Entry>> listener) {
        if (this.listeners.isEmpty()) {
            this.claims.clear();
        }

        this.listeners.put(id, listener);
    }

    public void removeListener(String id) {
        this.listeners.remove(id);
        if (this.listeners.isEmpty()) {
            this.claims.clear();
        }
    }

    public void update(String id, Component displayName, int color, Map<ChunkPos, Pair<Boolean, ClaimType>> claims,
            String teamId) {
        ID = id;
        claims.forEach((pos, pair) -> {
            if (pair.getFirst()) {
                Entry entry = new Entry(displayName, color, pair.getSecond(), teamId);
                this.claims.put(pos, entry);
            } else {
                this.claims.remove(pos);
            }
        });
        this.listeners.values().forEach((l) -> l.accept(this.claims));
    }

    public static ClientClaims get(ResourceKey<Level> dimension) {
        return CLAIMS.computeIfAbsent(dimension, ClientClaims::new);
    }

    public record Entry(Component name, int color, ClaimType type, String teamId) {
        public Component name() {
            return this.name;
        }

        public int color() {
            return this.color;
        }

        public ClaimType type() {
            return this.type;
        }

        public String teamId() {
            return teamId;
        }
    }
}
