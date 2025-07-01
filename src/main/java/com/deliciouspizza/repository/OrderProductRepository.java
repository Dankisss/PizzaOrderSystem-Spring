package com.deliciouspizza.repository;

import com.deliciouspizza.model.orders_products.OrderProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderProductRepository extends JpaRepository<OrderProduct, Long> {

    /**
     * Finds all OrderProduct entities associated with a given order ID.
     * Spring Data JPA automatically generates the query based on the method name.
     *
     * @param orderId The ID of the order.
     * @return A list of OrderProduct entities belonging to the specified order.
     */
    List<OrderProduct> findByOrderId(Long orderId);

    Optional<OrderProduct> findByOrder_IdAndProduct_Id(Long orderId, Long productId);

    void deleteByOrder_IdAndProduct_Id(Long orderId, Long productId);
}
