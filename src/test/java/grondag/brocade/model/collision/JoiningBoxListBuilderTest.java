package grondag.exotic_matter.model.collision;

import org.junit.jupiter.api.Test;

class JoiningBoxListBuilderTest
{

    @Test
    void test()
    {
        JoiningBoxListBuilder jbl = new JoiningBoxListBuilder();
        
        jbl.add(0, 0, 0, 2, 2, 2);
        
        assert jbl.boxes().size() == 1;
        
        jbl.add(2, 0, 0, 4, 2, 2);
        
        assert jbl.boxes().size() == 1;
        
        jbl.add(0, 2, 0, 2, 4, 2);
        
        assert jbl.boxes().size() == 2;
        
        jbl.add(2, 2, 0, 4, 4, 2);
        
        assert jbl.boxes().size() == 1;
        
    }

}
