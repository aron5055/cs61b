package deque;

import java.util.Iterator;
import java.util.Optional;

public class LinkedListDeque<T> implements Deque<T>, Iterable<T>{
    private ListNode head;
    private ListNode tail;
    private int size = 0;

    class ListNode {
        private ListNode prev;
        private T data;
        private ListNode next;

        public ListNode() {}

        public ListNode(T item) {
            data = item;
            prev = null;
            next = null;
        }
    }

    public LinkedListDeque() {
        tail = head = new ListNode();
        head.next = tail;
        tail.prev = head;
    }


    @Override
    public void addFirst(T item) {
        var node = new ListNode(item);
        insertBetween(head, tail, node);
        ++ size;
    }

    @Override
    public void addLast(T item) {
        var node = new ListNode(item);
        insertBetween(tail.prev, tail, node);
        ++ size;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void printDeque() {
        var sb = new StringBuilder("< ");
        for (var p = head.next; p != tail; p = p.next) {
            sb.append(p.data);
            sb.append(" ");
        }
        sb.append(">");
        System.out.println(sb);
    }

    @Override
    public T removeFirst() {
        var node = getNode(0).orElse(null);
        if (node != null) {
            deleteBetween(node.prev, node.next);
            var data = node.data;
            clearNode(node);
            -- size;
            return data;
        }
        return null;
    }

    @Override
    public T removeLast() {
        var node = getNode(size - 1).orElse(null);
        if (node != null) {
            deleteBetween(node.prev, node.next);
            var data = node.data;
            clearNode(node);
            -- size;
            return data;
        }
        return null;
    }

    @Override
    public T get(int index) {
        var opt = getNode(index);
        return opt.map(node -> node.data).orElse(null);
    }

    public T getRecursive(int index) {
        if (index < 0 || index >= size) {
            return null;
        }
        return getHelper(head.next, index);
    }

    public Iterator<T> iterator() {
        return new LinkedListIterator();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (o.getClass() != this.getClass()) {
            return false;
        }
        var other = (LinkedListDeque<T>) o;
        if (other.size() != size) {
            return false;
        }
        for (int i = 0; i < size; ++ i) {
            if (this.get(i) != other.get(i)) {
                return false;
            }
        }
        return true;
    }

    class LinkedListIterator implements Iterator<T> {
        private ListNode p;
        public LinkedListIterator() {
            p = head.next;
        }

        @Override
        public boolean hasNext() {
            return p != tail;
        }

        @Override
        public T next() {
            var item = p.data;
            p = p.next;
            return item;
        }
    }

    private T getHelper(ListNode node, int index) {
        if (index == 0) {
            return node.data;
        }
        return getHelper(node.next, index - 1);
    }


    private Optional<ListNode> getNode(int index) {
        if (index >= size || index < 0) {
            return Optional.empty();
        }
        ListNode p;
        if (index <= size / 2) {
            p = head;
            for (int i = 0; i <= index; ++ i) {
                p = p.next;
            }
        } else {
            p = tail;
            for (int i = size - index; i > 0; -- i) {
                p = p.prev;
            }
        }
        return Optional.of(p);
    }

    private void insertBetween(ListNode before, ListNode after, ListNode node) {
        before.next = node;
        node.prev = before;
        node.next = after;
        after.prev = node;
    }

    private void deleteBetween(ListNode before, ListNode after) {
        before.next = after;
        after.prev = before;
    }

    private void clearNode(ListNode node) {
        node.prev = null;
        node.next = null;
    }

}
