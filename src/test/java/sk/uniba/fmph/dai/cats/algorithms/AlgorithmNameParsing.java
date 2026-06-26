package sk.uniba.fmph.dai.cats.algorithms;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AlgorithmNameParsing {


    @Test
    void matchMhs(){
        Assertions.assertTrue(Algorithm.MHS.matchesName("MHS"));
    }

    @Test
    void matchMhsMxp(){
        Assertions.assertTrue(Algorithm.MHS_MXP.matchesName("MHS_MXP"));
    }

    @Test
    void matchMhsMxpDash(){
        Assertions.assertTrue(Algorithm.MHS_MXP.matchesName("MHS-MXP"));
    }

    @Test
    void matchMhsMxpSingleWord(){
        Assertions.assertTrue(Algorithm.MHS_MXP.matchesName("MHSMXP"));
    }

    @Test
    void matchHst(){
        Assertions.assertTrue(Algorithm.HST.matchesName("HST"));
    }

    @Test
    void matchHstMxp(){
        Assertions.assertTrue(Algorithm.HST_MXP.matchesName("HST_MXP"));
    }

    @Test
    void matchHstMxpDash(){
        Assertions.assertTrue(Algorithm.HST_MXP.matchesName("HST-MXP"));
    }

    @Test
    void matchHstMxpSingleWord(){
        Assertions.assertTrue(Algorithm.HST_MXP.matchesName("HSTMXP"));
    }

    @Test
    void matchHsdagSingleWord(){
        Assertions.assertTrue(Algorithm.HSDAG.matchesName("HSDAG"));
    }

    @Test
    void matchHsdagDash(){
        Assertions.assertTrue(Algorithm.HSDAG.matchesName("HS-DAG"));
    }

    @Test
    void matchHsdagMxpSingleWord(){
        Assertions.assertTrue(Algorithm.HSDAG_MXP.matchesName("HSDAGMXP"));
    }

    @Test
    void matchHsdagMxpDash(){
        Assertions.assertTrue(Algorithm.HSDAG_MXP.matchesName("HSDAG-MXP"));
    }

}
