package grondag.xm2.terrain;

import net.minecraft.util.StringIdentifiable;

public enum TerrainType implements StringIdentifiable {
    FILL_UP_ONE(1, true),
    FILL_UP_TWO(2, true),
    HEIGHT_1(1, false),
    HEIGHT_2(2, false),
    HEIGHT_3(3, false),
    HEIGHT_4(4, false),
    HEIGHT_5(5, false),
    HEIGHT_6(6, false),
    HEIGHT_7(7, false),
    HEIGHT_8(8, false),
    HEIGHT_9(9, false),
    HEIGHT_10(10, false),
    HEIGHT_11(11, false),
    HEIGHT_12(12, false),
    CUBE(1, true);
    
    public final String name;
    public final boolean isFiller;
    public final boolean isHeight;
    public final int height;
    public final int fillOffset;
    
    private TerrainType(int height, boolean filler) {
        this.name = this.name().toLowerCase();
        this.height = filler ? 0 : height;
        this.fillOffset = filler ? height : 0;
        this.isFiller = filler;
        this.isHeight = !filler;
    }
    
    @Override
    public String asString() {
        return name;
    }
}
