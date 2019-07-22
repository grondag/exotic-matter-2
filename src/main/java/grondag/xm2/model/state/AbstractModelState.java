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
package grondag.xm2.model.state;

import grondag.xm2.api.model.ModelPrimitive;
import grondag.xm2.api.model.ModelPrimitiveRegistry;
import grondag.xm2.api.model.ModelState;
import net.minecraft.util.PacketByteBuf;

abstract class AbstractModelState implements ModelState {
    // UGLY: belongs somewhere else
    public static final int MAX_SURFACES = 8;

    protected ModelPrimitive primitive;

    private final int[] paints = new int[MAX_SURFACES];

    private int hashCode = -1;

    /** contains indicators derived from shape and painters */
    private int stateFlags = 0;

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
        System.arraycopy(((AbstractModelState) template).paints, 0, this.paints, 0, this.primitive.surfaces(this).size());
    }

    protected int intSize() {
        return primitive.surfaces(this).size();
    }

    /**
     * Very important to call super and ammend it!
     */
    protected int computeHashCode() {
        int result = 0;
        final int limit = primitive.surfaces(this).size();
        for (int i = 0; i < limit; i++) {
            result ^= paints[i];
        }
        return result;
    }

    protected final void invalidateHashCode() {
        if (this.hashCode != -1)
            this.hashCode = -1;
    }

    @Override
    public ModelPrimitive primitive() {
        return primitive;
    }

    protected final int stateFlags() {
        int result = stateFlags;
        if (result == 0) {
            result = ModelStateVaria.getFlags(this);
            stateFlags = result;
        }
        return result;
    }

    protected final void clearStateFlags() {
        stateFlags = 0;
    }

    protected final int[] serializeToInts() {
        int[] result = new int[intSize()];
        doSerializeToInts(result, 0);
        return result;
    }

    protected void doSerializeToInts(int[] data, int startAt) {
        System.arraycopy(paints, 0, data, startAt, primitive.surfaces(this).size());
    }

    /**
     * Note does not reset state flag - do that if calling on an existing instance.
     */
    protected final void deserializeFromInts(int[] bits) {
        doDeserializeFromInts(bits, 0);
    }

    protected void doDeserializeFromInts(int[] data, int startAt) {
        System.arraycopy(data, startAt, paints, 0, primitive.surfaces(this).size());
    }

    /**
     * Does NOT consider isStatic in comparison. <br>
     * <br>
     * 
     * {@inheritDoc}
     */
    @Override
    public final boolean equals(Object obj) {
        return this == obj ? true : obj != null && obj.getClass() == this.getClass() && equalsInner(obj);
    }

    private boolean doPaintsMatch(AbstractModelState other) {
        final int limit = primitive.surfaces(this).size();
        if (limit == other.primitive.surfaces(other).size()) {
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

    protected boolean equalsInner(Object obj) {
        final AbstractModelState other = (AbstractModelState) obj;
        return this.primitive == other.primitive && doPaintsMatch(other);
    }

    @Override
    public boolean doesAppearanceMatch(ModelState other) {
        return other != null && other instanceof AbstractModelState && doPaintsMatch((AbstractModelState) other);
    }

    public void fromBytes(PacketByteBuf pBuff) {
        this.primitive = ModelPrimitiveRegistry.INSTANCE.get(pBuff.readVarInt());
        final int limit = primitive.surfaces(this).size();
        for (int i = 0; i < limit; i++) {
            this.paints[i] = pBuff.readVarInt();
        }
    }

    @Override
    public void toBytes(PacketByteBuf pBuff) {
        pBuff.writeVarInt(primitive.index());
        final int limit = primitive.surfaces(this).size();
        for (int i = 0; i < limit; i++) {
            pBuff.writeVarInt(paints[i]);
        }
    }

    public final void paint(int surfaceIndex, int paintIndex) {
        paints[surfaceIndex] = paintIndex;
    }

    @Override
    public final int paintIndex(int surfaceIndex) {
        return paints[surfaceIndex];
    }
}
