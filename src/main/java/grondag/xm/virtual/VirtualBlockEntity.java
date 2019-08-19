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
package grondag.xm.virtual;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.CompoundTag;

public class VirtualBlockEntity extends BlockEntity {
    public VirtualBlockEntity(BlockEntityType<?> blockEntityType) {
        super(blockEntityType);
    }

//    /**
//     * Identifies the domain to which this block belongs. Should be set immediately
//     * after creation and not changed.
//     * <p>
//     * 
//     * Somewhat redundant of {@link #buildID} but want both to be available
//     * client-side.
//     * <p>
//     * 
//     * NB: Not looking up domain instance here because needs to run on the client.
//     */
//    private int domainID = IIdentified.UNASSIGNED_ID;

//    /**
//     * Identifies the build to which this block belongs. Should be set immediately
//     * after creation and not changed.
//     * 
//     * Should be in the domain identified by {@link #domainID}
//     */
//    private int buildID = IIdentified.UNASSIGNED_ID;

    public boolean isVirtual() {
        return true;
    }

//    /**
//     * See {@link #domainID}
//     */
//    public int domainID() {
//        return this.domainID;
//    }

//    public boolean hasDomain() {
//        return this.domainID != IIdentified.UNASSIGNED_ID;
//    }

//    /**
//     * See {@link #domainID}
//     */
//    public void setDomain(IDomain domain) {
//        if (domain != null) {
//            this.domainID = domain.getId();
//            this.markDirty();
//        }
//    }

//    /**
//     * See {@link #buildID}
//     */
//    public int buildID() {
//        return this.buildID;
//    }

//    public boolean hasBuild() {
//        return this.buildID != IIdentified.UNASSIGNED_ID;
//    }

//    /**
//     * See {@link #buildID} Also sets domain.
//     */
//    public void setBuild(Build build) {
//        if (build != null) {
//            this.buildID = build.getId();
//            this.setDomain(build.getDomain());
//            this.markDirty();
//        }
//    }

    @Environment(EnvType.CLIENT)
    public boolean isVisible() {
        // TODO: what and how?
        // maybe just have it global so that people can see each other's blocks?
        // if so, could have virtual blocks act a opaque cubes and enable chunk culling?
        return true;
    }

//    private static final String NBT_DOMAIN_ID = NBTDictionary.claim("vtDomID");
//    private static final String NBT_BUILD_ID = NBTDictionary.claim("vtBuildID");

    @Override
    public CompoundTag toTag(CompoundTag compound) {
        compound = super.toTag(compound);
//        compound.putInt(NBT_DOMAIN_ID, this.domainID);
//        compound.putInt(NBT_BUILD_ID, this.buildID);
        return compound;
    }

    @Override
    public void fromTag(CompoundTag compound) {
        super.fromTag(compound);

//        this.domainID = compound.containsKey(NBT_DOMAIN_ID) ? compound.getInt(NBT_DOMAIN_ID) : IIdentified.UNASSIGNED_ID;
//
//        this.buildID = compound.containsKey(NBT_BUILD_ID) ? compound.getInt(NBT_BUILD_ID) : IIdentified.UNASSIGNED_ID;
    }

}
