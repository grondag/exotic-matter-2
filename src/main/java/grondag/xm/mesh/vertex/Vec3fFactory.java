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

import static org.apiguardian.api.API.Status.INTERNAL;

import org.apiguardian.api.API;

import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;

import grondag.xm.api.mesh.polygon.Vec3f;

@API(status = INTERNAL)
public class Vec3fFactory {
	private Vec3fFactory() {}

	private static final Vec3f[] FACES = new Vec3f[6];

	static {
		FACES[Direction.UP.ordinal()] = create(Direction.UP.getVector());
		FACES[Direction.DOWN.ordinal()] = create(Direction.DOWN.getVector());
		FACES[Direction.EAST.ordinal()] = create(Direction.EAST.getVector());
		FACES[Direction.WEST.ordinal()] = create(Direction.WEST.getVector());
		FACES[Direction.NORTH.ordinal()] = create(Direction.NORTH.getVector());
		FACES[Direction.SOUTH.ordinal()] = create(Direction.SOUTH.getVector());
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
