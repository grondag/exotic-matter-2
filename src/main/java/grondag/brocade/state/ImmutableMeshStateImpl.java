package grondag.brocade.state;

import java.util.List;
import java.util.Random;

import com.google.common.collect.ImmutableList;

import grondag.brocade.api.texture.TextureSet;
import grondag.brocade.connect.api.model.ClockwiseRotation;
import grondag.brocade.connect.api.state.CornerJoinState;
import grondag.brocade.connect.api.state.SimpleJoinState;
import grondag.brocade.mesh.ModelShape;
import grondag.brocade.painting.PaintLayer;
import grondag.brocade.painting.QuadPaintHandler;
import grondag.brocade.painting.VertexProcessor;
import grondag.brocade.terrain.TerrainState;
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
import net.minecraft.world.BlockView;

public class ImmutableMeshStateImpl extends MeshStateImpl implements ImmutableMeshState {

    public ImmutableMeshStateImpl(long coreBits, long shapeBits0, long shapeBits1, long layerBitsBase, long layerBitsCut,
            long layerBitsLamp, long layerBitsMiddle, long layerBitsOuter) {
        super(coreBits, shapeBits0, shapeBits1, layerBitsBase, layerBitsCut,
                layerBitsLamp, layerBitsMiddle, layerBitsOuter);
    }

    @Override
    public void setStatic(boolean isStatic) {
        throw new IllegalStateException();
    }

    @Override
    public MeshState refreshFromWorld(BlockState state, BlockView world, BlockPos pos) {
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
    public void disableLayer(PaintLayer layer) {
        throw new IllegalStateException();
    }

    @Override
    public void setTranslucent(PaintLayer layer, boolean isTranslucent) {
        throw new IllegalStateException();
    }

    @Override
    public void setTexture(PaintLayer layer, TextureSet tex) {
        throw new IllegalStateException();
    }

    @Override
    public void setVertexProcessor(PaintLayer layer, VertexProcessor vp) {
        throw new IllegalStateException();
    }

    @Override
    public void setEmissive(PaintLayer layer, boolean isEmissive) {
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
    public void setMetaData(int meta) {
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
    public ImmutableMeshState toImmutable() {
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
