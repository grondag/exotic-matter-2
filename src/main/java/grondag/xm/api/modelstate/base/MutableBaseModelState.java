package grondag.xm.api.modelstate.base;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.util.function.Consumer;
import java.util.function.Function;

import org.apiguardian.api.API;

import grondag.xm.api.connect.state.CornerJoinState;
import grondag.xm.api.connect.state.SimpleJoinState;
import grondag.xm.api.modelstate.ModelState;
import grondag.xm.api.modelstate.MutableModelState;
import grondag.xm.api.paint.XmPaint;
import grondag.xm.api.primitive.surface.XmSurface;
import net.minecraft.util.math.BlockPos;

@API(status = EXPERIMENTAL)
public interface MutableBaseModelState<R extends BaseModelState<R, W>, W extends MutableBaseModelState<R, W>> extends BaseModelState<R, W>, MutableModelState {
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

    W orientationIndex(int index);

    W cornerJoin(CornerJoinState join);

    W simpleJoin(SimpleJoinState join);

    W masonryJoin(SimpleJoinState join);

    W primitiveBits(int bits);

    <T> T applyAndRelease(Function<ModelState, T> func);

    W apply(Consumer<W> consumer);
}