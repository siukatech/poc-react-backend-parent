package com.siukatech.poc.react.backend.parent.data.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Slf4j
@Configuration
////@AutoConfigureAfter(JpaRepositoriesAutoConfiguration.class)
////@EnableTransactionManagement(proxyTargetClass = true)
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
@EntityScan(basePackages = {"com.siukatech.poc.react.backend.parent.data.entity"})
@EnableJpaRepositories("com.siukatech.poc.react.backend.parent.data.repository")
//@ComponentScan(basePackages = { "com.siukatech.poc.react.backend.parent.data" })
////@Import(StarterEntityRegistrar.class)
public class DataConfig {

    @Bean
    public AuditorAware<String> auditorAware() {
        return new AuditorAware<String>() {
            @Override
            public Optional<String> getCurrentAuditor() {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                String name = (authentication == null ? "NULL" : authentication.getName());
                log.debug("getCurrentAuditor - name: [{}], authentication: [{}]"
                        , name, authentication);
                return Optional.of(name);
            }
        };
    }

}
