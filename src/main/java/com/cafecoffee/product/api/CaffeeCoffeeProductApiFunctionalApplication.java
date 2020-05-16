package com.cafecoffee.product.api;

import com.cafecoffee.product.api.handler.CoffeeHandler;
import com.cafecoffee.product.api.model.Coffee;
import com.cafecoffee.product.api.repository.CoffeeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.nest;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@SpringBootApplication
public class CaffeeCoffeeProductApiFunctionalApplication {

    public static void main(String[] args) {
        SpringApplication.run(CaffeeCoffeeProductApiFunctionalApplication.class, args);
    }

    @Bean
    CommandLineRunner init(ReactiveMongoOperations reactiveMongoOperations, CoffeeRepository coffeeRepository) {
        return args -> {
            Flux<Coffee> coffeeFlux = Flux.just(
                    new Coffee(null, "Latte", 1.50),
                    new Coffee(null, "Hot Chocolate", 4.50),
                    new Coffee(null, "Mocha", 2.5))
                    .flatMap(coffeeRepository::save);

            coffeeFlux.thenMany(coffeeRepository.findAll())
                    .subscribe(System.out::println);
        };
    }

    @Bean
    public RouterFunction<ServerResponse> routes(CoffeeHandler coffeeHandler) {
        /*return route(GET("/coffees").and(accept(MediaType.APPLICATION_JSON)), coffeeHandler::getCoffees)
                .andRoute(GET("/coffees/event").and(contentType(MediaType.TEXT_EVENT_STREAM)), coffeeHandler::getCoffeeEvents)
                .andRoute(GET("/coffees/{id}").and(accept(MediaType.APPLICATION_JSON)), coffeeHandler::getCoffeeById)
                .andRoute(POST("/coffees").and(contentType(MediaType.APPLICATION_JSON)), coffeeHandler::saveCoffee)
                .andRoute(PUT("/coffees/{id}").and(contentType(MediaType.APPLICATION_JSON)), coffeeHandler::updateCoffee)
                .andRoute(DELETE("/coffees/{id}").and(accept(MediaType.APPLICATION_JSON)), coffeeHandler::deleteCoffee)
                .andRoute(DELETE("/coffees/{id}").and(accept(MediaType.APPLICATION_JSON)), coffeeHandler::deleteAllCoffees);*/

        return nest(path("/coffees"),
            nest(accept(MediaType.APPLICATION_JSON).or(contentType(MediaType.APPLICATION_JSON)).or(accept(MediaType.TEXT_EVENT_STREAM)),
                route(GET("/"), coffeeHandler::getCoffees)
                    .andRoute(method(HttpMethod.POST), coffeeHandler::saveCoffee)
                    .andRoute(DELETE("/"), coffeeHandler::deleteAllCoffees)
                    .andRoute(GET("/events"), coffeeHandler::getCoffeeEvents)
                    .andNest(path("/{id}"),
                        route(method(HttpMethod.GET), coffeeHandler::getCoffeeById)
                            .andRoute(method(HttpMethod.PUT), coffeeHandler::updateCoffee)
                            .andRoute(method(HttpMethod.DELETE), coffeeHandler::deleteCoffee)
                        )
                    )
                );
    }
}
