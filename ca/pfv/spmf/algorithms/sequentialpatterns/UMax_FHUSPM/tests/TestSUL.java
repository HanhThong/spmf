package ca.pfv.spmf.algorithms.sequentialpatterns.UMax_FHUSPM.tests;

import ca.pfv.spmf.algorithms.sequentialpatterns.UMax_FHUSPM.SUL;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

public class TestSUL {
    @Test
    public void testAddRecord() throws Exception {
        SUL sul = new SUL();
        sul.addRecord(0, "1[1] 2[4] -1 3[10] -1 6[9] -1 7[2] -1 1[1] -1 -2 SUtility:27");
        Assertions.assertEquals(sul.dbUtility, 27);
    }
}
