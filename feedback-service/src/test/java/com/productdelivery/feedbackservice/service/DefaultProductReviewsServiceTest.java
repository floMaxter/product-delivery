package com.productdelivery.feedbackservice.service;

import com.productdelivery.feedbackservice.model.ProductReview;
import com.productdelivery.feedbackservice.repository.ProductReviewRepository;
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
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class DefaultProductReviewsServiceTest {

    @Mock
    ProductReviewRepository productReviewRepository;

    @InjectMocks
    DefaultProductReviewsService service;

    @Test
    void createProductReview_ReturnsCreatedProductReview() {
        // given
        doAnswer(invocation -> Mono.justOrEmpty(invocation.getArguments()[0])).when(this.productReviewRepository)
                .save(any());

        // when
        StepVerifier.create(this.service.createProductReview(1, 3, "Такое себе",
                        "ccaa86c0-6037-4b28-93f8-d2a4308c137c"))
                // then
                .expectNextMatches(productReview ->
                        productReview.getProductId() == 1 && productReview.getRating() == 3 &&
                                productReview.getUserId().equals("ccaa86c0-6037-4b28-93f8-d2a4308c137c") &&
                                productReview.getReview().equals("Такое себе") &&
                                productReview.getId() != null)
                .verifyComplete();

    }

    @Test
    void findProductReviewsByProduct_ReturnsProductReviews() {
        // given
        doReturn(Flux.fromIterable(List.of(
                new ProductReview(UUID.fromString("67c9d350-3ec0-4238-a616-079643411f4c"), 1, 3,
                        "Отзыв №1", "user-1"),
                new ProductReview(UUID.fromString("b416b69f-4392-491f-a9ef-7629f026eb34"), 1, 4,
                        "Отзыв №2", "user-1"),
                new ProductReview(UUID.fromString("1bd7d60b-5830-47a2-a584-4f3294905fe8"), 1, 5,
                        "Отзыв №3", "user-1"))))
                .when(this.productReviewRepository).findAllByProductId(1);

        // when
        StepVerifier.create(this.service.findProductReviewsByProduct(1))
                // then
                .expectNext(
                        new ProductReview(UUID.fromString("67c9d350-3ec0-4238-a616-079643411f4c"),
                                1, 3, "Отзыв №1", "user-1"),
                        new ProductReview(UUID.fromString("b416b69f-4392-491f-a9ef-7629f026eb34"),
                                1, 4, "Отзыв №2", "user-1"),
                        new ProductReview(UUID.fromString("1bd7d60b-5830-47a2-a584-4f3294905fe8"),
                                1, 5, "Отзыв №3", "user-1"))
                .verifyComplete();
    }
}