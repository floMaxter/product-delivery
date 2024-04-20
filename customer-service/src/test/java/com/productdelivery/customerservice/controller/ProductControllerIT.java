package com.productdelivery.customerservice.controller;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import wiremock.org.apache.hc.client5.http.impl.Wire;

import static com.github.tomakehurst.wiremock.client.WireMock.created;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockUser;

@SpringBootTest
@AutoConfigureWebTestClient
@WireMockTest(httpPort = 54321)
class ProductControllerIT {

    @Autowired
    WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        WireMock.stubFor(WireMock.get("/catalog-api/products/1")
                .willReturn(WireMock.okJson("""
                                {
                                    "id": 1,
                                    "title": "Товар №1",
                                    "details": "Описание товара №1"
                                }""")
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));
    }

    @Test
    void getProductPage_ProductIsExists_ReturnsProductPage() {
        // given
        WireMock.stubFor(WireMock.get("/feedback-api/product-reviews/by-product-id/1")
                .willReturn(WireMock.okJson("""
                                [
                                    {
                                        "id": "e7c637af-5dd0-48d8-9182-c44fcead7f92",
                                        "productId": 1,
                                        "rating": 5,
                                        "review": "Very good!",
                                        "userId": "bb7979cd-93fd-4792-a430-6a9c0106ae06"
                                    },
                                    {
                                        "id": "fc9ce2c5-b9e5-490e-bb88-3211ebb6a31c",
                                        "productId": 1,
                                        "rating": 4,
                                        "review": "Good",
                                        "userId": "596fc2ba-4592-4882-bdea-e641feeae637"
                                    }
                                ]""")
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));

        WireMock.stubFor(WireMock.get("feedback-api/favourite-products/by-product-id/1")
                .willReturn(created()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                                {
                                    "id": "1f55e227-a0b9-49ed-85b5-f5b280037591",
                                    "productId": 1
                                }
                                """)));

        // when
        this.webTestClient
                .mutateWith(mockUser())
                .get()
                .uri("/customer/products/1")
                .exchange()
                // then
                .expectStatus().isOk();

        WireMock.verify(getRequestedFor(urlPathMatching("/catalog-api/products/1")));
        WireMock.verify(getRequestedFor(urlPathMatching("/feedback-api/product-reviews/by-product-id/1")));
        WireMock.verify(getRequestedFor(urlPathMatching("/feedback-api/favourite-products/by-product-id/1")));
    }

    @Test
    void getProductPage_ProductDoesNotExist_ReturnsNotFound() {
        // given

        // when
        this.webTestClient
                .mutateWith(mockUser())
                .get()
                .uri("/customer/products/404")
                .exchange()
                // then
                .expectStatus().isNotFound();

        WireMock.verify(getRequestedFor(urlPathMatching("/catalog-api/products/404")));
    }

    @Test
    void getProductPage_UserIsNotAuthorized_RedirectToLoginPage() {
        // given

        // when
        this.webTestClient
                .get()
                .uri("/customer/products/1")
                .exchange()
                // then
                .expectStatus().isFound()
                .expectHeader().location("/login");
    }

    @Test
    void addProductToFavourites_RequestIsValid_ReturnsRedirectionToProductPage() {
        // given
        WireMock.stubFor(WireMock.post("/feedback-api/favourite-products")
                .withRequestBody(WireMock.equalToJson("""
                        {
                            "productId": 1
                        }"""))
                .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(created()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                                {
                                    "id": "6bf278f0-4195-4cbe-8aef-ded2fc8e6566",
                                    "productId": 1
                                }""")));

        // when
        this.webTestClient
                .mutateWith(mockUser())
                .mutateWith(csrf())
                .post()
                .uri("/customer/products/1/add-to-favourites")
                .exchange()
                // then
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/customer/products/1");

        WireMock.verify(getRequestedFor(urlPathMatching("/catalog-api/products/1")));
        WireMock.verify(postRequestedFor(urlPathMatching("/feedback-api/favourite-products"))
                .withRequestBody(equalToJson("""
                        {
                            "productId": 1
                        }""")));
    }

    @Test
    void addProductToFavourites_ProductDoesNotExits_ReturnsNotFoundPage() {
        // given

        // when
        this.webTestClient
                .mutateWith(mockUser())
                .mutateWith(csrf())
                .post()
                .uri("/customer/products/404/add-to-favourites")
                .exchange()
                // then
                .expectStatus().isNotFound();

        WireMock.verify(getRequestedFor(urlPathMatching("/catalog-api/products/404")));
    }

    @Test
    void addProductToFavourites_UserIsNotAuthorized_RedirectToLoginPage() {
        // given

        // when
        this.webTestClient
                .mutateWith(csrf())
                .post()
                .uri("/customer/products/1/add-to-favourites")
                .exchange()
                // then
                .expectStatus().isFound()
                .expectHeader().location("/login");
    }

    @Test
    void removeProductFromFavourites_ProductExists_ReturnsRedirectionToProductPage() {
        // given
        WireMock.stubFor(WireMock.delete("/feedback-api/favourite-products/by-product-id/1")
                .willReturn(WireMock.noContent()));

        // when
        this.webTestClient
                .mutateWith(mockUser())
                .mutateWith(csrf())
                .post()
                .uri("/customer/products/1/remove-from-favourites")
                .exchange()
                // then
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/customer/products/1");

        WireMock.verify(getRequestedFor(urlPathMatching("/catalog-api/products/1")));
        WireMock.verify(WireMock.deleteRequestedFor(
                urlPathMatching("/feedback-api/favourite-products/by-product-id/1")));
    }

    @Test
    void removeProductFromFavourites_ProductDoesNotExist_ReturnsNotFoundPage() {
        // given

        // when
        this.webTestClient
                .mutateWith(mockUser())
                .mutateWith(csrf())
                .post()
                .uri("/customer/products/404/remove-from-favourites")
                .exchange()
                // then
                .expectStatus().isNotFound();

        WireMock.verify(getRequestedFor(urlPathMatching("/catalog-api/products/404")));
    }

    @Test
    void removeProductFromFavourites_UserIsNotAuthorized_RedirectsToLoginPage() {
        // given

        // when
        this.webTestClient
                .mutateWith(csrf())
                .post()
                .uri("/customer/products/1/remove-from-favourites")
                .exchange()
                // then
                .expectStatus().isFound()
                .expectHeader().location("/login");
    }

    @Test
    void createReview_RequestIsValid_RedirectsToProductPage() {
        // given
        WireMock.stubFor(WireMock.post("/feedback-api/product-reviews")
                .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
                .withRequestBody(equalToJson("""
                        {
                            "productId": 1,
                            "rating": 5,
                            "review": "Good stuff"
                        }"""))
                .willReturn(created()
                        .withHeader(HttpHeaders.LOCATION,
                                "http://localhost/feedback-api/product-reviews/de0b44b0-3a76-478e-ac1c-7d3c91913614")
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                                {
                                    "id": "de0b44b0-3a76-478e-ac1c-7d3c91913614",
                                    "productId": 1,
                                    "rating": 5,
                                    "review": "Good stuff",
                                    "userId": "1a24d4ec-cbc6-11ee-af3b-0b236022162c"
                                }""")));

        // when
        this.webTestClient
                .mutateWith(mockUser())
                .mutateWith(csrf())
                .post()
                .uri("/customer/products/1/create-review")
                .body(BodyInserters.fromFormData("rating", "5")
                        .with("review", "Good stuff"))
                // then
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/customer/products/1");

        WireMock.verify(postRequestedFor(urlPathMatching("/feedback-api/product-reviews"))
                .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
                .withRequestBody(equalToJson("""
                        {
                            "productId": 1,
                            "rating": 5,
                            "review": "Good stuff"
                        }""")));
    }

    @Test
    void createReview_RequestIsInvalid_ReturnsProductPage() {
        // given
        WireMock.stubFor(WireMock.post("/feedback-api/product-reviews")
                .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
                .withRequestBody(equalToJson("""
                        {
                            "productId": 1,
                            "rating": -1,
                            "review": "Long review"
                        }"""))
                .willReturn(WireMock.badRequest()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PROBLEM_JSON_VALUE)
                        .withBody("""
                                {
                                    "errors": ["Error 1", "Error 2"]
                                }""")));

        WireMock.stubFor(WireMock.get("/feedback-api/favourite-products/by-product-id/1")
                .willReturn(WireMock.okJson("""
                        {
                            "id": "ec586ecc-cbc8-11ee-8e7d-4fce5e860855",
                            "productId": 1,
                            "userId": "f1177a8e-cbc8-11ee-8ca2-0bf025125fd5"
                        }""")
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));

        // when
        this.webTestClient
                .mutateWith(mockUser())
                .mutateWith(csrf())
                .post()
                .uri("/customer/products/1/create-review")
                .body(BodyInserters.fromFormData("rating", "-1")
                        .with("review", "Long review"))
                // then
                .exchange()
                .expectStatus().isBadRequest();

        WireMock.verify(postRequestedFor(urlPathMatching("/feedback-api/product-reviews"))
                .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
                .withRequestBody(equalToJson("""
                        {
                            "productId": 1,
                            "rating": -1,
                            "review": "Long review"
                        }""")));
    }

    @Test
    void createReview_ProductDoesNotExist_ReturnsNotFoundPage() {
        // given

        // when
        this.webTestClient
                .mutateWith(mockUser())
                .mutateWith(csrf())
                .post()
                .uri("/customer/products/404/create-review")
                .body(BodyInserters.fromFormData("rating", "5")
                        .with("review", "Good stuff"))
                .exchange()
                // then
                .expectStatus().isNotFound();

        WireMock.verify(getRequestedFor(urlPathMatching("/catalog-api/products/404")));
    }

    @Test
    void createReview_UserIsNotAuthorized_RedirectsToLoginPage() {
        // given

        // when
        this.webTestClient
                .mutateWith(csrf())
                .post()
                .uri("/customer/products/1/create-review")
                .body(BodyInserters.fromFormData("rating", "5")
                        .with("review", "Good stuff"))
                .exchange()
                // then
                .expectStatus().isFound()
                .expectHeader().location("/login");
    }

}