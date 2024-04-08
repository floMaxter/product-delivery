package com.productdelivery.customerservice.client.payload;

public record NewProductReviewPayload(Integer productId, Integer rating, String review) {
}
