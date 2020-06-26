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

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import org.apiguardian.api.API;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;

import grondag.xm.api.paint.PaintIndex;

@API(status = EXPERIMENTAL)
public interface MutableModelState extends ModelState {
	void release();

	void retain();

	/**
	 * Copies what it can, excluding the primitive, and returns self.
	 */
	MutableModelState copyFrom(ModelState template);

	MutableModelState setStatic(boolean isStatic);

	ModelState releaseToImmutable();

	default void fromTag(CompoundTag tag) {
		fromTag(tag, PaintIndex.LOCAL);
	}

	void fromTag(CompoundTag tag, PaintIndex sync);

	default void fromBytes(PacketByteBuf pBuff) {
		fromBytes(pBuff, PaintIndex.LOCAL);
	}

	void fromBytes(PacketByteBuf pBuff, PaintIndex sync);
}