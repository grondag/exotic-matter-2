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

package grondag.xm.api.connect.species;

import java.util.Locale;

import org.jetbrains.annotations.ApiStatus.Experimental;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

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

	/** mode to use if player holding modifier key. */
	public SpeciesMode alternate() {
		switch (this) {
			case MATCH_CLICKED:
				return COUNTER_MOST;

			case MATCH_MOST:
				return COUNTER_MOST;

			case COUNTER_MOST:
			default:
				return MATCH_CLICKED;
		}
	}
}
