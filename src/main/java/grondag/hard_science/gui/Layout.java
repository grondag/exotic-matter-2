package grondag.hard_science.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public enum Layout
{
    /** use the given dimension exactly */
    FIXED,
    /** dimension represents weight for allocating variable space */
    WEIGHTED,
    /** scale dimension to other orthogonalAxis according to aspect ratio */
    PROPORTIONAL
}
