package com.productdelivery.productservice.controller;

import com.productdelivery.productservice.controller.payload.NewProductPayload;
import com.productdelivery.productservice.model.Product;
import com.productdelivery.productservice.service.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.MapBindingResult;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class ProductsRestControllerTest {

    @Mock
    ProductService productService;

    @InjectMocks
    ProductsRestController restController;

    @Test
    void findProducts_ReturnsProductsList() {
        // given
        var filter = "товар";

        doReturn(List.of(new Product(1, "Товар №1", "Описание товара №1"),
                new Product(2, "Товар №2", "Описание товара №2")))
                .when(this.productService)
                .findAllProducts("товар");

        // when
        var result = this.restController.findProducts(filter);

        // then
        assertEquals(List.of(new Product(1, "Товар №1", "Описание товара №1"),
                new Product(2, "Товар №2", "Описание товара №2")), result);
    }

    @Test
    void createProduct_RequestIsValid_ReturnsNoContent() throws BindException {
        // given
        var payload = new NewProductPayload("Новый товар", "Описание нового товара");
        var bindingResult = new MapBindingResult(Map.of(), "payload");
        var uriComponentsBuilder = UriComponentsBuilder.fromUriString("http://localhost");

        doReturn(new Product(1, "Новое название товара", "Новое описание товара"))
                .when(productService).createProduct("Новый товар", "Описание нового товара");

        // when
        var result = this.restController.createProduct(payload, bindingResult, uriComponentsBuilder);

        // then
        assertNotNull(result);
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(URI.create("http://localhost/catalog-api/products/1"), result.getHeaders().getLocation());
        assertEquals(new Product(1, "Новое название товара", "Новое описание товара"),
                result.getBody());

        verify(this.productService).createProduct("Новый товар", "Описание нового товара");
        verifyNoMoreInteractions(this.productService);
    }

    @Test
    void createProduct_RequestIsInvalid_ReturnsProductFormWithErrors() {
        // given
        var payload = new NewProductPayload("  ", null);
        var bindingResult = new MapBindingResult(Map.of(), "payload");
        bindingResult.addError(new FieldError("payload", "title", "error"));
        var uriComponentsBuilder = UriComponentsBuilder.fromUriString("http://localhost");

        // when
        var exception = assertThrows(BindException.class,
                () -> this.restController.createProduct(payload, bindingResult, uriComponentsBuilder));

        // then
        assertEquals(List.of(new FieldError("payload", "title", "error")),
                exception.getAllErrors());
        verifyNoMoreInteractions(this.productService);
    }

    @Test
    void createProduct_RequestIsInvalidAndBindingResultIsBindException_ReturnBadRequest() {
        // given
        var payload = new NewProductPayload("  ", null);
        var bindingResult = new BindException(new MapBindingResult(Map.of(), "payload"));
        bindingResult.addError(new FieldError("payload", "title", "error"));
        var uriComponentsBuilder = UriComponentsBuilder.fromUriString("http://localhost");

        // when
        var exception = assertThrows(BindException.class,
                () -> this.restController.createProduct(payload, bindingResult, uriComponentsBuilder));

        // then
        assertEquals(List.of(new FieldError("payload", "title", "error")),
                exception.getAllErrors());
        verifyNoMoreInteractions(this.productService);
    }


}