package grondag.brocade.primitives.polygon;

import grondag.exotic_matter.model.primitives.vertex.IMutableVertex;
import net.minecraft.client.Minecraft;
import net.minecraft.crash.CrashReport;

public abstract class AbstractLargeImmutablePolygon<T extends AbstractLargeImmutablePolygon<T>> extends AbstractLargePolygon<T>
{
    protected IMutableVertex[] vertices;
    
    protected AbstractLargeImmutablePolygon(int vertexCount, Class<? extends IMutableVertex> clazz)
    {
        super();
        assert vertexCount > 4;
        vertices = new IMutableVertex[vertexCount];
        try
        {
            for(int i = 0; i < vertexCount; i++)
                vertices[i] = clazz.newInstance();
        }
        catch (Exception e)
        {
            Minecraft.getMinecraft().crashed(new CrashReport("Unable to instantiate vertices. This is a bug.", e));
        }
    }

    @Override
    public final int vertexCount()
    {
        return vertices.length;
    }
    
    @Override
    public final IMutableVertex[] vertices()
    {
        return vertices;
    }
    
    @Override
    protected int computeArrayIndex(int vertexIndex)
    {
        return vertexIndex;
    }
}
