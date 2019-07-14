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

package grondag.xm2.dispatch;

import java.util.TreeSet;
import java.util.function.Consumer;

import grondag.fermion.varia.Useful;
import grondag.xm2.mesh.polygon.IPolygon;
import grondag.xm2.mesh.vertex.IVec3f;
import net.minecraft.util.math.Direction;

public class QuadListKeyBuilder implements Consumer<IPolygon> {
    private static ThreadLocal<QuadListKeyBuilder> locals = new ThreadLocal<QuadListKeyBuilder>() {
	@Override
	protected QuadListKeyBuilder initialValue() {
	    return new QuadListKeyBuilder();
	}
    };

    public static QuadListKeyBuilder prepareThreadLocal(Direction forFace) {
	return locals.get().prepare(forFace);
    }

    private int axis0;
    private int axis1;

    private TreeSet<Long> vertexKeys = new TreeSet<Long>();

    private QuadListKeyBuilder() {
    }

    private QuadListKeyBuilder prepare(Direction face) {
	vertexKeys.clear();
	switch (face.getAxis()) {
	case X:
	    axis0 = 1;
	    axis1 = 2;
	    break;
	case Y:
	    axis0 = 0;
	    axis1 = 2;
	    break;
	case Z:
	default:
	    axis0 = 0;
	    axis1 = 1;
	    break;
	}
	return this;
    }

    /** call after piping vertices into this instance */
    public int getQuadListKey() {
	long key = 0L;
	for (Long vk : vertexKeys) {
	    key += Useful.longHash(vk);
	}
	return (int) (key & 0xFFFFFFFF);
    }

    private void acceptVertex(float x, float y, float z) {
	float v0 = 0, v1 = 0;
	switch (axis0) {
	case 0:
	    v0 = x;
	    break;
	case 1:
	    v0 = y;
	    break;
	case 2:
	    v0 = z;
	    break;
	}

	switch (axis1) {
	case 0:
	    v1 = x;
	    break;
	case 1:
	    v1 = y;
	    break;
	case 2:
	    v1 = z;
	    break;
	}
	// don't need to check which element - position is the only one included
	vertexKeys.add(((long) (Float.floatToRawIntBits(v0)) | ((long) (Float.floatToRawIntBits(v1)) << 32)));
    }

    @Override
    public void accept(IPolygon t) {
	final int limit = t.vertexCount();
	for (int i = 0; i < limit; i++) {
	    IVec3f v = t.getPos(i);
	    this.acceptVertex(v.x(), v.y(), v.z());
	}
    }
}
