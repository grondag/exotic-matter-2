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
import net.minecraft.client.resource.language.I18n;

public enum SpeciesMode implements ILocalized {
    MATCH_CLICKED, MATCH_MOST, COUNTER_MOST;

    private static final String TAG_NAME = NBTDictionary.claim("speciesMode");

    public SpeciesMode deserializeNBT(CompoundTag tag) {
	return Useful.safeEnumFromTag(tag, TAG_NAME, this);
    }

    public void serializeNBT(CompoundTag tag) {
	Useful.saveEnumToTag(tag, TAG_NAME, this);
    }

    public SpeciesMode fromBytes(PacketByteBuf pBuff) {
	return pBuff.readEnumConstant(SpeciesMode.class);
    }

    public void toBytes(PacketByteBuf pBuff) {
	pBuff.writeEnumConstant(this);
    }

    @Override
    public String localizedName() {
	return I18n.translate("placement.species_mode." + this.name().toLowerCase());
    }

    /** mode to use if player holding alt key */
    public SpeciesMode alternate() {
	switch (this) {
	case COUNTER_MOST:
	default:
	    return MATCH_CLICKED;

	case MATCH_CLICKED:
	    return COUNTER_MOST;

	case MATCH_MOST:
	    return COUNTER_MOST;

	}
    }
}
