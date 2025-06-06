package deque;

import java.util.Iterator;

public class ArrayDeque<T> implements Deque<T>, Iterable<T> {
    private final int DEFAULT_SIZE = 8;
    private T[] items;
    private int head;
    private int tail;
    private int size;

    public ArrayDeque() {
        items = (T[]) new Object[DEFAULT_SIZE];
        head = 0;
        tail = 1;
        size = 0;
    }

    @Override
    public void addFirst(T item) {
        checkSize();
        items[head] = item;
        head = nextHead(head);
        ++size;
    }

    @Override
    public void addLast(T item) {
        checkSize();
        items[tail] = item;
        tail = nextTail(tail);
        ++size;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void printDeque() {
        var sb = new StringBuilder("< ");
        for (int i = nextTail(head); i != tail; i = nextTail(i)) {
            sb.append(items[i]);
            sb.append(" ");
        }
        sb.append(">");
        System.out.println(sb);
    }

    @Override
    public T removeFirst() {
        checkSize();
        if (size == 0) {
            return null;
        }
        head = nextTail(head);
        --size;
        return items[head];
    }

    @Override
    public T removeLast() {
        checkSize();
        if (size == 0) {
            return null;
        }
        tail = nextHead(tail);
        --size;
        return items[tail];
    }

    @Override
    public T get(int index) {
        if (index < 0 || index >= size) {
            return null;
        }
        var i = (head + 1 + index) % items.length;
        return items[i];
    }

    @Override
    public Iterator<T> iterator() {
        return new ArrayDequeIterator();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Deque)) {
            return false;
        }
        var o = (Deque<T>) obj;
        if (size() != o.size()) {
            return false;
        }
        for (int i = 0; i < size; ++i) {
            if (!get(i).equals(o.get(i))) {
                return false;
            }
        }
        return true;
    }

    private void resize(int capacity) {
        var newItems = (T[]) new Object[capacity];
        for (int i = 0; i < size; ++i) {
            newItems[i] = get(i);
        }
        head = capacity - 1;
        tail = size;
        items = newItems;
    }

    private int nextHead(int i) {
        return Math.floorMod(i - 1, items.length);
    }

    private int nextTail(int i) {
        return (i + 1) % items.length;
    }

    private void checkSize() {
        if (size == items.length) {
            resize(2 * size);
        } else if (size > 4 && size < items.length / 4) {
            resize(items.length / 4);
        }
    }

    private class ArrayDequeIterator implements Iterator<T> {
        private int start;
        private final int end;

        ArrayDequeIterator() {
            start = nextTail(head);
            end = tail;
        }

        @Override
        public boolean hasNext() {
            return start != end;
        }

        @Override
        public T next() {
            var item = items[start];
            start = nextTail(start);
            return item;
        }
    }

}
