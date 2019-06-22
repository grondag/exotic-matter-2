package grondag.brocade.init;

import static grondag.brocade.model.state.MetaUsage.SHAPE;
import static grondag.brocade.model.state.MetaUsage.SPECIES;

import grondag.brocade.mesh.CSGTestMeshFactory;
import grondag.brocade.mesh.CubeMeshFactory;
import grondag.brocade.mesh.ModelShape;
import grondag.brocade.mesh.ModelShapes;
import grondag.brocade.mesh.SphereMeshFactory;
import grondag.brocade.mesh.SquareColumnMeshFactory;
import grondag.brocade.mesh.StackedPlatesMeshFactory;
import grondag.brocade.mesh.StairMeshFactory;
import grondag.brocade.mesh.WedgeMeshFactory;

public class ModShapes {
    public static final ModelShape<?> CUBE = ModelShapes.create("cube", CubeMeshFactory.class, SPECIES);
    public static final ModelShape<?> COLUMN_SQUARE = ModelShapes.create("column_square", SquareColumnMeshFactory.class, SPECIES);
    public static final ModelShape<?> STACKED_PLATES = ModelShapes.create("stacked_plates", StackedPlatesMeshFactory.class, SHAPE);
    public static final ModelShape<?> WEDGE = ModelShapes.create("wedge", WedgeMeshFactory.class, SPECIES, true);
    public static final ModelShape<?> STAIR = ModelShapes.create("stair", StairMeshFactory.class, SPECIES, true);
    public static final ModelShape<?> SPHERE = ModelShapes.create("sphere", SphereMeshFactory.class, SPECIES, true);
    public static final ModelShape<?> CSGTEST = ModelShapes.create("csgtest", CSGTestMeshFactory.class, SPECIES, true);
    
    public static final ModelShape<?> TERRAIN_HEIGHT = null; //ModelShapes.create("terrain_height", TerrainMeshFactory.class, SHAPE, true);
    public static final ModelShape<?> TERRAIN_FILLER = null; //ModelShapes.create("terrain_filler", TerrainMeshFactory.class, SHAPE, false);
}
