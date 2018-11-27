import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author loujianhao
 */
public class MyMQ {
    private LinkedList<Object> list = new LinkedList<>();

    private AtomicInteger count = new AtomicInteger(0);

    private final int minSize = 0;

    private final int maxSize;

    public MyMQ(int maxSize) {
        this.maxSize = maxSize;
    }

    private final Object lock = new Object();

    public void put(Object object) {
        synchronized (lock) {
            if (count.get() == maxSize) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            list.add(object);
            count.incrementAndGet();
            lock.notify();
            System.out.println("new input obj is:" + object);
        }
    }

    public Object take() {
        Object ret = null;
        synchronized (lock) {
            if (count.get() == this.minSize) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            ret = list.removeFirst();
            count.decrementAndGet();
            lock.notify();
        }

        return ret;
    }


    public int Size() {
        return this.count.get();
    }

    public static void main(String[] args) {
        final MyMQ mq = new MyMQ(10);
        mq.put(1);
        mq.put(2);
        mq.put(3);
        mq.put(4);
        mq.put(5);
        mq.put(6);

        System.out.println("mainThread当前容器长度：" + mq.Size());

        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {

                mq.put(7);
                mq.put(8);
                mq.put(9);
                mq.put(10);
                mq.put(11);
                System.out.println("Thread1当前容器长度：" + mq.Size());
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mq.put(12);

            }
        }, "t1");

        t1.start();

        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("Thread2当前容器长度：" + mq.Size());
                int mqSize = mq.Size();
                for (int i = 0; i < mqSize; i++) {
                    Object obj = mq.take();
                    System.out.println("移除元素：" + obj);
                }
                Object obj = mq.take();
                System.out.println("移除元素：" + obj);
                Object o = mq.take();
                System.out.println("移除元素：" + o);
            }
        }, "t2");
        t2.start();
    }
}
