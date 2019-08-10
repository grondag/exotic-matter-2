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

import grondag.xm.api.modelstate.ModelState;
import grondag.xm.api.paint.XmPaint;
import grondag.xm.api.paint.XmPaintRegistry;
import grondag.xm.api.surface.XmSurface;

abstract class AbstractModelState {
    
    private static final AtomicIntegerFieldUpdater<AbstractModelState> retainCountUpdater =
            AtomicIntegerFieldUpdater.newUpdater(AbstractModelState.class, "refCount");

    private volatile int refCount = 0;
    
    protected boolean isImmutable = true;
    
    public final int refCount() {
        return refCount; 
    }
    
    public final boolean isImmutable() {
        return isImmutable;
    }
    
    protected final void confirmMutable() {
        if(isImmutable) {
            throw new UnsupportedOperationException("Encounted attempt to modify immutable model state.");
        }
    }
    public void retain() {
        confirmMutable();
        retainCountUpdater.getAndIncrement(this);
    }
    
    public void release() {
        confirmMutable();
        final int oldCount = retainCountUpdater.getAndDecrement(this);
        if(oldCount == 1) {
            onLastRelease();
        } else if (oldCount <= 0) {
            retainCountUpdater.getAndIncrement(this);
            throw new IllegalStateException("Encountered attempt to release an unreferenced ModelState instance.");
        }
    }
    
    protected void onLastRelease() {}
    
    // UGLY: belongs somewhere else
    public static final int MAX_SURFACES = 8;

    
    protected final int[] paints = new int[MAX_SURFACES];

    private int hashCode = -1;

    @Override
    public final int hashCode() {
        int result = hashCode;
        if (result == -1) {
            result = computeHashCode();
            hashCode = result;
        }
        return result;
    }

    protected <T extends AbstractModelState> void copyInternal(T template) {
        System.arraycopy(((AbstractModelState) template).paints, 0, this.paints, 0, this.surfaceCount());
    }

    protected abstract int surfaceCount();
    
    protected int intSize() {
        return surfaceCount();
    }

    /**
     * Very important to call super and ammend it!
     */
    protected int computeHashCode() {
        int result = 0;
        final int limit = this.surfaceCount();
        for (int i = 0; i < limit; i++) {
            result ^= paints[i];
        }
        return result;
    }

    protected final void invalidateHashCode() {
        if (this.hashCode != -1)
            this.hashCode = -1;
    }

    protected final int[] serializeToInts() {
        int[] result = new int[intSize()];
        doSerializeToInts(result, 0);
        return result;
    }

    protected void doSerializeToInts(int[] data, int startAt) {
        System.arraycopy(paints, 0, data, startAt, surfaceCount());
    }

    /**
     * Note does not reset state flag - do that if calling on an existing instance.
     */
    protected final void deserializeFromInts(int[] bits) {
        doDeserializeFromInts(bits, 0);
    }

    protected void doDeserializeFromInts(int[] data, int startAt) {
        System.arraycopy(data, startAt, paints, 0, surfaceCount());
    }

    public final boolean doPaintsMatch(AbstractModelState other) {
        final int limit = surfaceCount();
        if (limit == other.surfaceCount()) {
            final int[] paints = this.paints;
            final int[] otherPaints = other.paints;
            for (int i = 0; i < limit; i++) {
                if (otherPaints[i] != paints[i]) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns true if visual elements match. Does not consider species or geometry
     * in matching.
     */
    public final boolean doesAppearanceMatch(ModelState other) {
        return other != null && other instanceof AbstractModelState && doPaintsMatch((AbstractModelState) other);
    }

    protected final void paintInner(int surfaceIndex, int paintIndex) {
        paints[surfaceIndex] = paintIndex;
    }

    public final int paintIndex(int surfaceIndex) {
        return paints[surfaceIndex];
    }
    
    public final XmPaint paint(int surfaceIndex) {
        return XmPaintRegistry.INSTANCE.get(paintIndex(surfaceIndex));
    }

    public final XmPaint paint(XmSurface surface) {
        return XmPaintRegistry.INSTANCE.get(paintIndex(surface.ordinal()));
    }
}
