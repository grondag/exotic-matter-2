package grondag.brocade.world;

import grondag.brocade.block.ISuperBlockAccess;
import grondag.brocade.block.SuperBlockWorldAccess;
import grondag.brocade.model.state.ISuperModelState;
import grondag.fermion.varia.Useful;
import net.fabricmc.fabric.api.client.model.fabric.ModelHelper;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.ExtendedBlockView;

/**
 * Convenient way to gather and test block states for blocks adjacent to a given
 * position. Position is immutable, blockstates are looked up lazily and values
 * are cached for reuse.
 */
public class NeighborBlocks {

    public final static int[] FACE_FLAGS = { 1, 2, 4, 8, 16, 32 };

    // Direction.values().length + BlockCorner.values().length +
    // FarCorner.values().length = 6 +
    private final static int STATE_COUNT = 6 + 12 + 8;

    private BlockState blockStates[] = new BlockState[STATE_COUNT];
    private ISuperModelState modelStates[] = new ISuperModelState[STATE_COUNT];

    private final ISuperBlockAccess world;
    private final int x;
    private final int y;
    private final int z;
    private final IExtraStateFactory factory;
    private final BlockPos.Mutable mutablePos = new BlockPos.Mutable();

    /**
     * Gathers blockstates for adjacent positions as needed.
     */
    public NeighborBlocks(ExtendedBlockView worldIn, BlockPos pos) {
        this(worldIn, pos, (IExtraStateFactory) IExtraStateFactory.NONE);
    }

    public NeighborBlocks(ExtendedBlockView worldIn, BlockPos pos, IExtraStateFactory factory) {
        this.world = SuperBlockWorldAccess.access(worldIn);
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
        this.factory = factory;
    }

    //////////////////////////////
    // BLOCK STATE
    //////////////////////////////
    public BlockState getBlockState(Direction face) {
        BlockState result = blockStates[face.ordinal()];
        if (result == null) {
            final Vec3i vec = face.getDirectionVec();
            result = world.getBlockState(mutablePos.setPos(x + vec.getX(), y + vec.getY(), z + vec.getZ()));
            blockStates[face.ordinal()] = result;
        }
        return result;
    }

    public BlockState getBlockState(HorizontalFace face) {
        return getBlockState(face.face);
    }

    public BlockState getBlockStateUp(HorizontalFace face) {
        return getBlockState(face.face, Direction.UP);
    }

    public BlockState getBlockStateDown(HorizontalFace face) {
        return getBlockState(face.face, Direction.DOWN);
    }

    public BlockState getBlockState(Direction face1, Direction face2) {
        BlockCorner corner = BlockCorner.find(face1, face2);
        return getBlockState(corner);
    }

    public BlockState getBlockState(HorizontalCorner corner) {
        return getBlockState(corner.face1.face, corner.face2.face);
    }

    public BlockState getBlockStateUp(HorizontalCorner corner) {
        return getBlockState(corner.face1.face, corner.face2.face, Direction.UP);
    }

    public BlockState getBlockStateDown(HorizontalCorner corner) {
        return getBlockState(corner.face1.face, corner.face2.face, Direction.DOWN);
    }

    public BlockState getBlockState(BlockCorner corner) {
        BlockState result = blockStates[corner.superOrdinal];
        if (result == null) {
            final Vec3i vec = corner.directionVector;
            result = world.getBlockState(mutablePos.setPos(x + vec.getX(), y + vec.getY(), z + vec.getZ()));
            blockStates[corner.superOrdinal] = result;
        }
        return result;
    }

    public BlockState getBlockState(Direction face1, Direction face2, Direction face3) {
        FarCorner corner = FarCorner.find(face1, face2, face3);
        return getBlockState(corner);
    }

    public BlockState getBlockState(FarCorner corner) {
        BlockState result = blockStates[corner.superOrdinal];
        if (result == null) {
            final Vec3i vec = corner.directionVector;
            result = world.getBlockState(mutablePos.setPos(x + vec.getX(), y + vec.getY(), z + vec.getZ()));
            blockStates[corner.superOrdinal] = result;
        }
        return result;
    }

