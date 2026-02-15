import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.UUID;

/**
 * SingletonVsPrototype.java - Demonstrates singleton vs prototype bean scopes
 *
 * @see https://blog.marcnuri.com/spring-bean-scopes-guide-to-understand-the-different-bean-scopes
 */
public class SingletonVsPrototype {

    public static class Sample {
        private final String uuid = UUID.randomUUID().toString().substring(0, 8);
        public String getUuid() { return uuid; }
    }

    @Configuration
    public static class AppConfig {
        @Bean
        public Sample singletonSample() {
            return new Sample();
        }

        @Bean
        @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
        public Sample prototypeSample() {
            return new Sample();
        }
    }

    public static void main(String[] args) {
        try (var context = new AnnotationConfigApplicationContext(AppConfig.class)) {
            Sample singleton1 = context.getBean("singletonSample", Sample.class);
            Sample singleton2 = context.getBean("singletonSample", Sample.class);
            System.out.println("Singleton - same instance: " + (singleton1 == singleton2));

            Sample prototype1 = context.getBean("prototypeSample", Sample.class);
            Sample prototype2 = context.getBean("prototypeSample", Sample.class);
            System.out.println("Prototype - same instance: " + (prototype1 == prototype2));
        }
    }
}
