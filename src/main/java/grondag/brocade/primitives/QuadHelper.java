package grondag.brocade.primitives;

import java.util.List;

import com.google.common.collect.ImmutableList;

import grondag.brocade.primitives.vertex.IVec3f;
import grondag.brocade.primitives.vertex.IVertexCollection;
import grondag.brocade.primitives.vertex.Vec3Function;
import grondag.brocade.primitives.vertex.Vec3f;
import net.fabricmc.fabric.api.client.model.fabric.ModelHelper;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.util.math.Quaternion;
import net.minecraft.client.util.math.Vector4f;
import net.minecraft.util.math.Direction;
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
    
    public static Direction computeFaceForNormal(final float x, final float y, final float z)
    {
        Direction result = null;
        
        double minDiff = 0.0F;
    
        for(int i = 0; i < 6; i++)
        {
            final Direction f = ModelHelper.faceFromIndex(i);
            Vec3i faceNormal = f.getVector();
            float diff = Vec3Function.dotProduct(faceNormal.getX(), faceNormal.getY(), faceNormal.getZ(), x, y, z);
    
            if (diff >= 0.0 && diff > minDiff)
            {
                minDiff = diff;
                result = f;
            }
        }
    
        if (result == null)
        {
            return Direction.UP;
        }
        else
        {
            return result;
        }
    }

    public static Direction computeFaceForNormal(Vec3f normal)
    {
        return computeFaceForNormal(normal.x(), normal.y(), normal.z());
    }
    
    public static Direction computeFaceForNormal(Vector4f normal)
    {
        return computeFaceForNormal(normal.x(), normal.y(), normal.z());
    }

    /** returns the face that is normally the "top" of the given face */
    public static Direction defaultTopOf(Direction faceIn)
    {
        switch(faceIn)
        {
        case UP:
            return Direction.NORTH;
        case DOWN:
            return Direction.SOUTH;
        default:
            return Direction.UP;
        }
    }

    public static Direction bottomOf(Direction faceIn, Direction topFace)
       {
           return topFace.getOpposite();
       }

    public static Direction getAxisTop(Direction.Axis axis)
       {
           switch(axis)
           {
           case Y: 
               return Direction.UP;
           case X:
               return Direction.EAST;
           default:
               return Direction.NORTH;
           }
       }

    public static Direction leftOf(Direction faceIn, Direction topFace)
       {
           return QuadHelper.rightOf(faceIn, topFace).getOpposite();
       }

    public static Direction rightOf(Direction faceIn, Direction topFace)
       {
           switch (faceIn)
           {
               case NORTH:
                   switch (topFace)
                   {
                       case UP:
                           return Direction.WEST;
                       case EAST:
                           return Direction.UP;
                       case DOWN:
                           return Direction.EAST;
                       case WEST:
                       default:
                           return Direction.DOWN;
                   }
               case SOUTH:
                   switch (topFace)
                   {
                       case UP:
                           return Direction.EAST;
                       case EAST:
                           return Direction.DOWN;
                       case DOWN:
                           return Direction.WEST;
                       case WEST:
                       default:
                           return Direction.UP;
                   }
               case EAST:
                   switch (topFace)
                   {
                       case UP:
                           return Direction.NORTH;
                       case NORTH:
                           return Direction.DOWN;
                       case DOWN:
                           return Direction.SOUTH;
                       case SOUTH:
                       default:
                           return Direction.UP;
                   }
               case WEST:
                   switch (topFace)
                   {
                       case UP:
                           return Direction.SOUTH;
                       case NORTH:
                           return Direction.UP;
                       case DOWN:
                           return Direction.NORTH;
                       case SOUTH:
                       default:
                           return Direction.DOWN;
                   }
               case UP:
                   switch (topFace)
                   {
                       case NORTH:
                           return Direction.EAST;
                       case EAST:
                           return Direction.SOUTH;
                       case SOUTH:
                           return Direction.WEST;
                       case WEST:
                       default:
                           return Direction.NORTH;
                   }
               case DOWN:
               default:
                   switch (topFace)
                   {
                       case NORTH:
                           return Direction.WEST;
                       case EAST:
                           return Direction.NORTH;
                       case SOUTH:
                           return Direction.EAST;
                       case WEST:
                       default:
                           return Direction.SOUTH;
                   }
           }
       }

    /**
        * Builds the appropriate quaternion to rotate around the given orthogonalAxis.
        */
       public static Quaternion rotationForAxis(Direction.Axis axis, double degrees)
       {
           Quaternion retVal = new Quaternion();
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