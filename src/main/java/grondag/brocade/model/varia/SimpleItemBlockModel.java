package grondag.brocade.model.varia;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ImmutableMap;

import grondag.exotic_matter.model.primitives.QuadHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.model.TRSRTransformation;

public class SimpleItemBlockModel implements IBakedModel
{
    private final List<BakedQuad> quads;
    private final boolean isShaded;

    private static final ImmutableMap<TransformType, TRSRTransformation> BLOCK_TRANSFORMS;

    // below is borrowed from ForgeBlockStateV1
    private static TRSRTransformation getTransform(float tx, float ty, float tz, float ax, float ay, float az, float s)
    {
        return TRSRTransformation.blockCenterToCorner(new TRSRTransformation(
                new Vector3f(tx / 16, ty / 16, tz / 16),
                TRSRTransformation.quatFromXYZDegrees(new Vector3f(ax, ay, az)),
                new Vector3f(s, s, s),
                null));
    }

    private static final TRSRTransformation flipX = new TRSRTransformation(null, null, new Vector3f(-1, 1, 1), null);

    private static TRSRTransformation leftify(TRSRTransformation transform)
    {
        return TRSRTransformation.blockCenterToCorner(flipX.compose(TRSRTransformation.blockCornerToCenter(transform)).compose(flipX));
    }
    static
    {
        TRSRTransformation thirdperson = getTransform(0, 2.5f, 0, 75, 45, 0, 0.375f);
        ImmutableMap.Builder<TransformType, TRSRTransformation> builder = ImmutableMap.builder();
        builder.put(TransformType.GUI,                     getTransform(0, 0, 0, 30, 225, 0, 0.625f));
        builder.put(TransformType.GROUND,                  getTransform(0, 3, 0, 0, 0, 0, 0.25f));
        builder.put(TransformType.FIXED,                   getTransform(0, 0, 0, 0, 0, 0, 0.5f));
        builder.put(TransformType.THIRD_PERSON_RIGHT_HAND, thirdperson);
        builder.put(TransformType.THIRD_PERSON_LEFT_HAND,  leftify(thirdperson));
        builder.put(TransformType.FIRST_PERSON_RIGHT_HAND, getTransform(0, 0, 0, 0, 45, 0, 0.4f));
        builder.put(TransformType.FIRST_PERSON_LEFT_HAND,  getTransform(0, 0, 0, 0, 255, 0, 0.4f));
        BLOCK_TRANSFORMS = builder.build();
  
    }
    public SimpleItemBlockModel(List<BakedQuad> quads, boolean isShaded)
    {
        this.quads = quads;
        this.isShaded = isShaded;
    }

    @Override
    public boolean isAmbientOcclusion()
    {
        return isShaded;
    }

    @Override
    public boolean isGui3d()
    {
        return true;
    }

    @Override
    public boolean isBuiltInRenderer()
    {
        return false;
    }

    @Override
    public @Nonnull TextureAtlasSprite getParticleTexture()
    {
        assert false : "Unsupported method call: SimpleItemModel.getParticleTexture()";
        return Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelManager().getMissingModel().getParticleTexture();
    }

    @Override
    public @Nonnull ItemCameraTransforms getItemCameraTransforms()
    {
        return ItemCameraTransforms.DEFAULT;
    }

    @Override
    public @Nonnull List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        return side == null ? quads : QuadHelper.EMPTY_QUAD_LIST;
    }

    @Override
    public @Nonnull ItemOverrideList getOverrides() {
        return ItemOverrideList.NONE;
    }

    // Below is borrowed from the old IPerspectiveAwareModel.
    // I confess I'm not exactly sure how it works.
    // Seems necessary to have item model render properly in 1st person.
    public static Pair<? extends IBakedModel, Matrix4f> handlePerspective(IBakedModel model, ImmutableMap<TransformType, TRSRTransformation> transforms, TransformType cameraTransformType)
    {
        TRSRTransformation tr = transforms.get(cameraTransformType);
        Matrix4f mat = null;
        if(tr != null && !tr.equals(TRSRTransformation.identity())) mat = TRSRTransformation.blockCornerToCenter(tr).getMatrix();
        return Pair.of(model, mat);
    }
    
    @Override
    public @Nonnull Pair<? extends IBakedModel, Matrix4f> handlePerspective(@Nonnull TransformType cameraTransformType) {
        return handlePerspective(this, BLOCK_TRANSFORMS, cameraTransformType);

    }
}