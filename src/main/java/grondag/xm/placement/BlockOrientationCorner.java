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

package grondag.xm.placement;

import grondag.fermion.varia.NBTDictionary;
import grondag.fermion.varia.Useful;
import grondag.xm.api.orientation.CubeCorner;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.PacketByteBuf;

public enum BlockOrientationCorner {
    DYNAMIC(null), MATCH_CLOSEST(null), UP_NORTH_EAST(CubeCorner.UP_NORTH_EAST), UP_NORTH_WEST(CubeCorner.UP_NORTH_WEST),
    UP_SOUTH_EAST(CubeCorner.UP_SOUTH_EAST), UP_SOUTH_WEST(CubeCorner.UP_SOUTH_WEST), DOWN_NORTH_EAST(CubeCorner.DOWN_NORTH_EAST),
    DOWN_NORTH_WEST(CubeCorner.DOWN_NORTH_WEST), DOWN_SOUTH_EAST(CubeCorner.DOWN_SOUTH_EAST), DOWN_SOUTH_WEST(CubeCorner.DOWN_SOUTH_WEST);

    public final CubeCorner corner;

    private static final String TAG_NAME = NBTDictionary.claim("blockOrientCorner");

    private BlockOrientationCorner(CubeCorner corner) {
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

    public String localizedName() {
        return I18n.translate("placement.orientation.corner." + this.name().toLowerCase());
    }

    public boolean isFixed() {
        return !(this == DYNAMIC || this == MATCH_CLOSEST);
    }
}
