package com.productdelivery.feedbackservice.controller;

import com.productdelivery.feedbackservice.controller.payload.NewFavouriteProductPayload;
import com.productdelivery.feedbackservice.model.FavouriteProduct;
import com.productdelivery.feedbackservice.service.FavouriteProductsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("feedback-api/favourite-products")
public class FavouriteProductsRestController {

    private final FavouriteProductsService favouriteProductsService;

    @GetMapping
    public Flux<FavouriteProduct> findFavouriteProducts() {
        return this.favouriteProductsService.findFavouriteProducts();
    }

    @GetMapping("by-product-id/{productId:\\d+}")
    public Mono<FavouriteProduct> findFavouriteProductByProductId(@PathVariable("productId") int productId) {
        return this.favouriteProductsService.findFavouriteProductByProduct(productId);
    }

    @PostMapping
    public Mono<ResponseEntity<FavouriteProduct>> addProductToFavourite(
            @Valid @RequestBody Mono<NewFavouriteProductPayload> payloadMono,
            UriComponentsBuilder uriComponentsBuilder) {
        return payloadMono
                .flatMap(payload -> this.favouriteProductsService.addProductToFavourites(payload.productId()))
                .map(favouriteProduct -> ResponseEntity
                        .created(uriComponentsBuilder.replacePath("feedback-api/favourite-products/{id}")
                                .build(favouriteProduct.getId()))
                        .body(favouriteProduct));
    }

    @DeleteMapping("by-product-id/{productId:\\d+}")
    public Mono<ResponseEntity<Void>> removeProductFromFavourite(@PathVariable("productId") int productId) {
        return this.favouriteProductsService.removeProductFromFavourites(productId)
                .then(Mono.just(ResponseEntity.noContent().build()));
    }
}
