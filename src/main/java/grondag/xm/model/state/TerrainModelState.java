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

import grondag.xm.api.allocation.Reference;
import grondag.xm.api.connect.model.ClockwiseRotation;
import grondag.xm.api.model.ModelPrimitive;
import grondag.xm.api.model.ModelState;
import grondag.xm.api.model.MutableModelState;
import grondag.xm.api.model.OwnedModelState;
import grondag.xm.block.XmBlockRegistryImpl.XmBlockStateImpl;
import grondag.xm.terrain.TerrainState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.BlockView;

class TerrainModelState extends AbstractWorldModelState implements MutableModelState {
    protected TerrainModelState(ModelPrimitive primitive) {
        super();
        this.primitive = primitive;
    }

    private long flowBits;
    private int glowBits;

    @Override
    protected int intSize() {
        return super.intSize() + 3;
    }

    @Override
    protected void doRefreshFromWorld(XmBlockStateImpl xmState, BlockView world, BlockPos pos) {
        super.doRefreshFromWorld(xmState, world, pos);

        TerrainState.produceBitsFromWorldStatically(xmState.blockState, world, pos, (t, h) -> {
            this.flowBits = t;
            this.glowBits = h;
            return null;
        });
    }

    @Override
    public long getTerrainStateKey() {
        return flowBits;
    }

    @Override
    public int getTerrainHotness() {
        return glowBits;
    }

    @Override
    public void setTerrainStateKey(long terrainStateKey) {
        flowBits = terrainStateKey;
    }

    @Override
    public TerrainState getTerrainState() {
        return new TerrainState(flowBits, glowBits);
    }

    @Override
    public void setTerrainState(TerrainState flowState) {
        flowBits = flowState.getStateKey();
        glowBits = flowState.getHotness();
        invalidateHashCode();
    }

    @Override
    public TerrainModelState geometricState() {
        TerrainModelState result = new TerrainModelState(this.primitive);
        result.flowBits = this.flowBits;
        return result;
    }

    @Override
    public OwnedModelState mutableCopy() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean doShapeAndAppearanceMatch(ModelState other) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean doesAppearanceMatch(ModelState other) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void serializeNBT(CompoundTag tag) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isImmutable() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public <T extends Reference> T toImmutable() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setAxis(Axis axis) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setAxisInverted(boolean isInverted) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setAxisRotation(ClockwiseRotation rotation) {
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
    public MutableModelState copyFrom(ModelState template) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Direction rotateFace(Direction face) {
        // TODO Auto-generated method stub
        return null;
    }
}
