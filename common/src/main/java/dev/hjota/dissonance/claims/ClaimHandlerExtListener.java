package dev.hjota.dissonance.claims;

import com.mojang.datafixers.util.Pair;
import earth.terrarium.cadmus.common.claims.ClaimType;
import net.minecraft.world.level.ChunkPos;

import java.util.Map;

public interface ClaimHandlerExtListener {
    ClaimListenHandler dissonance$getListenHandler();

    Map<ChunkPos, Pair<String, ClaimType>> dissonance$getClaims();
}
