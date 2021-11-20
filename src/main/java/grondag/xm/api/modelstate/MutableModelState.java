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

package grondag.xm.api.modelstate;

import org.jetbrains.annotations.ApiStatus.Experimental;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

import grondag.xm.api.paint.PaintIndex;

@Experimental
public interface MutableModelState extends ModelState {
	void release();

	void retain();

	/**
	 * Copies what it can, excluding the primitive, and returns self.
	 */
	MutableModelState copyFrom(ModelState template);

	MutableModelState setStatic(boolean isStatic);

	ModelState releaseToImmutable();

	void fromTag(CompoundTag tag, PaintIndex paintIndex);

	void fromBytes(FriendlyByteBuf pBuff, PaintIndex sync);
}
