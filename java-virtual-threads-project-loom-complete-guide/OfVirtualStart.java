/**
 * OfVirtualStart.java - Method 2: Thread.ofVirtual().start()
 *
 * Builder pattern for more control over thread configuration.
 *
 * @see https://blog.marcnuri.com/java-virtual-threads-project-loom-complete-guide
 */
public class OfVirtualStart {
    public static void main(String[] args) throws InterruptedException {
        Thread vt = Thread.ofVirtual()
            .name("my-virtual-thread")
            .start(() -> {
                System.out.println("Thread name: " + Thread.currentThread().getName());
                simulateWork();
            });
        vt.join();
    }

    private static void simulateWork() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}