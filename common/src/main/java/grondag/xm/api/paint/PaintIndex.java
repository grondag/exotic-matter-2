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

package grondag.xm.api.paint;

import net.minecraft.world.level.Level;

import grondag.xm.paint.PaintIndexImpl;

public interface PaintIndex {
	XmPaint fromIndex(int paintIndex);

	/**
	 * Will throw an unsupported operation exception if called on client.
	 *
	 * <p>Persists paint with the world save and returns an index that can
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

	static PaintIndex forWorld(Level world) {
		return world == null || world.isClientSide ? PaintIndexImpl.CLIENT : PaintIndexImpl.SERVER;
	}
}
