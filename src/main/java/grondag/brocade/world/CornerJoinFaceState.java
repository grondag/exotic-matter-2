package grondag.brocade.world;

import java.util.ArrayList;

import net.minecraft.util.math.Direction;

/**
 * Corner bits indicate that a corner is needed, not that the corner is present. (These are normally inverse.)
 */
public enum CornerJoinFaceState
{
    NO_FACE(0, 0),
    NONE(0, 0), //must be after NO_FACE, overwrites NO_FACE in lookup table, should never be checked by lookup
    TOP(FaceSide.TOP.bitFlag, 0),
    BOTTOM(FaceSide.BOTTOM.bitFlag, 0),
    LEFT(FaceSide.LEFT.bitFlag, 0),
    RIGHT(FaceSide.RIGHT.bitFlag, 0),
    TOP_BOTTOM(FaceSide.TOP.bitFlag | FaceSide.BOTTOM.bitFlag, 0),
    LEFT_RIGHT(FaceSide.LEFT.bitFlag | FaceSide.RIGHT.bitFlag, 0),
    
    TOP_BOTTOM_RIGHT_NO_CORNERS(FaceSide.TOP.bitFlag | FaceSide.BOTTOM.bitFlag | FaceSide.RIGHT.bitFlag, 0, FaceCorner.TOP_RIGHT, FaceCorner.BOTTOM_RIGHT),
    TOP_BOTTOM_RIGHT_TR(FaceSide.TOP.bitFlag | FaceSide.BOTTOM.bitFlag | FaceSide.RIGHT.bitFlag, FaceCorner.TOP_RIGHT.bitFlag),
    TOP_BOTTOM_RIGHT_BR(FaceSide.TOP.bitFlag | FaceSide.BOTTOM.bitFlag | FaceSide.RIGHT.bitFlag, FaceCorner.BOTTOM_RIGHT.bitFlag),
    TOP_BOTTOM_RIGHT_TR_BR(FaceSide.TOP.bitFlag | FaceSide.BOTTOM.bitFlag | FaceSide.RIGHT.bitFlag, FaceCorner.TOP_RIGHT.bitFlag | FaceCorner.BOTTOM_RIGHT.bitFlag),
    
    TOP_BOTTOM_LEFT_NO_CORNERS(FaceSide.TOP.bitFlag | FaceSide.BOTTOM.bitFlag | FaceSide.LEFT.bitFlag, 0, FaceCorner.TOP_LEFT, FaceCorner.BOTTOM_LEFT),
    TOP_BOTTOM_LEFT_TL(FaceSide.TOP.bitFlag | FaceSide.BOTTOM.bitFlag | FaceSide.LEFT.bitFlag, FaceCorner.TOP_LEFT.bitFlag),
    TOP_BOTTOM_LEFT_BL(FaceSide.TOP.bitFlag | FaceSide.BOTTOM.bitFlag | FaceSide.LEFT.bitFlag, FaceCorner.BOTTOM_LEFT.bitFlag),
    TOP_BOTTOM_LEFT_TL_BL(FaceSide.TOP.bitFlag | FaceSide.BOTTOM.bitFlag | FaceSide.LEFT.bitFlag, FaceCorner.TOP_LEFT.bitFlag | FaceCorner.BOTTOM_LEFT.bitFlag),

    TOP_LEFT_RIGHT_NO_CORNERS(FaceSide.TOP.bitFlag | FaceSide.LEFT.bitFlag | FaceSide.RIGHT.bitFlag, 0, FaceCorner.TOP_LEFT, FaceCorner.TOP_RIGHT),
    TOP_LEFT_RIGHT_TL(FaceSide.TOP.bitFlag | FaceSide.LEFT.bitFlag | FaceSide.RIGHT.bitFlag, FaceCorner.TOP_LEFT.bitFlag),
    TOP_LEFT_RIGHT_TR(FaceSide.TOP.bitFlag | FaceSide.LEFT.bitFlag | FaceSide.RIGHT.bitFlag, FaceCorner.TOP_RIGHT.bitFlag),
    TOP_LEFT_RIGHT_TL_TR(FaceSide.TOP.bitFlag | FaceSide.LEFT.bitFlag | FaceSide.RIGHT.bitFlag, FaceCorner.TOP_LEFT.bitFlag | FaceCorner.TOP_RIGHT.bitFlag),
    