    //////////////////////////////
    // MODEL STATE
    //////////////////////////////

    public ISuperModelState getModelState(Direction face) {
        ISuperModelState result = modelStates[face.ordinal()];
        if (result == null) {
            BlockState state = this.getBlockState(face);
            final Vec3i vec = face.getDirectionVec();
            mutablePos.setPos(x + vec.getX(), y + vec.getY(), z + vec.getZ());
            result = this.factory.get(this.world, mutablePos, state);
            modelStates[face.ordinal()] = result;
        }
        return result;
    }

    public ISuperModelState getModelState(HorizontalFace face) {
        return getModelState(face.face);
    }

    public ISuperModelState getModelStateUp(HorizontalFace face) {
        return getModelState(face.face, Direction.UP);
    }

    public ISuperModelState getModelStateDown(HorizontalFace face) {
        return getModelState(face.face, Direction.DOWN);
    }

    public ISuperModelState getModelState(Direction face1, Direction face2) {
        BlockCorner corner = BlockCorner.find(face1, face2);
        return getModelState(corner);
    }

    public ISuperModelState getModelState(HorizontalCorner corner) {
        return getModelState(corner.face1.face, corner.face2.face);
    }

    public ISuperModelState getModelStateUp(HorizontalCorner corner) {
        return getModelState(corner.face1.face, corner.face2.face, Direction.UP);
    }

    public ISuperModelState getModelStateDown(HorizontalCorner corner) {
        return getModelState(corner.face1.face, corner.face2.face, Direction.DOWN);
    }

    public ISuperModelState getModelState(BlockCorner corner) {
        ISuperModelState result = modelStates[corner.superOrdinal];
        if (result == null) {
            BlockState state = this.getBlockState(corner);
            final Vec3i vec = corner.directionVector;
            mutablePos.setPos(x + vec.getX(), y + vec.getY(), z + vec.getZ());
            result = this.factory.get(this.world, mutablePos, state);
            modelStates[corner.superOrdinal] = result;
        }
        return result;
    }

    public ISuperModelState getModelState(Direction face1, Direction face2, Direction face3) {
        FarCorner corner = FarCorner.find(face1, face2, face3);
        return getModelState(corner);
    }

