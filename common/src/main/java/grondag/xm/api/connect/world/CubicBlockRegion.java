/*
 * This file is part of Exotic Matter and is licensed to the project under
 * terms that are compatible with the GNU Lesser General Public License.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership and licensing.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package grondag.xm.api.connect.world;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.ImmutableSet;

import net.minecraft.core.BlockPos;

public class CubicBlockRegion extends IntegerBox implements BlockRegion {
	private final boolean isHollow;

	private static final Set<BlockPos> EMPTY = ImmutableSet.of();

	private Set<BlockPos> exclusions = EMPTY;

	/**
	 * Created region includes from from and to positions.
	 */
	public CubicBlockRegion(BlockPos fromPos, BlockPos toPos, boolean isHollow) {
		super(fromPos, toPos);
		this.isHollow = isHollow;
	}

	public final boolean isHollow() {
		return isHollow;
	}

	public final Set<BlockPos> exclusions() {
		return Collections.unmodifiableSet(exclusions);
	}

	public void exclude(BlockPos pos) {
		if (exclusions == EMPTY) {
			exclusions = new HashSet<>();
		}

		exclusions.add(pos);
	}

	public void exclude(Collection<BlockPos> positions) {
		if (exclusions == EMPTY) {
			exclusions = new HashSet<>();
		}

		exclusions.addAll(positions);
	}

	public boolean isExcluded(BlockPos pos) {
		return !exclusions.contains(pos);
	}

	public void clearExclusions() {
		exclusions = EMPTY;
	}

	/**
	 * All positions contained in the region, including interior positions if it is
	 * hollow.
	 */
	public Iterable<BlockPos> allPositions() {
		return BlockPos.betweenClosed(minX, minY, minZ, maxX - 1, maxY - 1, maxZ - 1);
	}

	/**
	 * All positions on the surface of the region. Will be same as
	 * {@link #allPositions()} if region is not at least 3x3x3
	 */
	@Override
	public Iterable<BlockPos> surfacePositions() {
		return getAllOnBoxSurfaceMutable(minX, minY, minZ, maxX - 1, maxY - 1, maxZ - 1);
	}

	/**
	 * Positions that belong the region, excluding interior positions if hollow, but
	 * not excluding any excluded positions.
	 */
	public Iterable<BlockPos> positions() {
		return isHollow ? surfacePositions() : allPositions();
	}

	/**
	 * All positions on the surface of the region. Will be same as
	 * {@link #allPositions()} if region is not at least 3x3x3
	 */
	@Override
	public Iterable<BlockPos> adjacentPositions() {
		return getAllOnBoxSurfaceMutable(minX - 1, minY - 1, minZ - 1, maxX, maxY, maxZ);
	}

	/**
	 * All positions included in the region. Excludes interior positions if hollow,
	 * and excludes any excluded positions.
	 */
	public Iterable<BlockPos> includedPositions() {
		return new Iterable<>() {
			@Override
			public Iterator<BlockPos> iterator() {
				return new AbstractIterator<>() {
					Iterator<BlockPos> wrapped = positions().iterator();

					@Override
					protected BlockPos computeNext() {
						while (wrapped.hasNext()) {
							final BlockPos result = wrapped.next();

							if (result != null && !exclusions.contains(result)) {
								return result;
							}
						}

						return endOfData();
					}
				};
			}
		};
	}

	/**
	 * convenience method - returns set of all block positions in AABB defined by
	 * inputs, inclusive.
	 */
	public static Set<BlockPos> positionsInRegion(BlockPos from, BlockPos to) {
		final CubicBlockRegion temp = new CubicBlockRegion(from, to, false);
		final ImmutableSet.Builder<BlockPos> builder = ImmutableSet.builder();

		for (final BlockPos pos : temp.allPositions()) {
			builder.add(pos.immutable());
		}

		return builder.build();
	}

	/**
	 * Like the BlockPos method, but only returns Block positions on the surface of
	 * the AABB.
	 */
	public static Iterable<BlockPos> getAllOnBoxSurfaceMutable(final int x1, final int y1, final int z1, final int x2, final int y2, final int z2) {
		// has to be at least 3x3x3 or logic will get stuck and is also inefficient
		if (x2 - x1 < 2 || y2 - y1 < 2 || z2 - z1 < 2) {
			return BlockPos.betweenClosed(x1, y1, z1, x2, y2, z2);
		}

		return new Iterable<>() {
			@Override
			public Iterator<BlockPos> iterator() {
				return new AbstractIterator<>() {
					private boolean atStart = true;
					private int x = x1, y = y1, z = z1;
					private final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x1, y1, z1);

					@Override
					protected BlockPos.MutableBlockPos computeNext() {
						if (atStart) {
							// at beginning
							atStart = false;
							return pos;
						} else if (x == x2 && y == y2 && z == z2) {
							// at end
							return (BlockPos.MutableBlockPos) endOfData();
						} else {
							// if at either end of Z, normal behavior
							if (z == z1 || z == z2) {
								if (x < x2) {
									++x;
								} else if (y < y2) {
									x = x1;
									++y;
								} else if (z < z2) {
									x = x1;
									y = y1;
									++z;
								}
							} else {
								// in middle section, only do exterior points for x and y
								if (y == y1) {
									// on ends of Y, iterate X
									if (x < x2) {
										++x;
									} else {
										x = x1;
										++y;
									}
								} else if (y == y2) {
									// on ends of Y, iterate X
									if (x < x2) {
										++x;
									} else {
										x = x1;
										y = y1;
										++z;
									}
								} else {
									// between Y ends, only x values are minX and maxX
									if (x == x1) {
										x = x2;
									} else {
										x = x1;
										++y;
									}
								}
							}

							pos.set(x, y, z);
							return pos;
						}
					}
				};
			}
		};
	}
}
