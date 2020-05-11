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
package grondag.xm.mesh;

import static org.apiguardian.api.API.Status.INTERNAL;

import org.apiguardian.api.API;

import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

import grondag.fermion.color.ColorHelper;
import grondag.xm.api.mesh.polygon.MutablePolygon;
import grondag.xm.api.mesh.polygon.Polygon;
import grondag.xm.api.mesh.polygon.Vec3f;
import grondag.xm.api.paint.PaintBlendMode;
import grondag.xm.api.primitive.surface.XmSurface;
import grondag.xm.api.texture.TextureOrientation;
import grondag.xm.mesh.StreamBackedMutablePolygon.UVLocker;

@API(status = INTERNAL)
class StreamBackedMutablePolygon extends StreamBackedPolygon implements MutablePolygon {
	@Override
	public final MutablePolygon spriteVertex(int layerIndex, int vertexIndex, float u, float v, int color, int glow) {
		vertexIndex = vertexIndexer.applyAsInt(vertexIndex);
		color(vertexIndex, layerIndex, color);
		uv(vertexIndex, layerIndex, u, v);
		glow(vertexIndex, glow);
		return this;
	}

	@Override
	public final MutablePolygon maxU(int layerIndex, float maxU) {
		polyEncoder.setMaxU(stream, baseAddress, layerIndex, maxU);
		return this;
	}

	@Override
	public final MutablePolygon maxV(int layerIndex, float maxV) {
		polyEncoder.setMaxV(stream, baseAddress, layerIndex, maxV);
		return this;
	}

	@Override
	public final MutablePolygon minU(int layerIndex, float minU) {
		polyEncoder.setMinU(stream, baseAddress, layerIndex, minU);
		return this;
	}

	@Override
	public final MutablePolygon minV(int layerIndex, float minV) {
		polyEncoder.setMinV(stream, baseAddress, layerIndex, minV);
		return this;
	}

	@Override
	public MutablePolygon uvWrapDistance(float uvWrapDistance) {
		StaticEncoder.uvWrapDistance(stream, baseAddress, uvWrapDistance);
		return this;
	}

	@Override
	public final MutablePolygon textureSalt(int salt) {
		StaticEncoder.setTextureSalt(stream, baseAddress, salt);
		return this;
	}

	@Override
	public final MutablePolygon lockUV(int layerIndex, boolean lockUV) {
		StaticEncoder.setLockUV(stream, baseAddress, layerIndex, lockUV);
		return this;
	}

	@Override
	public final MutablePolygon assignLockedUVCoordinates(int layerIndex) {
		final UVLocker locker = UVLOCKERS[nominalFace().ordinal()];

		final int vertexCount = vertexCount();
		for (int i = 0; i < vertexCount; i++) {
			locker.apply(i, layerIndex, this);
		}

		return this;
	}

	@Override
	public final MutablePolygon sprite(int layerIndex, String textureName) {
		polyEncoder.setTextureName(stream, baseAddress, layerIndex, textureName);
		return this;
	}

	@Override
	public final MutablePolygon rotation(int layerIndex, TextureOrientation rotation) {
		StaticEncoder.setRotation(stream, baseAddress, layerIndex, rotation);
		return this;
	}

	@Override
	public final MutablePolygon contractUV(int layerIndex, boolean contractUVs) {
		StaticEncoder.setContractUVs(stream, baseAddress, layerIndex, contractUVs);
		return this;
	}

	@Override
	public final MutablePolygon blendMode(int layerIndex, PaintBlendMode layer) {
		StaticEncoder.setRenderLayer(stream, baseAddress, layerIndex, layer);
		return this;
	}

	/**
	 * Throws exception if not a mutable format.
	 */
	@Override
	public final MutablePolygon spriteDepth(int layerCount) {
		final int format = format();
		if (!MeshFormat.isMutable(format)) {
			throw new UnsupportedOperationException("Cannot change layer count on immutable polygon");
		}
		setFormat(MeshFormat.setLayerCount(format, layerCount));
		return this;
	}

	@Override
	public final MutablePolygon emissive(int layerIndex, boolean emissive) {
		StaticEncoder.setEmissive(stream, baseAddress, layerIndex, emissive);
		return this;
	}