    BOTTOM_LEFT_RIGHT_NO_CORNERS(FaceSide.BOTTOM.bitFlag | FaceSide.LEFT.bitFlag | FaceSide.RIGHT.bitFlag, 0, FaceCorner.BOTTOM_LEFT, FaceCorner.BOTTOM_RIGHT),
    BOTTOM_LEFT_RIGHT_BL(FaceSide.BOTTOM.bitFlag | FaceSide.LEFT.bitFlag | FaceSide.RIGHT.bitFlag, FaceCorner.BOTTOM_LEFT.bitFlag),
    BOTTOM_LEFT_RIGHT_BR(FaceSide.BOTTOM.bitFlag | FaceSide.LEFT.bitFlag | FaceSide.RIGHT.bitFlag, FaceCorner.BOTTOM_RIGHT.bitFlag),
    BOTTOM_LEFT_RIGHT_BL_BR(FaceSide.BOTTOM.bitFlag | FaceSide.LEFT.bitFlag | FaceSide.RIGHT.bitFlag, FaceCorner.BOTTOM_LEFT.bitFlag | FaceCorner.BOTTOM_RIGHT.bitFlag),

    TOP_LEFT_NO_CORNER(FaceSide.TOP.bitFlag | FaceSide.LEFT.bitFlag, 0, FaceCorner.TOP_LEFT),
    TOP_LEFT_TL(FaceSide.TOP.bitFlag | FaceSide.LEFT.bitFlag, FaceCorner.TOP_LEFT.bitFlag),
    
    TOP_RIGHT_NO_CORNER(FaceSide.TOP.bitFlag | FaceSide.RIGHT.bitFlag, 0, FaceCorner.TOP_RIGHT),
    TOP_RIGHT_TR(FaceSide.TOP.bitFlag | FaceSide.RIGHT.bitFlag, FaceCorner.TOP_RIGHT.bitFlag),
    
    BOTTOM_LEFT_NO_CORNER(FaceSide.BOTTOM.bitFlag | FaceSide.LEFT.bitFlag, 0, FaceCorner.BOTTOM_LEFT),
    BOTTOM_LEFT_BL(FaceSide.BOTTOM.bitFlag | FaceSide.LEFT.bitFlag, FaceCorner.BOTTOM_LEFT.bitFlag),
    
    BOTTOM_RIGHT_NO_CORNER(FaceSide.BOTTOM.bitFlag | FaceSide.RIGHT.bitFlag, 0, FaceCorner.BOTTOM_RIGHT),
    BOTTOM_RIGHT_BR(FaceSide.BOTTOM.bitFlag | FaceSide.RIGHT.bitFlag, FaceCorner.BOTTOM_RIGHT.bitFlag),
    
