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

import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

import grondag.xm.api.modelstate.ModelState;
import grondag.xm.api.modelstate.MutableModelState;
import grondag.xm.block.XmBlockRegistryImpl.XmBlockStateImpl;
import grondag.xm.mesh.polygon.IPolygon;
import grondag.xm.terrain.TerrainState;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

//TODO: move to terrain package
public class TerrainModelState extends AbstractPrimitiveModelState<TerrainModelState> implements MutableModelState {
    public static final ModelStateFactory<TerrainModelState> FACTORY = new ModelStateFactory<>(TerrainModelState::new);
    

    private long flowBits;
    private int glowBits;

    @Override
    protected int intSize() {
        return super.intSize() + 3;
    }

    public void doRefreshFromWorld(XmBlockStateImpl xmState, BlockView world, BlockPos pos) {
        //TODO: restore super state retrieval and move whole thing to external helper
        //super.doRefreshFromWorld(xmState, world, pos);

        TerrainState.produceBitsFromWorldStatically(xmState.blockState, world, pos, (t, h) -> {
            this.flowBits = t;
            this.glowBits = h;
            return null;
        });
    }

    public long getTerrainStateKey() {
        return flowBits;
    }

    public int getTerrainHotness() {
        return glowBits;
    }

    public MutableModelState setTerrainStateKey(long terrainStateKey) {
        flowBits = terrainStateKey;
        return this;
    }

    public TerrainState getTerrainState() {
        return new TerrainState(flowBits, glowBits);
    }

    public MutableModelState setTerrainState(TerrainState flowState) {
        flowBits = flowState.getStateKey();
        glowBits = flowState.getHotness();
        invalidateHashCode();
        return this;
    }

    @Override
    public TerrainModelState geometricState() {
        TerrainModelState result = FACTORY.claim(this.primitive);
        result.flowBits = this.flowBits;
        return result;
    }

    @Override
    public TerrainModelState mutableCopy() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void serializeNBT(CompoundTag tag) {
        // TODO Auto-generated method stub

    }

    @Override
    public void fromBytes(PacketByteBuf pBuff) {
        super.fromBytes(pBuff);
        flowBits = pBuff.readLong();
        glowBits = pBuff.readVarInt();
    }

    @Override
    public void toBytes(PacketByteBuf pBuff) {
        super.toBytes(pBuff);
        pBuff.writeLong(flowBits);
        pBuff.writeVarInt(glowBits);
    }

    @Override
    public TerrainModelState copyFrom(ModelState template) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Direction rotateFace(Direction face) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<BakedQuad> getBakedQuads(BlockState state, Direction face, Random rand) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void emitQuads(RenderContext context) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void produceQuads(Consumer<IPolygon> target) {
        primitive.produceQuads(this, target);
    }

    @Override
    public ModelStateFactory<TerrainModelState> factory() {
        return FACTORY;
    }
}
