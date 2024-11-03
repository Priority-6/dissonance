package dev.hjota.dissonance.client.compat.xaero;

import dev.ftb.mods.ftbteams.api.event.ClientTeamPropertiesChangedEvent;
import dev.ftb.mods.ftbteams.api.event.TeamEvent;
import dev.ftb.mods.ftbteams.data.ClientTeamManagerImpl;
import dev.hjota.dissonance.client.ClientClaims;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import xaero.map.MapProcessor;
import xaero.map.WorldMapSession;
import xaero.map.region.MapRegion;
import xaero.map.world.MapDimension;

import java.util.HashMap;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class XaeroCompat {
    private static final String LISTENER_ID = "xaeros";
    public static final XaeroClaimsManager manager = new XaeroClaimsManager();

    public XaeroCompat() {
        TeamEvent.CLIENT_PROPERTIES_CHANGED.register(this::onTeamPropsChanged);
    }

    private void onTeamPropsChanged(ClientTeamPropertiesChangedEvent event) {
        // Get team info once upfront to avoid multiple lookups
        final var teamId = event.getTeam().getId();
        final var optionalTeam = ClientTeamManagerImpl.getInstance().getTeam(teamId);

        if (optionalTeam.isEmpty())
            return;

        // Cache team data to avoid repeated access
        final var team = optionalTeam.get();
        final var teamIdString = teamId.toString();
        final var teamName = team.getName();
        final var teamColor = team.getColor();

        // Create new Entry once instead of for each matching claim
        manager.map.forEach((dimension, dimensionClaims) -> {
            boolean updated = false;
            for (var entry : dimensionClaims.entrySet()) {
                if (entry.getValue().teamId().equals(teamIdString)) {
                    entry.setValue(new ClientClaims.Entry(teamName, teamColor, entry.getValue().type(), teamIdString));
                    updated = true;
                }
            }
            // Only put back if we actually made changes
            if (updated)
                manager.put(dimension, dimensionClaims);
        });
    }

    public static void registerListener(ResourceKey<Level> dimension) {
        ClientClaims.get(dimension).addListener(LISTENER_ID, claims -> update(dimension, claims));
    }

    public static void removeListener(ResourceKey<Level> dimension) {
        ClientClaims.get(dimension).removeListener(LISTENER_ID);
        manager.clear(dimension);
        MapDimension mapDim = getMapDimension(dimension);
        if (mapDim != null) {
            mapDim.getHighlightHandler().clearCachedHashes();
        }
    }

    public static void update(ResourceKey<Level> dimension, Map<ChunkPos, ClientClaims.Entry> claims) {
        if (!Minecraft.getInstance().isSameThread())
            return;
        manager.put(dimension, claims);
        MapProcessor mapProc = getMapProcessor();
        MapDimension mapDim = getMapDimension(dimension);
        if (mapDim != null) {
            int caveLayer = mapProc.getCurrentCaveLayer();
            Map<Integer, Integer> regions = getRegions(claims);
            for (var region : regions.entrySet()) {
                for (int regionOffsetX = -1; regionOffsetX < 2; ++regionOffsetX) {
                    for (int regionOffsetZ = -1; regionOffsetZ < 2; ++regionOffsetZ) {
                        if (regionOffsetX == 0 && regionOffsetZ == 0
                                || regionOffsetX * regionOffsetX != regionOffsetZ * regionOffsetZ) {
                            mapDim.getHighlightHandler().clearCachedHash(region.getKey() + regionOffsetX,
                                    region.getValue() + regionOffsetZ);
                            MapRegion mapRegion = mapDim.getLayeredMapRegions().getLeaf(caveLayer,
                                    region.getKey() + regionOffsetX, region.getValue() + regionOffsetZ);
                            if (mapRegion != null) {
                                synchronized (mapRegion) {
                                    if (mapRegion.canRequestReload_unsynced()) {
                                        if (mapRegion.getLoadState() == 2) {
                                            mapRegion.requestRefresh(mapProc);
                                        } else {
                                            mapProc.getMapSaveLoad().requestLoad(mapRegion, "Gui");
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static MapProcessor getMapProcessor() {
        WorldMapSession session = WorldMapSession.getCurrentSession();
        return session.getMapProcessor();
    }

    private static MapDimension getMapDimension(ResourceKey<Level> dimension) {
        return getMapProcessor().getMapWorld().getDimension(dimension);
    }

    @NotNull
    private static Map<Integer, Integer> getRegions(Map<ChunkPos, ClientClaims.Entry> claims) {
        Map<Integer, Integer> regions = new HashMap<>();
        for (var chunk : claims.keySet()) {
            int regionX = chunk.getRegionX();
            int regionZ = chunk.getRegionZ();
            if (regions.containsKey(regionX) && regions.get(regionX).equals(regionZ)) {
                continue;
            } else {
                regions.put(regionX, regionZ);
            }
        }
        return regions;
    }

    public static XaeroHighlighter registerHighlighter() {
        return new XaeroHighlighter(manager);
    }
}
