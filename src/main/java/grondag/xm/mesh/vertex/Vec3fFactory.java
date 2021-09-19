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
package grondag.xm.mesh.vertex;

import org.jetbrains.annotations.ApiStatus.Internal;
import grondag.xm.api.mesh.polygon.Vec3f;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;

@Internal
public class Vec3fFactory {
	private Vec3fFactory() {}

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
