package kz.aitu.daa;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.*;

class BenchmarkTest {
    @Test
    void medianUsesMiddleValueWithoutChangingInput() {
        long[] input = {90, 5, 20, 10, 30};
        assertEquals(20, Benchmark.median(input));
        assertArrayEquals(new long[]{90, 5, 20, 10, 30}, input);
    }

    @Test
    void queriesAreReproducibleAndSearchHasEqualHitsAndMisses() {
        for (int n : Benchmark.SIZES) {
            int[] data = Benchmark.data(n);
            assertArrayEquals(data, Benchmark.data(n));
            int[] indices = Benchmark.queries("W1", data);
            assertEquals(10_000, indices.length);
            for (int index : indices) {
                assertTrue(index >= 0 && index < n);
            }
            int[] sorted = data.clone();
            Arrays.sort(sorted);
            int[] queries = Benchmark.queries("W2", data);
            assertArrayEquals(queries, Benchmark.queries("W2", data));
            assertEquals(1_000, queries.length);
            int hits = 0;
            for (int value : queries) {
                if (Arrays.binarySearch(sorted, value) >= 0) {
                    hits++;
                }
            }
            assertEquals(500, hits);
            assertEquals(1_000, Benchmark.queries("W3", data).length);
        }
    }
}
