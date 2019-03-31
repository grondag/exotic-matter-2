package grondag.brocade;

import org.junit.Test;

import grondag.fermion.world.CornerJoinBlockState;
import grondag.fermion.world.CornerJoinBlockStateSelector;
import grondag.fermion.world.NeighborBlocks;
import grondag.fermion.world.SimpleJoin;

public class CornerJoinBlockStateTest {

    @Test
    public void test() {
        // added simple join as an attribute of corner joins so don't need to have both
        // when both are needed
        // this test ensures consistency
        @SuppressWarnings("null")
        NeighborBlocks dummy = new NeighborBlocks(null, null);

        for (int i = 0; i < 1 << 26; i++) {
            NeighborBlocks.NeighborTestResults tests = dummy.getFakeNeighborTestResults(i);
            SimpleJoin simple = new SimpleJoin(tests);
            CornerJoinBlockState corner = CornerJoinBlockStateSelector
                    .getJoinState(CornerJoinBlockStateSelector.findIndex(tests));
            assert (corner.simpleJoin.getIndex() == simple.getIndex());
        }
    }

}