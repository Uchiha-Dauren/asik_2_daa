package kz.aitu.daa;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Random;

public final class Benchmark {
    static final int[] SIZES = {100, 1_000, 10_000, 100_000};
    private static final int WARMUP_RUNS = 5;
    private static final int MEASURED_RUNS = 5;
    private static volatile long sink;

    record Result(String workload, String variant, String structure, int n,
                  double timeMs, long steps, long moves, long comparisons) { }

    private record Sample(long nanos, long steps, long moves, long comparisons) { }

    private Benchmark() { }

    public static void main(String[] args) throws IOException {
        Path output = Path.of(args.length == 0 ? "results" : args[0]);
        Files.createDirectories(output);
        warmUpJvm();
        Result[] results = new Result[SIZES.length * 9];
        int count = 0;
        for (int n : SIZES) {
            int[] data = data(n);
            for (String workload : new String[]{"W1", "W2", "W3", "W4"}) {
                String[] variants = workload.equals("W3")
                        ? new String[]{"head", "middle"} : new String[]{"-"};
                String[] structures = workload.equals("W4")
                        ? new String[]{"MinHeap"} : new String[]{"DynamicArray", "MyLinkedList"};
                for (String variant : variants) {
                    for (String structure : structures) {
                        results[count++] = measure(workload, variant, structure, data);
                    }
                }
            }
            System.out.println("Completed n=" + n);
        }
        writeCsv(output.resolve("results.csv"), results);
        Plotter.write(output.resolve("plots"), results);
        System.out.println("Saved 36 cases and 4 plots to " + output.toAbsolutePath());
        System.out.println("Java " + System.getProperty("java.version") + ", "
                + System.getProperty("os.name") + " " + System.getProperty("os.arch"));
    }

    private static void warmUpJvm() {
        int[] data = data(1_000);
        int[] access = queries("W1", data);
        int[] search = queries("W2", data);
        int[] updates = queries("W3", data);
        for (int run = 0; run < 100; run++) {
            for (String structure : new String[]{"DynamicArray", "MyLinkedList"}) {
                runList("W1", "-", structure, data, access);
                runList("W2", "-", structure, data, search);
                runList("W3", "head", structure, data, updates);
                runList("W3", "middle", structure, data, updates);
            }
            runHeap(data);
        }
    }

    static int[] data(int n) {
        Random random = new Random(42);
        int[] values = new int[n];
        for (int i = 0; i < n; i++) {
            values[i] = 2 * random.nextInt(1_000_000);
        }
        return values;
    }

    static int[] queries(String workload, int[] data) {
        Random random = new Random(42);
        int[] queries = new int[workload.equals("W1") ? 10_000 : 1_000];
        for (int i = 0; i < queries.length; i++) {
            queries[i] = switch (workload) {
                case "W1" -> random.nextInt(data.length);
                case "W2" -> i % 2 == 0
                        ? data[random.nextInt(data.length)] : -1 - random.nextInt(1_000_000);
                case "W3" -> random.nextInt();
                default -> throw new IllegalArgumentException(workload);
            };
        }
        return queries;
    }

    private static Result measure(String workload, String variant, String structure, int[] data) {
        int[] queries = workload.equals("W4") ? new int[0] : queries(workload, data);
        long[] times = new long[MEASURED_RUNS];
        Sample reference = null;
        for (int run = -WARMUP_RUNS; run < MEASURED_RUNS; run++) {
            Sample sample = workload.equals("W4")
                    ? runHeap(data) : runList(workload, variant, structure, data, queries);
            if (run >= 0) {
                times[run] = sample.nanos();
                if (reference != null && (sample.steps() != reference.steps()
                        || sample.moves() != reference.moves()
                        || sample.comparisons() != reference.comparisons())) {
                    throw new IllegalStateException("Non-reproducible counters");
                }
                reference = sample;
            }
        }
        return new Result(workload, variant, structure, data.length,
                median(times) / 1_000_000.0,
                reference.steps(), reference.moves(), reference.comparisons());
    }

    private static Sample runList(String workload, String variant, String structure,
                                  int[] data, int[] queries) {
        IntList list = structure.equals("DynamicArray") ? new DynamicArray() : new MyLinkedList();
        for (int value : data) {
            list.add(value);
        }
        int[] removed = new int[1_000];
        long checksum = 0;
        list.metrics().reset();
        long start = System.nanoTime();
        switch (workload) {
            case "W1" -> {
                for (int index : queries) {
                    checksum += list.get(index);
                }
            }
            case "W2" -> {
                for (int value : queries) {
                    if (list.contains(value)) {
                        checksum++;
                    }
                }
            }
            case "W3" -> {
                int index = variant.equals("head") ? 0 : data.length / 2;
                for (int value : queries) {
                    list.add(index, value);
                }
                for (int i = 0; i < removed.length; i++) {
                    removed[i] = list.remove(index);
                    checksum += removed[i];
                }
            }
            default -> throw new IllegalArgumentException(workload);
        }
        long elapsed = System.nanoTime() - start;
        Sample sample = snapshot(elapsed, list.metrics());
        sink = checksum;
        if (workload.equals("W1")) {
            long expected = 0;
            for (int index : queries) {
                expected += data[index];
            }
            require(checksum == expected, "Random access checksum");
        } else if (workload.equals("W2")) {
            require(checksum == 500, "Search must find exactly 500 values");
        } else {
            require(list.size() == data.length, "Insert/remove size");
            for (int i = 0; i < removed.length; i++) {
                require(removed[i] == queries[queries.length - 1 - i], "Removal order");
            }
            require(list.get(0) == data[0] && list.get(data.length - 1) == data[data.length - 1]
                    && list.get(data.length / 2) == data[data.length / 2], "Restored values");
        }
        return sample;
    }

    private static Sample runHeap(int[] data) {
        MinHeap heap = new MinHeap();
        int[] sorted = new int[data.length];
        long start = System.nanoTime();
        for (int value : data) {
            heap.insert(value);
        }
        for (int i = 0; i < sorted.length; i++) {
            sorted[i] = heap.extractMin();
        }
        long elapsed = System.nanoTime() - start;
        Sample sample = snapshot(elapsed, heap.metrics());
        for (int i = 1; i < sorted.length; i++) {
            require(sorted[i - 1] <= sorted[i], "Heap output is not sorted");
        }
        require(heap.size() == 0, "Heap must be empty");
        sink = sorted.length == 0 ? 0 : sorted[0];
        return sample;
    }

    private static Sample snapshot(long nanos, Metrics metrics) {
        return new Sample(nanos, metrics.steps(), metrics.moves(), metrics.comparisons());
    }

    static long median(long[] values) {
        long[] sorted = values.clone();
        for (int i = 1; i < sorted.length; i++) {
            long value = sorted[i];
            int j = i - 1;
            while (j >= 0 && sorted[j] > value) {
                sorted[j + 1] = sorted[j];
                j--;
            }
            sorted[j + 1] = value;
        }
        return sorted[sorted.length / 2];
    }

    private static void writeCsv(Path path, Result[] results) throws IOException {
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(path, StandardCharsets.UTF_8))) {
            writer.println("workload,variant,structure,n,time_ms,steps,moves,comparisons");
            for (Result result : results) {
                writer.printf(Locale.ROOT, "%s,%s,%s,%d,%.6f,%d,%d,%d%n",
                        result.workload(), result.variant(), result.structure(), result.n(),
                        result.timeMs(), result.steps(), result.moves(), result.comparisons());
            }
            if (writer.checkError()) {
                throw new IOException("Could not write " + path);
            }
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
