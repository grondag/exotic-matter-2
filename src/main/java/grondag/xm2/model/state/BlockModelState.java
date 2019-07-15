package grondag.xm2.model.state;

import static grondag.xm2.model.state.ModelStateData.STATE_FLAG_NEEDS_POS;

import grondag.fermion.varia.BitPacker32;
import grondag.fermion.varia.Useful;
import grondag.xm2.api.model.ModelPrimitive;
import grondag.xm2.block.XmBlockRegistryImpl.XmBlockStateImpl;
import it.unimi.dsi.fastutil.HashCommon;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public abstract class BlockModelState extends AbstractModelState {
    /**
     * note that sign bit on core packer is reserved to persist static state during
     * serialization
     */
    private static final BitPacker32<BlockModelState> PACKER_CORE = new BitPacker32<BlockModelState>(m -> m.blockBits,
            (m, b) -> m.blockBits = b);
    private static final BitPacker32<BlockModelState>.IntElement POS_X = PACKER_CORE.createIntElement(256);
    private static final BitPacker32<BlockModelState>.IntElement POS_Y = PACKER_CORE.createIntElement(256);
    private static final BitPacker32<BlockModelState>.IntElement POS_Z = PACKER_CORE.createIntElement(256);
    private static final BitPacker32<BlockModelState>.IntElement SPECIES = PACKER_CORE.createIntElement(16);
    
    private int blockBits;
    
    protected BlockModelState(ModelPrimitive primitive) {
        super(primitive);
    }
    
    @Override
    protected int intSize() {
        return super.intSize() + 1;
    }
    
    @Override
    protected <T extends AbstractModelState> void copyInternal(T template) {
        super.copyInternal(template);
        blockBits = ((BlockModelState)template).blockBits;
    }
    
    @Override
    protected int computeHashCode() {
        return super.computeHashCode() ^ HashCommon.mix(this.blockBits);
    }
    
    public final void refreshFromWorld(XmBlockStateImpl xmState, BlockView world, BlockPos pos) {
        if (this.isStatic) return;
        
        doRefreshFromWorld(xmState, world, pos);
    }
    
    protected void doRefreshFromWorld(XmBlockStateImpl xmState, BlockView world, BlockPos pos) {
        final int stateFlags = stateFlags();
        if ((stateFlags & STATE_FLAG_NEEDS_POS) == STATE_FLAG_NEEDS_POS) {
            POS_X.setValue((pos.getX()), this);
            POS_Y.setValue((pos.getY()), this);
            POS_Z.setValue((pos.getZ()), this);
            invalidateHashCode();
        }
    }
    
    @Override
    public int posX() {
        return POS_X.getValue(this);
    }

    public void posX(int index) {
        POS_X.setValue(index, this);
        invalidateHashCode();
    }

    @Override
    public int posY() {
        return POS_Y.getValue(this);
    }

    public void posY(int index) {
        POS_Y.setValue(index, this);
        invalidateHashCode();
    }

    @Override
    public int posZ() {
        return POS_Z.getValue(this);
    }

    public void posZ(int index) {
        POS_Z.setValue(index, this);
        invalidateHashCode();
    }
    
    @Override
    public int species() {
        return SPECIES.getValue(this);
    }

    public void species(int species) {
        SPECIES.setValue(species, this);
        invalidateHashCode();
    }
    
    protected boolean isStatic = false;

    @Override
    public boolean isStatic() {
        return this.isStatic;
    }

    public void setStatic(boolean isStatic) {
        this.isStatic = isStatic;
    }
    
    @Override
    protected void doSerializeToInts(int[] data, int startAt) {
        data[startAt] = this.isStatic ? (blockBits  | Useful.INT_SIGN_BIT) : blockBits;
        super.doSerializeToInts(data, startAt + 1);
    }
    
    @Override
    protected void doDeserializeFromInts(int[] data, int startAt) {
        // sign on first long word is used to store static indicator
        this.isStatic = (Useful.INT_SIGN_BIT & data[startAt]) == Useful.INT_SIGN_BIT;
        this.blockBits = Useful.INT_SIGN_BIT_INVERSE & data[startAt];
        super.doDeserializeFromInts(data, startAt + 1);
    }
    
    @Override
    protected boolean equalsInner(Object obj) {
        return super.equalsInner(obj) && this.blockBits == ((BlockModelState)obj).blockBits;
    }
    
    public final boolean equalsIncludeStatic(Object obj) {
        if (this == obj)
            return true;

        if (obj instanceof BaseModelState) {
            BaseModelState other = (BaseModelState) obj;
            return this.isStatic == other.isStatic && equalsInner(other);
        } else {
            return false;
        }
    }
    
    @Override
    public void fromBytes(PacketByteBuf pBuff) {
        super.fromBytes(pBuff);
        this.blockBits = pBuff.readInt();
    }

    @Override
    public void toBytes(PacketByteBuf pBuff) {
        super.toBytes(pBuff);
        pBuff.writeInt(this.blockBits);
    }
}
