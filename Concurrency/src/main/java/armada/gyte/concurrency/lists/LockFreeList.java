package armada.gyte.concurrency.lists;

import java.util.concurrent.atomic.AtomicMarkableReference;

/**
 * Created by IntelliJ IDEA.
 * User: ELESSAR
 * Date: 01.04.2011
 * Time: 00:13
 * To change this template use File | Settings | File Templates.
 */
public class LockFreeList<T> {

    private Node<T> head;

    public LockFreeList() {
        head = new Node<T>(Integer.MIN_VALUE);
        head.next = new AtomicMarkableReference<Node<T>>(new Node<T>(Integer.MAX_VALUE), false);
    }

    public Window find(Node<T> head, int key) {
        Node<T> pred = null, curr = null, succ = null;
        boolean[] marked = {false};
        boolean snip;
        retry:
        while (true) {
            pred = head;
            curr = pred.next.getReference();
            while (true) {
                succ = curr.next.get(marked);
                while (marked[0]) {
                    snip = pred.next.compareAndSet(curr, succ, false, false);
                    if (!snip) continue retry;
                    curr = succ;
                    succ = curr.next.get(marked);
                }
                if (curr.key >= key)
                    return new Window(pred, curr);
                pred = curr;
                curr = succ;
            }
        }
    }

    public boolean add(T item) {
        int key = item.hashCode();
        while (true) {
            Window window = find(head, key);
            Node<T> pred = window.pred, curr = window.curr;
            if (curr.key == key) {
                return false;
            } else {
                Node<T> node = new Node<T>(item);
                node.next = new AtomicMarkableReference<Node<T>>(curr, false);
                if (pred.next.compareAndSet(curr, node, false, false)) {
                    return true;
                }
            }
        }
    }

    public boolean remove(T item) {
        int key = item.hashCode();
        boolean snip;
        while (true) {
            Window window = find(head, key);
            Node<T> pred = window.pred, curr = window.curr;
            if (curr.key != key) {
                return false;
            } else {
                Node<T> succ = curr.next.getReference();
                snip = curr.next.attemptMark(succ, true);
                if (!snip)
                    continue;
                pred.next.compareAndSet(curr, succ, false, false);
                return true;
            }
        }
    }

    public boolean contains(T item) {
        boolean[] marked = {false};
        int key = item.hashCode();
        Node<T> curr = head;
        while (curr.key < key) {
            curr = curr.next.getReference();
            Node succ = curr.next.get(marked);
        }
        return (curr.key == key && !marked[0]);
    }
}
