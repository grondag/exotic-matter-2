package grondag.brocade.init;

import static grondag.exotic_matter.model.state.MetaUsage.SHAPE;
import static grondag.exotic_matter.model.state.MetaUsage.SPECIES;

import grondag.exotic_matter.model.mesh.CSGTestMeshFactory;
import grondag.exotic_matter.model.mesh.CubeMeshFactory;
import grondag.exotic_matter.model.mesh.ModelShape;
import grondag.exotic_matter.model.mesh.ModelShapes;
import grondag.exotic_matter.model.mesh.SphereMeshFactory;
import grondag.exotic_matter.model.mesh.SquareColumnMeshFactory;
import grondag.exotic_matter.model.mesh.StackedPlatesMeshFactory;
import grondag.exotic_matter.model.mesh.StairMeshFactory;
import grondag.exotic_matter.model.mesh.WedgeMeshFactory;
import grondag.exotic_matter.terrain.TerrainMeshFactory;

public class ModShapes {

    public static final ModelShape<?> CUBE = ModelShapes.create("cube", CubeMeshFactory.class, SPECIES);
    public static final ModelShape<?> COLUMN_SQUARE = ModelShapes.create("column_square", SquareColumnMeshFactory.class,
            SPECIES);
    public static final ModelShape<?> STACKED_PLATES = ModelShapes.create("stacked_plates",
            StackedPlatesMeshFactory.class, SHAPE);
    public static final ModelShape<?> TERRAIN_HEIGHT = ModelShapes.create("terrain_height", TerrainMeshFactory.class,
            SHAPE, true);
    public static final ModelShape<?> TERRAIN_FILLER = ModelShapes.create("terrain_filler", TerrainMeshFactory.class,
            SHAPE, false);
    public static final ModelShape<?> WEDGE = ModelShapes.create("wedge", WedgeMeshFactory.class, SPECIES, true);
    public static final ModelShape<?> STAIR = ModelShapes.create("stair", StairMeshFactory.class, SPECIES, true);
    public static final ModelShape<?> SPHERE = ModelShapes.create("sphere", SphereMeshFactory.class, SPECIES, true);
    public static final ModelShape<?> CSGTEST = ModelShapes.create("csgtest", CSGTestMeshFactory.class, SPECIES, true);
}
