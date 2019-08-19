package grondag.xm.primitive;

import static grondag.xm.api.modelstate.ModelStateFlags.STATE_FLAG_NONE;

import java.util.function.Consumer;
import java.util.function.Function;

import grondag.xm.Xm;
import grondag.xm.api.mesh.XmMesh;
import grondag.xm.api.mesh.polygon.PolyTransform;
import grondag.xm.api.mesh.polygon.Polygon;
import grondag.xm.api.modelstate.SimpleModelState;
import grondag.xm.api.modelstate.SimpleModelState.Mutable;
import grondag.xm.api.orientation.OrientationType;
import grondag.xm.api.primitive.SimplePrimitive;
import grondag.xm.api.primitive.SimplePrimitive.Builder;
import grondag.xm.api.primitive.base.AbstractSimplePrimitive;
import grondag.xm.api.primitive.surface.XmSurfaceList;
import grondag.xm.model.state.SimpleModelStateImpl;

public class SimplePrimitiveBuilderImpl {
    protected static class BuilderImpl implements Builder {
        private OrientationType orientationType = OrientationType.NONE;
        private XmSurfaceList list = XmSurfaceList.ALL;
        private Function<PolyTransform, XmMesh> polyFactory;
        
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
        public Builder orientationType(OrientationType orientationType) {
            this.orientationType = orientationType == null ? OrientationType.NONE : orientationType;
            return this;
        }
        
        @Override
        public Builder polyFactory(Function<PolyTransform, XmMesh> polyFactory) {
            this.polyFactory = polyFactory == null ? t -> XmMesh.EMPTY : polyFactory;
            return this;
        }
    }
    protected static class Primitive extends AbstractSimplePrimitive {
        private final XmMesh[] cachedQuads;
        
        private final OrientationType orientationType;
        
        private final Function<PolyTransform, XmMesh> polyFactory;
        
        private boolean notifyException = true;
        
        static Function<SimpleModelState, XmSurfaceList> listWrapper(XmSurfaceList list) {
            return s -> list;
        }
        
        public Primitive(String idString, BuilderImpl builder) {
            super(idString, STATE_FLAG_NONE, SimpleModelStateImpl.FACTORY, listWrapper(builder.list));
            orientationType = builder.orientationType;
            polyFactory = builder.polyFactory;
            cachedQuads = new XmMesh[orientationType.enumClass.getEnumConstants().length];
            invalidateCache();
        }
        
        @Override
        public void invalidateCache() {
            notifyException = true;
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
        public OrientationType orientationType(SimpleModelState modelState) {
            return orientationType;
        }
        
        @Override
        public void produceQuads(SimpleModelState modelState, Consumer<Polygon> target) {
            try {
                cachedQuads[modelState.orientationIndex()].forEach(target);
            } catch (Exception e) {
                if(notifyException) {
                    notifyException = false;
                    Xm.LOG.error("Unexpected exception while rendering primitive '" + this.translationKey() + "'.  Subsequent errors will be supressed.", e);
                }
            }
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
