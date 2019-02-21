package grondag.exotic_matter;

import static org.junit.Assert.*;

import org.junit.Test;

import grondag.exotic_matter.terrain.TerrainState;
import grondag.exotic_matter.world.HorizontalCorner;
import grondag.exotic_matter.world.HorizontalFace;

public class TerrainStateTest {

    @Test
    public void test() {
        TerrainState state = new TerrainState(
                TerrainState.computeStateKey(13, new int[] { -12, -15, -5, 12 }, new int[] { 18, 0, 13, -16 }, 0), 0);
        System.out.print(state.toString());
        assertTrue(state.centerHeight() == 13);
        assertTrue(state.height(HorizontalFace.EAST) == -15);
        assertTrue(state.height(HorizontalCorner.SOUTH_WEST) == -16);
    }

}