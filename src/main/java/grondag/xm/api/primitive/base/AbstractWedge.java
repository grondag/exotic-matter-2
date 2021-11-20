/*
 * Copyright © Original Authors
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
 *
 * Additional copyright and licensing notices may apply for content that was
 * included from other projects. For more information, see ATTRIBUTION.md.
 */

package grondag.xm.api.primitive.base;

import static grondag.xm.api.modelstate.ModelStateFlags.NONE;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Function;

import org.jetbrains.annotations.ApiStatus.Experimental;

import net.minecraft.resources.ResourceLocation;

import grondag.fermion.orientation.api.CubeRotation;
import grondag.fermion.orientation.api.OrientationType;
import grondag.xm.api.mesh.ReadOnlyMesh;
import grondag.xm.api.mesh.polygon.PolyTransform;
import grondag.xm.api.mesh.polygon.Polygon;
import grondag.xm.api.modelstate.primitive.MutablePrimitiveState;
import grondag.xm.api.modelstate.primitive.PrimitiveState;
import grondag.xm.api.primitive.surface.XmSurfaceList;
import grondag.xm.modelstate.SimpleModelStateImpl;

@Experimental
public abstract class AbstractWedge extends AbstractSimplePrimitive {
	protected static final int KEY_COUNT = CubeRotation.COUNT * 3;

	protected static int computeKey(int edgeIndex, boolean isCorner, boolean isInside) {
		return edgeIndex * 3 + (isCorner ? (isInside ? 1 : 2) : 0);
	}

	protected final ReadOnlyMesh[] CACHE = new ReadOnlyMesh[KEY_COUNT];

	public AbstractWedge(ResourceLocation id, Function<PrimitiveState, XmSurfaceList> surfaceFunc) {
		super(id, NONE, SimpleModelStateImpl.FACTORY, surfaceFunc);
	}

	// mainly for run-time testing
	@Override
	public void invalidateCache() {
		Arrays.fill(CACHE, null);
	}

	@Override
	public void emitQuads(PrimitiveState modelState, Consumer<Polygon> target) {
		final int edgeIndex = modelState.orientationIndex();
		final boolean isCorner = isCorner(modelState);
		final boolean isInside = isInsideCorner(modelState);
		final int key = computeKey(edgeIndex, isCorner, isInside);

		ReadOnlyMesh mesh = CACHE[key];

		if (mesh == null) {
			mesh = buildMesh(PolyTransform.forEdgeRotation(edgeIndex), isCorner, isInside);
			CACHE[key] = mesh;
		}

		final Polygon reader = mesh.threadSafeReader();

		if (reader.origin()) {
			do {
				target.accept(reader);
			} while (reader.next());
		}

		reader.release();
	}

	protected abstract ReadOnlyMesh buildMesh(PolyTransform transform, boolean isCorner, boolean isInside);

	@Override
	public OrientationType orientationType(PrimitiveState modelState) {
		return OrientationType.ROTATION;
	}

	@Override
	public MutablePrimitiveState geometricState(PrimitiveState fromState) {
		final MutablePrimitiveState result = newState();

		if (fromState.primitive().orientationType(fromState) == OrientationType.ROTATION) {
			result.orientationIndex(fromState.orientationIndex());
			result.primitiveBits(fromState.primitiveBits());
		}

		return result;
	}

	@Override
	@Deprecated
	public boolean doesShapeMatch(PrimitiveState from, PrimitiveState to) {
		return from.primitive() == to.primitive()
			&& from.orientationIndex() == to.orientationIndex()
			&& from.primitiveBits() == to.primitiveBits();
	}

	private static final int CORNER_FLAG = 1;
	private static final int INSIDE_FLAG = 2;

	public static boolean isCorner(PrimitiveState modelState) {
		return (modelState.primitiveBits() & CORNER_FLAG) == CORNER_FLAG;
	}

	public static void setCorner(boolean isCorner, MutablePrimitiveState modelState) {
		final int oldBits = modelState.primitiveBits();
		modelState.primitiveBits(isCorner ? oldBits | CORNER_FLAG : oldBits & ~CORNER_FLAG);
	}

	/**
	 * Only applies when {@link #isCorner(PrimitiveState)} == (@code true}.
	 * @param modelState  State of this primitive.
	 * @return {@code true} when inside corner, {@code false} when outside corner.
	 */
	public static boolean isInsideCorner(PrimitiveState modelState) {
		return (modelState.primitiveBits() & INSIDE_FLAG) == INSIDE_FLAG;
	}

	/**
	 * See {@link #isInsideCorner(PrimitiveState)}.
	 * @param isCorner
	 * @param modelState
	 */
	public static void setInsideCorner(boolean isCorner, MutablePrimitiveState modelState) {
		final int oldBits = modelState.primitiveBits();
		modelState.primitiveBits(isCorner ? oldBits | INSIDE_FLAG : oldBits & ~INSIDE_FLAG);
	}
}
