package armada.gyte.concurrency.lists;

import java.util.concurrent.atomic.AtomicMarkableReference;

/**
 * Created by IntelliJ IDEA.
 * User: ELESSAR
 * Date: 01.04.2011
 * Time: 00:11
 * To change this template use File | Settings | File Templates.
 */
public class Node<T> {
    public Node(int key) {
        this.key = key;
    }

    public Node(T item) {
        this.item = item;
    }

    T item;
    public int key;
    public AtomicMarkableReference<Node<T>> next;
}
