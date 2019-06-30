package grondag.xm2.primitives.stream;

import grondag.fermion.intstream.IntStreams;
import grondag.xm2.primitives.polygon.IPolygon;

public class ReadOnlyPolyStream extends AbstractPolyStream implements IReadOnlyPolyStream {
    void load(WritablePolyStream streamIn, int formatFlags) {
        prepare(IntStreams.claim(streamIn.stream.capacity()));

        if (!streamIn.isEmpty()) {
            streamIn.origin();
            IPolygon reader = streamIn.reader();
            do
                this.appendCopy(reader, formatFlags);
            while (streamIn.next());
        }

        this.stream.compact();
    }

    @Override
    protected void doRelease() {
        super.doRelease();
    }

    @Override
    protected void returnToPool() {
        PolyStreams.release(this);
    }
}
