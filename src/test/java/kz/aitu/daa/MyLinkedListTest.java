package kz.aitu.daa;

import org.junit.jupiter.api.Test;
import static kz.aitu.daa.DynamicArrayTest.assertMetrics;
import static org.junit.jupiter.api.Assertions.*;

class MyLinkedListTest extends IntListTest {
    @Override
    IntList createList() {
        return new MyLinkedList();
    }

    @Test
    void repairsTailAfterLastElementRemoval() {
        MyLinkedList list = new MyLinkedList();
        list.add(10);
        list.add(20);
        assertEquals(20, list.remove(1));
        list.add(30);
        assertEquals(30, list.get(1));
        list.remove(1);
        list.remove(0);
        list.add(40);
        assertEquals(40, list.get(0));
    }

    @Test
    void countsTraversalAndPersistentLinkUpdatesExactly() {
        MyLinkedList list = new MyLinkedList();
        list.add(10);
        assertMetrics(list.metrics(), 0, 2, 0);
        list.add(20);
        list.add(30);
        list.metrics().reset();
        assertEquals(30, list.get(2));
        assertMetrics(list.metrics(), 2, 0, 0);
        list.metrics().reset();
        list.add(0, 5);
        assertMetrics(list.metrics(), 0, 2, 0);
        list.metrics().reset();
        assertEquals(5, list.remove(0));
        assertMetrics(list.metrics(), 1, 1, 0);
        list.metrics().reset();
        list.add(1, 15);
        assertMetrics(list.metrics(), 1, 2, 0);
        list.metrics().reset();
        assertEquals(15, list.remove(1));
        assertMetrics(list.metrics(), 2, 1, 0);
        list.metrics().reset();
        assertTrue(list.contains(30));
        assertMetrics(list.metrics(), 2, 0, 3);
        list.metrics().reset();
        assertFalse(list.contains(40));
        assertMetrics(list.metrics(), 3, 0, 3);
    }
}
