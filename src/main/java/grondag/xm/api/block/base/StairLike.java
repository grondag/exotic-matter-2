package grondag.xm.api.block.base;

import static grondag.xm.api.block.XmProperties.HORIZONTAL_FACING;
import static grondag.xm.api.block.XmProperties.VERTICAL_FACING_XORTHO;
import static grondag.xm.api.block.XmProperties.VERTICAL_FACING_ZORTHO;
import static net.minecraft.block.StairsBlock.HALF;
import static net.minecraft.block.StairsBlock.SHAPE;
import static net.minecraft.block.StairsBlock.WATERLOGGED;

import java.util.Random;
import java.util.function.Supplier;

import grondag.fermion.spatial.DirectionHelper;
import grondag.xm.api.collision.CollisionDispatcher;
import grondag.xm.api.modelstate.SimpleModelState;
import grondag.xm.api.modelstate.SimpleModelStateMap;
import grondag.xm.api.orientation.ExactEdge;
import grondag.xm.api.primitive.simple.Stair;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPlacementEnvironment;
import net.minecraft.block.BlockRenderLayer;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.Waterloggable;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.block.enums.StairShape;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateFactory;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.IWorld;
import net.minecraft.world.ViewableWorld;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;

public abstract class StairLike extends Block implements Waterloggable {
    protected final Block baseBlock;
    protected final BlockState baseBlockState;
    protected final Direction upDirection;
    protected final Direction downDirection;
    protected final Axis leftRightAxis;
    protected final Axis frontBackAxis;
    protected final Supplier<SimpleModelState.Mutable> modelStateFactory;
    
    
    public StairLike(BlockState blockState, Settings settings, Supplier<SimpleModelState.Mutable> modelStateFactory) {
        super(settings);
        
        final Axis axis = axis();
        //TODO: see if can implement getOutlineShape directly - if so won't need this
        this.modelStateFactory = modelStateFactory;
        this.upDirection = axis == Axis.Y ? Direction.UP : (axis == Axis.X ? Direction.EAST : Direction.SOUTH);
        this.downDirection = upDirection.getOpposite();
        leftRightAxis = axis == Axis.Y ? Axis.Z : Axis.Y;
        frontBackAxis = axis == Axis.X ? Axis.Z : Axis.X;
        
        this.setDefaultState(this.stateFactory.getDefaultState().with(faceProperty(), Direction.from(frontBackAxis, AxisDirection.NEGATIVE)).with(HALF, BlockHalf.BOTTOM).with(SHAPE, StairShape.STRAIGHT).with(WATERLOGGED, false));
        this.baseBlock = blockState.getBlock();
        this.baseBlockState = blockState;
    }

    public abstract Axis axis();
    
//    //TODO: remove
    @Override
    public boolean hasDynamicBounds() {
        return false;
    }
    
    public final DirectionProperty faceProperty() {
        final Axis axis = axis();
        return axis == Axis.Y ? HORIZONTAL_FACING : (axis == Axis.X ? VERTICAL_FACING_XORTHO : VERTICAL_FACING_ZORTHO);
    }
    
