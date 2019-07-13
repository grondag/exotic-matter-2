package grondag.xm2.surface.api;

import grondag.xm2.painting.SurfaceTopology;

public interface XmSurfaceListBuilder {
	XmSurfaceListBuilder add(String nameKey, SurfaceTopology topology, float uvWrapDistance, int flags);
	
	XmSurfaceList build();
}
