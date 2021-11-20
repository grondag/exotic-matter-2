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

package grondag.xm.modelstate;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
abstract class AbstractModelState {
	/////// REFERENCE COUNTING /////////

	private static final AtomicIntegerFieldUpdater<AbstractModelState> retainCountUpdater = AtomicIntegerFieldUpdater.newUpdater(AbstractModelState.class,
			"refCount");

	private volatile int refCount = 0;

	public final int refCount() {
		return refCount;
	}

	protected abstract void onLastRelease();

	public void release() {
		confirmMutable();
		final int oldCount = retainCountUpdater.getAndDecrement(this);

		if (oldCount == 1) {
			onLastRelease();
		} else if (oldCount <= 0) {
			retainCountUpdater.getAndIncrement(this);
			throw new IllegalStateException("Encountered attempt to release an unreferenced ModelState instance.");
		}
	}

	public void retain() {
		confirmMutable();
		retainCountUpdater.getAndIncrement(this);
	}

	/////// HASH CODE /////////

	private int hashCode = -1;

	protected abstract int computeHashCode();

	@Override
	public final int hashCode() {
		int result = hashCode;

		if (result == -1) {
			result = computeHashCode();
			hashCode = result;
		}

		return result;
	}

	protected final void invalidateHashCode() {
		if (hashCode != -1) {
			hashCode = -1;
		}
	}

	/////// MUTABILITY /////////

	protected boolean isImmutable = true;

	public final boolean isImmutable() {
		return isImmutable;
	}

	protected final void confirmMutable() {
		if (isImmutable) {
			throw new UnsupportedOperationException("Encounted attempt to modify immutable model state.");
		}
	}
}
