package grondag.brocade.model.state;

import static grondag.exotic_matter.model.state.ModelStateData.STATE_FLAG_DISABLE_BLOCK_ONLY;
import static grondag.exotic_matter.model.state.ModelStateData.STATE_FLAG_HAS_TRANSLUCENT_GEOMETRY;
import static grondag.exotic_matter.model.state.ModelStateData.STATE_FLAG_IS_POPULATED;
import static grondag.exotic_matter.model.state.ModelStateData.STATE_FLAG_NEEDS_SPECIES;
import static grondag.exotic_matter.model.state.ModelStateData.STATE_FLAG_HAS_TRANSLUCENT_RENDER;
import static grondag.exotic_matter.model.state.ModelStateData.STATE_FLAG_HAS_SOLID_RENDER;

import grondag.exotic_matter.model.mesh.ModelShape;
import grondag.exotic_matter.model.mesh.ShapeMeshGenerator;
import grondag.exotic_matter.model.painting.PaintLayer;
import grondag.exotic_matter.model.texture.ITexturePalette;
import grondag.exotic_matter.model.texture.TexturePaletteRegistry;

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

        ITexturePalette texBase = state.getTexture(PaintLayer.BASE);
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
            ITexturePalette texLamp = state.getTexture(PaintLayer.LAMP);
            if (texLamp != TexturePaletteRegistry.NONE)
                flags |= texLamp.stateFlags();

            if (state.isTranslucent(PaintLayer.LAMP))
                flags |= (STATE_FLAG_HAS_TRANSLUCENT_GEOMETRY | STATE_FLAG_HAS_TRANSLUCENT_RENDER);
            else
                flags |= STATE_FLAG_HAS_SOLID_RENDER;
        }

        ITexturePalette texOverlay = state.getTexture(PaintLayer.MIDDLE);
        if (texOverlay != TexturePaletteRegistry.NONE)
            flags |= (texOverlay.stateFlags() | STATE_FLAG_HAS_TRANSLUCENT_RENDER);

        texOverlay = state.getTexture(PaintLayer.OUTER);
        if (texOverlay != TexturePaletteRegistry.NONE)
            flags |= (texOverlay.stateFlags() | STATE_FLAG_HAS_TRANSLUCENT_RENDER);

        // turn off this.stateFlags that don't apply to non-block formats if we aren't
        // one
        if (mesh.stateFormat != StateFormat.BLOCK)
            flags &= STATE_FLAG_DISABLE_BLOCK_ONLY;

        return flags;
    }
}
