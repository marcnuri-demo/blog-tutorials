import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.UUID;

/**
 * PrototypeInSingleton.java - Demonstrates the scoped bean injection problem
 *
 * When a prototype bean is injected into a singleton, the prototype is only
 * instantiated once (when the singleton is created), defeating its purpose.
 *
 * @see https://blog.marcnuri.com/spring-bean-scopes-singleton-prototypes
 */
public class PrototypeInSingleton {

    public static class Sample {
        private final String uuid = UUID.randomUUID().toString().substring(0, 8);
        public String getUuid() { return uuid; }
    }

    public static class SingletonWithPrototype {
        private final Sample sample;
        public SingletonWithPrototype(Sample sample) { this.sample = sample; }
        public Sample getSample() { return sample; }
    }

    @Configuration
    public static class AppConfig {
        @Bean
        @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
        public Sample sample() {
            return new Sample();
        }

        @Bean
        public SingletonWithPrototype singletonWithPrototype(Sample sample) {
            return new SingletonWithPrototype(sample);
        }
    }

    public static void main(String[] args) {
        try (var context = new AnnotationConfigApplicationContext(AppConfig.class)) {
            SingletonWithPrototype s1 = context.getBean(SingletonWithPrototype.class);
            SingletonWithPrototype s2 = context.getBean(SingletonWithPrototype.class);
            System.out.println("Singleton - same instance: " + (s1 == s2));
            System.out.println("Injected prototype - same instance: " + (s1.getSample() == s2.getSample()));
        }
    }
}
