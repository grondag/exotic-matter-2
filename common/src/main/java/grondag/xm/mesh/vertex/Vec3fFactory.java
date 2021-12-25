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

package grondag.xm.mesh.vertex;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;

import grondag.xm.api.mesh.polygon.Vec3f;

@Internal
public class Vec3fFactory {
	private Vec3fFactory() { }

	private static final Vec3f[] FACES = new Vec3f[6];

	static {
		FACES[Direction.UP.ordinal()] = create(Direction.UP.getNormal());
		FACES[Direction.DOWN.ordinal()] = create(Direction.DOWN.getNormal());
		FACES[Direction.EAST.ordinal()] = create(Direction.EAST.getNormal());
		FACES[Direction.WEST.ordinal()] = create(Direction.WEST.getNormal());
		FACES[Direction.NORTH.ordinal()] = create(Direction.NORTH.getNormal());
		FACES[Direction.SOUTH.ordinal()] = create(Direction.SOUTH.getNormal());
	}

	public static final Vec3f ZERO = Vec3fImpl.ZERO;

	public static Vec3f forFace(Direction face) {
		return FACES[face.ordinal()];
	}

	public static Vec3f create(Vec3i vec) {
		return create(vec.getX(), vec.getY(), vec.getZ());
	}

	public static Vec3f create(float x, float y, float z) {
		return Vec3fCache.INSTANCE.get(x, y, z);
	}
}
