import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;

public class UpdateNginx {
  public static void main(String[] args) {
    try (var client = new KubernetesClientBuilder().build()) {
      client.apps().deployments().inNamespace("default").withName("nginx")
        .edit(nginx -> new DeploymentBuilder(nginx)
          .editSpec().editTemplate().editSpec().editContainer(0)
          .withImage("nginx:1.27")
          .endContainer().endSpec().endTemplate().endSpec()
          .build());
    }
  }
}
