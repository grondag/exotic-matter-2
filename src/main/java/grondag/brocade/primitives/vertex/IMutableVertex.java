package grondag.brocade.primitives.vertex;

import grondag.brocade.primitives.PolyFactory;
import grondag.brocade.primitives.polygon.IPrimitiveFactory;
import grondag.fermion.color.ColorHelper;

public interface IMutableVertex extends IVec3f
{
    /**
     * WARNING: Will always return an immutable reference to ensure safety.
     * Do not use on mutable instances to avoid memory allocation overhead.
     */
    public Vec3f pos();

    /**
     * WARNING: Will always return an immutable reference to ensure safety.
     * Do not use on mutable instances to avoid memory allocation overhead.
     */
    public Vec3f normal();
    
    public float normalX();
    
    public float normalY();
    
    public float normalZ();

    public boolean hasNormal();
    
    /**
     * Will not retain a reference to normal if it is mutable.
     */
    public void setNormal(Vec3f normal);
    
    public void setNormal(float x, float y, float z);
    
    /**
     * Will not retain a reference to pos if it is mutable.
     */
    public void setPos(Vec3f pos);
    
    public void setPos(float x, float y, float z);

    /**
     * Blends this result with other vertex according to weight. 
     * Weight 0 gives this vertex. Weight 1 gives other vertex.
     * The two input vertices and output vertex must have the same layer count. 
     * Neither input instance will be modified.<br>
     * Does not retain a reference to the output or either input.<br>
     */
    public default void interpolate(IMutableVertex other, float otherWeight, IMutableVertex output)
    {
        final int layerCount = getLayerCount();
        assert layerCount == other.getLayerCount();
        output.setLayerCount(layerCount);
        
        final float newX = this.x() + (other.x() - this.x()) * otherWeight;
        final float newY = this.y() + (other.y() - this.y()) * otherWeight;
        final float newZ = this.z() + (other.z() - this.z()) * otherWeight;
        output.setPos(newX, newY, newZ);
        
        output.setGlow((int) (getGlow() + (other.getGlow() - getGlow()) * otherWeight));
        
        if(this.hasNormal() && other.hasNormal())
        {
            final float normX = normalX() + (other.normalX() - normalX()) * otherWeight;
            final float normY = normalY() + (other.normalY() - normalY()) * otherWeight;
            final float normZ = normalZ() + (other.normalZ() - normalZ()) * otherWeight;
            final float normScale = 1f / (float)Math.sqrt(normX * normX + normY *  normY + normZ * normZ);
            output.setNormal(normX * normScale, normY * normScale, normZ * normScale);
        }
        else
            output.setNormal(null);
        
        output.setColor(0, ColorHelper.interpolate(getColor(0), other.getColor(0), otherWeight));
        
        output.setUV(0, 
                getU(0) + (other.getU(0) - getU(0)) * otherWeight, 
                getV(0) + (other.getV(0) - getV(0)) * otherWeight);
        
        if(layerCount > 1)
        {
            output.setColor(1, ColorHelper.interpolate(getColor(1), other.getColor(1), otherWeight));
            
            output.setUV(1, 
                    getU(1) + (other.getU(1) - getU(1)) * otherWeight, 
                    getV(1) + (other.getV(1) - getV(1)) * otherWeight);
            
            if(layerCount == 3)
            {
                output.setColor(2, ColorHelper.interpolate(getColor(2), other.getColor(2), otherWeight));
                
                output.setUV(2, 
                        getU(2) + (other.getU(2) - getU(2)) * otherWeight, 
                        getV(2) + (other.getV(2) - getV(2)) * otherWeight);
            }
        }
    }

    /**
     * Like {@link #interpolate(IMutableVertex, float, IMutableVertex)} but 
     * returns result as a new instance.<br>
     * Does not retain a reference to the output or either input.<br>
     */
    public default IMutableVertex interpolate(IMutableVertex otherVertex, float otherWeight)
    {
        assert this.getLayerCount() == otherVertex.getLayerCount();
        IMutableVertex result = factory().claimMutableVertex();
        result.setLayerCount(this.getLayerCount());
        interpolate(otherVertex, otherWeight, result);
        return result;
    }
    
    public int getColor(int layerIndex);

    public int getGlow();

    public float getU(int layerIndex);
    
    public float getV(int layerIndex);

    public void setColor(int layerIndex, int color);

    public void setGlow(int glow);

    public void setUV(int layerIndex, float u, float v);

    public void setU(int layerIndex, float u);
    
    public void setV(int layerIndex, float v);

    public int getLayerCount();

    public void setLayerCount(int layerCount);

    public default void copyFrom(IMutableVertex source)
    {
        if(source.hasNormal())
            setNormal(source.normalX(), source.normalY(), source.normalZ());
        else
            setNormal(null);

        setPos(source.x(), source.y(), source.z());

        setGlow(source.getGlow());
        
        final int layerCount = source.getLayerCount();
        setLayerCount(layerCount);
        
        setColor(0, source.getColor(0));
        setUV(0, source.getU(0), source.getV(0));
        
        if(layerCount > 1)
        {
            setColor(1, source.getColor(1));
            setUV(1, source.getU(1), source.getV(1));
            
            if(layerCount == 3)
            {
                setColor(2, source.getColor(2));
                setUV(2, source.getU(2), source.getV(2));
            }
        }
    }
    
    /**
     * Allocation manager for this instance. May or may not be
     * a pooled allocation manager. If it is pooled, then can 
     * be used to allocate instances in the same pool
     * and (if supported) discover and inspect allocated objects
     * in the same pool.  Not generally intended to be used directly.
     */
    default IPrimitiveFactory factory()
    {
        return PolyFactory.COMMON_POOL;
    }
    
    /**
     * Signals to allocation manager this instance is being referenced
     * by something other than the original requester and will prevent
     * the object from being recycled if the original allocator releases it.<p>
     * 
     * Note that retain count is always 1 when an object is first created,
     * so if the object is held by the originator there is no need to call this.
     */
    default void retain()
    {
        
    }
    
    /**
     * Should be called by when the original reference or another reference
     * created via {@link #retain()} is no longer held.  <p>
     * 
     * When retain count is 0 the object will be returned to its allocation
     * pool if it has one.
     */
    default void release()
    {
        
    }

    /**
     * Should be called instead of {@link #release()} when this is the last
     * held reference allocated by this objects factory. Will raise an assertion
     * error (if enabled) if this is not the last retained instance in the factory pool.<p>
     * 
     * For use as a debugging aid - has no functional necessity otherwise.
     * Also has no effect/meaning for unpooled instances.
     */
    default void releaseLast()
    {
        
    }
}
