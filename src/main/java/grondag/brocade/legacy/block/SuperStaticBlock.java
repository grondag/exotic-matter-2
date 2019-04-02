package grondag.brocade.legacy.block;

import grondag.brocade.block.BlockSubstance;
import grondag.brocade.model.state.ISuperModelState;
import grondag.brocade.model.varia.WorldLightOpacity;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ExtendedBlockView;

public class SuperStaticBlock extends SuperBlockPlus {
    private final BlockSubstance substance;
    private final boolean isGeometryFullCube;
    private final WorldLightOpacity worldLightOpacity;

    @SuppressWarnings("null")
    public SuperStaticBlock(String blockName, BlockSubstance substance, ISuperModelState defaultModelState) {
        super(blockName, substance.material, defaultModelState, null);

        // make sure proper shape is set
        ISuperModelState modelState = defaultModelState.clone();
        modelState.setStatic(true);
        this.defaultModelStateBits = modelState.serializeToInts();
        this.isGeometryFullCube = defaultModelState.isCube();
        this.worldLightOpacity = WorldLightOpacity.getClosest(substance, defaultModelState);

        this.substance = substance;
        this.blockHardness = substance.hardness;
        this.blockResistance = substance.resistance;
        this.setHarvestLevel(substance.harvestTool.toolString, substance.harvestLevel);
    }

    @Override
    public BlockSubstance getSubstance(BlockState state, ExtendedBlockView world, BlockPos pos) {
        return this.substance;
    }

    @Override
    public BlockSubstance defaultSubstance() {
        return this.substance;
    }

    @Override
    public boolean isGeometryFullCube(BlockState state) {
        return this.isGeometryFullCube;
    }

    @Override
    public boolean isHypermatter() {
        return this.substance.isHyperMaterial;
    }

    @Override
    protected WorldLightOpacity worldLightOpacity(BlockState state) {
        return this.worldLightOpacity;
    }

}
