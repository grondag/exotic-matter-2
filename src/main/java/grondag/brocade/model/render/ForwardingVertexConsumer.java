package grondag.brocade.model.render;

import javax.annotation.Nullable;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;

/**
 * Forge has a class like this, but wrapped consumer here is non-final.
 */
public abstract class ForwardingVertexConsumer implements IVertexConsumer {
    protected @Nullable IVertexConsumer wrapped;

    @SuppressWarnings("null")
    @Override
    public VertexFormat getVertexFormat() {
        return wrapped.getVertexFormat();
    }

    @SuppressWarnings("null")
    @Override
    public void setQuadTint(int tint) {
        wrapped.setQuadTint(tint);
    }

    @SuppressWarnings("null")
    @Override
    public void setQuadOrientation(EnumFacing orientation) {
        wrapped.setQuadOrientation(orientation);
    }

    @SuppressWarnings("null")
    @Override
    public void setApplyDiffuseLighting(boolean diffuse) {
        wrapped.setApplyDiffuseLighting(diffuse);
    }

    @SuppressWarnings("null")
    @Override
    public void setTexture(TextureAtlasSprite texture) {
        wrapped.setTexture(texture);
    }

    @SuppressWarnings("null")
    @Override
    public void put(final int element, float... data) {
        wrapped.put(element, data);
    }
}
