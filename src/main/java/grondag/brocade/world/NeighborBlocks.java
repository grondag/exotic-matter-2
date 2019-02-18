package grondag.brocade.world;

import grondag.exotic_matter.block.ISuperBlockAccess;
import grondag.exotic_matter.block.SuperBlockWorldAccess;
import grondag.exotic_matter.model.state.ISuperModelState;
import grondag.exotic_matter.varia.Useful;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.IBlockAccess;

/**
 * Convenient way to gather and test block states
 * for blocks adjacent to a given position.
 * Position is immutable, blockstates are looked up lazily 
 * and values are cached for reuse.
 */
public class NeighborBlocks
{

    public final static int[] FACE_FLAGS = {1, 2, 4, 8, 16, 32};

    // EnumFacing.values().length + BlockCorner.values().length + FarCorner.values().length = 6 + 
    private final static int STATE_COUNT = 6 + 12 + 8;
    
    private IBlockState blockStates[] = new IBlockState[STATE_COUNT];
    private ISuperModelState modelStates[] = new ISuperModelState[STATE_COUNT];


    private final ISuperBlockAccess world;
    private final int x;
    private final int y;
    private final int z;
    private final IExtraStateFactory factory;
    private final MutableBlockPos mutablePos = new MutableBlockPos();

    /**
     * Gathers blockstates for adjacent positions as needed.
     */
    public NeighborBlocks(IBlockAccess worldIn, BlockPos pos) 
    {
        this(worldIn, pos, (IExtraStateFactory) IExtraStateFactory.NONE );
    }

    public NeighborBlocks(IBlockAccess worldIn, BlockPos pos, IExtraStateFactory factory) 
    {
        this.world = SuperBlockWorldAccess.access(worldIn);
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
        this.factory = factory;
    }
    
    //////////////////////////////
    // BLOCK STATE
    //////////////////////////////
    public IBlockState getBlockState(EnumFacing face)
    {
        IBlockState result = blockStates[face.ordinal()];
        if(result == null)
        {
            final Vec3i vec = face.getDirectionVec();
            result = world.getBlockState(mutablePos.setPos(x + vec.getX(), y + vec.getY(), z + vec.getZ()));
            blockStates[face.ordinal()] = result;
        }
        return result;
    }

    public IBlockState getBlockState(HorizontalFace face)
    {
        return getBlockState(face.face);
    }

    public IBlockState getBlockStateUp(HorizontalFace face)
    {
        return getBlockState(face.face, EnumFacing.UP);
    }

    public IBlockState getBlockStateDown(HorizontalFace face)
    {
        return getBlockState(face.face, EnumFacing.DOWN);
    }
    public IBlockState getBlockState(EnumFacing face1, EnumFacing face2)
    {
        BlockCorner corner = BlockCorner.find(face1, face2);
        return getBlockState(corner);
    }

    public IBlockState getBlockState(HorizontalCorner corner)
    {
        return getBlockState(corner.face1.face, corner.face2.face);
    }

    public IBlockState getBlockStateUp(HorizontalCorner corner)
    {
        return getBlockState(corner.face1.face, corner.face2.face, EnumFacing.UP);
    }

    public IBlockState getBlockStateDown(HorizontalCorner corner)
    {
        return getBlockState(corner.face1.face, corner.face2.face, EnumFacing.DOWN);
    }
    public IBlockState getBlockState(BlockCorner corner)
    {
        IBlockState result = blockStates[corner.superOrdinal];
        if(result == null)
        {
            final Vec3i vec = corner.directionVector;
            result = world.getBlockState(mutablePos.setPos(x + vec.getX(), y + vec.getY(), z + vec.getZ()));
            blockStates[corner.superOrdinal] = result;
        }
        return result;
    }

    public IBlockState getBlockState(EnumFacing face1, EnumFacing face2, EnumFacing face3)
    {
        FarCorner corner = FarCorner.find(face1, face2, face3);
        return getBlockState(corner);
    }

    public IBlockState getBlockState(FarCorner corner)
    {
        IBlockState result = blockStates[corner.superOrdinal];
        if(result == null)
        {
            final Vec3i vec = corner.directionVector;
            result = world.getBlockState(mutablePos.setPos(x + vec.getX(), y + vec.getY(), z + vec.getZ()));
            blockStates[corner.superOrdinal] = result;
        }
        return result;
    }
    
    //////////////////////////////
    // MODEL STATE
    //////////////////////////////
    
