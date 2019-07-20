/*******************************************************************************
 * Copyright 2019 grondag
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package grondag.xm2.block.virtual;

import java.util.IdentityHashMap;
import java.util.Map;

import grondag.exotic_matter.network.S2C_ExcavationRenderUpdate;
import grondag.exotic_matter.network.S2C_PacketExcavationRenderRefresh;
import grondag.exotic_matter.simulator.domain.DomainManager;
import grondag.exotic_matter.simulator.domain.IDomain;
import grondag.fermion.structures.SimpleUnorderedArrayList;
import grondag.fermion.world.WorldMap;
import grondag.hs.simulator.jobs.Job;
import grondag.xm2.Xm;
import grondag.xm2.XmConfig;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.Packet;
import net.minecraft.server.network.ServerPlayerEntity;

@SuppressWarnings("serial")
public class ExcavationRenderTracker extends WorldMap<Int2ObjectOpenHashMap<ExcavationRenderEntry>> {

    public static final ExcavationRenderTracker INSTANCE = new ExcavationRenderTracker();

    private IdentityHashMap<ServerPlayerEntity, PlayerData> playerTracking = new IdentityHashMap<>();

    @Override
    protected Int2ObjectOpenHashMap<ExcavationRenderEntry> load(int dimension) {
        return new Int2ObjectOpenHashMap<ExcavationRenderEntry>();
    }

    public void add(Job job) {
        ExcavationRenderEntry entry = new ExcavationRenderEntry(job);
        if (XmConfig.logExcavationRenderTracking)
            Xm.LOG.info("id = %d new Entry, valid=%s", entry.id, Boolean.toString(entry.isValid()));
        if (entry.isValid()) {
            synchronized (this) {
                this.get(entry.dimensionID).put(entry.id, entry);
            }

            for (Map.Entry<ServerPlayerEntity, PlayerData> playerEntry : playerTracking.entrySet()) {
                PlayerData pd = playerEntry.getValue();
                if (pd.dimensionID == entry.dimensionID && pd.domainID == entry.domainID) {
                    if (XmConfig.logExcavationRenderTracking)
                        Xm.LOG.info("adding listeners for %s", playerEntry.getKey().getDisplayName());
                    entry.addListener(playerEntry.getKey(), true);
                }
            }
        }
    }

    public synchronized void remove(ExcavationRenderEntry entry) {
        if (XmConfig.logExcavationRenderTracking)
            Xm.LOG.info("id = %d removing excavation render entry", entry.id);

        this.get(entry.dimensionID).remove(entry.id);

        Packet<?> packet = S2C_ExcavationRenderUpdate.toPacket(entry.id);

        for (Map.Entry<ServerPlayerEntity, PlayerData> playerEntry : playerTracking.entrySet()) {
            PlayerData pd = playerEntry.getValue();
            if (pd.dimensionID == entry.dimensionID && pd.domainID == entry.domainID) {
                entry.removeListener(playerEntry.getKey());
                ServerSidePacketRegistry.INSTANCE.sendToPlayer(playerEntry.getKey(), packet);
            }
        }
    }

    public static void onDomainChanged(PlayerEntity player, IDomain oldDomain, IDomain newDomain) {
        if (player instanceof ServerPlayerEntity) {
            INSTANCE.updatePlayerTracking((ServerPlayerEntity) player);
        }
    }

    /**
     * Call when player joins server, changes dimension or changes active domain.
     */
    public void updatePlayerTracking(ServerPlayerEntity player) {
        if (XmConfig.logExcavationRenderTracking)
            Xm.LOG.info("updatePlayerTracking for %s", player.getName());

        PlayerData newData = new PlayerData(player);
        PlayerData oldData = playerTracking.get(player);

        if (oldData != null && oldData.dimensionID == newData.dimensionID && oldData.domainID == newData.domainID) {
            // no changes
            if (XmConfig.logExcavationRenderTracking)
                Xm.LOG.info("updatePlayerTracking exit no changes");
            return;
        }

        playerTracking.put(player, newData);

        // remove old listeners if needed
        if (oldData != null) {
            synchronized (this) {
                Int2ObjectOpenHashMap<ExcavationRenderEntry> entries = this.get(oldData.dimensionID);
                if (entries != null && !entries.isEmpty()) {
                    for (ExcavationRenderEntry entry : entries.values()) {
                        if (entry.domainID == oldData.domainID)
                            entry.removeListener(player);
                    }
                }
            }
        }

        // build refresh
        final SimpleUnorderedArrayList<ExcavationRenderEntry> output = new SimpleUnorderedArrayList<>();
        synchronized (this) {
            Int2ObjectOpenHashMap<ExcavationRenderEntry> entries = this.get(newData.dimensionID);
            if (entries != null && !entries.isEmpty()) {
                for (ExcavationRenderEntry entry : entries.values()) {
                    if (entry.domainID == newData.domainID) {
                        entry.addListener(player, false);
                        if (entry.isFirstComputeDone())
                            output.add(entry);
                    }
                }
            }
        }
        ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, S2C_PacketExcavationRenderRefresh.toPacket(output));
    }

    public void stopPlayerTracking(ServerPlayerEntity player) {
        PlayerData oldData = playerTracking.get(player);

        if (oldData == null)
            return;

        synchronized (this) {
            Int2ObjectOpenHashMap<ExcavationRenderEntry> entries = this.get(oldData.dimensionID);
            if (entries != null && !entries.isEmpty()) {
                for (ExcavationRenderEntry entry : entries.values()) {
                    if (entry.domainID == oldData.domainID)
                        entry.removeListener(player);
                }
            }
        }

        playerTracking.remove(player);
    }

    private static class PlayerData {
        private final int domainID;
        private final int dimensionID;

        private PlayerData(ServerPlayerEntity player) {
            this.domainID = DomainManager.instance().getActiveDomain(player).getId();
            this.dimensionID = player.dimension.getRawId();
        }
    }

    @Override
    public void clear() {
        super.clear();
        playerTracking.clear();
    }
}
