package grondag.brocade.dispatch;

import grondag.brocade.state.ImmutableMeshState;
import grondag.brocade.state.MeshState;
import grondag.fermion.cache.ObjectSimpleLoadingCache;

// PERF: consider having keys cache their own output: set vs map
// Could this mean holders of keys could trade for an immutable copy
// with direct access to the result?

// custom loading cache is at least 2X faster than guava LoadingCache for our
// use case
public class BrocadeDispatcher extends ObjectSimpleLoadingCache<MeshState, ImmutableMeshState> {
   
    public static final BrocadeDispatcher INSTANCE = new BrocadeDispatcher(0xFFFF);
    
    public BrocadeDispatcher(int maxSize) {
        super(k -> k.toImmutable(), maxSize);
    }
}
