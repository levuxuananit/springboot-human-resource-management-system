package com.r2s.auth;

import com.r2s.core.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.r2s")
@EnableJpaRepositories(basePackages = "com.r2s.auth.repository")
@EnableConfigurationProperties(JwtProperties.class)
@EntityScan(basePackages = "com.r2s.core.entity")
@EnableJpaAuditing
public class AuthServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
