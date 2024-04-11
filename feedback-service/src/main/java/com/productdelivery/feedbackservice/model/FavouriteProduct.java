package com.productdelivery.feedbackservice.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FavouriteProduct {

    @Id
    private UUID id;

    private int productId;

    private String userId;
}
