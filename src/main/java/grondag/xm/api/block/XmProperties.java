package grondag.xm.api.block;

import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction.Axis;

public class XmProperties {
    public static enum VerticalFacingX {
        
    }
    public static final DirectionProperty HORIZONTAL_FACING = Properties.HORIZONTAL_FACING;
    public static final DirectionProperty VERTICAL_FACING_XORTHO = DirectionProperty.of("facing", d -> d.getAxis() != Axis.X);
    public static final DirectionProperty VERTICAL_FACING_ZORTHO = DirectionProperty.of("facing", d -> d.getAxis() != Axis.Z);
    public static final IntProperty SPECIES = IntProperty.of("xm2_species", 0, 15);
}
