package armada.gyte.concurrency.hashset;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicMarkableReference;

/**
 * Created by IntelliJ IDEA.
 * User: ELESSAR
 * Date: 01.04.2011
 * Time: 00:48
 * To change this template use File | Settings | File Templates.
 */
public class LockFreeHashSet<T> {
    public static int THRESHOLD = 16; // check
    public static int SEGMENTSIZE = 4096; // check
    public static int MAXCAPACITYINLOG = 64; // check
    //    protected BucketList<T>[] bucket;
    protected AtomicInteger bucketSize;
    protected AtomicInteger setSize;
    protected AtomicMarkableReference<BucketList<T>[]>[] firstSegment;

    public LockFreeHashSet() {
        firstSegment = new AtomicMarkableReference[MAXCAPACITYINLOG];
        BucketList<T>[] bucket = new BucketList[SEGMENTSIZE];
        bucket[0] = new BucketList<T>();
        firstSegment[0] = new AtomicMarkableReference<BucketList<T>[]>(bucket, true);
        for (int i = 1; i < firstSegment.length; i++) {
            firstSegment[i] = new AtomicMarkableReference<BucketList<T>[]>(null, true);
        }
        bucketSize = new AtomicInteger(2);
        setSize = new AtomicInteger(0);
    }

    public boolean add(T x) {
        int myBucket = x.hashCode() % bucketSize.get();
        BucketList<T> b = getBucketList(myBucket);
        if (!b.add(x))
            return false;
        int setSizeNow = setSize.getAndIncrement();
        int bucketSizeNow = bucketSize.get();
        if (setSizeNow / bucketSizeNow > THRESHOLD)
            bucketSize.compareAndSet(bucketSizeNow, 2 * bucketSizeNow);
        return true;
    }

    private BucketList<T> getBucketList(int myBucket) {
        double segment = myBucket / SEGMENTSIZE;
        int segmentNo = (int) segment;
        int bucketNo = myBucket % ( SEGMENTSIZE*segmentNo);
        BucketList<T>[] bucket = getBuckeListRef(segmentNo);
        if (bucket[bucketNo] == null)
            initializeBucket(bucket, bucketNo, myBucket);
        return bucket[bucketNo];
    }

    private BucketList<T>[] getBuckeListRef(int segmentNo) {

        while (true) {
            if (firstSegment[segmentNo].getReference() != null)
                return firstSegment[segmentNo].getReference();
            else {
                BucketList<T>[] bucket = new BucketList[SEGMENTSIZE];
                if (firstSegment[segmentNo].compareAndSet(null, bucket, true, true))
                    return firstSegment[segmentNo].getReference();
                else
                    continue;
            }
        }

    }

    private void initializeBucket(BucketList<T>[] bucket, int bucketNo, int myBucket) {
        int parent = getParent(myBucket);
        double segment = parent / SEGMENTSIZE;
        int segmentNo = (int) segment;
        int parentBucketNo = parent% (SEGMENTSIZE*segmentNo);
        BucketList<T>[] parentBuckeListRef = getBuckeListRef(segmentNo);
        if (parentBuckeListRef[parentBucketNo] == null)
            initializeBucket(parentBuckeListRef, parentBucketNo, bucketNo);
        BucketList<T> b = bucket[parent].getSentinel(myBucket);
        if (b != null)
            bucket[bucketNo] = b;
    }

    private int getParent(int myBucket) {
        int parent = bucketSize.get();
        do {
            parent = parent >> 1;
        } while (parent > myBucket);
        parent = myBucket - parent;
        return parent;
    }

}


