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

package grondag.xm2.primitives.polygon;

import grondag.fermion.world.Rotation;
import grondag.xm2.painting.SurfaceTopology;
import grondag.xm2.primitives.QuadHelper;
import grondag.xm2.primitives.vertex.IVec3f;
import grondag.xm2.primitives.vertex.IVertexCollection;
import grondag.xm2.primitives.vertex.Vec3f;
import grondag.xm2.surface.api.XmSurface;
import net.fabricmc.fabric.api.renderer.v1.model.ModelHelper;
import net.minecraft.block.BlockRenderLayer;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;

public interface IPolygon extends IVertexCollection, IStreamPolygon// , IPipelinedQuad
{
    public Vec3f getFaceNormal();

    public default float getFaceNormalX() {
        return getFaceNormal().x();
    }

    public default float getFaceNormalY() {
        return getFaceNormal().y();
    }

    public default float getFaceNormalZ() {
        return getFaceNormal().z();
    }

    public default Box getAABB() {
        IVec3f p0 = getPos(0);
        IVec3f p1 = getPos(1);
        IVec3f p2 = getPos(2);
        IVec3f p3 = getPos(3);

        double minX = Math.min(Math.min(p0.x(), p1.x()), Math.min(p2.x(), p3.x()));
        double minY = Math.min(Math.min(p0.y(), p1.y()), Math.min(p2.y(), p3.y()));
        double minZ = Math.min(Math.min(p0.z(), p1.z()), Math.min(p2.z(), p3.z()));

        double maxX = Math.max(Math.max(p0.x(), p1.x()), Math.max(p2.x(), p3.x()));
        double maxY = Math.max(Math.max(p0.y(), p1.y()), Math.max(p2.y(), p3.y()));
        double maxZ = Math.max(Math.max(p0.z(), p1.z()), Math.max(p2.z(), p3.z()));

        return new Box(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public static final int VERTEX_NOT_FOUND = -1;

    /**
     * Will return {@link #VERTEX_NOT_FOUND} (-1) if vertex is not found in this
     * polygon.
     */
    public default int indexForVertex(Vec3f v) {
        final int limit = this.vertexCount();
        for (int i = 0; i < limit; i++) {
            if (v.equals(this.getPos(i)))
                return i;
        }
        return VERTEX_NOT_FOUND;
    }

    public default boolean isConvex() {
        return QuadHelper.isConvex(this);
    }

    public default boolean isOrthogonalTo(Direction face) {
        Vec3i dv = face.getVector();
        float dot = this.getFaceNormal().dotProduct(dv.getX(), dv.getY(), dv.getZ());
        return Math.abs(dot) <= QuadHelper.EPSILON;
    }

    public default boolean isOnSinglePlane() {
        if (this.vertexCount() == 3)
            return true;

        IVec3f fn = this.getFaceNormal();

        float faceX = fn.x();
        float faceY = fn.y();
        float faceZ = fn.z();

        IVec3f first = this.getPos(0);

        for (int i = 3; i < this.vertexCount(); i++) {
            IVec3f v = this.getPos(i);

            float dx = v.x() - first.x();
            float dy = v.y() - first.y();
            float dz = v.z() - first.z();

            if (Math.abs(faceX * dx + faceY * dy + faceZ * dz) > QuadHelper.EPSILON)
                return false;
        }

        return true;
    }

    public default boolean isOnFace(Direction face, float tolerance) {
        if (face == null)
            return false;
        for (int i = 0; i < this.vertexCount(); i++) {
            if (!getPos(i).isOnFacePlane(face, tolerance))
                return false;
        }
        return true;
    }

    public default Vec3f computeFaceNormal() {
        try {
            final IVec3f v0 = getPos(0);
            final IVec3f v1 = getPos(1);
            final IVec3f v2 = getPos(2);
            final IVec3f v3 = getPos(3);

            final float x0 = v2.x() - v0.x();
            final float y0 = v2.y() - v0.y();
            final float z0 = v2.z() - v0.z();

            final float x1 = v3.x() - v1.x();
            final float y1 = v3.y() - v1.y();
            final float z1 = v3.z() - v1.z();

            final float x = y0 * z1 - z0 * y1;
            final float y = z0 * x1 - x0 * z1;
            final float z = x0 * y1 - y0 * x1;

            float mag = MathHelper.sqrt(x * x + y * y + z * z);
            if (mag < 1.0E-4F)
                mag = 1f;

            return Vec3f.create(x / mag, y / mag, z / mag);
        } catch (Exception e) {
            assert false : "Bad polygon structure during face normal request.";
            return Vec3f.ZERO;
        }
    }

    // adapted from http://geomalgorithms.com/a01-_area.html
    // Copyright 2000 softSurfer, 2012 Dan Sunday
    // This code may be freely used and modified for any purpose
    // providing that this copyright notice is included with it.
    // iSurfer.org makes no warranty for this code, and cannot be held
    // liable for any real or imagined damage resulting from its use.
    // Users of this code must verify correctness for their application.
    public default float getArea() {
        float area = 0;
        float an, ax, ay, az; // abs value of normal and its coords
        int coord; // coord to ignore: 1=x, 2=y, 3=z
        int i, j, k; // loop indices
        final int n = this.vertexCount();
        Vec3f N = this.getFaceNormal();

        if (n < 3)
            return 0; // a degenerate polygon

        // select largest abs coordinate to ignore for projection
        ax = (N.x() > 0 ? N.x() : -N.x()); // abs x-coord
        ay = (N.y() > 0 ? N.y() : -N.y()); // abs y-coord
        az = (N.z() > 0 ? N.z() : -N.z()); // abs z-coord

        coord = 3; // ignore z-coord
        if (ax > ay) {
            if (ax > az)
                coord = 1; // ignore x-coord
        } else if (ay > az)
            coord = 2; // ignore y-coord

        // compute area of the 2D projection
        switch (coord) {
        case 1:
            for (i = 1, j = 2, k = 0; i < n; i++, j++, k++)
                area += (getPosModulo(i)).y() * (getPosModulo(j).z() - getPosModulo(k).z());
            break;
        case 2:
            for (i = 1, j = 2, k = 0; i < n; i++, j++, k++)
                area += (getPosModulo(i).z() * (getPosModulo(j).x() - getPosModulo(k).x()));
            break;
        case 3:
            for (i = 1, j = 2, k = 0; i < n; i++, j++, k++)
                area += (getPosModulo(i).x() * (getPosModulo(j).y() - getPosModulo(k).y()));
            break;
        }

        switch (coord) { // wrap-around term
        case 1:
            area += (getPosModulo(n).y() * (getPosModulo(1).z() - getPosModulo(n - 1).z()));
            break;
        case 2:
            area += (getPosModulo(n).z() * (getPosModulo(1).x() - getPosModulo(n - 1).x()));
            break;
        case 3:
            area += (getPosModulo(n).x() * (getPosModulo(1).y() - getPosModulo(n - 1).y()));
            break;
        }

        // scale to get area before projection
        an = MathHelper.sqrt(ax * ax + ay * ay + az * az); // length of normal vector
        switch (coord) {
        case 1:
            area *= (an / (2 * N.x()));
            break;
        case 2:
            area *= (an / (2 * N.y()));
            break;
        case 3:
            area *= (an / (2 * N.z()));
        }
        return area;
    }

    public XmSurface surface();

    /**
     * Returns computed face normal if no explicit normal assigned.
     */
    public Vec3f getVertexNormal(int vertexIndex);

    /**
     * Face to use for shading testing. Based on which way face points. Never null
     */
    public default Direction lightFace() {
        return QuadHelper.computeFaceForNormal(this.getFaceNormal());
    }

    Direction nominalFace();
    
    /**
     * Face to use for occlusion testing. Null if not fully on one of the faces.
     * Fudges a bit because painted quads can be slightly offset from the plane.
     */
    public default Direction cullFace() {
        Direction nominalFace = this.nominalFace();

        // semantic face will be right most of the time
        if (this.isOnFace(nominalFace, QuadHelper.EPSILON))
            return nominalFace;

        for (int i = 0; i < 6; i++) {
            final Direction f = ModelHelper.faceFromIndex(i);
            if (f != nominalFace && this.isOnFace(f, QuadHelper.EPSILON))
                return f;
        }
        return null;
    }

    float getMaxU(int layerIndex);

    float getMaxV(int layerIndex);

    float getMinU(int layerIndex);

    float getMinV(int layerIndex);

    /**
     * The maximum wrapping uv distance for either dimension on this surface.
     * <p>
     * 
     * Must be zero or positive. Setting to zero disable uvWrapping - painter will
     * use a 1:1 scale.
     * <p>
     * 
     * If the surface is painted with a texture larger than this distance, the
     * texture will be scaled down to fit in order to prevent visible seams. A scale
     * of 4, for example, would force a 32x32 texture to be rendered at 1/8 scale.
     * <p>
     * 
     * If the surface is painted with a texture smaller than this distance, then the
     * texture will be zoomed tiled to fill the surface.
     * <p>
     * 
     * Default is 0 and generally only comes into play for non-cubic surface
     * painters.
     * <p>
     * 
     * See also {@link SurfaceTopology#TILED}
     */
    float uvWrapDistance();
    
    int layerCount();

    String getTextureName(int layerIndex);

    boolean shouldContractUVs(int layerIndex);

    Rotation getRotation(int layerIndex);

    /**
     * Will return quad color if vertex color not set.
     */
    int spriteColor(int vertexIndex, int layerIndex);

    /**
     * Will return zero if vertex color not set.
     */
    int getVertexGlow(int vertexIndex);

    int getTextureSalt();

    boolean isLockUV(int layerIndex);

    public default boolean hasRenderLayer(BlockRenderLayer layer) {
        if (getRenderLayer(0) == layer)
            return true;

        final int count = this.layerCount();
        return (count > 1 && getRenderLayer(1) == layer) || (count == 3 && getRenderLayer(2) == layer);
    }

    BlockRenderLayer getRenderLayer(int layerIndex);

    boolean isEmissive(int layerIndex);

    // Use materials instead
//    int getPipelineIndex();
//    
//    @Override
//    default IRenderPipeline getPipeline()
//    {
//        return ClientProxy.acuityPipeline(getPipelineIndex());
//    }

    // TODO: convert to Fabric Renderer API
//    @Override
//    public default void produceVertices(IPipelinedVertexConsumer vertexLighter)
//    {
//        float[][][] uvData = AcuityHelper.getUVData(this);
//        int lastGlow = 0;
//        final int layerCount = layerCount();
//        
//        vertexLighter.setEmissive(0, isEmissive(0));
//        if(layerCount > 1)
//        {
//            vertexLighter.setEmissive(1, isEmissive(1));
//            if(layerCount == 3)
//                vertexLighter.setEmissive(2, isEmissive(2));
//        }
//        
//        for(int i = 0; i < 4; i++)
//        {
//            // passing layer 0 glow as an extra data point (for lava)
//            int currentGlow = this.getVertexGlow(i);
//            if(currentGlow != lastGlow)
//            {
//                final int g = currentGlow * 17;
//                
//                vertexLighter.setBlockLightMap(g, g, g, 255);
//                lastGlow = currentGlow;
//            }
//            
//            switch(layerCount)
//            {
//            case 1:
//                vertexLighter.acceptVertex(
//                        getVertexX(i), getVertexY(i), getVertexZ(i), 
//                        getVertexNormalX(i), getVertexNormalY(i), getVertexNormalZ(i),
//                        getVertexColor(0, i), uvData[0][i][0], uvData[0][i][1]);
//                break;
//                
//            case 2:
//                vertexLighter.acceptVertex(
//                        getVertexX(i), getVertexY(i), getVertexZ(i), 
//                        getVertexNormalX(i), getVertexNormalY(i), getVertexNormalZ(i),
//                        getVertexColor(0, i), uvData[0][i][0], uvData[0][i][1],
//                        getVertexColor(1, i), uvData[1][i][0], uvData[1][i][1]);
//                break;
//            
//            case 3:
//                vertexLighter.acceptVertex(
//                        getVertexX(i), getVertexY(i), getVertexZ(i), 
//                        getVertexNormalX(i), getVertexNormalY(i), getVertexNormalZ(i),
//                        getVertexColor(0, i), uvData[0][i][0], uvData[0][i][1],
//                        getVertexColor(1, i), uvData[1][i][0], uvData[1][i][1],
//                        getVertexColor(2, i), uvData[2][i][0], uvData[2][i][1]);
//                break;
//            
//            default:
//                throw new ArrayIndexOutOfBoundsException();
//            }
//        }
//    }

    /**
     * Should be called by when the original reference or another reference created
     * via {@link #retain()} is no longer held.
     * <p>
     * 
     * When retain count is 0 the object will be returned to its allocation pool if
     * it has one.
     */
    default void release() {

    }

    /**
     * Should be called instead of {@link #release()} when this is the last held
     * reference allocated by this objects factory. Will raise an assertion error
     * (if enabled) if this is not the last retained instance in the factory pool.
     * <p>
     * 
     * For use as a debugging aid - has no functional necessity otherwise. Also has
     * no effect/meaning for unpooled instances.
     */
    default void releaseLast() {
        release();
    }
    
    default int tag() {
        return NO_LINK_OR_TAG;
    }

    float x(int vertexIndex);

    float y(int vertexIndex);

    float z(int vertexIndex);

    float spriteU(int vertexIndex, int layerIndex);

    float spriteV(int vertexIndex, int layerIndex);

    boolean hasNormal(int vertexIndex);

    float normalX(int vertexIndex);

    float normalY(int vertexIndex);

    float normalZ(int vertexIndex);
}
