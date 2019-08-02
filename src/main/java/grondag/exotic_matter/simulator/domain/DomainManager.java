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
import java.util.List;
import java.util.Map.Entry;

import com.google.common.collect.ImmutableList;

import grondag.exotic_matter.simulator.Simulator;
import grondag.exotic_matter.simulator.persistence.AssignedNumber;
import grondag.exotic_matter.simulator.persistence.AssignedNumbersAuthority.IdentifiedIndex;
import grondag.exotic_matter.simulator.persistence.IIdentified;
import grondag.exotic_matter.simulator.persistence.SimulationTopNode;
import grondag.fermion.serialization.NBTDictionary;
import grondag.xm2.Xm;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.network.ServerPlayerEntity;

public class DomainManager extends SimulationTopNode {
    private static final String NBT_DOMAIN_MANAGER = NBTDictionary.claim("domMgr");
    private static final String NBT_DOMAIN_MANAGER_DOMAINS = NBTDictionary.claim("domMgrAll");
    private static final String NBT_DOMAIN_PLAYER_DOMAINS = NBTDictionary.claim("domMgrPlayer");
    private static final String NBT_DOMAIN_ACTIVE_DOMAINS = NBTDictionary.claim("domMgrActive");

    /**
     * Set to null when Simulator creates singleton and when it shuts down to force
     * retrieval of current instance.
     */
    private static DomainManager instance;

    public static DomainManager instance() {
        return instance;
    }

    private boolean isDeserializationInProgress = false;

    boolean isDirty = false;

    private boolean isLoaded = false;

    private IDomain defaultDomain;

    /**
     * Each player has a domain that is automatically created for them and which
     * they always own. This will be their initially active domain.
     */
    private HashMap<String, IDomain> playerIntrinsicDomains = new HashMap<>();

    /**
     * Each player has a currently active domain. This will initially be their
     * intrinsic domain.
     */
    private HashMap<String, IDomain> playerActiveDomains = new HashMap<>();

    /**
     * If isNew=true then won't wait for a deserialize to become loaded.
     */
    public DomainManager() {
        super(NBT_DOMAIN_MANAGER);
        // force refresh of singleton reference
        instance = null;

    }

    /**
     * Called at shutdown
     */
    @Override
    public void unload() {
        this.playerActiveDomains.clear();
        this.playerIntrinsicDomains.clear();
        this.defaultDomain = null;
        this.isLoaded = false;
    }

    @Override
    public void afterCreated(Simulator sim) {
        instance = this;
    }

    @Override
    public void loadNew() {
        this.unload();
        this.isLoaded = true;
    }

    /**
     * Domain for unmanaged objects.
     */
    public IDomain defaultDomain() {
        this.checkLoaded();
        if (this.defaultDomain == null) {
            defaultDomain = domainFromId(1);
            if (defaultDomain == null) {
                this.defaultDomain = new Domain(this);
                this.defaultDomain.setSecurityEnabled(false);
                this.defaultDomain.setId(IIdentified.DEFAULT_ID);
                this.defaultDomain.setName("Public");
                ;
                Simulator.instance().assignedNumbersAuthority().register(defaultDomain);
            }
        }
        return this.defaultDomain;
    }

    public List<IDomain> getAllDomains() {
        this.checkLoaded();
        ImmutableList.Builder<IDomain> builder = ImmutableList.builder();
        for (IIdentified domain : Simulator.instance().assignedNumbersAuthority().getIndex(AssignedNumber.DOMAIN).values()) {
            builder.add((Domain) domain);
        }
        return builder.build();
    }

    public IDomain getDomain(int id) {
        this.checkLoaded();
        return domainFromId(id);
    }

    public synchronized IDomain createDomain() {
        this.checkLoaded();
        Domain result = new Domain(this);
        Simulator.instance().assignedNumbersAuthority().register(result);
        result.name = "Domain " + result.id;
        this.isDirty = true;
        return result;
    }

    /**
     * Does NOT destroy any of the contained objects in the domain!
     */
    public synchronized void removeDomain(IDomain domain) {
        this.checkLoaded();
        Simulator.instance().assignedNumbersAuthority().unregister(domain);
        this.isDirty = true;
    }

    @Override
    public boolean isDirty() {
        return this.isDirty;
    }

    @Override
    public void setDirty(boolean isDirty) {
        this.isDirty = isDirty;
    }

