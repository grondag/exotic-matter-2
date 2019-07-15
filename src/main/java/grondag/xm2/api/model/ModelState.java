package grondag.xm2.api.model;

import grondag.xm2.api.allocation.Reference;
import grondag.xm2.api.paint.XmPaint;
import grondag.xm2.api.paint.XmPaintRegistry;
import grondag.xm2.api.surface.XmSurface;
import net.minecraft.nbt.CompoundTag;

public interface ModelState extends ModelWorldState, ModelPrimitiveState, Reference {
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
