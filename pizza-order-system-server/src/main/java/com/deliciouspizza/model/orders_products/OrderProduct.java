package com.deliciouspizza.model.orders_products;

import com.deliciouspizza.model.order.Order;
import com.deliciouspizza.model.product.Product;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "orders_products")
@Data
@NoArgsConstructor
public class OrderProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "orders_products_id_seq_generator")
    @SequenceGenerator(name = "orders_products_id_seq_generator", sequenceName = "orders_products_id_seq", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    // Many-to-one relationship with the Order entity
    // Multiple OrderProduct entries can belong to one Order
    @ManyToOne(fetch = FetchType.LAZY) // Lazy loading is generally good for performance
    @JoinColumn(name = "order_id", nullable = false) // Maps to the 'order_id' column in orders_products
    private Order order;

    // Many-to-one relationship with the Product entity
    // Multiple OrderProduct entries can reference one Product
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "price_at_order_time", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceAtOrderTime;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        OrderProduct that = (OrderProduct) o;
        return Objects.equals(order.getId(), that.order.getId()) && Objects.equals(product, that.product);
    }

    @Override
    public int hashCode() {
        return Objects.hash(order.getId(), product.getId());
    }
}