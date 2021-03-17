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
package grondag.xm;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.util.Identifier;

import net.fabricmc.api.ModInitializer;

import grondag.xm.api.paint.VertexProcessorRegistry;
import grondag.xm.api.paint.VertexProcessors;
import grondag.xm.network.Packets;

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

	public static Identifier id(String path) {
		return new Identifier(MODID, path);
	}
}
