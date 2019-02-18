package grondag.brocade.primitives;

import java.util.List;

import javax.vecmath.AxisAngle4d;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector4f;

import com.google.common.collect.ImmutableList;

import grondag.exotic_matter.model.primitives.vertex.IVec3f;
import grondag.exotic_matter.model.primitives.vertex.IVertexCollection;
import grondag.exotic_matter.model.primitives.vertex.Vec3Function;
import grondag.exotic_matter.model.primitives.vertex.Vec3f;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3i;

public class QuadHelper
{
    public static final float EPSILON = 1.0E-5F;

    public static final List<BakedQuad> EMPTY_QUAD_LIST = new ImmutableList.Builder<BakedQuad>().build();

    @Deprecated
    public static boolean epsilonEquals(double first, double second)
    {
        return Math.abs(first - second) < EPSILON;
    }
    
    public static boolean epsilonEquals(float first, float second)
    {
        return Math.abs(first - second) < EPSILON;
    }
    
    public static float[] colorComponentsARGB(int colorARGB)
    {
        float[] result = new float[4];
        result[0] = (float)((colorARGB >> 24) & 0xFF) / 0xFF;
        result[1] = (float)((colorARGB >> 16) & 0xFF) / 0xFF;
        result[2] = (float)((colorARGB >> 8) & 0xFF) / 0xFF;
        result[3] = (float)(colorARGB & 0xFF) / 0xFF;
        return result;
    }
    
    public static int shadeColor(int color, float shade, boolean glOrder)
    {
        int red = (int) (shade * 255f * ((color >> 16 & 0xFF) / 255f));
        int green = (int) (shade * 255f * ((color >> 8 & 0xFF) / 255f));
        int blue = (int) (shade * 255f * ((color & 0xFF) / 255f));
        int alpha = color >> 24 & 0xFF;

        return glOrder ? red  | green << 8 | blue << 16 | alpha << 24 : red << 16 | green << 8 | blue | alpha << 24;
    }
    
    public static EnumFacing computeFaceForNormal(final float x, final float y, final float z)
    {
        EnumFacing result = null;
        
        double minDiff = 0.0F;
    
        for(int i = 0; i < 6; i++)
        {
            final EnumFacing f = EnumFacing.VALUES[i];
            Vec3i faceNormal = f.getDirectionVec();
            float diff = Vec3Function.dotProduct(faceNormal.getX(), faceNormal.getY(), faceNormal.getZ(), x, y, z);
    
            if (diff >= 0.0 && diff > minDiff)
            {
                minDiff = diff;
                result = f;
            }
        }
    
        if (result == null)
        {
            return EnumFacing.UP;
        }
        else
        {
            return result;
        }
    }

    public static EnumFacing computeFaceForNormal(Vec3f normal)
    {
        return computeFaceForNormal(normal.x(), normal.y(), normal.z());
    }
    
    public static EnumFacing computeFaceForNormal(Vector4f normal)
    {
        return computeFaceForNormal(normal.x, normal.y, normal.z);
    }

    /** returns the face that is normally the "top" of the given face */
    public static EnumFacing defaultTopOf(EnumFacing faceIn)
    {
        switch(faceIn)
        {
        case UP:
            return EnumFacing.NORTH;
        case DOWN:
            return EnumFacing.SOUTH;
        default:
            return EnumFacing.UP;
        }
    }

    public static EnumFacing bottomOf(EnumFacing faceIn, EnumFacing topFace)
       {
           return topFace.getOpposite();
       }

    public static EnumFacing getAxisTop(EnumFacing.Axis axis)
       {
           switch(axis)
           {
           case Y: 
               return EnumFacing.UP;
           case X:
               return EnumFacing.EAST;
           default:
               return EnumFacing.NORTH;
           }
       }

    public static EnumFacing leftOf(EnumFacing faceIn, EnumFacing topFace)
       {
           return QuadHelper.rightOf(faceIn, topFace).getOpposite();
       }

