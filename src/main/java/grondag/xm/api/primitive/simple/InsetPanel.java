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
package grondag.xm.api.primitive.simple;

import static grondag.xm.api.connect.state.CornerJoinFaceStates.ALL_BL;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.ALL_BR;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.ALL_NO_CORNERS;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.ALL_TL;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.ALL_TL_BL;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.ALL_TL_BL_BR;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.ALL_TL_BR;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.ALL_TL_TR;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.ALL_TL_TR_BL;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.ALL_TL_TR_BL_BR;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.ALL_TL_TR_BR;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.ALL_TR;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.ALL_TR_BL;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.ALL_TR_BL_BR;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.ALL_TR_BR;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.ALL_BL_BR;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.BOTTOM;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.BOTTOM_LEFT_BL;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.BOTTOM_LEFT_NO_CORNER;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.BOTTOM_LEFT_RIGHT_BL;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.BOTTOM_LEFT_RIGHT_BL_BR;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.BOTTOM_LEFT_RIGHT_BR;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.BOTTOM_LEFT_RIGHT_NO_CORNERS;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.BOTTOM_RIGHT_BR;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.BOTTOM_RIGHT_NO_CORNER;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.COUNT;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.LEFT;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.LEFT_RIGHT;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.NONE;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.RIGHT;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.TOP;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.TOP_BOTTOM;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.TOP_BOTTOM_LEFT_BL;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.TOP_BOTTOM_LEFT_NO_CORNERS;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.TOP_BOTTOM_LEFT_TL;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.TOP_BOTTOM_LEFT_TL_BL;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.TOP_BOTTOM_RIGHT_BR;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.TOP_BOTTOM_RIGHT_NO_CORNERS;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.TOP_BOTTOM_RIGHT_TR;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.TOP_BOTTOM_RIGHT_TR_BR;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.TOP_LEFT_NO_CORNER;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.TOP_LEFT_RIGHT_NO_CORNERS;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.TOP_LEFT_RIGHT_TL;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.TOP_LEFT_RIGHT_TL_TR;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.TOP_LEFT_RIGHT_TR;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.TOP_LEFT_TL;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.TOP_RIGHT_NO_CORNER;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.TOP_RIGHT_TR;
import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.util.function.Function;

import org.apiguardian.api.API;

import grondag.fermion.color.Color;
import grondag.xm.Xm;
import grondag.xm.api.connect.state.CornerJoinFaceState;
import grondag.xm.api.connect.state.CornerJoinState;
import grondag.xm.api.connect.state.SimpleJoinState;
import grondag.xm.api.mesh.CsgMeshBuilder;
import grondag.xm.api.mesh.WritableMesh;
import grondag.xm.api.mesh.XmMesh;
import grondag.xm.api.mesh.polygon.FaceVertex;
import grondag.xm.api.mesh.polygon.MutablePolygon;
import grondag.xm.api.mesh.polygon.PolyHelper;
import grondag.xm.api.modelstate.primitive.PrimitiveState;
import grondag.xm.api.orientation.OrientationType;
import grondag.xm.api.paint.SurfaceTopology;
import grondag.xm.api.primitive.SimplePrimitive;
import grondag.xm.api.primitive.surface.XmSurface;
import grondag.xm.api.primitive.surface.XmSurfaceList;
import grondag.xm.api.texture.TextureOrientation;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;

@API(status = EXPERIMENTAL)
public class InsetPanel {
    private InsetPanel() {}
    
    private static final float DEPTH = 2f / 16f;
    private static final float INV_DEPTH = 1 -  DEPTH;
    private static final float NEAR = DEPTH * 2;
    private static final float FAR = 1 - NEAR;
    
    public static final XmSurfaceList SURFACES = XmSurfaceList.builder()
            .add("outer", SurfaceTopology.CUBIC, XmSurface.FLAG_ALLOW_BORDERS)
            .add("cut", SurfaceTopology.CUBIC, XmSurface.FLAG_LAMP_GRADIENT)
            .add("inner", SurfaceTopology.CUBIC, XmSurface.FLAG_ALLOW_BORDERS | XmSurface.FLAG_LAMP)
            .build();