    ALL_NO_CORNERS(FaceSide.TOP.bitFlag | FaceSide.BOTTOM.bitFlag | FaceSide.LEFT.bitFlag | FaceSide.RIGHT.bitFlag, 0, FaceCorner.TOP_LEFT, FaceCorner.TOP_RIGHT, FaceCorner.BOTTOM_LEFT, FaceCorner.BOTTOM_RIGHT),
    ALL_TL(FaceSide.TOP.bitFlag | FaceSide.BOTTOM.bitFlag | FaceSide.LEFT.bitFlag | FaceSide.RIGHT.bitFlag, FaceCorner.TOP_LEFT.bitFlag),
    ALL_TR(FaceSide.TOP.bitFlag | FaceSide.BOTTOM.bitFlag | FaceSide.LEFT.bitFlag | FaceSide.RIGHT.bitFlag, FaceCorner.TOP_RIGHT.bitFlag),
    ALL_TL_TR(FaceSide.TOP.bitFlag | FaceSide.BOTTOM.bitFlag | FaceSide.LEFT.bitFlag | FaceSide.RIGHT.bitFlag, FaceCorner.TOP_RIGHT.bitFlag | FaceCorner.TOP_LEFT.bitFlag),
    ALL_BL(FaceSide.TOP.bitFlag | FaceSide.BOTTOM.bitFlag | FaceSide.LEFT.bitFlag | FaceSide.RIGHT.bitFlag, FaceCorner.BOTTOM_LEFT.bitFlag),
    ALL_TL_BL(FaceSide.TOP.bitFlag | FaceSide.BOTTOM.bitFlag | FaceSide.LEFT.bitFlag | FaceSide.RIGHT.bitFlag, FaceCorner.BOTTOM_LEFT.bitFlag | FaceCorner.TOP_LEFT.bitFlag),
    ALL_TR_BL(FaceSide.TOP.bitFlag | FaceSide.BOTTOM.bitFlag | FaceSide.LEFT.bitFlag | FaceSide.RIGHT.bitFlag, FaceCorner.BOTTOM_LEFT.bitFlag | FaceCorner.TOP_RIGHT.bitFlag),
    ALL_TL_TR_BL(FaceSide.TOP.bitFlag | FaceSide.BOTTOM.bitFlag | FaceSide.LEFT.bitFlag | FaceSide.RIGHT.bitFlag, FaceCorner.BOTTOM_LEFT.bitFlag | FaceCorner.TOP_RIGHT.bitFlag | FaceCorner.TOP_LEFT.bitFlag),
    ALL_BR(FaceSide.TOP.bitFlag | FaceSide.BOTTOM.bitFlag | FaceSide.LEFT.bitFlag | FaceSide.RIGHT.bitFlag, FaceCorner.BOTTOM_RIGHT.bitFlag),
    ALL_TL_BR(FaceSide.TOP.bitFlag | FaceSide.BOTTOM.bitFlag | FaceSide.LEFT.bitFlag | FaceSide.RIGHT.bitFlag, FaceCorner.BOTTOM_RIGHT.bitFlag | FaceCorner.TOP_LEFT.bitFlag),
    ALL_TR_BR(FaceSide.TOP.bitFlag | FaceSide.BOTTOM.bitFlag | FaceSide.LEFT.bitFlag | FaceSide.RIGHT.bitFlag, FaceCorner.BOTTOM_RIGHT.bitFlag | FaceCorner.TOP_RIGHT.bitFlag),
    ALL_TL_TR_BR(FaceSide.TOP.bitFlag | FaceSide.BOTTOM.bitFlag | FaceSide.LEFT.bitFlag | FaceSide.RIGHT.bitFlag, FaceCorner.BOTTOM_RIGHT.bitFlag | FaceCorner.TOP_RIGHT.bitFlag | FaceCorner.TOP_LEFT.bitFlag),
    ALL_BL_BR(FaceSide.TOP.bitFlag | FaceSide.BOTTOM.bitFlag | FaceSide.LEFT.bitFlag | FaceSide.RIGHT.bitFlag, FaceCorner.BOTTOM_RIGHT.bitFlag | FaceCorner.BOTTOM_LEFT.bitFlag),
    ALL_TL_BL_BR(FaceSide.TOP.bitFlag | FaceSide.BOTTOM.bitFlag | FaceSide.LEFT.bitFlag | FaceSide.RIGHT.bitFlag, FaceCorner.BOTTOM_RIGHT.bitFlag | FaceCorner.BOTTOM_LEFT.bitFlag | FaceCorner.TOP_LEFT.bitFlag),
    ALL_TR_BL_BR(FaceSide.TOP.bitFlag | FaceSide.BOTTOM.bitFlag | FaceSide.LEFT.bitFlag | FaceSide.RIGHT.bitFlag, FaceCorner.BOTTOM_RIGHT.bitFlag | FaceCorner.BOTTOM_LEFT.bitFlag | FaceCorner.TOP_RIGHT.bitFlag),
    ALL_TL_TR_BL_BR(FaceSide.TOP.bitFlag | FaceSide.BOTTOM.bitFlag | FaceSide.LEFT.bitFlag | FaceSide.RIGHT.bitFlag, FaceCorner.BOTTOM_RIGHT.bitFlag | FaceCorner.BOTTOM_LEFT.bitFlag | FaceCorner.TOP_RIGHT.bitFlag | FaceCorner.TOP_LEFT.bitFlag);
    
    /**
     * Sparsely populated - only meaningful states are non-null.  
     * For example, cannot also have corners on side with a border.
     */
    private static final CornerJoinFaceState[] LOOKUP = new CornerJoinFaceState[256];

    private final int bitFlags;
    private final FaceCorner[] cornerTests;
    private CornerJoinFaceState[] subStates;

