package grondag.xm.primitive;

import static grondag.xm.api.modelstate.ModelStateFlags.STATE_FLAG_NONE;

import java.util.function.Consumer;
import java.util.function.Function;

import grondag.xm.api.mesh.PolyTransform;
import grondag.xm.api.modelstate.SimpleModelState;
import grondag.xm.api.modelstate.SimpleModelState.Mutable;
import grondag.xm.api.primitive.SimplePrimitive;
import grondag.xm.api.primitive.SimplePrimitive.Builder;
import grondag.xm.api.primitive.base.AbstractSimplePrimitive;
import grondag.xm.api.primitive.surface.XmSurfaceList;
import grondag.xm.mesh.polygon.Polygon;
import grondag.xm.mesh.stream.PolyStream;
import grondag.xm.model.state.SimpleModelStateImpl;
import grondag.xm.model.varia.BlockOrientationType;

public class SimplePrimitiveBuilderImpl {
    protected static class BuilderImpl implements Builder {
        private BlockOrientationType orientationType = BlockOrientationType.NONE;
        private XmSurfaceList list = XmSurfaceList.ALL;
        private Function<PolyTransform, PolyStream> polyFactory;
        
        @Override
        public SimplePrimitive build(String idString) {
            return new Primitive(idString, this);
        }
        
        @Override
        public Builder surfaceList(XmSurfaceList list) {
            this.list = list == null ? XmSurfaceList.ALL : list;
            return this;
        }
        
        @Override
        public Builder orientationType(BlockOrientationType orientationType) {
            this.orientationType = orientationType == null ? BlockOrientationType.NONE : orientationType;
            return this;
        }
        
        @Override
        public Builder polyFactory(Function<PolyTransform, PolyStream> polyFactory) {
            this.polyFactory = polyFactory == null ? t -> PolyStream.EMPTY : polyFactory;
            return this;
        }
    }
    protected static class Primitive extends AbstractSimplePrimitive {
        private final PolyStream[] cachedQuads;
        
        private final BlockOrientationType orientationType;
        
        private final Function<PolyTransform, PolyStream> polyFactory;
        
        static Function<SimpleModelState, XmSurfaceList> listWrapper(XmSurfaceList list) {
            return s -> list;
        }
        
        public Primitive(String idString, BuilderImpl builder) {
            super(idString, STATE_FLAG_NONE, SimpleModelStateImpl.FACTORY, listWrapper(builder.list));
            orientationType = builder.orientationType;
            polyFactory = builder.polyFactory;
            cachedQuads = new PolyStream[orientationType.enumClass.getEnumConstants().length];
            invalidateCache();
        }
        
        @Override
        public void invalidateCache() {
            final int limit = cachedQuads.length;
            SimpleModelState.Mutable state = newState();
            for(int i = 0; i < limit; i++) {
                state.orientationIndex(i);
                PolyTransform transform = PolyTransform.get(state);
                cachedQuads[i] = polyFactory.apply(transform);
            }
            state.release();
        }
        
        @Override
        public BlockOrientationType orientationType(SimpleModelState modelState) {
            return orientationType;
        }
        
        @Override
        public void produceQuads(SimpleModelState modelState, Consumer<Polygon> target) {
            cachedQuads[modelState.orientationIndex()].forEach(target);
        }

        @Override
        public Mutable geometricState(SimpleModelState fromState) {
            return newState().orientationIndex(fromState.orientationIndex());
        }

        @Override
        public boolean doesShapeMatch(SimpleModelState from, SimpleModelState to) {
            return from.primitive() == to.primitive() && from.orientationIndex() == to.orientationIndex();
        }
    }
    
    public static Builder builder() {
        return new BuilderImpl();
    }
}
