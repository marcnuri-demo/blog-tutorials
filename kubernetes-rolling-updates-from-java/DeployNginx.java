import io.fabric8.kubernetes.api.model.PodSpecBuilder;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.api.model.apps.DeploymentSpecBuilder;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;

public class DeployNginx {
  public static void main(String[] args) {
    try (var client = new KubernetesClientBuilder().build()) {
      var nginx = new DeploymentBuilder()
        .withNewMetadata().withName("nginx").endMetadata()
        .withSpec(new DeploymentSpecBuilder()
          .withReplicas(1)
          .withNewSelector().addToMatchLabels("app", "nginx").endSelector()
          .withNewTemplate()
          .withNewMetadata().addToLabels("app", "nginx").endMetadata()
          .withSpec(new PodSpecBuilder()
            .addNewContainer()
            .withName("nginx")
            .withImage("nginx:1.26")
            .addNewPort().withContainerPort(80).endPort()
            .endContainer()
            .build())
          .endTemplate()
          .build())
        .build();
      client.apps().deployments().inNamespace("default").resource(nginx).serverSideApply();
    }
  }
}
