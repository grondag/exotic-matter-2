package grondag.xm.api.connect.species;


import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

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
	int species(BlockView world, BlockState blockState, BlockPos pos);

	default int species(BlockView world, BlockPos pos) {
		return species(world, world.getBlockState(pos), pos);
	}
}
