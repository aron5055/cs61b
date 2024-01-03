package tester;

import static org.junit.Assert.*;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;
import student.StudentArrayDeque;

public class TestArrayDequeEC {
    @Test
    public void randomTestAdd() {
        var sad1 = new StudentArrayDeque<Integer>();
        var ad1 = new ArrayDequeSolution<Integer>();

        for (int i = 0; i < 100; i += 1) {
            double numberBetweenZeroAndOne = StdRandom.uniform();

            if (numberBetweenZeroAndOne < 0.5) {
                sad1.addLast(i);
                ad1.addLast(i);
                assertEquals("addLast(" + i + ")", ad1.get(i), sad1.get(i));
            } else {
                sad1.addFirst(i);
                ad1.addFirst(i);
                assertEquals("addFirst(" + i + ")", ad1.get(0), sad1.get(0));
            }
        }
    }

    @Test
    public void randomTestRemove() {
        var sad1 = new StudentArrayDeque<Integer>();
        var ad1 = new ArrayDequeSolution<Integer>();

        for (int i = 0; i < 100; i += 1) {
            double numberBetweenZeroAndOne = StdRandom.uniform();

            if (numberBetweenZeroAndOne < 0.5) {
                sad1.addLast(i);
                ad1.addLast(i);
            } else {
                sad1.addFirst(i);
                ad1.addFirst(i);
            }
        }

        for (int i = 0; i < 100; i += 1) {
            double numberBetweenZeroAndOne = StdRandom.uniform();

            if (numberBetweenZeroAndOne < 0.5) {
                Integer expected = ad1.removeLast();
                Integer actual = sad1.removeLast();
                var message = "removeLast(), student was " + actual + ", correct was " + expected;
                assertEquals(message, expected, actual);
            } else {
                Integer expected = ad1.removeFirst();
                Integer actual = sad1.removeFirst();
                var message = "removeFirst(), student was " + actual + ", correct was " + expected;
                assertEquals(message, expected, actual);
            }
        }
    }

    @Test
    public void randomTestSize() {
        var sad1 = new StudentArrayDeque<Integer>();
        var ad1 = new ArrayDequeSolution<Integer>();

        for (int i = 0; i < 100; i += 1) {
            double numberBetweenZeroAndOne = StdRandom.uniform();

            if (numberBetweenZeroAndOne < 0.5) {
                sad1.addLast(i);
                ad1.addLast(i);
                assertEquals("size()", ad1.size(), sad1.size());
            } else {
                sad1.addFirst(i);
                ad1.addFirst(i);
                assertEquals("size()", ad1.size(), sad1.size());
            }
        }
    }

    @Test
    public void randomTestEmpty() {
        var sad1 = new StudentArrayDeque<Integer>();
        var ad1 = new ArrayDequeSolution<Integer>();

        assertEquals("isEmpty()", ad1.isEmpty(), sad1.isEmpty());
        int i = 0;
        sad1.addLast(i);
        ad1.addLast(i);
        assertEquals("isEmpty()", ad1.isEmpty(), sad1.isEmpty());
    }

}
