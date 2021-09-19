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
package grondag.xm.painter;

import java.util.function.Consumer;

import org.jetbrains.annotations.ApiStatus.Internal;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.fabricmc.fabric.api.renderer.v1.material.MaterialFinder;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import grondag.xm.Xm;
import grondag.xm.api.mesh.MutableMesh;
import grondag.xm.api.mesh.XmMeshes;
import grondag.xm.api.mesh.polygon.MutablePolygon;
import grondag.xm.api.mesh.polygon.Polygon;
import grondag.xm.api.modelstate.base.BaseModelState;
import grondag.xm.api.paint.XmPaint;
import grondag.xm.api.primitive.surface.XmSurface;
import grondag.xm.api.texture.TextureOrientation;
import grondag.xm.painter.AbstractQuadPainter.PaintMethod;
import grondag.xm.target.RenderTarget;
import grondag.xm.texture.TextureSetHelper;

@Environment(EnvType.CLIENT)
@SuppressWarnings("rawtypes")
@Internal
public class PaintManager implements Consumer<Polygon> {
	private static final ThreadLocal<PaintManager> POOL = ThreadLocal.withInitial(PaintManager::new);

	private static final Renderer RENDERER = RendererAccess.INSTANCE.getRenderer();

	private static final boolean FREX_ACTIVE = RenderTarget.instance().isFrex();

	private static final BlendMode[] BLEND_MODES = BlendMode.values();

	public static Mesh paint(BaseModelState meshState) {
		return POOL.get().handlePaint(meshState);
	}

	private final MeshBuilder builder = RENDERER.meshBuilder();
	private final MutableMesh work = XmMeshes.claimMutable();
	private final QuadEmitter emitter = builder.getEmitter();
	private BaseModelState modelState;
	private final MaterialFinder finder = RENDERER.materialFinder();

