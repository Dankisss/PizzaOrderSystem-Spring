package com.deliciouspizza.service;

import com.deliciouspizza.dto.order.OrderFilterDto;
import com.deliciouspizza.dto.order.OrderRequestDto;
import com.deliciouspizza.dto.order.OrderResponseDto;
import com.deliciouspizza.dto.order.OrderUpdateDto;
import com.deliciouspizza.dto.order.ProcessOrderRequestDto;
import com.deliciouspizza.dto.order.ProcessOrderResponseDto;
import com.deliciouspizza.dto.order_product.OrderProductRequestDto;
import com.deliciouspizza.dto.order_product.OrderProductResponseDto;
import com.deliciouspizza.exception.FailedCalculationException;
import com.deliciouspizza.exception.InvalidCountException;
import com.deliciouspizza.exception.OrderNotFoundException;
import com.deliciouspizza.exception.OrderNotProcessedException;
import com.deliciouspizza.exception.OrderProductAlreadyExistsException;
import com.deliciouspizza.exception.OrderProductNotFoundException;
import com.deliciouspizza.exception.ProductNotFoundException;
import com.deliciouspizza.exception.UserNotFoundException;
import com.deliciouspizza.model.order.Order;
import com.deliciouspizza.model.order.OrderStatus;
import com.deliciouspizza.model.orders_products.OrderProduct;
import com.deliciouspizza.model.product.Product;
import com.deliciouspizza.model.user.User;
import com.deliciouspizza.repository.OrderProductRepository;
import com.deliciouspizza.repository.OrderRepository;
import com.deliciouspizza.repository.ProductRepository;
import com.deliciouspizza.repository.UserRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private static final int AVERAGE_CAR_SPEED = 30;

    private final OrderRepository orderRepository;
    private final OrderProductRepository orderProductRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
