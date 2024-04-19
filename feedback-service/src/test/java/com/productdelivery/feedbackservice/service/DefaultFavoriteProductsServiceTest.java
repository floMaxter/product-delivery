package com.productdelivery.feedbackservice.service;

import com.productdelivery.feedbackservice.model.FavouriteProduct;
import com.productdelivery.feedbackservice.repository.FavouriteProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DefaultFavoriteProductsServiceTest {

    @Mock
    FavouriteProductRepository favouriteProductRepository;

    @InjectMocks
    DefaultFavoriteProductsService service;

    @Test
    void addProductToFavourites_ReturnsAddedFavouriteProduct() {
        // given
        doAnswer(invocation -> Mono.justOrEmpty(invocation.getArguments()[0]))
                .when(this.favouriteProductRepository).save(any());

        // when
        StepVerifier.create(this.service
                        .addProductToFavourites(1, "9fcb9f42-0a9d-4430-a0d3-105931c78d58"))
                // then
                .expectNextMatches(favouriteProduct -> favouriteProduct.getProductId() == 1 &&
                        favouriteProduct.getUserId().equals("9fcb9f42-0a9d-4430-a0d3-105931c78d58") &&
                        favouriteProduct.getId() != null)
                .verifyComplete();

        verify(this.favouriteProductRepository).save(argThat(favouriteProductRepository ->
                favouriteProductRepository.getProductId() == 1 &&
                        favouriteProductRepository.getUserId().equals("9fcb9f42-0a9d-4430-a0d3-105931c78d58") &&
                        favouriteProductRepository.getId() != null));
    }

    @Test
    void removeProductFromFavourites_ReturnsEmptyMono() {
        // given
        doReturn(Mono.empty()).when(this.favouriteProductRepository)
                .deleteByProductIdAndUserId(1, "9fcb9f42-0a9d-4430-a0d3-105931c78d58");

        // when
        StepVerifier.create(this.service
                        .removeProductFromFavourites(1, "9fcb9f42-0a9d-4430-a0d3-105931c78d58"))
                // then
                .verifyComplete();
    }

    @Test
    void findFavouriteProductByProduct_ReturnsFavouriteProduct() {
        // given
        doReturn(Mono.just(new FavouriteProduct(UUID.fromString("2e94bcc6-16a5-4577-8b65-15180f8ef2e7"),
                1, "fe4e545f-6d30-4253-bea6-4df3dc1f2cfa")))
                .when(this.favouriteProductRepository)
                .findByProductIdAndUserId(1, "fe4e545f-6d30-4253-bea6-4df3dc1f2cfa");

        // when
        StepVerifier.create(this.service
                .findFavouriteProductByProduct(1, "fe4e545f-6d30-4253-bea6-4df3dc1f2cfa"))
                // then
                .expectNext(new FavouriteProduct(UUID.fromString("2e94bcc6-16a5-4577-8b65-15180f8ef2e7"),
                        1, "fe4e545f-6d30-4253-bea6-4df3dc1f2cfa"))
                .verifyComplete();
    }

    @Test
    void findFavouriteProducts_ReturnFavouriteProducts() {
        // given
        doReturn(Flux.fromIterable(List.of(
                new FavouriteProduct(UUID.fromString("464d87af-49b7-4627-8892-4e237a90a5c8"),1,
                        "a77e0cd2-3fd8-4c49-8146-432e7a2ff8dd"),
                new FavouriteProduct(UUID.fromString("b3aed5d5-1dcb-462d-9dd0-47c7e736a0d6"),2,
                        "a77e0cd2-3fd8-4c49-8146-432e7a2ff8dd")
        ))).when(this.favouriteProductRepository).findAllByUserId("a77e0cd2-3fd8-4c49-8146-432e7a2ff8dd");

        // when
        StepVerifier.create(this.service.findFavouriteProducts("a77e0cd2-3fd8-4c49-8146-432e7a2ff8dd"))
                // then
                .expectNext(
                        new FavouriteProduct(UUID.fromString("464d87af-49b7-4627-8892-4e237a90a5c8"),1,
                                "a77e0cd2-3fd8-4c49-8146-432e7a2ff8dd"),
                        new FavouriteProduct(UUID.fromString("b3aed5d5-1dcb-462d-9dd0-47c7e736a0d6"),2,
                                "a77e0cd2-3fd8-4c49-8146-432e7a2ff8dd"))
                .verifyComplete();
    }
}