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
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
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
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "products_id_seq_generator")
    @SequenceGenerator(name = "products_id_seq_generator", sequenceName = "products_id_seq", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status = ProductStatus.ACTIVE; // Changed default to ACTIVE, as per DDL or common sense

    @Enumerated(EnumType.STRING)
    @Column(insertable = false, updatable = false)
    private ProductCategory category;

    @Enumerated(EnumType.STRING)
    private ProductSize capacity;

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
    private Set<OrderProduct> orderProductsInOrders = new HashSet<>();

    private byte[] imageData;

    public Product(
            ProductCategory category,
            String name,
            String description,
            ProductStatus status,
            ProductSize size,
            BigDecimal price,
            boolean active,
            BigDecimal totalAmount
    ) {
        this.category = category;
        this.name = name;
        this.description = description;
        this.status = status;
        this.capacity = size;
        this.price = price;
        this.active = active;
        this.totalAmount = totalAmount;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

}

