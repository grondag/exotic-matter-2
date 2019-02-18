package grondag.exotic_matter;



import org.junit.Test;

import grondag.exotic_matter.varia.Base32Namer;


public class Base32NamerTest
{

    @Test
    public void test()
    {
        assert Base32Namer.makeRawName(1).equals("1");
        assert Base32Namer.makeRawName(32).equals("10");
        assert Base32Namer.makeRawName(1024).equals("100");
        assert Base32Namer.makeRawName(32768).equals("1000");
        
        assert Base32Namer.makeFilteredName(1).equals("1");
        assert Base32Namer.makeFilteredName(32).equals("10");
        assert Base32Namer.makeFilteredName(1024).equals("100");
        assert Base32Namer.makeFilteredName(32768).equals("1000");
        
        assert Base32Namer.makeRawName(31).equals("Z");
        assert Base32Namer.makeRawName(1023).equals("ZZ");
        assert Base32Namer.makeRawName(32767).equals("ZZZ");
        assert Base32Namer.makeRawName(32769).equals("1001");
        
        assert Base32Namer.makeRawName(518547).equals("FUCK");
        assert Base32Namer.makeFilteredName(518547).equals("FUCK");
        
        Base32Namer.loadBadNames("b1", "fu", "h0", "ky", "uk", "wn", "ab0", "a55", "bra", "cum", "d1e", "d1x", "er0", "evl", "fag", "fat", "f0k", "fuc", "fuk", "gay", "g1n", "g0b", "g0d", "g0y", "gun", "gyp", "h1v", "jap", "jew", "k1d", "kkk", "kum", "lez", "l5d", "mad", "n1g", "n1p", "pee", "p0m", "p00", "p0t", "pud", "5ex", "50b", "505", "t1t", "tnt", "uck", "wab", "w0g", "w0p", "wtf", "xtc", "xxx", "abb0", "alla", "anal", "anu5", "arab", "ar5e", "babe", "barf", "ba5t", "bl0w", "b0mb", "b0md", "b0ng", "b00b", "b00m", "burn", "butt", "chav", "ch1n", "c1g5", "cl1t", "c0ck", "c00n", "crap", "cumm", "cunn", "cunt", "dag0", "damn", "dead", "deg0", "deth", "d1ck", "d1ed", "d1e5", "d1ke", "d1nk", "d1ve", "d0ng", "d00m", "d0pe", "drug", "dumb", "dyke", "fart", "fear", "f1re", "fl00", "f0re", "fuck", "fuk5", "geez", "gen1", "g1pp", "g00k", "gr0e", "gyp0", "gypp", "hapa", "hebe", "heeb", "hell", "h0b0", "h0e5", "h0le", "h0m0", "h0nk", "h00k", "h0re", "h0rk", "h0rn", "1key", "1tch", "jade", "jeez", "j1ga", "j1gg", "j15m", "j1z ", "j1zz", "jug5", "k1ke", "k1ll", "k1nk", "k0ck", "k00n", "krap", "kum5", "kunt", "kyke", "la1d", "lezz", "l1e5", "l1my", "mam5", "meth", "m1lf", "m0f0", "m0ky", "muff", "munt", "naz1", "n1gg", "n1gr", "n00k", "nude", "nuke", "0ral", "0rga", "0rgy", "pak1", "pay0", "peck", "perv", "phuk", "phu0", "p155", "p1ky", "p1mp", "p155", "p1xy", "p0hm", "p00n", "p00p", "p0rn", "pr1c", "pr05", "pube", "pudd", "puke", "pu55", "pu5y", "0u1m", "ra85", "rape", "rere", "rump", "5cag", "5cat", "5cum", "5exy", "5hag", "5hat", "5hav", "5h1t", "51ck", "5kum", "5lav", "5lut", "5mut", "5n0t", "5p1c", "5p1g", "5p1k", "5p1t", "5uck", "taff", "tang", "tard", "teat", "t1t5", "turd", "twat", "v1br", "wank", "wetb", "wh1t", "wh1z", "wh0p", "wu55", "717", "7n7", "7aff", "7ang", "7ard", "7ea7", "7175", "7urd", "7wa7", "w7f", "x7c", "17ch", "fa7", "p07", "bu77", "de7h", "me7h", "we7b", "ba57", "cl17", "cun7", "far7", "kun7", "mun7", "5ca7", "5ha7", "5h17", "5lu7", "5mu7", "5n07", "5p17", "wh17", "3r0", "3vl", "t3at", "d3th", "m3th", "w3tb", "j3w", "l3z", "p33", "53x", "d3ad", "d3g0", "f3ar", "g33z", "g3n1", "h3b3", "h33b", "h3ll", "j33z", "l3zz", "p3ck", "p3rv", "r3r3", "53xy", "d13d", "d135", "h035", "1k3y", "l135", "d13", "ar53", "bab3", "d1k3", "d1v3", "d0p3", "dyk3", "f1r3", "f0r3", "gr03", "h0l3", "h0r3", "jad3", "k1k3", "kyk3", "nud3", "nuk3", "pub3", "puk3", "rap3", "73a7", "d37h", "m37h", "w37b");
        
        assert Base32Namer.isBadName("fat");
        assert Base32Namer.isBadName("BUTT");
        assert Base32Namer.isBadName("A55");
        assert !Base32Namer.isBadName("Nice");
        
        assert Base32Namer.makeFilteredName(518547).equals("N1CE");
        assert Base32Namer.makeFilteredName(518547 | 104928400000L).equals("ZYWK");
    }

}