    public ISuperModelState getModelState(EnumFacing face)
    {
        ISuperModelState result = modelStates[face.ordinal()];
        if(result == null)
        {
            IBlockState state = this.getBlockState(face);
            final Vec3i vec = face.getDirectionVec();
            mutablePos.setPos(x + vec.getX(), y + vec.getY(), z + vec.getZ());
            result = this.factory.get(this.world, mutablePos, state);
            modelStates[face.ordinal()] = result;
        }
        return result;
    }

    public ISuperModelState getModelState(HorizontalFace face)
    {
        return getModelState(face.face);
    }

    public ISuperModelState getModelStateUp(HorizontalFace face)
    {
        return getModelState(face.face, EnumFacing.UP);
    }

    public ISuperModelState getModelStateDown(HorizontalFace face)
    {
        return getModelState(face.face, EnumFacing.DOWN);
    }
    public ISuperModelState getModelState(EnumFacing face1, EnumFacing face2)
    {
        BlockCorner corner = BlockCorner.find(face1, face2);
        return getModelState(corner);
    }

    public ISuperModelState getModelState(HorizontalCorner corner)
    {
        return getModelState(corner.face1.face, corner.face2.face);
    }

    public ISuperModelState getModelStateUp(HorizontalCorner corner)
    {
        return getModelState(corner.face1.face, corner.face2.face, EnumFacing.UP);
    }

    public ISuperModelState getModelStateDown(HorizontalCorner corner)
    {
        return getModelState(corner.face1.face, corner.face2.face, EnumFacing.DOWN);
    }
    
    public ISuperModelState getModelState(BlockCorner corner)
    {
        ISuperModelState result = modelStates[corner.superOrdinal];
        if(result == null)
        {
            IBlockState state = this.getBlockState(corner);
            final Vec3i vec = corner.directionVector;
            mutablePos.setPos(x + vec.getX(), y + vec.getY(), z + vec.getZ());
            result = this.factory.get(this.world, mutablePos, state);
            modelStates[corner.superOrdinal] = result;
        }
        return result;
    }

    public ISuperModelState getModelState(EnumFacing face1, EnumFacing face2, EnumFacing face3)
    {
        FarCorner corner = FarCorner.find(face1, face2, face3);
        return getModelState(corner);
    }

    public ISuperModelState getModelState(FarCorner corner)
    {
        ISuperModelState result = modelStates[corner.superOrdinal];
        if(result == null)
        {
            IBlockState state = this.getBlockState(corner);
            final Vec3i vec = corner.directionVector;
            mutablePos.setPos(x + vec.getX(), y + vec.getY(), z + vec.getZ());
            result = this.factory.get(this.world, mutablePos, state);
            modelStates[corner.superOrdinal] = result;
        }
        return result;
    }

    //////////////////////////////
    // TESTS AND OTHER STUFF
    //////////////////////////////
    
    /**
     * Apply given test to neighboring block states.
     */
    public NeighborTestResults getNeighborTestResults(IBlockTest test) {
        return new NeighborTestResults(test);
    }

    /**
     * For testing
     */
    public NeighborTestResults getFakeNeighborTestResults(int faceFlags) {
        return new NeighborTestResults(faceFlags);
    }
    
    /**
     * Convenient data structure for returning test results.
     */
    public class NeighborTestResults implements ICornerJoinTestProvider
    {
        private int completionFlags = 0;
        private int resultFlags = 0;
        private final IBlockTest test;

        private NeighborTestResults(IBlockTest test) {
            this.test = test;
        }

        // for testing
        @SuppressWarnings("null")
        private NeighborTestResults(int faceFlags)
        {
            this.test = null;
            this.resultFlags = faceFlags;
            this.completionFlags = Useful.intBitMask(26);
        }
        
        private boolean doTest(EnumFacing face)
        {
            IBlockState state = getBlockState(face);
            final Vec3i vec = face.getDirectionVec();
            
            if(test.wantsModelState())
            {
                ISuperModelState modelState = getModelState(face);
                return test.testBlock(face, world, state, mutablePos.setPos(x + vec.getX(), y + vec.getY(), z + vec.getZ()), modelState);
            }
            else
            {
                return test.testBlock(face, world, state, mutablePos.setPos(x + vec.getX(), y + vec.getY(), z + vec.getZ()));
            }
        }
        
