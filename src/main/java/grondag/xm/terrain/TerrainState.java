/*******************************************************************************
 * Copyright 2019 grondag
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package grondag.xm.terrain;

import it.unimi.dsi.fastutil.HashCommon;
import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockView;

import grondag.fermion.bits.BitPacker64;
import grondag.fermion.orientation.api.HorizontalEdge;
import grondag.fermion.orientation.api.HorizontalFace;
import grondag.fermion.position.PackedBlockPos;
import grondag.xm.api.mesh.polygon.PolyHelper;

@Internal
public class TerrainState {

	public final static long FULL_BLOCK_STATE_KEY = TerrainState.computeStateKey(12, new int[] { 12, 12, 12, 12 }, new int[] { 12, 12, 12, 12 }, 0);
	public final static long EMPTY_BLOCK_STATE_KEY = TerrainState.computeStateKey(1, new int[] { 1, 1, 1, 1 }, new int[] { 1, 1, 1, 1 }, 1);

	public final static TerrainState EMPTY_STATE = new TerrainState(EMPTY_BLOCK_STATE_KEY, 0);

	/**
	 * Eight 6-bit blocks that store a corner and side value, plus 4 bits for center
	 * height and 3 bits for offset
	 */
	public final static long STATE_BIT_COUNT = 55;

	public final static long STATE_BIT_MASK = 0x7FFFFFFFFFFFFFL;

	public final static int BLOCK_LEVELS_INT = 12;
	public final static int BLOCK_LEVELS_INT_HALF = BLOCK_LEVELS_INT / 2;
	public final static float BLOCK_LEVELS_FLOAT = BLOCK_LEVELS_INT;
	public final static int MIN_HEIGHT = -23;
	public final static int NO_BLOCK = MIN_HEIGHT - 1;
	public final static int MAX_HEIGHT = 36;

	public final static ITerrainBitConsumer<TerrainState> FACTORY = (t, h) -> new TerrainState(t, h);
	/**
	 * Want to avoid the synchronization penalty of pooled block pos.
	 */
	private static ThreadLocal<BlockPos.Mutable> mutablePos = new ThreadLocal<BlockPos.Mutable>() {

		@Override
		protected BlockPos.Mutable initialValue() {
			return new BlockPos.Mutable();
		}
	};

	// Use these insted of magic number for filler block meta values
	/**
	 * This value is for a height block two below another height block, offset of 2
	 * added to vertex heights
	 */
	//    public final static int FILL_META_DOWN2 = 0;
	//    public final static int FILL_META_DOWN1 = 1;

	/**
	 * This value indicates a top height block, means no offset, no effect on vertex
	 * calculations
	 */
	//    public final static int FILL_META_LEVEL = 2;
	//    public final static int FILL_META_UP1 = 3;
	//    public final static int FILL_META_UP2 = 4;

	//    /**
	//     * Number of possible values for non-center blocks.
	//     * Includes negative values, positive values, zero and NO_BLOCK values.
	//     */
	//    private final static int VALUE_COUNT = -MIN_HEIGHT + MAX_HEIGHT + 1 + 1;

	/**
	 * Returns values -2 through +2 from a triad (3 bits).
	 */
	public static int getYOffsetFromTriad(int triad) {
		return Math.min(4, triad & 7) - 2;
	}

	/**
	 * Stores values from -2 to +2 in a triad (3 bits). Invalid values are handled
	 * same as +1.
	 */
	public static int getTriadWithYOffset(int offset) {
		return Math.min(4, (offset + 2) & 7);
	}

	private final byte centerHeight;
	private final byte sideHeight[] = new byte[4];
	private final byte cornerHeight[] = new byte[4];
	private final byte yOffset;
	private final long stateKey;
	private final int hotness;

	private static final byte SIMPLE_FLAG[] = new byte[4];
	private static final byte SIMPLE_FLAG_TOP = 16;
	private static final byte SIMPLE_FLAG_MOST_SIDES = 32;
	static {
		SIMPLE_FLAG[HorizontalFace.EAST.ordinal()] = 1;
		SIMPLE_FLAG[HorizontalFace.WEST.ordinal()] = 2;
		SIMPLE_FLAG[HorizontalFace.NORTH.ordinal()] = 4;
		SIMPLE_FLAG[HorizontalFace.SOUTH.ordinal()] = 8;
	}

	/** true if model vertex height calculations current */
	private boolean vertexCalcsDone = false;
	/** cache model vertex height calculations */
	private final float midCornerHeight[] = new float[HorizontalEdge.values().length];
	/** cache model vertex height calculations */
	private final float farCornerHeight[] = new float[HorizontalEdge.values().length];
	/** cache model vertex height calculations */
	private final float midSideHeight[] = new float[HorizontalFace.values().length];
	/** cache model vertex height calculations */
	private final float farSideHeight[] = new float[HorizontalFace.values().length];

	private float minVertexHeightExcludingCenter;
	private float maxVertexHeightExcludingCenter;
	private float averageVertexHeightIncludingCenter;

	private byte simpleFlags = 0;

	public final long getStateKey() {
		return stateKey;
	}

	public final int getHotness() {
		return hotness;
	}

	@Override
	public final int hashCode() {
		return (int) HashCommon.mix(stateKey ^ hotness);
	}

	@Override
	public final boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (obj instanceof TerrainState) {
			final TerrainState other = (TerrainState) obj;
			return other.stateKey == stateKey && other.hotness == hotness;
		}
		return false;
	}

	public static long computeStateKey(int centerHeightIn, int[] sideHeightIn, int[] cornerHeightIn, int yOffsetIn) {
		long stateKey = (centerHeightIn - 1) | getTriadWithYOffset(yOffsetIn) << 4;

		int shift = 7;
		for (int i = 0; i < 4; i++) {
			stateKey |= ((long) ((sideHeightIn[i] - NO_BLOCK)) << shift);
			shift += 6;
			stateKey |= ((long) ((cornerHeightIn[i] - NO_BLOCK)) << shift);
			shift += 6;
		}
		return stateKey;
	}

	public TerrainState(int centerHeightIn, int[] sideHeightIn, int[] cornerHeightIn, int yOffsetIn) {
		this(computeStateKey(centerHeightIn, sideHeightIn, cornerHeightIn, yOffsetIn), 0);
	}

	public TerrainState(long stateKey, int hotness) {
		this.stateKey = stateKey;
		this.hotness = hotness;
		centerHeight = (byte) ((stateKey & 0xF) + 1);
		yOffset = (byte) getYOffsetFromTriad((int) ((stateKey >> 4) & 0x7));

		int shift = 7;
		for (int i = 0; i < 4; i++) {
			sideHeight[i] = (byte) (((stateKey >> shift) & 63) + NO_BLOCK);
			shift += 6;
			cornerHeight[i] = (byte) (((stateKey >> shift) & 63) + NO_BLOCK);
			shift += 6;
		}
	}

	/**
	 * Rendering height of center block ranges from 1 to 12 and is stored in state
	 * key as values 0-11.
	 */
	public final int centerHeight() {
		return centerHeight;
	}

	// Rendering height of corner and side neighbors ranges
	// from -24 to 36.
	public final int height(HorizontalFace side) {
		return sideHeight[side.ordinal()];
	}

	public final int height(HorizontalEdge corner) {
		return cornerHeight[corner.ordinal()];
	}

	public final int height(final int x, final int z) {
		switch (x + 1) {
		case 0:
			// west
			switch (z + 1) {
			case 0:
				// north
				return this.height(HorizontalEdge.NORTH_WEST);
			case 1:
				// center (n-s)
				return this.height(HorizontalFace.WEST);
			case 2:
				// south
				return this.height(HorizontalEdge.SOUTH_WEST);
			default:
				return 0;
			}
		case 1:
			// center (e-w)
			switch (z + 1) {
			case 0:
				// north
				return this.height(HorizontalFace.NORTH);
			case 1:
				// center (n-s)
				return centerHeight();
			case 2:
				// south
				return this.height(HorizontalFace.SOUTH);
			default:
				return 0;
			}
		case 2:
			// east
			switch (z + 1) {
			case 0:
				// north
				return this.height(HorizontalEdge.NORTH_EAST);
			case 1:
				// center (n-s)
				return this.height(HorizontalFace.EAST);
			case 2:
				// south
				return this.height(HorizontalEdge.SOUTH_EAST);
			default:
				return 0;
			}
		default:
			return 0;
		}
	}

	public final int getCenterHotness() {
		return CENTER_HOTNESS.getValue(hotness);
	}

	/**
	 * Returns heat value using relative x, z coordinate. 0,0 represents center.
	 * Values outside the range -1, 1 return 0;
	 */
	public final int neighborHotness(final int x, final int z) {
		switch (x + 1) {
		case 0:
			// west
			switch (z + 1) {
			case 0:
				// north
				return this.neighborHotness(HorizontalEdge.NORTH_WEST);
			case 1:
				// center (n-s)
				return this.neighborHotness(HorizontalFace.WEST);
			case 2:
				// south
				return this.neighborHotness(HorizontalEdge.SOUTH_WEST);
			default:
				return 0;
			}
		case 1:
			// center (e-w)
			switch (z + 1) {
			case 0:
				// north
				return this.neighborHotness(HorizontalFace.NORTH);
			case 1:
				// center (n-s)
				return getCenterHotness();
			case 2:
				// south
				return this.neighborHotness(HorizontalFace.SOUTH);
			default:
				return 0;
			}
		case 2:
			// east
			switch (z + 1) {
			case 0:
				// north
				return this.neighborHotness(HorizontalEdge.NORTH_EAST);
			case 1:
				// center (n-s)
				return this.neighborHotness(HorizontalFace.EAST);
			case 2:
				// south
				return this.neighborHotness(HorizontalEdge.SOUTH_EAST);
			default:
				return 0;
			}
		default:
			return 0;
		}
	}

	public int getYOffset() {
		return yOffset;
	}

	/**
	 * Returns how many filler blocks are needed on top to cover a cut surface.
	 * Possible return values are 0, 1 and 2.
	 */
	public int topFillerNeeded() {
		// filler only applies to level blocks
		if (yOffset != 0)
			return 0;
		refreshVertexCalculationsIfNeeded();

		double max = 0;

		// center vertex does not matter if top is simplified to a single quad
		if (!isTopSimple()) {
			max = Math.max(max, getCenterVertexHeight());
		}

		for (int i = 0; i < 4; i++) {
			// side does not matter if side geometry is simplified
			if (!isSideSimple(i)) {
				max = Math.max(max, midSideHeight[i]);
			}
			max = Math.max(max, midCornerHeight[i]);
		}

		return max > 2.01 ? 2 : max > 1.01 ? 1 : 0;
	}

	public boolean isSideSimple(HorizontalFace face) {
		return this.isSideSimple(face.ordinal());
	}

	private boolean isSideSimple(int ordinal) {
		refreshVertexCalculationsIfNeeded();
		final byte flag = SIMPLE_FLAG[ordinal];
		return (simpleFlags & flag) == flag;
	}

	/**
	 * True if top can be simplified to no more than two tris. Is true implies all
	 * sides are simple. Exception: top is not allowed to be simple if this block is
	 * hot, because that would defeat per-vertex lighting.
	 */
	public boolean isTopSimple() {
		refreshVertexCalculationsIfNeeded();
		return (simpleFlags & SIMPLE_FLAG_TOP) == SIMPLE_FLAG_TOP;
	}

	/**
	 * True if at least two sides are simple. Exception: sides are not allowed to be
	 * simple if this block is hot, because that would defeat per-vertex lighting.
	 */
	public boolean areMostSidesSimple() {
		refreshVertexCalculationsIfNeeded();
		return (simpleFlags & SIMPLE_FLAG_MOST_SIDES) == SIMPLE_FLAG_MOST_SIDES;
	}

	/**
	 * Returns true of geometry of flow block should be a full cube based on self
	 * and neighboring flow blocks. Returns false if otherwise or if is not a flow
	 * block.
	 */
	public boolean isFullCube() {
		refreshVertexCalculationsIfNeeded();
		final double top = 1.0 + yOffset + PolyHelper.EPSILON;

		// center vertex does not matter if top is simplified to a single quad
		if (!isTopSimple()) {
			if (getCenterVertexHeight() < top)
				return false;
		}

		for (int i = 0; i < 4; i++) {
			// side does not matter if side geometry is simplified
			if (!isSideSimple(i)) {
				if (midSideHeight[i] < top)
					return false;
			}

			if (midCornerHeight[i] < top)
				return false;
		}
		return true;
	}

	/**
	 * Returns true if geometry of flow block has nothing in it.
	 */
	public boolean isEmpty() {
		refreshVertexCalculationsIfNeeded();
		final double bottom = 0.0 + yOffset;

		// center vertex does not matter if top is simplified to a single quad
		if (!isTopSimple()) {
			if (getCenterVertexHeight() > bottom)
				return false;
		}

		for (int i = 0; i < 4; i++) {
			// side does not matter if side geometry is simplified
			if (!isSideSimple(i)) {
				if (midSideHeight[i] > bottom)
					return false;
			}

			if (midCornerHeight[i] > bottom)
				return false;
		}
		return true;
	}

	/**
	 * how much sky light is blocked by this shape. 0 = none, 14 = most, 255 = all
	 */
	public int verticalOcclusion() {
		refreshVertexCalculationsIfNeeded();
		final double bottom = 0.0 + yOffset;

		int aboveCount = 0;

		for (int i = 0; i < 4; i++) {
			if (midSideHeight[i] > bottom) {
				aboveCount++;
			}
			if (midCornerHeight[i] > bottom) {
				aboveCount++;
			}
		}

		if (getCenterVertexHeight() > bottom) {
			aboveCount *= 2;
		}

		return aboveCount >= 16 ? 255 : aboveCount;
	}

	public float getCenterVertexHeight() {
		return centerHeight() / BLOCK_LEVELS_FLOAT;
	}

	public float getFarCornerVertexHeight(HorizontalEdge corner) {
		refreshVertexCalculationsIfNeeded();
		return farCornerHeight[corner.ordinal()];
	}

	public float getMidCornerVertexHeight(HorizontalEdge corner) {
		refreshVertexCalculationsIfNeeded();
		return midCornerHeight[corner.ordinal()];
	}

	public float getFarSideVertexHeight(HorizontalFace face) {
		refreshVertexCalculationsIfNeeded();
		return farSideHeight[face.ordinal()];
	}

	public float getMidSideVertexHeight(HorizontalFace face) {
		refreshVertexCalculationsIfNeeded();
		return midSideHeight[face.ordinal()];
	}

	private void refreshVertexCalculationsIfNeeded() {
		if (vertexCalcsDone)
			return;

		final float centerHeight = getCenterVertexHeight();

		float max = Float.MIN_VALUE;
		float min = Float.MAX_VALUE;
		float total = centerHeight;

		for (final HorizontalFace side : HorizontalFace.values()) {
			final float h = calcMidSideVertexHeight(side);
			total += h;
			if (h > max) {
				max = h;
			}
			if (h < min) {
				min = h;
			}

			midSideHeight[side.ordinal()] = h;
			farSideHeight[side.ordinal()] = calcFarSideVertexHeight(side);

		}

		for (final HorizontalEdge corner : HorizontalEdge.values()) {
			final float h = calcMidCornerVertexHeight(corner);

			total += h;
			if (h > max) {
				max = h;
			}
			if (h < min) {
				min = h;
			}

			midCornerHeight[corner.ordinal()] = h;
			farCornerHeight[corner.ordinal()] = calcFarCornerVertexHeight(corner);
		}

		maxVertexHeightExcludingCenter = max;
		minVertexHeightExcludingCenter = min;
		averageVertexHeightIncludingCenter = total / 9;

		// determine if sides and top geometry can be simplified
		// simplification not possible if block is hot - to preserve per-vertex lighting
		if (getCenterHotness() == 0) {
			boolean topIsSimple = true;
			int simpleSideCount = 0;

			for (final HorizontalFace side : HorizontalFace.values()) {
				float avg = midCornerHeight[HorizontalEdge.find(side, side.left()).ordinal()];
				avg += midCornerHeight[HorizontalEdge.find(side, side.right()).ordinal()];
				avg /= 2;
				final boolean sideIsSimple = Math.abs(avg - midSideHeight[side.ordinal()]) < PolyHelper.EPSILON;
				if (sideIsSimple) {
					simpleFlags |= SIMPLE_FLAG[side.ordinal()];
					simpleSideCount++;
				} else {
					topIsSimple = false;
				}
			}

			if (simpleSideCount > 1) {
				simpleFlags |= SIMPLE_FLAG_MOST_SIDES;
			}

			if (topIsSimple) {
				final float cross1 = (midCornerHeight[HorizontalEdge.NORTH_EAST.ordinal()] + midCornerHeight[HorizontalEdge.SOUTH_WEST.ordinal()]) / 2.0f;
				final float cross2 = (midCornerHeight[HorizontalEdge.NORTH_WEST.ordinal()] + midCornerHeight[HorizontalEdge.SOUTH_EAST.ordinal()]) / 2.0f;
				if (Math.abs(cross1 - cross2) < 1.0f) {
					simpleFlags |= SIMPLE_FLAG_TOP;
				}
			}
		}
		vertexCalcsDone = true;

	}

	private float calcFarCornerVertexHeight(HorizontalEdge corner) {
		int heightCorner = height(corner);

		if (heightCorner == TerrainState.NO_BLOCK) {
			final int max = Math.max(Math.max(height(corner.left), height(corner.right)), centerHeight());
			heightCorner = max - BLOCK_LEVELS_INT;
		}

		return (heightCorner) / BLOCK_LEVELS_FLOAT;
	}

	private float calcMidCornerVertexHeight(HorizontalEdge corner) {
		int heightSide1 = height(corner.left);
		int heightSide2 = height(corner.right);
		int heightCorner = height(corner);

		final int max = Math.max(Math.max(heightSide1, heightSide2), Math.max(heightCorner, centerHeight())) - BLOCK_LEVELS_INT;

		if (heightSide1 == TerrainState.NO_BLOCK) {
			heightSide1 = max;
		}
		if (heightSide2 == TerrainState.NO_BLOCK) {
			heightSide2 = max;
		}
		if (heightCorner == TerrainState.NO_BLOCK) {
			heightCorner = max;
		}

		final float numerator = centerHeight() + heightSide1 + heightSide2 + heightCorner;

		return numerator / (BLOCK_LEVELS_FLOAT * 4F);

	}

	public final int neighborHotness(HorizontalEdge corner) {
		return CORNER_HOTNESS[corner.ordinal()].getValue(hotness);
	}

	public final int neighborHotness(HorizontalFace face) {
		return SIDE_HOTNESS[face.ordinal()].getValue(hotness);
	}

	/**
	 * Returns heat value using relative x, z coordinate for the corners of this
	 * block. 0,0 represents center. Values outside the range -1, 1 return 0;
	 */
	public final float midHotness(final int x, final int z) {
		switch (x + 1) {
		case 0:
			// west
			switch (z + 1) {
			case 0:
				// north
				return this.midHotness(HorizontalEdge.NORTH_WEST);
			case 1:
				// center (n-s)
				return this.midHotness(HorizontalFace.WEST);
			case 2:
				// south
				return this.midHotness(HorizontalEdge.SOUTH_WEST);
			default:
				return 0;
			}
		case 1:
			// center (e-w)
			switch (z + 1) {
			case 0:
				// north
				return this.midHotness(HorizontalFace.NORTH);
			case 1:
				// center (n-s)
				return getCenterHotness();
			case 2:
				// south
				return this.midHotness(HorizontalFace.SOUTH);
			default:
				return 0;
			}
		case 2:
			// east
			switch (z + 1) {
			case 0:
				// north
				return this.midHotness(HorizontalEdge.NORTH_EAST);
			case 1:
				// center (n-s)
				return this.midHotness(HorizontalFace.EAST);
			case 2:
				// south
				return this.midHotness(HorizontalEdge.SOUTH_EAST);
			default:
				return 0;
			}
		default:
			return 0;
		}
	}

	/**
	 * If at least 3 block touching corner are hot, returns average heat. Returns 0
	 * if 2 or fewer. This block must be hot to be non-zero.
	 */
	public final float midHotness(HorizontalEdge corner) {
		final int centerHeat = getCenterHotness();
		if (centerHeat == 0)
			return 0;

		final int heatSide1 = SIDE_HOTNESS[corner.left.ordinal()].getValue(hotness);
		final int heatSide2 = SIDE_HOTNESS[corner.right.ordinal()].getValue(hotness);
		final int heatCorner = CORNER_HOTNESS[corner.ordinal()].getValue(hotness);

		if (heatSide1 == 0) {
			if (heatSide2 == 0)
				return 0;
			else
				return heatCorner == 0 ? 0 : (centerHeat + heatSide2 + heatCorner) / 3f;
		} else if (heatSide2 == 0)
			// heatside1 is known to be hot at this point
			return heatCorner == 0 ? 0 : (centerHeat + heatSide1 + heatCorner) / 3f;
		else
			// both sides are hot
			return heatCorner == 0 ? (centerHeat + heatSide1 + heatSide2) / 3f : (centerHeat + heatSide1 + heatSide2 + heatCorner) / 4f;
	}

	/**
	 * If both this block and side block are hot, is average heat, rounded up. 0.2
	 * otherwise.
	 */
	public final float midHotness(HorizontalFace face) {
		final int centerHeat = getCenterHotness();
		if (centerHeat == 0)
			return 0;

		final int heatSide = SIDE_HOTNESS[face.ordinal()].getValue(hotness);

		return heatSide == 0 ? 0.2f : (heatSide + centerHeat) / 2f;

	}

	public float lavaAlpha(int x, int z) {
		switch (x + 1) {
		case 0:
			// west
			switch (z + 1) {
			case 0:
				// north
				return this.lavaAlpha(HorizontalEdge.NORTH_WEST);
			case 1:
				// center (n-s)
				return this.lavaAlpha(HorizontalFace.WEST);
			case 2:
				// south
				return this.lavaAlpha(HorizontalEdge.SOUTH_WEST);
			default:
				return 0;
			}
		case 1:
			// center (e-w)
			switch (z + 1) {
			case 0:
				// north
				return this.lavaAlpha(HorizontalFace.NORTH);
			case 1:
				// center (n-s)
				return getCenterHotness() == 0 ? 0 : 1;
			case 2:
				// south
				return this.lavaAlpha(HorizontalFace.SOUTH);
			default:
				return 0;
			}
		case 2:
			// east
			switch (z + 1) {
			case 0:
				// north
				return this.lavaAlpha(HorizontalEdge.NORTH_EAST);
			case 1:
				// center (n-s)
				return this.lavaAlpha(HorizontalFace.EAST);
			case 2:
				// south
				return this.lavaAlpha(HorizontalEdge.SOUTH_EAST);
			default:
				return 0;
			}
		default:
			return 0;
		}
	}

	private final float lavaAlpha(HorizontalEdge corner) {
		if (getCenterHotness() == 0)
			return 0;
		if (this.neighborHotness(corner) == 0 && this.height(corner) != NO_BLOCK)
			return 0;
		if (this.neighborHotness(corner.left) == 0 && this.height(corner.left) != NO_BLOCK)
			return 0;
		if (this.neighborHotness(corner.right) == 0 && this.height(corner.right) != NO_BLOCK)
			return 0;

		return 1;
	}

	private final float lavaAlpha(HorizontalFace face) {
		if (getCenterHotness() == 0)
			return 0;
		if (this.neighborHotness(face) == 0 && this.height(face) != NO_BLOCK)
			return 0;

		return 1;
	}

	public float crustAlpha(int x, int z) {
		switch (x + 1) {
		case 0:
			// west
			switch (z + 1) {
			case 0:
				// north
				return this.crustAlpha(HorizontalEdge.NORTH_WEST);
			case 1:
				// center (n-s)
				return this.crustAlpha(HorizontalFace.WEST);
			case 2:
				// south
				return this.crustAlpha(HorizontalEdge.SOUTH_WEST);
			default:
				return 0;
			}
		case 1:
			// center (e-w)
			switch (z + 1) {
			case 0:
				// north
				return this.crustAlpha(HorizontalFace.NORTH);
			case 1:
				// center (n-s)
				return crustAlphaCenter();
			case 2:
				// south
				return this.crustAlpha(HorizontalFace.SOUTH);
			default:
				return 0;
			}
		case 2:
			// east
			switch (z + 1) {
			case 0:
				// north
				return this.crustAlpha(HorizontalEdge.NORTH_EAST);
			case 1:
				// center (n-s)
				return this.crustAlpha(HorizontalFace.EAST);
			case 2:
				// south
				return this.crustAlpha(HorizontalEdge.SOUTH_EAST);
			default:
				return 0;
			}
		default:
			return 0;
		}
	}

	private final float crustAlphaCenter() {
		if (getCenterHotness() == IHotBlock.MAX_HEAT) {
			// this is a lava block
			if (this.neighborHotness(HorizontalFace.EAST) < IHotBlock.MAX_HEAT && this.height(HorizontalFace.EAST) != NO_BLOCK)
				return 0.5f;
			if (this.neighborHotness(HorizontalFace.WEST) < IHotBlock.MAX_HEAT && this.height(HorizontalFace.WEST) != NO_BLOCK)
				return 0.5f;
			if (this.neighborHotness(HorizontalFace.NORTH) < IHotBlock.MAX_HEAT && this.height(HorizontalFace.NORTH) != NO_BLOCK)
				return 0.5f;
			if (this.neighborHotness(HorizontalFace.SOUTH) < IHotBlock.MAX_HEAT && this.height(HorizontalFace.SOUTH) != NO_BLOCK)
				return 0.5f;
			return 0;
		}

		return 1;
	}

	private final float crustAlpha(HorizontalEdge corner) {
		final int ch = getCenterHotness();
		if (ch == 0)
			return 1;
		if (ch == IHotBlock.MAX_HEAT) {
			// this is a lava block
			if (this.neighborHotness(corner) < IHotBlock.MAX_HEAT && this.height(corner) != NO_BLOCK)
				return 0.5f;
			if (this.neighborHotness(corner.left) < IHotBlock.MAX_HEAT && this.height(corner.left) != NO_BLOCK)
				return 0.5f;
			if (this.neighborHotness(corner.right) < IHotBlock.MAX_HEAT && this.height(corner.right) != NO_BLOCK)
				return 0.5f;
			return 0;
		} else // hot basalt
		{
			if (this.neighborHotness(corner) == IHotBlock.MAX_HEAT)
				return 0.5f;
			if (this.neighborHotness(corner.left) == IHotBlock.MAX_HEAT)
				return 0.5f;
			if (this.neighborHotness(corner.right) == IHotBlock.MAX_HEAT)
				return 0.5f;
			return 1;
		}
	}

	private final float crustAlpha(HorizontalFace face) {
		final int ch = getCenterHotness();
		if (ch == 0)
			return 1;
		if (ch == IHotBlock.MAX_HEAT) {
			// this is a lava block
			if (this.neighborHotness(face) < IHotBlock.MAX_HEAT && this.height(face) != NO_BLOCK)
				return 0.5f;
			return 0;
		} else // hot basalt
		{
			if (this.neighborHotness(face) == IHotBlock.MAX_HEAT)
				return 0.5f;
			return 1;
		}
	}

	/**
	 * How opaque/cool crust should appear. <br>
	 * 1 = look like cool basalt. 0 = look like normal hot surface.<br>
	 * Will be 1 for corners and sides that border cool basalt.<br>
	 * Will be 0 for corners and sides that border a non-flow block or a hot block.
	 */
	public float edgeAlpha(int x, int z) {
		switch (x + 1) {
		case 0:
			// west
			switch (z + 1) {
			case 0:
				// north
				return this.edgeAlpha(HorizontalEdge.NORTH_WEST);
			case 1:
				// center (n-s)
				return this.edgeAlpha(HorizontalFace.WEST);
			case 2:
				// south
				return this.edgeAlpha(HorizontalEdge.SOUTH_WEST);
			default:
				return 0;
			}
		case 1:
			// center (e-w)
			switch (z + 1) {
			case 0:
				// north
				return this.edgeAlpha(HorizontalFace.NORTH);
			case 1:
				// center (n-s)
				return 0;
			case 2:
				// south
				return this.edgeAlpha(HorizontalFace.SOUTH);
			default:
				return 0;
			}
		case 2:
			// east
			switch (z + 1) {
			case 0:
				// north
				return this.edgeAlpha(HorizontalEdge.NORTH_EAST);
			case 1:
				// center (n-s)
				return this.edgeAlpha(HorizontalFace.EAST);
			case 2:
				// south
				return this.edgeAlpha(HorizontalEdge.SOUTH_EAST);
			default:
				return 0;
			}
		default:
			return 0;
		}
	}

	private final float edgeAlpha(HorizontalEdge corner) {
		if (this.height(corner) != NO_BLOCK && this.neighborHotness(corner) == 0)
			return 1;
		if (this.height(corner.left) != NO_BLOCK && this.neighborHotness(corner.left) == 0)
			return 1;
		if (this.height(corner.right) != NO_BLOCK && this.neighborHotness(corner.right) == 0)
			return 1;
		return 0;
	}

	private final float edgeAlpha(HorizontalFace face) {
		if (this.height(face) != NO_BLOCK && this.neighborHotness(face) == 0)
			return 1;
		else
			return 0;
	}

	private float calcFarSideVertexHeight(HorizontalFace face) {
		return (height(face) == TerrainState.NO_BLOCK ? centerHeight() - BLOCK_LEVELS_INT : (height(face)) / BLOCK_LEVELS_FLOAT);
	}

	private float calcMidSideVertexHeight(HorizontalFace face) {
		final float sideHeight = height(face) == TerrainState.NO_BLOCK ? centerHeight() - BLOCK_LEVELS_INT : (float) height(face);
		return (sideHeight + centerHeight()) / (BLOCK_LEVELS_FLOAT * 2F);
	}

	@Override
	public String toString() {
		String retval = "CENTER=" + centerHeight();
		for (final HorizontalFace side : HorizontalFace.values()) {
			retval += " " + side.name() + "=" + this.height(side);
		}
		for (final HorizontalEdge corner : HorizontalEdge.values()) {
			retval += " " + corner.name() + "=" + this.height(corner);
		}
		retval += " Y-OFFSET=" + yOffset;
		return retval;
	}

	public int concavity() {
		int count = 0;

		final int center = centerHeight();
		for (final HorizontalFace side : HorizontalFace.values()) {
			count += Math.max(0, this.height(side) - center) / 12;
		}
		for (final HorizontalEdge corner : HorizontalEdge.values()) {
			count += Math.max(0, this.height(corner) - center) / 12;
		}
		return count;
	}

	public int spread() {

		final int center = centerHeight();
		int min = center;
		int max = center;
		for (final HorizontalFace side : HorizontalFace.values()) {
			final int h = this.height(side);
			if (h > max) {
				max = h;
			} else if (h < min) {
				min = h;
			}
		}
		for (final HorizontalEdge corner : HorizontalEdge.values()) {
			final int h = this.height(corner);
			if (h > max) {
				max = h;
			} else if (h < min) {
				min = h;
			}
		}
		return max - min;
	}

	public int divergence() {

		final int center = centerHeight();
		int div = 0;
		for (final HorizontalFace side : HorizontalFace.values()) {
			div += Math.abs(this.height(side) - center);
		}
		for (final HorizontalEdge corner : HorizontalEdge.values()) {
			div += Math.abs(this.height(corner) - center);
		}
		return div;
	}

	public static <T> T produceBitsFromWorldStatically(BlockState state, BlockView world, BlockPos pos, ITerrainBitConsumer<T> consumer) {
		return produceBitsFromWorldStatically(TerrainBlockHelper.isFlowFiller(state), state, world, pos, consumer);
	}

	public static TerrainState terrainState(BlockView world, BlockState state, BlockPos pos) {
		if (!TerrainBlockHelper.isFlowBlock(state))
			return TerrainState.EMPTY_STATE;
		return produceBitsFromWorldStatically(state, world, pos, TerrainState.FACTORY);
	}

	public static final BitPacker64<Void> HOTNESS_PACKER = new BitPacker64<>(null, null);
	public static final BitPacker64<Void>.IntElement CENTER_HOTNESS = HOTNESS_PACKER.createIntElement(6);
	@SuppressWarnings("unchecked")
	public static final BitPacker64<Void>.IntElement[] CORNER_HOTNESS = (BitPacker64<Void>.IntElement[]) new BitPacker64<?>.IntElement[4];
	@SuppressWarnings("unchecked")
	public static final BitPacker64<Void>.IntElement[] SIDE_HOTNESS = (BitPacker64<Void>.IntElement[]) new BitPacker64<?>.IntElement[4];

	public static final int ALL_HOT;
	static {
		SIDE_HOTNESS[HorizontalFace.NORTH.ordinal()] = HOTNESS_PACKER.createIntElement(IHotBlock.HEAT_LEVEL_COUNT);
		SIDE_HOTNESS[HorizontalFace.EAST.ordinal()] = HOTNESS_PACKER.createIntElement(IHotBlock.HEAT_LEVEL_COUNT);
		SIDE_HOTNESS[HorizontalFace.SOUTH.ordinal()] = HOTNESS_PACKER.createIntElement(IHotBlock.HEAT_LEVEL_COUNT);
		SIDE_HOTNESS[HorizontalFace.WEST.ordinal()] = HOTNESS_PACKER.createIntElement(IHotBlock.HEAT_LEVEL_COUNT);

		CORNER_HOTNESS[HorizontalEdge.NORTH_EAST.ordinal()] = HOTNESS_PACKER.createIntElement(IHotBlock.HEAT_LEVEL_COUNT);
		CORNER_HOTNESS[HorizontalEdge.NORTH_WEST.ordinal()] = HOTNESS_PACKER.createIntElement(IHotBlock.HEAT_LEVEL_COUNT);
		CORNER_HOTNESS[HorizontalEdge.SOUTH_EAST.ordinal()] = HOTNESS_PACKER.createIntElement(IHotBlock.HEAT_LEVEL_COUNT);
		CORNER_HOTNESS[HorizontalEdge.SOUTH_WEST.ordinal()] = HOTNESS_PACKER.createIntElement(IHotBlock.HEAT_LEVEL_COUNT);
		ALL_HOT = (int) HOTNESS_PACKER.bitMask();

	}

	private static <T> T produceBitsFromWorldStatically(boolean isFlowFiller, BlockState state, BlockView world, final BlockPos pos,
			ITerrainBitConsumer<T> consumer) {
		final int sideHeight[] = new int[4];
		final int cornerHeight[] = new int[4];
		int yOffset = 0;

		long hotness = 0;

		final BlockPos.Mutable mPos = mutablePos.get();

		// HardScience.log.info("flowstate getBitsFromWorld @" + pos.toString());

		int yOrigin = pos.getY();
		BlockState originState = state;
		if (isFlowFiller) {
			final int offset = TerrainBlockHelper.getYOffsetFromState(state);
			yOrigin -= offset;
			yOffset = offset;

			mPos.set(pos.getX(), pos.getY() - offset, pos.getZ());
			originState = world.getBlockState(mPos);
			if (!TerrainBlockHelper.isFlowHeight(originState))
				return consumer.apply(EMPTY_BLOCK_STATE_KEY, 0);
		} else {
			// If under another flow height block, handle similar to filler block.
			// Not a perfect fix if they are stacked, but shouldn't normally be.
			// HardScience.log.info("flowstate is height block");

			// try to use block above as height origin
			mPos.set(pos.getX(), pos.getY() + 2, pos.getZ());
			originState = world.getBlockState(mPos);
			if (TerrainBlockHelper.isFlowHeight(originState)) {
				yOrigin += 2;
				yOffset = -2;
				// HardScience.log.info("origin 2 up");
			} else {
				mPos.set(pos.getX(), pos.getY() + 1, pos.getZ());
				originState = world.getBlockState(mPos);
				if (TerrainBlockHelper.isFlowHeight(originState)) {
					yOrigin += 1;
					yOffset = -1;
					// HardScience.log.info("origin 1 up");
				} else {
					// didn't work, handle as normal height block
					originState = state;
					// HardScience.log.info("origin self");
				}
			}
		}

		// PERF: use packed block pos instead of mutable here
		final int[][] neighborHeight = new int[3][3];
		neighborHeight[1][1] = TerrainBlockHelper.getFlowHeightFromState(originState);
		final int centerHeight = neighborHeight[1][1];

		if (centerHeight > 0) {
			hotness = CENTER_HOTNESS.setValue(TerrainBlockHelper.getHotness(originState), hotness);
		}
		final boolean hasHotness = hotness != 0;

		for (int x = 0; x < 3; x++) {
			for (int z = 0; z < 3; z++) {
				if (x == 1 && z == 1) {
					continue;
				}
				mPos.set(pos.getX() - 1 + x, yOrigin, pos.getZ() - 1 + z);

				// use cache if available
				neighborHeight[x][z] = TerrainBlockHelper.getFlowHeightFromState(world.getBlockState(mPos));
			}
		}

		for (final HorizontalFace side : HorizontalFace.values()) {
			final int x = side.vector.getX();
			final int z = side.vector.getZ();
			final int h = neighborHeight[x + 1][z + 1];
			sideHeight[side.ordinal()] = h;
			if (h != TerrainState.NO_BLOCK && hasHotness) {
				final int y = yOrigin - 2 + (h - TerrainState.MIN_HEIGHT) / TerrainState.BLOCK_LEVELS_INT;
				mPos.set(pos.getX() + x, y, pos.getZ() + z);
				final int heat = TerrainBlockHelper.getHotness(world.getBlockState(mPos));
				if (heat != 0) {
					hotness = SIDE_HOTNESS[side.ordinal()].setValue(heat, hotness);
				}
			}
		}

		for (final HorizontalEdge corner : HorizontalEdge.values()) {
			final int x = corner.vector.getX();
			final int z = corner.vector.getZ();
			final int h = neighborHeight[x + 1][z + 1];
			cornerHeight[corner.ordinal()] = h;

			if (h != TerrainState.NO_BLOCK && hasHotness) {
				final int y = yOrigin - 2 + (h - TerrainState.MIN_HEIGHT) / TerrainState.BLOCK_LEVELS_INT;
				mPos.set(pos.getX() + x, y, pos.getZ() + z);
				final int heat = TerrainBlockHelper.getHotness(world.getBlockState(mPos));
				if (heat != 0) {
					hotness = CORNER_HOTNESS[corner.ordinal()].setValue(heat, hotness);
				}
			}
		}

		return consumer.apply(computeStateKey(centerHeight, sideHeight, cornerHeight, yOffset), (int) hotness);

	}

	/**
	 * Pass in pos with Y of flow block for which we are getting data. Returns
	 * relative flow height based on blocks 2 above through 2 down. Gets called
	 * frequently, thus the use of mutable pos.
	 */
	public static int getFlowHeight(BlockView world, long packedBlockPos) {
		final Mutable mPos = mutablePos.get();
		BlockState state = world.getBlockState(PackedBlockPos.unpackTo(PackedBlockPos.up(packedBlockPos, 2), mPos));
		int h = TerrainBlockHelper.getFlowHeightFromState(state);
		if (h > 0)
			return 2 * BLOCK_LEVELS_INT + h;

		state = world.getBlockState(PackedBlockPos.unpackTo(PackedBlockPos.up(packedBlockPos), mPos));
		h = TerrainBlockHelper.getFlowHeightFromState(state);
		if (h > 0)
			return BLOCK_LEVELS_INT + h;

		state = world.getBlockState(PackedBlockPos.unpackTo(packedBlockPos, mPos));
		h = TerrainBlockHelper.getFlowHeightFromState(state);
		if (h > 0)
			return h;

		state = world.getBlockState(PackedBlockPos.unpackTo(PackedBlockPos.down(packedBlockPos), mPos));
		h = TerrainBlockHelper.getFlowHeightFromState(state);
		if (h > 0)
			return -BLOCK_LEVELS_INT + h;

		state = world.getBlockState(PackedBlockPos.unpackTo(PackedBlockPos.down(packedBlockPos, 2), mPos));
		h = TerrainBlockHelper.getFlowHeightFromState(state);
		if (h > 0)
			return -2 * BLOCK_LEVELS_INT + h;

		return NO_BLOCK;
	}

	/**
	 * Amount of lava, in fluid levels, that should be retained on top of this
	 * block. Designed to promote smooth terrain generation by acting similar to a
	 * box filter.
	 * <p>
	 *
	 * Computed based on slopes of lines from side and corner middle verticesto
	 * center vertex.
	 * <p>
	 *
	 * Return values are clamped to the range from 1 level to 18 levels (1.5 blocks)
	 */
	public int retentionLevels() {
		refreshVertexCalculationsIfNeeded();

		final float center = getCenterVertexHeight();

		final float max = maxVertexHeightExcludingCenter;
		final float min = minVertexHeightExcludingCenter;
		final float avg = averageVertexHeightIncludingCenter;

		final float drop = max - min;

		// no drop gives one half block of retention
		if (drop == 0)
			return BLOCK_LEVELS_INT_HALF;

		/** essentially the distance from a box filter result */
		final float diffFromAvgLevels = (avg - center) * BLOCK_LEVELS_INT;

		int result;

		if (center <= min) {
			// center is (or shares) lowest point
			result = Math.round(diffFromAvgLevels) + BLOCK_LEVELS_INT_HALF;
		} else {
			// center is part of a slope
			result = drop < 1 ? Math.round(diffFromAvgLevels + (1 - drop) * BLOCK_LEVELS_INT_HALF) : Math.round(diffFromAvgLevels);
		}

		return MathHelper.clamp(result, 1, BLOCK_LEVELS_INT + BLOCK_LEVELS_INT_HALF);
	}

	@FunctionalInterface
	public interface INeighborConsumer {
		void accept(long packedBlockPos, boolean isSurfaceBlock);
	}

	/**
	 * Calls consumer with packed positions of all neighboring height blocks either
	 * influencing or influenced by a block with this state at the given position.
	 * Includes blocks underneath the surface, in addition to surface blocks. Does
	 * not include the block at the given position.
	 */
	public void produceNeighbors(long myPackedPosition, INeighborConsumer consumer) {
		// does't apply to filler blocks
		if (yOffset != 0)
			return;

		myPackedPosition = PackedBlockPos.down(myPackedPosition, 2);
		produceNeighborsInner(PackedBlockPos.east(myPackedPosition), height(HorizontalFace.EAST), consumer);
		produceNeighborsInner(PackedBlockPos.west(myPackedPosition), height(HorizontalFace.WEST), consumer);
		produceNeighborsInner(PackedBlockPos.north(myPackedPosition), height(HorizontalFace.NORTH), consumer);
		produceNeighborsInner(PackedBlockPos.south(myPackedPosition), height(HorizontalFace.SOUTH), consumer);

		produceNeighborsInner(PackedBlockPos.northEast(myPackedPosition), height(HorizontalEdge.NORTH_EAST), consumer);
		produceNeighborsInner(PackedBlockPos.northWest(myPackedPosition), height(HorizontalEdge.NORTH_WEST), consumer);
		produceNeighborsInner(PackedBlockPos.southEast(myPackedPosition), height(HorizontalEdge.SOUTH_EAST), consumer);
		produceNeighborsInner(PackedBlockPos.southWest(myPackedPosition), height(HorizontalEdge.SOUTH_WEST), consumer);
	}

	private void produceNeighborsInner(long basePosition, int height, INeighborConsumer consumer) {
		if (height == NO_BLOCK)
			return;

		// -1 because full block is a single block, don't want to count it as block
		// above
		final int to = (height - NO_BLOCK - 1) / BLOCK_LEVELS_INT;

		for (int i = 0; i <= to; i++) {
			consumer.accept(PackedBlockPos.up(basePosition, i), i == to);
		}
	}
}
