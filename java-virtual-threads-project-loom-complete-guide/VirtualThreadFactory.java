/**
 * VirtualThreadFactory.java - Method 4: ThreadFactory for virtual threads
 *
 * Useful when integrating with libraries that accept a ThreadFactory parameter.
 *
 * @see https://blog.marcnuri.com/java-virtual-threads-project-loom-complete-guide
 */

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

public class VirtualThreadFactory {
    public static void main(String[] args) throws InterruptedException {
        AtomicLong counter = new AtomicLong();

        ThreadFactory factory = Thread.ofVirtual()
            .name("worker-", counter.getAndIncrement())
            .factory();

        Thread t1 = factory.newThread(() -> System.out.println(Thread.currentThread().getName()));
        Thread t2 = factory.newThread(() -> System.out.println(Thread.currentThread().getName()));

        t1.start();
        t2.start();
        t1.join();
        t2.join();
    }
}