	@Override
	public MutablePolygon disableAo(int layerIndex, boolean disable) {
		StaticEncoder.disableAo(stream, baseAddress, layerIndex, disable);
		return this;
	}

	@Override
	public MutablePolygon disableDiffuse(int layerIndex, boolean disable) {
		StaticEncoder.disableDiffuse(stream, baseAddress, layerIndex, disable);
		return this;
	}

	@Override
	public final MutablePolygon vertex(int vertexIndex, float x, float y, float z, float u, float v, int color, int glow) {
		vertexIndex = vertexIndexer.applyAsInt(vertexIndex);
		pos(vertexIndex, x, y, z);
		uv(vertexIndex, 0, u, v);
		color(vertexIndex, 0, color);
		glow(vertexIndex, glow);
		return this;
	}

	@Override
	public final MutablePolygon pos(int vertexIndex, float x, float y, float z) {
		vertexEncoder.setVertexPos(stream, vertexAddress, vertexIndexer.applyAsInt(vertexIndex), x, y, z);
		return this;
	}

	@Override
	public final MutablePolygon pos(int vertexIndex, Vec3f pos) {
		vertexEncoder.setVertexPos(stream, vertexAddress, vertexIndexer.applyAsInt(vertexIndex), pos.x(), pos.y(), pos.z());
		return this;
	}

	@Override
	public final MutablePolygon color(int vertexIndex, int layerIndex, int color) {
		if (vertexEncoder.hasColor()) {
			vertexEncoder.setVertexColor(stream, vertexAddress, layerIndex, vertexIndexer.applyAsInt(vertexIndex), color);
		} else {
			polyEncoder.setVertexColor(stream, baseAddress, layerIndex, color);
		}
		return this;
	}

	@Override
	public final MutablePolygon x(int vertexIndex, float x) {
		vertexEncoder.setVertexX(stream, vertexAddress, vertexIndexer.applyAsInt(vertexIndex), x);
		return this;
	}

	@Override
	public final MutablePolygon y(int vertexIndex, float y) {
		vertexEncoder.setVertexY(stream, vertexAddress, vertexIndexer.applyAsInt(vertexIndex), y);
		return this;
	}

	@Override
	public final MutablePolygon z(int vertexIndex, float z) {
		vertexEncoder.setVertexZ(stream, vertexAddress, vertexIndexer.applyAsInt(vertexIndex), z);
		return this;
	}

	@Override
	public final MutablePolygon u(int vertexIndex, int layerIndex, float u) {
		vertexEncoder.setVertexU(stream, vertexAddress, layerIndex, vertexIndexer.applyAsInt(vertexIndex), u);
		return this;
	}

	@Override
	public final MutablePolygon v(int vertexIndex, int layerIndex, float v) {
		vertexEncoder.setVertexV(stream, vertexAddress, layerIndex, vertexIndexer.applyAsInt(vertexIndex), v);
		return this;
	}

	@Override
	public final MutablePolygon uv(int vertexIndex, int layerIndex, float u, float v) {
		vertexEncoder.setVertexUV(stream, vertexAddress, layerIndex, vertexIndexer.applyAsInt(vertexIndex), u, v);
		return this;
	}

	@Override
	public final MutablePolygon glow(int vertexIndex, int glow) {
		glowEncoder.setGlow(stream, glowAddress, vertexIndexer.applyAsInt(vertexIndex), glow);
		return this;
	}

	@Override
	public final MutablePolygon normal(int vertexIndex, Vec3f normal) {
		if (vertexEncoder.hasNormals()) {
			if (normal == null) {
				vertexEncoder.setVertexNormal(stream, vertexAddress, vertexIndexer.applyAsInt(vertexIndex), Float.NaN, Float.NaN, Float.NaN);
			} else {
				vertexEncoder.setVertexNormal(stream, vertexAddress, vertexIndexer.applyAsInt(vertexIndex), normal.x(), normal.y(), normal.z());
			}
		}
		return this;
	}

	@Override
	public final MutablePolygon normal(int vertexIndex, float x, float y, float z) {
		if (vertexEncoder.hasNormals()) {
			vertexEncoder.setVertexNormal(stream, vertexAddress, vertexIndexer.applyAsInt(vertexIndex), x, y, z);
		}
		return this;
	}

