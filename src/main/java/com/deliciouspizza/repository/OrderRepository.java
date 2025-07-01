package com.deliciouspizza.repository;

import com.deliciouspizza.model.order.Order;
import com.deliciouspizza.model.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    List<Order> findByUser_Username(String username);

}