    public static final XmSurface SURFACE_OUTER = SURFACES.get(0);
    public static final XmSurface SURFACE_CUT = SURFACES.get(1);
    public static final XmSurface SURFACE_INNER = SURFACES.get(2);

    private static final float[][] SPECS = new float[COUNT][];
    
    private static void spec(CornerJoinFaceState state, float... values) {
        SPECS[state.ordinal()] = values;
    }
    
    static {
        spec(ALL_NO_CORNERS, 0, 0, 1, 1);
        spec(NONE, NEAR, NEAR, FAR, FAR);
        spec(TOP, NEAR, NEAR, FAR, 1);
        spec(BOTTOM, NEAR, 0, FAR, FAR);
        spec(LEFT, 0, NEAR, FAR, FAR);
        spec(RIGHT, NEAR, NEAR, 1, FAR);
        spec(TOP_BOTTOM, NEAR, 0, FAR, 1);
        spec(LEFT_RIGHT, 0, NEAR, 1, FAR);
        spec(ALL_TL_TR_BL_BR, NEAR, 0, FAR, 1, 0, NEAR, 1, FAR);
        spec(BOTTOM_LEFT_NO_CORNER, 0, 0, FAR, FAR);
        spec(TOP_LEFT_NO_CORNER, 0, NEAR, FAR, 1);
        spec(TOP_RIGHT_NO_CORNER, NEAR, NEAR, 1, 1);
        spec(BOTTOM_RIGHT_NO_CORNER, NEAR, 0, 1, FAR);
        spec(BOTTOM_LEFT_RIGHT_NO_CORNERS, 0, 0, 1, FAR);
        spec(TOP_BOTTOM_LEFT_NO_CORNERS, 0, 0, FAR, 1);
        spec(TOP_LEFT_RIGHT_NO_CORNERS, 0, NEAR, 1, 1);
        spec(TOP_BOTTOM_RIGHT_NO_CORNERS, NEAR, 0, 1, 1);
        spec(ALL_TR, 0, 0, FAR, 1, 0, 0, 1, FAR);
        spec(ALL_BR, 0, 0, FAR, 1, 0, NEAR, 1, 1);
        spec(ALL_BL, NEAR, 0, 1, 1, 0, NEAR, 1, 1);
        spec(ALL_TL, NEAR, 0, 1, 1, 0, 0, 1, FAR);
        spec(ALL_TL_TR, NEAR, 0, FAR, 1, 0, 0, 1, FAR);
        spec(ALL_TR_BR, 0, 0, FAR, 1, 0, NEAR, 1, FAR);
        spec(ALL_TL_BL, NEAR, 0, 1, 1, 0, NEAR, 1, FAR);
        spec(ALL_TR_BL, NEAR, 0, 1, FAR, 0, NEAR, FAR, 1);
        spec(ALL_TL_BR, 0, 0, FAR, FAR, NEAR, NEAR, 1, 1);
        spec(ALL_BL_BR, 0, NEAR, 1, 1, NEAR, 0, FAR, 1);
        spec(ALL_TR_BL_BR, NEAR, 0, FAR, 1, 0, NEAR, 1, FAR, 0, FAR, NEAR, 1);
        spec(ALL_TL_BL_BR, NEAR, 0, FAR, 1, 0, NEAR, 1, FAR, FAR, FAR, 1, 1);
        spec(ALL_TL_TR_BL, NEAR, 0, FAR, 1, 0, NEAR, 1, FAR, FAR, 0, 1, NEAR);
        spec(ALL_TL_TR_BR, NEAR, 0, FAR, 1, 0, NEAR, 1, FAR, 0, 0, NEAR, NEAR);
        spec(BOTTOM_LEFT_RIGHT_BR, 0, NEAR, 1, FAR, 0, 0, FAR, NEAR);
        spec(BOTTOM_LEFT_RIGHT_BL, 0, NEAR, 1, FAR, NEAR, 0, 1, NEAR);
        spec(TOP_LEFT_RIGHT_TL, 0, NEAR, 1, FAR, NEAR, FAR, 1, 1);
        spec(TOP_LEFT_RIGHT_TR, 0, NEAR, 1, FAR, 0, FAR, FAR, 1);
        spec(TOP_BOTTOM_LEFT_BL, NEAR, 0, FAR, 1, 0, NEAR, NEAR, 1);
        spec(TOP_BOTTOM_LEFT_TL, NEAR, 0, FAR, 1, 0, 0, NEAR, FAR);
        spec(TOP_BOTTOM_RIGHT_TR, NEAR, 0, FAR, 1, FAR, 0, 1, FAR);
        spec(TOP_BOTTOM_RIGHT_BR, NEAR, 0, FAR, 1, FAR, NEAR, 1, 1);
        spec(BOTTOM_LEFT_BL, NEAR, 0, FAR, FAR, 0, NEAR, NEAR, FAR);
        spec(TOP_LEFT_TL, NEAR, NEAR, FAR, 1, 0, NEAR, NEAR, FAR);
        spec(TOP_RIGHT_TR, NEAR, NEAR, FAR, 1, FAR, NEAR, 1, FAR);
        spec(BOTTOM_RIGHT_BR, NEAR, 0, FAR, FAR, FAR, NEAR, 1, FAR);
        spec(BOTTOM_LEFT_RIGHT_BL_BR, NEAR, 0, FAR, NEAR, 0, NEAR, 1, FAR);
        spec(TOP_BOTTOM_LEFT_TL_BL, NEAR, 0, FAR, 1, 0, NEAR, NEAR, FAR);
        spec(TOP_LEFT_RIGHT_TL_TR, 0, NEAR, 1, FAR, NEAR, FAR, FAR, 1);
        spec(TOP_BOTTOM_RIGHT_TR_BR, NEAR, 0, FAR, 1, FAR, NEAR, 1, FAR);
    }
    
