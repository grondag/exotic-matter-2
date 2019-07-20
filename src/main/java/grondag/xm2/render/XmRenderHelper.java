package grondag.xm2.render;

import net.minecraft.client.render.VisibleRegion;

public abstract class XmRenderHelper {
    private XmRenderHelper() {}
    
    private static VisibleRegion visibleRegion;
    private static float tickDelta;
    
    public static void visibleRegion(VisibleRegion region) {
        visibleRegion = region;
    }
    
    public static VisibleRegion visibleRegion() {
        return visibleRegion;
    }

    public static void tickDelta(float tickDeltaIn) {
        tickDelta = tickDeltaIn;
    }
    
    public static float tickDelta() {
        return tickDelta;
    }
}
