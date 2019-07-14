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

import grondag.xm2.mesh.CSGTestMeshFactory;
import grondag.xm2.mesh.CubeMeshFactory;
import grondag.xm2.mesh.ModelShape;
import grondag.xm2.mesh.ModelShapes;
import grondag.xm2.mesh.SphereMeshFactory;
import grondag.xm2.mesh.SquareColumnMeshFactory;
import grondag.xm2.mesh.StackedPlatesMeshFactory;
import grondag.xm2.mesh.StairMeshFactory;
import grondag.xm2.mesh.WedgeMeshFactory;

public class ModShapes {
    public static final ModelShape<?> CUBE = ModelShapes.create("cube", CubeMeshFactory.class);
    public static final ModelShape<?> COLUMN_SQUARE = ModelShapes.create("column_square", SquareColumnMeshFactory.class);
    public static final ModelShape<?> STACKED_PLATES = ModelShapes.create("stacked_plates", StackedPlatesMeshFactory.class);
    public static final ModelShape<?> WEDGE = ModelShapes.create("wedge", WedgeMeshFactory.class, true);
    public static final ModelShape<?> STAIR = ModelShapes.create("stair", StairMeshFactory.class, true);
    public static final ModelShape<?> SPHERE = ModelShapes.create("sphere", SphereMeshFactory.class, true);
    public static final ModelShape<?> CSGTEST = ModelShapes.create("csgtest", CSGTestMeshFactory.class, true);
    
    public static final ModelShape<?> TERRAIN_HEIGHT = null; //ModelShapes.create("terrain_height", TerrainMeshFactory.class, SHAPE, true);
    public static final ModelShape<?> TERRAIN_FILLER = null; //ModelShapes.create("terrain_filler", TerrainMeshFactory.class, SHAPE, false);
}
