package grondag.xm2.primitives.polygon;

/**
 * For stream reader polygons that can be moved to an address within the stream.
 */
public interface IStreamReaderPolygon extends IPolygon {
    public void moveTo(int address);

    public boolean hasValue();

    public boolean next();

    public boolean nextLink();
}
