package com.productdelivery.feedbackservice.controller;

import com.productdelivery.feedbackservice.controller.payload.NewProductReviewPayload;
import com.productdelivery.feedbackservice.model.ProductReview;
import com.productdelivery.feedbackservice.service.ProductReviewsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class ProductReviewsRestControllerTest {

    @Mock
    ProductReviewsService productReviewsService;

    @InjectMocks
    ProductReviewsRestController controller;

    @Test
    void findProductReviewsByProductId_ReturnsProductReviews() {
        // given
        doReturn(Flux.fromIterable(List.of(
                new ProductReview(UUID.fromString("bc38e7f3-e028-4523-a4a6-b1286265b1cc"), 1, 5,
                        "Отлично", "user-1"),
                new ProductReview(UUID.fromString("677c8307-2e52-42a1-af6f-02fe06865297"), 1, 4,
                        "Хорошо", "user-2"),
                new ProductReview(UUID.fromString("318762a9-7f82-4b58-96b0-b8b3f1bbf651"), 1, 3,
                        "Удовлетворительно", "user-3")
        ))).when(this.productReviewsService).findProductReviewsByProduct(1);

        // when
        StepVerifier.create(this.controller.findProductReviewsByProductId(1))
                // then
                .expectNext(
                        new ProductReview(UUID.fromString("bc38e7f3-e028-4523-a4a6-b1286265b1cc"), 1, 5,
                                "Отлично", "user-1"),
                        new ProductReview(UUID.fromString("677c8307-2e52-42a1-af6f-02fe06865297"), 1, 4,
                                "Хорошо", "user-2"),
                        new ProductReview(UUID.fromString("318762a9-7f82-4b58-96b0-b8b3f1bbf651"), 1, 3,
                                "Удовлетворительно", "user-3")
                ).verifyComplete();

        verify(this.productReviewsService).findProductReviewsByProduct(1);
        verifyNoMoreInteractions(this.productReviewsService);
    }

    @Test
    void createProductReview_ReturnsCreatedProductReview() {
        // given
        doReturn(Mono.just(new ProductReview(UUID.fromString("1dda2f75-4624-4c02-b81d-522298f9d9ef"), 1, 3,
                "Сойдет", "1dda2f75-4624-4c02-b81d-522298f9d9ef")))
                .when(this.productReviewsService)
                .createProductReview(1, 3, "Сойдет", "1dda2f75-4624-4c02-b81d-522298f9d9ef");

        // when
        StepVerifier.create(this.controller.createProductReview(
                        Mono.just(new JwtAuthenticationToken(Jwt.withTokenValue("e4.e5")
                                .headers(headers -> headers.put("foo", "bar"))
                                .claim("sub", "1dda2f75-4624-4c02-b81d-522298f9d9ef").build())),
                        Mono.just(new NewProductReviewPayload(1, 3, "Сойдет")),
                        UriComponentsBuilder.fromUriString("http://localhost")))
                // then
                .expectNext(ResponseEntity
                        .created(URI.create("http://localhost/feedback-api/product-reviews/1dda2f75-4624-4c02-b81d-522298f9d9ef"))
                        .body(new ProductReview(UUID.fromString("1dda2f75-4624-4c02-b81d-522298f9d9ef"), 1, 3,
                                "Сойдет", "1dda2f75-4624-4c02-b81d-522298f9d9ef")))
                .verifyComplete();

        verify(this.productReviewsService)
                .createProductReview(1, 3, "Сойдет", "1dda2f75-4624-4c02-b81d-522298f9d9ef");
        verifyNoMoreInteractions(this.productReviewsService);
    }
}











