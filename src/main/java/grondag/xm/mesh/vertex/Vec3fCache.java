/*
 * Copyright © Original Authors
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
 *
 * Additional copyright and licensing notices may apply for content that was
 * included from other projects. For more information, see ATTRIBUTION.md.
 */

package grondag.xm.mesh.vertex;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import it.unimi.dsi.fastutil.HashCommon;
import org.jetbrains.annotations.ApiStatus.Internal;

import io.vram.sc.cache.ISimpleLoadingCache;

import grondag.xm.api.mesh.polygon.Vec3f;

// PERF: sucks - avoid using instanced immutable vectors in more places
@Internal
public class Vec3fCache {
	static final Vec3fCache INSTANCE = new Vec3fCache(0x10000);

	public final int capacity;
	public final int maxFill;
	protected final int positionMask;

	private final AtomicInteger backupMissCount = new AtomicInteger(0);

	protected volatile Vec3fCacheState activeState;
	private final AtomicReference<Vec3fCacheState> backupState = new AtomicReference<>();

	private final Object writeLock = new Object();

	private Vec3fCache(int maxSize) {
		capacity = 1 << (Integer.SIZE - Integer.numberOfLeadingZeros((int) (maxSize / ISimpleLoadingCache.LOAD_FACTOR)));
		maxFill = (int) (capacity * ISimpleLoadingCache.LOAD_FACTOR);
		positionMask = capacity - 1;
		activeState = new Vec3fCacheState(capacity);
		clear();
	}

	public int size() {
		return activeState.size.get();
	}

	public void clear() {
		activeState = new Vec3fCacheState(capacity);
	}

	public Vec3f get(final float x, final float y, final float z) {
		final Vec3fCacheState localState = activeState;

		// Zero value normally indicates an unused spot in key array
		// so requires privileged handling to prevent search weirdness.
		if (x == 0f && y == 0f && z == 0f) {
			return Vec3fImpl.ZERO;
		}

		final long hash = (Float.floatToIntBits(x)) ^ (((long) Float.floatToIntBits(y)) << 16) ^ (((long) Float.floatToIntBits(z)) << 32);

		int position = (int) (HashCommon.mix(hash) & positionMask);

		do {
			final Vec3fImpl check = localState.values[position];

			if (check == null) {
				return load(localState, x, y, z, position);
			} else if (check.x() == x && check.y() == y && check.z() == z) {
				return check;
			}

			position = (position + 1) & positionMask;
		} while (true);
	}

	protected Vec3fImpl loadFromBackup(Vec3fCacheState backup, final float x, final float y, final float z, int position) {
		do {
			final Vec3fImpl v = backup.values[position];

			if (v != null && v.x() == x && v.y() == y && v.z() == z) {
				return v;
			}

			if (v == null) {
				if ((backupMissCount.incrementAndGet() & 0xFF) == 0xFF) {
					if (backupMissCount.get() > activeState.size.get() / 2) {
						backupState.compareAndSet(backup, null);
					}
				}

				return new Vec3fImpl(x, y, z);
			}

			position = (position + 1) & positionMask;
		} while (true);
	}

	protected Vec3fImpl load(Vec3fCacheState localState, final float x, final float y, final float z, int position) {
		// no need to handle zero key here - is handled as privileged case in get();
		final Vec3fCacheState backupState = this.backupState.get();
		final Vec3fImpl result = backupState == null ? new Vec3fImpl(x, y, z) : loadFromBackup(backupState, x, y, z, position);

		do {
			Vec3fImpl currentKey;
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
			if (currentKey.x() == result.x() && currentKey.y() == result.y() && currentKey.z() == result.z()) {
				return currentKey;
			}

			position = (position + 1) & positionMask;
		} while (true);

		if (localState.size.incrementAndGet() == maxFill) {
			final Vec3fCacheState newState = new Vec3fCacheState(capacity);
			this.backupState.set(activeState);
			activeState = newState;
			backupMissCount.set(0);
		}

		return result;
	}

	private static class Vec3fCacheState {
		protected AtomicInteger size = new AtomicInteger(0);
		protected final Vec3fImpl[] values;

		Vec3fCacheState(int capacityIn) {
			values = new Vec3fImpl[capacityIn];
		}
	}
}
