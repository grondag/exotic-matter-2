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
package grondag.exotic_matter.simulator.domain;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.eventbus.EventBus;

import grondag.exotic_matter.simulator.persistence.IDirtListenerProvider;
import grondag.exotic_matter.simulator.persistence.IIdentified;
import grondag.exotic_matter.simulator.persistence.ISimulationNode;
import net.minecraft.entity.player.PlayerEntity;

public interface IDomain extends ISimulationNode, IDirtListenerProvider, IIdentified {

    EventBus eventBus();

    List<DomainUser> getAllUsers();

    @Nullable
    DomainUser findPlayer(PlayerEntity player);

    @Nullable
    DomainUser findUser(String userName);

    boolean hasPrivilege(PlayerEntity player, Privilege privilege);

    /**
     * Will return existing user if already exists.
     */
    DomainUser addPlayer(PlayerEntity player);

    String getName();

    void setName(String name);

    boolean isSecurityEnabled();

    void setSecurityEnabled(boolean isSecurityEnabled);

    <V extends IDomainCapability> V getCapability(Class<V> capability);

}
