package com.car_pooling_system.userservice.config;

import com.car_pooling_system.userservice.factory.ConcreteUserFactory;
import com.car_pooling_system.userservice.factory.RoleBasedUserFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FactoryConfig {

    /**
     * Exposes the Singleton ConcreteUserFactory as a Spring Bean
     * so it can be injected via @Autowired / constructor injection.
     */
    @Bean
    public RoleBasedUserFactory roleBasedUserFactory() {
        return ConcreteUserFactory.getInstance();
    }
}
