package grondag.exotic_matter.model.primitives;

import java.util.function.Consumer;

import javax.annotation.Nullable;

import org.junit.jupiter.api.Test;

import grondag.acuity.api.IRenderPipeline;
import grondag.acuity.api.IUniform.IUniform1f;
import grondag.acuity.api.IUniform.IUniform1i;
import grondag.acuity.api.IUniform.IUniform2f;
import grondag.acuity.api.IUniform.IUniform2i;
import grondag.acuity.api.IUniform.IUniform3f;
import grondag.acuity.api.IUniform.IUniform3i;
import grondag.acuity.api.IUniform.IUniform4f;
import grondag.acuity.api.IUniform.IUniform4i;
import grondag.acuity.api.IUniform.IUniformMatrix4f;
import grondag.acuity.api.TextureFormat;
import grondag.acuity.api.UniformUpdateFrequency;
import grondag.exotic_matter.model.painting.Surface;
import grondag.exotic_matter.model.painting.SurfaceTopology;
import grondag.exotic_matter.model.primitives.stream.StaticEncoder;
import grondag.exotic_matter.varia.intstream.IIntStream;
import grondag.exotic_matter.varia.intstream.IntStreams;
import grondag.exotic_matter.world.Rotation;
import net.minecraft.util.BlockRenderLayer;

class StaticEncoderTest {
    final IRenderPipeline pipe = new IRenderPipeline() {

        @Override
        public int getIndex() {
            return 3;
        }

        @Override
        public TextureFormat textureFormat() {
            return null;
        }

        @Override
        public void uniform1f(@Nullable String name, @Nullable UniformUpdateFrequency frequency,
                @Nullable Consumer<IUniform1f> initializer) {

        }

        @Override
        public void uniform2f(@Nullable String name, @Nullable UniformUpdateFrequency frequency,
                @Nullable Consumer<IUniform2f> initializer) {

        }

        @Override
        public void uniform3f(@Nullable String name, @Nullable UniformUpdateFrequency frequency,
                @Nullable Consumer<IUniform3f> initializer) {

        }

        @Override
        public void uniform4f(@Nullable String name, @Nullable UniformUpdateFrequency frequency,
                @Nullable Consumer<IUniform4f> initializer) {

        }

        @Override
        public void uniform1i(@Nullable String name, @Nullable UniformUpdateFrequency frequency,
                @Nullable Consumer<IUniform1i> initializer) {

        }

        @Override
        public void uniform2i(@Nullable String name, @Nullable UniformUpdateFrequency frequency,
                @Nullable Consumer<IUniform2i> initializer) {

        }

        @Override
        public void uniform3i(@Nullable String name, @Nullable UniformUpdateFrequency frequency,
                @Nullable Consumer<IUniform3i> initializer) {

        }

        @Override
        public void uniform4i(@Nullable String name, @Nullable UniformUpdateFrequency frequency,
                @Nullable Consumer<IUniform4i> initializer) {

        }

        @Override
        public IRenderPipeline finish() {
            return null;
        }

        @Override
        public void uniformMatrix4f(@Nullable String name, @Nullable UniformUpdateFrequency frequency,
                @Nullable Consumer<IUniformMatrix4f> initializer) {

        }

    };

