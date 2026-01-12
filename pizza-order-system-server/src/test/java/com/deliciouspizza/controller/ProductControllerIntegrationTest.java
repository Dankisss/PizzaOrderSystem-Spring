package com.deliciouspizza.controller;

import com.deliciouspizza.dto.product.ProductInputDto;
import com.deliciouspizza.dto.product.ProductUpdateDto;
import com.deliciouspizza.model.product.Drink;
import com.deliciouspizza.model.product.Pizza;
import com.deliciouspizza.model.product.Product;
import com.deliciouspizza.model.product.ProductCategory;
import com.deliciouspizza.model.product.ProductSize;
import com.deliciouspizza.model.product.ProductStatus;
import com.deliciouspizza.repository.ProductRepository;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @TestConfiguration
    static class TestOpenRouteServiceConfig {
        @Bean
        public OpenRouteService openRouteService() {
            return Mockito.mock(OpenRouteService.class); // Return a Mockito mock instance
        }
    }

    @Autowired
    private OpenRouteService openRouteService;

    /**
     * Cleans up the database before each test to ensure test isolation.
     * Deletes all products.
     */
    @BeforeEach
    void setUp() {
        productRepository.deleteAllInBatch();
    }

    private Product createPizzaInDb(String name, String description, BigDecimal price, ProductSize size) {
        Pizza pizza = new Pizza(
                ProductStatus.ACTIVE,
                name,
                description,
                size,
                price,
                true,
                BigDecimal.ZERO
        );
        return productRepository.save(pizza);
    }

    private Product createDrinkInDb(
            String name,
            String description,
            BigDecimal price,
            ProductSize size,
            boolean alcoholic) {
        Drink drink = new Drink(
                ProductStatus.ACTIVE,
                name,
                description,
                size,
                price,
                true,
                BigDecimal.ZERO,
                alcoholic
        );
        return productRepository.save(drink);
    }

    private ProductInputDto createPizzaInputDto(String name, String description, BigDecimal price, ProductSize size) {
        ProductInputDto dto = new ProductInputDto();
        dto.setName(name);
        dto.setDescription(description);
        dto.setCategory(ProductCategory.PIZZA);
        dto.setStatus(ProductStatus.ACTIVE);
        dto.setSize(size);
        dto.setPrice(price);
        dto.setActive(true);
        dto.setTotalAmount(BigDecimal.ZERO);
        return dto;
    }

    private ProductInputDto createDrinkInputDto(
            String name,
            String description,
            BigDecimal price,
            ProductSize size,
            Boolean isAlcoholic) {
        ProductInputDto dto = new ProductInputDto();
        dto.setName(name);
        dto.setDescription(description);
        dto.setCategory(ProductCategory.DRINK);
        dto.setStatus(ProductStatus.ACTIVE);
        dto.setSize(size);
        dto.setPrice(price);
        dto.setActive(true);
        dto.setTotalAmount(BigDecimal.ZERO);
        // Removed: dto.setDrinkType(drinkType);
        dto.setAlcoholic(isAlcoholic); // Retained for Drink
        return dto;
    }

    @Test
    void getAllProducts_shouldReturnAllProducts_whenNoFilters() throws Exception {
        createPizzaInDb("Margherita Pizza", "Classic Italian", new BigDecimal("15.00"), ProductSize.MEDIUM);
        createDrinkInDb("Coca-Cola", "Refreshing soda", new BigDecimal("3.00"), ProductSize._330ML, false);

        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").exists())
                .andExpect(jsonPath("$[1].name").exists());
    }

    @Test
    void getAllProducts_shouldReturnFilteredProducts_byCategory() throws Exception {
        createPizzaInDb("Pepperoni Pizza", "Spicy pepperoni", new BigDecimal("18.00"), ProductSize.LARGE);
        createDrinkInDb("Sprite Can", "Lemon-Lime soda", new BigDecimal("3.50"), ProductSize._500ML, false);

        mockMvc.perform(get("/api/v1/products")
                        .queryParam("category", ProductCategory.PIZZA.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Pepperoni Pizza"))
                .andExpect(jsonPath("$[0].category").value("PIZZA"));
    }

    @Test
    void getAllProducts_shouldReturnFilteredProducts_byPriceRange() throws Exception {
        createPizzaInDb("Veggie Pizza", "Healthy", new BigDecimal("16.50"), ProductSize.MEDIUM);
        createDrinkInDb("Still Water", "Pure water", new BigDecimal("2.00"), ProductSize._500ML, false);
        createPizzaInDb("Supreme Pizza", "Loaded", new BigDecimal("22.00"), ProductSize.LARGE);

        mockMvc.perform(get("/api/v1/products")
                        .queryParam("minPrice", "10.00")
                        .queryParam("maxPrice", "17.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Veggie Pizza"))
                .andExpect(jsonPath("$[0].price").value(16.50));
    }

    @Test
    void getAllProducts_shouldReturnFilteredProducts_byActiveStatus() throws Exception {
        Product activeProduct = createPizzaInDb("Active Pizza", "Active desc", new BigDecimal("10.00"), ProductSize.SMALL);
        Product inactiveProduct = createDrinkInDb("Inactive Drink", "Inactive desc", new BigDecimal("2.00"), ProductSize._330ML, false);
        inactiveProduct.setActive(false);
        productRepository.save(inactiveProduct); // Save to update active status

        mockMvc.perform(get("/api/v1/products")
                        .queryParam("active", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Active Pizza"))
                .andExpect(jsonPath("$[0].active").value(true));
    }

    @Test
    void getProduct_shouldReturnProduct_whenIdExists() throws Exception {
        Product pizza = createPizzaInDb("Test Pizza", "Just for testing", new BigDecimal("12.00"), ProductSize.SMALL);

        mockMvc.perform(get("/api/v1/products/{id}", pizza.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(pizza.getId()))
                .andExpect(jsonPath("$.name").value("Test Pizza"))
                .andExpect(jsonPath("$.description").value("Just for testing")) // Check description
                .andExpect(jsonPath("$.category").value("PIZZA"))
                .andExpect(jsonPath("$.hasImage").value(false));
    }

    @Test
    void getProduct_shouldReturnNotFound_whenIdDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/v1/products/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    // --- Tests for POST /api/v1/products (createProduct) ---

    @Test
    void createProduct_shouldCreatePizzaWithoutPhoto_whenValidInput() throws Exception {
        ProductInputDto inputDto = createPizzaInputDto("New Pizza", "A brand new pizza", new BigDecimal("20.00"), ProductSize.LARGE);

        MockMultipartFile requestPart = new MockMultipartFile(
                "request",
                "request.json",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(inputDto)
        );

        mockMvc.perform(multipart("/api/v1/products")
                        .file(requestPart)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("New Pizza"))
                .andExpect(jsonPath("$.description").value("A brand new pizza"))
                .andExpect(jsonPath("$.category").value("PIZZA"))
                .andExpect(jsonPath("$.hasImage").value(false));

        Optional<Product> createdProduct = productRepository.findByName("New Pizza");
        assertTrue(createdProduct.isPresent());
        assertInstanceOf(Pizza.class, createdProduct.get());
        assertNull(createdProduct.get().getImageData());
    }

    @Test
    void createProduct_shouldCreateDrinkWithPhoto_whenValidInput() throws Exception {
        ProductInputDto inputDto = createDrinkInputDto("Sparkling Water", "Refreshing", new BigDecimal("4.50"), ProductSize._500ML, false);
        byte[] photoContent = "drink_image_bytes".getBytes();
        MockMultipartFile photoPart = new MockMultipartFile(
                "photo",
                "drink.png",
                MediaType.IMAGE_PNG_VALUE,
                photoContent
        );
        MockMultipartFile requestPart = new MockMultipartFile(
                "request",
                "request.json",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(inputDto)
        );

        mockMvc.perform(multipart("/api/v1/products")
                        .file(requestPart)
                        .file(photoPart)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Sparkling Water"))
                .andExpect(jsonPath("$.description").value("Refreshing"))
                .andExpect(jsonPath("$.category").value("DRINK"))
                .andExpect(jsonPath("$.isAlcoholic").value(false)) // Check Drink-specific field
                .andExpect(jsonPath("$.hasImage").value(true));

        Optional<Product> createdProduct = productRepository.findByName("Sparkling Water");
        assertTrue(createdProduct.isPresent());
        assertInstanceOf(Drink.class, createdProduct.get());
        assertNotNull(createdProduct.get().getImageData());
        assertArrayEquals(photoContent, createdProduct.get().getImageData());
    }

    @Test
    void createProduct_shouldReturnBadRequest_whenInvalidInput() throws Exception {
        ProductInputDto inputDto = new ProductInputDto(); // Missing required fields like name, price, category
        inputDto.setCategory(ProductCategory.PIZZA);
        inputDto.setPrice(new BigDecimal("-1.00")); // Invalid price
        inputDto.setName(""); // Blank name

        MockMultipartFile requestPart = new MockMultipartFile(
                "request", "request.json", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(inputDto)
        );

        mockMvc.perform(multipart("/api/v1/products")
                        .file(requestPart)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateProduct_shouldPerformPartialUpdate_withoutPhoto() throws Exception {
        Product existingPizza = createPizzaInDb("Old Pizza Name", "Old description", new BigDecimal("10.00"), ProductSize.SMALL);

        ProductUpdateDto updateDto = new ProductUpdateDto();
        updateDto.setName("Updated Pizza Name");
        updateDto.setPrice(new BigDecimal("12.50"));
        updateDto.setStatus(ProductStatus.INACTIVE); // Update status
        updateDto.setDescription("Updated description for pizza");

        MockMultipartFile requestPart = new MockMultipartFile(
                "request",
                "update.json",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(updateDto)
        );

        mockMvc.perform(multipart("/api/v1/products/{id}", existingPizza.getId())
                        .file(requestPart)
                        .with(request -> {
                            request.setMethod("PATCH");
                            return request;
                        })
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(existingPizza.getId()))
                .andExpect(jsonPath("$.name").value("Updated Pizza Name"))
                .andExpect(jsonPath("$.price").value(12.50))
                .andExpect(jsonPath("$.status").value("INACTIVE"))
                .andExpect(jsonPath("$.description").value("Updated description for pizza"));

        Product updatedProductInDb = productRepository.findById(existingPizza.getId()).get();
        assertEquals("Updated Pizza Name", updatedProductInDb.getName());
        assertEquals(new BigDecimal("12.50"), updatedProductInDb.getPrice());
        assertEquals(ProductStatus.INACTIVE, updatedProductInDb.getStatus());
        assertEquals("Updated description for pizza", updatedProductInDb.getDescription());
    }

    @Test
    void updateProduct_shouldUpdatePhoto_keepingOtherFields() throws Exception {
        byte[] oldPhotoContent = "old_image_bytes".getBytes();
        Product existingDrink = createDrinkInDb("Old Soda", "Original fizz", new BigDecimal("2.50"), ProductSize._330ML, false);
        existingDrink.setImageData(oldPhotoContent);
        productRepository.save(existingDrink);

        byte[] newPhotoContent = "new_image_bytes".getBytes();
        MockMultipartFile photoPart = new MockMultipartFile(
                "photo",
                "new_soda.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                newPhotoContent
        );
        ProductUpdateDto updateDto = new ProductUpdateDto(); // Empty DTO, only updating photo
        MockMultipartFile requestPart = new MockMultipartFile(
                "request",
                "update.json",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(updateDto)
        );

        mockMvc.perform(multipart("/api/v1/products/{id}", existingDrink.getId())
                        .file(requestPart)
                        .file(photoPart)
                        .with(request -> {
                            request.setMethod("PATCH");
                            return request;
                        })
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(existingDrink.getId()))
                .andExpect(jsonPath("$.hasImage").value(true));

        Product updatedProductInDb = productRepository.findById(existingDrink.getId()).get();
        assertNotNull(updatedProductInDb.getImageData());
        assertArrayEquals(newPhotoContent, updatedProductInDb.getImageData());
        assertEquals("Old Soda", updatedProductInDb.getName());
        assertEquals("Original fizz", updatedProductInDb.getDescription());
    }

    @Test
    void updateProduct_shouldRemovePhoto_whenEmptyPhotoSent() throws Exception {
        byte[] oldPhotoContent = "old_image_to_remove".getBytes();
        Product existingPizza = createPizzaInDb("Removable Image Pizza", "Has an image", new BigDecimal("15.00"), ProductSize.MEDIUM);
        existingPizza.setImageData(oldPhotoContent);
        productRepository.save(existingPizza);

        MockMultipartFile emptyPhotoPart = new MockMultipartFile(
                "photo",
                "empty.png",
                MediaType.IMAGE_PNG_VALUE,
                new byte[0]
        );
        ProductUpdateDto updateDto = new ProductUpdateDto();
        MockMultipartFile requestPart = new MockMultipartFile(
                "request",
                "update.json",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(updateDto)
        );

        mockMvc.perform(multipart("/api/v1/products/{id}", existingPizza.getId())
                        .file(requestPart)
                        .file(emptyPhotoPart)
                        .with(request ->  {
                            request.setMethod("PATCH");
                            return request;
                        })
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(existingPizza.getId()))
                .andExpect(jsonPath("$.hasImage").value(false));

        Product updatedProductInDb = productRepository.findById(existingPizza.getId()).get();
        assertNull(updatedProductInDb.getImageData());
    }


    @Test
    void updateProduct_shouldReturnNotFound_whenIdDoesNotExist() throws Exception {
        ProductUpdateDto updateDto = new ProductUpdateDto();
        updateDto.setName("NonExistentProduct");

        MockMultipartFile requestPart = new MockMultipartFile(
                "request", "update.json", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(updateDto)
        );

        mockMvc.perform(multipart("/api/v1/products/{id}", 999L)
                        .file(requestPart)
                        .with(request ->  {
                            request.setMethod("PATCH");
                            return request;
                        })
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void updateProduct_shouldReturnBadRequest_whenCategoryMismatch() throws Exception {
        Product existingPizza = createPizzaInDb("Pizza To Change", "Original Desc", new BigDecimal("15.00"), ProductSize.MEDIUM);

        ProductUpdateDto updateDto = new ProductUpdateDto();
        updateDto.setCategory(ProductCategory.DRINK); // Attempt to change category

        MockMultipartFile requestPart = new MockMultipartFile(
                "request", "update.json", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(updateDto)
        );

        mockMvc.perform(multipart("/api/v1/products/{id}", existingPizza.getId())
                        .file(requestPart)
                        .with(request ->  {
                            request.setMethod("PATCH");
                            return request;
                        })
                )
                .andExpect(status().isBadRequest()); // Assuming ProductCategoryMismatchException maps to 400
    }

    // --- Tests for DELETE /api/v1/products/{id} (deleteProduct) ---

    @Test
    void deleteProduct_shouldDeleteProduct_whenIdExists() throws Exception {
        Product productToDelete = createPizzaInDb("Delete This Pizza", "Deletion test", new BigDecimal("5.00"), ProductSize.SMALL);

        mockMvc.perform(delete("/api/v1/products/{id}", productToDelete.getId()))
                .andExpect(status().isNoContent());

        Optional<Product> deletedProduct = productRepository.findById(productToDelete.getId());
        assertFalse(deletedProduct.isPresent());
    }

    @Test
    void deleteProduct_shouldReturnNotFound_whenIdDoesNotExist() throws Exception {
        mockMvc.perform(delete("/api/v1/products/{id}", 999L))
                .andExpect(status().isNotFound());
    }

}
