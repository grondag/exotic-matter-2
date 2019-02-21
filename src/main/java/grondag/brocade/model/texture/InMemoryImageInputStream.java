package grondag.brocade.model.texture;

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Nullable;
import javax.imageio.stream.ImageInputStreamImpl;

import org.apache.commons.io.IOUtils;

public class InMemoryImageInputStream extends ImageInputStreamImpl {
    private final byte[] streamData;
    final int length;

    public InMemoryImageInputStream(InputStream stream) throws IOException {
        this(IOUtils.toByteArray(stream));
    }

    public InMemoryImageInputStream(byte[] streamData) {
        this.streamData = streamData;
        this.length = this.streamData.length;
        this.streamPos = 0;
    }

    @Override
    public int read() throws IOException {
        if (streamPos == length) {
            return -1;
        } else {
            return streamData[(int) streamPos++];
        }
    }

    @Override
    public int read(@Nullable byte[] b, int off, int len) throws IOException {
        if (streamPos == length) {
            return -1;
        }

        len = (int) Math.min(len, length - streamPos);
        System.arraycopy(this.streamData, (int) streamPos, b, off, len);
        streamPos += len;
        return len;
    }

    @Override
    public long length() {
        return this.length();
    }

    @Override
    public void seek(long pos) throws IOException {
        streamPos = pos;
    }

    @Override
    protected void finalize() throws Throwable {
        // Empty finalizer (for improved performance; no need to call
        // super.finalize() in this case)
    }
}