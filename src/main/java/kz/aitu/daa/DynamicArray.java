package kz.aitu.daa;

public final class DynamicArray implements IntList {
    private int[] values = new int[8];
    private int size;
    private final Metrics metrics = new Metrics();

    @Override
    public void add(int value) {
        ensureCapacity();
        values[size++] = value;
    }

    @Override
    public void add(int index, int value) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException(index);
        }
        ensureCapacity();
        for (int i = size; i > index; i--) {
            values[i] = read(i - 1);
            metrics.move();
        }
        values[index] = value;
        size++;
    }

    @Override
    public int remove(int index) {
        checkIndex(index);
        int removed = read(index);
        for (int i = index; i < size - 1; i++) {
            values[i] = read(i + 1);
            metrics.move();
        }
        size--;
        return removed;
    }

    @Override
    public int get(int index) {
        checkIndex(index);
        return read(index);
    }

    @Override
    public boolean contains(int value) {
        for (int i = 0; i < size; i++) {
            int current = read(i);
            metrics.compare();
            if (current == value) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public Metrics metrics() {
        return metrics;
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

    private void checkIndex(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException(index);
        }
    }
}
