package grondag.xm2.dispatch;

import grondag.fermion.cache.ObjectSimpleLoadingCache;
import grondag.xm2.state.ImmutableModelState;
import grondag.xm2.state.ModelState;

// PERF: consider having keys cache their own output: set vs map
// Could this mean holders of keys could trade for an immutable copy
// with direct access to the result?

// custom loading cache is at least 2X faster than guava LoadingCache for our
// use case
public class XmDispatcher extends ObjectSimpleLoadingCache<ModelState, ImmutableModelState> {
   
    public static final XmDispatcher INSTANCE = new XmDispatcher(0xFFFF);
    
    public XmDispatcher(int maxSize) {
        super(k -> k.toImmutable(), maxSize);
    }
}