    static
    {
        for(CornerJoinFaceState state : CornerJoinFaceState.values())
        {
            LOOKUP[state.bitFlags] = state;
            
            ArrayList<CornerJoinFaceState> subStateList = new ArrayList<CornerJoinFaceState>();
            
            if(state == NO_FACE)
            {
                subStateList.add(NO_FACE);
            }
            else
            {
                for(CornerJoinFaceState subState : CornerJoinFaceState.values())
                {
                    if(subState != NO_FACE && (subState.bitFlags & state.bitFlags & 15) == (subState.bitFlags & 15))
                    {
                        subStateList.add(subState);
                    }
                }
            }
            state.subStates = subStateList.toArray(new CornerJoinFaceState[subStateList.size()]);
        }
    }
    
    private CornerJoinFaceState(int faceBits, int cornerBits, FaceCorner... cornerTests)
    {
        this.bitFlags = faceBits | (cornerBits << 4);
        this.cornerTests = cornerTests;
    }
    
    private static CornerJoinFaceState find(int faceBits, int cornerBits)
    {
        return LOOKUP[(faceBits & 15) | ((cornerBits & 15) << 4)];
    }
    
//    private int getCornerBits()
//    {
//        return (this.bitFlags >> 4) & 15;
//    }
//    
//    private int getFaceBits()
//    {
//        return this.bitFlags & 15;
//    }
    
    public static CornerJoinFaceState find(Direction face, SimpleJoin join)
    {
        int faceFlags = 0;
        
        CornerJoinFaceState fjs;
        
        if(join.isJoined(face))
        {
            fjs = CornerJoinFaceState.NO_FACE;
        }
        else
        {                   
            for(FaceSide fside : FaceSide.values())
            {
                if(join.isJoined(fside.getRelativeFace(face)))
                {
                    faceFlags |= fside.bitFlag;
                }
            }
        
            fjs = CornerJoinFaceState.find(faceFlags, 0);
        }
        return fjs;
    }
    
    public static CornerJoinFaceState find(Direction face, ICornerJoinTestProvider tests)
    {
        int faceFlags = 0;
        int cornerFlags = 0;
        
        CornerJoinFaceState fjs;
        
        if(tests.result(face))
        {
            fjs = CornerJoinFaceState.NO_FACE;
        }
        else
        {                   
            for(FaceSide fside : FaceSide.values())
            {
                Direction joinFace = fside.getRelativeFace(face);
                if(tests.result(joinFace) && !tests.result(BlockCorner.find(face, joinFace)))
                {
                    faceFlags |= fside.bitFlag;
                }
            }
        
            fjs = CornerJoinFaceState.find(faceFlags, cornerFlags);

            if(fjs.hasCornerTests())
            {
                for(FaceCorner corner : fjs.getCornerTests())
                {
                    if(!tests.result(corner.leftSide.getRelativeFace(face), corner.rightSide.getRelativeFace(face))
                            || tests.result(corner.leftSide.getRelativeFace(face), corner.rightSide.getRelativeFace(face), face))
                    {
                        cornerFlags |= corner.bitFlag;
                    }
                }
                
                fjs = CornerJoinFaceState.find(faceFlags, cornerFlags);
            }
        }
        return fjs;
    }
    
    private boolean hasCornerTests()
    {
        return (cornerTests != null && cornerTests.length > 0);
    }
    
    private FaceCorner[] getCornerTests()
    {
        return cornerTests;
    }
    
    public CornerJoinFaceState[] getSubStates()
    {
        return subStates;
    }
    
    public boolean isJoined(FaceSide side)
    {
        return (this.bitFlags & side.bitFlag) == side.bitFlag;
    }
    
    public boolean isJoined(Direction toFace, Direction onFace)
    {
        FaceSide side = FaceSide.lookup(toFace, onFace);
        return side == null ? false : this.isJoined(side);
    }
    
    /**
     * True if connected-texture/shape blocks need to render corner due
     * to missing/covered block in adjacent corner.
     */
    public boolean needsCorner(FaceCorner corner)
    {
        return ((this.bitFlags >> 4) & corner.bitFlag) == corner.bitFlag;
    }
    
    public boolean needsCorner(Direction face1, Direction face2, Direction onFace)
    {
        FaceSide side1 = FaceSide.lookup(face1, onFace);
        FaceSide side2 = FaceSide.lookup(face2, onFace);
        return side1 == null || side2 == null ? false : this.needsCorner(FaceCorner.find(side1, side2));
    }
}