    @Override
    public void fromTag(CompoundTag tag) {
        this.isDeserializationInProgress = true;

        this.unload();

        // need to do this before loading domains, otherwise they will cause complaints
        this.isLoaded = true;

        if (tag == null)
            return;

        ListTag nbtDomains = tag.getList(NBT_DOMAIN_MANAGER_DOMAINS, 10);
        if (nbtDomains != null && !nbtDomains.isEmpty()) {
            for (int i = 0; i < nbtDomains.size(); ++i) {
                Domain domain = new Domain(this, nbtDomains.getCompoundTag(i));
                Simulator.instance().assignedNumbersAuthority().register(domain);
            }
        }

        CompoundTag nbtPlayerDomains = tag.getCompound(NBT_DOMAIN_PLAYER_DOMAINS);
        if (nbtPlayerDomains != null && !nbtPlayerDomains.isEmpty()) {
            for (String playerName : nbtPlayerDomains.getKeys()) {
                IDomain d = domainFromId(nbtPlayerDomains.getInt(playerName));
                if (d != null)
                    this.playerIntrinsicDomains.put(playerName, d);
            }
        }

        CompoundTag nbtActiveDomains = tag.getCompound(NBT_DOMAIN_ACTIVE_DOMAINS);
        if (nbtActiveDomains != null && !nbtActiveDomains.isEmpty()) {
            for (String playerName : nbtActiveDomains.getKeys()) {
                IDomain d = domainFromId(nbtActiveDomains.getInt(playerName));
                if (d != null)
                    this.playerActiveDomains.put(playerName, d);
            }
        }

        this.isDeserializationInProgress = false;
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        ListTag nbtDomains = new ListTag();

        IdentifiedIndex domains = Simulator.instance().assignedNumbersAuthority().getIndex(AssignedNumber.DOMAIN);

        if (!domains.isEmpty()) {
            for (IIdentified domain : domains.values()) {
                nbtDomains.add(((Domain) domain).toTag());
            }
        }
        tag.put(NBT_DOMAIN_MANAGER_DOMAINS, nbtDomains);

        if (!this.playerIntrinsicDomains.isEmpty()) {
            CompoundTag nbtPlayerDomains = new CompoundTag();
            for (Entry<String, IDomain> entry : this.playerIntrinsicDomains.entrySet()) {
                nbtPlayerDomains.putInt(entry.getKey(), entry.getValue().getId());
            }
            tag.put(NBT_DOMAIN_PLAYER_DOMAINS, nbtPlayerDomains);
        }

        if (!this.playerActiveDomains.isEmpty()) {
            CompoundTag nbtActiveDomains = new CompoundTag();
            for (Entry<String, IDomain> entry : this.playerActiveDomains.entrySet()) {
                nbtActiveDomains.putInt(entry.getKey(), entry.getValue().getId());
            }
            tag.put(NBT_DOMAIN_ACTIVE_DOMAINS, nbtActiveDomains);
        }
        return tag;
    }

    private boolean checkLoaded() {
        if (!this.isLoaded) {
            Xm.LOG.warn("Domain manager accessed before it was loaded.  This is a bug and probably means simulation state has been lost.");
        }
        return this.isLoaded;
    }

    /**
     * The player's currently active domain. If player has never specified, will be
     * the player's intrinsic domain.
     */
    public IDomain getActiveDomain(ServerPlayerEntity player) {
        IDomain result = this.playerActiveDomains.get(player.getUuidAsString());
        if (result == null) {
            synchronized (this.playerActiveDomains) {
                result = this.playerActiveDomains.get(player.getUuidAsString());
                if (result == null) {
                    result = this.getIntrinsicDomain(player);
                    this.playerActiveDomains.put(player.getUuidAsString(), result);
                }
            }
        }
        return result;
    }

    /**
     * Set the player's currently active domain.<br>
     * Posts an event so that anything dependent on active domain can react.
     */
    public void setActiveDomain(ServerPlayerEntity player, IDomain domain) {
        synchronized (this.playerActiveDomains) {
            IDomain result = this.playerActiveDomains.put(player.getUuidAsString(), domain);
            if (result == null || result != domain) {
                PlayerDomainChangeCallback.EVENT.invoker().onDomainChange(player, result, domain);
            }
        }
    }

    /**
     * The player's private, default domain. Created if does not already exist.
     */
    public IDomain getIntrinsicDomain(ServerPlayerEntity player) {
        IDomain result = this.playerIntrinsicDomains.get(player.getUuidAsString());
        if (result == null) {
            synchronized (this.playerIntrinsicDomains) {
                result = this.playerIntrinsicDomains.get(player.getUuidAsString());
                if (result == null) {
                    result = this.createDomain();
                    result.setSecurityEnabled(true);
                    result.setName(I18n.translate("misc.default_domain_template", player.getName()));
                    DomainUser user = result.addPlayer(player);
                    user.setPrivileges(Privilege.ADMIN);
                    this.playerIntrinsicDomains.put(player.getUuidAsString(), result);
                }
            }
        }
        return result;
    }

    public boolean isDeserializationInProgress() {
        return this.isDeserializationInProgress;
    }

    // convenience object lookup methods
    public static IDomain domainFromId(int id) {
        return (Domain) Simulator.instance().assignedNumbersAuthority().get(id, AssignedNumber.DOMAIN);
    }

    @Override
    public void afterDeserialization() {
        IdentifiedIndex domains = Simulator.instance().assignedNumbersAuthority().getIndex(AssignedNumber.DOMAIN);

        if (!domains.isEmpty()) {
            for (IIdentified domain : domains.values()) {
                ((Domain) domain).afterDeserialization();
            }
        }
    }

    @Override
    public void setDirty() {
        // TODO Auto-generated method stub

    }
}
