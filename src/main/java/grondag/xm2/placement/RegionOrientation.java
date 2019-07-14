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

package grondag.xm2.placement;

import grondag.fermion.serialization.NBTDictionary;
import grondag.fermion.varia.ILocalized;
import grondag.fermion.varia.Useful;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.client.resource.language.I18n;

public enum RegionOrientation implements ILocalized {
    AUTOMATIC, XYZ, ZYX, ZXY, XZY, YXZ, YZX;

    private static final String TAG_NAME = NBTDictionary.claim("regionOrientation");

    public RegionOrientation deserializeNBT(CompoundTag tag) {
	return Useful.safeEnumFromTag(tag, TAG_NAME, this);
    }

    public void serializeNBT(CompoundTag tag) {
	Useful.saveEnumToTag(tag, TAG_NAME, this);
    }

    public RegionOrientation fromBytes(PacketByteBuf pBuff) {
	return pBuff.readEnumConstant(RegionOrientation.class);
    }

    public void toBytes(PacketByteBuf pBuff) {
	pBuff.writeEnumConstant(this);
    }

    @Override
    public String localizedName() {
	return I18n.translate("placement.orientation.region." + this.name().toLowerCase());
    }

    public BlockPos rotatedRegionPos(BlockPos fromPos) {
	switch (this) {
	case XYZ:
	case AUTOMATIC:
	default:
	    return fromPos;

	case XZY:
	    return new BlockPos(fromPos.getX(), fromPos.getZ(), fromPos.getY());

	case YXZ:
	    return new BlockPos(fromPos.getY(), fromPos.getX(), fromPos.getZ());

	case YZX:
	    return new BlockPos(fromPos.getY(), fromPos.getZ(), fromPos.getX());

	case ZXY:
	    return new BlockPos(fromPos.getZ(), fromPos.getX(), fromPos.getY());

	case ZYX:
	    return new BlockPos(fromPos.getZ(), fromPos.getY(), fromPos.getX());

	}
    }
}
