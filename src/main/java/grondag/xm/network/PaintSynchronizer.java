package grondag.xm.network;

import grondag.xm.api.paint.XmPaint;

public interface PaintSynchronizer {
	XmPaint fromInt(int val);

	int toInt(XmPaint paint);
}
