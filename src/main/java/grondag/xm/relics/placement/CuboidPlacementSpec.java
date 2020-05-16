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
package grondag.xm.relics.placement;

import static grondag.xm.relics.placement.PlacementPreviewRenderMode.OBSTRUCTED;
import static grondag.xm.relics.placement.PlacementPreviewRenderMode.PLACE;
import static grondag.xm.relics.placement.PlacementPreviewRenderMode.SELECT;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.function.BooleanSupplier;

import com.mojang.blaze3d.platform.GlStateManager;
import org.apache.commons.lang3.tuple.Pair;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.lwjgl.opengl.GL11;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import grondag.fermion.position.BlockRegion;
import grondag.fermion.position.CubicBlockRegion;
import grondag.fermion.world.WorldHelper;
import grondag.xm.render.RenderUtil;
import grondag.xm.render.XmRenderHelper;

@API(status = Status.DEPRECATED)
@Deprecated
public class CuboidPlacementSpec extends VolumetricPlacementSpec {

	protected CubicBlockRegion region;

	/**
	 * Where should block shape preview sample be drawn?
	 */
	protected BlockPos previewPos;

	public CuboidPlacementSpec(ItemStack placedStack, PlayerEntity player, PlacementPosition pPos) {
		super(placedStack, player, pPos);
		previewPos = pPos.inPos;
	}

	//    protected CuboidPlacementSpec buildSpec()
	//    {
	//        CuboidPlacementSpec result = new CuboidPlacementSpec(this);
	//        result.isHollow = this.isHollow;
	//        result.region = this.region;
	//        return result;
	//    }

	@Override
	protected boolean doValidate() {
		if (isSelectionInProgress) {
			region = new CubicBlockRegion(pPos.inPos, placementItem.fixedRegionSelectionPos(placedStack()).getLeft(), false);
			return true;
		}

		if (isFixedRegion) {
			final FixedRegionBounds bounds = placementItem.getFixedRegion(placedStack());
			region = new CubicBlockRegion(bounds.fromPos, bounds.toPos, isHollow);
			excludeObstaclesInRegion(region);

			outputStack = PlacementHandler.cubicPlacementStack(this);
			return canPlaceRegion(region);

		}

		else if (isExcavation) {
			// excavation regions do not take adjustment and are always
			// relative to the "inPos" block.
			BlockPos startPos = pPos.inPos;
			BlockPos endPos = pPos.inPos;

			if (offsetPos.getZ() > 1) {
				// depth
				endPos = endPos.offset(pPos.onFace.getOpposite(), offsetPos.getZ() - 1);
			}

			final Pair<Direction, Direction> nearestCorner = WorldHelper.closestAdjacentFaces(pPos.onFace, (float) pPos.hitX, (float) pPos.hitY,
					(float) pPos.hitZ);

			// height
			final int h = offsetPos.getY();
			if (h > 1) {
				final Direction relativeUp = WorldHelper.relativeUp(player, pPos.onFace);
				final int half_h = h / 2;

				final boolean isUpclosest = nearestCorner.getLeft() == relativeUp || nearestCorner.getRight() == relativeUp;

				final boolean fullUp = (h & 1) == 1 || isUpclosest;
				final boolean fullDown = (h & 1) == 1 || !isUpclosest;

				startPos = startPos.offset(relativeUp, fullUp ? half_h : half_h - 1);
				endPos = endPos.offset(relativeUp.getOpposite(), fullDown ? half_h : half_h - 1);
			}

			// width
			final int w = offsetPos.getX();
			if (w > 1) {
				final Direction relativeLeft = WorldHelper.relativeLeft(player, pPos.onFace);
				final int half_w = w / 2;

				final boolean isLeftclosest = nearestCorner.getLeft() == relativeLeft || nearestCorner.getRight() == relativeLeft;

				final boolean fullLeft = (w & 1) == 1 || isLeftclosest;
				final boolean fullRight = (w & 1) == 1 || !isLeftclosest;

				startPos = startPos.offset(relativeLeft, fullLeft ? half_w : half_w - 1);
				endPos = endPos.offset(relativeLeft.getOpposite(), fullRight ? half_w : half_w - 1);
			}

			region = new CubicBlockRegion(startPos, endPos, false);
			return true;
		}

		else {
			// pass null face into relative offset when using floating selection
			// to avoid re-orientation based on hit face
			final Direction offsetFace = pPos.isFloating ? null : pPos.onFace;

			BlockPos endPos = PlacementHandler.getPlayerRelativeOffset(pPos.inPos, offsetPos, player, offsetFace, OffsetPosition.FLIP_NONE);
			CubicBlockRegion region = new CubicBlockRegion(pPos.inPos, endPos, isHollow);

			excludeObstaclesInRegion(region);
			boolean isClear = canPlaceRegion(region);

			if (isAdjustmentEnabled && !isClear) {
				// try to adjust

				for (final OffsetPosition offset : OffsetPosition.ALTERNATES) {
					// start try pivoting the selection box around the position being targeted
					final BlockPos endPos2 = PlacementHandler.getPlayerRelativeOffset(pPos.inPos, offsetPos, player, offsetFace, offset);
					final CubicBlockRegion region2 = new CubicBlockRegion(pPos.inPos, endPos2, isHollow);
					excludeObstaclesInRegion(region2);

					if (canPlaceRegion(region2)) {
						endPos = endPos2;
						region = region2;
						isClear = true;
						break;
					}
				}

				if (!isClear) {
					// that didn't work, so try nudging the region a block in each direction
					final Direction[] checkOrder = PlacementHandler.faceCheckOrder(player, offsetFace);

					for (final Direction face : checkOrder) {
						final BlockPos startPos2 = pPos.inPos.offset(face);
						final BlockPos endPos2 = endPos.offset(face);
						final CubicBlockRegion region2 = new CubicBlockRegion(startPos2, endPos2, isHollow);
						excludeObstaclesInRegion(region2);
						if (canPlaceRegion(region2)) {
							endPos = endPos2;
							region = region2;
							previewPos = startPos2;
							isClear = true;
							break;
						}
					}
				}
			}

			this.region = region;

			outputStack = PlacementHandler.cubicPlacementStack(this);

			return isClear;
		}
	}

