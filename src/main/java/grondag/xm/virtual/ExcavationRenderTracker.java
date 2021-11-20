/*
 * Copyright © Original Authors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Additional copyright and licensing notices may apply for content that was
 * included from other projects. For more information, see ATTRIBUTION.md.
 */

package grondag.xm.virtual;

import java.util.IdentityHashMap;
import java.util.Map;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import io.vram.sc.unordered.SimpleUnorderedArrayList;

import grondag.fermion.world.WorldMap;
import grondag.xm.Xm;
import grondag.xm.XmConfig;
import grondag.xm.network.S2C_ExcavationRenderUpdate;
import grondag.xm.network.S2C_PacketExcavationRenderRefresh;

@Internal
public class ExcavationRenderTracker extends WorldMap<Int2ObjectOpenHashMap<ExcavationRenderEntry>> {
	public static final ExcavationRenderTracker INSTANCE = new ExcavationRenderTracker();

	private final IdentityHashMap<ServerPlayer, PlayerData> playerTracking = new IdentityHashMap<>();

	@Override
	protected Int2ObjectOpenHashMap<ExcavationRenderEntry> load(Level world) {
		return new Int2ObjectOpenHashMap<>();
	}

	public void add(ExcavationRenderTask task) {
		final ExcavationRenderEntry entry = new ExcavationRenderEntry(task);

		if (XmConfig.logExcavationRenderTracking) {
			Xm.LOG.info("id = %d new Entry, valid=%s", entry.id, Boolean.toString(entry.isValid()));
		}

		if (entry.isValid()) {
			synchronized (this) {
				this.get(entry.world).put(entry.id, entry);
			}

			for (final Map.Entry<ServerPlayer, PlayerData> playerEntry : playerTracking.entrySet()) {
				final PlayerData pd = playerEntry.getValue();

				if (pd.world == entry.world && task.visibleTo(playerEntry.getKey())) {
					if (XmConfig.logExcavationRenderTracking) {
						Xm.LOG.info("adding listeners for %s", playerEntry.getKey().getDisplayName());
					}

					entry.addListener(playerEntry.getKey(), true);
				}
			}
		}
	}

	public synchronized void remove(ExcavationRenderEntry entry) {
		if (XmConfig.logExcavationRenderTracking) {
			Xm.LOG.info("id = %d removing excavation render entry", entry.id);
		}

		this.get(entry.world).remove(entry.id);

		final Packet<?> packet = S2C_ExcavationRenderUpdate.toPacket(entry.id);

		for (final Map.Entry<ServerPlayer, PlayerData> playerEntry : playerTracking.entrySet()) {
			final PlayerData pd = playerEntry.getValue();

			if (pd.world == entry.world) {
				entry.removeListener(playerEntry.getKey());
				playerEntry.getKey().connection.send(packet);
			}
		}
	}

	/**
	 * Call when player joins server, changes dimension or changes active domain.
	 */
	public void updatePlayerTracking(ServerPlayer player) {
		if (XmConfig.logExcavationRenderTracking) {
			Xm.LOG.info("updatePlayerTracking for %s", player.getName());
		}

		final PlayerData newData = new PlayerData(player);
		final PlayerData oldData = playerTracking.get(player);

		if (oldData != null && oldData.world == newData.world && oldData.domainID == newData.domainID) {
			// no changes
			if (XmConfig.logExcavationRenderTracking) {
				Xm.LOG.info("updatePlayerTracking exit no changes");
			}

			return;
		}

		playerTracking.put(player, newData);

		// remove old listeners if needed
		if (oldData != null) {
			synchronized (this) {
				final Int2ObjectOpenHashMap<ExcavationRenderEntry> entries = this.get(oldData.world);

				if (entries != null && !entries.isEmpty()) {
					for (final ExcavationRenderEntry entry : entries.values()) {
						entry.removeListener(player);
					}
				}
			}
		}

		// build refresh
		final SimpleUnorderedArrayList<ExcavationRenderEntry> output = new SimpleUnorderedArrayList<>();
		synchronized (this) {
			final Int2ObjectOpenHashMap<ExcavationRenderEntry> entries = this.get(newData.world);

			if (entries != null && !entries.isEmpty()) {
				for (final ExcavationRenderEntry entry : entries.values()) {
					if (entry.task.visibleTo(player)) {
						entry.addListener(player, false);

						if (entry.isFirstComputeDone()) {
							output.add(entry);
						}
					}
				}
			}
		}

		player.connection.send(S2C_PacketExcavationRenderRefresh.toPacket(output));
	}

	public void stopPlayerTracking(ServerPlayer player) {
		final PlayerData oldData = playerTracking.get(player);

		if (oldData == null) {
			return;
		}

		synchronized (this) {
			final Int2ObjectOpenHashMap<ExcavationRenderEntry> entries = this.get(oldData.world);

			if (entries != null && !entries.isEmpty()) {
				for (final ExcavationRenderEntry entry : entries.values()) {
					entry.removeListener(player);
				}
			}
		}

		playerTracking.remove(player);
	}

	private static class PlayerData {
		private final int domainID;
		private final Level world;

		private PlayerData(ServerPlayer player) {
			// FIX: put back when Domains are moved out of Simulator
			domainID = 1; //DomainManager.instance().getActiveDomain(player).getAssignedNumber();
			world = player.level;
		}
	}

	@Override
	public void clear() {
		super.clear();
		playerTracking.clear();
	}
}
