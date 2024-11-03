package dev.hjota.dissonance.mixin.ftbchunks;
import dev.ftb.mods.ftbchunks.client.map.MapDimension;
import dev.ftb.mods.ftbchunks.client.map.UpdateChunkFromServerTask;
import dev.ftb.mods.ftbchunks.net.SendChunkPacket;
import dev.hjota.dissonance.claims.ClaimType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import dev.ftb.mods.ftbteams.data.ClientTeamManagerImpl;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Date;
import java.util.UUID;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ChunkPos;
import dev.hjota.dissonance.client.ClientClaims;
import dev.hjota.dissonance.client.compat.xaero.XaeroCompat;

@Mixin(UpdateChunkFromServerTask.class)
public class UpdateFromServerTaskMixin {

    @Shadow(remap = false) @Final
    private MapDimension dimension;

    @Shadow(remap = false) @Final private SendChunkPacket.SingleChunk chunk;

    @Shadow(remap = false) @Final private UUID teamId;

    @Inject(method = "runMapTask", at = @At("RETURN"), remap = false)
    private void onRunMapTask(CallbackInfo ci) {
        ResourceKey<Level> dimensionKey = dimension.dimension;

        Map<ChunkPos, ClientClaims.Entry> claims = XaeroCompat.manager.map.get(dimensionKey);
        if (claims == null)
            claims = new HashMap<>();

        long now = new Date().getTime();
        ChunkPos pos = new ChunkPos(chunk.getX(), chunk.getZ());
        ClientClaims.Entry claimData = ClientTeamManagerImpl.getInstance().getTeam(teamId)
                .map(team -> new ClientClaims.Entry(team.getName(), team.getColor(), chunk.getDateInfo(true, now).forceLoaded() != null  ? ClaimType.CHUNK_LOADED : ClaimType.CLAIMED, teamId.toString()))
                .orElse(null);

        // TODO: Forceloaded style fix

        if (claimData != null && claimData.teamId() != null && !teamId.equals(new UUID(0, 0))) {
            claims.put(pos, claimData);
        } else {
            claims.remove(pos);
        }

        XaeroCompat.update(dimensionKey, claims);
    }
}
