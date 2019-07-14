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

import grondag.xm2.api.model.ModelPrimitive;
import grondag.xm2.model.primitive.CSGTestPrimitive;
import grondag.xm2.model.primitive.CubePrimitive;
import grondag.xm2.model.primitive.SpherePrimitive;
import grondag.xm2.model.primitive.SquareColumnPrimitive;
import grondag.xm2.model.primitive.StackedPlatesPrimitive;
import grondag.xm2.model.primitive.StairPrimitive;
import grondag.xm2.model.primitive.WedgePrimitive;

public class XmPrimitives {
    public static final ModelPrimitive CUBE = new CubePrimitive("xm2:cube");
    public static final ModelPrimitive COLUMN_SQUARE = new SquareColumnPrimitive("xm2:column_square");
    public static final ModelPrimitive STACKED_PLATES = new StackedPlatesPrimitive("xm2:stacked_plates");
    public static final ModelPrimitive WEDGE = new WedgePrimitive("xm2:wedge");
    public static final ModelPrimitive STAIR = new StairPrimitive("xm2:stair");
    public static final ModelPrimitive SPHERE = new SpherePrimitive("xm2:sphere");
    public static final ModelPrimitive CSGTEST = new CSGTestPrimitive("xm2:csgtest");

    public static final ModelPrimitive TERRAIN_HEIGHT = null; // ModelShapes.create("terrain_height",
    // TerrainMeshFactory.class, SHAPE);
    public static final ModelPrimitive TERRAIN_FILLER = null; // ModelShapes.create("terrain_filler",
    // TerrainMeshFactory.class, SHAPE);
}
