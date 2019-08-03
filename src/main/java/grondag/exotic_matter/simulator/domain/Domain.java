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

import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;

import grondag.exotic_matter.simulator.persistence.AssignedNumber;
import grondag.exotic_matter.simulator.persistence.IDirtListener;
import grondag.exotic_matter.simulator.persistence.IDirtListenerProvider;
import grondag.exotic_matter.simulator.persistence.IIdentified;
import grondag.fermion.varia.NBTDictionary;
import grondag.fermion.varia.ReadWriteNBT;
import grondag.xm.Xm;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

public class Domain implements ReadWriteNBT, IDirtListenerProvider, IIdentified, IDomain {
    private static final String NBT_DOMAIN_SECURITY_ENABLED = NBTDictionary.claim("domSecOn");
    private static final String NBT_DOMAIN_NAME = NBTDictionary.claim("domName");
    private static final String NBT_DOMAIN_USERS = NBTDictionary.claim("domUsers");

    private static final HashSet<Class<? extends IDomainCapability>> capabilityTypes = new HashSet<>();

    public static void registerCapability(Class<? extends IDomainCapability> capabilityType) {
        capabilityTypes.add(capabilityType);
    }

    private final DomainManager domainManager;
    int id;
    String name;
    boolean isSecurityEnabled;
    private final IdentityHashMap<Class<? extends IDomainCapability>, IDomainCapability> capabilities = new IdentityHashMap<>();

    private final EventBus eventBus = new EventBus();

    private HashMap<String, DomainUser> users = new HashMap<String, DomainUser>();

    // private constructor
    Domain(DomainManager domainManager) {
        this.domainManager = domainManager;

        this.capabilities.clear();
        if (!capabilityTypes.isEmpty()) {
            for (Class<? extends IDomainCapability> capType : capabilityTypes) {
                try {
                    IDomainCapability cap;
                    cap = capType.newInstance();
                    cap.setDomain(this);
                    this.capabilities.put(capType, cap);
                } catch (Exception e) {
                    Xm.LOG.error("Unable to create domain capability", e);
                }
            }
        }
    }

    Domain(DomainManager domainManager, CompoundTag tag) {
        this(domainManager);
        this.writeTag(tag);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V extends IDomainCapability> V getCapability(Class<V> capability) {
        return (V) this.capabilities.get(capability);
    }

    @Override
    public EventBus eventBus() {
        return this.eventBus;
    }

    @Override
    public List<DomainUser> getAllUsers() {
        return ImmutableList.copyOf(users.values());
    }

    @Override
    @Nullable
    public DomainUser findPlayer(PlayerEntity player) {
        return this.findUser(player.getUuidAsString());
    }

    @Override
    @Nullable
    public DomainUser findUser(String userUUID) {
        return this.users.get(userUUID);
    }

    @Override
    public boolean hasPrivilege(PlayerEntity player, Privilege privilege) {
        DomainUser user = findPlayer(player);
        return user == null ? false : user.hasPrivilege(privilege);
    }

    /**
     * Will return existing user if already exists.
     */
    @Override
    public synchronized DomainUser addPlayer(PlayerEntity player) {
        DomainUser result = this.findPlayer(player);
        if (result == null) {
            result = new DomainUser(this, player);
            this.users.put(result.userName, result);
            this.domainManager.isDirty = true;
        }
        return result;
    }

    @Override
    public int getIdRaw() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public AssignedNumber idType() {
        return AssignedNumber.DOMAIN;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
        this.domainManager.isDirty = true;
    }

    @Override
    public boolean isSecurityEnabled() {
        return isSecurityEnabled;
    }

    @Override
    public void setSecurityEnabled(boolean isSecurityEnabled) {
        this.isSecurityEnabled = isSecurityEnabled;
        this.domainManager.isDirty = true;
    }

    @Override
    public void setDirty() {
        this.domainManager.isDirty = true;
    }

    @Override
    public void readTag(CompoundTag tag) {
        this.serializeID(tag);
        tag.putBoolean(NBT_DOMAIN_SECURITY_ENABLED, this.isSecurityEnabled);
        tag.putString(NBT_DOMAIN_NAME, this.name);

        ListTag nbtUsers = new ListTag();

        if (!this.users.isEmpty()) {
            for (DomainUser user : this.users.values()) {
                nbtUsers.add(user.toTag());
            }
        }
        tag.put(NBT_DOMAIN_USERS, nbtUsers);
    }

    @Override
    public void writeTag(@Nullable CompoundTag tag) {
        this.deserializeID(tag);
        this.isSecurityEnabled = tag.getBoolean(NBT_DOMAIN_SECURITY_ENABLED);
        this.name = tag.getString(NBT_DOMAIN_NAME);

        ListTag nbtUsers = tag.getList(NBT_DOMAIN_USERS, 10);
        if (nbtUsers != null && !nbtUsers.isEmpty()) {
            for (int i = 0; i < nbtUsers.size(); ++i) {
                DomainUser user = new DomainUser(this, nbtUsers.getCompoundTag(i));
                this.users.put(user.userName, user);
            }
        }
    }

    public DomainManager domainManager() {
        return this.domainManager;
    }

    @Override
    public IDirtListener getDirtListener() {
        return this.domainManager;
    }

    @Override
    public void afterDeserialization() {
        this.capabilities.values().forEach(c -> c.afterDeserialization());
    }

    @Override
    public void unload() {
        this.capabilities.values().forEach(c -> c.unload());
    }

    @Override
    public void loadNew() {
        this.capabilities.values().forEach(c -> c.loadNew());
    }

}
