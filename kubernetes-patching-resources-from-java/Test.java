/// usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 21+
//DEPS io.fabric8:kubernetes-client:7.5.1
//DEPS io.fabric8:kube-api-test:7.5.1
//DEPS org.assertj:assertj-core:3.27.6
//SOURCES ./JsonPatch.java
//SOURCES ./JsonMergePatch.java
//SOURCES ./StrategicMergePatch.java

import io.fabric8.kubeapitest.KubeAPIServer;
import io.fabric8.kubeapitest.KubeAPIServerConfigBuilder;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.PodSpecBuilder;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.api.model.apps.DeploymentSpecBuilder;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import org.assertj.core.api.InstanceOfAssertFactories;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings({"CallToPrintStackTrace"})
public final class Test {
  public static void main(String[] args) {
    var kubeApi = new KubeAPIServer(
      KubeAPIServerConfigBuilder.anAPIServerConfig().withUpdateKubeConfig(false).build());
    try {
      kubeApi.start();
      final var config = Config.fromKubeconfig(kubeApi.getKubeConfigYaml());
      System.setProperty("kubernetes.master", config.getMasterUrl());
      System.setProperty("kubernetes.trust.certificates", "true");
      System.setProperty("kubernetes.certs.client.file", config.getClientCertFile());
      System.setProperty("kubernetes.certs.client.key.file", config.getClientKeyFile());
      final var client = new KubernetesClientBuilder().withConfig(config).build();

      deploy(client);
      StrategicMergePatch.main(null);
      assertThat(client.apps().deployments().inNamespace("default").withName("my-deployment").get())
        .isNotNull()
        .extracting(d -> d.getSpec().getTemplate().getSpec().getContainers())
        .asInstanceOf(InstanceOfAssertFactories.list(Container.class))
        .extracting("image")
        .contains("nginx:latest");

      delete(client);
      deploy(client);
      JsonPatch.main(null);
      assertThat(client.apps().deployments().inNamespace("default").withName("my-deployment").get())
        .isNotNull()
        .extracting(d -> d.getSpec().getTemplate().getSpec().getContainers())
        .asInstanceOf(InstanceOfAssertFactories.list(Container.class))
        .extracting("image")
        .contains("nginx:latest");

      delete(client);
      deploy(client);
      JsonMergePatch.main(null);
      assertThat(client.apps().deployments().inNamespace("default").withName("my-deployment").get())
        .isNotNull()
        .extracting("metadata.annotations").asInstanceOf(InstanceOfAssertFactories.map(String.class, String.class))
        .isEmpty();

      System.exit(0);
    } catch (Exception ex) {
      ex.printStackTrace();
      System.exit(1);
    } finally {
      kubeApi.stop();
    }
  }

  private static void deploy(KubernetesClient client) {
    var nginx = new DeploymentBuilder()
      .withNewMetadata().withName("my-deployment").addToAnnotations("foo", "bar").endMetadata()
      .withSpec(new DeploymentSpecBuilder()
        .withReplicas(1)
        .withNewSelector().addToMatchLabels("app", "nginx").endSelector()
        .withNewTemplate()
        .withNewMetadata().addToLabels("app", "nginx").endMetadata()
        .withSpec(new PodSpecBuilder()
          .addNewContainer()
          .withName("my-deployment")
          .withImage("nginx:1.26")
          .addNewPort().withContainerPort(80).endPort()
          .endContainer()
          .build())
        .endTemplate()
        .build())
      .build();
    client.apps().deployments().inNamespace("default").resource(nginx).serverSideApply();
  }

  private static void delete(KubernetesClient client) {
    client.apps().deployments().inNamespace("default").withName("my-deployment").withTimeoutInMillis(1000).delete();
  }
}


