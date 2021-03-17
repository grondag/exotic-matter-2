package grondag.xm.api.modelstate.primitive;

import org.jetbrains.annotations.ApiStatus.Experimental;

import grondag.xm.api.modelstate.base.MutableBaseModelState;

@Experimental
public interface MutablePrimitiveState extends PrimitiveState, MutableBaseModelState<PrimitiveState, MutablePrimitiveState> {

}