    public static EnumFacing rightOf(EnumFacing faceIn, EnumFacing topFace)
       {
           switch (faceIn)
           {
               case NORTH:
                   switch (topFace)
                   {
                       case UP:
                           return EnumFacing.WEST;
                       case EAST:
                           return EnumFacing.UP;
                       case DOWN:
                           return EnumFacing.EAST;
                       case WEST:
                       default:
                           return EnumFacing.DOWN;
                   }
               case SOUTH:
                   switch (topFace)
                   {
                       case UP:
                           return EnumFacing.EAST;
                       case EAST:
                           return EnumFacing.DOWN;
                       case DOWN:
                           return EnumFacing.WEST;
                       case WEST:
                       default:
                           return EnumFacing.UP;
                   }
               case EAST:
                   switch (topFace)
                   {
                       case UP:
                           return EnumFacing.NORTH;
                       case NORTH:
                           return EnumFacing.DOWN;
                       case DOWN:
                           return EnumFacing.SOUTH;
                       case SOUTH:
                       default:
                           return EnumFacing.UP;
                   }
               case WEST:
                   switch (topFace)
                   {
                       case UP:
                           return EnumFacing.SOUTH;
                       case NORTH:
                           return EnumFacing.UP;
                       case DOWN:
                           return EnumFacing.NORTH;
                       case SOUTH:
                       default:
                           return EnumFacing.DOWN;
                   }
               case UP:
                   switch (topFace)
                   {
                       case NORTH:
                           return EnumFacing.EAST;
                       case EAST:
                           return EnumFacing.SOUTH;
                       case SOUTH:
                           return EnumFacing.WEST;
                       case WEST:
                       default:
                           return EnumFacing.NORTH;
                   }
               case DOWN:
               default:
                   switch (topFace)
                   {
                       case NORTH:
                           return EnumFacing.WEST;
                       case EAST:
                           return EnumFacing.NORTH;
                       case SOUTH:
                           return EnumFacing.EAST;
                       case WEST:
                       default:
                           return EnumFacing.SOUTH;
                   }
           }
       }

    /**
        * Builds the appropriate quaternion to rotate around the given orthogonalAxis.
        */
       public static Quat4f rotationForAxis(EnumFacing.Axis axis, double degrees)
       {
       	Quat4f retVal = new Quat4f();
       	switch (axis) {
       	case X:
       		retVal.set(new AxisAngle4d(1, 0, 0, Math.toRadians(degrees)));
       		break;
       	case Y:
       		retVal.set(new AxisAngle4d(0, 1, 0, Math.toRadians(degrees)));
       		break;
       	case Z:
       		retVal.set(new AxisAngle4d(0, 0, 1, Math.toRadians(degrees)));
       		break;
       	}
       	return retVal;
       }

    public static boolean isConvex(IVertexCollection vertices)
    {
        final int vertexCount = vertices.vertexCount();
        if(vertexCount == 3) return true;
    
        float testX = 0;
        float testY = 0;
        float testZ = 0;
        boolean needTest = true;
        
        IVec3f priorVertex = vertices.getPos(vertexCount - 2);
        IVec3f thisVertex =  vertices.getPos(vertexCount - 1);
        
        for(int nextIndex = 0; nextIndex < vertexCount; nextIndex++)
        {
            IVec3f nextVertex = vertices.getPos(nextIndex);
            
            final float ax = thisVertex.x() - priorVertex.x();
            final float ay = thisVertex.y() - priorVertex.y();
            final float az = thisVertex.z() - priorVertex.z();
            
            final float bx = nextVertex.x() - thisVertex.x();
            final float by = nextVertex.y() - thisVertex.y();
            final float bz = nextVertex.z() - thisVertex.z();
    
            final float crossX = ay * bz - az * by;
            final float crossY = az * bx - ax * bz;
            final float crossZ = ax * by - ay * bx;
            
            if(needTest)
            {
                needTest = false;
                testX = crossX;
                testY = crossY;
                testZ = crossZ;
            }
            else if(testX * crossX  + testY * crossY + testZ * crossZ < 0) 
            {
                return false;
            }
            
            priorVertex = thisVertex;
            thisVertex =  nextVertex;
        }
        return true;
    }
}