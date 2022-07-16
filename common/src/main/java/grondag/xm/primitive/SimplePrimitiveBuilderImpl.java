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

package grondag.xm.primitive;

import static grondag.xm.api.connect.state.SimpleJoinState.AXIS_JOIN_BIT_COUNT;
import static grondag.xm.api.modelstate.ModelStateFlags.CORNER_JOIN;
import static grondag.xm.api.modelstate.ModelStateFlags.NONE;
import static grondag.xm.api.modelstate.ModelStateFlags.SIMPLE_JOIN;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Function;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.resources.ResourceLocation;

import grondag.xm.Xm;
import grondag.xm.api.mesh.XmMesh;
import grondag.xm.api.mesh.polygon.Polygon;
import grondag.xm.api.modelstate.primitive.MutablePrimitiveState;
import grondag.xm.api.modelstate.primitive.PrimitiveState;
import grondag.xm.api.paint.XmPaint;
import grondag.xm.api.primitive.SimplePrimitive;
import grondag.xm.api.primitive.SimplePrimitive.Builder;
import grondag.xm.api.primitive.base.AbstractSimplePrimitive;
import grondag.xm.api.primitive.surface.XmSurfaceList;
import grondag.xm.connect.SimpleJoinStateImpl;
import grondag.xm.modelstate.SimpleModelStateImpl;
import grondag.xm.orientation.api.OrientationType;

@Internal
public class SimplePrimitiveBuilderImpl {
	protected static class BuilderImpl implements Builder {
		private OrientationType orientationType = OrientationType.NONE;
		private XmSurfaceList list = XmSurfaceList.ALL;
		private Function<PrimitiveState, XmMesh> polyFactory;
		private int bitCount = 0;
		private boolean axisJoin;
		private boolean simpleJoin;
		private boolean cornerJoin;
		private boolean alternateJoinAffectsGeometry;

		@Override
		public SimplePrimitive build(ResourceLocation id) {
			return new Primitive(id, this);
		}

		@Override
		public Builder surfaceList(XmSurfaceList list) {
			this.list = list == null ? XmSurfaceList.ALL : list;
			return this;
		}

		@Override
		public Builder orientationType(OrientationType orientationType) {
			this.orientationType = orientationType == null ? OrientationType.NONE : orientationType;
			return this;
		}

		@Override
		public Builder polyFactory(Function<PrimitiveState, XmMesh> polyFactory) {
			this.polyFactory = polyFactory == null ? t -> XmMesh.EMPTY : polyFactory;
			return this;
		}

		@Override
		public Builder primitiveBitCount(int bitCount) {
			this.bitCount = bitCount;
			return this;
		}

		@Override
		public Builder axisJoin(boolean needsJoin) {
			axisJoin = needsJoin;
			return this;
		}

		@Override
		public Builder simpleJoin(boolean needsJoin) {
			simpleJoin = needsJoin;
			return this;
		}

		@Override
		public Builder cornerJoin(boolean needsJoin) {
			cornerJoin = needsJoin;
			return this;
		}

		@Override
		public Builder alternateJoinAffectsGeometry(boolean affectsGeometry) {
			alternateJoinAffectsGeometry = affectsGeometry;
			return this;
		}
	}

	protected static class Primitive extends AbstractSimplePrimitive {
		private final XmMesh[] cachedQuads;

		private final OrientationType orientationType;

		private final Function<PrimitiveState, XmMesh> polyFactory;

		private boolean notifyException = true;

		private final int bitShift;

		/** True when needs only per-axis connections. */
		private final boolean axisJoin;
		private final boolean simpleJoin;
		private final boolean cornerJoin;
		private final boolean alternateJoinAffectsGeometry;

		static Function<PrimitiveState, XmSurfaceList> listWrapper(XmSurfaceList list) {
			return s -> list;
		}

