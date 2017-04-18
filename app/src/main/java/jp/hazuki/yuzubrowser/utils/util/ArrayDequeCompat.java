package jp.hazuki.yuzubrowser.utils.util;

import android.support.annotation.NonNull;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

public class ArrayDequeCompat<T> extends LinkedList<T> implements DequeCompat<T> {
    private static final long serialVersionUID = -1564802120641280932L;

    public static final class ArrayDequeCompatNew<T> extends ArrayDeque<T> implements DequeCompat<T> {
        private static final long serialVersionUID = -2031317301850371201L;

        public ArrayDequeCompatNew() {
            super();
        }

        public ArrayDequeCompatNew(int numElements) {
            super(numElements);
        }

        public ArrayDequeCompatNew(Collection<? extends T> c) {
            super(c);
        }
    }

    public static <T> DequeCompat<T> makeDeque() {
        return new ArrayDequeCompatNew<>();
    }

    public ArrayDequeCompat(int numElements) {
        super();// TODO numElements
    }

    public ArrayDequeCompat(Collection<? extends T> c) {
        super(c);
    }

    @Override
    public boolean offerFirst(T e) {
        addFirst(e);
        return true;
    }

    @Override
    public boolean offerLast(T e) {
        addLast(e);
        return true;
    }

    @Override
    public T pollFirst() {
        if (isEmpty())
            return null;
        return removeFirst();
    }

    @Override
    public T pollLast() {
        if (isEmpty())
            return null;
        return removeLast();
    }

    @Override
    public T peekFirst() {
        if (isEmpty())
            return null;
        return getFirst();
    }

    @Override
    public T peekLast() {
        if (isEmpty())
            return null;
        return getLast();
    }

    @Override
    public boolean removeFirstOccurrence(Object o) {
        return remove(o);
    }

    @Override
    public boolean removeLastOccurrence(Object o) {
        int i = lastIndexOf(o);
        if (i >= 0) {
            remove(i);
            return true;
        }
        return false;
    }

    @Override
    public void push(T e) {
        addFirst(e);
    }

    @Override
    public T pop() {
        return removeFirst();
    }

    @NonNull
    @Override
    public Iterator<T> descendingIterator() {
        return iterator();
    }
}
