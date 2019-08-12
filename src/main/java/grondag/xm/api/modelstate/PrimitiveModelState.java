package grondag.xm.api.modelstate;

import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;

import grondag.xm.api.connect.model.ClockwiseRotation;
import grondag.xm.api.connect.state.CornerJoinState;
import grondag.xm.api.connect.state.SimpleJoinState;
import grondag.xm.api.paint.XmPaint;
import grondag.xm.api.primitive.ModelPrimitive;
import grondag.xm.api.surface.XmSurface;
import grondag.xm.mesh.polygon.Polygon;
import grondag.xm.model.varia.BlockOrientationType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public interface PrimitiveModelState<R extends PrimitiveModelState<R, W>, W extends PrimitiveModelState.Mutable<R,W>> extends ModelState {
    
    public static interface ModelStateFactory<R extends PrimitiveModelState<R, W>, W extends PrimitiveModelState.Mutable<R,W>> {

        W claim(ModelPrimitive<R, W> primitive);

        W fromBuffer(ModelPrimitive<R, W> primitive, PacketByteBuf buf);

        W fromTag(ModelPrimitive<R, W> primitive, CompoundTag tag);

    }
    
    ModelStateFactory<R, W> factory();

    @Override
    R toImmutable();

    @Override
    W mutableCopy();

    /**
     * Does NOT consider isStatic in comparison.<p>
     * 
     * {@inheritDoc}
     */
    @Override
    boolean equals(Object obj);

    @Override
    boolean equalsIncludeStatic(Object obj);

    /**
     * Returns true if visual elements and geometry match. Does not consider species
     * in matching.
     */
    boolean doShapeAndAppearanceMatch(ModelState other);

    /**
     * Returns true if visual elements match. Does not consider species or geometry
     * in matching.
     */
    boolean doesAppearanceMatch(ModelState other);

    @Override
    void serializeNBT(CompoundTag tag);

    void fromBytes(PacketByteBuf pBuff);

    @Override
    void toBytes(PacketByteBuf pBuff);

    int stateFlags();

    ModelPrimitive<R, W> primitive();

    @Override
    void produceQuads(Consumer<Polygon> target);

    @Override
    W geometricState();

    boolean hasAxisOrientation();

    boolean hasAxisRotation();

    boolean hasAxis();

    BlockOrientationType orientationType();

    boolean isAxisOrthogonalToPlacementFace();

    Direction rotateFace(Direction face);

    @Override
    boolean isStatic();

    boolean doPaintsMatch(ModelState other);

    int paintIndex(int surfaceIndex);

    XmPaint paint(int surfaceIndex);

    XmPaint paint(XmSurface surface);

    int posX();

    int posY();

    int posZ();

    /**
     * Means that one or more elements (like a texture) uses species. Does not mean
     * that the shape or block actually capture or generate species other than 0.
     */
    boolean hasSpecies();

    /**
     * Will return 0 if model state does not include species. This is more
     * convenient than checking each place species is used.
     * 
     * @return
     */
    int species();

    Direction.Axis axis();

    boolean isAxisInverted();
    
    CornerJoinState cornerJoin();
    
    SimpleJoinState simpleJoin();
    
    SimpleJoinState masonryJoin();


    /**
     * For machines and other blocks with a privileged horizontal face, North is
     * considered the zero rotation.
     */
    ClockwiseRotation axisRotation();

    int primitiveBits();

    ////////////////////////////////////////// RENDERING //////////////////////////////////////////
    
    @Override
    @Environment(EnvType.CLIENT)
    List<BakedQuad> getBakedQuads(BlockState state, Direction face, Random rand);

    @Override
    @Environment(EnvType.CLIENT)
    void emitQuads(RenderContext context);
    
    public static interface Mutable<R extends PrimitiveModelState<R, W>, W extends PrimitiveModelState.Mutable<R, W>> extends PrimitiveModelState<R, W>, ModelState.Mutable {
        @Override
        W copyFrom(ModelState template);

        @Override
        R releaseToImmutable();
        
        @Override
        W setStatic(boolean isStatic);

        W paint(int surfaceIndex, int paintIndex);

        W paint(int surfaceIndex, XmPaint paint);
        
        W paint(XmSurface surface, XmPaint paint);

        W paint(XmSurface surface, int paintIndex);

        W paintAll(XmPaint paint);

        W paintAll(int paintIndex);

        W posX(int index);

        W posY(int index);

        W posZ(int index);

        W pos(BlockPos pos);
        
        W species(int species);

        W axis(Direction.Axis axis);

        W setAxisInverted(boolean isInverted);

        W cornerJoin(CornerJoinState join);

        W simpleJoin(SimpleJoinState join);

        W masonryJoin(SimpleJoinState join);
        
        W axisRotation(ClockwiseRotation rotation);

        W primitiveBits(int bits);

        <T> T applyAndRelease(Function<ModelState, T> func);
        
        W apply(Consumer<W> consumer);
    }
}
