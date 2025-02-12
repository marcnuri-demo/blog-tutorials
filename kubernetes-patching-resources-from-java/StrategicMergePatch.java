import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;

public class StrategicMergePatch {
  public static void main(String[] args) {
    try (KubernetesClient client = new KubernetesClientBuilder().build()) {
      String patch = "{"
        + "  \"spec\": {"
        + "    \"template\": {"
        + "      \"spec\": {"
        + "        \"containers\": ["
        + "          {\"name\": \"my-container\", \"image\": \"nginx:latest\"}"
        + "        ]"
        + "      }"
        + "    }"
        + "  }"
        + "}";
      client.apps().deployments().inNamespace("default").withName("my-deployment")
        .patch(patch);
    }
  }
}
