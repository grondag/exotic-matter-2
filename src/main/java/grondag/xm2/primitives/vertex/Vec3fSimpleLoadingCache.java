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

package grondag.xm2.primitives.vertex;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import grondag.fermion.cache.ISimpleLoadingCache;
import grondag.fermion.varia.Useful;

class Vec3fSimpleLoadingCache {
    static final Vec3fSimpleLoadingCache INSTANCE = new Vec3fSimpleLoadingCache(0x80000);

    public final int capacity;
    public final int maxFill;
    protected final int positionMask;

    private final AtomicInteger backupMissCount = new AtomicInteger(0);

    protected volatile Vec3fCacheState activeState;
    private final AtomicReference<Vec3fCacheState> backupState = new AtomicReference<Vec3fCacheState>();

    private final Object writeLock = new Object();

    private Vec3fSimpleLoadingCache(int maxSize) {
        this.capacity = 1 << (Integer.SIZE
                - Integer.numberOfLeadingZeros((int) (maxSize / ISimpleLoadingCache.LOAD_FACTOR)));
        this.maxFill = (int) (capacity * ISimpleLoadingCache.LOAD_FACTOR);
        this.positionMask = capacity - 1;
        this.activeState = new Vec3fCacheState(this.capacity);
        this.clear();
    }

    public int size() {
        return activeState.size.get();
    }

    public void clear() {
        this.activeState = new Vec3fCacheState(this.capacity);
    }

    public Vec3f get(final float x, final float y, final float z) {
        Vec3fCacheState localState = activeState;

        // Zero value normally indicates an unused spot in key array
        // so requires privileged handling to prevent search weirdness.
        if (x == 0f && y == 0f && z == 0f)
            return Vec3f.ZERO;

        final long hash = ((long) Float.floatToIntBits(x)) ^ (((long) Float.floatToIntBits(y)) << 16)
                ^ (((long) Float.floatToIntBits(z)) << 32);

        int position = (int) (Useful.longHash(hash) & positionMask);

        do {
            Vec3f check = localState.values[position];

            if (check == null)
                return load(localState, x, y, z, position);

            else if (check.x() == x && check.y() == y && check.z() == z)
                return check;

            position = (position + 1) & positionMask;

        } while (true);
    }

    protected Vec3f loadFromBackup(Vec3fCacheState backup, final float x, final float y, final float z, int position) {
        do {
            Vec3f v = backup.values[position];
            if (v != null && v.x() == x && v.y() == y && v.z() == z)
                return v;

            if (v == null) {
                if ((backupMissCount.incrementAndGet() & 0xFF) == 0xFF) {
                    if (backupMissCount.get() > activeState.size.get() / 2) {
                        backupState.compareAndSet(backup, null);
                    }
                }
                return new Vec3f(x, y, z);
            }
            position = (position + 1) & positionMask;
        } while (true);
    }

    protected Vec3f load(Vec3fCacheState localState, final float x, final float y, final float z, int position) {
        // no need to handle zero key here - is handled as privileged case in get();

        Vec3fCacheState backupState = this.backupState.get();

        final Vec3f result = backupState == null ? new Vec3f(x, y, z) : loadFromBackup(backupState, x, y, z, position);
        do {
            Vec3f currentKey;
            synchronized (writeLock) {
                currentKey = localState.values[position];
                if (currentKey == null) {
                    // write value start in case another thread tries to read it before we can write
                    // it
                    localState.values[position] = result;
                    break;
                }
            }

            // small chance another thread added our value before we got our lock
            if (currentKey.x() == result.x() && currentKey.y() == result.y() && currentKey.z() == result.z())
                return currentKey;

            position = (position + 1) & positionMask;

        } while (true);

        if (localState.size.incrementAndGet() == this.maxFill) {
            Vec3fCacheState newState = new Vec3fCacheState(this.capacity);
            this.backupState.set(this.activeState);
            this.activeState = newState;
            this.backupMissCount.set(0);
        }

        return result;
    }
}