    @Override
    public boolean hasSidedTransparency(BlockState blockState_1) {
        return true;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState blockState, BlockView blockView, BlockPos pos, EntityContext entityContext) {
        return CollisionDispatcher.shapeFor(MODELSTATE_FROM_BLOCKSTATE.apply(modelStateFactory.get(), blockState));
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void randomDisplayTick(BlockState blockState_1, World world_1, BlockPos blockPos_1, Random random_1) {
        this.baseBlock.randomDisplayTick(blockState_1, world_1, blockPos_1, random_1);
    }

    @Override
    public void onBlockBreakStart(BlockState blockState_1, World world_1, BlockPos blockPos_1, PlayerEntity playerEntity_1) {
        this.baseBlockState.onBlockBreakStart(world_1, blockPos_1, playerEntity_1);
    }

    @Override
    public void onBroken(IWorld iWorld_1, BlockPos blockPos_1, BlockState blockState_1) {
        this.baseBlock.onBroken(iWorld_1, blockPos_1, blockState_1);
    }

    @Override
    public float getBlastResistance() {
        return this.baseBlock.getBlastResistance();
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return this.baseBlock.getRenderLayer();
    }

    @Override
    public int getTickRate(ViewableWorld viewableWorld_1) {
        return this.baseBlock.getTickRate(viewableWorld_1);
    }

    @Override
    public void onBlockAdded(BlockState blockState, World world, BlockPos pos, BlockState blockStateOther, boolean notify) {
        if (blockState.getBlock() != blockStateOther.getBlock()) {
            this.baseBlockState.neighborUpdate(world, pos, Blocks.AIR, pos, false);
            this.baseBlock.onBlockAdded(this.baseBlockState, world, pos, blockStateOther, false);
        }
    }

    @Override
    public void onBlockRemoved(BlockState oldState, World world, BlockPos pos, BlockState newState, boolean notify) {
        if (newState.getBlock() != oldState.getBlock()) {
            this.baseBlockState.onBlockRemoved(world, pos, oldState, notify);
        }
    }

    @Override
    public void onSteppedOn(World world_1, BlockPos blockPos_1, Entity entity_1) {
        this.baseBlock.onSteppedOn(world_1, blockPos_1, entity_1);
    }

    @Override
    public void onScheduledTick(BlockState blockState_1, World world_1, BlockPos blockPos_1, Random random_1) {
        this.baseBlock.onScheduledTick(blockState_1, world_1, blockPos_1, random_1);
    }

    @Override
    public boolean activate(BlockState blockState_1, World world_1, BlockPos blockPos_1, PlayerEntity playerEntity_1, Hand hand_1, BlockHitResult blockHitResult_1) {
        return this.baseBlockState.activate(world_1, playerEntity_1, hand_1, blockHitResult_1);
    }

    @Override
    public void onDestroyedByExplosion(World world_1, BlockPos blockPos_1, Explosion explosion_1) {
        this.baseBlock.onDestroyedByExplosion(world_1, blockPos_1, explosion_1);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext itemPlacementContext) {
        final Direction face = itemPlacementContext.getSide();
        final BlockPos pos = itemPlacementContext.getBlockPos();
        final Axis axis = this.axis();
        final FluidState fluidState = itemPlacementContext.getWorld().getFluidState(pos);
        final Direction playerFacing = itemPlacementContext.getPlayerFacing();
        final Direction backFace = playerFacing.getAxis() == axis
                ? (itemPlacementContext.getPlayer().pitch > 0 ? Direction.DOWN : Direction.UP)
                : playerFacing;
        
        final BlockState result = this.getDefaultState()
                .with(faceProperty(), backFace)
                .with(HALF, face != downDirection && (face == upDirection 
                    || itemPlacementContext.getHitPos().getComponentAlongAxis(axis) - axis.choose(pos.getX(), pos.getY(), pos.getZ()) <= 0.5D)
                    ? BlockHalf.BOTTOM : BlockHalf.TOP) 
                .with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER);
        return result.with(SHAPE, computeShape(result, itemPlacementContext.getWorld(), pos));
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState blockState_1, Direction direction_1, BlockState blockState_2, IWorld iWorld_1, BlockPos blockPos_1, BlockPos blockPos_2) {
        if ((Boolean)blockState_1.get(WATERLOGGED)) {
            iWorld_1.getFluidTickScheduler().schedule(blockPos_1, Fluids.WATER, Fluids.WATER.getTickRate(iWorld_1));
        }

        return direction_1.getAxis().isHorizontal() ? (BlockState)blockState_1.with(SHAPE, computeShape(blockState_1, iWorld_1, blockPos_1)) : super.getStateForNeighborUpdate(blockState_1, direction_1, blockState_2, iWorld_1, blockPos_1, blockPos_2);
    }

