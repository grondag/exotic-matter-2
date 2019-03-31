package grondag.brocade.terrain;

import java.util.List;
import java.util.Random;




import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.BlockState;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.block.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.BoundingBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class DepletedFluidBlock extends Block {

    public DepletedFluidBlock() {
        super(Material.STRUCTURE_VOID);
    }

    @Override
    public boolean isTopSolid(BlockState state) {
        return false;
    }

    @Override
    public boolean isFullBlock(BlockState state) {
        return false;
    }

    @Override
    public boolean isTranslucent(BlockState state) {
        return true;
    }

    @Override
    public boolean isBlockNormalCube(BlockState state) {
        return false;
    }

    @Override
    public boolean isNormalCube(BlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(BlockState state) {
        return false;
    }

    @Override
    public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
        return true;
    }

    @Override
    public EnumBlockRenderType getRenderType(BlockState state) {
        return EnumBlockRenderType.INVISIBLE;
    }

    @Override
    public boolean isReplaceable(IBlockAccess worldIn, BlockPos pos) {
        return true;
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, BlockState state,
            BlockPos pos, Direction face) {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    public void addCollisionBoxToList(BlockState state, World worldIn, BlockPos pos,
            BoundingBox entityBox, List<BoundingBox> collidingBoxes, Entity entityIn,
            boolean isActualState) {
        return;
    }

    @Override
    public BoundingBox getCollisionBoundingBox(BlockState blockState,
            IBlockAccess worldIn, BlockPos pos) {
        return Block.NULL_AABB;
    }

    @Override
    public boolean isOpaqueCube(BlockState state) {
        return false;
    }

    @Override
    public boolean canCollideCheck(BlockState state, boolean hitIfLiquid) {
        return false;
    }

    @Override
    public boolean isCollidable() {
        return false;
    }

    @Override
    public int quantityDropped(Random random) {
        return 0;
    }

    @Override
    public int quantityDroppedWithBonus(int fortune, Random random) {
        return 0;
    }

    @Override
    public boolean canSpawnInBlock() {
        return true;
    }

    @Override
    public boolean isNormalCube(BlockState state, IBlockAccess world, BlockPos pos) {
        return false;
    }

    @Override
    public boolean doesSideBlockRendering(BlockState state, IBlockAccess world,
            BlockPos pos, Direction face) {
        return false;
    }

    @Override
    public boolean isSideSolid(BlockState base_state, IBlockAccess world, BlockPos pos,
            Direction side) {
        return false;
    }

    @Override
    public int quantityDropped(BlockState state, int fortune, Random random) {
        return 0;
    }

    @Override
    public boolean canBeReplacedByLeaves(BlockState state, IBlockAccess world,
            BlockPos pos) {
        return true;
    }

    @Override
    public boolean canPlaceTorchOnTop(BlockState state, IBlockAccess world, BlockPos pos) {
        return false;
    }

    @Override
    public boolean addLandingEffects(BlockState state, WorldServer worldObj,
            BlockPos blockPosition, BlockState BlockState, LivingEntity entity,
            int numberOfParticles) {
        return true;
    }

    @Override
    public boolean addHitEffects(BlockState state, World worldObj, RayTraceResult target,
            ParticleManager manager) {
        return true;
    }

    @Override
    public boolean addDestroyEffects(World world, BlockPos pos, ParticleManager manager) {
        return true;
    }

    @Override
    public boolean canRenderInLayer(BlockState state, BlockRenderLayer layer) {
        return false;
    }

}