    public ISuperModelState getModelState(FarCorner corner) {
        ISuperModelState result = modelStates[corner.superOrdinal];
        if (result == null) {
            BlockState state = this.getBlockState(corner);
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
    public class NeighborTestResults implements ICornerJoinTestProvider {
        private int completionFlags = 0;
        private int resultFlags = 0;
        private final IBlockTest test;

        private NeighborTestResults(IBlockTest test) {
            this.test = test;
        }

        // for testing
        @SuppressWarnings("null")
        private NeighborTestResults(int faceFlags) {
            this.test = null;
            this.resultFlags = faceFlags;
            this.completionFlags = Useful.intBitMask(26);
        }

        private boolean doTest(Direction face) {
            BlockState state = getBlockState(face);
            final Vec3i vec = face.getDirectionVec();

            if (test.wantsModelState()) {
                ISuperModelState modelState = getModelState(face);
                return test.testBlock(face, world, state,
                        mutablePos.setPos(x + vec.getX(), y + vec.getY(), z + vec.getZ()), modelState);
            } else {
                return test.testBlock(face, world, state,
                        mutablePos.setPos(x + vec.getX(), y + vec.getY(), z + vec.getZ()));
            }
        }

        private boolean doTest(BlockCorner corner) {
            BlockState state = getBlockState(corner);
            final Vec3i vec = corner.directionVector;

            if (test.wantsModelState()) {
                ISuperModelState modelState = getModelState(corner);
                return test.testBlock(corner, world, state,
                        mutablePos.setPos(x + vec.getX(), y + vec.getY(), z + vec.getZ()), modelState);
            } else {
                return test.testBlock(corner, world, state,
                        mutablePos.setPos(x + vec.getX(), y + vec.getY(), z + vec.getZ()));
            }
        }

        private boolean doTest(FarCorner corner) {
            BlockState state = getBlockState(corner);
            final Vec3i vec = corner.directionVector;

            if (test.wantsModelState()) {
                ISuperModelState modelState = getModelState(corner);
                return test.testBlock(corner, world, state,
                        mutablePos.setPos(x + vec.getX(), y + vec.getY(), z + vec.getZ()), modelState);
            } else {
                return test.testBlock(corner, world, state,
                        mutablePos.setPos(x + vec.getX(), y + vec.getY(), z + vec.getZ()));
            }
        }

        @Override
        public boolean result(Direction face) {
            int bitFlag = FACE_FLAGS[face.ordinal()];
            if ((completionFlags & bitFlag) != bitFlag) {
                if (doTest(face)) {
                    resultFlags |= bitFlag;
                }
                completionFlags |= bitFlag;
            }
            return (resultFlags & bitFlag) == bitFlag;
        }

        /** use this to override world results */
        public void override(Direction face, boolean override) {
            int bitFlag = FACE_FLAGS[face.ordinal()];
            completionFlags |= bitFlag;
            if (override) {
                resultFlags |= bitFlag;
            } else {
                resultFlags &= ~bitFlag;
            }
        }

        public boolean result(HorizontalFace face) {
            return result(face.face);
        }

        public boolean resultUp(HorizontalFace face) {
            return result(face.face, Direction.UP);
        }

        public boolean resultDown(HorizontalFace face) {
            return result(face.face, Direction.DOWN);
        }

        /** convenience method */
        public int resultBit(Direction face) {
            return result(face) ? 1 : 0;
        }

        public boolean result(HorizontalCorner corner) {
            return result(corner.face1.face, corner.face2.face);
        }

        public boolean resultUp(HorizontalCorner corner) {
            return result(corner.face1.face, corner.face2.face, Direction.UP);
        }

        public boolean resultDown(HorizontalCorner corner) {
            return result(corner.face1.face, corner.face2.face, Direction.DOWN);
        }

        @Override
        public boolean result(BlockCorner corner) {
            if ((completionFlags & corner.bitFlag) != corner.bitFlag) {
                if (doTest(corner)) {
                    resultFlags |= corner.bitFlag;
                }
                completionFlags |= corner.bitFlag;
            }
            return (resultFlags & corner.bitFlag) == corner.bitFlag;
        }

        public int resultBit(Direction face1, Direction face2) {
            return result(face1, face2) ? 1 : 0;
        }

        public int resultBit(BlockCorner corner) {
            return result(corner) ? 1 : 0;
        }

        @Override
        public boolean result(FarCorner corner) {
            if ((completionFlags & corner.bitFlag) != corner.bitFlag) {
                if (doTest(corner)) {
                    resultFlags |= corner.bitFlag;
                }
                completionFlags |= corner.bitFlag;
            }
            return (resultFlags & corner.bitFlag) == corner.bitFlag;
        }

        public int resultBit(Direction face1, Direction face2, Direction face3) {
            return result(face1, face2, face3) ? 1 : 0;
        }

        public int resultBit(FarCorner corner) {
            return result(corner) ? 1 : 0;
        }

        @Override
        public String toString() {
            String retval = "";

            for (int i = 0; i < 6; i++) {
                final Direction face = ModelHelper.faceFromIndex(i);
                retval += face.toString() + "=" + this.result(face) + " ";
            }

            for (BlockCorner corner : BlockCorner.values()) {
                retval += corner.toString() + "=" + this.result(corner) + " ";
            }

            for (FarCorner corner : FarCorner.values()) {
                retval += corner.toString() + "=" + this.result(corner) + " ";
            }
            return retval;
        }
    }
}
