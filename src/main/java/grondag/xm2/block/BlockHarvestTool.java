package grondag.xm2.block;

public enum BlockHarvestTool {
    ANY(null), PICK("pickaxe"), AXE("axe"), SHOVEL("shovel");

    /**
     * String MC uses to compare test for this tool type. Null means any tool can
     * harvest.
     */
    
    public final String toolString;

    private BlockHarvestTool(String toolString) {
        this.toolString = toolString;
    }
}
