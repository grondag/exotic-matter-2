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

package grondag.xm.api.modelstate;

import org.jetbrains.annotations.ApiStatus.Experimental;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;

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

	void fromTag(NbtCompound tag, PaintIndex paintIndex);

	void fromBytes(PacketByteBuf pBuff, PaintIndex sync);
}