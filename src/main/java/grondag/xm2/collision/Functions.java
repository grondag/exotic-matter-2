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

package grondag.xm2.collision;

public interface Functions {

    @FunctionalInterface
    public static interface AreaBoundsIntFunction {
        /**
         * Max values are inclusive.
         */
        int apply(int xMin, int yMin, int xMax, int yMax);
    }
    
    @FunctionalInterface
    public static interface BoxBoundsIntConsumer {
        void accept(int minX, int minY, int minZ, int maxX, int maxY, int maxZ);
    }
    
    @FunctionalInterface
    public static interface BoxBoundsIntFunction {
        int accept(int minX, int minY, int minZ, int maxX, int maxY, int maxZ);
    }
    
    @FunctionalInterface
    public static interface BoxBoundsObjectFunction<V> {
        V accept(int minX, int minY, int minZ, int maxX, int maxY, int maxZ);
    }
    
    @FunctionalInterface
    public static interface Int3Consumer {
        public void accept(int x, int y, int z);
    }
    
    @FunctionalInterface
    public static interface Float3Test {
        public boolean apply(float x, float y, float z);
    }
    
    @FunctionalInterface
    public static interface Float3Consumer {
        public void accept(float x, float y, float z);
    }
}
