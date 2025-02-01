/// usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 21+
//DEPS io.fabric8:kubernetes-client:7.0.1
//DEPS io.fabric8:kube-api-test:7.0.1
//DEPS org.assertj:assertj-core:3.27.3
//SOURCES ./DeployNginx.java
//SOURCES ./PauseNginx.java
//SOURCES ./ResumeNginx.java
//SOURCES ./RollbackNginx.java
//SOURCES ./UpdateNginx.java

import io.fabric8.kubeapitest.KubeAPIServer;
import io.fabric8.kubeapitest.KubeAPIServerConfigBuilder;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.apps.ReplicaSetBuilder;
import io.fabric8.kubernetes.client.Config;
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
      @SuppressWarnings("resource")
      final var client = new KubernetesClientBuilder().withConfig(config).build();

      DeployNginx.main(null);
      final var initialDeployment = client.apps().deployments().inNamespace("default").withName("nginx").get();
      assertThat(initialDeployment)
        .isNotNull()
        .extracting(d -> d.getSpec().getTemplate().getSpec().getContainers())
        .asInstanceOf(InstanceOfAssertFactories.list(Container.class))
        .extracting("image")
        .contains("nginx:1.26");
      client.resource(new ReplicaSetBuilder()
        .withNewMetadata()
        .withName("rs-1")
        .withLabels(initialDeployment.getSpec().getSelector().getMatchLabels())
        .addToAnnotations("deployment.kubernetes.io/revision", "1")
        .endMetadata()
        .withNewSpec()
        .withSelector(initialDeployment.getSpec().getSelector())
        .withTemplate(initialDeployment.getSpec().getTemplate())
        .endSpec()
        .build()
      ).create();

      UpdateNginx.main(null);
      final var updatedDeployment = client.apps().deployments().inNamespace("default").withName("nginx").get();
      assertThat(updatedDeployment)
        .isNotNull()
        .extracting(d -> d.getSpec().getTemplate().getSpec().getContainers())
        .asInstanceOf(InstanceOfAssertFactories.list(Container.class))
        .extracting("image")
        .contains("nginx:1.27");
      client.resource(new ReplicaSetBuilder()
        .withNewMetadata()
        .withName("rs-2")
        .withLabels(updatedDeployment.getSpec().getSelector().getMatchLabels())
        .addToAnnotations("deployment.kubernetes.io/revision", "2")
        .endMetadata()
        .withNewSpec()
        .withSelector(updatedDeployment.getSpec().getSelector())
        .withTemplate(updatedDeployment.getSpec().getTemplate())
        .endSpec()
        .build()).create();

      RollbackNginx.main(null);
      assertThat(client.apps().deployments().inNamespace("default").withName("nginx").get())
        .isNotNull()
        .extracting(d -> d.getSpec().getTemplate().getSpec().getContainers())
        .asInstanceOf(InstanceOfAssertFactories.list(Container.class))
        .extracting("image")
        .contains("nginx:1.26");

      PauseNginx.main(null);
      assertThat(client.apps().deployments().inNamespace("default").withName("nginx").get())
        .extracting("spec.paused")
        .isEqualTo(true);

      ResumeNginx.main(null);
      assertThat(client.apps().deployments().inNamespace("default").withName("nginx").get())
        .extracting("spec.paused")
        .isNull();

      System.exit(0);
    } catch (Exception ex) {
      ex.printStackTrace();
      System.exit(1);
    } finally {
      kubeApi.stop();
    }
  }
}


