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

package grondag.xm.init;

import grondag.xm.api.primitive.SimplePrimitive;
import grondag.xm.api.terrain.TerrainPrimitive;
import grondag.xm.model.primitive.AxisCubePrimitive;
import grondag.xm.model.primitive.CSGTestPrimitive;
import grondag.xm.model.primitive.CubePrimitive;
import grondag.xm.model.primitive.SpherePrimitive;
import grondag.xm.model.primitive.SquareColumnPrimitive;
import grondag.xm.model.primitive.StackedPlatesPrimitive;
import grondag.xm.model.primitive.StairPrimitive;
import grondag.xm.model.primitive.WedgePrimitive;

public class XmPrimitives {
    public static final SimplePrimitive CUBE = new CubePrimitive("xm2:cube");
    public static final SimplePrimitive CUBE_AXIS = new AxisCubePrimitive("xm2:cube_axis");
    public static final SimplePrimitive COLUMN_SQUARE = new SquareColumnPrimitive("xm2:column_square");
    public static final SimplePrimitive STACKED_PLATES = new StackedPlatesPrimitive("xm2:stacked_plates");
    public static final SimplePrimitive WEDGE = new WedgePrimitive("xm2:wedge");
    public static final SimplePrimitive STAIR = new StairPrimitive("xm2:stair");
    public static final SimplePrimitive SPHERE = new SpherePrimitive("xm2:sphere");
    public static final SimplePrimitive CSGTEST = new CSGTestPrimitive("xm2:csgtest");

    public static final TerrainPrimitive TERRAIN_CUBE = null; // ModelShapes.create("terrain_height",
    public static final TerrainPrimitive TERRAIN_HEIGHT = null; // ModelShapes.create("terrain_height",
    // TerrainMeshFactory.class, SHAPE);
    public static final TerrainPrimitive TERRAIN_FILLER = null; // ModelShapes.create("terrain_filler",
    // TerrainMeshFactory.class, SHAPE);
}
