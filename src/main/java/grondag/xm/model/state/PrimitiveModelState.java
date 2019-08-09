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

package grondag.xm.model.state;

import static grondag.xm.api.modelstate.ModelStateFlags.STATE_FLAG_NEEDS_CORNER_JOIN;
import static grondag.xm.api.modelstate.ModelStateFlags.STATE_FLAG_NEEDS_MASONRY_JOIN;
import static grondag.xm.api.modelstate.ModelStateFlags.STATE_FLAG_NEEDS_POS;
import static grondag.xm.api.modelstate.ModelStateFlags.STATE_FLAG_NEEDS_SIMPLE_JOIN;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;

import com.google.common.collect.ImmutableList;

import grondag.xm.Xm;
import grondag.xm.api.connect.model.ClockwiseRotation;
import grondag.xm.api.connect.state.CornerJoinState;
import grondag.xm.api.connect.state.SimpleJoinState;
import grondag.xm.api.connect.world.BlockNeighbors;
import grondag.xm.api.connect.world.ModelStateFunction;
import grondag.xm.api.modelstate.ModelState;
import grondag.xm.api.modelstate.MutableModelState;
import grondag.xm.api.primitive.ModelPrimitive;
import grondag.xm.api.primitive.ModelPrimitiveRegistry;
import grondag.xm.block.WorldToModelStateFunction;
import grondag.xm.block.XmMasonryMatch;
import grondag.xm.init.XmPrimitives;
import grondag.xm.painting.QuadPaintHandler;
import grondag.xm.terrain.TerrainState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.model.ModelHelper;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;

public class PrimitiveModelState extends AbstractPrimitiveModelState implements ModelState {
    private static ArrayBlockingQueue<Mutable> POOL = new ArrayBlockingQueue<>(4096);

    private static final PrimitiveModelState TEMPLATE = new PrimitiveModelState();

    static {
        TEMPLATE.primitive = XmPrimitives.CUBE;
    }

    static Mutable claim(ModelPrimitive primitive) {
        return claimInner(TEMPLATE, primitive);
    }
    
    static Mutable claim() {
        return claimInner(TEMPLATE, XmPrimitives.CUBE);
    }

    private static Mutable claimInner(PrimitiveModelState template, ModelPrimitive primitive) {
        Mutable result = POOL.poll();
        if (result == null) {
            result = new Mutable();
        } else {
            result.retain();
        }
        result.primitive = primitive;
        result.copyFrom(template);
        return result;
    }

    static class Mutable extends PrimitiveModelState implements MutableModelState {
        private Mutable() {}
        
        private Mutable(PrimitiveModelState template) {
            copyInternal(template);
        }
        
        @Override
        protected final void onLastRelease() {
            POOL.offer(this);
        }
        
        @Override
        public final MutableModelState setStatic(boolean isStatic) {
            setStaticInner(isStatic);
            return this;
        }

        @Override
        public final MutableModelState setAxis(Axis axis) {
            setAxisInner(axis);
            return this;
        }

        @Override
        public final MutableModelState setAxisInverted(boolean isInverted) {
            setAxisInvertedInner(isInverted);
            return this;
        }

        @Override
        public final MutableModelState posX(int index) {
            posXInner(index);
            return this;
        }

        @Override
        public final MutableModelState posY(int index) {
            posYInner(index);
            return this;
        }

        @Override
        public final MutableModelState posZ(int index) {
            posZInner(index);
            return this;
        }

        @Override
        public final MutableModelState species(int species) {
            speciesInner(species);
            return this;
        }

        @Override
        public final MutableModelState cornerJoin(CornerJoinState join) {
            cornerJoinInner(join);
            return this;
        }

        @Override
        public final MutableModelState simpleJoin(SimpleJoinState join) {
            simpleJoinInner(join);
            return this;
        }

        @Override
        public final MutableModelState masonryJoin(SimpleJoinState join) {
            masonryJoinInner(join);
            return this;
        }

        @Override
        public final MutableModelState setAxisRotation(ClockwiseRotation rotation) {
            setAxisRotationInner(rotation);
            return this;
        }

        @Override
        public final MutableModelState setTerrainStateKey(long terrainStateKey) {
            return this;
        }

        @Override
        public final MutableModelState setTerrainState(TerrainState flowState) {
            return this;
        }

        @Override
        public final void fromBytes(PacketByteBuf pBuff) {
            super.fromBytes(pBuff);
        }
        
        @Override
        public final boolean isImmutable() {
            return false;
        }
        
        @Override
        public final ModelState toImmutable() {
            return new PrimitiveModelState(this);
        }

        @Override
        public final MutableModelState primitiveBits(int bits) {
            primitiveBitsInner(bits);
            return this;
        }

        @Override
        public final MutableModelState pos(BlockPos pos) {
            posInner(pos);
            return this;
        }

        @Override
        public final MutableModelState copyFrom(ModelState template) {
            copyInternal((PrimitiveModelState)template);
            return this;
        }

        @Override
        public final MutableModelState paint(int surfaceIndex, int paintIndex) {
            paintInner(surfaceIndex, paintIndex);
            return this;
        }
    }

