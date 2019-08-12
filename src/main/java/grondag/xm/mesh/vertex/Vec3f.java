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

import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;

public class Vec3f implements Vertex3f {
//    private static AtomicInteger createCount = new AtomicInteger();
//    private static AtomicInteger initCount = new AtomicInteger();

    public static final Vec3f ZERO = new Vec3f(0, 0, 0);

    private static final Vec3f[] FACES = new Vec3f[6];

    static {
        FACES[Direction.UP.ordinal()] = create(Direction.UP.getVector());
        FACES[Direction.DOWN.ordinal()] = create(Direction.DOWN.getVector());
        FACES[Direction.EAST.ordinal()] = create(Direction.EAST.getVector());
        FACES[Direction.WEST.ordinal()] = create(Direction.WEST.getVector());
        FACES[Direction.NORTH.ordinal()] = create(Direction.NORTH.getVector());
        FACES[Direction.SOUTH.ordinal()] = create(Direction.SOUTH.getVector());
    }

    public static Vec3f forFace(Direction face) {
        return FACES[face.ordinal()];
    }

    public static Vec3f create(Vec3i vec) {
        return create(vec.getX(), vec.getY(), vec.getZ());
    }

    public static Vec3f create(float x, float y, float z) {
//        if((createCount.incrementAndGet() & 0xFFFFF) == 0xFFFFF)
//        {
//            int c = createCount.get();
//            int i = initCount.get();
//            System.out.println("Instance count = " + i);
//            System.out.println("Create count = " + c);
//            System.out.println("Miss rate = " + i * 100 / c);
//            System.out.println("");
//        }
        return Vec3fSimpleLoadingCache.INSTANCE.get(x, y, z);
    }

    protected float x;
    protected float y;
    protected float z;

    Vec3f(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
//        initCount.incrementAndGet();
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

    public static class Mutable extends Vec3f {
//        private static final ArrayBlockingQueue<Mutable> POOL = new ArrayBlockingQueue<>(1024);
//        
//        public static Mutable claim()
//        {
//            Mutable result = POOL.poll();
//            
//            if(result == null)
//                result = new Mutable(0, 0, 0);
//            
//            return result;
//        }
//        
//        public static void release(Mutable vec)
//        {
//            POOL.offer(vec);
//        }

        public Mutable(float x, float y, float z) {
            super(x, y, z);
        }

        public Mutable load(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
            return this;
        }

        public final Mutable load(Vec3f fromVec) {
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
            return create(x, y, z);
        }

        public final Mutable subtract(Vec3f vec) {
            return this.subtract(vec.x, vec.y, vec.z);
        }

        public final Mutable subtract(float x, float y, float z) {
            return this.addVector(-x, -y, -z);
        }

        public final Mutable add(Vec3f vec) {
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

        if (obj instanceof Vec3f) {
            Vec3f v = (Vec3f) obj;
            return v.x == x && v.y == y && v.z == z;
        } else
            return false;
    }
}
