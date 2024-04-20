package com.productdelivery.customerservice.controller;

import com.productdelivery.customerservice.client.FavouriteProductClient;
import com.productdelivery.customerservice.client.ProductsClient;
import com.productdelivery.customerservice.model.FavouriteProduct;
import com.productdelivery.customerservice.model.Product;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ConcurrentModel;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class ProductsControllerTest {

    @Mock
    ProductsClient productsClient;

    @Mock
    FavouriteProductClient favouriteProductClient;

    @InjectMocks
    ProductsController controller;

    @Test
    void getProductsListPage_ReturnsProductsListPage() {
        // given
        var model = new ConcurrentModel();

        doReturn(Flux.fromIterable(List.of(
                new Product(1, "Товар №1", "Описание товара №1"),
                new Product(2, "Товар №2", "Описание товара №2"),
                new Product(3, "Товар №3", "Описание товара №3")
        ))).when(this.productsClient).findAllProducts("товар");

        // when
        StepVerifier.create(this.controller.getProductsListPage(model, "товар"))
                // then
                .expectNext("customer/products/list")
                .verifyComplete();

        assertEquals("товар", model.getAttribute("filter"));
        assertEquals(List.of(
                        new Product(1, "Товар №1", "Описание товара №1"),
                        new Product(2, "Товар №2", "Описание товара №2"),
                        new Product(3, "Товар №3", "Описание товара №3")),
                model.getAttribute("products"));


        verify(this.productsClient).findAllProducts("товар");
        verifyNoMoreInteractions(this.productsClient);
        verifyNoInteractions(this.favouriteProductClient);
    }

    @Test
    void getFavouritesProductsPage_ReturnsFavouritesProductsPage() {
        // given
        var model = new ConcurrentModel();

        doReturn(Flux.fromIterable(List.of(
                new FavouriteProduct(UUID.fromString("4688e5a1-26f7-47b6-be6b-db103f7f00b3"), 1),
                new FavouriteProduct(UUID.fromString("cf1a4d28-0ba3-4403-bacf-eb0fc267c2a2"), 2)
        ))).when(this.favouriteProductClient).findFavouriteProducts();

        doReturn(Flux.fromIterable(List.of(
                new Product(1, "Товар №1", "Описание товара №1"),
                new Product(2, "Товар №2", "Описание товара №2"),
                new Product(3, "Товар №3", "Описание товара №3")
        ))).when(this.productsClient).findAllProducts("товар");

        // when
        StepVerifier.create(this.controller.getFavouritesProductsPage(model, "товар"))
                // then
                .expectNext("customer/products/favourites")
                .verifyComplete();

        assertEquals("товар", model.getAttribute("filter"));
        assertEquals(List.of(
                new Product(1, "Товар №1", "Описание товара №1"),
                new Product(2, "Товар №2", "Описание товара №2")
        ), model.getAttribute("products"));

        verify(this.favouriteProductClient).findFavouriteProducts();
        verify(this.productsClient).findAllProducts("товар");
        verifyNoMoreInteractions(this.favouriteProductClient, this.productsClient);
    }

}