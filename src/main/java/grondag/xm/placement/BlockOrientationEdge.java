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
import grondag.xm.api.orientation.Edge;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.PacketByteBuf;

public enum BlockOrientationEdge {
    DYNAMIC(null), MATCH_CLOSEST(null), UP_EAST(Edge.UP_EAST), UP_WEST(Edge.UP_WEST), UP_NORTH(Edge.UP_NORTH), UP_SOUTH(Edge.UP_SOUTH),
    NORTH_EAST(Edge.NORTH_EAST), NORTH_WEST(Edge.NORTH_WEST), SOUTH_EAST(Edge.SOUTH_EAST), SOUTH_WEST(Edge.SOUTH_WEST),
    DOWN_EAST(Edge.DOWN_EAST), DOWN_WEST(Edge.DOWN_WEST), DOWN_NORTH(Edge.DOWN_NORTH), DOWN_SOUTH(Edge.DOWN_SOUTH);

    public final Edge edge;

    private static final String TAG_NAME = NBTDictionary.claim("blockOrientEdge");

    private BlockOrientationEdge(Edge edge) {
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

    public String localizedName() {
        return I18n.translate("placement.orientation.edge." + this.name().toLowerCase());
    }

    public boolean isFixed() {
        return !(this == DYNAMIC || this == MATCH_CLOSEST);
    }
}
