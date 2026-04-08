package com.smartlogix.pedidos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class ProductQuantity {
    @Column(name = "product_id")
    private Long productId;

    private Long quantity;

}