	@Environment(EnvType.CLIENT)
	@Override
	protected BlockPos previewPos() {
		return previewPos;
	}

	@Environment(EnvType.CLIENT)
	@Override
	protected void drawSelection(Tessellator tessellator, BufferBuilder bufferBuilder) {
		if (region == null)
			return;

		final Box box = region.toAABB();
		// draw edge without depth to show extent of region
		GlStateManager.disableDepthTest();
		GlStateManager.lineWidth(2.0F);
		bufferBuilder.begin(GL11.GL_LINE_STRIP, VertexFormats.POSITION_COLOR);
		WorldRenderer.drawBox(bufferBuilder, box.x1, box.y1, box.z1, box.x2, box.y2, box.z2, SELECT.red, SELECT.green, SELECT.blue, 1f);
		tessellator.draw();

		// draw sides with depth to better show what parts are unobstructed

		// TODO: reimplement with new render methods
		//        GlStateManager.enableDepthTest();
		//        bufferBuilder.begin(GL11.GL_TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
		//        WorldRenderer.buildBox(bufferBuilder, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, SELECT.red, SELECT.green, SELECT.blue, 0.4f);
		//        tessellator.draw();
	}

	@Environment(EnvType.CLIENT)
	@Override
	protected void drawPlacement(Tessellator tessellator, BufferBuilder bufferBuilder, PlacementPreviewRenderMode previewMode) {
		if (region == null)
			return;

		final Box box = region.toAABB();

		// fixed regions could be outside of view
		final Frustum visible = XmRenderHelper.frustum();
		if (!visible.isVisible(box))
			return;

		// draw edges without depth to show extent of region
		GlStateManager.disableDepthTest();
		GlStateManager.lineWidth(2.0F);
		bufferBuilder.begin(GL11.GL_LINE_STRIP, VertexFormats.POSITION_COLOR);
		WorldRenderer.drawBox(bufferBuilder, box.x1, box.y1, box.z1, box.x2, box.y2, box.z2, previewMode.red, previewMode.green,
				previewMode.blue, 1f);
		tessellator.draw();

		@SuppressWarnings("resource")
		final Entity entity = MinecraftClient.getInstance().cameraEntity;
		if (entity != null) {
			GlStateManager.lineWidth(1.0F);
			bufferBuilder.begin(GL11.GL_LINES, VertexFormats.POSITION_COLOR);
			RenderUtil.drawGrid(bufferBuilder, box, entity.getCameraPosVec(XmRenderHelper.tickDelta()), 0, 0, 0, previewMode.red, previewMode.green,
					previewMode.blue, 0.5f);

			tessellator.draw();
		}

		if (previewMode == OBSTRUCTED) {
			// try to show where obstructions are
			for (final BlockPos pos : region.exclusions()) {
				bufferBuilder.begin(GL11.GL_LINE_STRIP, VertexFormats.POSITION_COLOR);
				WorldRenderer.drawBox(bufferBuilder, pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1, OBSTRUCTED.red,
						OBSTRUCTED.green, OBSTRUCTED.blue, 1f);
				tessellator.draw();
			}
		} else if (previewMode == PLACE) {
			// show shape/orientation of blocks to be be placed via a sample
			drawPlacementPreview(tessellator, bufferBuilder);
		}

		// draw sides with depth to better show what parts are unobstructed
		// Will need to be fixed for new rendering methods
		//        GlStateManager.enableDepthTest();
		//        bufferBuilder.begin(GL11.GL_TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
		//        WorldRenderer.buildBox(bufferBuilder, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, previewMode.red, previewMode.green, previewMode.blue,
		//                0.4f);
		//        tessellator.draw();
	}

