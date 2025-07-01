package com.deliciouspizza.controller;

import com.deliciouspizza.dto.geocode.DirectionsResponseDto;
import com.deliciouspizza.dto.geocode.GeocodeSearchResponseDto;
import com.deliciouspizza.dto.order.OrderRequestDto;
import com.deliciouspizza.dto.order.OrderUpdateDto;
import com.deliciouspizza.dto.order.ProcessOrderRequestDto;
import com.deliciouspizza.dto.order_product.OrderProductRequestDto;
import com.deliciouspizza.model.order.Order;
import com.deliciouspizza.model.order.OrderStatus;
import com.deliciouspizza.model.orders_products.OrderProduct;
import com.deliciouspizza.model.product.Drink;
import com.deliciouspizza.model.product.Pizza;
import com.deliciouspizza.model.product.Product;
import com.deliciouspizza.model.product.ProductSize;
import com.deliciouspizza.model.product.ProductStatus;
import com.deliciouspizza.model.user.User;
import com.deliciouspizza.model.user.UserRole;
import com.deliciouspizza.repository.OrderProductRepository;
import com.deliciouspizza.repository.OrderRepository;
import com.deliciouspizza.repository.ProductRepository;
import com.deliciouspizza.repository.UserRepository;
import com.deliciouspizza.service.OpenRouteService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // Rolls back DB changes after each test
public class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderProductRepository orderProductRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @TestConfiguration
    static class TestOpenRouteServiceConfig {
        @Bean // 3. Define the mock as a Spring Bean
        public OpenRouteService openRouteService() {
            return Mockito.mock(OpenRouteService.class); // Return a Mockito mock instance
        }
    }

    @Autowired
    private OpenRouteService openRouteService;

    // --- BeforeEach: Clean DB and setup common test data ---
    @BeforeEach
    void setUp() {
        orderProductRepository.deleteAllInBatch();
        orderRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
        productRepository.deleteAllInBatch();
    }

    // --- Helper methods to create entities in DB ---

    private User createUserInDb(String username, String email, String password, UserRole role, String address) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(role);
        user.setActive(true);
        user.setAddress(address);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    private Product createPizzaInDb(String name, String description, BigDecimal price, ProductSize size) {
        Pizza pizza = new Pizza(ProductStatus.ACTIVE, name, description, size, price, true, BigDecimal.ZERO);
        return productRepository.save(pizza);
    }

    private Product createDrinkInDb(String name, String description, BigDecimal price, ProductSize size, Boolean isAlcoholic) {
        Drink drink = new Drink(ProductStatus.ACTIVE, name, description, size, price, true, BigDecimal.ZERO, isAlcoholic);
        return productRepository.save(drink);
    }

    private Order createOrderInDb(User user, String address, OrderStatus status) {
        Order order = new Order();
        order.setUser(user);
        order.setAddress(address);
        order.setStatus(status);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        return orderRepository.save(order);
    }

    private OrderProduct createOrderProductInDb(Order order, Product product, int quantity, BigDecimal priceAtOrderTime) {
        OrderProduct op = new OrderProduct();
        op.setOrder(order);
        op.setProduct(product);
        op.setQuantity(quantity);
        op.setPriceAtOrderTime(priceAtOrderTime);
        order.getOrderProducts().add(op); // Link bidirectional
        return orderProductRepository.save(op);
    }

    // --- Helper methods to create Dtos ---

    private OrderRequestDto createOrderRequestDto(Long userId, String address, List<OrderProductRequestDto> products) {
        OrderRequestDto dto = new OrderRequestDto();
        dto.setUserId(userId);
        dto.setAddress(address);
        dto.setItems(products);
        return dto;
    }

    private OrderProductRequestDto createOrderProductRequestDto(Long productId, Integer quantity) {
        OrderProductRequestDto dto = new OrderProductRequestDto();
        dto.setProductId(productId);
        dto.setQuantity(quantity);
        return dto;
    }

    @Test
    void getAllOrders_shouldReturnAllOrders_whenNoFilter() throws Exception {
        User user1 = createUserInDb("user1", "user1@example.com", "pass12345", UserRole.CUSTOMER, "Address 1");
        User user2 = createUserInDb("user2", "user2@example.com", "pass12345", UserRole.CUSTOMER, "Address 2");
        createOrderInDb(user1, "Order Address 1", OrderStatus.PROCESSING);
        createOrderInDb(user2, "Order Address 2", OrderStatus.NEW);

        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[1].id").exists());
    }

    @Test
    void getAllOrders_shouldReturnOrdersByUserId_whenFilterProvided() throws Exception {
        User user1 = createUserInDb("user1", "user1@example.com", "pass12345", UserRole.CUSTOMER, "Address 1");
        User user2 = createUserInDb("user2", "user2@example.com", "pass12345", UserRole.CUSTOMER, "Address 2");
        createOrderInDb(user1, "Order Address 1", OrderStatus.NEW);
        createOrderInDb(user1, "Order Address 1.2", OrderStatus.PROCESSING);
        createOrderInDb(user2, "Order Address 2", OrderStatus.NEW);

        mockMvc.perform(get("/api/v1/orders")
                        .queryParam("userId", String.valueOf(user1.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].userId").value(user1.getId()))
                .andExpect(jsonPath("$[1].userId").value(user1.getId()));
    }

    @Test
    void getAllOrders_shouldReturnEmptyList_whenUserIdNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/orders")
                        .queryParam("userId", "999"))
                .andExpect(status().isOk()) // Should return 200 OK with empty list
                .andExpect(jsonPath("$.length()").value(0));
    }


    // --- Tests for GET /api/v1/orders/{id} (getOrder) ---

    @Test
    void getOrder_shouldReturnOrder_whenIdExists() throws Exception {
        User user = createUserInDb("testuser", "test@example.com", "pass12345", UserRole.CUSTOMER, "User Address");
        Order order = createOrderInDb(user, "Order Address", OrderStatus.NEW);

        mockMvc.perform(get("/api/v1/orders/{id}", order.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(order.getId()))
                .andExpect(jsonPath("$.status").value("NEW"))
                .andExpect(jsonPath("$.address").value("Order Address"));
    }

    @Test
    void getOrder_shouldReturnNotFound_whenIdDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/v1/orders/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    // --- Tests for POST /api/v1/orders (createNewOrder) ---

    @Test
    void createNewOrder_shouldCreateOrder_whenValidInput() throws Exception {
        User user = createUserInDb("customer", "customer@example.com", "pass123", UserRole.CUSTOMER, "Customer Address");
        Product pizza = createPizzaInDb("Pepperoni", "Spicy pizza", new BigDecimal("15.00"), ProductSize.LARGE);
        Product drink = createDrinkInDb("Cola", "Sweet cola", new BigDecimal("3.00"), ProductSize._330ML, false);

        List<OrderProductRequestDto> products = new ArrayList<>();
        products.add(createOrderProductRequestDto(pizza.getId(), 1));
        products.add(createOrderProductRequestDto(drink.getId(), 2));

        OrderRequestDto requestDto = createOrderRequestDto(user.getId(), "Delivery Address", products);

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.userId").value(user.getId()))
                .andExpect(jsonPath("$.address").value("Delivery Address"))
                .andExpect(jsonPath("$.status").value("NEW"))
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.items[0].productId").exists());

        Order createdOrder = orderRepository.findByUser_Username("customer").getFirst();
        assertEquals(createdOrder.getStatus(), OrderStatus.NEW);
        assertEquals(2, createdOrder.getOrderProducts().size());
    }

    @Test
    void createNewOrder_shouldReturnBadRequest_whenInvalidInput() throws Exception {
        OrderRequestDto requestDto = new OrderRequestDto(); // Missing required fields
        // Missing userId, address, products list
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createNewOrder_shouldReturnNotFound_whenUserDoesNotExist() throws Exception {
        Product pizza = createPizzaInDb("TestPizza", "Test desc", new BigDecimal("10.00"), ProductSize.MEDIUM);
        List<OrderProductRequestDto> products = Collections.singletonList(createOrderProductRequestDto(pizza.getId(), 1));

        OrderRequestDto requestDto = createOrderRequestDto(999L, "Delivery Address", products); // Non-existent user

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound()); // Assuming UserNotFoundException handler returns 404
    }

    @Test
    void updateOrder_shouldUpdateOrder_whenValidInput() throws Exception {
        User user = createUserInDb("user", "user@example.com", "pass", UserRole.CUSTOMER, "Address");
        Order order = createOrderInDb(user, "Old Address", OrderStatus.NEW);

        OrderUpdateDto updateDto = new OrderUpdateDto();
        updateDto.setAddress("New Delivery Address");
        updateDto.setStatus("PROCESSING");

        mockMvc.perform(put("/api/v1/orders/{id}", order.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(order.getId()))
                .andExpect(jsonPath("$.address").value("New Delivery Address"))
                .andExpect(jsonPath("$.status").value("PROCESSING"));

        Optional<Order> updatedOrder = orderRepository.findById(order.getId());
        assertTrue(updatedOrder.isPresent());
        assertEquals("New Delivery Address", updatedOrder.get().getAddress());
        assertEquals(OrderStatus.PROCESSING, updatedOrder.get().getStatus());
    }

    @Test
    void updateOrder_shouldReturnNotFound_whenIdDoesNotExist() throws Exception {
        OrderUpdateDto updateDto = new OrderUpdateDto();
        updateDto.setAddress("Some Address");

        mockMvc.perform(put("/api/v1/orders/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateOrder_shouldReturnBadRequest_whenInvalidStatus() throws Exception {
        User user = createUserInDb("user", "user@example.com", "pass", UserRole.CUSTOMER, "Address");
        Order order = createOrderInDb(user, "Old Address", OrderStatus.NEW);

        OrderUpdateDto updateDto = new OrderUpdateDto();
        updateDto.setStatus("INVALID_STATUS"); // Invalid enum value

        mockMvc.perform(put("/api/v1/orders/{id}", order.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isBadRequest());
    }

    // --- Tests for DELETE /api/v1/orders/{id} (deleteOrder) ---

    @Test
    void deleteOrder_shouldDeleteOrder_whenIdExists() throws Exception {
        User user = createUserInDb("user", "user@example.com", "pass", UserRole.CUSTOMER, "Address");
        Order order = createOrderInDb(user, "Order Address", OrderStatus.NEW);

        mockMvc.perform(delete("/api/v1/orders/{id}", order.getId()))
                .andExpect(status().isNoContent());

        assertFalse(orderRepository.findById(order.getId()).isPresent());
    }

    @Test
    void deleteOrder_shouldReturnNotFound_whenIdDoesNotExist() throws Exception {
        mockMvc.perform(delete("/api/v1/orders/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    // --- Tests for GET /api/v1/orders/{id}/products (getProductsInOrder) ---

    @Test
    void getProductsInOrder_shouldReturnItems_whenOrderHasProducts() throws Exception {
        User user = createUserInDb("user", "user@example.com", "pass", UserRole.CUSTOMER, "Address");
        Order order = createOrderInDb(user, "Order Address", OrderStatus.NEW);
        Product pizza = createPizzaInDb("Pizza", "Desc", new BigDecimal("10.00"), ProductSize.MEDIUM);
        Product drink = createDrinkInDb("Drink", "Desc", new BigDecimal("2.00"), ProductSize._330ML, false);
        createOrderProductInDb(order, pizza, 1, pizza.getPrice());
        createOrderProductInDb(order, drink, 2, drink.getPrice());

        mockMvc.perform(get("/api/v1/orders/{id}/products", order.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].productId").exists())
                .andExpect(jsonPath("$[1].productId").exists());
    }

    @Test
    void getProductsInOrder_shouldReturnEmptyList_whenOrderHasNoProducts() throws Exception {
        User user = createUserInDb("user", "user@example.com", "pass", UserRole.CUSTOMER, "Address");
        Order order = createOrderInDb(user, "Order Address", OrderStatus.NEW); // No products added

        mockMvc.perform(get("/api/v1/orders/{id}/products", order.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getProductsInOrder_shouldReturnNotFound_whenOrderIdDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/v1/orders/{id}/products", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void addProductToOrder_shouldAddNewProduct_whenOrderAndProductExist() throws Exception {
        User user = createUserInDb("user", "user@example.com", "pass", UserRole.CUSTOMER, "Address");
        Order order = createOrderInDb(user, "Order Address", OrderStatus.NEW);
        Product pizza = createPizzaInDb("New Pizza", "Desc", new BigDecimal("12.00"), ProductSize.MEDIUM);

        OrderProductRequestDto requestDto = createOrderProductRequestDto(pizza.getId(), 1);

        mockMvc.perform(post("/api/v1/orders/{id}/products", order.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(order.getId()))
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].productId").value(pizza.getId()));

        Order updatedOrderInDb = orderRepository.findById(order.getId()).get();
        assertEquals(1, updatedOrderInDb.getOrderProducts().size());
    }

    @Test
    void addProductToOrder_shouldUpdateQuantity_whenProductAlreadyInOrder() throws Exception {
        User user = createUserInDb("user", "user@example.com", "pass", UserRole.CUSTOMER, "Address");
        Order order = createOrderInDb(user, "Order Address", OrderStatus.NEW);
        Product pizza = createPizzaInDb("Pizza", "Desc", new BigDecimal("10.00"), ProductSize.MEDIUM);
        createOrderProductInDb(order, pizza, 1, pizza.getPrice()); // Already 1 item

        OrderProductRequestDto requestDto = createOrderProductRequestDto(pizza.getId(), 2); // Add 2 more

        mockMvc.perform(post("/api/v1/orders/{id}/products", order.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(order.getId()))
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].quantity").value(3)); // Quantity should be 1 + 2 = 3

        Order updatedOrderInDb = orderRepository.findById(order.getId()).get();
        assertEquals(1, updatedOrderInDb.getOrderProducts().size()); // Still 1 line item
        assertEquals(3, updatedOrderInDb.getOrderProducts().iterator().next().getQuantity());
    }

    @Test
    void addProductToOrder_shouldReturnNotFound_whenOrderDoesNotExist() throws Exception {
        Product pizza = createPizzaInDb("TestPizza", "Desc", new BigDecimal("10.00"), ProductSize.MEDIUM);
        OrderProductRequestDto requestDto = createOrderProductRequestDto(pizza.getId(), 1);

        mockMvc.perform(post("/api/v1/orders/{id}/products", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void addProductToOrder_shouldReturnNotFound_whenProductDoesNotExist() throws Exception {
        User user = createUserInDb("user", "user@example.com", "pass", UserRole.CUSTOMER, "Address");
        Order order = createOrderInDb(user, "Order Address", OrderStatus.NEW);
        OrderProductRequestDto requestDto = createOrderProductRequestDto(999L, 1); // Non-existent product

        mockMvc.perform(post("/api/v1/orders/{id}/products", order.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound());
    }

    // --- Tests for DELETE /api/v1/orders/{orderId}/products/{productId} (removeProductFromOrder) ---

    /**
     * By adding @DirtiesContext, we tell Spring to reset the application context
     * after this test, ensuring a clean database state for any subsequent tests.
     * This is good practice for tests that modify data.
     */
    @Test
    @DirtiesContext
    void removeProductFromOrder_shouldRemoveProduct_whenExists() throws Exception {
        // ARRANGE: Set up the initial state
        User user = createUserInDb("user", "user@example.com", "pass", UserRole.CUSTOMER, "Address");
        Order order = createOrderInDb(user, "Order Address", OrderStatus.NEW);
        Product pizza = createPizzaInDb("Pizza", "Desc", new BigDecimal("10.00"), ProductSize.MEDIUM);
        Product drink = createDrinkInDb("Drink", "Desc", new BigDecimal("2.00"), ProductSize._330ML, false);
        createOrderProductInDb(order, pizza, 1, pizza.getPrice());
        createOrderProductInDb(order, drink, 2, drink.getPrice());

        // ACTION: Perform the DELETE request to remove the 'drink' from the order
        mockMvc.perform(delete("/api/v1/orders/{orderId}/products/{productId}", order.getId(), drink.getId()))
                .andExpect(status().isNoContent());

        // ASSERT: Verify the final state from the database
        Order updatedOrder = orderRepository.findById(order.getId())
                .orElseThrow(() -> new AssertionError("Order should not be null"));

        // 1. Assert that the order now contains exactly one item.
        assertThat(updatedOrder.getOrderProducts()).hasSize(1);

        // 2. Assert that the remaining item is the 'pizza'.
        Product remainingProduct = updatedOrder.getOrderProducts().iterator().next().getProduct();
        assertThat(remainingProduct.getId()).isEqualTo(pizza.getId());
        assertThat(remainingProduct.getName()).isEqualTo("Pizza");
    }

    @Test
    void removeProductFromOrder_shouldReturnNotFound_whenItemNotInOrder() throws Exception {
        User user = createUserInDb("user", "user@example.com", "pass", UserRole.CUSTOMER, "Address");
        Order order = createOrderInDb(user, "Order Address", OrderStatus.NEW);
        Product pizza = createPizzaInDb("Pizza", "Desc", new BigDecimal("10.00"), ProductSize.MEDIUM);
        // Pizza is not in this order

        mockMvc.perform(delete("/api/v1/orders/{orderId}/products/{productId}", order.getId(), pizza.getId()))
                .andExpect(status().isNotFound()); // Assuming OrderItemNotFoundException maps to 404
    }

    @Test
    void removeProductFromOrder_shouldReturnNotFound_whenOrderDoesNotExist() throws Exception {
        Product pizza = createPizzaInDb("Pizza", "Desc", new BigDecimal("10.00"), ProductSize.MEDIUM);
        mockMvc.perform(delete("/api/v1/orders/{orderId}/products/{productId}", 999L, pizza.getId()))
                .andExpect(status().isNotFound());
    }

    // --- Tests for PATCH /api/v1/orders/{orderId}/products/{productId} (updateCount) ---

    @Test
    void updateCount_shouldUpdateQuantity_whenValidInput() throws Exception {
        User user = createUserInDb("user", "user@example.com", "pass", UserRole.CUSTOMER, "Address");
        Order order = createOrderInDb(user, "Order Address", OrderStatus.NEW);
        Product pizza = createPizzaInDb("Pizza", "Desc", new BigDecimal("10.00"), ProductSize.MEDIUM);
        createOrderProductInDb(order, pizza, 1, pizza.getPrice());

        int newCount = 5;
        mockMvc.perform(patch("/api/v1/orders/{orderId}/products/{productId}", order.getId(), pizza.getId())
                        .queryParam("newCount", String.valueOf(newCount)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(pizza.getId()))
                .andExpect(jsonPath("$.quantity").value(newCount));

        OrderProduct updatedOp = orderProductRepository.findByOrder_IdAndProduct_Id(order.getId(), pizza.getId()).get();
        assertEquals(newCount, updatedOp.getQuantity());
    }

    @Test
    void updateCount_shouldReturnBadRequest_whenNewCountIsZero() throws Exception {
        User user = createUserInDb("user", "user@example.com", "pass", UserRole.CUSTOMER, "Address");
        Order order = createOrderInDb(user, "Order Address", OrderStatus.NEW);
        Product pizza = createPizzaInDb("Pizza", "Desc", new BigDecimal("10.00"), ProductSize.MEDIUM);
        createOrderProductInDb(order, pizza, 1, pizza.getPrice());

        mockMvc.perform(patch("/api/v1/orders/{orderId}/products/{productId}", order.getId(), pizza.getId())
                        .queryParam("newCount", "0"))
                .andExpect(status().isBadRequest()); // Assuming IllegalArgumentException is handled by @ControllerAdvice for 400
    }

    @Test
    void updateCount_shouldReturnNotFound_whenItemNotInOrder() throws Exception {
        User user = createUserInDb("user", "user@example.com", "pass", UserRole.CUSTOMER, "Address");
        Order order = createOrderInDb(user, "Order Address", OrderStatus.NEW);
        Product pizza = createPizzaInDb("Pizza", "Desc", new BigDecimal("10.00"), ProductSize.MEDIUM);

        mockMvc.perform(patch("/api/v1/orders/{orderId}/products/{productId}", order.getId(), pizza.getId())
                        .queryParam("newCount", "2"))
                .andExpect(status().isNotFound());
    }

    @Test
    void processOrder_shouldProcessOrder_whenOrderIsValid() throws Exception {
        // ARRANGE: Create entities and the request object
        User customer = createUserInDb("customer", "customer@example.com", "pass", UserRole.CUSTOMER, "Customer Address");
        User employee = createUserInDb("employee", "employee@example.com", "pass", UserRole.EMPLOYEE, "Employee Address");
        Product pizza = createPizzaInDb("Pizza", "Desc", new BigDecimal("10.00"), ProductSize.MEDIUM);

        Order order = createOrderInDb(customer, "Order Address", OrderStatus.NEW);
        createOrderProductInDb(order, pizza, 1, pizza.getPrice());

        ProcessOrderRequestDto requestDto = new ProcessOrderRequestDto();
        requestDto.setEmployeeId(employee.getId());

        when(openRouteService.getDistance(anyList(), anyList())).thenReturn(Mono.just(15000.0)); // 15 km

        mockMvc.perform(patch("/api/v1/orders/{orderId}", order.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(order.getId()))
                .andExpect(jsonPath("$.status").value("PROCESSING")); // Status should be updated

        Order processedOrderInDb = orderRepository.findById(order.getId()).get();

        assertThat(processedOrderInDb.getStatus()).isEqualTo(OrderStatus.PROCESSING);
    }

    @Test
    void processOrder_shouldReturnNotFound_whenOrderDoesNotExist() throws Exception {
        mockMvc.perform(patch("/api/v1/orders/{orderId}/process", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void processOrder_shouldReturnBadRequest_whenOrderAlreadyInTerminalState() throws Exception {
        User customer = createUserInDb("customer", "customer@example.com", "pass", UserRole.CUSTOMER, "Customer Address");
        User employee = createUserInDb("employee", "employee@example.com", "pass", UserRole.EMPLOYEE, "Work Address");
        Product pizza = createPizzaInDb("Pizza", "Desc", new BigDecimal("10.00"), ProductSize.MEDIUM);

        Order order = createOrderInDb(customer, "Order Address", OrderStatus.COMPLETED);
        createOrderProductInDb(order, pizza, 1, pizza.getPrice());

        ProcessOrderRequestDto requestDto = new ProcessOrderRequestDto();
        requestDto.setEmployeeId(employee.getId());

        mockMvc.perform(patch("/api/v1/orders/{orderId}", order.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }
} 