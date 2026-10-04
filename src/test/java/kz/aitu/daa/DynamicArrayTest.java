package kz.aitu.daa;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DynamicArrayTest extends IntListTest {
    @Override
    IntList createList() {
        return new DynamicArray();
    }

    @Test
    void preservesValuesAcrossRepeatedDoubling() {
        DynamicArray array = new DynamicArray();
        for (int i = 0; i < 10_000; i++) {
            array.add(i);
        }
        for (int i = 0; i < array.size(); i++) {
            assertEquals(i, array.get(i));
        }
    }

    @Test
    void countsReadsShiftsComparisonsAndResizingExactly() {
        DynamicArray array = new DynamicArray();
        for (int i = 0; i < 8; i++) {
            array.add(i);
        }
        array.metrics().reset();
        array.add(8);
        assertMetrics(array.metrics(), 8, 8, 0);
        array.metrics().reset();
        assertEquals(5, array.get(5));
        assertMetrics(array.metrics(), 1, 0, 0);
        array.metrics().reset();
        array.add(0, 99);
        assertMetrics(array.metrics(), 9, 9, 0);
        array.metrics().reset();
        assertEquals(99, array.remove(0));
        assertMetrics(array.metrics(), 10, 9, 0);
        array.metrics().reset();
        assertTrue(array.contains(2));
        assertMetrics(array.metrics(), 3, 0, 3);
        array.metrics().reset();
        assertFalse(array.contains(-1));
        assertMetrics(array.metrics(), 9, 0, 9);
        array.metrics().reset();
        assertMetrics(array.metrics(), 0, 0, 0);
    }

    static void assertMetrics(Metrics metrics, long steps, long moves, long comparisons) {
        assertEquals(steps, metrics.steps(), "steps");
        assertEquals(moves, metrics.moves(), "moves");
        assertEquals(comparisons, metrics.comparisons(), "comparisons");
    }
}
