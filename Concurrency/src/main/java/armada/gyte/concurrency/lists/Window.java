package armada.gyte.concurrency.lists;

/**
* Created by IntelliJ IDEA.
* User: ELESSAR
* Date: 01.04.2011
* Time: 00:43
* To change this template use File | Settings | File Templates.
*/
public class Window<T> {

    public Node<T> pred, curr;

    public Window(Node<T> myPred, Node<T> myCurr) {
        pred = myPred;
        curr = myCurr;
    }

}
