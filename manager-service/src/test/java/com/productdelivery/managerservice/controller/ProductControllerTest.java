package com.productdelivery.managerservice.controller;

import com.productdelivery.managerservice.client.BadRequestException;
import com.productdelivery.managerservice.client.ProductsRestClient;
import com.productdelivery.managerservice.controller.payload.UpdateProductPayload;
import com.productdelivery.managerservice.model.Product;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.ui.ConcurrentModel;

import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    ProductsRestClient productsRestClient;

    @Mock
    MessageSource messageSource;

    @InjectMocks
    ProductController controller;

    @Test
    void product_ProductExists_ReturnsProduct() {
        // given
        var product = new Product(1, "Чоколадка", "Вкусная чоколадка");

        doReturn(Optional.of(product)).when(this.productsRestClient).findProduct(1);

        // when
        var result = this.controller.product(1);

        // then
        assertEquals(product, result);

        verify(this.productsRestClient).findProduct(1);
        verifyNoMoreInteractions(this.productsRestClient);
    }

    @Test
    void product_ProductDoesNotExist_ThrowNoSuchElementException() {
        // given

        // when
        var exception = assertThrows(NoSuchElementException.class, () -> this.controller.product(1));

        // then
        assertEquals("catalog.errors.product.not_found", exception.getMessage());
        verify(this.productsRestClient).findProduct(1);
        verifyNoMoreInteractions(this.productsRestClient);
    }

    @Test
    void getProduct_ReturnsProductPage() {
        // given

        // when
        var result = this.controller.getProduct();

        // then
        assertEquals("catalog/products/product", result);
        verifyNoMoreInteractions(this.productsRestClient);
    }

    @Test
    void getProductEditPage_ReturnsProductEditPage() {
        // given

        // when
        var result = this.controller.getProductEditPage();

        // then
        assertEquals("catalog/products/edit", result);
        verifyNoMoreInteractions(this.productsRestClient);
    }

    @Test
    void updateProduct_RequestIsValid_RedirectToProductPage() {
        // given
        var product = new Product(1, "Новый товар", "Описание нового товара");
        var payload = new UpdateProductPayload("Новое название", "Новое описание");
        var model = new ConcurrentModel();

        // when
        var result = this.controller.updateProduct(product, payload, model);

        // then
        assertEquals("redirect:/catalog/products/1", result);
        verify(this.productsRestClient).updateProduct(1, "Новое название", "Новое описание");
        verifyNoMoreInteractions(this.productsRestClient);
    }

    @Test
    void updateProduct_RequestIsInvalid_ReturnsProductEditPage() {
        // given
        var product = new Product(1, "Новый товар", "Описание нового товара");
        var payload = new UpdateProductPayload("   ", null);
        var model = new ConcurrentModel();

        doThrow(new BadRequestException(List.of("Ошибка 1", "Ошибка 2")))
                .when(this.productsRestClient).updateProduct(1, "   ", null);

        // when
        var result = this.controller.updateProduct(product, payload, model);

        // then
        assertEquals("catalog/products/edit", result);
        assertEquals(payload, model.getAttribute("payload"));
        assertEquals(List.of("Ошибка 1", "Ошибка 2"), model.getAttribute("errors"));

        verify(this.productsRestClient).updateProduct(1, "   ", null);
        verifyNoMoreInteractions(this.productsRestClient);
    }

    @Test
    void deleteProduct_RedirectsToProductsListPage() {
        // given
        var product = new Product(1, "Новый товар", "Описание нового товара");

        // when
        var result = this.controller.deleteProduct(product);

        // then
        assertEquals("redirect:/catalog/products/list", result);

        verify(this.productsRestClient).deleteProduct(1);
        verifyNoMoreInteractions(this.productsRestClient);
    }

    @Test
    void handleNoSuchElementException_Returns404ErrorPage() {
        // given
        var exception = new NoSuchElementException("error");
        var model = new ConcurrentModel();
        var response = new MockHttpServletResponse();
        var locale = Locale.of("ru", "RU");

        doReturn("Ошибка").when(this.messageSource)
                .getMessage("error", new Object[0], "error",
                        Locale.of("ru", "RU"));

        // when
        var result = this.controller.handleNoSuchElementException(exception, model, response, locale);

        // then
        assertEquals("errors/404", result);
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());

        verify(this.messageSource).getMessage("error", new Object[0], "error",
                Locale.of("ru", "RU"));
        verifyNoMoreInteractions(this.messageSource);
        verifyNoMoreInteractions(this.productsRestClient);
    }
}