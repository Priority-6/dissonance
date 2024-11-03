package dev.hjota.dissonance.claims;

import com.teamresourceful.bytecodecs.base.ByteCodec;

public enum ClaimType {
    CLAIMED,
    CHUNK_LOADED,
    ;

    public static final ByteCodec<earth.terrarium.cadmus.common.claims.ClaimType> CODEC = ByteCodec.ofEnum(earth.terrarium.cadmus.common.claims.ClaimType.class);
}