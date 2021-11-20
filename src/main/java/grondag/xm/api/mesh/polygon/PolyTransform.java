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

package grondag.xm.api.mesh.polygon;

import java.util.function.Consumer;

import org.jetbrains.annotations.ApiStatus.Experimental;

import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;

import grondag.fermion.orientation.api.CubeRotation;
import grondag.xm.api.modelstate.base.BaseModelState;
import grondag.xm.mesh.helper.PolyTransformImpl;

@Experimental
@FunctionalInterface
public interface PolyTransform extends Consumer<MutablePolygon> {
	@Override
	void accept(MutablePolygon poly);

	/**
	 * Find appropriate transformation assuming base model is oriented with as follows:
	 * Axis = Y with positive orientation if orientation applies.
	 *
	 * <p>For the default rotation, generally, {@code DOWN} is considered the "bottom"
	 * and {@code SOUTH} is the "back" when facing the "front" of the primitive.
	 *
	 * <p>For primitives oriented to a corner, the default corner is "bottom, right, back"
	 * in the frame just described, or {@code DOWN}, {@code SOUTH}, {@code EAST} in terms
	 * of specific faces.
	 */
	@SuppressWarnings("rawtypes")
	static PolyTransform get(BaseModelState modelState) {
		return PolyTransformImpl.get(modelState);
	}

	static PolyTransform forEdgeRotation(int ordinal) {
		return PolyTransformImpl.forEdgeRotation(ordinal);
	}

	static PolyTransform get(CubeRotation corner) {
		return PolyTransformImpl.get(corner);
	}

	static PolyTransform get(Axis axis) {
		return PolyTransformImpl.get(axis);
	}

	static PolyTransform get(Direction face) {
		return PolyTransformImpl.get(face);
	}
}