		public Primitive(ResourceLocation id, BuilderImpl builder) {
			super(id, (builder.cornerJoin ? CORNER_JOIN : NONE) | (builder.simpleJoin || builder.axisJoin ? SIMPLE_JOIN : NONE), SimpleModelStateImpl.FACTORY, listWrapper(builder.list));
			axisJoin = builder.axisJoin & !builder.simpleJoin & !builder.cornerJoin;
			simpleJoin = builder.simpleJoin;
			cornerJoin = builder.cornerJoin;
			orientationType = builder.orientationType;
			polyFactory = builder.polyFactory;
			bitShift = builder.bitCount + 1; // + 1 for lamp
			alternateJoinAffectsGeometry = builder.alternateJoinAffectsGeometry;
			int count = orientationType.enumClass.getEnumConstants().length << bitShift;

			if (simpleJoin) {
				count <<= 6;
			} else if (axisJoin) {
				count <<= AXIS_JOIN_BIT_COUNT;
			}

			cachedQuads = cornerJoin || alternateJoinAffectsGeometry ? null : new XmMesh[count];
			invalidateCache();
		}

		@Override
		public void invalidateCache() {
			notifyException = true;

			if (!(cornerJoin || alternateJoinAffectsGeometry)) {
				Arrays.fill(cachedQuads, null);
			}
		}

		@Override
		public OrientationType orientationType(PrimitiveState modelState) {
			return orientationType;
		}

		@Override
		public void emitQuads(PrimitiveState modelState, Consumer<Polygon> target) {
			try {
				XmMesh mesh;

				if (cornerJoin || alternateJoinAffectsGeometry) {
					mesh = polyFactory.apply(modelState);
				} else {
					int index = (modelState.orientationIndex() << bitShift) | (modelState.primitiveBits() << 1);

					if (modelState.primitive().lampSurface(modelState) != null) {
						index |= 1;
					}

					if (simpleJoin) {
						index = (index << 6) | modelState.simpleJoin().ordinal();
					} else if (axisJoin) {
						index = (index << AXIS_JOIN_BIT_COUNT) | SimpleJoinStateImpl.toAxisJoinIndex(modelState.simpleJoin());
					}

					mesh = cachedQuads[index];

					if (mesh == null) {
						mesh = polyFactory.apply(modelState);
						cachedQuads[index] = mesh;
					}
				}

				final Polygon reader = mesh.threadSafeReader();

				if (reader.origin()) {
					do {
						target.accept(reader);
					} while (reader.next());
				}

				reader.release();
			} catch (final Exception e) {
				if (notifyException) {
					notifyException = false;
					Xm.LOG.error("Unexpected exception while rendering primitive '" + translationKey() + "'.  Subsequent errors will be supressed.", e);
				}
			}
		}

		@Override
		public MutablePrimitiveState geometricState(PrimitiveState fromState) {
			final MutablePrimitiveState result = newState()
					.orientationIndex(fromState.orientationIndex())
					.paintAll(XmPaint.DEFAULT_PAINT)
					.primitiveBits(fromState.primitiveBits());

			if (cornerJoin) {
				result.cornerJoin(fromState.cornerJoin());
			} else if (simpleJoin) {
				result.simpleJoin(fromState.simpleJoin());
			}

			if (alternateJoinAffectsGeometry) {
				result.alternateJoinBits(fromState.alternateJoinBits());
			}

			return result;
		}

		@Override
		@Deprecated
		public boolean doesShapeMatch(PrimitiveState from, PrimitiveState to) {
			if (from.primitive() != to.primitive()
					|| from.orientationIndex() != to.orientationIndex()
					|| from.primitiveBits() != to.primitiveBits()
					|| (alternateJoinAffectsGeometry && from.alternateJoinBits() != to.alternateJoinBits())) {
				return false;
			}

			if (cornerJoin) {
				return from.cornerJoin() == to.cornerJoin();
			} else if (simpleJoin || axisJoin) {
				return from.simpleJoin() == to.simpleJoin();
			} else {
				return true;
			}
		}
	}

	public static Builder builder() {
		return new BuilderImpl();
	}
}
