/*
 * Copyright © Original Authors
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
 *
 * Additional copyright and licensing notices may apply for content that was
 * included from other projects. For more information, see ATTRIBUTION.md.
 */

package grondag.xm.virtual;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Internal
public class VirtualBlockEntity extends BlockEntity {
	public VirtualBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state) {
		super(blockEntityType, pos, state);
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
	public CompoundTag save(CompoundTag compound) {
		compound = super.save(compound);
		//        compound.putInt(NBT_DOMAIN_ID, this.domainID);
		//        compound.putInt(NBT_BUILD_ID, this.buildID);
		return compound;
	}

	@Override
	public void load(CompoundTag compound) {
		super.load(compound);

		//        this.domainID = compound.containsKey(NBT_DOMAIN_ID) ? compound.getInt(NBT_DOMAIN_ID) : IIdentified.UNASSIGNED_ID;
		//
		//        this.buildID = compound.containsKey(NBT_BUILD_ID) ? compound.getInt(NBT_BUILD_ID) : IIdentified.UNASSIGNED_ID;
	}
}
