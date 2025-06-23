package com.deliciouspizza.model.product;

import com.deliciouspizza.model.order.Order;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;

@Entity
@Table(name = "products")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "category", discriminatorType = DiscriminatorType.STRING)
@NoArgsConstructor
@Data
public abstract class Product {

    @Id @GeneratedValue
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status = ProductStatus.INACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(insertable = false, updatable = false)
    private ProductCategory category;

    @Enumerated(EnumType.STRING)
    private ProductSize size;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "total_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt = Instant.now();

    @ManyToMany
    @JoinTable(
            name = "orders_items",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "order_id")
    )
    private Set<Order> orders;

    private byte[] imageData;

    public Product(ProductCategory category, ProductStatus status, ProductSize size, BigDecimal price, boolean active, BigDecimal totalAmount) {
        this.category = category;
        this.status = status;
        this.size = size;
        this.price = price;
        this.active = active;
        this.totalAmount = totalAmount;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

}

