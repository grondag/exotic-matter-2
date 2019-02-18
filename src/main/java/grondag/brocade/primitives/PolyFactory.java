package grondag.brocade.primitives;

import grondag.exotic_matter.model.primitives.polygon.AbstractPolygon;
import grondag.exotic_matter.model.primitives.polygon.IMutablePolygon;
import grondag.exotic_matter.model.primitives.polygon.IPolygon;
import grondag.exotic_matter.model.primitives.polygon.IPrimitiveFactory;
import grondag.exotic_matter.model.primitives.polygon.MutablePolygon3x3;
import grondag.exotic_matter.model.primitives.polygon.MutablePolygon3x4;
import grondag.exotic_matter.model.primitives.polygon.MutablePolygonNxN;
import grondag.exotic_matter.model.primitives.polygon.Polygon1x3;
import grondag.exotic_matter.model.primitives.polygon.Polygon1x4;
import grondag.exotic_matter.model.primitives.polygon.Polygon1xN;
import grondag.exotic_matter.model.primitives.polygon.Polygon2x3;
import grondag.exotic_matter.model.primitives.polygon.Polygon2x4;
import grondag.exotic_matter.model.primitives.polygon.Polygon2xN;
import grondag.exotic_matter.model.primitives.polygon.Polygon3x3;
import grondag.exotic_matter.model.primitives.polygon.Polygon3x4;
import grondag.exotic_matter.model.primitives.polygon.Polygon3xN;
import grondag.exotic_matter.model.primitives.vertex.IMutableVertex;
import grondag.exotic_matter.model.primitives.vertex.UnpackedVertex3;
import net.minecraft.util.math.MathHelper;

public class PolyFactory
{
    /**
     * Instances in the common pool are stand-alone instances and do not
     * attempt to ensure locality of reference.  There is no allocation 
     * management (they simply add themselves back to the pool when released.)<p>
     * 
     * They have no overhead/waste of partial allocation but may carry a little
     * more overhead due to having own array(s) or other internal state
     * depending on implementation provided.
     * 
     */
    public static final IPrimitiveFactory COMMON_POOL = new IPrimitiveFactory()
    {
        @Override
        public IMutablePolygon newPaintable(int vertexCount, int layerCount)
        {
            return vertexCount == 4 
                    ? new MutablePolygon3x4().prepare(layerCount)
                    : vertexCount == 3
                        ? new MutablePolygon3x3().prepare(layerCount)
                        : new MutablePolygonNxN(MathHelper.log2DeBruijn(vertexCount)).prepare(layerCount, vertexCount);
        }
        
        @Override
        public IMutablePolygon newPaintable(int vertexCount)
        {
            return newPaintable(vertexCount, 1);
        }

        @Override
        public IMutableVertex claimMutableVertex()
        {
            return new UnpackedVertex3();
        }
        
        @Override
        public IPolygon toPainted(IMutablePolygon mutable)
        {
            AbstractPolygon<?> result = MAKERS[mutable.layerCount() - 1][Math.min(2, mutable.vertexCount() - 3)].make(mutable);
            result.load(mutable);
            return result;
        }

        @Override
        public IMutablePolygon claimCopy(IPolygon template, int vertexCount)
        {
            IMutablePolygon result = newPaintable(vertexCount, template.layerCount());
            ((AbstractPolygon<?>)result).load(template);
            return result;
        }
    };

    @FunctionalInterface 
    static private interface Maker
    {
        AbstractPolygon<?> make(IMutablePolygon from);
    }
    
    private static Maker[][] MAKERS = new Maker[3][3];
    
    static
    {
        MAKERS[0][0] = p -> new Polygon1x3();
        MAKERS[0][1] = p -> new Polygon1x4();
        MAKERS[0][2] = p -> new Polygon1xN(p.vertexCount());
        
        MAKERS[1][0] = p -> new Polygon2x3();
        MAKERS[1][1] = p -> new Polygon2x4();
        MAKERS[1][2] = p -> new Polygon2xN(p.vertexCount());
        
        MAKERS[2][0] = p -> new Polygon3x3();
        MAKERS[2][1] = p -> new Polygon3x4();
        MAKERS[2][2] = p -> new Polygon3xN(p.vertexCount());
    }

}
