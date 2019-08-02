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

package grondag.xm2.model.state;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;

import com.google.common.collect.ImmutableList;

import grondag.xm2.Xm;
import grondag.xm2.api.connect.model.ClockwiseRotation;
import grondag.xm2.api.connect.state.CornerJoinState;
import grondag.xm2.api.connect.state.SimpleJoinState;
import grondag.xm2.api.model.ImmutableModelState;
import grondag.xm2.api.model.ModelPrimitive;
import grondag.xm2.api.model.ModelPrimitiveRegistry;
import grondag.xm2.api.model.OwnedModelState;
import grondag.xm2.init.XmPrimitives;
import grondag.xm2.painting.QuadPaintHandler;
import grondag.xm2.terrain.TerrainState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.model.ModelHelper;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;

class PrimitiveModelState extends AbstractPrimitiveModelState implements ImmutableModelState, OwnedModelState {
    private static ArrayBlockingQueue<PrimitiveModelState> POOL = new ArrayBlockingQueue<>(4096);

    private static final PrimitiveModelState TEMPLATE = new PrimitiveModelState();

    static {
        TEMPLATE.primitive = XmPrimitives.CUBE;
    }

    static PrimitiveModelState claim(ModelPrimitive primitive) {
        return claimInner(TEMPLATE, primitive);
    }
    
    static PrimitiveModelState claim() {
        return claimInner(TEMPLATE, XmPrimitives.CUBE);
    }

    private static PrimitiveModelState claimInner(PrimitiveModelState template, ModelPrimitive primitive) {
        PrimitiveModelState result = POOL.poll();
        if (result == null) {
            result = new PrimitiveModelState();
        } else {
            result.isImmutable = false;
        }
        result.primitive = primitive;
        result.copyFrom(template);
        return result;
    }

    private static void release(PrimitiveModelState state) {
        state.isImmutable = true;
        POOL.offer(state);
    }

    private boolean isImmutable = false;

    @Override
    public void setStatic(boolean isStatic) {
        if (isImmutable)
            throw new IllegalStateException();
        super.setStatic(isStatic);
    }

    @Override
    public void setAxis(Axis axis) {
        if (isImmutable)
            throw new IllegalStateException();
        super.setAxis(axis);
    }

    @Override
    public void setAxisInverted(boolean isInverted) {
        if (isImmutable)
            throw new IllegalStateException();
        super.setAxisInverted(isInverted);
    }

    @Override
    public void posX(int index) {
        if (isImmutable)
            throw new IllegalStateException();
        super.posX(index);
    }

    @Override
    public void posY(int index) {
        if (isImmutable)
            throw new IllegalStateException();
        super.posY(index);
    }

    @Override
    public void posZ(int index) {
        if (isImmutable)
            throw new IllegalStateException();
        super.posZ(index);
    }

    @Override
    public void species(int species) {
        if (isImmutable)
            throw new IllegalStateException();
        super.species(species);
    }

    @Override
    public void cornerJoin(CornerJoinState join) {
        if (isImmutable)
            throw new IllegalStateException();
        super.cornerJoin(join);
    }

    @Override
    public void simpleJoin(SimpleJoinState join) {
        if (isImmutable)
            throw new IllegalStateException();
        super.simpleJoin(join);
    }

    @Override
    public void masonryJoin(SimpleJoinState join) {
        if (isImmutable)
            throw new IllegalStateException();
        super.masonryJoin(join);
    }

    @Override
    public void setAxisRotation(ClockwiseRotation rotation) {
        if (isImmutable)
            throw new IllegalStateException();
        super.setAxisRotation(rotation);
    }

    @Override
    public void setTerrainStateKey(long terrainStateKey) {
        if (isImmutable)
            throw new IllegalStateException();
        super.setTerrainStateKey(terrainStateKey);
    }

    @Override
    public void setTerrainState(TerrainState flowState) {
        if (isImmutable)
            throw new IllegalStateException();
        super.setTerrainState(flowState);
    }

    @Override
    public void fromBytes(PacketByteBuf pBuff) {
        if (isImmutable)
            throw new IllegalStateException();
        super.fromBytes(pBuff);
    }

    @Override
    public boolean isImmutable() {
        return isImmutable;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ImmutableModelState toImmutable() {
        if (isImmutable) {
            return this;
        } else {
            PrimitiveModelState result = claimInner(this, this.primitive);
            result.isImmutable = true;
            return result;
        }
    }

    @Environment(EnvType.CLIENT)
    private Mesh mesh = null;

    @Environment(EnvType.CLIENT)
    private List<BakedQuad>[] quadLists = null;

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

    static OwnedModelState fromTag(CompoundTag tag) {
        ModelPrimitive shape = ModelPrimitiveRegistry.INSTANCE.get(tag.getString(ModelStateTagHelper.NBT_SHAPE));
        if (shape == null) {
            return null;
        }
        PrimitiveModelState result = claimInner(TEMPLATE, shape);

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
    public OwnedModelState mutableCopy() {
        return claimInner(this, this.primitive);
    }

    @Override
    public void release() {
        if (isImmutable)
            throw new IllegalStateException();
        release(this);
    }
}
