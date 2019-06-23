package grondag.brocade.state;

import static grondag.brocade.state.ModelStateData.STATE_FLAG_DISABLE_BLOCK_ONLY;
import static grondag.brocade.state.ModelStateData.STATE_FLAG_HAS_SOLID_RENDER;
import static grondag.brocade.state.ModelStateData.STATE_FLAG_HAS_TRANSLUCENT_GEOMETRY;
import static grondag.brocade.state.ModelStateData.STATE_FLAG_HAS_TRANSLUCENT_RENDER;
import static grondag.brocade.state.ModelStateData.STATE_FLAG_IS_POPULATED;
import static grondag.brocade.state.ModelStateData.STATE_FLAG_NEEDS_SPECIES;

import grondag.brocade.api.texture.TextureSet;
import grondag.brocade.apiimpl.texture.TextureSetRegistryImpl;
import grondag.brocade.mesh.ModelShape;
import grondag.brocade.mesh.ShapeMeshGenerator;
import grondag.brocade.painting.PaintLayer;

/**
 * Populates state flags for a given model state.
 * 
 * Results are returns as STATE_FLAG_XXXX values from ModelState for easy
 * persistence and usage within that class.
 */
public class ModelStateFlagHelper {
    public static final int getFlags(ISuperModelState state) {
        final ModelShape<?> shape = state.getShape();
        final ShapeMeshGenerator mesh = shape.meshFactory();

        int flags = STATE_FLAG_IS_POPULATED | mesh.getStateFlags(state);

        if (shape.metaUsage() == MetaUsage.SPECIES)
            flags |= STATE_FLAG_NEEDS_SPECIES;

        TextureSet texBase = state.getTexture(PaintLayer.BASE);
        flags |= texBase.stateFlags();

        flags |= state.getTexture(PaintLayer.CUT).stateFlags();

        if (state.isTranslucent(PaintLayer.BASE))
            flags |= (STATE_FLAG_HAS_TRANSLUCENT_GEOMETRY | STATE_FLAG_HAS_TRANSLUCENT_RENDER);
        else
            flags |= STATE_FLAG_HAS_SOLID_RENDER;

        if (state.isTranslucent(PaintLayer.CUT))
            flags |= (STATE_FLAG_HAS_TRANSLUCENT_GEOMETRY | STATE_FLAG_HAS_TRANSLUCENT_RENDER);
        else
            flags |= STATE_FLAG_HAS_SOLID_RENDER;

        if (mesh.hasLampSurface(state)) {
            TextureSet texLamp = state.getTexture(PaintLayer.LAMP);
            if (texLamp.id() != TextureSetRegistryImpl.NONE_ID)
                flags |= texLamp.stateFlags();

            if (state.isTranslucent(PaintLayer.LAMP))
                flags |= (STATE_FLAG_HAS_TRANSLUCENT_GEOMETRY | STATE_FLAG_HAS_TRANSLUCENT_RENDER);
            else
                flags |= STATE_FLAG_HAS_SOLID_RENDER;
        }

        TextureSet texOverlay = state.getTexture(PaintLayer.MIDDLE);
        if (texOverlay.id() != TextureSetRegistryImpl.NONE_ID)
            flags |= (texOverlay.stateFlags() | STATE_FLAG_HAS_TRANSLUCENT_RENDER);

        texOverlay = state.getTexture(PaintLayer.OUTER);
        if (texOverlay.id() != TextureSetRegistryImpl.NONE_ID)
            flags |= (texOverlay.stateFlags() | STATE_FLAG_HAS_TRANSLUCENT_RENDER);

        // turn off this.stateFlags that don't apply to non-block formats if we aren't
        // one
        if (mesh.stateFormat != StateFormat.BLOCK)
            flags &= STATE_FLAG_DISABLE_BLOCK_ONLY;

        return flags;
    }
}
