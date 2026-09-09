package com.parfum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.parfum.jpa.repository")
@EnableMongoRepositories(basePackages = "com.parfum.mongo.repository")
public class ParfumApplication {
    public static void main(String[] args) {
        SpringApplication.run(ParfumApplication.class, args);
    }
}