	private Mesh handlePaint(BaseModelState modelState) {
		this.modelState = modelState;
		modelState.emitPolygons(this);
		return builder.build();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void accept(Polygon poly) {
		final BaseModelState modelState = this.modelState;
		final QuadEmitter emitter = this.emitter;
		final MutableMesh mesh = work;
		final MutablePolygon editor = mesh.editor();

		mesh.clear();

		XmSurface surface = poly.surface();
		if(surface == null) {
			//TODO: remove
			Xm.LOG.info("Encountered null surface during paint, using default surface");
			surface = modelState.primitive().surfaces(modelState).get(0);
		}
		final XmPaint paint = modelState.paint(surface);

		mesh.appendCopy(poly);
		editor.origin();

		if(editor.vertexCount() > 4) {
			//TODO: remove
			Xm.LOG.info("Encountered higher-order polygon during paint. Bad primitive output.");
		}

		final int depth = paint.textureDepth();
		editor.spriteDepth(depth);

		// Copy generator UVs (quad and vertex)
		// from layer 0 to upper layers.
		if (depth > 1) {
			final float minU = editor.minU(0);
			final float maxU = editor.maxU(0);
			final float minV = editor.minV(0);
			final float maxV = editor.maxV(0);
			editor.minU(1, minU);
			editor.maxU(1, maxU);
			editor.minV(1, minV);
			editor.maxV(1, maxV);
			if (depth == 3) {
				editor.minU(2, minU);
				editor.maxU(2, maxU);
				editor.minV(2, minV);
				editor.maxV(2, maxV);
			}

			final int vertexCount = editor.vertexCount();
			for (int i = 0; i < vertexCount; i++) {
				final int c = editor.color(i, 0);
				final float u = editor.u(i, 0);
				final float v = editor.v(i, 0);
				editor.color(i, 1, c);
				editor.uv(i, 1, u, v);
				if (depth == 3) {
					editor.color(i, 2, c);
					editor.uv(i, 2, u, v);
				}
			}
		}

		for (int i = 0; i < depth; i++) {
			final int limit = mesh.writerAddress();
			editor.origin();

			do {
				final PaintMethod painter = PainterFactory.getPainter(modelState, surface, paint, i);
				if(painter != null) {
					painter.paintQuads(mesh, modelState, surface, paint, i);
				} else {
					//TODO: put back
					//assert false : "Missing paint method";
				}
			} while (editor.next() && editor.address() < limit);
		}

		editor.origin();

		do {
			// omit layers that weren't textured by any painter
			if (!editor.spriteName(0).isEmpty()) {
				final int layerCount = editor.spriteName(1).isEmpty() ? 1 : editor.spriteName(2).isEmpty() ? 2 : 3;
				editor.spriteDepth(layerCount);
				polyToMesh(editor, emitter);
			}
		} while (editor.next());
	}

	private void polyToMesh(MutablePolygon poly, QuadEmitter emitter) {
		if (FREX_ACTIVE) {
			polyToMeshFrex(poly, emitter);
		} else {
			polyToMeshIndigo(poly, emitter);
		}
	}

	private void polyToMeshFrex(MutablePolygon poly, QuadEmitter emitterIn) {
		final int depth = poly.spriteDepth();
		final grondag.frex.api.mesh.QuadEmitter emitter = (grondag.frex.api.mesh.QuadEmitter) emitterIn;
		final grondag.frex.api.material.MaterialFinder finder = (grondag.frex.api.material.MaterialFinder) this.finder;

		finder.clear()
		.blendMode(BLEND_MODES[poly.blendMode().ordinal()])
		.emissive(poly.emissive(0))
		.disableAo(poly.disableAo(0))
		.disableDiffuse(poly.disableDiffuse(0));

		bakeSprite(0, poly);
		emitter.material(finder.find());
		outputFrexQuad(poly, emitter, 0);

		if (depth > 1) {
			bakeSprite(1, poly);

			finder.clear()
			.blendMode(BlendMode.TRANSLUCENT)
			.emissive(poly.emissive(1))
			.disableAo(poly.disableAo(1))
			.disableDiffuse(poly.disableDiffuse(1));

			emitter.material(finder.find());
			outputFrexQuad(poly, emitter, 1);

			if (depth == 3) {
				bakeSprite(2, poly);

				finder.clear()
				.blendMode(BlendMode.TRANSLUCENT)
				.emissive(poly.emissive(2))
				.disableAo(poly.disableAo(2))
				.disableDiffuse(poly.disableDiffuse(2));

				emitter.material(finder.find());
				outputFrexQuad(poly, emitter, 2);
			}
		}
	}

	private static void outputFrexQuad(MutablePolygon poly, grondag.frex.api.mesh.QuadEmitter emitter, int spriteIndex) {
		emitter.cullFace(poly.cullFace());
		emitter.nominalFace(poly.nominalFace());
		emitter.tag(poly.tag());

		for (int v = 0; v < 4; v++) {
			emitter.pos(v, poly.x(v), poly.y(v), poly.z(v));

			final int g = poly.glow(v);

			if(g > 0) {
				emitter.lightmap(v, g);
			}

			if (poly.hasNormal(v)) {
				emitter.normal(v, poly.normalX(v), poly.normalY(v), poly.normalZ(v));
			}

			emitter.sprite(v, poly.u(v, spriteIndex), poly.v(v, spriteIndex));
			emitter.vertexColor(v, poly.color(v, spriteIndex));
		}

		emitter.emit();
	}

	private void polyToMeshIndigo(MutablePolygon poly, QuadEmitter emitter) {
		final int depth = poly.spriteDepth();
		final MaterialFinder finder = this.finder;

		finder.clear()
		.blendMode(0, BLEND_MODES[poly.blendMode().ordinal()])
		.emissive(0, poly.emissive(0))
		.disableAo(0, poly.disableAo(0))
		.disableDiffuse(0, poly.disableDiffuse(0));

		bakeSprite(0, poly);
		emitter.material(finder.find());
		outputIndigoQuad(poly, emitter, 0);

		if (depth > 1) {
			bakeSprite(1, poly);

			finder.clear()
			.blendMode(0, BlendMode.TRANSLUCENT)
			.emissive(0, poly.emissive(1))
			.disableAo(0, poly.disableAo(1))
			.disableDiffuse(0, poly.disableDiffuse(1));

			emitter.material(finder.find());
			outputIndigoQuad(poly, emitter, 1);

			if (depth == 3) {
				bakeSprite(2, poly);

				finder.clear()
				.blendMode(0, BlendMode.TRANSLUCENT)
				.emissive(0, poly.emissive(2))
				.disableAo(0, poly.disableAo(2))
				.disableDiffuse(0, poly.disableDiffuse(2));

				emitter.material(finder.find());
				outputIndigoQuad(poly, emitter, 2);
			}
		}
	}

	private static void outputIndigoQuad(MutablePolygon poly, QuadEmitter emitter, int spriteIndex) {
		emitter.cullFace(poly.cullFace());
		emitter.nominalFace(poly.nominalFace());
		emitter.tag(poly.tag());

		for (int v = 0; v < 4; v++) {
			emitter.pos(v, poly.x(v), poly.y(v), poly.z(v));

			final int g = poly.glow(v);

			if(g > 0) {
				emitter.lightmap(v, g);
			}

			if (poly.hasNormal(v)) {
				emitter.normal(v, poly.normalX(v), poly.normalY(v), poly.normalZ(v));
			}

			emitter.sprite(v, 0, poly.u(v, spriteIndex), poly.v(v, spriteIndex));
			emitter.spriteColor(v, 0, poly.color(v, spriteIndex));
		}

		emitter.emit();
	}

	// PERF: consider in-lining all the coordinates
	private void bakeSprite(int spriteIndex, MutablePolygon poly) {
		applyTextureRotation(spriteIndex, poly);

		// these need to  be captured after texture rotation is applied due to flipping
		final float minU = poly.minU(spriteIndex);
		final float minV = poly.minV(spriteIndex);
		final float spanU = poly.maxU(spriteIndex) - minU;
		final float spanV = poly.maxV(spriteIndex) - minV;

		// scale UV coordinates to size of texture sub-region
		final int vCount = poly.vertexCount();
		for (int v = 0; v < vCount; v++) {
			poly.uv(v, spriteIndex, minU + spanU * poly.u(v, spriteIndex), minV + spanV * poly.v(v, spriteIndex));
		}

		final TextureAtlasSprite sprite = TextureSetHelper.blockAtas().getSprite(new ResourceLocation(poly.spriteName(spriteIndex)));

		if (poly.shouldContractUVs(spriteIndex)) {
			contractUVs(spriteIndex, sprite, poly);
		}

		final float spriteMinU = sprite.getU0();
		final float spriteSpanU = sprite.getU1() - spriteMinU;
		final float spriteMinV = sprite.getV0();
		final float spriteSpanV = sprite.getV1() - spriteMinV;

		// doing interpolation here vs using sprite methods to avoid wasteful multiply
		// and divide by 16
		for (int v = 0; v < vCount; v++) {
			poly.uv(v, spriteIndex, spriteMinU + spriteSpanU * poly.u(v, spriteIndex), spriteMinV + spriteSpanV * poly.v(v, spriteIndex));
		}
	}

	/**
	 * Prevents pinholes or similar artifacts along texture seams by nudging all
	 * texture coordinates slightly towards the vertex centroid of the UV
	 * coordinates.
	 */
	private void contractUVs(int spriteIndex, TextureAtlasSprite sprite, MutablePolygon poly) {
		final float uPixels = sprite.getWidth() / (sprite.getU1() - sprite.getU0());
		final float vPixels = sprite.getHeight() / (sprite.getV1() - sprite.getV0());
		final float nudge = 4.0f / Math.max(vPixels, uPixels);

		final float u0 = poly.u(0, spriteIndex);
		final float u1 = poly.u(1, spriteIndex);
		final float u2 = poly.u(2, spriteIndex);
		final float u3 = poly.u(3, spriteIndex);

		final float v0 = poly.v(0, spriteIndex);
		final float v1 = poly.v(1, spriteIndex);
		final float v2 = poly.v(2, spriteIndex);
		final float v3 = poly.v(3, spriteIndex);

		final float uCenter = (u0 + u1 + u2 + u3) * 0.25F;
		final float vCenter = (v0 + v1 + v2 + v3) * 0.25F;

		poly.uv(0, spriteIndex, Mth.lerp(nudge, u0, uCenter), Mth.lerp(nudge, v0, vCenter));
		poly.uv(1, spriteIndex, Mth.lerp(nudge, u1, uCenter), Mth.lerp(nudge, v1, vCenter));
		poly.uv(2, spriteIndex, Mth.lerp(nudge, u2, uCenter), Mth.lerp(nudge, v2, vCenter));
		poly.uv(3, spriteIndex, Mth.lerp(nudge, u3, uCenter), Mth.lerp(nudge, v3, vCenter));
	}

	private void applyTextureRotation(int spriteIndex, MutablePolygon poly) {
		final int vCount = poly.vertexCount();
		final TextureOrientation orientation = poly.rotation(spriteIndex);

		switch (orientation.rotation) {
			case ROTATE_NONE:
			default:
				break;

			case ROTATE_90:
				for (int i = 0; i < vCount; i++) {
					final float uOld = poly.u(i, spriteIndex);
					final float vOld = poly.v(i, spriteIndex);
					poly.uv(i, spriteIndex, vOld, 1 - uOld);
				}
				break;

			case ROTATE_180:
				for (int i = 0; i < vCount; i++) {
					final float uOld = poly.u(i, spriteIndex);
					final float vOld = poly.v(i, spriteIndex);
					poly.uv(i, spriteIndex, 1 - uOld, 1 - vOld);
				}
				break;

			case ROTATE_270:
				for (int i = 0; i < vCount; i++) {
					final float uOld = poly.u(i, spriteIndex);
					final float vOld = poly.v(i, spriteIndex);
					poly.uv(i, spriteIndex, 1 - vOld, uOld);
				}
				break;

		}

		if (orientation.flipU) {
			final float swap = poly.minU(spriteIndex);
			poly.minU(spriteIndex, poly.maxU(spriteIndex));
			poly.maxU(spriteIndex, swap);
		}

		if (orientation.flipV) {
			final float swap = poly.minV(spriteIndex);
			poly.minV(spriteIndex, poly.maxV(spriteIndex));
			poly.maxV(spriteIndex, swap);
		}
	}

}
