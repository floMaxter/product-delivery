package com.productdelivery.productservice.service;

import com.productdelivery.productservice.model.Product;
import com.productdelivery.productservice.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class DefaultProductServiceTest {

    @Mock
    ProductRepository productRepository;

    @InjectMocks
    DefaultProductService service;

    @Test
    void findAllProducts_FilterIsNotSet_ReturnsProductsList() {
        // given
        var products = IntStream.range(1, 4)
                .mapToObj(i -> new Product(i, "Товар №%d".formatted(i), "Описание товара №%d".formatted(i)))
                .toList();

        doReturn(products).when(this.productRepository).findAll();

        // when
        var result = this.service.findAllProducts(null);

        // then
        assertEquals(products, result);

        verify(this.productRepository).findAll();
        verifyNoMoreInteractions(this.productRepository);
    }

    @Test
    void findAllProducts_FilterIsSet_ReturnsFilteredProductsList() {
        // given
        var filter = "%товар%";
        var products = IntStream.range(1, 4)
                .mapToObj(i -> new Product(i, "Товар №%d".formatted(i), "Описание товара №%d".formatted(i)))
                .toList();

        doReturn(products).when(this.productRepository).findAllByTitleLikeIgnoreCase(filter);

        // when
        var result = this.service.findAllProducts("товар");

        // then
        assertEquals(products, result);

        verify(this.productRepository).findAllByTitleLikeIgnoreCase(filter);
        verifyNoMoreInteractions(this.productRepository);
    }

    @Test
    void findProduct_ProductExists_ReturnsNotEmptyOptional() {
        // given
        var product = new Product(1, "Товар №1", "Описание товара №1");
        doReturn(Optional.of(product)).when(this.productRepository).findById(1);

        // when
        var result = this.service.findProduct(1);

        // then
        assertNotNull(result);
        assertTrue(result.isPresent());
        assertEquals(product, result.orElseThrow());

        verify(this.productRepository).findById(1);
        verifyNoMoreInteractions(this.productRepository);
    }

    @Test
    void findProduct_ProductDoesNotExits_ReturnsEmptyOptional() {
        // given

        // when
        var result = this.service.findProduct(1);

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(this.productRepository).findById(1);
        verifyNoMoreInteractions(this.productRepository);
    }

    @Test
    void createProduct_ReturnsCreatedProduct() {
        // given
        var title = "Чоколадка";
        var details = "Очень вкусная чоколадка";

        doReturn(new Product(1, "Чоколадка", "Очень вкусная чоколадка"))
                .when(this.productRepository)
                .save(new Product(null, "Чоколадка", "Очень вкусная чоколадка"));

        // when
        var result = this.service.createProduct(title, details);

        // then
        assertEquals(new Product(1, "Чоколадка", "Очень вкусная чоколадка"), result);

        verify(this.productRepository).save(new Product(null, "Чоколадка", "Очень вкусная чоколадка"));
        verifyNoMoreInteractions(this.productRepository);
    }

    @Test
    void updateProduct_ProductExists_UpdatesProduct() {
        // given
        var productId = 1;
        var title = "Чоколадка";
        var details = "Очень вкусная чоколадка";
        var product = new Product(1, "Чоколадка", "Очень вкусная чоколадка");

        doReturn(Optional.of(product)).when(this.productRepository).findById(1);

        // when
        this.service.updateProduct(productId, title, details);

        // then
        verify(this.productRepository).findById(productId);
        verifyNoMoreInteractions(this.productRepository);
    }

    @Test
    void updateProduct_ProductDoesNotExists_ThrowsNoSuchElementException() {
        // given
        var productId = 1;
        var title = "Чоколадка";
        var details = "Очень вкусная чоколадка";
        var product = new Product(1, "Чоколадка", "Очень вкусная чоколадка");

        // when
        assertThrows(NoSuchElementException.class,
                () -> this.service.updateProduct(productId, title, details));

        // then
        verify(this.productRepository).findById(productId);
        verifyNoMoreInteractions(this.productRepository);
    }

    @Test
    void deleteProduct_DeletesProduct() {
        // given
        var productId = 1;

        // when
        this.service.deleteProduct(1);

        // then
        verify(this.productRepository).deleteById(1);
        verifyNoMoreInteractions(this.productRepository);
    }
}