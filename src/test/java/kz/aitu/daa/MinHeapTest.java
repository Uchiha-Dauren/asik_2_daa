package kz.aitu.daa;

import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
import java.util.PriorityQueue;
import java.util.Random;

import static kz.aitu.daa.DynamicArrayTest.assertMetrics;
import static org.junit.jupiter.api.Assertions.*;

class MinHeapTest {
    @Test
    void handlesEmptySingletonDuplicatesAndExtremeValues() throws Exception {
        MinHeap heap = new MinHeap();
        assertThrows(IllegalStateException.class, heap::peekMin);
        assertThrows(IllegalStateException.class, heap::extractMin);
        int[] input = {7, 7, Integer.MAX_VALUE, Integer.MIN_VALUE, 0};
        int[] expected = {Integer.MIN_VALUE, 0, 7, 7, Integer.MAX_VALUE};
        for (int value : input) {
            heap.insert(value);
            assertHeapProperty(heap);
        }
        for (int value : expected) {
            assertEquals(value, heap.peekMin());
            assertEquals(value, heap.extractMin());
            assertHeapProperty(heap);
        }
        assertEquals(0, heap.size());
        assertThrows(IllegalStateException.class, heap::extractMin);
        heap.insert(42);
        assertEquals(42, heap.peekMin());
        assertEquals(42, heap.extractMin());
        assertHeapProperty(heap);
    }

    @Test
    void matchesPriorityQueueAfterEveryRandomOperation() throws Exception {
        MinHeap actual = new MinHeap();
        PriorityQueue<Integer> expected = new PriorityQueue<>();
        Random random = new Random(42);
        for (int i = 0; i < 5_000; i++) {
            if (expected.isEmpty() || random.nextInt(3) != 0) {
                int value = random.nextInt(201) - 100;
                actual.insert(value);
                expected.add(value);
            } else {
                assertEquals(expected.remove().intValue(), actual.extractMin());
            }
            assertHeapProperty(actual);
            assertEquals(expected.size(), actual.size());
            if (!expected.isEmpty()) {
                assertEquals(expected.peek().intValue(), actual.peekMin());
            }
        }
        while (!expected.isEmpty()) {
            assertEquals(expected.remove().intValue(), actual.extractMin());
            assertHeapProperty(actual);
        }
    }

    @Test
    void extractsRandomInputInSortedOrder() throws Exception {
        MinHeap heap = new MinHeap();
        Random random = new Random(42);
        for (int i = 0; i < 2_000; i++) {
            heap.insert(random.nextInt());
            assertHeapProperty(heap);
        }
        int previous = Integer.MIN_VALUE;
        while (heap.size() > 0) {
            int current = heap.extractMin();
            assertTrue(previous <= current);
            previous = current;
            assertHeapProperty(heap);
        }
    }

    @Test
    void countsSwapsAndArrayReadsExactly() {
        MinHeap heap = new MinHeap();
        heap.insert(3);
        heap.metrics().reset();
        heap.insert(1);
        assertMetrics(heap.metrics(), 2, 2, 1);
        heap.insert(2);
        heap.metrics().reset();
        assertEquals(1, heap.peekMin());
        assertMetrics(heap.metrics(), 1, 0, 0);
        heap.metrics().reset();
        assertEquals(1, heap.extractMin());
        assertMetrics(heap.metrics(), 4, 1, 1);
        heap.metrics().reset();
        assertEquals(2, heap.extractMin());
        assertMetrics(heap.metrics(), 2, 1, 0);
        heap.metrics().reset();
        assertEquals(3, heap.extractMin());
        assertMetrics(heap.metrics(), 1, 0, 0);
    }

    private static void assertHeapProperty(MinHeap heap) throws Exception {
        Field field = MinHeap.class.getDeclaredField("values");
        field.setAccessible(true);
        int[] values = (int[]) field.get(heap);
        for (int child = 1; child < heap.size(); child++) {
            assertTrue(values[(child - 1) / 2] <= values[child], "child " + child);
        }
    }
}
