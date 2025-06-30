package com.deliciouspizza.model.product;

import com.deliciouspizza.model.orders_products.OrderProduct;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "products")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "category", discriminatorType = DiscriminatorType.STRING)
@NoArgsConstructor
@Data
public abstract class Product {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

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

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    private Set<OrderProduct> orderProductsInOrders = new HashSet<>(); // Name it clearly

    private byte[] imageData;

    public Product(ProductCategory category, String name,  ProductStatus status, ProductSize size, BigDecimal price, boolean active, BigDecimal totalAmount) {
        this.category = category;
        this.name = name;
        this.status = status;
        this.size = size;
        this.price = price;
        this.active = active;
        this.totalAmount = totalAmount;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

}