    static final Function<PrimitiveState, XmMesh> POLY_FACTORY = modelState -> {
        final CornerJoinState joins = modelState.cornerJoin();

        if(joins.simpleJoin() == SimpleJoinState.ALL_JOINS) {
            return XmMesh.EMPTY;
        }
        
        final CsgMeshBuilder csg = CsgMeshBuilder.threadLocal();
        emitOuter(csg.input(), joins);
        
        csg.union();
        
        final boolean isLit = modelState.primitive().lampSurface(modelState) != null;
        
        for (int i = 0; i < 6; i++) {
            final Direction face = Direction.byId(i);
            cutSide(face, csg, joins.faceState(face), isLit);
        }
        
        return csg.build();

    };
    
    private static void cutSide(Direction face, CsgMeshBuilder csg, CornerJoinFaceState faceJoin, boolean isLit) {
        
        float[] spec = SPECS[faceJoin.ordinal()];
        
        if(spec == null) return;
        
        Direction top = PolyHelper.defaultTopOf(face);
        final Direction opposite = face.getOpposite();

        final int limit = spec.length / 4;
        for(int i = 0; i < limit; i++) {
            WritableMesh mesh = csg.input();
            MutablePolygon writer = mesh.writer();
            
            writer.colorAll(0, 0xFFFFFFFF);
            writer.lockUV(0, true);
            writer.rotation(0, TextureOrientation.IDENTITY);
            writer.sprite(0, "");
            writer.saveDefaults();
            
            final int index = i * 4;
            final float x0 = spec[index];
            final float y0 = spec[index + 1];
            final float x1 = spec[index + 2];
            final float y1 = spec[index + 3];
            
            writer.surface(SURFACE_INNER);
            writer.setupFaceQuad(opposite, 1 - x1, y0, 1 - x0, y1, INV_DEPTH, top);
            writer.append();
            
            writer.surface(SURFACE_OUTER);
            writer.setupFaceQuad(face, x0, y0, x1, y1, 0, top);
            writer.append();
            
            setupCutSideQuad(writer, x0, INV_DEPTH, x1, 1, y0, PolyHelper.bottomOf(face, top), face, isLit);
            setupCutSideQuad(writer, 1 - x1, INV_DEPTH, 1 - x0, 1, 1 - y1, top, face, isLit);
            
            setupCutSideQuad(writer, 1 - y1, INV_DEPTH, 1 - y0, 1, x0, PolyHelper.leftOf(face, top), face, isLit);
            setupCutSideQuad(writer, y0, INV_DEPTH, y1, 1, 1 - x1, PolyHelper.rightOf(face, top), face, isLit);
            csg.difference();
        }
    }
    