    @Override
    protected void appendProperties(StateFactory.Builder<Block, BlockState> stateFactory$Builder_1) {
        stateFactory$Builder_1.add(faceProperty(), HALF, SHAPE, WATERLOGGED);
    }

    @Override
    public FluidState getFluidState(BlockState blockState_1) {
        return (Boolean)blockState_1.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(blockState_1);
    }

    @Override
    public boolean canPlaceAtSide(BlockState blockState_1, BlockView blockView_1, BlockPos blockPos_1, BlockPlacementEnvironment blockPlacementEnvironment_1) {
        return false;
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        final DirectionProperty faceProp = faceProperty();
        return state.with(faceProp, rotation.rotate(state.get(faceProp)));
    }

    @SuppressWarnings("incomplete-switch")
    @Override
    public BlockState mirror(BlockState state, BlockMirror mirrir) {
        Direction face = (Direction)state.get(faceProperty());
        StairShape shape = (StairShape)state.get(SHAPE);
        switch(mirrir) {
        case LEFT_RIGHT:
            if (face.getAxis() == leftRightAxis) {
                switch(shape) {
                case INNER_LEFT:
                    return state.rotate(BlockRotation.CLOCKWISE_180).with(SHAPE, StairShape.INNER_RIGHT);
                case INNER_RIGHT:
                    return state.rotate(BlockRotation.CLOCKWISE_180).with(SHAPE, StairShape.INNER_LEFT);
                case OUTER_LEFT:
                    return state.rotate(BlockRotation.CLOCKWISE_180).with(SHAPE, StairShape.OUTER_RIGHT);
                case OUTER_RIGHT:
                    return state.rotate(BlockRotation.CLOCKWISE_180).with(SHAPE, StairShape.OUTER_LEFT);
                default:
                    return state.rotate(BlockRotation.CLOCKWISE_180);
                }
            }
            break;
        case FRONT_BACK:
            if (face.getAxis() == frontBackAxis) {
                switch(shape) {
                case INNER_LEFT:
                    return state.rotate(BlockRotation.CLOCKWISE_180).with(SHAPE, StairShape.INNER_LEFT);
                case INNER_RIGHT:
                    return state.rotate(BlockRotation.CLOCKWISE_180).with(SHAPE, StairShape.INNER_RIGHT);
                case OUTER_LEFT:
                    return state.rotate(BlockRotation.CLOCKWISE_180).with(SHAPE, StairShape.OUTER_RIGHT);
                case OUTER_RIGHT:
                    return state.rotate(BlockRotation.CLOCKWISE_180).with(SHAPE, StairShape.OUTER_LEFT);
                case STRAIGHT:
                    return state.rotate(BlockRotation.CLOCKWISE_180);
                }
            }
        }

        return super.mirror(state, mirrir);
    }

    protected StairShape computeShape(BlockState placedState, BlockView world, BlockPos inPos) {
        final DirectionProperty faceProp = faceProperty();
        final Axis axis = axis();
        final Direction backFace = placedState.get(faceProp);
        final BlockState onState = world.getBlockState(inPos.offset(backFace));
        if (isMatchingStairs(onState) && placedState.get(HALF) == onState.get(HALF)) {
            final Direction onBackFace = onState.get(faceProp);
            if (onBackFace.getAxis() != (placedState.get(faceProp)).getAxis() && computeShapeInner(placedState, world, inPos, onBackFace.getOpposite(), faceProp)) {
                if (onBackFace == DirectionHelper.counterClockwise(backFace, axis)) {
                    return StairShape.OUTER_LEFT;
                }

                return StairShape.OUTER_RIGHT;
            }
        }

        BlockState frontState = world.getBlockState(inPos.offset(backFace.getOpposite()));
        if (isMatchingStairs(frontState) && placedState.get(HALF) == frontState.get(HALF)) {
            final Direction frontBackFace = frontState.get(faceProp);
            if (frontBackFace.getAxis() != (placedState.get(faceProp)).getAxis() && computeShapeInner(placedState, world, inPos, frontBackFace, faceProp)) {
                if (frontBackFace == DirectionHelper.counterClockwise(backFace, axis)) {
                    return StairShape.INNER_LEFT;
                }

                return StairShape.INNER_RIGHT;
            }
        }

        return StairShape.STRAIGHT;
    }