    @Test
    void test() {
        final int BASE = 23;
        IIntStream stream = IntStreams.claim();

        assert StaticEncoder.shouldContractUVs(stream, BASE, 0);
        assert StaticEncoder.shouldContractUVs(stream, BASE, 1);
        assert StaticEncoder.shouldContractUVs(stream, BASE, 2);

        StaticEncoder.setContractUVs(stream, BASE, 1, false);
        assert !StaticEncoder.shouldContractUVs(stream, BASE, 1);
        StaticEncoder.setContractUVs(stream, BASE, 1, true);
        assert StaticEncoder.shouldContractUVs(stream, BASE, 1);

        StaticEncoder.setContractUVs(stream, BASE, 1, false);
        StaticEncoder.setContractUVs(stream, BASE, 2, false);

        assert !StaticEncoder.isLockUV(stream, BASE, 0);
        assert !StaticEncoder.isLockUV(stream, BASE, 1);
        assert !StaticEncoder.isLockUV(stream, BASE, 2);

        StaticEncoder.setLockUV(stream, BASE, 1, true);
        assert StaticEncoder.isLockUV(stream, BASE, 1);
        StaticEncoder.setLockUV(stream, BASE, 1, false);
        assert !StaticEncoder.isLockUV(stream, BASE, 1);

        StaticEncoder.setLockUV(stream, BASE, 1, true);
        StaticEncoder.setLockUV(stream, BASE, 2, true);

        assert StaticEncoder.getRotation(stream, BASE, 0) == Rotation.ROTATE_NONE;
        assert StaticEncoder.getRotation(stream, BASE, 1) == Rotation.ROTATE_NONE;
        assert StaticEncoder.getRotation(stream, BASE, 2) == Rotation.ROTATE_NONE;

        StaticEncoder.setRotation(stream, BASE, 1, Rotation.ROTATE_270);
        assert StaticEncoder.getRotation(stream, BASE, 1) == Rotation.ROTATE_270;

        StaticEncoder.setRotation(stream, BASE, 0, Rotation.ROTATE_180);
        StaticEncoder.setRotation(stream, BASE, 1, Rotation.ROTATE_90);

        assert StaticEncoder.getRenderLayer(stream, BASE, 0) == BlockRenderLayer.SOLID;
        assert StaticEncoder.getRenderLayer(stream, BASE, 1) == BlockRenderLayer.SOLID;
        assert StaticEncoder.getRenderLayer(stream, BASE, 2) == BlockRenderLayer.SOLID;

        StaticEncoder.setRenderLayer(stream, BASE, 1, BlockRenderLayer.TRANSLUCENT);
        assert StaticEncoder.getRenderLayer(stream, BASE, 1) == BlockRenderLayer.TRANSLUCENT;

        StaticEncoder.setRenderLayer(stream, BASE, 0, BlockRenderLayer.CUTOUT_MIPPED);
        StaticEncoder.setRenderLayer(stream, BASE, 1, BlockRenderLayer.CUTOUT);

        assert StaticEncoder.getTextureSalt(stream, BASE) == 0;
        StaticEncoder.setTextureSalt(stream, BASE, 255);
        assert StaticEncoder.getTextureSalt(stream, BASE) == 255;
        StaticEncoder.setTextureSalt(stream, BASE, 1);

        Surface S1 = Surface.builder(SurfaceTopology.CUBIC).build();
        Surface S2 = Surface.builder(SurfaceTopology.TILED).build();

        StaticEncoder.setSurface(stream, BASE, S1);
        assert StaticEncoder.getSurface(stream, BASE) == S1;

        StaticEncoder.setPipelineIndex(stream, BASE, pipe.getIndex());

        StaticEncoder.setSurface(stream, 47, S2);
        assert StaticEncoder.getSurface(stream, BASE) == S1;
        assert StaticEncoder.getSurface(stream, 47) == S2;

        StaticEncoder.setSurface(stream, BASE, Surface.NO_SURFACE);
        assert StaticEncoder.getSurface(stream, BASE) == Surface.NO_SURFACE;

        assert StaticEncoder.shouldContractUVs(stream, BASE, 0);
        assert !StaticEncoder.shouldContractUVs(stream, BASE, 1);
        assert !StaticEncoder.shouldContractUVs(stream, BASE, 2);

        assert !StaticEncoder.isLockUV(stream, BASE, 0);
        assert StaticEncoder.isLockUV(stream, BASE, 1);
        assert StaticEncoder.isLockUV(stream, BASE, 2);

        assert StaticEncoder.getRotation(stream, BASE, 0) == Rotation.ROTATE_180;
        assert StaticEncoder.getRotation(stream, BASE, 1) == Rotation.ROTATE_90;

        assert StaticEncoder.getRenderLayer(stream, BASE, 0) == BlockRenderLayer.CUTOUT_MIPPED;
        assert StaticEncoder.getRenderLayer(stream, BASE, 1) == BlockRenderLayer.CUTOUT;

        assert StaticEncoder.getTextureSalt(stream, BASE) == 1;

        stream.release();
    }

}