	// TODO replace with render material
	//    @Override
	//    public final IMutablePolygon setPipelineIndex(int pipelineIndex) {
	//        StaticEncoder.setPipelineIndex(stream, baseAddress, pipelineIndex);
	//        return this;
	//    }

	@Override
	public final MutablePolygon clearFaceNormal() {
		final int normalFormat = MeshFormat.getFaceNormalFormat(format());

		assert normalFormat == MeshFormat.FACE_NORMAL_FORMAT_COMPUTED : "Face normal clear should only happen for full-precision normals";

		polyEncoder.clearFaceNormal(stream, baseAddress);
		return this;
	}

	@Override
	public final MutablePolygon nominalFace(Direction face) {
		setFormat(MeshFormat.setNominalFace(format(), face));
		return this;
	}

	@Override
	public final MutablePolygon cullFace(Direction face) {
		setFormat(MeshFormat.setCullFace(format(), face));
		return this;
	}

	@Override
	public final MutablePolygon surface(XmSurface surface) {
		StaticEncoder.surface(stream, baseAddress, surface);
		return this;
	}

	@Override
	public final MutablePolygon copyVertexFrom(int targetIndex, Polygon source, int sourceIndex) {
		if (source.hasNormal(sourceIndex)) {
			assert vertexEncoder.hasNormals();
			normal(targetIndex, source.normalX(sourceIndex), source.normalY(sourceIndex), source.normalZ(sourceIndex));
		} else if (vertexEncoder.hasNormals()) {
			this.normal(targetIndex, (Vec3f) null);
		}

		pos(targetIndex, source.x(sourceIndex), source.y(sourceIndex), source.z(sourceIndex));

		if (glowEncoder.glowFormat() == MeshFormat.VERTEX_GLOW_PER_VERTEX) {
			glow(targetIndex, source.glow(sourceIndex));
		} else if (targetIndex == 0 && glowEncoder.glowFormat() == MeshFormat.VERTEX_GLOW_SAME) {
			glow(0, source.glow(sourceIndex));
		}

		final int layerCount = source.spriteDepth();
		assert layerCount <= spriteDepth();

		// do for all vertices even if all the same - slightly wasteful but fewer logic
		// paths
		color(targetIndex, 0, source.color(sourceIndex, 0));
		if (layerCount > 1) {
			color(targetIndex, 1, source.color(sourceIndex, 1));

			if (layerCount == 3) {
				color(targetIndex, 2, source.color(sourceIndex, 2));
			}
		}

		uv(targetIndex, 0, source.u(sourceIndex, 0), source.v(sourceIndex, 0));
		if (vertexEncoder.multiUV() && layerCount > 1) {
			uv(targetIndex, 1, source.u(sourceIndex, 1), source.v(sourceIndex, 1));

			if (layerCount == 3) {
				uv(targetIndex, 2, source.u(sourceIndex, 2), source.v(sourceIndex, 2));
			}
		}

		return this;
	}

