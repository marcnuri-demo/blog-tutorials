import io.fabric8.kubernetes.client.KubernetesClientBuilder;

public class ResumeNginx {
  public static void main(String[] args) {
    try (var client = new KubernetesClientBuilder().build()) {
      client.apps().deployments().inNamespace("default").withName("nginx")
        .rolling().resume();
    }
  }
}
