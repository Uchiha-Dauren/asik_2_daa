package kz.aitu.daa;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

abstract class IntListTest {
    abstract IntList createList();

    @Test
    void handlesEmptySingletonDuplicatesAndBoundaryIndices() {
        IntList list = createList();
        assertEquals(0, list.size());
        assertFalse(list.contains(7));
        list.add(0, 7);
        assertEquals(7, list.get(0));
        assertEquals(7, list.remove(0));
        list.add(4);
        list.add(0, 4);
        list.add(list.size(), Integer.MIN_VALUE);
        list.add(Integer.MAX_VALUE);
        assertEquals(4, list.remove(0));
        assertEquals(Integer.MAX_VALUE, list.remove(list.size() - 1));
        assertEquals(4, list.get(0));
        assertEquals(Integer.MIN_VALUE, list.get(list.size() - 1));
        assertTrue(list.contains(4));
        assertFalse(list.contains(3));
        while (list.size() > 0) {
            list.remove(0);
        }
        list.add(9);
        assertEquals(9, list.remove(0));
    }

    @Test
    void rejectsInvalidIndicesWithoutChangingContents() {
        IntList list = createList();
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(0));
        assertThrows(IndexOutOfBoundsException.class, () -> list.remove(0));
        assertThrows(IndexOutOfBoundsException.class, () -> list.add(1, 5));
        list.add(10);
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(1));
        assertThrows(IndexOutOfBoundsException.class, () -> list.remove(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> list.remove(1));
        assertThrows(IndexOutOfBoundsException.class, () -> list.add(-1, 5));
        assertThrows(IndexOutOfBoundsException.class, () -> list.add(2, 5));
        assertEquals(1, list.size());
        assertEquals(10, list.get(0));
    }

    @Test
    void matchesArrayListAfterRandomOperations() {
        IntList actual = createList();
        ArrayList<Integer> expected = new ArrayList<>();
        Random random = new Random(42);
        for (int operation = 0; operation < 3_000; operation++) {
            int value = random.nextInt(101) - 50;
            switch (random.nextInt(5)) {
                case 0 -> {
                    actual.add(value);
                    expected.add(value);
                }
                case 1 -> {
                    int index = random.nextInt(expected.size() + 1);
                    actual.add(index, value);
                    expected.add(index, value);
                }
                case 2 -> {
                    if (!expected.isEmpty()) {
                        int index = random.nextInt(expected.size());
                        assertEquals(expected.remove(index).intValue(), actual.remove(index));
                    }
                }
                case 3 -> {
                    if (!expected.isEmpty()) {
                        int index = random.nextInt(expected.size());
                        assertEquals(expected.get(index).intValue(), actual.get(index));
                    }
                }
                case 4 -> assertEquals(expected.contains(value), actual.contains(value));
                default -> throw new AssertionError();
            }
            assertEquals(expected.size(), actual.size());
            for (int i = 0; i < expected.size(); i++) {
                assertEquals(expected.get(i).intValue(), actual.get(i));
            }
        }
    }
}
