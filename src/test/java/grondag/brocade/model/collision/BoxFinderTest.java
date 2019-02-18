package grondag.exotic_matter.model.collision;

import org.junit.jupiter.api.Test;

import grondag.exotic_matter.model.collision.BoxFinderUtils.Slice;
import grondag.exotic_matter.varia.BitHelper;
import it.unimi.dsi.fastutil.ints.IntArrayList;

class BoxFinderTest
{
    @Test
    void test()
    {
//        perfTest();
//        perfTest();
//        perfTest();
//        perfTest();
//        perfTest();
//        perfTest();
//        perfTest();
//        perfTest();
//        perfTest();
//        perfTest();
//        perfTest();
//        perfTest();
//        perfTest();
//        perfTest();
        
        logicTest();
     
    }
    
    private void logicTest()
    {
   forEachBitTest();
        
        assert Slice.D1_0.layerBits == 1;
        assert Slice.D1_7.layerBits == 128;
        assert Slice.D8_0.layerBits == 255;
        
        for(Slice slice : Slice.values())
        {
            for(int i = 0; i < BoxFinderUtils.AREAS.length; i++)
            {
                int volumeKey = BoxFinderUtils.volumeKey(slice, i);
                
                assert BoxFinderUtils.volumeFromKey(volumeKey) == BoxFinderUtils.volume(slice, i);
                assert BoxFinderUtils.sliceFromKey(volumeKey) == slice;
                
                long pattern =  BoxFinderUtils.patternFromKey(volumeKey);
                assert pattern == BoxFinderUtils.AREAS[i];
            }
        }
        
        long[] snapshot = new long[8];
        
        BoxFinder bf = new BoxFinder();
        
        bf.setFilled(0, 0, 0, 7, 7, 7);
        bf.calcCombined();
        assert bf.isVolumeMaximal(BoxFinderUtils.VOLUME_KEYS[0]);
        for(int i = 1; i < BoxFinderUtils.VOLUME_KEYS.length; i++)
        {
            assert !bf.isVolumeMaximal(BoxFinderUtils.VOLUME_KEYS[i]);
        }
        
        for(Slice slice : Slice.values())
        {
            for(int i = 0; i < BoxFinderUtils.AREAS.length; i++)
            {
                final int p = i;
                BoxFinderUtils.testAreaBounds(p, (minX, minY, maxX, maxY) -> 
                {
                    bf.clear();
                    bf.setFilled(minX, minY, slice.min, maxX, maxY, slice.max);
                    bf.calcCombined();
                    assert bf.isVolumeMaximal(BoxFinderUtils.volumeKey(slice, p));
                    
                    if(slice.min > 0)
                    {
                        bf.clear();
                        bf.setFilled(minX, minY, slice.min - 1, maxX, maxY, slice.max);
                        bf.calcCombined();
                        assert !bf.isVolumeMaximal(BoxFinderUtils.volumeKey(slice, p));
                        
                        bf.clear();
                        bf.setFilled(0, 0, slice.min - 1, 7, 7, slice.max);
                        bf.calcCombined();
                        assert !bf.isVolumeMaximal(BoxFinderUtils.volumeKey(slice, p));
                    }
                    
                    if(slice.max < 7)
                    {
                        bf.clear();
                        bf.setFilled(minX, minY, slice.min, maxX, maxY, slice.max + 1);
                        bf.calcCombined();
                        assert !bf.isVolumeMaximal(BoxFinderUtils.volumeKey(slice, p));
                        
                        bf.clear();
                        bf.setFilled(0, 0, slice.min, 7, 7, slice.max + 1);
                        bf.calcCombined();
                        assert !bf.isVolumeMaximal(BoxFinderUtils.volumeKey(slice, p));
                    }
                    
                    if(minX > 0)
                    {
                        bf.clear();
                        bf.setFilled(minX - 1, minY, slice.min, maxX, maxY, slice.max);
                        bf.calcCombined();
                        assert !bf.isVolumeMaximal(BoxFinderUtils.volumeKey(slice, p));
                        
                        bf.clear();
                        bf.setFilled(minX - 1, 0, 0, maxX, 7, 7);
                        bf.calcCombined();
                        assert !bf.isVolumeMaximal(BoxFinderUtils.volumeKey(slice, p));
                    }
                    
                    if(maxX < 7)
                    {
                        bf.clear();
                        bf.setFilled(minX, minY, slice.min, maxX + 1, maxY, slice.max);
                        bf.calcCombined();
                        assert !bf.isVolumeMaximal(BoxFinderUtils.volumeKey(slice, p));
                        
                        bf.clear();
                        bf.setFilled(minX, 0, 0, maxX + 1, 7, 7);
                        bf.calcCombined();
                        assert !bf.isVolumeMaximal(BoxFinderUtils.volumeKey(slice, p));
                    }
                    
                    if(minY > 0)
                    {
                        bf.clear();
                        bf.setFilled(minX, minY - 1, slice.min, maxX, maxY, slice.max);
                        bf.calcCombined();
                        assert !bf.isVolumeMaximal(BoxFinderUtils.volumeKey(slice, p));
                        
                        bf.clear();
                        bf.setFilled(0, minY - 1, 0, 7, maxY, 7);
                        bf.calcCombined();
                        assert !bf.isVolumeMaximal(BoxFinderUtils.volumeKey(slice, p));
                    }
                    
                    if(maxY < 7)
                    {
                        bf.clear();
                        bf.setFilled(0, minY, 0, 7, maxY + 1, 7);
                        bf.calcCombined();
                        assert !bf.isVolumeMaximal(BoxFinderUtils.volumeKey(slice, p));
                    }
                    return 0;
                });
            }
        }
        
        // H - shape
        bf.clear();
        
        bf.setFilled(0, 0, 0, 1, 1, 7);
        bf.setFilled(0, 6, 0, 1, 7, 7);
        bf.calcCombined();
        bf.populateMaximalVolumes();
        
        assert bf.volumeCount == 2;
        assert BoxFinderUtils.volumeFromKey(bf.maximalVolumes[0]) == 32;
        assert BoxFinderUtils.volumeFromKey(bf.maximalVolumes[1]) == 32;
        
        bf.setFilled(0, 2, 4, 1, 5, 5);
        
        bf.saveTo(snapshot);
        
        bf.calcCombined();
        bf.populateMaximalVolumes();
        
        assert bf.volumeCount == 3;
        assert BoxFinderUtils.volumeFromKey(bf.maximalVolumes[0]) == 32;
        assert BoxFinderUtils.volumeFromKey(bf.maximalVolumes[1]) == 32;
        assert BoxFinderUtils.volumeFromKey(bf.maximalVolumes[2]) == 32;
        
        bf.populateIntersects();
        
        bf.findDisjointSets();
        
        assert bf.disjointSets.size() == 2;
        
        bf.scoreMaximalVolumes();
        
        assert bf.volumeScores[0] == 0;
        assert bf.volumeScores[1] == 0;
        assert bf.volumeScores[2] == 2;
        
        bf.explainDisjointSets();
        
        bf.restoreFrom(snapshot);
        assert bf.simplify(1);
        
        bf.calcCombined();
        bf.populateMaximalVolumes();
        assert bf.volumeCount == 1;
        bf.populateIntersects();
        bf.findDisjointSets(); 
        bf.explainDisjointSets();
        
        // Many intersecting volumes
        bf.clear();
        
        bf.setFilled(0, 0, 0, 7, 3, 7);
        bf.setFilled(1, 0, 1, 7, 4, 7);
        bf.setFilled(2, 0, 2, 6, 5, 6);
        bf.setFilled(5, 0, 5, 7, 7, 7);
        bf.calcCombined();
        bf.populateMaximalVolumes();
        bf.populateIntersects();
        bf.scoreMaximalVolumes();
        bf.explainMaximalVolumes();
        assert bf.volumeCount == 6;
        assert BoxFinderUtils.areVolumesSame(bf.maximalVolumes[0], 0, 0, 0, 7, 3, 7);
        assert BoxFinderUtils.areVolumesSame(bf.maximalVolumes[1], 1, 0, 1, 7, 4, 7);
        assert BoxFinderUtils.areVolumesSame(bf.maximalVolumes[2], 2, 0, 2, 6, 5, 6);
        assert BoxFinderUtils.areVolumesSame(bf.maximalVolumes[3], 5, 0, 2, 6, 5, 7);
        assert BoxFinderUtils.areVolumesSame(bf.maximalVolumes[4], 5, 0, 5, 7, 7, 7);
        assert BoxFinderUtils.areVolumesSame(bf.maximalVolumes[5], 2, 0, 5, 7, 5, 6);
        bf.findDisjointSets();
        assert bf.disjointSets.size() == 6;
        bf.explainDisjointSets();
    }
    
//    private void perfTest()
//    {
//        final ModelState workingModel = new ModelState();
//        workingModel.setShape(ModShapes.TERRAIN_HEIGHT);
//        workingModel.setTexture(PaintLayer.BASE, ModTextures.BIGTEX_TEST_SINGLE);
//        workingModel.setColorRGB(PaintLayer.BASE, BlockColorMapProvider.COLOR_BASALT);
//        workingModel.setTexture(PaintLayer.CUT, ModTextures.BIGTEX_TEST_SINGLE);
//        workingModel.setColorRGB(PaintLayer.CUT, BlockColorMapProvider.COLOR_BASALT);
//        workingModel.setTerrainStateKey(8522383571036711L);
//        
//        final OptimalBoxGenerator boxGen = new OptimalBoxGenerator();
//        
//        workingModel.getShape().meshFactory().produceShapeQuads(workingModel, boxGen);
//        long start = System.nanoTime();
//        boxGen.build();
//        System.out.println("Boxgen duration (ms) = " + ((System.nanoTime() - start) / 1000000f)); 
//    }

