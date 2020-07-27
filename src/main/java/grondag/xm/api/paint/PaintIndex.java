/*******************************************************************************
 * Copyright 2020 grondag
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
package grondag.xm.api.paint;

import net.minecraft.world.World;

import grondag.xm.paint.PaintIndexImpl;

public interface PaintIndex {
	XmPaint fromIndex(int paintIndex);

	/**
	 * Will throw an unsupported operation exception if called on client.
	 *
	 * Persists paint with the world save and returns an index that can
	 * later be used to retrieve an anonymous paint instance. These
	 * indexes are not discoverable via the index, and should be
	 * saved and/or organized somehow by the consuming mod.

	 * @param paint to be indexed
	 * @return identical paint with world-persistent index
	 */
	XmPaint index(XmPaint paint);

	/**
	 * Remaps the paint associated with the given index.
	 * Throws an exception if the index does not exist.
	 * @param index
	 * @param paint
	 */
	void updateIndex(int index, XmPaint paint);

	static PaintIndex forWorld(World world) {
		return world == null || world.isClient ? PaintIndexImpl.CLIENT : PaintIndexImpl.SERVER;
	}
}
