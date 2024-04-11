package com.productdelivery.feedbackservice.repository;

import com.productdelivery.feedbackservice.model.FavouriteProduct;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface FavouriteProductRepository extends ReactiveCrudRepository<FavouriteProduct, UUID> {

    Flux<FavouriteProduct> findAllByUserId(String userId);

    Mono<Void> deleteByProductIdAndUserId(int productId, String userId);

    Mono<FavouriteProduct> findByProductIdAndUserId(int productId, String userId);
}
