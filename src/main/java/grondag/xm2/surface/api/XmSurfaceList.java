package grondag.xm2.surface.api;

import grondag.xm2.surface.impl.XmSurfaceImpl;

public interface XmSurfaceList {
	static XmSurfaceListBuilder builder() {
		return XmSurfaceImpl.builder();
	}
	
	int size();
	
	XmSurface get(int index);
}
