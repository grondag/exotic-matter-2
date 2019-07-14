package grondag.xm2.api.surface;

import grondag.xm2.surface.XmSurfaceImpl;

public interface XmSurfaceList {
    static XmSurfaceListBuilder builder() {
	return XmSurfaceImpl.builder();
    }

    int size();

    XmSurface get(int index);
}
