package com.productdelivery.managerservice.controller;

import com.productdelivery.managerservice.client.BadRequestException;
import com.productdelivery.managerservice.client.ProductsRestClient;
import com.productdelivery.managerservice.controller.payload.NewProductPayload;
import com.productdelivery.managerservice.model.Product;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ConcurrentModel;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class ProductsControllerTest {
    @Mock
    ProductsRestClient productsRestClient;

    @InjectMocks
    ProductsController controller;

    @Test
    void getProductsList_ReturnsProductsListPage() {
        // given
        var model = new ConcurrentModel();
        var filter = "Товар";
        var products = IntStream.range(1, 4)
                .mapToObj(i -> new Product(i, "Товар №%d".formatted(i),
                        "Описание товара №%d".formatted(i)))
                .toList();

        doReturn(products).when(this.productsRestClient).findAllProducts(filter);

        // when
        var result = this.controller.getProductsList(model, filter);

        // then
        assertEquals("catalog/products/list", result);
        assertEquals(filter, model.getAttribute("filter"));
        assertEquals(products, model.getAttribute("products"));
    }

    @Test
    void getNewProductPage_ReturnsNewProductPage() {
        // given

        // when
        var result = this.controller.getNewProductPage();

        // then
        assertEquals("catalog/products/new_product", result);
    }

    @Test
    void createProduct_RequestIsValid_ReturnsRedirectionToProductPage() {
        // given
        var payload = new NewProductPayload("Новый товар", "Описание нового товара");
        var model = new ConcurrentModel();

        doReturn(new Product(1, "Новый товар", "Описание нового товара"))
                .when(this.productsRestClient)
                .createProduct("Новый товар", "Описание нового товара");

        // when
        var result = this.controller.createProduct(payload, model);

        // then
        assertEquals("redirect:/catalog/products/1", result);

        verify(this.productsRestClient).createProduct("Новый товар", "Описание нового товара");
        verifyNoMoreInteractions(this.productsRestClient);
    }

    @Test
    void createProduct_RequestIsInvalid_ReturnsProductFormWithErrors() {
        // given
        var payload = new NewProductPayload("   ", null);
        var model = new ConcurrentModel();

        doThrow(new BadRequestException(List.of("Ошибка 1", "Ошибка 2")))
                .when(this.productsRestClient)
                .createProduct("   ", null);

        // when
        var result = this.controller.createProduct(payload, model);

        // then
        assertEquals("catalog/products/new_product", result);
        assertEquals(payload, model.getAttribute("payload"));
        assertEquals(List.of("Ошибка 1", "Ошибка 2"), model.getAttribute("errors"));

        verify(this.productsRestClient).createProduct("   ", null);
        verifyNoMoreInteractions(this.productsRestClient);
    }
}