import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.kubernetes.client.dsl.base.PatchContext;
import io.fabric8.kubernetes.client.dsl.base.PatchType;

public class JsonPatch {
  public static void main(String[] args) {
    try (KubernetesClient client = new KubernetesClientBuilder().build()) {
      String patch = "["
        + "{\"op\": \"replace\", \"path\": \"/spec/template/spec/containers/0/image\", \"value\": \"nginx:latest\"}"
        + "]";
      client.apps().deployments().inNamespace("default").withName("my-deployment")
        .patch(PatchContext.of(PatchType.JSON), patch);
    }
  }
}
