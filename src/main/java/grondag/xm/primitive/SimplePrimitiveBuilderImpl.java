/*******************************************************************************
 * Copyright 2019 grondag
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package grondag.xm.primitive;

import static grondag.xm.api.modelstate.ModelStateFlags.NONE;
import static grondag.xm.api.modelstate.ModelStateFlags.SIMPLE_JOIN;
import static grondag.xm.api.modelstate.ModelStateFlags.CORNER_JOIN;
import static org.apiguardian.api.API.Status.INTERNAL;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apiguardian.api.API;

import grondag.xm.Xm;
import grondag.xm.api.mesh.XmMesh;
import grondag.xm.api.mesh.polygon.Polygon;
import grondag.xm.api.modelstate.primitive.MutablePrimitiveState;
import grondag.xm.api.modelstate.primitive.PrimitiveState;
import grondag.xm.api.orientation.OrientationType;
import grondag.xm.api.primitive.SimplePrimitive;
import grondag.xm.api.primitive.SimplePrimitive.Builder;
import grondag.xm.api.primitive.base.AbstractSimplePrimitive;
import grondag.xm.api.primitive.surface.XmSurfaceList;
import grondag.xm.modelstate.SimpleModelStateImpl;

@API(status = INTERNAL)
public class SimplePrimitiveBuilderImpl {
    protected static class BuilderImpl implements Builder {
        private OrientationType orientationType = OrientationType.NONE;
        private XmSurfaceList list = XmSurfaceList.ALL;
        private Function<PrimitiveState, XmMesh> polyFactory;
        private int bitCount = 0;
        private boolean simpleJoin;
        private boolean cornerJoin;
        
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
        public Builder polyFactory(Function<PrimitiveState, XmMesh> polyFactory) {
            this.polyFactory = polyFactory == null ? t -> XmMesh.EMPTY : polyFactory;
            return this;
        }

        @Override
        public Builder primitiveBitCount(int bitCount) {
            this.bitCount = bitCount;
            return this;
        }

        @Override
        public Builder simpleJoin(boolean needsJoin) {
            this.simpleJoin = needsJoin;
            return this;
        }
        
        @Override
        public Builder cornerJoin(boolean needsJoin) {
            this.cornerJoin = needsJoin;
            return this;
        }
    }
    protected static class Primitive extends AbstractSimplePrimitive {
        private final XmMesh[] cachedQuads;
        
        private final OrientationType orientationType;
        
        private final Function<PrimitiveState, XmMesh> polyFactory;
        
        private boolean notifyException = true;
        
        private final int bitShift;
        
        private final boolean simpleJoin;
        private final boolean cornerJoin;
        
        static Function<PrimitiveState, XmSurfaceList> listWrapper(XmSurfaceList list) {
            return s -> list;
        }
        
        public Primitive(String idString, BuilderImpl builder) {
            super(idString, (builder.cornerJoin ? CORNER_JOIN : NONE) | (builder.simpleJoin ? SIMPLE_JOIN : NONE), SimpleModelStateImpl.FACTORY, listWrapper(builder.list));
            simpleJoin = builder.simpleJoin;
            cornerJoin = builder.cornerJoin;
            orientationType = builder.orientationType;
            polyFactory = builder.polyFactory;
            bitShift = builder.bitCount;
            int count = orientationType.enumClass.getEnumConstants().length << bitShift;
            if(simpleJoin) {
                count <<= 6;
            }
            cachedQuads = cornerJoin ? null : new XmMesh[count];
            invalidateCache();
        }
        
        @Override
        public void invalidateCache() {
            notifyException = true;
            if(!cornerJoin) {
                Arrays.fill(cachedQuads, null);
            }
        }
        
        @Override
        public OrientationType orientationType(PrimitiveState modelState) {
            return orientationType;
        }
        
        @Override
        public void emitQuads(PrimitiveState modelState, Consumer<Polygon> target) {
            try {
                XmMesh mesh;
                
                if (cornerJoin) {
                    mesh = polyFactory.apply(modelState);
                    
                } else {
                    int index = (modelState.orientationIndex() << bitShift) | modelState.primitiveBits();
                    if(simpleJoin) {
                        index = (index << 6) | modelState.simpleJoin().ordinal();
                    }
                    mesh =  cachedQuads[index];
                    if(mesh == null) {
                        mesh = polyFactory.apply(modelState);
                        cachedQuads[index] = mesh;
                    }
                }
                
                final Polygon reader = mesh.threadSafeReader();
                if(reader.origin()) {
                    do {
                        target.accept(reader);
                    } while (reader.next());
                }
                reader.release();
                
            } catch (Exception e) {
                if(notifyException) {
                    notifyException = false;
                    Xm.LOG.error("Unexpected exception while rendering primitive '" + this.translationKey() + "'.  Subsequent errors will be supressed.", e);
                }
            }
        }

        @Override
        public MutablePrimitiveState geometricState(PrimitiveState fromState) {
            MutablePrimitiveState result = newState()
                    .orientationIndex(fromState.orientationIndex())
                    .primitiveBits(fromState.primitiveBits());
            
            if (cornerJoin) {
                result.cornerJoin(fromState.cornerJoin());
            } else if (simpleJoin) {
                result.simpleJoin(fromState.simpleJoin());
            }
            
            return result;
        }

        @Override
        public boolean doesShapeMatch(PrimitiveState from, PrimitiveState to) {
            if (from.primitive() != to.primitive()
                    || from.orientationIndex() != to.orientationIndex()
                    || from.primitiveBits() != to.primitiveBits()) {
                return false;
            }
            if (cornerJoin) {
                return from.cornerJoin() == to.cornerJoin();
            } else if (simpleJoin) {
                return from.simpleJoin() == to.simpleJoin();
            } else {
                return true;
            }
        }
    }
    
    public static Builder builder() {
        return new BuilderImpl();
    }
}
