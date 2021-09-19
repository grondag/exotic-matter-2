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
package grondag.xm.api.connect.species;

import java.util.Locale;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.ApiStatus.Experimental;
import grondag.fermion.varia.NBTDictionary;
import grondag.fermion.varia.Useful;

@Experimental
public enum SpeciesMode {
	MATCH_CLICKED,
	MATCH_MOST,
	COUNTER_MOST;

	private static final String TAG_NAME = NBTDictionary.GLOBAL.claim("speciesMode");

	public SpeciesMode deserializeNBT(CompoundTag tag) {
		return Useful.safeEnumFromTag(tag, TAG_NAME, this);
	}

	public void serializeNBT(CompoundTag tag) {
		Useful.saveEnumToTag(tag, TAG_NAME, this);
	}

	public SpeciesMode fromBytes(FriendlyByteBuf pBuff) {
		return pBuff.readEnum(SpeciesMode.class);
	}

	public void toBytes(FriendlyByteBuf pBuff) {
		pBuff.writeEnum(this);
	}

	public String localizedName() {
		return I18n.get("placement.species_mode." + name().toLowerCase(Locale.ROOT));
	}

	/** mode to use if player holding modifier key */
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
