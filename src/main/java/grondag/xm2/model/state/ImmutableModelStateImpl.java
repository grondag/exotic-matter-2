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

import com.google.common.collect.ImmutableList;

import grondag.xm2.api.connect.model.ClockwiseRotation;
import grondag.xm2.api.connect.state.CornerJoinState;
import grondag.xm2.api.connect.state.SimpleJoinState;
import grondag.xm2.model.registry.ModelShape;
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

public class ImmutableModelStateImpl extends ModelStateImpl implements ImmutableModelState {

    public ImmutableModelStateImpl(ModelStateImpl template) {
        super(template);
    }

    @Override
    public void setStatic(boolean isStatic) {
        throw new IllegalStateException();
    }

    @Override
    public void setShape(ModelShape<?> shape) {
        throw new IllegalStateException();
    }

    @Override
    public void setAxis(Axis axis) {
        throw new IllegalStateException();
    }

    @Override
    public void setAxisInverted(boolean isInverted) {
        throw new IllegalStateException();
    }

    @Override
    public void setPosX(int index) {
        throw new IllegalStateException();
    }

    @Override
    public void setPosY(int index) {
        throw new IllegalStateException();
    }

    @Override
    public void setPosZ(int index) {
        throw new IllegalStateException();
    }

    @Override
    public void setStaticShapeBits(long bits) {
        throw new IllegalStateException();
    }

    @Override
    public void setSpecies(int species) {
        throw new IllegalStateException();
    }

    @Override
    public void setCornerJoin(CornerJoinState join) {
        throw new IllegalStateException();
    }

    @Override
    public void setSimpleJoin(SimpleJoinState join) {
        throw new IllegalStateException();
    }

    @Override
    public void setMasonryJoin(SimpleJoinState join) {
        throw new IllegalStateException();
    }

    @Override
    public void setAxisRotation(ClockwiseRotation rotation) {
        throw new IllegalStateException();
    }

    @Override
    public void setMultiBlockBits(long bits) {
        throw new IllegalStateException();
    }

    @Override
    public void setTerrainStateKey(long terrainStateKey) {
        throw new IllegalStateException();
    }

    @Override
    public void setTerrainState(TerrainState flowState) {
        throw new IllegalStateException();
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        throw new IllegalStateException();
    }
    
    @Override
    public void fromBytes(PacketByteBuf pBuff) {
        throw new IllegalStateException();
    }

    @Override
    public boolean isImmutable() {
        return true;
    }

    @Override
    public ImmutableModelState toImmutable() {
        return this;
    }

    @Environment(EnvType.CLIENT)
    private Mesh mesh = null;
    
    @Environment(EnvType.CLIENT)
    private List<BakedQuad>[] quadLists = null;
    
    @Environment(EnvType.CLIENT)
    private Mesh mesh() {
        Mesh result = mesh;
        if(result == null) {
            result = QuadPaintHandler.paint(this);
            mesh = result;
        }
        return result;
    }
    
    @Environment(EnvType.CLIENT)
    @Override
    public List<BakedQuad> getBakedQuads(BlockState state, Direction face, Random rand) {
        List<BakedQuad>[] lists = quadLists;
        if(lists == null) {
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
}