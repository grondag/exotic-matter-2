package grondag.brocade.world;

import net.minecraft.util.math.BlockPos;

/** Public interface for Alternator class. Look there for details. */
public interface IAlternator {
	/** 
	 * Returns a uniformly distributed integer (byte) values
	 * between 0 and the alternate count - 1.  Alternate count
	 * is determined when you retrieve the object with getAlternator().
	 */
	int getAlternate(BlockPos pos);
	int getAlternateCount();
}
