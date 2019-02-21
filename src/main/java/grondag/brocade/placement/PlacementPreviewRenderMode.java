package grondag.brocade.placement;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public enum PlacementPreviewRenderMode {
    SELECT(0x91BFBD), PLACE(0xA0FFFF), EXCAVATE(0xFC8D59), OBSTRUCTED(0xFFFFBF);

    public final float red;
    public final float green;
    public final float blue;

    private PlacementPreviewRenderMode(int rgbColor) {
        this.red = ((rgbColor >> 16) & 0xFF) / 255f;
        this.green = ((rgbColor >> 8) & 0xFF) / 255f;
        this.blue = (rgbColor & 0xFF) / 255f;
    }
}