    private static void setupCutSideQuad(MutablePolygon poly, float x0, float y0, float x1, float y1, float depth, Direction face, Direction topFace, boolean isLit) {
        final int glow = isLit ? 255 : 0;
        
        poly.surface(SURFACE_CUT);
        
        poly.setupFaceQuad(face, 
                new FaceVertex.Colored(x0, y0, depth, Color.WHITE, glow),
                new FaceVertex.Colored(x1, y0, depth, Color.WHITE, glow),
                new FaceVertex.Colored(x1, y1, depth, Color.WHITE, glow / 3),
                new FaceVertex.Colored(x0, y1, depth, Color.WHITE, glow / 3),
                topFace);
        
        // force vertex normals out to prevent lighting anomalies
        final Vec3i vec = face.getVector();
        final float x = vec.getX();
        final float y = vec.getY();
        final float z = vec.getZ();
        for(int i = 0; i < 4; i++) {
            poly.normal(i, x, y, z);
        }
        
        poly.append();
    }
    
    private static final void emitOuter(WritableMesh mesh, CornerJoinState joins) {
        final MutablePolygon writer = mesh.writer();
        
        writer.colorAll(0, 0xFFFFFFFF);
        writer.lockUV(0, true);
        writer.rotation(0, TextureOrientation.IDENTITY);
        writer.sprite(0, "");
        writer.saveDefaults();

        final SimpleJoinState j = joins.simpleJoin();
        
        if (!j.isJoined(Direction.DOWN)) {
            writer.surface(SURFACE_OUTER);
            writer.setupFaceQuad(Direction.DOWN, 0, 0, 1, 1, 0, Direction.NORTH);
            writer.append();
        }
           
        if (!j.isJoined(Direction.UP)) {
            writer.surface(SURFACE_OUTER);
            writer.setupFaceQuad(Direction.UP, 0, 0, 1, 1, 0, Direction.NORTH);
            writer.append();
        }
        
        if (!j.isJoined(Direction.EAST)) {
            writer.surface(SURFACE_OUTER);
            writer.setupFaceQuad(Direction.EAST, 0, 0, 1, 1, 0, Direction.UP);
            writer.append();
        }
        
        if (!j.isJoined(Direction.WEST)) {
            writer.surface(SURFACE_OUTER);
            writer.setupFaceQuad(Direction.WEST, 0, 0, 1, 1, 0, Direction.UP);
            writer.append();
        }
        
        if (!j.isJoined(Direction.NORTH)) {
            writer.surface(SURFACE_OUTER);
            writer.setupFaceQuad(Direction.NORTH, 0, 0, 1, 1, 0, Direction.UP);
            writer.append();
        }
        
        if (!j.isJoined(Direction.SOUTH)) {
            writer.surface(SURFACE_OUTER);
            writer.setupFaceQuad(Direction.SOUTH, 0, 0, 1, 1, 0, Direction.UP);
            writer.append();
        }
    }
    
    public static final SimplePrimitive INSTANCE = SimplePrimitive.builder()
            .surfaceList(SURFACES)
            .cornerJoin(true)
            .polyFactory(POLY_FACTORY)
            .orientationType(OrientationType.NONE)
            .build(Xm.idString("inset_panel"));
}