    protected boolean computeShapeInner(BlockState placedState, BlockView world, BlockPos inPos, Direction testFace, DirectionProperty faceProp) {
        BlockState testState = world.getBlockState(inPos.offset(testFace));
        return !isMatchingStairs(testState) || testState.get(faceProp) != placedState.get(faceProp) || testState.get(HALF) != placedState.get(HALF);
    }

    protected boolean isMatchingStairs(BlockState blockState) {
        final Block block = blockState.getBlock();
        return block instanceof StairLike && ((StairLike)block).axis() == axis();
    }

    public static StairLike ofAxisY(BlockState blockState, Settings settings, Supplier<SimpleModelState.Mutable> modelStateFactory) {
        return new StairLike(blockState, settings, modelStateFactory) {
            @Override
            public Axis axis() {
                return Axis.Y;
            }
        };
    }
    
    public static StairLike ofAxisX(BlockState blockState, Settings settings, Supplier<SimpleModelState.Mutable> modelStateFactory) {
        return new StairLike(blockState, settings, modelStateFactory) {
            @Override
            public Axis axis() {
                return Axis.X;
            }
        };
    }
    
    public static StairLike ofAxisZ(BlockState blockState, Settings settings, Supplier<SimpleModelState.Mutable> modelStateFactory) {
        return new StairLike(blockState, settings, modelStateFactory) {
            @Override
            public Axis axis() {
                return Axis.Z;
            }
        };
    }
    
    public static SimpleModelStateMap.Modifier MODELSTATE_FROM_BLOCKSTATE = (modelState, blockState) -> {
        final Block rawBlock = blockState.getBlock();
        if(!(rawBlock instanceof StairLike)) {
            return modelState;
        }
        final StairLike block = (StairLike)rawBlock;
        final DirectionProperty faceProp = block.faceProperty();
        
        final Comparable<?> half = blockState.getEntries().get(StairsBlock.HALF);
        final Comparable<?> shapeVal = blockState.getEntries().get(StairsBlock.SHAPE);
        final Comparable<?> faceVal = blockState.getEntries().get(faceProp);

        if (faceProp != null && half != null && shapeVal != null) {
            final StairShape shape = StairsBlock.SHAPE.getValueType().cast(shapeVal);
            Direction face = faceProp.getValueType().cast(faceVal);
            final boolean bottom = StairsBlock.HALF.getValueType().cast(half) == BlockHalf.BOTTOM;
            final boolean corner = shape != StairShape.STRAIGHT;
            boolean inside = false;
            boolean left = false;
            
            final Axis axis = block.axis();
            
            switch(shape) {
            case INNER_LEFT:
               left = true;
            case INNER_RIGHT:
               inside = true;
                break;
            case OUTER_LEFT:
                left = true;
                break;
            default:
                break;
            }
            
            if(corner) {
                if(bottom) {
                    if(left) {
                        face = DirectionHelper.counterClockwise(face, axis);
                    }
                } else {
                    if(!left) {
                        face = DirectionHelper.clockwise(face, axis);
                    }
                }
            }
            Stair.setCorner(corner, modelState);
            Stair.setInsideCorner(corner && inside, modelState);
            
            modelState.orientationIndex(ExactEdge.find(Direction.from(axis, bottom ? AxisDirection.NEGATIVE : AxisDirection.POSITIVE), face).ordinal());
        }
        
        return modelState;
    };
}