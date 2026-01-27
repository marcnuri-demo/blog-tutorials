/**
 * Repository.java - Record representing a repository for demo purposes.
 *
 * Modern Java record alternative to traditional POJO classes.
 *
 * @see https://blog.marcnuri.com/java-streams-convert-list-into-map
 */
public record Repository(String name, String fullName, String description, boolean fork) {

    // Static factory method to create sample data
    public static java.util.List<Repository> sampleData() {
        return java.util.List.of(
            new Repository("kubernetes-client", "fabric8io/kubernetes-client", "Kubernetes client for Java", false),
            new Repository("jkube", "eclipse-jkube/jkube", "Build and deploy Java apps to Kubernetes", false),
            new Repository("electronim", "manusa/electronim", "Electron-based multi IM client", false),
            new Repository("helm-java", "manusa/helm-java", "Helm client for Java", false)
        );
    }
}
