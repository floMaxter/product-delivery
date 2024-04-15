package com.productdelivery.productservice.controller;


import com.productdelivery.productservice.controller.payload.UpdateProductPayload;
import com.productdelivery.productservice.model.Product;
import com.productdelivery.productservice.service.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.MapBindingResult;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class ProductRestControllerTest {

    @Mock
    ProductService productService;

    @Mock
    MessageSource messageSource;

    @InjectMocks
    ProductRestController restController;

    @Test
    void getProduct_ProductExists_ReturnsProductPage() {
        // given
        var product = new Product(1, "Товар №1", "Описание товара №1");

        doReturn(Optional.of(product)).when(this.productService).findProduct(1);

        // when
        var result = this.restController.getProduct(1);

        // then
        assertEquals(product, result);
    }

    @Test
    void getProduct_ProductDoesNotExist_ThrowsNoSuchElementException() {
        // given

        // when
        var exception = assertThrows(NoSuchElementException.class,
                () -> this.restController.getProduct(1));

        // then
        assertEquals("catalog.errors.product.not_found", exception.getMessage());
    }

    @Test
    void findProduct_ReturnsProduct() {
        // given
        var product = new Product(1, "Новый товар", "Описание нового товара");

        // then
        var result = this.restController.findProduct(product);

        // when
        assertEquals(new Product(1, "Новый товар", "Описание нового товара"), result);
    }

    @Test
    void updateProduct_RequestIsValid_ReturnsNoContent() throws BindException {
        // given
        var payload = new UpdateProductPayload("Новый товар", "Описание нового товара");
        var bindingResult = new MapBindingResult(Map.of(), "payload");

        // when
        var result = this.restController.updateProduct(1, payload, bindingResult);

        // then
        assertNotNull(result);
        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());

        verify(this.productService).updateProduct(1, "Новый товар", "Описание нового товара");
    }

    @Test
    void updateProducts_RequestIsInvalid_ReturnsBadRequest() {
        // given
        var payload = new UpdateProductPayload("  ", null);
        var bindingResult = new MapBindingResult(Map.of(), "payload");
        bindingResult.addError(new FieldError("payload", "title", "error"));

        // when
        var exception = assertThrows(BindException.class,
                () -> this.restController.updateProduct(1, payload, bindingResult));

        // then
        assertEquals(List.of(new FieldError("payload", "title", "error")),
                exception.getAllErrors());
        verifyNoMoreInteractions(this.productService);
    }

    @Test
    void updateProducts_RequestIsInvalidAndBindingResultIsBindException() {
        // given
        var payload = new UpdateProductPayload("  ", null);
        var bindingResult = new BindException(new MapBindingResult(Map.of(), "payload"));
        bindingResult.addError(new FieldError("payload", "title", "error"));

        // when
        var exception = assertThrows(BindException.class,
                () -> this.restController.updateProduct(1, payload, bindingResult));

        // then
        assertEquals(List.of(new FieldError("payload", "title", "error")),
                exception.getAllErrors());
        verifyNoMoreInteractions(this.productService);
    }

    @Test
    void deleteProduct_ReturnsNoContent() {
        // given

        // when
        var result = this.restController.deleteProduct(1);

        // then
        assertNotNull(result);
        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        verify(this.productService).deleteProduct(1);
    }

    @Test
    void handleNoSuchElementException_ReturnsNotFound() {
        // given
        var exception = new NoSuchElementException("error_code");
        var locale = Locale.of("ru");

        doReturn("error details").when(this.messageSource)
                .getMessage("error_code", new Object[0], "error_code", Locale.of("ru"));

        // when
        var result = this.restController.handleNoSuchElementException(exception, locale);

        // then
        assertNotNull(result);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND.value(), result.getStatusCode().value());
        assertInstanceOf(ProblemDetail.class, result.getBody());
        assertEquals("error details", result.getBody().getDetail());

        verifyNoMoreInteractions(this.productService);
    }
}