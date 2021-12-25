/*
 * This file is part of Exotic Matter and is licensed to the project under
 * terms that are compatible with the GNU Lesser General Public License.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership and licensing.
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
 */

package grondag.xm.api.connect.species;

import java.util.Locale;

import org.jetbrains.annotations.ApiStatus.Experimental;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

@Experimental
public enum SpeciesMode {
	MATCH_CLICKED,
	MATCH_MOST,
	COUNTER_MOST;

	private static final String TAG_NAME = "speciesMode";

	private static final SpeciesMode[] VALUES = values();

	public SpeciesMode deserializeNBT(CompoundTag tag) {
		if (tag == null) {
			return this;
		}

		final int ordinal = tag.getInt(TAG_NAME);
		return ordinal < 0 || ordinal >= VALUES.length ? this : VALUES[ordinal];
	}

	public void serializeNBT(CompoundTag tag) {
		tag.putInt(TAG_NAME, this.ordinal());
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
