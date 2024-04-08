package com.productdelivery.feedbackservice.controller;

import com.productdelivery.feedbackservice.controller.payload.NewProductReviewPayload;
import com.productdelivery.feedbackservice.model.ProductReview;
import com.productdelivery.feedbackservice.service.ProductReviewsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("feedback-api/product-reviews")
public class ProductReviewsRestController {

    private final ProductReviewsService productReviewsService;

    @GetMapping("by-product-id/{productId:\\d+}")
    public Flux<ProductReview> findProductReviewsByProductId(@PathVariable("productId") int productId) {
        return this.productReviewsService.findProductReviewsByProduct(productId);
    }

    @PostMapping
    public Mono<ResponseEntity<ProductReview>> createProductReview(
            @Valid @RequestBody Mono<NewProductReviewPayload> payloadMono,
            UriComponentsBuilder uriComponentsBuilder) {
        return payloadMono
                .flatMap(payload -> this.productReviewsService.createProductReview(payload.productId(),
                        payload.rating(), payload.review()))
                .map(productReview -> ResponseEntity
                        .created(uriComponentsBuilder.replacePath("/feedback-api/product-reviews/{id}")
                                .build(productReview.getProductId()))
                        .body(productReview));
    }
}