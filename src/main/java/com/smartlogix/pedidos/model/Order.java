package com.smartlogix.pedidos.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String customerName;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @ElementCollection
    @CollectionTable(name = "product_quantity", joinColumns = @JoinColumn(name = "id"))
    @Column(nullable = false)
    private List<ProductQuantity> productList;

}
