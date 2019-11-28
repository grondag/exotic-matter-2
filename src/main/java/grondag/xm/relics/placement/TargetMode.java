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
package grondag.xm.relics.placement;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.minecraft.client.resource.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.PacketByteBuf;

import grondag.fermion.varia.NBTDictionary;
import grondag.fermion.varia.Useful;

/**
 * Determines how blocks are to be selected for operation of the placement item.
 */
@API(status = Status.DEPRECATED)
@Deprecated
public enum TargetMode {
	/** affect a single block - normal MC behavior */
	ON_CLICKED_FACE(false),

	/** use the placement item's selection region */
	FILL_REGION(true),

	/** use only the exterior blocks the placement item's selection region */
	HOLLOW_REGION(true),

	/**
	 * use the placement item's selection region ONLY if all blocks in region match
	 * the filter criteria
	 */
	COMPLETE_REGION(true),

	/**
	 * flood fill search for blocks that match the clicked block - like an exchanger
	 */
	MATCH_CLICKED_BLOCK(false),

	/**
	 * flood fill of adjacent surfaces that match the clicked block - like a
	 * builder's wand
	 */
	ON_CLICKED_SURFACE(false);

	private static final String TAG_NAME = NBTDictionary.claim("targetMode");

	/**
	 * If true, this mode uses the geometrically-defined volume defined by the
	 * placement item's current selection region. By extension, also determines if
	 * the current filter mode applies. If false, affects a single block or employs
	 * some other logic for determining what blocks are affected.
	 */
	public final boolean usesSelectionRegion;

	private TargetMode(boolean usesSelectionRegion) {
		this.usesSelectionRegion = usesSelectionRegion;
	}

	public TargetMode deserializeNBT(CompoundTag tag) {
		return Useful.safeEnumFromTag(tag, TAG_NAME, this);
	}

	public void serializeNBT(CompoundTag tag) {
		Useful.saveEnumToTag(tag, TAG_NAME, this);
	}

	public TargetMode fromBytes(PacketByteBuf pBuff) {
		return pBuff.readEnumConstant(TargetMode.class);
	}

	public void toBytes(PacketByteBuf pBuff) {
		pBuff.writeEnumConstant(this);
	}

	public String localizedName() {
		return I18n.translate("placement.target_mode." + name().toLowerCase());
	}
}
