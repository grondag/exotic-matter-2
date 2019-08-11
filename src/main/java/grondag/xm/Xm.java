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

import grondag.fermion.simulator.Simulator;
import grondag.xm.network.Packets;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.server.ServerStartCallback;
import net.fabricmc.fabric.api.event.server.ServerTickCallback;

public class Xm implements ModInitializer {

    @Override
    public void onInitialize() {
        ServerStartCallback.EVENT.register(Simulator::start);
        ServerTickCallback.EVENT.register(s -> Simulator.instance().tick(s));
        Packets.initializeCommon();
    }

    public static Logger LOG = LogManager.getLogger("Exotic Matter");

    public static final String MODID = "exotic-matter";
}
