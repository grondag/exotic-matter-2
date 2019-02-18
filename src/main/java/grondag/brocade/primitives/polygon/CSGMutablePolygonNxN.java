package grondag.brocade.primitives.polygon;


public class CSGMutablePolygonNxN extends MutablePolygonNxN
{
    boolean isMarked = false;
    boolean isDeleted = false;
    int tag = IPolygon.NO_LINK_OR_TAG;
    int link = IPolygon.NO_LINK_OR_TAG;
    
    public CSGMutablePolygonNxN()
    {
        super(8);
    }

    @Override
    protected void copyPolyAttributesFrom(IPolygon template)
    {
        super.copyPolyAttributesFrom(template);
        isMarked = template.isMarked();
        isDeleted = template.isDeleted();
        tag = template.getTag();
        // NOTE: link not copied
    }

    @Override
    public void setMark(boolean isMarked)
    {
        this.isMarked = isMarked;
    }

    @Override
    public boolean isMarked()
    {
        return isMarked;
    }

    @Override
    public void flipMark()
    {
        isMarked = !isMarked;
    }
    
    @Override
    public void setDeleted()
    {
        this.isDeleted = true;
    }
    
    @Override
    public boolean isDeleted()
    {
        return this.isDeleted;
    }

    @Override
    public void setLink(int link)
    {
        this.link = link;
    }

    @Override
    public int getLink()
    {
        return this.link;
    }
    
    @Override
    public void setTag(int tag)
    {
        this.tag = tag;
    }
    
    @Override
    public int getTag()
    {
        return this.tag;
    }

    @Override
    public IMutablePolygon claimCopy()
    {
        return factory().claimCSGMutable(this);
    }

    @Override
    public IMutablePolygon claimCopy(int vertexCount)
    {
        return factory().claimCSGMutable(this, vertexCount);
    }

}
