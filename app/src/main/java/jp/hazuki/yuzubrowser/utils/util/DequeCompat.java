package jp.hazuki.yuzubrowser.utils.util;

import java.util.Iterator;

public interface DequeCompat<T> {
    void addFirst(T e);

    void addLast(T e);

    boolean offerFirst(T e);

    boolean offerLast(T e);

    T removeFirst();

    T removeLast();

    T pollFirst();

    T pollLast();

    T getFirst();

    T getLast();

    T peekFirst();

    T peekLast();

    boolean removeFirstOccurrence(Object o);

    boolean removeLastOccurrence(Object o);

    boolean add(T e);

    boolean offer(T e);

    T remove();

    T poll();

    T element();

    T peek();

    void push(T e);

    T pop();

    boolean remove(Object o);

    boolean contains(Object o);

    int size();

    Iterator<T> iterator();

    Iterator<T> descendingIterator();
}