	@Override
	public MutablePolygon copyInterpolatedVertexFrom(int targetIndex, Polygon from, int fromIndex, Polygon to, int toIndex, float toWeight) {
		final int layerCount = from.spriteDepth();
		assert layerCount == to.spriteDepth();
		if(this.spriteDepth() < layerCount) {
			this.spriteDepth(layerCount);
		}

		this.pos(targetIndex,
				MathHelper.lerp(toWeight, from.x(fromIndex), to.x(toIndex)),
				MathHelper.lerp(toWeight, from.y(fromIndex), to.y(toIndex)),
				MathHelper.lerp(toWeight, from.z(fromIndex), to.z(toIndex)));

		final int fromGlow = from.glow(fromIndex);
		this.glow(targetIndex, (int)(fromGlow + (to.glow(toIndex) - fromGlow) * toWeight));

		if (from.hasNormal(fromIndex) && to.hasNormal(toIndex)) {
			final float normX = MathHelper.lerp(toWeight, from.normalX(fromIndex), to.normalX(toIndex));
			final float normY = MathHelper.lerp(toWeight, from.normalY(fromIndex), to.normalY(toIndex));
			final float normZ = MathHelper.lerp(toWeight, from.normalZ(fromIndex), to.normalZ(toIndex));
			final float normScale = 1f / (float) Math.sqrt(normX * normX + normY * normY + normZ * normZ);
			this.normal(targetIndex, normX * normScale, normY * normScale, normZ * normScale);
		} else {
			this.normal(targetIndex, null);
		}

		this.color(targetIndex, 0, ColorHelper.interpolate(from.color(fromIndex, 0), to.color(toIndex, 0), toWeight));
		uv(targetIndex, 0,
				MathHelper.lerp(toWeight, from.u(fromIndex, 0), to.u(toIndex, 0)),
				MathHelper.lerp(toWeight, from.v(fromIndex, 0), to.v(toIndex, 0)));

		if (layerCount > 1) {
			this.color(targetIndex, 1, ColorHelper.interpolate(from.color(fromIndex, 1), to.color(toIndex, 1), toWeight));
			uv(targetIndex, 1,
					MathHelper.lerp(toWeight, from.u(fromIndex, 1), to.u(toIndex, 1)),
					MathHelper.lerp(toWeight, from.v(fromIndex, 1), to.v(toIndex, 1)));

			if (layerCount == 3) {
				this.color(targetIndex, 2, ColorHelper.interpolate(from.color(fromIndex, 2), to.color(toIndex, 2), toWeight));
				uv(targetIndex, 2,
						MathHelper.lerp(toWeight, from.u(fromIndex, 2), to.u(toIndex, 2)),
						MathHelper.lerp(toWeight, from.v(fromIndex, 2), to.v(toIndex, 2)));
			}
		}

		return this;
	}

	@Override
	public final MutablePolygon copyFrom(Polygon polyIn, boolean includeVertices) {
		// PERF: make this faster for other stream-based polys
		nominalFace(polyIn.nominalFace());
		cullFace(polyIn.cullFace());

		final int faceNormalFormat = MeshFormat.getFaceNormalFormat(format());
		if (faceNormalFormat == MeshFormat.FACE_NORMAL_FORMAT_COMPUTED) {
			clearFaceNormal();
		} else if (faceNormalFormat == MeshFormat.FACE_NORMAL_FORMAT_QUANTIZED) {
			polyEncoder.setFaceNormal(stream, faceNormalFormat, polyIn.faceNormal());
		}

		final int layerCount = polyIn.spriteDepth();
		if (spriteDepth() < layerCount) {
			spriteDepth(layerCount);
		}

		textureSalt(polyIn.textureSalt());
		surface(polyIn.surface());

		uvWrapDistance(polyIn.uvWrapDistance());
		tag(polyIn.tag());

		for (int l = 0; l < layerCount; l++) {
			maxU(l, polyIn.maxU(l));
			maxV(l, polyIn.maxV(l));
			minU(l, polyIn.minU(l));
			minV(l, polyIn.minV(l));
			emissive(l, polyIn.emissive(l));
			blendMode(l, polyIn.blendMode(l));
			lockUV(l, polyIn.lockUV(l));
			contractUV(l, polyIn.shouldContractUVs(l));
			rotation(l, polyIn.rotation(l));
			sprite(l, polyIn.spriteName(l));
		}

		if (includeVertices) {
			final int vertexCount = polyIn.vertexCount();
			if (vertexCount() == vertexCount) {
				for (int i = 0; i < vertexCount; i++) {
					copyVertexFrom(i, polyIn, i);
				}
			} else {
				throw new UnsupportedOperationException("Polygon vertex counts must match when copying vertex data.");
			}
		}
		return this;
	}

	/**
	 * Specialized version for CSG operations. Never includes vertex info and does
	 * include marks and tags.
	 */
	public void copyFromCSG(Polygon polyIn) {
		copyFrom(polyIn, false);
		tag(polyIn.tag());
	}

