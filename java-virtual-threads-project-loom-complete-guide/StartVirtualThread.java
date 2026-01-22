/**
 * StartVirtualThread.java - Method 1: Thread.startVirtualThread()
 *
 * The simplest way to start a virtual thread immediately.
 *
 * @see https://blog.marcnuri.com/java-virtual-threads-project-loom-complete-guide
 */
public class StartVirtualThread {
    public static void main(String[] args) throws InterruptedException {
        Thread vt = Thread.startVirtualThread(() -> {
            System.out.println("Running in: " + Thread.currentThread());
            System.out.println("Is virtual: " + Thread.currentThread().isVirtual());
        });
        vt.join();
    }
}