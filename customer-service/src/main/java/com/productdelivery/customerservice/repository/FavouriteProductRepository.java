package com.productdelivery.customerservice.repository;

import com.productdelivery.customerservice.model.FavouriteProduct;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FavouriteProductRepository {

    public Mono<FavouriteProduct> save(FavouriteProduct favouriteProduct);

    public Mono<Void> deleteByProductId(int productId);

    Mono<FavouriteProduct> findByProductId(int productId);

    Flux<FavouriteProduct> findAll();
}
