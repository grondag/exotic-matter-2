/*
 * This file is part of Exotic Matter and is licensed to the project under
 * terms that are compatible with the GNU Lesser General Public License.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership and licensing.
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
 */

package grondag.xm;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.resources.ResourceLocation;

import net.fabricmc.api.ModInitializer;

import grondag.xm.api.paint.VertexProcessorRegistry;
import grondag.xm.api.paint.VertexProcessors;
import grondag.xm.network.Packets;

// WIP: move to Fabric source tree
@Internal
public class Xm implements ModInitializer {
	@Override
	public void onInitialize() {
		XmConfig.init();
		Packets.initializeCommon();

		VertexProcessorRegistry.INSTANCE.add(Xm.id("variation"), VertexProcessors.SPECIES_VARIATION);
	}

	public static Logger LOG = LogManager.getLogger("Exotic Matter");

	public static final String MODID = "exotic-matter";

	public static String idString(String path) {
		return MODID + ":" + path;
	}

	public static ResourceLocation id(String path) {
		return new ResourceLocation(MODID, path);
	}
}
