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

package grondag.xm2;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import grondag.exotic_matter.simulator.Simulator;
import grondag.exotic_matter.simulator.domain.PlayerDomainChangeCallback;
import grondag.hard_science.network.Packets;
import grondag.xm2.block.virtual.ExcavationRenderTracker;
import grondag.xm2.init.XmBlocks;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.server.ServerStartCallback;
import net.fabricmc.fabric.api.event.server.ServerTickCallback;

public class Xm implements ModInitializer {
    public static Xm INSTANCE = new Xm();

    @Override
    public void onInitialize() {
        XmBlocks.init();
        ServerStartCallback.EVENT.register(Simulator::start);
        ServerTickCallback.EVENT.register(Simulator.instance()::tick);
        Packets.initializeCommon();
        PlayerDomainChangeCallback.EVENT.register(ExcavationRenderTracker::onDomainChanged);
    }

    public static Logger LOG = LogManager.getLogger("Exotic Matter");

    public static final String MODID = "xm2";
}
