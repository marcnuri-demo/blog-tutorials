import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.UUID;

/**
 * ObjectProviderSolution.java - Solving prototype injection with ObjectProvider
 *
 * ObjectProvider allows lazy retrieval of prototype beans, ensuring a new
 * instance is created on each call.
 *
 * @see https://blog.marcnuri.com/spring-bean-scopes-singleton-prototypes
 */
public class ObjectProviderSolution {

    public static class Sample {
        private final String uuid = UUID.randomUUID().toString().substring(0, 8);
        public String getUuid() { return uuid; }
    }

    public static class SingletonWithObjectProvider {
        private final ObjectProvider<Sample> sampleProvider;
        public SingletonWithObjectProvider(ObjectProvider<Sample> sampleProvider) {
            this.sampleProvider = sampleProvider;
        }
        public Sample getNewSample() {
            return sampleProvider.getObject();
        }
    }

    @Configuration
    public static class AppConfig {
        @Bean
        @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
        public Sample sample() {
            return new Sample();
        }

        @Bean
        public SingletonWithObjectProvider singleton(ObjectProvider<Sample> sampleProvider) {
            return new SingletonWithObjectProvider(sampleProvider);
        }
    }

    public static void main(String[] args) {
        try (var context = new AnnotationConfigApplicationContext(AppConfig.class)) {
            SingletonWithObjectProvider singleton = context.getBean(SingletonWithObjectProvider.class);
            Sample s1 = singleton.getNewSample();
            Sample s2 = singleton.getNewSample();
            System.out.println("ObjectProvider - same instance: " + (s1 == s2));
            System.out.println("UUID 1: " + s1.getUuid());
            System.out.println("UUID 2: " + s2.getUuid());
        }
    }
}
