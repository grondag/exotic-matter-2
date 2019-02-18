package grondag.brocade.world;

import net.minecraft.util.math.Direction;

public class CornerJoinBlockState
{
    private final int index;
    
    /** join state considering only direct neighbors */
    public final SimpleJoin simpleJoin;
    
    private byte faceJoinIndex[] = new byte[6];
    
    CornerJoinBlockState(int index, SimpleJoin simpleJoin)
    {
        this.index = index;
        this.simpleJoin = simpleJoin;
    }
    
    public int getIndex()
    {
        return index;
    }
    
    void setFaceJoinState(Direction face, CornerJoinFaceState state)
    {
        faceJoinIndex[face.ordinal()]=(byte)state.ordinal();
    }
    
    public CornerJoinFaceState getFaceJoinState(Direction face)
    {
        return CornerJoinFaceState.values()[faceJoinIndex[face.ordinal()]];
    }
}