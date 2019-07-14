package grondag.xm2.api.model;

import grondag.xm2.api.paint.XmPaint;
import grondag.xm2.api.paint.XmPaintRegistry;
import grondag.xm2.api.surface.XmSurface;
import net.minecraft.nbt.CompoundTag;

public interface ModelState extends ModelWorldState, ModelPrimitiveState {
    boolean isImmutable();

    ImmutableModelState toImmutable();

    MutableModelState mutableCopy();

    /**
     * Persisted but not part of hash nor included in equals comparison. If true,
     * refreshFromWorldState does nothing.
     */
    boolean isStatic();

    int paintIndex(int surfaceIndex);

    default XmPaint paint(int surfaceIndex) {
        return XmPaintRegistry.INSTANCE.get(paintIndex(surfaceIndex));
    }

    default XmPaint paint(XmSurface surface) {
        return XmPaintRegistry.INSTANCE.get(paintIndex(surface.ordinal()));
    }

    /**
     * True if base paint layer is translucent or lamp paint layer is present and
     * translucent.
     */
    boolean hasTranslucentGeometry();

    boolean hasMasonryJoin();

    boolean hasTextureRotation();

    /**
     * Means that one or more elements (like a texture) uses species. Does not mean
     * that the shape or block actually capture or generate species other than 0.
     */
    boolean hasSpecies();

    /**
     * Returns true if visual elements and geometry match. Does not consider species
     * in matching.
     */
    boolean doShapeAndAppearanceMatch(ModelState other);

    /**
     * Returns true if visual elements match. Does not consider species or geometry
     * in matching.
     */
    boolean doesAppearanceMatch(ModelState other);

    void serializeNBT(CompoundTag tag);
}