        private boolean doTest(BlockCorner corner)
        {
            IBlockState state = getBlockState(corner);
            final Vec3i vec = corner.directionVector;
            
            if(test.wantsModelState())
            {
                ISuperModelState modelState = getModelState(corner);
                return test.testBlock(corner, world, state, mutablePos.setPos(x + vec.getX(), y + vec.getY(), z + vec.getZ()), modelState);
            }
            else
            {
                return test.testBlock(corner, world, state, mutablePos.setPos(x + vec.getX(), y + vec.getY(), z + vec.getZ()));
            }
        }
        
        private boolean doTest(FarCorner corner)
        {
            IBlockState state = getBlockState(corner);
            final Vec3i vec = corner.directionVector;
            
            if(test.wantsModelState())
            {
                ISuperModelState modelState = getModelState(corner);
                return test.testBlock(corner, world, state, mutablePos.setPos(x + vec.getX(), y + vec.getY(), z + vec.getZ()), modelState);
            }
            else
            {
                return test.testBlock(corner, world, state, mutablePos.setPos(x + vec.getX(), y + vec.getY(), z + vec.getZ()));
            }
        }
        
        @Override
        public boolean result(EnumFacing face)
        {
            int bitFlag = FACE_FLAGS[face.ordinal()];
            if((completionFlags & bitFlag) != bitFlag) {
                if(doTest(face))
                {
                    resultFlags |= bitFlag;
                }
                completionFlags |= bitFlag;
            }
            return (resultFlags & bitFlag) == bitFlag;
        }
        
        /** use this to override world results */
        public void override(EnumFacing face, boolean override)
        {
            int bitFlag = FACE_FLAGS[face.ordinal()];
            completionFlags |= bitFlag;
            if(override)
            {
                resultFlags |= bitFlag;
            }
            else
            {
                resultFlags &= ~bitFlag;
            }
        }

        public boolean result(HorizontalFace face)
        {
            return result(face.face);
        }

        public boolean resultUp(HorizontalFace face)
        {
            return result(face.face, EnumFacing.UP);
        }

        public boolean resultDown(HorizontalFace face)
        {
            return result(face.face, EnumFacing.DOWN);
        }

        /** convenience method */
        public int resultBit(EnumFacing face){
            return  result(face) ? 1 : 0;
        }

        public boolean result(HorizontalCorner corner)
        {
            return result(corner.face1.face, corner.face2.face);
        }

        public boolean resultUp(HorizontalCorner corner)
        {
            return result(corner.face1.face, corner.face2.face, EnumFacing.UP);
        }

        public boolean resultDown(HorizontalCorner corner)
        {
            return result(corner.face1.face, corner.face2.face, EnumFacing.DOWN);
        }

        @Override
        public boolean result(BlockCorner corner)
        {
            if((completionFlags & corner.bitFlag) != corner.bitFlag) {
                if(doTest(corner))
                {
                    resultFlags |= corner.bitFlag;
                }
                completionFlags |= corner.bitFlag;
            }
            return (resultFlags & corner.bitFlag) == corner.bitFlag;
        }

        public int resultBit(EnumFacing face1, EnumFacing face2)
        {
            return  result(face1, face2) ? 1 : 0;
        }

        public int resultBit(BlockCorner corner)
        {
            return  result(corner) ? 1 : 0;
        }

        @Override
        public boolean result(FarCorner corner)
        {
            if((completionFlags & corner.bitFlag) != corner.bitFlag) {
                if(doTest(corner))
                {
                    resultFlags |= corner.bitFlag;
                }
                completionFlags |= corner.bitFlag;
            }
            return (resultFlags & corner.bitFlag) == corner.bitFlag;
        }

        public int resultBit(EnumFacing face1, EnumFacing face2, EnumFacing face3)
        {
            return  result(face1, face2, face3) ? 1 : 0;
        }

        public int resultBit(FarCorner corner)
        {
            return  result(corner) ? 1 : 0;
        }
        
        @Override
        public String toString()
        {
            String retval = "";
            
            for(int i = 0; i < 6; i++)
            {
                final EnumFacing face = EnumFacing.VALUES[i];
                retval += face.toString() + "=" + this.result(face) + " ";
            }

            for(BlockCorner corner : BlockCorner.values())
            {
                retval += corner.toString() + "=" + this.result(corner) + " ";
            }
            
            for(FarCorner corner : FarCorner.values())
            {
                retval += corner.toString() + "=" + this.result(corner) + " ";
            }
            return retval;
        }
    }
}