//    private final OpenRouteService openRouteService;

    public OrderService(
            OrderRepository orderRepository,
            OrderProductRepository orderProductRepository,
            ProductRepository productRepository,
            UserRepository userRepository
//            OpenRouteService openRouteService
    ) {
        this.orderRepository = orderRepository;
        this.orderProductRepository = orderProductRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
//        this.openRouteService = openRouteService;
    }

    public List<OrderResponseDto> findAllOrders(OrderFilterDto filterDto) {
        Specification<Order> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filterDto == null) {
                return criteriaBuilder.conjunction();
            }

            Join<Order, User> userJoin = root.join("user");

            if (filterDto.getUserId() != null) {
                predicates.add(criteriaBuilder.equal(userJoin.get("id"), filterDto.getUserId()));
            }

            if (filterDto.getUsername() != null && !filterDto.getUsername().isBlank()) {
                predicates.add(criteriaBuilder.equal(userJoin.get("username"), filterDto.getUsername()));
            }

            if (filterDto.getCreatedAfter() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), filterDto.getCreatedAfter()));
            }

            if (filterDto.getCreatedBefore() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), filterDto.getCreatedBefore()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return orderRepository.findAll(spec)
                .stream()
                .map(order -> {
                    List<OrderProduct> orderProducts = orderProductRepository.findByOrderId(order.getId());

                    return new OrderResponseDto(
                            order.getId(),
                            order.getStatus().toString(),
                            order.getUser().getId(),
                            order.getAddress(),
                            order.getCreatedAt(),
                            order.getUpdatedAt(),
                            findItemsByOrderId(order.getId())
                    );
                })
                .toList();
    }

    @Transactional
    public OrderResponseDto findOrderById(long id) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException("Id: " + id));
        List<OrderProduct> orderProducts = orderProductRepository.findByOrderId(id);

        return new OrderResponseDto(
                order.getId(),
                order.getStatus().toString(),
                order.getUser().getId(),
                order.getAddress(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                findItemsByOrderId(id)
        );
    }

    @Transactional
    public OrderResponseDto addItemToOrder(long orderId, OrderProductRequestDto productRequestDto) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException("Id: " + orderId));

        Product product = productRepository.findById(productRequestDto.getProductId()).orElseThrow(() -> new ProductNotFoundException("Id: " + orderId));

        Optional<OrderProduct> optionalOrderProduct = orderProductRepository.findByOrder_IdAndProduct_Id(orderId, productRequestDto.getProductId());

        if (optionalOrderProduct.isEmpty()) {
            throw new OrderProductAlreadyExistsException(orderId, productRequestDto.getProductId());
        }

        OrderProduct orderProduct = new OrderProduct();
        orderProduct.setOrder(order);
        orderProduct.setProduct(product);
        orderProduct.setQuantity(productRequestDto.getQuantity());
        orderProduct.setPriceAtOrderTime(product.getPrice());

        order.getOrderProducts().add(orderProduct);

        orderProductRepository.saveAndFlush(orderProduct);
        orderRepository.save(order);

        return new OrderResponseDto(
                orderId,
                order.getStatus().toString(),
                order.getUser().getId(),
                order.getAddress(),
                order.getCreatedAt(),
                LocalDateTime.now(),
                findItemsByOrderId(orderId));
    }

    public List<OrderProductResponseDto> findItemsByOrderId(long orderId) {
        orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with ID: " + orderId));

        List<OrderProduct> orderProducts = orderProductRepository.findByOrderId(orderId);

        return orderProducts.stream()
                .map(this::mapOrderProductToOrderProductResponseDTO)
                .toList();
    }

    private OrderProductResponseDto mapOrderProductToOrderProductResponseDTO(OrderProduct orderProduct) {
        return new OrderProductResponseDto(
                orderProduct.getId(),
                orderProduct.getProduct().getId(),
                orderProduct.getQuantity(),
                orderProduct.getPriceAtOrderTime()
        );
    }

    public void deleteOrder(long id) {
        orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException("Id: " + id));

        orderRepository.deleteById(id);
    }

    public OrderResponseDto updateOrder(long id, OrderUpdateDto updateDto) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException("Id: " + id));

        order.setAddress(updateDto.getAddress());
        order.setStatus(OrderStatus.valueOf(updateDto.getStatus()));

        orderRepository.saveAndFlush(order);

        return new OrderResponseDto(
                id,
                updateDto.getStatus(),
                order.getUser().getId(),
                updateDto.getAddress(),
                order.getCreatedAt(),
                LocalDateTime.now(),
                findItemsByOrderId(id)
        );

    }

    @Transactional
    public OrderResponseDto removeItemFromOrder(long orderId, long productId) {
        OrderProduct orderProduct = orderProductRepository.findByOrder_IdAndProduct_Id(orderId, productId)
                .orElseThrow(() -> new OrderProductNotFoundException(orderId, productId));

        orderProductRepository.delete(orderProduct);

        Order order = orderRepository.findById(orderId).get();

        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

        return new OrderResponseDto(
                orderId,
                order.getStatus().toString(),
                order.getUser().getId(),
                order.getAddress(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                findItemsByOrderId(orderId)
        );
    }

    public OrderProductResponseDto updateProductCount(long orderId, long productId, int newCount) {
        if (newCount <= 0) {
            throw new InvalidCountException("The count of the product is invalid: " + newCount);
        }

        OrderProduct orderProduct = orderProductRepository.findByOrder_IdAndProduct_Id(orderId, productId)
                .orElseThrow(() -> new OrderProductNotFoundException(orderId, productId));

        orderProduct.setQuantity(newCount);

        orderProductRepository.save(orderProduct);

        return new OrderProductResponseDto(
                orderId,
                productId,
                newCount,
                orderProduct.getPriceAtOrderTime()
        );
    }

    /**
     * Removes a specific product line item from an order.
     * This method identifies the line item by the combination of orderId and productId.
     *
     * @param orderId The ID of the order from which to remove the product.
     * @param productId The ID of the product to remove.
     * @return 204 No Content (handled by controller), service returns void or success status.
     * @throws OrderNotFoundException if the specified order does not exist.
     * @throws OrderProductNotFoundException if the product is not found as a line item in the specified order.
     */
    /**
     * Removes a product from a specific order by deleting the OrderProduct association.
     *
     * @param orderId   The ID of the order from which to remove the product.
     * @param productId The ID of the product to remove.
     * @throws OrderProductNotFoundException if the order does not exist, or if the product
     *                                       is not found within that specific order.
     */
    @Transactional
    public void removeProductFromOrder(long orderId, long productId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Id: " + orderId));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Id: " + productId));

        OrderProduct orderProductToRemove = orderProductRepository
                .findByOrder_IdAndProduct_Id(orderId, productId)
                .orElseThrow(() -> new OrderProductNotFoundException(
                        orderId, productId));

        order.setOrderProducts(order.getOrderProducts()
                .stream()
                .filter(op -> op.getProduct().getId() != productId)
                .collect(Collectors.toSet())
        );

        orderProductRepository.delete(orderProductToRemove);
    }

    public void removeOrderProduct(Order order, OrderProduct orderProduct) {
        if (order.getOrderProducts().stream().anyMatch(op -> Objects.equals(op.getId(), orderProduct.getId()))) {
            order.getOrderProducts().remove(orderProduct);
            orderProductRepository.delete(orderProduct);
        }
    }

    public OrderResponseDto addProductToOrder(long orderId, OrderProductRequestDto productDto) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException("Id: " + orderId));
        Product product = productRepository.findById(productDto.getProductId()).orElseThrow(() -> new ProductNotFoundException("Id: " + productDto));

        Optional<OrderProduct> orderProductOptional = orderProductRepository.findByOrder_IdAndProduct_Id(orderId, product.getId());

        OrderProduct orderProduct;

        if (orderProductOptional.isEmpty()) {
            orderProduct = new OrderProduct();

            orderProduct.setOrder(order);
            orderProduct.setProduct(product);
            orderProduct.setPriceAtOrderTime(product.getPrice());

            orderProduct.setQuantity(productDto.getQuantity());
            order.getOrderProducts().add(orderProduct);

        } else {
            orderProduct = orderProductOptional.get();
            orderProduct.setQuantity(productDto.getQuantity() + orderProduct.getQuantity());
        }

        orderProductRepository.saveAndFlush(orderProduct);

        return new OrderResponseDto(
                order.getId(),
                order.getStatus().toString(),
                order.getUser().getId(),
                order.getAddress(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                findItemsByOrderId(orderId)
        );
    }

    public OrderResponseDto createNewOrder(OrderRequestDto orderRequestDto) {
        User user = userRepository.findById(orderRequestDto.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + orderRequestDto.getUserId()));

        Order newOrder = new Order();
        newOrder.setUser(user);
        newOrder.setAddress(orderRequestDto.getAddress());
        newOrder.setStatus(OrderStatus.NEW);
        newOrder.setCreatedAt(LocalDateTime.now());
        newOrder.setUpdatedAt(LocalDateTime.now());

        for (OrderProductRequestDto productRequest : orderRequestDto.getItems()) {
            Product product = productRepository.findById(productRequest.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + productRequest.getProductId()));

            if (productRequest.getQuantity() <= 0) {
                throw new InvalidCountException("Product quantity must be positive for product ID: " + product.getId());
            }

            OrderProduct orderProduct = new OrderProduct();
            orderProduct.setProduct(product);
            orderProduct.setQuantity(productRequest.getQuantity());
            orderProduct.setPriceAtOrderTime(product.getPrice());

            orderProduct.setOrder(newOrder);

            newOrder.getOrderProducts().add(orderProduct);
        }

        Order savedOrder = orderRepository.save(newOrder);

        return mapOrderToOrderResponseDto(savedOrder);
    }


    // Helper method to map the final entity to a DTO
    private OrderResponseDto mapOrderToOrderResponseDto(Order order) {
        List<OrderProductResponseDto> responseProducts = order.getOrderProducts().stream()
                .map(item -> {
                    OrderProductResponseDto orderProductResponseDto = new OrderProductResponseDto();
                    orderProductResponseDto.setProductId(item.getProduct().getId());
                    orderProductResponseDto.setQuantity(item.getQuantity());
                    orderProductResponseDto.setPriceAtOrderTime(item.getPriceAtOrderTime());
                    return orderProductResponseDto;
                })
                .toList();

        OrderResponseDto responseDto = new OrderResponseDto();
        responseDto.setId(order.getId());
        responseDto.setUserId(order.getUser().getId());
        responseDto.setAddress(order.getAddress());
        responseDto.setStatus(order.getStatus().toString());
        responseDto.setCreatedAt(order.getCreatedAt());
        responseDto.setUpdatedAt(order.getUpdatedAt());
        responseDto.setItems(responseProducts);
        // You can add total price calculation here if needed

        return responseDto;
    }

    /**
     * Processes an order, changing its status to PROCESSING and calculating delivery distance.
     *
     * @param orderId    The ID of the order to process.
     * @param requestDto The ID of the employee processing the order wrapped up in a Dto.
     * @return The updated OrderResponseDTO.
     * @throws OrderNotFoundException If the order does not exist.
     * @throws UserNotFoundException  If the employee user does not exist.
     * @throws IllegalStateException  If the order cannot be processed (e.g., wrong status).
     */
    @org.springframework.transaction.annotation.Transactional
    public ProcessOrderResponseDto processOrder(long orderId, ProcessOrderRequestDto requestDto) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with ID: " + orderId));

        if (order.getStatus() != OrderStatus.NEW) {
            throw new OrderNotProcessedException("Order with ID " + orderId + " cannot be processed. Current status: " + order.getStatus());
        }

        long employeeId = requestDto.getEmployeeId();

        User employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new UserNotFoundException("Employee not found with ID: " + employeeId));

        String employeeAddress = employee.getAddress();
        String orderAddress = order.getAddress();

        double calculatedDistance = 0.0;

        try {
//            Mono<List<Double>> employeeCoordsMono = openRouteService.getCoordinates(employeeAddress);
//            Mono<List<Double>> orderCoordsMono = openRouteService.getCoordinates(orderAddress);

//            List<Double> employeeCoordinates = employeeCoordsMono.block();
//            List<Double> orderCoordinates = orderCoordsMono.block();

//            if (employeeCoordinates != null && employeeCoordinates.size() == 2 &&
//                    orderCoordinates != null && orderCoordinates.size() == 2) {
////                calculatedDistance = openRouteService.getDistance(employeeCoordinates, orderCoordinates).block();
////                logger.info(String.format("Calculated distance for order %d: %.2f meters", orderId, calculatedDistance));
//            } else {
////                logger.warning(String.format("Could not get coordinates for order %d or employee %d. Distance not calculated.", orderId, employeeId));
//            }

        } catch (Exception e) {
            throw new FailedCalculationException("An error occurred while calculating the distance", e);
//            logger.severe("Failed to calculate distance for order " + orderId + ": " + e.getMessage());
        }

        order.setStatus(OrderStatus.PROCESSING);
        order.setUpdatedAt(LocalDateTime.now());
        Order savedOrder = orderRepository.save(order);

        List<OrderProductResponseDto> orderItems = findItemsByOrderId(orderId);

        OrderResponseDto responseDto = new OrderResponseDto(
                savedOrder.getId(),
                savedOrder.getStatus().toString(),
                savedOrder.getUser().getId(),
                savedOrder.getAddress(),
                savedOrder.getCreatedAt(),
                savedOrder.getUpdatedAt(),
                orderItems
        );

        return new ProcessOrderResponseDto(
                responseDto,
                calculatedDistance + " km",
                (calculatedDistance / AVERAGE_CAR_SPEED) + " minutes"
        );

    }

}
