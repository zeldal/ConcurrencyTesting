package armada.gyte.concurrency.hashset;

import armada.gyte.concurrency.lists.Node;
import armada.gyte.concurrency.lists.Window;

import java.util.concurrent.atomic.AtomicMarkableReference;

/**
 * Created by IntelliJ IDEA.
 * User: ELESSAR
 * Date: 01.04.2011
 * Time: 00:38
 * To change this template use File | Settings | File Templates.
 */
public class BucketList<T> {
    static final int HI_MASK = 0x00800000;
    static final int MASK = 0x00FFFFFF;
    Node<T> head;

    public BucketList() {
        head = new Node<T>(0);
        head.next = new AtomicMarkableReference<Node<T>>(new Node<T>(Integer.MAX_VALUE), false);
    }

    public BucketList(Node<T> head) {
        this.head = head;
    }

    public int makeOrdinaryKey(T x) {
        int code = x.hashCode() & MASK; // take 3 lowest bytes
        return Integer.reverse(code | HI_MASK);
    }

    private static int makeSentinelKey(int key) {
        return Integer.reverse(key & MASK);
    }

    public boolean contains(T x) {
        int key = makeOrdinaryKey(x);
        Window<T> window = find(head, key);
        Node<T> pred = window.pred;
        Node<T> curr = window.curr;
        return (curr.key == key);
    }

    public BucketList<T> getSentinel(int index) {
        int key = makeSentinelKey(index);
        boolean splice;
        while (true) {
            Window<T> window = find(head, key);
            Node<T> pred = window.pred;
            Node<T> curr = window.curr;
            if (curr.key == key) {
                return new BucketList<T>(curr);
            } else {
                Node<T> node = new Node<T>(key);
                node.next.set(pred.next.getReference(), false);
                splice = pred.next.compareAndSet(curr, node, false, false);
                if (splice)
                    return new BucketList<T>(node);
                else
                    continue;
            }
        }
    }

    public Window<T> find(Node<T> head, int key) {
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
                    return new Window<T>(pred, curr);
                pred = curr;
                curr = succ;
            }
        }
    }

    public boolean add(T item) {
        int key = item.hashCode();
        while (true) {
            Window<T> window = find(head, key);
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

}
