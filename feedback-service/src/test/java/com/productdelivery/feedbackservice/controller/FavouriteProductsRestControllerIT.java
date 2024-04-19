package com.productdelivery.feedbackservice.controller;

import com.productdelivery.feedbackservice.model.FavouriteProduct;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;
import java.util.UUID;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

@SpringBootTest
@AutoConfigureWebTestClient
class FavouriteProductsRestControllerIT {

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    ReactiveMongoTemplate reactiveMongoTemplate;

    @BeforeEach
    void setUp() {
        this.reactiveMongoTemplate.insertAll(List.of(
                new FavouriteProduct(UUID.fromString("1e8e029a-b516-4d5c-8590-0d8b78db75e9"), 1,
                        "6177a957-3d91-4e62-92d9-a785e22fc587"),
                new FavouriteProduct(UUID.fromString("7d4823bc-3b7d-4fa5-a061-a677b27e7f5b"), 2,
                        "f87aab7f-9087-4624-8127-d127147a9a75"),
                new FavouriteProduct(UUID.fromString("fdd24369-1ca0-4896-b874-ac9b540b6a56"), 3,
                        "6177a957-3d91-4e62-92d9-a785e22fc587")
        )).blockLast();
    }

    @AfterEach
    void tearDown() {
        this.reactiveMongoTemplate.remove(FavouriteProduct.class).all().block();
    }

    @Test
    void findFavouriteProducts_ReturnsFavouriteProducts() {
        // given
        // when
        this.webTestClient
                .mutateWith(mockJwt().jwt(builder -> builder.subject("6177a957-3d91-4e62-92d9-a785e22fc587")))
                .get()
                .uri("/feedback-api/favourite-products")
                .exchange()
                // then
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody().json("""
                        [
                            {
                                "id": "1e8e029a-b516-4d5c-8590-0d8b78db75e9",
                                "productId": 1,
                                "userId": "6177a957-3d91-4e62-92d9-a785e22fc587"
                            },
                            {
                                "id": "fdd24369-1ca0-4896-b874-ac9b540b6a56",
                                "productId": 3,
                                "userId": "6177a957-3d91-4e62-92d9-a785e22fc587"
                            }
                        ]""");
    }

    @Test
    void findFavouriteProducts_UserIsNotAuthorized_ReturnsUnauthorized() {
        // given
        // when
        this.webTestClient
                .get()
                .uri("/feedback-api/favourite-products")
                .exchange()
                // then
                .expectStatus().isUnauthorized();
    }

    @Test
    void findFavouriteProductByProductId_ReturnsFavouriteProducts() {
        // given
        // when
        this.webTestClient
                .mutateWith(mockJwt().jwt(builder -> builder.subject("6177a957-3d91-4e62-92d9-a785e22fc587")))
                .get()
                .uri("/feedback-api/favourite-products/by-product-id/1")
                .exchange()
                // then
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody().json("""
                        {
                            "id": "1e8e029a-b516-4d5c-8590-0d8b78db75e9",
                            "productId": 1,
                            "userId": "6177a957-3d91-4e62-92d9-a785e22fc587"
                        }""");
    }

    @Test
    void findFavouriteProductByProductId_UserIsNotAuthorized_ReturnsUnauthorized() {
        // given
        // when
        this.webTestClient
                .get()
                .uri("/feedback-api/favourite-products/by-product-id/1")
                .exchange()
                // then
                .expectStatus().isUnauthorized();
    }

    @Test
    void addProductToFavourites_RequestIsValid_ReturnsCreatedFavouriteProduct() {
        // given
        // when
        this.webTestClient
                .mutateWith(mockJwt().jwt(builder -> builder.subject("7d4823bc-3b7d-4fa5-a061-a677b27e7f5b")))
                .post()
                .uri("/feedback-api/favourite-products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                            "productId": 10
                        }""")
                .exchange()
                // then
                .expectStatus().isCreated()
                .expectHeader().exists(HttpHeaders.LOCATION)
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody().json("""
                        {
                            "productId": 10,
                            "userId": "7d4823bc-3b7d-4fa5-a061-a677b27e7f5b"
                        }""").jsonPath("$.id").exists();
    }

    @Test
    void addProductToFavourites_RequestIsInvalid_ReturnsBadRequest() {
        // given
        // when
        this.webTestClient
                .mutateWith(mockJwt().jwt(builder -> builder.subject("7d4823bc-3b7d-4fa5-a061-a677b27e7f5b")))
                .post()
                .uri("/feedback-api/favourite-products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                            "productId": null
                        }""")
                .exchange()
                // then
                .expectStatus().isBadRequest()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody().json("""
                        {
                            "errors": ["Товар не указан"]
                        }""");
    }

    @Test
    void addProductToFavourites_UserIsNotAuthorized_ReturnsUnauthorized() {
        // given
        // when
        this.webTestClient
                .post()
                .uri("/feedback-api/favourite-products/")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                            "productId": 10
                        }""")
                .exchange()
                // then
                .expectStatus().isUnauthorized();
    }

    @Test
    void removeProductFromFavourites_ReturnsNoContent() {
        // given
        // when
        this.webTestClient
                .mutateWith(mockJwt().jwt(builder -> builder.subject("7d4823bc-3b7d-4fa5-a061-a677b27e7f5b")))
                .delete()
                .uri("/feedback-api/favourite-products/by-product-id/1")
                .exchange()
                // then
                .expectStatus().isNoContent();
    }

    @Test
    void removeProductFromFavourites_UserIsNotAuthorized_ReturnsUnauthorized() {
        // when
        this.webTestClient
                .delete()
                .uri("/feedback-api/favourite-products/by-product-id/1")
                .exchange()
                // then
                .expectStatus().isUnauthorized();
    }
}