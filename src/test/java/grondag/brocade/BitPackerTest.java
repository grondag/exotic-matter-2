package grondag.exotic_matter;

import org.junit.Test;

import grondag.exotic_matter.varia.BitPacker64;

public class BitPackerTest
{

    private enum Things1 {ONE, TWO, THREE}
    
    private enum Things2 {A, B, C, D, E, F, G, H, I, J, K}
    
    private long bits;
    
    @Test
    public void test()
    {
        BitPacker64<BitPackerTest> packer = new BitPacker64<BitPackerTest>( v -> v.bits,  (o, v) -> o.bits = v);
        
        BitPacker64<BitPackerTest>.BooleanElement bool1 = packer.createBooleanElement();
        
        BitPacker64<BitPackerTest>.IntElement int1 = packer.createIntElement(0, 67);
        BitPacker64<BitPackerTest>.EnumElement<Things1> enum1 = packer.createEnumElement(Things1.class);

        BitPacker64<BitPackerTest>.IntElement int2 = packer.createIntElement(-53555643, 185375);
        BitPacker64<BitPackerTest>.EnumElement<Things2> enum2 = packer.createEnumElement(Things2.class);
        
        BitPacker64<BitPackerTest>.BooleanElement bool2 = packer.createBooleanElement();
        
        BitPacker64<BitPackerTest>.LongElement long1 = packer.createLongElement(-1, 634235);
        
        BitPacker64<BitPackerTest>.IntElement int3 = packer.createIntElement(8);
        
        assert(packer.bitLength() == 64);

        bits |= int1.getBits(42);
        bits |= enum1.getBits(Things1.THREE);
        bits |= int2.getBits(-582375);
        bits |= enum2.getBits(Things2.H);
        bits |= bool1.getBits(false);
        bits |= bool2.getBits(true);
        bits |= long1.getBits(0);
        bits |= int3.getBits(7);
        
        assert(enum1.getValue(this) == Things1.THREE);
        assert(enum2.getValue(this) == Things2.H);
        assert(int1.getValue(this) == 42);
        assert(int2.getValue(this) == -582375);
        assert(bool1.getValue(this) == false);
        assert(bool2.getValue(this) == true);
        assert(long1.getValue(this) == 0);
        assert(int3.getValue(this) == 7);
        
        int1.setValue(38, this);
        enum1.setValue(Things1.ONE, this);
        long1.setValue(52947, this);
        bool1.setValue(true, this);
        int3.setValue(0, this);
        
        assert(enum1.getValue(this) == Things1.ONE);
        assert(int1.getValue(this) == 38);
        assert(bool1.getValue(this) == true);
        assert(long1.getValue(this) == 52947);
        assert(int3.getValue(this) == 0);
    }

}