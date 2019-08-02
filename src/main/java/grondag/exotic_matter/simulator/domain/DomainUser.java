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

import java.util.HashSet;
import java.util.IdentityHashMap;

import javax.annotation.Nullable;

import grondag.fermion.serialization.ReadWriteNBT;
import grondag.fermion.serialization.NBTDictionary;
import grondag.xm2.Xm;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;

public class DomainUser implements ReadWriteNBT, IDomainMember {
    private static final HashSet<Class<? extends IUserCapability>> capabilityTypes = new HashSet<>();

    public static void registerCapability(Class<? extends IUserCapability> capabilityType) {
        capabilityTypes.add(capabilityType);
    }

    private static final String DOMAIN_USER_NAME = NBTDictionary.claim("domUserName");
    private static final String DOMAIN_USER_UUID = NBTDictionary.claim("domUserUUID");
    private static final String DOMAIN_USER_FLAGS = NBTDictionary.claim("domUserFlags");

    private final IDomain domain;

    // TODO: encapsulate these
    public String userName;
    public String uuid;

    private int privilegeFlags;

    private final IdentityHashMap<Class<? extends IUserCapability>, IUserCapability> capabilities = new IdentityHashMap<>();

    public DomainUser(IDomain domain, PlayerEntity player) {
        this.domain = domain;
        this.userName = player.getEntityName();
        this.uuid = player.getUuidAsString();
        this.createCapabilities();
    }

    public DomainUser(IDomain domain, CompoundTag tag) {
        this.domain = domain;
        this.createCapabilities();
        this.writeTag(tag);
    }

    private void createCapabilities() {
        this.capabilities.clear();
        if (!capabilityTypes.isEmpty()) {
            for (Class<? extends IUserCapability> capType : capabilityTypes) {
                try {
                    IUserCapability cap;
                    cap = capType.newInstance();
                    cap.setDomainUser(this);
                    this.capabilities.put(capType, cap);
                } catch (Exception e) {
                    Xm.LOG.error("Unable to create domain user capability", e);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <V extends IUserCapability> V getCapability(Class<V> capability) {
        return (V) this.capabilities.get(capability);
    }

    /**
     * Will return true for admin users, regardless of other Privilege grants. Will
     * also return true if security is disabled for the domain.
     */
    public boolean hasPrivilege(Privilege p) {
        return !this.domain.isSecurityEnabled() || Privilege.PRIVILEGE_FLAG_SET.isFlagSetForValue(Privilege.ADMIN, privilegeFlags)
                || Privilege.PRIVILEGE_FLAG_SET.isFlagSetForValue(p, privilegeFlags);
    }

    public void grantPrivilege(Privilege p, boolean hasPrivilege) {
        this.privilegeFlags = Privilege.PRIVILEGE_FLAG_SET.setFlagForValue(p, privilegeFlags, hasPrivilege);
        this.domain.setDirty();
        ;
    }

    public void setPrivileges(Privilege... granted) {
        this.privilegeFlags = Privilege.PRIVILEGE_FLAG_SET.getFlagsForIncludedValues(granted);
        this.domain.setDirty();
        ;
    }

    @Override
    public void readTag(CompoundTag nbt) {
        nbt.putString(DOMAIN_USER_NAME, this.userName);
        nbt.putString(DOMAIN_USER_UUID, this.uuid);
        nbt.putInt(DOMAIN_USER_FLAGS, this.privilegeFlags);

        if (!this.capabilities.isEmpty()) {
            for (IUserCapability cap : this.capabilities.values()) {
                if (!cap.isSerializationDisabled())
                    nbt.put(cap.tagName(), cap.toTag());
            }
        }
    }

    @Override
    public void writeTag(@Nullable CompoundTag nbt) {
        this.userName = nbt.getString(DOMAIN_USER_NAME);
        this.uuid = nbt.getString(DOMAIN_USER_UUID);
        this.privilegeFlags = nbt.getInt(DOMAIN_USER_FLAGS);
        this.capabilities.clear();

        if (!this.capabilities.isEmpty()) {
            for (IUserCapability cap : this.capabilities.values()) {
                if (nbt.containsKey(cap.tagName())) {
                    cap.writeTag(nbt.getCompound(cap.tagName()));
                }
            }
        }
    }

    @Override
    public @Nullable IDomain getDomain() {
        return this.domain;
    }

}
