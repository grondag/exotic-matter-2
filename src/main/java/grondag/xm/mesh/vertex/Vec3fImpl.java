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

import grondag.xm.api.mesh.polygon.Vec3f;
import it.unimi.dsi.fastutil.HashCommon;

class Vec3fImpl implements Vec3f {

    static final Vec3fImpl ZERO = new Vec3fImpl(0, 0, 0);
    
    protected float x;
    protected float y;
    protected float z;

    Vec3fImpl(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public boolean isMutable() {
        return false;
    }

    @Override
    public final float x() {
        return x;
    }

    @Override
    public final float y() {
        return y;
    }

    @Override
    public final float z() {
        return z;
    }

    public Mutable mutableCopy() {
        return new Mutable(x, y, z);
    }

    public static class Mutable extends Vec3fImpl {
        public Mutable(float x, float y, float z) {
            super(x, y, z);
        }

        public Mutable load(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
            return this;
        }

        public final Mutable load(Vec3fImpl fromVec) {
            this.x = fromVec.x;
            this.y = fromVec.y;
            this.z = fromVec.z;
            return this;
        }

        @Override
        public boolean isMutable() {
            return true;
        }

        public final Vec3f toImmutable() {
            return Vec3f.create(x, y, z);
        }

        public final Mutable subtract(Vec3fImpl vec) {
            return this.subtract(vec.x, vec.y, vec.z);
        }

        public final Mutable subtract(float x, float y, float z) {
            return this.addVector(-x, -y, -z);
        }

        public final Mutable add(Vec3fImpl vec) {
            return this.addVector(vec.x, vec.y, vec.z);
        }

        public final Mutable addVector(float x, float y, float z) {
            this.x += x;
            this.y += y;
            this.z += z;
            return this;
        }

        public final Mutable scale(float factor) {
            this.x *= factor;
            this.y *= factor;
            this.z *= factor;
            return this;
        }

        public final Mutable invert() {
            this.x = -this.x;
            this.y = -this.y;
            this.z = -this.z;
            return this;
        }

        public final Mutable normalize() {
            final float mag = length();
            if (mag < 1.0E-4F) {
                x = 0;
                y = 0;
                z = 0;
            } else {
                this.x /= mag;
                this.y /= mag;
                this.z /= mag;
            }
            return this;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;

        if (obj == null)
            return false;

        if (obj instanceof Vec3fImpl) {
            Vec3fImpl v = (Vec3fImpl) obj;
            return v.x == x && v.y == y && v.z == z;
        } else
            return false;
    }

    @Override
    public int hashCode() {
        return (int) HashCommon.mix((Float.floatToRawIntBits(x) ^ Float.floatToRawIntBits(z)) | (long)Float.floatToRawIntBits(y) << 32);
    }
}
