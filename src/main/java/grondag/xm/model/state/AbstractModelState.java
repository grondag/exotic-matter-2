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
package grondag.xm.model.state;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

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
        if (this.hashCode != -1)
            this.hashCode = -1;
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

    /////// INT ARRAY SERIALIZATION /////////

    protected abstract int intSize();

    protected abstract void doDeserializeFromInts(int[] data, int startAt);

    protected abstract void doSerializeToInts(int[] data, int startAt);

    /**
     * Note does not reset state flag - do that if calling on an existing instance.
     */
    protected final void deserializeFromInts(int[] bits) {
        doDeserializeFromInts(bits, 0);
    }


    protected final int[] serializeToInts() {
        int[] result = new int[intSize()];
        doSerializeToInts(result, 0);
        return result;
    }
}
