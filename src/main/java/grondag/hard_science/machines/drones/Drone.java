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
package grondag.hard_science.machines.drones;

import javax.annotation.Nullable;

import grondag.exotic_matter.serialization.IReadWriteNBT;
import grondag.exotic_matter.serialization.NBTDictionary;
import grondag.exotic_matter.simulator.domain.DomainManager;
import grondag.exotic_matter.simulator.domain.IDomain;
import grondag.exotic_matter.simulator.persistence.AssignedNumber;
import grondag.exotic_matter.simulator.persistence.IIdentified;
import grondag.exotic_matter.world.Location;
import grondag.hard_science.machines.energy.DeviceEnergyManager;
import grondag.hard_science.machines.matbuffer.BufferManager;
import grondag.hard_science.simulator.device.IDevice;
import grondag.hard_science.simulator.fobs.NewProcurementTask;
import grondag.hard_science.simulator.resource.IResource;
import net.minecraft.nbt.NBTTagCompound;

public class Drone implements IDevice, IReadWriteNBT {
    private static final String NBT_DOMAIN_ID = NBTDictionary.claim("droneDomID");

    private int id = IIdentified.UNASSIGNED_ID;

    private Location location;

    private int domainID = IIdentified.UNASSIGNED_ID;

    /** don't reference directly */
    private IDomain domain = null;

    @Override
    public int getIdRaw() {
        return this.id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
        this.setDirty();
    }

    @Override
    public AssignedNumber idType() {
        return AssignedNumber.DEVICE;
    }

    @Override
    public @Nullable IDomain getDomain() {
        if (this.domain == null && this.domainID != IIdentified.UNASSIGNED_ID) {
            this.domain = DomainManager.instance().getDomain(this.domainID);
        }
        return this.domain;
    }

    @Override
    public @Nullable Location getLocation() {
        return this.location;
    }

    @Override
    public void setLocation(@Nullable Location loc) {
        this.location = loc;
        this.setDirty();
    }

    @Override
    public void deserializeNBT(@Nullable NBTTagCompound tag) {
        this.deserializeID(tag);
        this.deserializeLocation(tag);
        this.domainID = tag.getInteger(NBT_DOMAIN_ID);
    }

    @Override
    public void serializeNBT(NBTTagCompound tag) {
        this.serializeID(tag);
        this.serializeLocation(tag);
        tag.setInteger(NBT_DOMAIN_ID, this.domainID);
    }

    @Override
    public boolean isConnected() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public long onProduce(IResource<?> resource, long quantity, boolean simulate, @Nullable NewProcurementTask<?> request) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long onConsume(IResource<?> resource, long quantity, boolean simulate, @Nullable NewProcurementTask<?> request) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public DeviceEnergyManager energyManager() {
        return null;
    }

    @Override
    public @Nullable BufferManager getBufferManager() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onConnect() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onDisconnect() {
        // TODO Auto-generated method stub

    }
}
