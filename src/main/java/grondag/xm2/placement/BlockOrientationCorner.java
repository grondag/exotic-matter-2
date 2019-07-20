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
import grondag.xm2.api.connect.model.BlockCorner;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.client.resource.language.I18n;

public enum BlockOrientationCorner implements ILocalized {
    DYNAMIC(null), MATCH_CLOSEST(null), UP_NORTH_EAST(BlockCorner.UP_NORTH_EAST), UP_NORTH_WEST(BlockCorner.UP_NORTH_WEST),
    UP_SOUTH_EAST(BlockCorner.UP_SOUTH_EAST), UP_SOUTH_WEST(BlockCorner.UP_SOUTH_WEST), DOWN_NORTH_EAST(BlockCorner.DOWN_NORTH_EAST),
    DOWN_NORTH_WEST(BlockCorner.DOWN_NORTH_WEST), DOWN_SOUTH_EAST(BlockCorner.DOWN_SOUTH_EAST), DOWN_SOUTH_WEST(BlockCorner.DOWN_SOUTH_WEST);

    public final BlockCorner corner;

    private static final String TAG_NAME = NBTDictionary.claim("blockOrientCorner");

    private BlockOrientationCorner(BlockCorner corner) {
        this.corner = corner;
    }

    public BlockOrientationCorner deserializeNBT(CompoundTag tag) {
        return Useful.safeEnumFromTag(tag, TAG_NAME, this);
    }

    public void serializeNBT(CompoundTag tag) {
        Useful.saveEnumToTag(tag, TAG_NAME, this);
    }

    public BlockOrientationCorner fromBytes(PacketByteBuf pBuff) {
        return pBuff.readEnumConstant(BlockOrientationCorner.class);
    }

    public void toBytes(PacketByteBuf pBuff) {
        pBuff.writeEnumConstant(this);
    }

    @Override
    public String localizedName() {
        return I18n.translate("placement.orientation.corner." + this.name().toLowerCase());
    }

    public boolean isFixed() {
        return !(this == DYNAMIC || this == MATCH_CLOSEST);
    }
}
