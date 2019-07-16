package grondag.xm2.model.state;

import grondag.xm2.api.connect.world.ModelStateFunction;
import grondag.xm2.api.model.ModelPrimitive;
import grondag.xm2.api.model.ModelState;
import grondag.xm2.api.model.ModelStateFlags;
import grondag.xm2.api.paint.XmPaint;
import grondag.xm2.block.XmBlockStateAccess;

class ModelStateVaria {
    
    /**
     * Populates state flags for a given model state.
     * 
     * Results are returns as STATE_FLAG_XXXX values from ModelState for easy
     * persistence and usage within that class.
     */
    static final int getFlags(ModelState state) {
        final ModelPrimitive mesh = state.primitive();
    
        int flags = ModelStateFlags.STATE_FLAG_IS_POPULATED | mesh.stateFlags(state);
    
        final int surfCount = mesh.surfaces(state).size();
        for (int i = 0; i < surfCount; i++) {
            XmPaint p = state.paint(i);
            final int texDepth = p.textureDepth();
            for (int j = 0; j < texDepth; j++) {
                flags |= p.texture(j).stateFlags();
            }
        }
    
        return flags;
    }

    /**
     * Use this as factory for model state block tests that DON'T need to refresh
     * from world.
     */
    static final ModelStateFunction TEST_GETTER_STATIC = (w, b, p) -> XmBlockStateAccess.modelState(b, w, p, false);
    /**
     * Use this as factory for model state block tests that DO need to refresh from
     * world.
     */
    static final ModelStateFunction TEST_GETTER_DYNAMIC = (w, b, p) -> XmBlockStateAccess.modelState(b, w, p, true);

}
