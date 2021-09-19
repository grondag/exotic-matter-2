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
package grondag.xm.virtual;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import com.mojang.blaze3d.vertex.BufferBuilder;
import grondag.xm.Xm;
import grondag.xm.XmConfig;
import grondag.xm.render.OutlineRenderer;

@Internal
public class ExcavationRenderer {
	public final int id;

	/**
	 * If true, is replacement instead of straight excavation.
	 */
	public final boolean isExchange;

	private AABB aabb;

	private AABB visibilityBounds;

	private boolean didDrawBoundsLastTime = false;

	private Vec3 lastEyePosition;

	/**
	 * If non-null, then we should render individual positions instead of AABB.
	 */
	private BlockPos[] positions;

	public ExcavationRenderer(int id, AABB aabb, boolean isExchange, @Nullable BlockPos[] positions) {
		this.id = id;
		this.isExchange = isExchange;
		setBounds(aabb, positions);
	}

	public void setBounds(AABB bounds, @Nullable BlockPos[] positions) {
		aabb = bounds;
		visibilityBounds = bounds.inflate(192);
		this.positions = positions;

		if (XmConfig.logExcavationRenderTracking) {
			Xm.LOG.info("id %d Renderer setBounds position count = %d", id, positions == null ? 0 : positions.length);
		}
	}

	public AABB bounds() {
		return aabb;
	}

	public AABB visibilityBounds() {
		return visibilityBounds;
	}

	/** return true if something was drawn */
	@Environment(EnvType.CLIENT)
	public boolean drawBounds(BufferBuilder bufferbuilder, Entity viewEntity, double d0, double d1, double d2, float partialTicks) {
		lastEyePosition = viewEntity.getEyePosition(partialTicks);
		if (visibilityBounds.contains(lastEyePosition)) {
			if (positions == null) {
				final AABB box = aabb;
				LevelRenderer.addChainedFilledBoxVertices(bufferbuilder, box.minX - d0, box.minY - d1, box.minZ - d2, box.maxX - d0, box.maxY - d1, box.maxZ - d2, 1f, 0.3f, 0.3f,
					1f);
			} else {
				for (final BlockPos pos : positions) {
					final double x = pos.getX() - d0;
					final double y = pos.getY() - d1;
					final double z = pos.getZ() - d2;
					LevelRenderer.addChainedFilledBoxVertices(bufferbuilder, x, y, z, x + 1, y + 1, z + 1, 1f, 0.3f, 0.3f, 1f);
				}
			}
			didDrawBoundsLastTime = true;
			return true;
		} else {
			didDrawBoundsLastTime = false;
			return false;
		}
	}

	@Environment(EnvType.CLIENT)
	public void drawGrid(BufferBuilder buffer, double d0, double d1, double d2) {
		if (didDrawBoundsLastTime && positions == null) {
			OutlineRenderer.drawGrid(buffer, aabb, lastEyePosition, d0, d1, d2, 1f, 0.3f, 0.3f, 0.5F);
		}
	}

	@Environment(EnvType.CLIENT)
	public void drawBox(BufferBuilder bufferbuilder, double d0, double d1, double d2) {
		if (didDrawBoundsLastTime) {
			if (positions == null) {
				final AABB box = aabb;
				LevelRenderer.addChainedFilledBoxVertices(bufferbuilder, box.minX - d0, box.minY - d1, box.minZ - d2, box.maxX - d0, box.maxY - d1, box.maxZ - d2, 1f, 0.3f,
					0.3f, 0.3f);
			} else {
				for (final BlockPos pos : positions) {
					final double x = pos.getX() - d0;
					final double y = pos.getY() - d1;
					final double z = pos.getZ() - d2;
					LevelRenderer.addChainedFilledBoxVertices(bufferbuilder, x, y, z, x + 1, y + 1, z + 1, 1f, 0.3f, 0.3f, 0.3f);
				}
			}
		}
	}
}
