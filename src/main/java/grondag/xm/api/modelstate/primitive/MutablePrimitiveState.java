package grondag.xm.api.modelstate.primitive;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import org.apiguardian.api.API;

import grondag.xm.api.modelstate.base.MutableBaseModelState;

@API(status = EXPERIMENTAL)
public interface MutablePrimitiveState extends PrimitiveState, MutableBaseModelState<PrimitiveState, MutablePrimitiveState> {

}