    private PrimitiveModelState() {}
    
    private PrimitiveModelState(PrimitiveModelState template) {
        copyInternal(template);
    }
    
    @Override
    public boolean isImmutable() {
        return true;
    }

    @Override
    public ModelState toImmutable() {
        return this;
    }

    @Environment(EnvType.CLIENT)
    private Mesh mesh = null;

    @Environment(EnvType.CLIENT)
    private List<BakedQuad>[] quadLists = null;

    public static final WorldToModelStateFunction DEFAULT_PRIMITIVE = (modelState, xmBlockState, world, pos, refreshFromWorld) -> {
        if(refreshFromWorld) {
            final int stateFlags = modelState.stateFlags();
            if ((stateFlags & STATE_FLAG_NEEDS_POS) == STATE_FLAG_NEEDS_POS) {
                modelState.pos(pos);
            }
            
            BlockNeighbors neighbors = null;
    
            if ((STATE_FLAG_NEEDS_CORNER_JOIN & stateFlags) == STATE_FLAG_NEEDS_CORNER_JOIN) {
                neighbors = BlockNeighbors.claim(world, pos, ModelStateFunction.STATIC, xmBlockState.blockJoinTest());
                modelState.cornerJoin(CornerJoinState.fromWorld(neighbors));
    
            } else if ((STATE_FLAG_NEEDS_SIMPLE_JOIN & stateFlags) == STATE_FLAG_NEEDS_SIMPLE_JOIN) {
                neighbors = BlockNeighbors.claim(world, pos, ModelStateFunction.STATIC, xmBlockState.blockJoinTest());
                modelState.simpleJoin(SimpleJoinState.fromWorld(neighbors));
            }
    
            if ((STATE_FLAG_NEEDS_MASONRY_JOIN & stateFlags) == STATE_FLAG_NEEDS_MASONRY_JOIN) {
                if (neighbors == null) {
                    neighbors = BlockNeighbors.claim(world, pos, ModelStateFunction.STATIC, XmMasonryMatch.INSTANCE);
                } else {
                    neighbors.withTest(XmMasonryMatch.INSTANCE);
                }
                modelState.masonryJoin(SimpleJoinState.fromWorld(neighbors));
            }
    
            if (neighbors != null) {
                neighbors.release();
            }
        }
    };

    @Environment(EnvType.CLIENT)
    private Mesh mesh() {
        Mesh result = mesh;
        if (result == null) {
            result = QuadPaintHandler.paint(this);
            mesh = result;
        }
        return result;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public List<BakedQuad> getBakedQuads(BlockState state, Direction face, Random rand) {
        List<BakedQuad>[] lists = quadLists;
        if (lists == null) {
            lists = ModelHelper.toQuadLists(mesh());
            quadLists = lists;
        }
        List<BakedQuad> result = lists[face == null ? 6 : face.getId()];
        return result == null ? ImmutableList.of() : result;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void emitQuads(RenderContext context) {
        context.meshConsumer().accept(mesh());
    }

    static Mutable fromTag(CompoundTag tag) {
        ModelPrimitive shape = ModelPrimitiveRegistry.INSTANCE.get(tag.getString(ModelStateTagHelper.NBT_SHAPE));
        if (shape == null) {
            return null;
        }
        Mutable result = claimInner(TEMPLATE, shape);

        if (tag.containsKey(ModelStateTagHelper.NBT_MODEL_BITS)) {
            int[] stateBits = tag.getIntArray(ModelStateTagHelper.NBT_MODEL_BITS);
            if (stateBits.length != 22) {
                Xm.LOG.warn("Bad or missing data encounter during ModelState NBT deserialization.");
            } else {
                result.deserializeFromInts(stateBits);
            }
        }

        // textures and vertex processors serialized by name because registered can
        // change if mods/config change
//        String layers = tag.getString(NBT_LAYERS);
//        if (layers.isEmpty()) {
//            String[] names = layers.split(",");
//            if (names.length != 0) {
//                int i = 0;
//                for (PaintLayer l : PaintLayer.VALUES) {
//                    if (ModelStateData.PAINT_TEXTURE[l.ordinal()].getValue(this) != 0) {
//                        TextureSet tex = TextureSetRegistryImpl.INSTANCE.getById(new Identifier(names[i++]));
//                        ModelStateData.PAINT_TEXTURE[l.ordinal()].setValue(tex.index(), this);
//                        if (i == names.length)
//                            break;
//                    }
//
//                    if (ModelStateData.PAINT_VERTEX_PROCESSOR[l.ordinal()].getValue(this) != 0) {
//                        VertexProcessor vp = VertexProcessors.get(names[i++]);
//                        ModelStateData.PAINT_VERTEX_PROCESSOR[l.ordinal()].setValue(vp.ordinal, this);
//                        if (i == names.length)
//                            break;
//                    }
//                }
//            }
//        }

        result.clearStateFlags();
        return result;
    }

    @Override
    public MutableModelState mutableCopy() {
        return new Mutable(this);
    }
}