	@Override
	public BooleanSupplier worldTask(ServerPlayerEntity player) {
		if (isExcavation)
			// excavation world task sequences entries using
			// a flood fill starting with the block last clicked
			// by the player.
			return new BooleanSupplier() {
			//                private CuboidPlacementSpec spec = (CuboidPlacementSpec) buildSpec();
			//                private Job job = new Job(RequestPriority.MEDIUM, player);
			//                IDomain domain = DomainManager.instance().getActiveDomain(player);

			/**
			 * Block positions to be checked. Will initially contain only the starting
			 * (user-clicked) position. Entry is antecedent, or null if no dependency.
			 */
			//ArrayDeque<Pair<BlockPos, ExcavationTask>> queue = new ArrayDeque<Pair<BlockPos, ExcavationTask>>();
			ArrayDeque<BlockPos> queue = new ArrayDeque<>();

			/**
			 * Block positions that have been tested.
			 */
			HashSet<BlockPos> checked = new HashSet<>();

			World world = player.world;

			{
				scheduleVisitIfNotAlreadyVisited(pPos.inPos); //, null);
			}

			@Override
			public boolean getAsBoolean() {
				if (!queue.isEmpty()) {
					//Pair<BlockPos, ExcavationTask> visit = queue.poll();
					final BlockPos pos = queue.poll();
					if (pos != null) {
						//BlockPos pos = visit.getLeft();

						// is the position inside our region?
						// is the position inside the world?
						if (region.contains(pos) && World.method_24794(pos)) {
							boolean canPassThrough = false;

							final BlockState blockState = world.getBlockState(pos);

							// will be antecedent for any branches from here
							// if this is empty space, then will be antecedent for this visit
							//                                ExcavationTask branchAntecedent = visit.getRight();

							// is the block at the position affected
							// by this excavation?
							if (effectiveFilterMode.shouldAffectBlock(blockState, world, pos, outputStack, isVirtual)) {
								//                                    branchAntecedent = new ExcavationTask(pos);
								//                                    job.addTask(branchAntecedent);
								//                                    if (visit.getRight() != null) {
								//                                        AbstractTask.link(visit.getRight(), branchAntecedent);
								//                                    }
								canPassThrough = true;
							}

							// even if we can't excavate the block,
							// can we move through it to check others?
							canPassThrough = canPassThrough || !blockState.getMaterial().blocksMovement();

							// check adjacent blocks if are or will
							// be accessible and haven't already been
							// checked.
							if (canPassThrough) {
								// PERF: allocation
								scheduleVisitIfNotAlreadyVisited(pos.up()); //, branchAntecedent);
								scheduleVisitIfNotAlreadyVisited(pos.down(1)); //, branchAntecedent);
								scheduleVisitIfNotAlreadyVisited(pos.east()); //, branchAntecedent);
								scheduleVisitIfNotAlreadyVisited(pos.west()); //, branchAntecedent);
								scheduleVisitIfNotAlreadyVisited(pos.north()); //, branchAntecedent);
								scheduleVisitIfNotAlreadyVisited(pos.south()); //, branchAntecedent);
							}
						}
					}
				}

				if (queue.isEmpty()) {
					// when done, finalize entries list and submit job
					checked.clear();
					//                        if (domain != null)
					//                            domain.getCapability(JobManager.class).addJob(job);
					return false;
				} else
					return true;
			}

			private void scheduleVisitIfNotAlreadyVisited(BlockPos pos) { //, ExcavationTask task) {
				if (checked.contains(pos))
					return;
				checked.add(pos);
				queue.addLast(pos.toImmutable());
				//                    this.queue.addLast(Pair.of(pos, task));
			}
		};
		else
			// Placement world task places virtual blocks in the currently active build
			return new BooleanSupplier() {
			/**
			 * Block positions to be checked.
			 */
			private final Iterator<BlockPos> positionIterator = region.includedPositions().iterator();

			private final World world = player.world;

			//                private Build build = BuildManager.getActiveBuildForPlayer(player);
			//
			//                {
			//                    if (build == null) {
			//                        String chatMessage = I18n.translate("placement.message.no_build");
			//                        player.sendMessage(new TranslatableText(chatMessage));
			//                    }
			//                }

			@Override
			public boolean getAsBoolean() {
				//                    if (build == null)
				//                        return false;

				if (positionIterator.hasNext()) { // && build.isOpen()) {
					final BlockPos pos = positionIterator.next().toImmutable();

					// is the position inside the world?
					if (World.method_24794(pos)) {

						final BlockState blockState = world.getBlockState(pos);

						// is the block at the position affected
						// by this excavation?
						if (CuboidPlacementSpec.this.effectiveFilterMode.shouldAffectBlock(blockState, world, pos, CuboidPlacementSpec.this.placedStack(),
								CuboidPlacementSpec.this.isVirtual)) {
							PlacementHandler.placeVirtualBlock(world, CuboidPlacementSpec.this.outputStack, player, pos); //, build);
						}
					}

				}
				return positionIterator.hasNext();
				//                    return build != null && build.isOpen() && this.positionIterator.hasNext();
			}
		};
	}

	@Override
	public BlockRegion region() {
		return region;
	}
}
