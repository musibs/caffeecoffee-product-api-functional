package com.cafecoffee.product.api.handler;

import com.cafecoffee.product.api.model.Coffee;
import com.cafecoffee.product.api.model.CoffeeEvent;
import com.cafecoffee.product.api.repository.CoffeeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static org.springframework.web.reactive.function.BodyInserters.fromPublisher;

@Component
public class CoffeeHandler {

    private CoffeeRepository coffeeRepository;

    public CoffeeHandler(CoffeeRepository coffeeRepository) {
        this.coffeeRepository = coffeeRepository;
    }

    public Mono<ServerResponse> getCoffees(ServerRequest serverRequest) {
        Flux<Coffee> coffees = coffeeRepository.findAll();
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(coffees, Coffee.class);
    }

    public Mono<ServerResponse> getCoffeeById(ServerRequest serverRequest) {
        String id = serverRequest.pathVariable("id");
        Mono<Coffee> coffeeMono = coffeeRepository.findById(id);

        return coffeeMono
                .flatMap(coffee -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(fromPublisher(coffeeMono, Coffee.class)))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> saveCoffee(ServerRequest serverRequest) {
        Mono<Coffee> cofferMono = serverRequest.bodyToMono(Coffee.class);
        return cofferMono.flatMap(coffee ->
                ServerResponse.status(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(coffeeRepository.save(coffee), Coffee.class));
    }

    public Mono<ServerResponse> updateCoffee(ServerRequest serverRequest) {
        Mono<Coffee> existingMonoCoffee = this.coffeeRepository.findById(serverRequest.pathVariable("id"));
        Mono<Coffee> toBeUpdatedMonoCoffee = serverRequest.bodyToMono(Coffee.class);

        return toBeUpdatedMonoCoffee.zipWith(existingMonoCoffee,
                (toBeUpdatedCoffee, existingCoffee) -> new Coffee(existingCoffee.getId(), toBeUpdatedCoffee.getName(), toBeUpdatedCoffee.getPrice()))
                .flatMap(coffee -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(coffeeRepository.save(coffee), Coffee.class))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> deleteCoffee(ServerRequest serverRequest) {
        Mono<Coffee> monoCoffee = this.coffeeRepository.findById(serverRequest.pathVariable("id"));
        return monoCoffee.flatMap(existingCoffee ->
                    ServerResponse.ok()
                            .build(this.coffeeRepository.delete(existingCoffee)))
                    .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> deleteAllCoffees(ServerRequest serverRequest) {
        return ServerResponse.ok()
                .build(this.coffeeRepository.deleteAll());
    }

    public Mono<ServerResponse> getCoffeeEvents(ServerRequest serverRequest) {
        Flux<CoffeeEvent> coffeeEventFlux = Flux.interval(Duration.ofSeconds(1))
                .map(value -> new CoffeeEvent(value, "Server Event"));

        return ServerResponse
                .ok()
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .body(coffeeEventFlux, CoffeeEvent.class);
    }
    
}
