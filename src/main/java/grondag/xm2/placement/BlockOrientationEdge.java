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
import grondag.xm2.api.connect.model.BlockEdge;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.client.resource.language.I18n;

public enum BlockOrientationEdge implements ILocalized {
    DYNAMIC(null), 
    MATCH_CLOSEST(null), 
    UP_EAST(BlockEdge.UP_EAST), 
    UP_WEST(BlockEdge.UP_WEST),
    UP_NORTH(BlockEdge.UP_NORTH), 
    UP_SOUTH(BlockEdge.UP_SOUTH), 
    NORTH_EAST(BlockEdge.NORTH_EAST),
    NORTH_WEST(BlockEdge.NORTH_WEST), 
    SOUTH_EAST(BlockEdge.SOUTH_EAST), 
    SOUTH_WEST(BlockEdge.SOUTH_WEST),
    DOWN_EAST(BlockEdge.DOWN_EAST), 
    DOWN_WEST(BlockEdge.DOWN_WEST), 
    DOWN_NORTH(BlockEdge.DOWN_NORTH),
    DOWN_SOUTH(BlockEdge.DOWN_SOUTH);

    public final BlockEdge edge;

    private static final String TAG_NAME = NBTDictionary.claim("blockOrientEdge");

    private BlockOrientationEdge(BlockEdge edge) {
        this.edge = edge;
    }

    public BlockOrientationEdge deserializeNBT(CompoundTag tag) {
        return Useful.safeEnumFromTag(tag, TAG_NAME, this);
    }

    public void serializeNBT(CompoundTag tag) {
        Useful.saveEnumToTag(tag, TAG_NAME, this);
    }

    public BlockOrientationEdge fromBytes(PacketByteBuf pBuff) {
        return pBuff.readEnumConstant(BlockOrientationEdge.class);
    }

    public void toBytes(PacketByteBuf pBuff) {
        pBuff.writeEnumConstant(this);
    }

    @Override
    public String localizedName() {
        return I18n.translate("placement.orientation.edge." + this.name().toLowerCase());
    }

    public boolean isFixed() {
        return !(this == DYNAMIC || this == MATCH_CLOSEST);
    }
}
