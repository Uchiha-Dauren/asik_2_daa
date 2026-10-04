package kz.aitu.daa;

public final class MinHeap {
    private int[] values = new int[8];
    private int size;
    private final Metrics metrics = new Metrics();

    public void insert(int value) {
        ensureCapacity();
        int index = size++;
        values[index] = value;
        while (index > 0) {
            int parent = (index - 1) / 2;
            int parentValue = read(parent);
            int childValue = read(index);
            metrics.compare();
            if (parentValue <= childValue) {
                break;
            }
            swap(index, parent, childValue, parentValue);
            index = parent;
        }
    }

    public int peekMin() {
        checkNotEmpty();
        return read(0);
    }

    public int extractMin() {
        checkNotEmpty();
        int minimum = read(0);
        size--;
        if (size > 0) {
            values[0] = read(size);
            metrics.move();
            bubbleDown();
        }
        return minimum;
    }

    public int size() {
        return size;
    }

    public Metrics metrics() {
        return metrics;
    }

    private void bubbleDown() {
        int index = 0;
        while (index < size / 2) {
            int child = 2 * index + 1;
            int childValue = read(child);
            int right = child + 1;
            if (right < size) {
                int rightValue = read(right);
                metrics.compare();
                if (rightValue < childValue) {
                    child = right;
                    childValue = rightValue;
                }
            }
            int parentValue = read(index);
            metrics.compare();
            if (parentValue <= childValue) {
                break;
            }
            swap(index, child, parentValue, childValue);
            index = child;
        }
    }

    private void swap(int first, int second, int firstValue, int secondValue) {
        values[first] = secondValue;
        metrics.move();
        values[second] = firstValue;
        metrics.move();
    }

    private int read(int index) {
        metrics.step();
        return values[index];
    }

    private void ensureCapacity() {
        if (size < values.length) {
            return;
        }
        int[] expanded = new int[Math.multiplyExact(values.length, 2)];
        for (int i = 0; i < size; i++) {
            expanded[i] = read(i);
            metrics.move();
        }
        values = expanded;
    }

    private void checkNotEmpty() {
        if (size == 0) {
            throw new IllegalStateException("Heap is empty");
        }
    }
}
