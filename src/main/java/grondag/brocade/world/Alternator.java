package grondag.brocade.world;

import java.util.Random;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

/**
 * Fast local randomizer, intended for randomizing block alternates for a block
 * position. We don't use the MineCraft alternate functionality because it is
 * non-deterministic for different block states. This causes undesirable changes
 * to alternate selection when neighbor blocks change. Uses bytes to save space,
 * so can only have up to 127 alternates.
 *
 * "Local" means values repeat every 32 blocks in all directions. Most of our
 * textures aren't noisy enough for repeating patterns to be visible when far
 * enough away to see 32 blocks repeat. The interface hides the implementation,
 * so we can change this later if it becomes a problem.
 * 
 * If the optional blockSize parameter is provided and > 0, then the random
 * values generated are the same for cubes of the given size within the 32x32x32
 * volume. The blockSize value is an exponent of 2 in the range 0 through 5,
 * giving possible sub-cube dimensions of 1, 2, 4, 8, 16 and 32. For example, if
 * blockSize is 4, then all positions in a 16x16x16 chunk will have the same
 * random value and will repeat in 32x16 blocks in each direction.
 */
public class Alternator implements IAlternator {

    protected final byte[][][] mix = new byte[32][32][32];
    private final int alternateCount;

    /** lightweight, privileged-case handler for 0 alternates */
    private static final UnAlternator noAlternative = new UnAlternator();

    /**
     * Convenience factory method.
     */
    public static IAlternator getAlternator(int alternateCount, int seed) {
        return getAlternator(alternateCount, seed, 0);
    }

    public static IAlternator getAlternator(int alternateCount, int seed, int blockSize) {
        if (alternateCount == 1) {
            return noAlternative;
        } else if (blockSize > 0) {
            return new MultiBlockAlternator(alternateCount, seed, blockSize);
        } else {
            return new Alternator(alternateCount, seed);
        }
    }

    /**
     * Creates new alternator that returns uniformly distributed integer (byte)
     * values between 0 and alternateCount - 1. Do not call directly. Use
     * getAlternator instead.
     */
    private Alternator(int alternateCount, int seed) {
        this.alternateCount = alternateCount;
        final Random r = new Random(seed);
        for (int i = 0; i < 32; i++) {
            for (int j = 0; j < 32; j++) {
                for (int k = 0; k < 32; k++) {
                    mix[i][j][k] = (byte) r.nextInt(alternateCount);
                    int tryCount = 0;
                    while (tryCount < 3 && ((i > 0 && mix[i - 1][j][k] == mix[i][j][k])
                            || (j > 0 && mix[i][j - 1][k] == mix[i][j][k])
                            || (k > 0 && mix[i][j][k - 1] == mix[i][j][k]))) {
                        mix[i][j][k] = (byte) r.nextInt(alternateCount);
                        tryCount++;
                    }
                }
            }
        }
    }

    /**
     * Returns a uniformly distributed integer (byte) values between 0 and the
     * alternate count - 1. Alternate count is determined when you retrieve the
     * object with getAlternator().
     */
    @Override
    public int getAlternate(BlockPos pos) {
        return mix[pos.getX() & 31][pos.getY() & 31][pos.getZ() & 31];
    }

    @Override
    public int getAlternateCount() {
        return this.alternateCount;
    }

    private static class MultiBlockAlternator extends Alternator {
        private final int blockSize;

        private MultiBlockAlternator(int alternateCount, int seed, int blockSize) {
            super(alternateCount, seed);
            this.blockSize = MathHelper.clamp(blockSize, 0, 5);
        }

        @Override
        public int getAlternate(BlockPos pos) {
            return mix[(pos.getX() >> blockSize) & 31][(pos.getY() >> blockSize) & 31][(pos.getZ() >> blockSize) & 31];
        }
    }

    /**
     * Handles privileged case of no alternates
     */
    private static class UnAlternator implements IAlternator {
        @Override
        public int getAlternate(BlockPos pos) {
            return 0;
        }

        @Override
        public int getAlternateCount() {
            return 1;
        }
    }

}
