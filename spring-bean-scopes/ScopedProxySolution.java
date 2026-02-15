import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;

import java.util.UUID;

/**
 * ScopedProxySolution.java - Solving prototype injection with scoped proxies
 *
 * Using @Scope with proxyMode creates a proxy that delegates to a fresh
 * prototype instance on each method invocation.
 *
 * @see https://blog.marcnuri.com/spring-bean-scopes-singleton-prototypes
 */
public class ScopedProxySolution {

    public static class Sample {
        private final String uuid = UUID.randomUUID().toString().substring(0, 8);
        public String getUuid() { return uuid; }
    }

    public static class SingletonWithProxy {
        private final Sample sample;
        public SingletonWithProxy(Sample sample) { this.sample = sample; }
        public String getSampleUuid() { return sample.getUuid(); }
        public Class<?> getSampleClass() { return sample.getClass(); }
    }

    @Configuration
    public static class AppConfig {
        @Bean
        @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE, proxyMode = ScopedProxyMode.TARGET_CLASS)
        public Sample sample() {
            return new Sample();
        }

        @Bean
        public SingletonWithProxy singleton(Sample sample) {
            return new SingletonWithProxy(sample);
        }
    }

    public static void main(String[] args) {
        try (var context = new AnnotationConfigApplicationContext(AppConfig.class)) {
            SingletonWithProxy singleton = context.getBean(SingletonWithProxy.class);
            String uuid1 = singleton.getSampleUuid();
            String uuid2 = singleton.getSampleUuid();
            System.out.println("Scoped proxy - same UUID: " + uuid1.equals(uuid2));
            System.out.println("UUID 1: " + uuid1);
            System.out.println("UUID 2: " + uuid2);
            System.out.println("Is CGLIB proxy: " + singleton.getSampleClass().getName().contains("CGLIB"));
        }
    }
}
