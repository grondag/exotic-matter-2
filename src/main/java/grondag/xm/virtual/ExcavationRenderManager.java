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

package grondag.xm.virtual;

import java.util.ArrayList;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.client.player.LocalPlayer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import grondag.xm.Xm;
import grondag.xm.XmConfig;

@Internal
public class ExcavationRenderManager {
	private static final Int2ObjectOpenHashMap<ExcavationRenderer> excavations = new Int2ObjectOpenHashMap<>();

	/**
	 * Updated whenever map changes. Can be accessed safely from render thread
	 * without causing any concurrency problems because setting/accessing array
	 * value is safe. Could make volatile but not really a problem if a couple
	 * frames use stale data.
	 */
	private static ExcavationRenderer[] renderCopy = new ExcavationRenderer[0];

	/**
	 * Keep reference to avoid garbage creation.
	 */
	@Environment(EnvType.CLIENT)
	private static final ArrayList<ExcavationRenderer> secondPass = new ArrayList<>();

	@Environment(EnvType.CLIENT)
	public static void render(float tickDelta, LocalPlayer player) {
		if (player == null) {
			return;
		}

		if (renderCopy == null || renderCopy.length == 0) {
		}

		//		GlStateManager.enableBlend();
		//		GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
		//				GlStateManager.DestFactor.ZERO);
		//		GlStateManager.disableTexture();
		//		GlStateManager.depthMask(false);
		//		GlStateManager.disableDepthTest();
		//		GlStateManager.lineWidth(2);

		// TODO: reimplement for new rendering - this likely won't work
		//		final Tessellator tessellator = Tessellator.getInstance();
		//		final BufferBuilder bufferbuilder = tessellator.getBuffer();
		//		bufferbuilder.begin(GL11.GL_LINE_STRIP, VertexFormats.POSITION_COLOR);
		//
		//		secondPass.clear();
		//
		//		//		final Frustum visibleRegion = XmRenderHelper.frustum();
		//
		//		final double d0 = player.lastRenderX + (player.getX() - player.lastRenderX) * tickDelta;
		//		final double d1 = player.lastRenderY + (player.getY() - player.lastRenderY) * tickDelta;
		//		final double d2 = player.lastRenderZ + (player.getZ() - player.lastRenderZ) * tickDelta;
		//
		//		// TODO: needed?
		//		// bufferBuilder.setOffset(-d0, -d1, -d2);
		//
		//		for (final ExcavationRenderer ex : renderCopy) {
		//			// FIX: add back visibility test using supported API
		//			if (ex.bounds() != null) { // && visibleRegion.isVisible(ex.bounds())) {
		//				if (ex.drawBounds(bufferbuilder, player, d0, d1, d2, tickDelta)) {
		//					secondPass.add(ex);
		//				}
		//			}
		//		}
		//
		//		tessellator.draw();
		//
		//		if (!secondPass.isEmpty()) {
		//
		//			GlStateManager.lineWidth(1);
		//			bufferbuilder.begin(GL11.GL_LINES, VertexFormats.POSITION_COLOR);
		//			for (final ExcavationRenderer ex : secondPass) {
		//				ex.drawGrid(bufferbuilder, d0, d1, d2);
		//			}
		//			tessellator.draw();
		//
		//			GlStateManager.enableDepthTest();
		//
		//			// prevent z-fighting
		//			GlStateManager.enablePolygonOffset();
		//			GlStateManager.polygonOffset(-1, -1);
		//
		//			bufferbuilder.begin(GL11.GL_TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
		//			for (final ExcavationRenderer ex : secondPass) {
		//				ex.drawBox(bufferbuilder, d0, d1, d2);
		//			}
		//			tessellator.draw();
		//
		//			GlStateManager.disablePolygonOffset();
		//		}
		//
		//		GlStateManager.depthMask(true);
		//		GlStateManager.enableTexture();
		//		GlStateManager.disableBlend();
		//		GlStateManager.enableAlphaTest();
	}

	public static void clear() {
		excavations.clear();
		renderCopy = new ExcavationRenderer[0];
	}

	public static void addOrUpdate(ExcavationRenderer... renders) {
		for (final ExcavationRenderer render : renders) {
			excavations.put(render.id, render);
		}

		renderCopy = excavations.values().toArray(new ExcavationRenderer[excavations.size()]);

		if (XmConfig.logExcavationRenderTracking) {
			Xm.LOG.info("mass update, excavationSize = %d, renderSize = %d", excavations.size(), renderCopy.length);
		}
	}

	public static void addOrUpdate(ExcavationRenderer render) {
		excavations.put(render.id, render);
		renderCopy = excavations.values().toArray(new ExcavationRenderer[excavations.size()]);

		if (XmConfig.logExcavationRenderTracking) {
			Xm.LOG.info("addOrUpdate id = %d, excavationSize = %d, renderSize = %d", render.id, excavations.size(), renderCopy.length);
		}
	}

	public static void remove(int id) {
		excavations.remove(id);
		renderCopy = excavations.values().toArray(new ExcavationRenderer[excavations.size()]);

		if (XmConfig.logExcavationRenderTracking) {
			Xm.LOG.info("remove id = %d, excavationSize = %d, renderSize = %d", id, excavations.size(), renderCopy.length);
		}
	}
}
