package com.deliciouspizza.model.order;

import com.deliciouspizza.model.orders_products.OrderProduct;
import com.deliciouspizza.model.product.Product;
import com.deliciouspizza.model.user.User;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "ORDERS")
@Data
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Change from AUTO for PostgreSQL SERIAL
    private Long id;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @ManyToOne(fetch = FetchType.LAZY) // Always specify fetch type
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false) // Add nullable=false if address is always required
    private String address;

    // Remove @Temporal for LocalDateTime, it's not needed for modern JPA (2.2+)
    @Column(name = "created_at", nullable = false, updatable = false) // Make it non-updatable if default
    private LocalDateTime createdAt;

    // Remove @Temporal for LocalDateTime
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Correct mapping: One Order has Many OrderProduct (line items)
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<OrderProduct> orderProducts = new HashSet<>();


}
