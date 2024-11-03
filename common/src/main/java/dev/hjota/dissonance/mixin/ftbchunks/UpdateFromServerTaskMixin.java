package dev.hjota.dissonance.mixin.ftbchunks;
import dev.ftb.mods.ftbchunks.client.map.MapDimension;
import dev.ftb.mods.ftbchunks.client.map.UpdateChunkFromServerTask;
import dev.ftb.mods.ftbchunks.net.SendChunkPacket;
import dev.hjota.dissonance.Dissonance;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import dev.ftb.mods.ftbteams.data.ClientTeamManagerImpl;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ChunkPos;
import dev.hjota.dissonance.client.ClientClaims;
import earth.terrarium.cadmus.common.claims.ClaimType;
import dev.hjota.dissonance.client.compat.xaero.XaeroCompat;

@Mixin(UpdateChunkFromServerTask.class)
public class UpdateFromServerTaskMixin {

    @Shadow(remap = false) @Final
    private MapDimension dimension;

    @Shadow(remap = false) @Final private SendChunkPacket.SingleChunk chunk;

    @Shadow(remap = false) @Final private UUID teamId;

    @Inject(method = "runMapTask", at = @At("RETURN"), remap = false)
    private void onRunMapTask(CallbackInfo ci) {
        Dissonance.LOGGER.error("FTB Chunks Update Received:");
        Dissonance.LOGGER.error("  Dimension: {}", dimension.dimension);
        Dissonance.LOGGER.error("  Chunk Position: {}, {}", chunk.getX(), chunk.getZ());
        Dissonance.LOGGER.error("  Team ID: {}", teamId); // On delete its "00000000-0000-0000-0000-000000000000"
        
        ResourceKey<Level> dimensionKey = dimension.dimension;


        // Create claims map for XaeroCompa
        Map<ChunkPos, ClientClaims.Entry> claims = XaeroCompat.manager.map.get(dimensionKey);
        if (claims == null)
            claims = new HashMap<>();
        Dissonance.LOGGER.error("Processing claims: {}", claims);

        ChunkPos pos = new ChunkPos(chunk.getX(), chunk.getZ());
        // Debug logs for claim processing


        ClientClaims.Entry claimData = ClientTeamManagerImpl.getInstance().getTeam(teamId)
                .map(team -> new ClientClaims.Entry(team.getName(), team.getColor(), ClaimType.CLAIMED, teamId.toString()))
                .orElse(null);

        // TODO: Forceloaded

        // Only add to claims if there's a valid team, and it's not the zero UUID (which indicates unclaimed)
        if (claimData != null && claimData.teamId() != null && !teamId.equals(new UUID(0, 0))) {
            claims.put(pos, claimData);
        } else {
            claims.remove(pos);
        }
        
        // Update XaeroCompat with the new chunk data
        XaeroCompat.update(dimensionKey, claims);
    }
}
