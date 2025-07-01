package com.deliciouspizza.controller;

import com.deliciouspizza.dto.order.OrderFilterDto;
import com.deliciouspizza.dto.order.OrderRequestDto;
import com.deliciouspizza.dto.order.OrderResponseDto;
import com.deliciouspizza.dto.order.OrderUpdateDto;
import com.deliciouspizza.dto.order.ProcessOrderRequestDto;
import com.deliciouspizza.dto.order_product.OrderProductRequestDto;
import com.deliciouspizza.dto.order_product.OrderProductResponseDto;
import com.deliciouspizza.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Retrieves all orders.
     * GET /api/v1/orders
     * @return A list of OrderResponseDto.
     */
    @GetMapping
    public ResponseEntity<List<OrderResponseDto>> getAllOrders(@ModelAttribute OrderFilterDto filterDto) {
        List<OrderResponseDto> orders = orderService.findAllOrders(filterDto);
        return ResponseEntity.ok(orders);
    }

    /**
     * Retrieves a single order by its ID.
     * GET /api/v1/orders/{id}
     * @param id The ID of the order.
     * @return The OrderResponseDto for the specified ID, or 404 if not found.
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDto> getOrder(@PathVariable long id) {
        return ResponseEntity.ok(orderService.findOrderById(id));
    }

    /**
     * Creates a new order.
     * POST /api/v1/orders
     *
     * IMPORTANT: As requested, the logic for this method is implemented directly
     * in the controller without calling a service layer. This is for demonstration
     * purposes and is NOT recommended for production applications where business
     * logic should reside in the service layer.
     *
     * @param orderDto The Dto containing order details and initial products.
     * @return The created OrderResponseDto with a 201 status, or 400 if validation fails.
     */
    @PostMapping
    public ResponseEntity<OrderResponseDto> createNewOrder(@Valid @RequestBody OrderRequestDto orderDto) {

        return new ResponseEntity<>(orderService.createNewOrder(orderDto), HttpStatus.CREATED);
    }


    /**
     * Updates an existing order's status and/or address.
     * PUT /api/v1/orders/{id}
     * @param id The ID of the order to update.
     * @param orderUpdateDto The Dto containing fields to update.
     * @return The updated OrderResponseDto, or 404 if not found, 400 if invalid input.
     */
    @PutMapping("/{id}")
    public ResponseEntity<OrderResponseDto> updateOrder(@PathVariable long id, @Valid @RequestBody OrderUpdateDto orderUpdateDto) {
        OrderResponseDto updatedOrder = orderService.updateOrder(id, orderUpdateDto);
        if (updatedOrder != null) {
            return ResponseEntity.ok(updatedOrder);
        }
        return ResponseEntity.notFound().build(); // Or HttpStatus.BAD_REQUEST for validation issues handled in service
    }

    /**
     * Cancels/deletes an order.
     * DELETE /api/v1/orders/{id}
     * @param id The ID of the order to cancel.
     * @return 204 No Content on successful deletion, or 404 if not found.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable long id) {
        orderService.deleteOrder(id);

        return ResponseEntity.noContent().build();
    }

    /**
     * Lists products in a specific order.
     * GET /api/v1/orders/{id}/products
     * @param id The ID of the order.
     * @return A list of OrderProductResponseDto for the order, or 404 if order not found.
     */
    @GetMapping("/{id}/products")
    public ResponseEntity<List<OrderProductResponseDto>> getProductsInOrder(@PathVariable long id) {
        List<OrderProductResponseDto> products = orderService.findItemsByOrderId(id);
        if (products != null) {
            return ResponseEntity.ok(products);
        }
        return ResponseEntity.notFound().build(); // Assuming service returns null if order doesn't exist
    }

    /**
     * Adds a product to an existing order.
     * POST /api/v1/orders/{id}/products
     * @param id The ID of the order to add product to.
     * @param productDto The Dto containing product ID and quantity.
     * @return The updated OrderResponseDto, or 404 if order/product not found, 400 if invalid input.
     */
    @PostMapping("/{id}/products")
    public ResponseEntity<OrderResponseDto> addProductToOrder(@PathVariable long id, @Valid @RequestBody OrderProductRequestDto productDto) {
        OrderResponseDto updatedOrder = orderService.addProductToOrder(id, productDto);

        if (updatedOrder != null) {
            return ResponseEntity.ok(updatedOrder);
        }

        return ResponseEntity.notFound().build();
    }

    /**
     * Removes an product from a specific order.
     * DELETE /api/v1/orders/{orderId}/products/{productId}
     * @param orderId The ID of the order.
     * @param productId The ID of the specific product (from orders_products table) to remove.
     * @return 204 No Content on successful removal, or 404 if order/product not found.
     */
    @DeleteMapping("/{orderId}/products/{productId}")
    public ResponseEntity<Void> removeProductFromOrder(@PathVariable long orderId, @PathVariable long productId) {
        orderService.removeProductFromOrder(orderId, productId);

        return ResponseEntity.noContent().build();
    }
    
    @PatchMapping("/{orderId}/products/{productId}")
    public ResponseEntity<OrderProductResponseDto> updateCount(@PathVariable long orderId, @PathVariable long productId, @RequestParam int newCount) {
        return ResponseEntity.ok(orderService.updateProductCount(orderId, productId, newCount));
    }

    @PatchMapping("/{orderId}")
    public ResponseEntity<OrderResponseDto> processOrder(@PathVariable long orderId, @RequestBody ProcessOrderRequestDto requestDto) {
        return ResponseEntity.ok(orderService.processOrder(orderId, requestDto));
    }
    // You could also consider adding:
    // - PATCH /api/v1/orders/{orderId}/products/{productId} to update quantity of an product

    // TODO: This should be implemented using JWT authorization
    //    @PostMapping("/auth/login")
    //    public ResponseEntity<User> signIn() {
    //
    //    }
}
