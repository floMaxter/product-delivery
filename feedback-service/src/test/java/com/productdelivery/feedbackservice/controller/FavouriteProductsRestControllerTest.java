package com.productdelivery.feedbackservice.controller;

import com.productdelivery.feedbackservice.controller.payload.NewFavouriteProductPayload;
import com.productdelivery.feedbackservice.model.FavouriteProduct;
import com.productdelivery.feedbackservice.service.FavouriteProductsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class FavouriteProductsRestControllerTest {

    @Mock
    FavouriteProductsService favouriteProductsService;

    @InjectMocks
    FavouriteProductsRestController controller;

    @Test
    void findFavouriteProducts_ReturnsFavouriteProducts() {
        // given
        doReturn(Flux.fromIterable(List.of(
                new FavouriteProduct(UUID.fromString("c817c10c-c29a-407f-a12b-1dbf32bb8645"), 1,
                        "291f1e98-6577-4b61-b5e4-e05aab844be8"),
                new FavouriteProduct(UUID.fromString("bec738ee-996f-4caf-a7b9-d4fb9ad7af14"), 1,
                        "291f1e98-6577-4b61-b5e4-e05aab844be8")
        ))).when(this.favouriteProductsService).findFavouriteProducts("291f1e98-6577-4b61-b5e4-e05aab844be8");

        // when
        StepVerifier.create(this.controller.findFavouriteProducts(
                        Mono.just(new JwtAuthenticationToken(Jwt.withTokenValue("e4.e5")
                                .headers(headers -> headers.put("foo", "bar"))
                                .claim("sub", "291f1e98-6577-4b61-b5e4-e05aab844be8").build()))))
                // then
                .expectNext(
                        new FavouriteProduct(UUID.fromString("c817c10c-c29a-407f-a12b-1dbf32bb8645"), 1,
                                "291f1e98-6577-4b61-b5e4-e05aab844be8"),
                        new FavouriteProduct(UUID.fromString("bec738ee-996f-4caf-a7b9-d4fb9ad7af14"), 1,
                                "291f1e98-6577-4b61-b5e4-e05aab844be8")
                ).verifyComplete();

        verify(this.favouriteProductsService).findFavouriteProducts("291f1e98-6577-4b61-b5e4-e05aab844be8");
        verifyNoMoreInteractions(this.favouriteProductsService);
    }

    @Test
    void findFavouriteProductByProductId_ReturnsFavouriteProduct() {
        // given
        doReturn(Mono.just(
                new FavouriteProduct(UUID.fromString("c817c10c-c29a-407f-a12b-1dbf32bb8645"), 1,
                        "291f1e98-6577-4b61-b5e4-e05aab844be8")))
                .when(this.favouriteProductsService)
                .findFavouriteProductByProduct(1, "291f1e98-6577-4b61-b5e4-e05aab844be8");

        // when
        StepVerifier.create(this.controller.findFavouriteProductByProductId(
                        Mono.just(new JwtAuthenticationToken(Jwt.withTokenValue("e4.e5")
                                .headers(headers -> headers.put("foo", "bar"))
                                .claim("sub", "291f1e98-6577-4b61-b5e4-e05aab844be8").build())), 1))
                // then
                .expectNext(
                        new FavouriteProduct(UUID.fromString("c817c10c-c29a-407f-a12b-1dbf32bb8645"), 1,
                                "291f1e98-6577-4b61-b5e4-e05aab844be8"))
                .verifyComplete();

        verify(this.favouriteProductsService).
                findFavouriteProductByProduct(1, "291f1e98-6577-4b61-b5e4-e05aab844be8");
        verifyNoMoreInteractions(this.favouriteProductsService);
    }

    @Test
    void addProductToFavourites_ReturnsCreatedFavouriteProduct() {
        // given
        doReturn(Mono.just(
                new FavouriteProduct(UUID.fromString("c817c10c-c29a-407f-a12b-1dbf32bb8645"), 1,
                        "291f1e98-6577-4b61-b5e4-e05aab844be8")))
                .when(this.favouriteProductsService)
                .addProductToFavourites(1, "291f1e98-6577-4b61-b5e4-e05aab844be8");

        // when
        StepVerifier.create(this.controller.addProductToFavourites(
                        Mono.just(new JwtAuthenticationToken(Jwt.withTokenValue("e4.e5")
                                .headers(headers -> headers.put("foo", "bar"))
                                .claim("sub", "291f1e98-6577-4b61-b5e4-e05aab844be8").build())),
                        Mono.just(new NewFavouriteProductPayload(1)),
                        UriComponentsBuilder.fromUriString("http://localhost")))
                // then
                .expectNext(
                        ResponseEntity
                                .created(URI.create("http://localhost/feedback-api/favourite-products/c817c10c-c29a-407f-a12b-1dbf32bb8645"))
                                .body(new FavouriteProduct(UUID.fromString("c817c10c-c29a-407f-a12b-1dbf32bb8645"), 1,
                                        "291f1e98-6577-4b61-b5e4-e05aab844be8")))
                .verifyComplete();
        verify(this.favouriteProductsService)
                .addProductToFavourites(1, "291f1e98-6577-4b61-b5e4-e05aab844be8");
        verifyNoMoreInteractions(this.favouriteProductsService);
    }

    @Test
    void removeProductFromFavourite_ReturnsNoContent() {
        // given
        doReturn(Mono.empty()).when(this.favouriteProductsService)
                .removeProductFromFavourites(1, "291f1e98-6577-4b61-b5e4-e05aab844be8");

        // when
        StepVerifier.create(this.controller.removeProductFromFavourites(
                        Mono.just(new JwtAuthenticationToken(Jwt.withTokenValue("e4.e5")
                                .headers(headers -> headers.put("foo", "bar"))
                                .claim("sub", "291f1e98-6577-4b61-b5e4-e05aab844be8").build())), 1))
                // then
                .expectNext(
                        ResponseEntity.noContent().build())
                .verifyComplete();

        verify(this.favouriteProductsService)
                .removeProductFromFavourites(1, "291f1e98-6577-4b61-b5e4-e05aab844be8");
        verifyNoMoreInteractions(this.favouriteProductsService);
    }
}