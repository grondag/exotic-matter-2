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

package grondag.xm2.init;

import grondag.xm2.model.primitive.CSGTestPrimitive;
import grondag.xm2.model.primitive.CubePrimitive;
import grondag.xm2.model.primitive.SpherePrimitive;
import grondag.xm2.model.primitive.SquareColumnPrimitive;
import grondag.xm2.model.primitive.StackedPlatesPrimitive;
import grondag.xm2.model.primitive.StairPrimitive;
import grondag.xm2.model.primitive.WedgePrimitive;
import grondag.xm2.model.registry.ModelShape;
import grondag.xm2.model.registry.ModelShapes;

public class ModShapes {
    public static final ModelShape<?> CUBE = ModelShapes.create("cube", CubePrimitive.class);
    public static final ModelShape<?> COLUMN_SQUARE = ModelShapes.create("column_square", SquareColumnPrimitive.class);
    public static final ModelShape<?> STACKED_PLATES = ModelShapes.create("stacked_plates", StackedPlatesPrimitive.class);
    public static final ModelShape<?> WEDGE = ModelShapes.create("wedge", WedgePrimitive.class);
    public static final ModelShape<?> STAIR = ModelShapes.create("stair", StairPrimitive.class);
    public static final ModelShape<?> SPHERE = ModelShapes.create("sphere", SpherePrimitive.class);
    public static final ModelShape<?> CSGTEST = ModelShapes.create("csgtest", CSGTestPrimitive.class);
    
    public static final ModelShape<?> TERRAIN_HEIGHT = null; //ModelShapes.create("terrain_height", TerrainMeshFactory.class, SHAPE);
    public static final ModelShape<?> TERRAIN_FILLER = null; //ModelShapes.create("terrain_filler", TerrainMeshFactory.class, SHAPE);
}