    void forEachBitTest()
    {
        IntArrayList results = new IntArrayList();
        
        BitHelper.forEachBit(0xFFFFFFFFFFFFFFFFL, i -> results.add(i));
        
        assert results.size() == 64;
        
        for(int i = 0; i < 64; i++)
        {
            assert results.contains(i);
        }
    }
    
//    void boundPerfTest()
//    {
//        boundPerfArray();
//        boundPerfArray();
//        boundPerfArray();
//        boundPerfArray();
//        boundPerfArray();
//        
//        boundPerfBitwise();
//        boundPerfBitwise();
//        boundPerfBitwise();
//        boundPerfBitwise();
//        boundPerfBitwise();
//        
//        long start = System.nanoTime();
//        boundPerfArray();
//        boundPerfArray();
//        boundPerfArray();
//        boundPerfArray();
//        boundPerfArray();
//        ExoticMatter.INSTANCE.info("Array bound testing %d ns", System.nanoTime() - start);
//        
//        start = System.nanoTime();
//        boundPerfBitwise();
//        boundPerfBitwise();
//        boundPerfBitwise();
//        boundPerfBitwise();
//        boundPerfBitwise();
//        ExoticMatter.INSTANCE.info("Bitwise bound testing %d ns", System.nanoTime() - start);
//    }
//    
//    
//    void boundPerfArray()
//    {
//        for(int i = 0; i < BoxFinderUtils.AREAS.length; i++)
//        {
//            callSink(BoxFinderUtils.MIN_X[i], BoxFinderUtils.MIN_Y[i], BoxFinderUtils.MAX_X[i], BoxFinderUtils.MAX_Y[i]);
//        }
//    }
//    
//    void boundPerfBitwise()
//    {
//        for(long pattern : BoxFinderUtils.AREAS)
//        {
//            BoxFinderUtils.findAreaBounds(pattern, (minX, minY, maxX, maxY) ->
//            {
//                callSink(minX, minY, maxX, maxY);
//            });
//        }
//    }
//    
//    private int xTotal;
//    private int yTotal;
//    
//    private void callSink(int minX, int minY, int maxX, int maxY)
//    {
//        xTotal += maxX - minX;
//        yTotal += maxY - minY;
//    }
}
