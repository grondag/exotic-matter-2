package grondag.exotic_matter;

import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.util.function.Consumer;

import org.junit.Test;

import grondag.exotic_matter.model.primitives.polygon.IPolygon;
import grondag.exotic_matter.model.state.ModelState;
import grondag.exotic_matter.terrain.TerrainMeshFactory;

public class TerrainPerf
{
   
    @Test
    public void test()
    {
        ModelState[] modelStates = new ModelState[120000];
//        int[] offenders = new int[modelStates.length];
        ConfigXM.BLOCKS.simplifyTerrainBlockGeometry = true;
        
        try
          {
            FileInputStream fis = new FileInputStream("terrainState.data");
            ByteBuffer bytes = ByteBuffer.allocate(modelStates.length * 4 * Long.BYTES);
            fis.getChannel().read(bytes);
            fis.close();
            bytes.flip();
            for(int i = 0; i < modelStates.length; i++)
            {
                // NOT WORKING: will be have to be redone with new model state if want to use again
//                ModelState newState = new ModelState(bytes.getLong(), bytes.getLong(), bytes.getLong(), bytes.getLong());
//                assert newState.getShape() == ModShapes.TERRAIN_FILLER || newState.getShape() == ModShapes.TERRAIN_HEIGHT;
//                assert newState.getTerrainState() != null;
//                modelStates[i] = newState;
            }
          }
          catch (Exception e)
          {
              e.printStackTrace();
              return;
          }
        
        Consumer<IPolygon> sink = new Consumer<IPolygon>()
        {
            @Override
            public void accept(IPolygon t)
            {
                // NOOP
            }
        };
        
        for(int i = 0; i < 64; i++)
        {
            TerrainMeshFactory mesher = new TerrainMeshFactory();
            
            long elapsed = 0;
            long min = Long.MAX_VALUE;
            long max = 0;
            int longCount = 0;
            int errorCount = 0;
            
//            int minOffset = Integer.MAX_VALUE;
//            int maxOffset = Integer.MIN_VALUE;
            
            for(int j = 0; j < modelStates.length; j++)
            {
                ModelState modelState = modelStates[j];
                
//                int y = modelState.getTerrainState().getYOffset();
//                if(y < minOffset) minOffset = y;
//                if(y > maxOffset) maxOffset = y;
                
                final long start = System.nanoTime();
                try
                {
                    mesher.produceShapeQuads(modelState, sink);;
                }
                catch(Exception e)
                {
                    errorCount++;
//                    e.printStackTrace();
                }
                long t = System.nanoTime() - start;
                min = Math.min(min, t);
                max = Math.max(max, t);
                if(t > 60000)
                {
//                    offenders[j]++;
                    longCount++;
                }
                elapsed += t;
            }
            
            System.out.println("getShapeQuads mean time = " + elapsed / modelStates.length  + "ns");
            System.out.println("getShapeQuads min time  = " + min  + "ns");
            System.out.println("getShapeQuads max time  = " + max  + "ns");
            System.out.println("Runs exceeding 60,000ns: " + longCount);
//            mesher.reportCacheHits();
//            TerrainMeshFactory.reportAndClearHitCount();
//            CSGNode.Root.recombinedRenderableQuadsCounter.reportAndClear();
//            CSGNode.recombineCounter.reportAndClear();
//            CSGNode.reportRecombineStats();
//            CSGPlane.splitTimer.reportAndClear();
//            CSGPlane.splitSpanningTimer.reportAndClear();
            System.out.println("Error count = " + errorCount);
//            CSGPlane.printTagCounts();
//            System.out.println("minOffset = " + minOffset);
//            System.out.println("maxOffset = " + maxOffset);
            System.out.println(" ");
        }
        
//        int offenderCount = 0;
//        int goodConcavity = 0;
//        int offenderConcavity = 0;
//        System.out.println("Repeat offenders");
//        for(int j = 0; j < modelStates.length; j++)
//        {
//            final int concavity = modelStates[j].getTerrainState().divergence();
//            if(offenders[j] > 4)
//            {
//                offenderConcavity += concavity;
//                offenderCount++;
//                System.out.println(offenders[j] + "x " + modelStates[j].getTerrainState().toString());
//            }
//            else
//            {
//                goodConcavity += concavity;
//            }
//        }
//        System.out.println("Repeat offender count: " + offenderCount);
//        System.out.println("Average concavity non-offenders: " + (float) goodConcavity / (modelStates.length - offenderCount));
//        System.out.println("Average concavity offenders: " + (float) offenderConcavity / offenderCount);
    }

    
}