package datastructures;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A simple generic singly linked list (head + tail pointers).
 * Used to store students, faculty, courses and fee records.
 */
public class SinglyLinkedList<T> implements Iterable<T> {

    private static class Node<T> {
        T data;
        Node<T> next;

        Node(T data) {
            this.data = data;
        }
    }

    private Node<T> head;
    private Node<T> tail;
    private int size;

    public void addLast(T item) {
        Node<T> node = new Node<>(item);
        if (head == null) {
            head = node;
            tail = node;
        } else {
            tail.next = node;
            tail = node;
        }
        size++;
    }

    /** Inserts an item at the given position (used by Undo to restore deleted records). */
    public void addAt(int index, T item) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index: " + index);
        }
        if (index == size) {
            addLast(item);
            return;
        }
        Node<T> node = new Node<>(item);
        if (index == 0) {
            node.next = head;
            head = node;
        } else {
            Node<T> previous = nodeAt(index - 1);
            node.next = previous.next;
            previous.next = node;
        }
        size++;
    }

    public T get(int index) {
        return nodeAt(index).data;
    }

    public T removeAt(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index);
        }
        Node<T> removed;
        if (index == 0) {
            removed = head;
            head = head.next;
            if (head == null) {
                tail = null;
            }
        } else {
            Node<T> previous = nodeAt(index - 1);
            removed = previous.next;
            previous.next = removed.next;
            if (removed == tail) {
                tail = previous;
            }
        }
        size--;
        return removed.data;
    }

    public int indexOf(T item) {
        int index = 0;
        for (Node<T> current = head; current != null; current = current.next) {
            if (current.data.equals(item)) {
                return index;
            }
            index++;
        }
        return -1;
    }

    public boolean contains(T item) {
        return indexOf(item) >= 0;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void clear() {
        head = null;
        tail = null;
        size = 0;
    }

    private Node<T> nodeAt(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index);
        }
        Node<T> current = head;
        for (int i = 0; i < index; i++) {
            current = current.next;
        }
        return current;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private Node<T> current = head;

            @Override
            public boolean hasNext() {
                return current != null;
            }

            @Override
            public T next() {
                if (current == null) {
                    throw new NoSuchElementException();
                }
                T data = current.data;
                current = current.next;
                return data;
            }
        };
    }
}
