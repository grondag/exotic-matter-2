package grondag.xm.api.connect.species;


import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

@FunctionalInterface
public interface SpeciesFunction {
	int NO_SPECIES = -1;

	/**
	 *
	 * @param world
	 * @param blockState
	 * @param pos
	 * @return Numeric species value >=0 if block has one, {@link #NO_SPECIES} otherwise.
	 */
	int species(BlockGetter world, BlockState blockState, BlockPos pos);

	default int species(BlockGetter world, BlockPos pos) {
		return species(world, world.getBlockState(pos), pos);
	}
}
