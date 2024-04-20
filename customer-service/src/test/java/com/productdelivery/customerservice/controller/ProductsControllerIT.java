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

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockUser;

@SpringBootTest
@AutoConfigureWebTestClient
@WireMockTest(httpPort = 54321)
class ProductsControllerIT {

    @Autowired
    WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        WireMock.stubFor(WireMock.get(WireMock.urlPathMatching("/catalog-api/products"))
                .withQueryParam("filter", WireMock.equalTo("товар"))
                .willReturn(WireMock.okJson("""
                        [
                            {
                                "id": 1,
                                "title": "Товар №1",
                                "details": "Описание товара №1"
                            },
                            {
                                "id": 2,
                                "title": "Товар №2",
                                "details": "Описание товара №2"
                            },
                            {
                                "id": 3,
                                "title": "Товар №3",
                                "details": "Описание товара №3"
                            }
                        ]""")));
    }

    @Test
    void getProductsListPage_ReturnsProductsPage() {
        // given

        // when
        this.webTestClient
                .mutateWith(mockUser())
                .get()
                .uri("/customer/products/list?filter=товар")
                .exchange()
                // then
                .expectStatus().isOk();

        WireMock.verify(WireMock.getRequestedFor(WireMock.urlPathMatching("/catalog-api/products"))
                .withQueryParam("filter", WireMock.equalTo("товар")));
    }

    @Test
    void getProductsListPage_UserIsNotAuthenticated_RedirectsToLoginPage() {
        // given

        // when
        this.webTestClient
                .get()
                .uri("/customer/products/list")
                .exchange()
                // then
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/login");
    }

    @Test
    void getFavouriteProductsPage_ReturnsFavouriteProductsPage() {
        // given
        WireMock.stubFor(WireMock.get("/feedback-api/favourite-products")
                .willReturn(WireMock.okJson("""
                        [
                            {
                                "id": "7b1917af-4348-46b3-98ad-fb97c995f5a4",
                                "productId": 1,
                                "userId": "d62ead00-dbc5-4fa7-9091-56a9059ec94f"
                            },
                            {
                                "id": "2b2eb11c-ef6c-407d-b16f-c39d10189362",
                                "productId": 3,
                                "userId": "d62ead00-dbc5-4fa7-9091-56a9059ec94f"
                            }
                        ]""")
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));

        // when
        this.webTestClient
                .mutateWith(mockUser())
                .get()
                .uri("/customer/products/favourites?filter=товар")
                .exchange()
                // then
                .expectStatus().isOk();

        WireMock.verify(WireMock.getRequestedFor(WireMock.urlPathMatching("/catalog-api/products"))
                .withQueryParam("filter", WireMock.equalTo("товар")));
        WireMock.verify(WireMock.getRequestedFor(WireMock.urlPathMatching("/feedback-api/favourite-products")));
    }

    @Test
    void getFavouriteProductsPage_UserIsNotAuthenticated_RedirectsToLoginPage() {
        // given

        // when
        this.webTestClient
                .get()
                .uri("/customer/products/favourites")
                .exchange()
                // then
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/login");
    }
}