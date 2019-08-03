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
package grondag.xm.placement;

import static grondag.xm.placement.PlacementPreviewRenderMode.OBSTRUCTED;
import static grondag.xm.placement.PlacementPreviewRenderMode.PLACE;
import static grondag.xm.placement.PlacementPreviewRenderMode.SELECT;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.function.BooleanSupplier;

import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;

import grondag.fermion.position.BlockRegion;
import grondag.fermion.position.CubicBlockRegion;
import grondag.fermion.world.WorldHelper;
import grondag.xm.dispatch.RenderUtil;
import grondag.xm.render.XmRenderHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VisibleRegion;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class CuboidPlacementSpec extends VolumetricPlacementSpec {

    protected CubicBlockRegion region;

    /**
     * Where should block shape preview sample be drawn?
     */
    protected BlockPos previewPos;

    public CuboidPlacementSpec(ItemStack placedStack, PlayerEntity player, PlacementPosition pPos) {
        super(placedStack, player, pPos);
        this.previewPos = pPos.inPos;
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
        if (this.isSelectionInProgress) {
            this.region = new CubicBlockRegion(pPos.inPos, this.placementItem.fixedRegionSelectionPos(this.placedStack()).getLeft(), false);
            return true;
        }

        if (this.isFixedRegion) {
            FixedRegionBounds bounds = this.placementItem.getFixedRegion(this.placedStack());
            this.region = new CubicBlockRegion(bounds.fromPos, bounds.toPos, this.isHollow);
            this.excludeObstaclesInRegion(this.region);

            this.outputStack = PlacementHandler.cubicPlacementStack(this);
            return this.canPlaceRegion(region);

        }

        else if (this.isExcavation) {
            // excavation regions do not take adjustment and are always
            // relative to the "inPos" block.
            BlockPos startPos = pPos.inPos;
            BlockPos endPos = pPos.inPos;

            if (this.offsetPos.getZ() > 1) {
                // depth
                endPos = endPos.offset(pPos.onFace.getOpposite(), this.offsetPos.getZ() - 1);
            }

            Pair<Direction, Direction> nearestCorner = WorldHelper.closestAdjacentFaces(this.pPos.onFace, (float) this.pPos.hitX, (float) this.pPos.hitY,
                    (float) this.pPos.hitZ);

            // height
            final int h = this.offsetPos.getY();
            if (h > 1) {
                Direction relativeUp = WorldHelper.relativeUp(this.player, this.pPos.onFace);
                final int half_h = h / 2;

                final boolean isUpclosest = nearestCorner.getLeft() == relativeUp || nearestCorner.getRight() == relativeUp;

                final boolean fullUp = (h & 1) == 1 || isUpclosest;
                final boolean fullDown = (h & 1) == 1 || !isUpclosest;

                startPos = startPos.offset(relativeUp, fullUp ? half_h : half_h - 1);
                endPos = endPos.offset(relativeUp.getOpposite(), fullDown ? half_h : half_h - 1);
            }

            // width
            final int w = this.offsetPos.getX();
            if (w > 1) {
                Direction relativeLeft = WorldHelper.relativeLeft(this.player, this.pPos.onFace);
                final int half_w = w / 2;

                final boolean isLeftclosest = nearestCorner.getLeft() == relativeLeft || nearestCorner.getRight() == relativeLeft;

                final boolean fullLeft = (w & 1) == 1 || isLeftclosest;
                final boolean fullRight = (w & 1) == 1 || !isLeftclosest;

                startPos = startPos.offset(relativeLeft, fullLeft ? half_w : half_w - 1);
                endPos = endPos.offset(relativeLeft.getOpposite(), fullRight ? half_w : half_w - 1);
            }

            this.region = new CubicBlockRegion(startPos, endPos, false);
            return true;
        }

        else {
            // pass null face into relative offset when using floating selection
            // to avoid re-orientation based on hit face
            Direction offsetFace = pPos.isFloating ? null : pPos.onFace;

            BlockPos endPos = PlacementHandler.getPlayerRelativeOffset(this.pPos.inPos, this.offsetPos, this.player, offsetFace, OffsetPosition.FLIP_NONE);
            CubicBlockRegion region = new CubicBlockRegion(pPos.inPos, endPos, this.isHollow);

            this.excludeObstaclesInRegion(region);
            boolean isClear = this.canPlaceRegion(region);

            if (this.isAdjustmentEnabled && !isClear) {
                // try to adjust

                for (OffsetPosition offset : OffsetPosition.ALTERNATES) {
                    // start try pivoting the selection box around the position being targeted
                    BlockPos endPos2 = PlacementHandler.getPlayerRelativeOffset(this.pPos.inPos, this.offsetPos, this.player, offsetFace, offset);
                    CubicBlockRegion region2 = new CubicBlockRegion(pPos.inPos, endPos2, this.isHollow);
                    this.excludeObstaclesInRegion(region2);

                    if (this.canPlaceRegion(region2)) {
                        endPos = endPos2;
                        region = region2;
                        isClear = true;
                        break;
                    }
                }

                if (!isClear) {
                    // that didn't work, so try nudging the region a block in each direction
                    Direction[] checkOrder = PlacementHandler.faceCheckOrder(player, offsetFace);

                    for (Direction face : checkOrder) {
                        BlockPos startPos2 = pPos.inPos.offset(face);
                        BlockPos endPos2 = endPos.offset(face);
                        CubicBlockRegion region2 = new CubicBlockRegion(startPos2, endPos2, isHollow);
                        this.excludeObstaclesInRegion(region2);
                        if (this.canPlaceRegion(region2)) {
                            endPos = endPos2;
                            region = region2;
                            this.previewPos = startPos2;
                            isClear = true;
                            break;
                        }
                    }
                }
            }

            this.region = region;

            this.outputStack = PlacementHandler.cubicPlacementStack(this);

            return isClear;
        }
    }

    @Environment(EnvType.CLIENT)
    @Override
    protected BlockPos previewPos() {
        return this.previewPos;
    }

    @Environment(EnvType.CLIENT)
    @Override
    protected void drawSelection(Tessellator tessellator, BufferBuilder bufferBuilder) {
        if (this.region == null)
            return;

        Box box = this.region.toAABB();
        // draw edge without depth to show extent of region
        GlStateManager.disableDepthTest();
        GlStateManager.lineWidth(2.0F);
        bufferBuilder.begin(GL11.GL_LINE_STRIP, VertexFormats.POSITION_COLOR);
        WorldRenderer.buildBoxOutline(bufferBuilder, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, SELECT.red, SELECT.green, SELECT.blue, 1f);
        tessellator.draw();

        // draw sides with depth to better show what parts are unobstructed
        GlStateManager.enableDepthTest();
        bufferBuilder.begin(GL11.GL_TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
        WorldRenderer.buildBox(bufferBuilder, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, SELECT.red, SELECT.green, SELECT.blue, 0.4f);
        tessellator.draw();
    }

    @Environment(EnvType.CLIENT)
    @Override
    protected void drawPlacement(Tessellator tessellator, BufferBuilder bufferBuilder, PlacementPreviewRenderMode previewMode) {
        if (this.region == null)
            return;

        Box box = this.region.toAABB();

        // fixed regions could be outside of view
        final VisibleRegion visible = XmRenderHelper.visibleRegion();
        if (!visible.intersects(box))
            return;

        // draw edges without depth to show extent of region
        GlStateManager.disableDepthTest();
        GlStateManager.lineWidth(2.0F);
        bufferBuilder.begin(GL11.GL_LINE_STRIP, VertexFormats.POSITION_COLOR);
        WorldRenderer.buildBoxOutline(bufferBuilder, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, previewMode.red, previewMode.green,
                previewMode.blue, 1f);
        tessellator.draw();

        Entity entity = MinecraftClient.getInstance().cameraEntity;
        if (entity != null) {
            GlStateManager.lineWidth(1.0F);
            bufferBuilder.begin(GL11.GL_LINES, VertexFormats.POSITION_COLOR);
            RenderUtil.drawGrid(bufferBuilder, box, entity.getCameraPosVec(XmRenderHelper.tickDelta()), 0, 0, 0, previewMode.red, previewMode.green,
                    previewMode.blue, 0.5f);

            tessellator.draw();
        }

        if (previewMode == OBSTRUCTED) {
            // try to show where obstructions are
            for (BlockPos pos : this.region.exclusions()) {
                bufferBuilder.begin(GL11.GL_LINE_STRIP, VertexFormats.POSITION_COLOR);
                WorldRenderer.buildBoxOutline(bufferBuilder, pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1, OBSTRUCTED.red,
                        OBSTRUCTED.green, OBSTRUCTED.blue, 1f);
                tessellator.draw();
            }
        } else if (previewMode == PLACE) {
            // show shape/orientation of blocks to be be placed via a sample
            this.drawPlacementPreview(tessellator, bufferBuilder);
        }

        // draw sides with depth to better show what parts are unobstructed
        GlStateManager.enableDepthTest();
        bufferBuilder.begin(GL11.GL_TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
        WorldRenderer.buildBox(bufferBuilder, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, previewMode.red, previewMode.green, previewMode.blue,
                0.4f);
        tessellator.draw();
    }

    @Override
    public BooleanSupplier worldTask(ServerPlayerEntity player) {
        if (this.isExcavation) {
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
                HashSet<BlockPos> checked = new HashSet<BlockPos>();

                World world = player.world;

                {
                    scheduleVisitIfNotAlreadyVisited(pPos.inPos); //, null);
                }

                @Override
                public boolean getAsBoolean() {
                    if (!queue.isEmpty()) {
                        //Pair<BlockPos, ExcavationTask> visit = queue.poll();
                        BlockPos pos = queue.poll();
                        if (pos != null) {
                            //BlockPos pos = visit.getLeft();

                            // is the position inside our region?
                            // is the position inside the world?
                            if (region.contains(pos) && World.isValid(pos)) {
                                boolean canPassThrough = false;

                                BlockState blockState = world.getBlockState(pos);

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
                                    scheduleVisitIfNotAlreadyVisited(pos.down()); //, branchAntecedent);
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
                        this.checked.clear();
//                        if (domain != null)
//                            domain.getCapability(JobManager.class).addJob(job);
                        return false;
                    } else {
                        return true;
                    }
                }

                private void scheduleVisitIfNotAlreadyVisited(BlockPos pos) { //, ExcavationTask task) {
                    if (this.checked.contains(pos))
                        return;
                    this.checked.add(pos);
                    this.queue.addLast(pos.toImmutable());
//                    this.queue.addLast(Pair.of(pos, task));
                }
            };
        } else {
            // Placement world task places virtual blocks in the currently active build
            return new BooleanSupplier() {
                /**
                 * Block positions to be checked.
                 */
                private Iterator<BlockPos> positionIterator = CuboidPlacementSpec.this.region.includedPositions().iterator();

                private World world = player.world;

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
                        BlockPos pos = positionIterator.next().toImmutable();

                        // is the position inside the world?
                        if (World.isValid(pos)) {

                            BlockState blockState = world.getBlockState(pos);

                            // is the block at the position affected
                            // by this excavation?
                            if (CuboidPlacementSpec.this.effectiveFilterMode.shouldAffectBlock(blockState, world, pos, CuboidPlacementSpec.this.placedStack(),
                                    CuboidPlacementSpec.this.isVirtual)) {
                                PlacementHandler.placeVirtualBlock(world, CuboidPlacementSpec.this.outputStack, player, pos); //, build);
                            }
                        }

                    }
                    return this.positionIterator.hasNext();
//                    return build != null && build.isOpen() && this.positionIterator.hasNext();
                }
            };

        }
    }

    @Override
    public BlockRegion region() {
        return this.region;
    }
}