	@Override
	public MutablePolygon flip() {
		final int vCount = this.vertexCount();
		final int midPoint = (vCount + 1) / 2;
		nominalFace(nominalFace().getOpposite());

		for(int low = 0; low < midPoint; low++) {
			final int high = vCount - low - 1;

			// flip low vertex normal, or mid-point on odd-numbered polys
			if(hasNormal(low)) {
				normal(low, -normalX(low), -normalY(low), -normalZ(low));
			}

			if(low != high) {
				// flip high vertex normal
				if(hasNormal(high)) {
					normal(high, -normalX(high), -normalY(high), -normalZ(high));
				}

				// swap low with high
				final float x = x(low);
				final float y = y(low);
				final float z = z(low);
				final boolean hasNormal = hasNormal(low);
				float normX = 0, normY = 0, normZ = 0;
				if(hasNormal) {
					normX = normalX(low);
					normY = normalY(low);
					normZ = normalZ(low);
				}
				final boolean doGlow = glowEncoder.glowFormat() == MeshFormat.VERTEX_GLOW_PER_VERTEX;
				int glow = 0;
				if(doGlow) {
					glow = glow(low);
				}
				final int color0 = color(low, 0);
				final float u0 = u(low, 0);
				final float v0 = u(low, 0);

				int color1 = 0, color2 = 0;
				float u1 = 0, u2 = 0, v1 = 0, v2 = 0;
				final int depth = spriteDepth();
				if(depth > 1) {
					color1 = color(low, 1);
					u1 = u(low, 1);
					v1 = u(low, 1);
					if(depth == 3) {
						color2 = color(low, 2);
						u2 = u(low, 2);
						v2 = u(low, 2);
					}
				}
				copyVertexFrom(low, this, high);

				pos(high, x, y, z);
				if(hasNormal) {
					normal(high, normX, normY, normZ);
				} else {
					normal(high, null);
				}
				if(doGlow) {
					glow(high, glow);
				}
				uv(high, 0, u0, v0);
				color(high, 0, color0);

				if(depth > 1) {
					uv(high, 1, u1, v1);
					color(high, 1, color1);
					if(depth == 3) {
						uv(high, 2, u2, v2);
						color(high, 2, color2);
					}
				}
			}
		}

		clearFaceNormal();

		return this;
	}

	public void loadStandardDefaults() {
		maxU(0, 1f);
		maxU(1, 1f);
		maxU(2, 1f);

		maxV(0, 1f);
		maxV(1, 1f);
		maxV(2, 1f);
		cullFace(null);
		clearFaceNormal();
	}

	@Override
	public MutablePolygon tag(int tag) {
		polyEncoder.setTag(stream, baseAddress, tag);
		return this;
	}

	@FunctionalInterface
	interface UVLocker {
		void apply(int vertexIndex, int layerIndex, MutablePolygon poly);
	}

	private static final UVLocker[] UVLOCKERS = new UVLocker[6];

	static {
		UVLOCKERS[Direction.EAST.ordinal()] = (v, l, p) -> p.uv(v, l, 1 - p.z(v), 1 - p.y(v));

		UVLOCKERS[Direction.WEST.ordinal()] = (v, l, p) -> p.uv(v, l, p.z(v), 1 - p.y(v));

		UVLOCKERS[Direction.NORTH.ordinal()] = (v, l, p) -> p.uv(v, l, 1 - p.x(v), 1 - p.y(v));

		UVLOCKERS[Direction.SOUTH.ordinal()] = (v, l, p) -> p.uv(v, l, p.x(v), 1 - p.y(v));

		UVLOCKERS[Direction.DOWN.ordinal()] = (v, l, p) -> p.uv(v, l, p.x(v), 1 - p.z(v));

		// our default semantic for UP is different than MC
		// "top" is north instead of south
		UVLOCKERS[Direction.UP.ordinal()] = (v, l, p) -> p.uv(v, l, p.x(v), p.z(v));
	}

	@Override
	public MutablePolygon append() {
		((WritableMeshImpl)mesh).append();
		return this;
	}

	@Override
	public MutablePolygon saveDefaults() {
		((WritableMeshImpl)mesh).saveDefaults();
		return this;
	}

	@Override
	public MutablePolygon clearDefaults() {
		((WritableMeshImpl)mesh).clearDefaults();
		return this;
	}

	@Override
	public MutablePolygon loadDefaults() {
		((WritableMeshImpl)mesh).loadDefaults();
		return this;
	}

	@Override
	public MutablePolygon vertexCount(int vertexCount) {
		setFormat(MeshFormat.setVertexCount(format(), vertexCount));
		return this;
	}

	@Override
	public boolean splitIfNeeded() {
		return ((WritableMeshImpl)mesh).splitIfNeeded(address()) != Polygon.NO_LINK_OR_TAG;
	}
}
