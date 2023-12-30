package randomizedtest;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by hug.
 */
public class TestBuggyAList {
    @Test
    public void testThreeAddThreeRemove() {
        var testLst = new BuggyAList<Integer>();
        var correctLst = new AListNoResizing<Integer>();
        for (int i = 4; i <= 6; ++ i) {
            testLst.addLast(i);
            correctLst.addLast(i);
        }
        for (int i = 0; i < 3; ++ i) {
            assertEquals(testLst.removeLast(), correctLst.removeLast());
        }
    }

    @Test
    public void randomizedTest() {
        AListNoResizing<Integer> L = new AListNoResizing<>();
        var testLst = new BuggyAList<Integer>();

        int N = 500;
        for (int i = 0; i < N; i += 1) {
            int operationNumber = StdRandom.uniform(0, 3);
            if (operationNumber == 0) {
                // addLast
                int randVal = StdRandom.uniform(0, 100);
                L.addLast(randVal);
                testLst.addLast(randVal);
            } else if (operationNumber == 1) {
                // size
                int size = L.size();
                int testSize = testLst.size();
                assertEquals(testSize, size);
            } else if (operationNumber == 2) {
                int size = L.size();
                int testSize = testLst.size();
                if (size != 0 && testSize != 0) {
                    var val = L.removeLast();
                    var testVal = testLst.removeLast();
                    assertEquals(testVal, val);
                }
            }
        }
    }
}
