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

import grondag.fermion.varia.NBTDictionary;
import grondag.fermion.varia.Useful;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.math.Direction;

public enum BlockOrientationFace {
    DYNAMIC(null), MATCH_CLOSEST(null), UP(Direction.UP), DOWN(Direction.DOWN), NORTH(Direction.NORTH), EAST(Direction.EAST), SOUTH(Direction.SOUTH),
    WEST(Direction.WEST);

    private static final String TAG_NAME = NBTDictionary.claim("blockOrientFace");

    public final Direction face;

    private BlockOrientationFace(Direction face) {
        this.face = face;
    }

    public BlockOrientationFace deserializeNBT(CompoundTag tag) {
        return Useful.safeEnumFromTag(tag, TAG_NAME, this);
    }

    public void serializeNBT(CompoundTag tag) {
        Useful.saveEnumToTag(tag, TAG_NAME, this);
    }

    public BlockOrientationFace fromBytes(PacketByteBuf pBuff) {
        return pBuff.readEnumConstant(BlockOrientationFace.class);
    }

    public void toBytes(PacketByteBuf pBuff) {
        pBuff.writeEnumConstant(this);
    }

    public String localizedName() {
        return I18n.translate("placement.orientation.face." + this.name().toLowerCase());
    }

    public boolean isFixed() {
        return !(this == DYNAMIC || this == MATCH_CLOSEST);
    }
}
