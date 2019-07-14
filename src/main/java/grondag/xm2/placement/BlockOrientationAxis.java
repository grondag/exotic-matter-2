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
import net.minecraft.util.math.Direction;
import net.minecraft.client.resource.language.I18n;

public enum BlockOrientationAxis implements ILocalized {

    DYNAMIC(null), MATCH_CLOSEST(null), X(Direction.Axis.X), Y(Direction.Axis.Y), Z(Direction.Axis.Z);

    private static final String TAG_ORIENTATION = NBTDictionary.claim("plcmnt_orient");

    public final Direction.Axis axis;

    private BlockOrientationAxis(Direction.Axis axis) {
        this.axis = axis;
    }

    public BlockOrientationAxis deserializeNBT(CompoundTag tag) {
        return Useful.safeEnumFromTag(tag, TAG_ORIENTATION, this);
    }

    public void serializeNBT(CompoundTag tag) {
        Useful.saveEnumToTag(tag, TAG_ORIENTATION, this);
    }

    public BlockOrientationAxis fromBytes(PacketByteBuf pBuff) {
        return pBuff.readEnumConstant(BlockOrientationAxis.class);
    }

    public void toBytes(PacketByteBuf pBuff) {
        pBuff.writeEnumConstant(this);
    }

    @Override
    public String localizedName() {
        return I18n.translate("placement.orientation.axis." + this.name().toLowerCase());
    }

    public boolean isFixed() {
        return !(this == DYNAMIC
                || this == MATCH_CLOSEST);
    }
}
