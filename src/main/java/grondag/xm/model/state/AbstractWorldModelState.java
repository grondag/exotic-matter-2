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

import grondag.fermion.bits.BitPacker32;
import grondag.fermion.varia.Useful;
import it.unimi.dsi.fastutil.HashCommon;
import net.minecraft.util.math.BlockPos;

abstract class AbstractWorldModelState extends AbstractModelState {
    /**
     * note that sign bit on core packer is reserved to persist static state during
     * serialization
     */
    private static final BitPacker32<AbstractWorldModelState> WORLD_BITS = new BitPacker32<AbstractWorldModelState>(m -> m.blockBits,
            (m, b) -> m.blockBits = b);
    private static final BitPacker32<AbstractWorldModelState>.IntElement POS_X = WORLD_BITS.createIntElement(256);
    private static final BitPacker32<AbstractWorldModelState>.IntElement POS_Y = WORLD_BITS.createIntElement(256);
    private static final BitPacker32<AbstractWorldModelState>.IntElement POS_Z = WORLD_BITS.createIntElement(256);
    protected static final BitPacker32<AbstractWorldModelState>.IntElement SPECIES = WORLD_BITS.createIntElement(16);

    protected int blockBits;

    @Override
    protected int intSize() {
        return super.intSize() + 1;
    }

    @Override
    protected <T extends AbstractModelState> void copyInternal(T template) {
        super.copyInternal(template);
        blockBits = ((AbstractWorldModelState) template).blockBits;
    }

    @Override
    protected int computeHashCode() {
        return super.computeHashCode() ^ HashCommon.mix(this.blockBits);
    }

    public int posX() {
        return POS_X.getValue(this);
    }

    protected final void posXInner(int index) {
        POS_X.setValue(index, this);
        invalidateHashCode();
    }

    public int posY() {
        return POS_Y.getValue(this);
    }

    protected final void posYInner(int index) {
        POS_Y.setValue(index, this);
        invalidateHashCode();
    }

    public int posZ() {
        return POS_Z.getValue(this);
    }

    protected final void posZInner(int index) {
        POS_Z.setValue(index, this);
        invalidateHashCode();
    }

    protected final void posInner(BlockPos pos) {
        POS_X.setValue((pos.getX()), this);
        POS_Y.setValue((pos.getY()), this);
        POS_Z.setValue((pos.getZ()), this);
        invalidateHashCode();
    }
    
    protected boolean isStatic = false;

    public boolean isStatic() {
        return this.isStatic;
    }

    protected final void setStaticInner(boolean isStatic) {
        this.isStatic = isStatic;
    }

    @Override
    protected void doSerializeToInts(int[] data, int startAt) {
        data[startAt] = this.isStatic ? (blockBits | Useful.INT_SIGN_BIT) : blockBits;
        super.doSerializeToInts(data, startAt + 1);
    }

    @Override
    protected void doDeserializeFromInts(int[] data, int startAt) {
        // sign on first long word is used to store static indicator
        this.isStatic = (Useful.INT_SIGN_BIT & data[startAt]) == Useful.INT_SIGN_BIT;
        this.blockBits = Useful.INT_SIGN_BIT_INVERSE & data[startAt];
        super.doDeserializeFromInts(data, startAt + 1);
    }
}
