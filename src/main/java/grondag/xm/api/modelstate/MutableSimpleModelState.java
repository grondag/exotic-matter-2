package grondag.xm.api.modelstate;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import org.apiguardian.api.API;

@API(status = EXPERIMENTAL)
public interface MutableSimpleModelState extends SimpleModelState, MutablePrimitiveModelState<SimpleModelState, MutableSimpleModelState> {
    
}