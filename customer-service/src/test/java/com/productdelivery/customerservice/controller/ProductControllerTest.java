package com.productdelivery.customerservice.controller;


import com.productdelivery.customerservice.client.FavouriteProductClient;
import com.productdelivery.customerservice.client.ProductReviewsClient;
import com.productdelivery.customerservice.client.ProductsClient;
import com.productdelivery.customerservice.client.exception.ClientBadRequestException;
import com.productdelivery.customerservice.controller.payload.NewProductReviewPayload;
import com.productdelivery.customerservice.model.FavouriteProduct;
import com.productdelivery.customerservice.model.Product;
import com.productdelivery.customerservice.model.ProductReview;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpResponse;
import org.springframework.ui.ConcurrentModel;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    ProductsClient productsClient;

    @Mock
    FavouriteProductClient favouriteProductClient;

    @Mock
    ProductReviewsClient productReviewsClient;

    @InjectMocks
    ProductController controller;

    @Test
    void loadProduct_ProductExists_ReturnsNotEmptyMono() {
        // given
        var product = new Product(1, "Товар №1", "Описание товара №1");
        doReturn(Mono.just(product)).when(this.productsClient).findProduct(1);

        // when
        StepVerifier.create(this.controller.loadProduct(1))
                // then
                .expectNext(new Product(1, "Товар №1", "Описание товара №1"))
                .expectComplete()
                .verify();

        verify(this.productsClient).findProduct(1);
        verifyNoMoreInteractions(this.productsClient);
        verifyNoMoreInteractions(this.favouriteProductClient, this.productReviewsClient);
    }

    @Test
    void loadProduct_ProductDoesNotExist_ReturnsMonoWithNoSuchElementException() {
        // given
        doReturn(Mono.empty()).when(this.productsClient).findProduct(1);

        // when
        StepVerifier.create(this.controller.loadProduct(1))
                // then
                .expectErrorMatches(exception -> exception instanceof NoSuchElementException e &&
                        e.getMessage().equals("customer.products.error.not_found"))
                .verify();

        verify(this.productsClient).findProduct(1);
        verifyNoMoreInteractions(this.productsClient);
        verifyNoMoreInteractions(this.favouriteProductClient, this.productReviewsClient);
    }

    @Test
    void getProductPage_ReturnsProductPage() {
        // given
        var model = new ConcurrentModel();
        var productReviews = List.of(
                new ProductReview(UUID.fromString("0203d262-b8f5-489c-8128-015b5539bbb4"),
                        1, 5, "Good stuff"),
                new ProductReview(UUID.fromString("6029d9e6-d75c-42d2-a478-fa48f1384c99"),
                        1, 4, "No bad"));

        doReturn(Flux.fromIterable(productReviews)).when(this.productReviewsClient)
                .findProductReviewsByProductId(1);

        var favouriteProduct =
                new FavouriteProduct(UUID.fromString("9cace9c2-7304-49f4-9ff1-e1b66fa7ab3c"), 1);
        doReturn(Mono.just(favouriteProduct)).when(this.favouriteProductClient).findFavouriteProductByProductId(1);

        // when
        StepVerifier.create(this.controller.getProductPage(
                        Mono.just(new Product(1, "Chocolate", "Delicious chocolate bar")), model))
                // then
                .expectNext("customer/products/product")
                .verifyComplete();

        assertEquals(productReviews, model.getAttribute("reviews"));
        assertEquals(true, model.getAttribute("inFavourite"));

        verify(this.productReviewsClient).findProductReviewsByProductId(1);
        verify(this.favouriteProductClient).findFavouriteProductByProductId(1);
        verifyNoMoreInteractions(this.productReviewsClient, this.favouriteProductClient);
        verifyNoInteractions(this.productsClient);
    }

    @Test
    void addProductToFavourites_RequestIsValid_RedirectsToProductPage() {
        // given
        doReturn(Mono.just(
                new FavouriteProduct(UUID.fromString("ee1b66e2-8b44-4552-b780-ac0e3d34f37d"), 1)))
                .when(this.favouriteProductClient).addProductToFavourites(1);

        // when
        StepVerifier.create(this.controller.addProductToFavourites(
                        Mono.just(new Product(1, "Chocolate", "Delicious chocolate bar"))))
                // then
                .expectNext("redirect:/customer/products/1")
                .verifyComplete();

        verify(this.favouriteProductClient).addProductToFavourites(1);
        verifyNoMoreInteractions(this.favouriteProductClient);
        verifyNoInteractions(this.productsClient, this.productReviewsClient);
    }

    @Test
    void addProductToFavourites_RequestIsInvalid_RedirectsToProductPage() {
        // given
        doReturn(Mono.error(new ClientBadRequestException("bad request exception", null, List.of("errors"))))
                .when(this.favouriteProductClient).addProductToFavourites(1);

        // when
        StepVerifier.create(this.controller.addProductToFavourites(
                        Mono.just(new Product(1, "Chocolate", "Delicious chocolate bar"))))
                // then
                .expectNext("redirect:/customer/products/1")
                .verifyComplete();

        verify(this.favouriteProductClient).addProductToFavourites(1);
        verifyNoMoreInteractions(this.favouriteProductClient);
        verifyNoInteractions(this.productsClient, this.productReviewsClient);
    }

    @Test
    void removeProductFromFavourites_RedirectsToProductPage() {
        // given
        doReturn(Mono.empty()).when(this.favouriteProductClient).removeProductFromFavourites(1);

        // when
        StepVerifier.create(this.controller.removeProductFromFavourites(
                        Mono.just(new Product(1, "Chocolate", "Delicious chocolate bar"))))
                // then
                .expectNext("redirect:/customer/products/1")
                .verifyComplete();

        verify(this.favouriteProductClient).removeProductFromFavourites(1);
        verifyNoMoreInteractions(this.favouriteProductClient);
        verifyNoInteractions(this.productReviewsClient, this.productsClient);
    }

    @Test
    void createReview_RequestIsValid_RedirectsToProductPage() {
        // given
        var model = new ConcurrentModel();
        var response = new MockServerHttpResponse();

        doReturn(Mono.just(new ProductReview(UUID.fromString("18c33d21-4e45-4134-9cf0-fffb4bad07f8"),
                1, 5, "Good stuff")))
                .when(this.productReviewsClient).createProductReview(1, 5, "Good stuff");

        // when
        StepVerifier.create(this.controller.createReview(
                        Mono.just(new Product(1, "Chocolate", "Good chocolate")),
                        new NewProductReviewPayload(5, "Good stuff"), model, response))
                // then
                .expectNext("redirect:/customer/products/1")
                .verifyComplete();

        assertNull(response.getStatusCode());

        verify(this.productReviewsClient).createProductReview(1, 5, "Good stuff");
        verifyNoMoreInteractions(this.productReviewsClient);
        verifyNoInteractions(this.productsClient, this.favouriteProductClient);
    }

    @Test
    void createReview_RequestIsInvalid_ReturnsProductPageWithPayloadAndErrors() {
        // given
        var model = new ConcurrentModel();
        var response = new MockServerHttpResponse();

        var favouriteProduct =
                new FavouriteProduct(UUID.fromString("327245fe-384d-4457-af40-de37788a7f9a"), 1);
        doReturn(Mono.just(favouriteProduct)).when(this.favouriteProductClient).findFavouriteProductByProductId(1);

        doReturn(Mono.error(new ClientBadRequestException("Anything error", null, List.of("Error 1", "Error 2"))))
                .when(this.productReviewsClient).createProductReview(1, null, "bad");

        // when
        StepVerifier.create(this.controller.createReview(
                        Mono.just(new Product(1, "Chocolate", "Good chocolate")),
                        new NewProductReviewPayload(null, "bad"), model, response))
                // then
                .expectNext("customer/products/product")
                .verifyComplete();

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(true, model.getAttribute("inFavourite"));
        assertEquals(new NewProductReviewPayload(null, "bad"), model.getAttribute("payload"));
        assertEquals(List.of("Error 1", "Error 2"), model.getAttribute("errors"));

        verify(this.productReviewsClient).createProductReview(1, null, "bad");
        verify(this.favouriteProductClient).findFavouriteProductByProductId(1);
        verifyNoMoreInteractions(this.productsClient, this.favouriteProductClient);
        verifyNoInteractions(this.productsClient);
    }

    @Test
    void handleNoSuchElementException_ReturnsError404Page() {
        // given
        var exception = new NoSuchElementException("Товар не найден");
        var model = new ConcurrentModel();
        var response = new MockServerHttpResponse();

        // when
        var result = this.controller.handleNoSuchElementException(exception, model, response);

        // then
        assertEquals("errors/404", result);
        assertEquals("Товар не найден", model.getAttribute("error"));
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}