package grondag.xm2.model.state;

import grondag.xm2.api.model.ModelPrimitive;
import grondag.xm2.api.model.ModelState;
import net.minecraft.util.PacketByteBuf;

public abstract class AbstractModelState implements ModelState {
    protected final ModelPrimitive primitive;
    
    protected final int[] paints;
    
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
        System.arraycopy(template.paints, 0, this.paints, 0, paints.length);
    }
    
    protected int intSize() {
        return paints.length;
    }
    
    /** 
     * Very important to call super and ammend it!
     */
    protected int computeHashCode() {
        int result = 0;
        final int limit = paints.length;
        for(int i = 0; i < limit; i++) {
            result ^= paints[i];
        }
        return result;
    }
    
    protected final void invalidateHashCode() {
        if (this.hashCode != -1)
            this.hashCode = -1;
    }
    
    protected AbstractModelState(ModelPrimitive primitive) {
        this.primitive = primitive;
        paints = new int[primitive.surfaces().size()];
    }
    
    @Override
    public ModelPrimitive primitive() {
        return primitive;
    }
    
    protected final int stateFlags() {
        int result = stateFlags;
        if (result == 0) {
            result = ModelStateData.getFlags(this);
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
        System.arraycopy(paints, 0, data, startAt, paints.length);
    }
    
    /**
     * Note does not reset state flag - do that if calling on an existing instance.
     */
    protected final void deserializeFromInts(int[] bits) {
        doSerializeToInts(bits, 0);
    }
    
    protected void doDeserializeFromInts(int[] data, int startAt) {
        System.arraycopy(data, startAt, paints, 0, paints.length);
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
        final int[] paints = this.paints;
        final int limit = paints.length;
        final int[] otherPaints = other.paints;
        
        if(limit == otherPaints.length) {
            for(int i = 0; i < limit; i++) {
                if(otherPaints[i] != paints[i]) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }
    
    protected boolean equalsInner(Object obj) {
        final AbstractModelState other = (AbstractModelState)obj;
        return this.primitive == other.primitive && doPaintsMatch(other);
    }
    
    @Override
    public boolean doesAppearanceMatch(ModelState other) {
        return other != null && other instanceof AbstractModelState && doPaintsMatch((AbstractModelState)other);
    }
    
    public void fromBytes(PacketByteBuf pBuff) {
        final int limit = paints.length;
        for(int i = 0; i < limit; i++) {
            this.paints[i] = pBuff.readVarInt();
        }
    }

    public void toBytes(PacketByteBuf pBuff) {
        final int limit = paints.length;
        for(int i = 0; i < limit; i++) {
            pBuff.writeVarInt(paints[i]);
        }
    }
}
