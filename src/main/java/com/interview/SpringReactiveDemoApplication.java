package com.interview;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.interview.handler.UserHandler;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@SpringBootApplication
@EnableR2dbcRepositories
@ComponentScan(basePackages = "com.interview")
public class SpringReactiveDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringReactiveDemoApplication.class, args);
    }

    // Functional routing example - alternative to @RestController
    @Bean
    public RouterFunction<ServerResponse> functionalRoutes(UserHandler userHandler) {
        return route(GET("/api/functional/users"), userHandler::getAllUsers)
                .andRoute(GET("/api/functional/users/{id}"), userHandler::getUserById)
                .andRoute(POST("/api/functional/users"), userHandler::createUser);
    }
}