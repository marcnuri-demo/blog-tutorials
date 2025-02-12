import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.kubernetes.client.dsl.base.PatchContext;
import io.fabric8.kubernetes.client.dsl.base.PatchType;

public class JsonMergePatch {
  public static void main(String[] args) {
    try (KubernetesClient client = new KubernetesClientBuilder().build()) {
      // In this patch, setting the "foo" annotation to null removes it.
      String jsonMergePatch = "{"
        + "\"metadata\": {"
        + "  \"annotations\": {"
        + "    \"foo\": null"
        + "  }"
        + "}"
        + "}";
      PatchContext patchContext = PatchContext.of(PatchType.JSON_MERGE);
      client.apps().deployments().inNamespace("default").withName("my-deployment")
        .patch(patchContext, jsonMergePatch);
    }
  }
}
