import io.fabric8.kubernetes.client.KubernetesClientBuilder;

public class RollbackNginx {
  public static void main(String[] args) {
    try (var client = new KubernetesClientBuilder().build()) {
      client.apps().deployments().inNamespace("default").withName("nginx")
        .rolling().undo();
    }
  }
}
