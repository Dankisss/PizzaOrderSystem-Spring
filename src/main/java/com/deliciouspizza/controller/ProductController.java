package com.deliciouspizza.controller;

import com.deliciouspizza.dto.product.ProductFilterDto;
import com.deliciouspizza.dto.product.ProductInputDto;
import com.deliciouspizza.dto.product.ProductResponseDto;
import com.deliciouspizza.dto.product.ProductUpdateDto;
import com.deliciouspizza.model.product.Product;
import com.deliciouspizza.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Retrieves all products, with optional filtering based on query parameters.
     * <p>
     * This endpoint allows clients to fetch a list of products, optionally applying filters
     * such as category, status, size, price range, and active status.
     * </p>
     * <p>Example usage:
     * <ul>
     * <li>{@code GET /api/v1/products} (returns all products)</li>
     * <li>{@code GET /api/v1/products?category=PIZZA} (filters by pizza category)</li>
     * <li>{@code GET /api/v1/products?status=ACTIVE&size=MEDIUM} (filters by active status AND medium size)</li>
     * <li>{@code GET /api/v1/products?minPrice=10.00&maxPrice=25.00} (filters products with price between $10 and $25)</li>
     * <li>{@code GET /api/v1/products?category=DRINK&active=true&maxPrice=5.00} (combines multiple filters)</li>
     * </ul>
     * </p>
     *
     * @param filterDto An object containing filter criteria parsed from query parameters.
     * Fields not provided in the query will be {@code null} in the DTO and ignored by the filtering logic in the service.
     * @return A {@code ResponseEntity} containing a list of {@link ProductResponseDto}
     * matching the filter, and an {@code HttpStatus.OK} (200) status.
     */
    @GetMapping
    public ResponseEntity<List<ProductResponseDto>> getAllProducts(@ModelAttribute ProductFilterDto filterDto) {
        return ResponseEntity.ok(productService.getAllProducts(filterDto));
    }

    /**
     * Retrieves a single product by its unique ID.
     * <p>
     * This endpoint provides detailed information for a specific product.
     * </p>
     * <p>Example usage: {@code GET /api/v1/products/123}</p>
     *
     * @param id The unique identifier of the product to retrieve.
     * @return A {@code ResponseEntity} containing the {@link ProductResponseDto} for the specified ID
     * and an {@code HttpStatus.OK} (200) status.
     * @throws com.deliciouspizza.exception.ProductNotFoundException if a product with the given ID is not found,
     * resulting in an {@code HttpStatus.NOT_FOUND} (404) response.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDto> getProduct(@PathVariable long id) {
        return ResponseEntity.ok(productService.getById(id));
    }

    /**
     * Creates a new product with its details and an optional associated photo.
     * <p>
     * This endpoint accepts product details (e.g., category, price, name) and an optional
     * image file. The request must use {@code multipart/form-data} content type.
     * </p>
     * <p>Example cURL command:</p>
     * <pre>{@code
     * curl -X POST http://localhost:8080/api/v1/products \
     * -H "Content-Type: multipart/form-data" \
     * -F "request=@product-details.json;type=application/json" \
     * -F "photo=@/path/to/your/image.jpg;type=image/jpeg"
     * }</pre>
     * <p>Where {@code product-details.json} contains:
     * {@code {"category": "PIZZA", "status": "ACTIVE", "size": "LARGE", "price": 25.00, "active": true, "totalAmount": 0.00, "pizzaType": "Pepperoni"}}
     * </p>
     *
     * @param request The {@link ProductInputDto} containing product details. This should be sent
     * as a JSON part within the {@code multipart/form-data} request, named "request".
     * It must be valid according to defined constraints (e.g., {@code @NotBlank}, {@code @Min}).
     * @param photo   Optional {@link MultipartFile} representing the product's image. This should be sent
     * as a file part named "photo". If not provided, no image will be associated.
     * @return A {@code ResponseEntity} containing the newly created {@link ProductResponseDto}
     * and an {@code HttpStatus.CREATED} (201) status.
     * @throws jakarta.validation.ConstraintViolationException (or {@code MethodArgumentNotValidException}) if input
     * validation fails, resulting in an {@code HttpStatus.BAD_REQUEST} (400) response.
     * @throws IllegalArgumentException if the product category is unsupported during concrete class creation in the service.
     * @throws RuntimeException if there is an issue reading the uploaded photo.
     */
    @PostMapping
    public ResponseEntity<ProductResponseDto> createProduct(
            @Validated @RequestPart("request") ProductInputDto request,
            @RequestPart(value = "photo", required = false) MultipartFile photo) {
        ProductResponseDto createdProduct = productService.createProduct(request, photo);
        return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
    }

    /**
     * Updates an existing product's details partially and optionally updates its photo.
     * <p>
     * This endpoint allows for flexible updates to product properties. Only fields present
     * (non-null for wrapper types) in the {@code requestDto} will be updated. The product's
     * category (discriminator column) cannot be changed through this endpoint.
     * The request must use {@code multipart/form-data} content type.
     * </p>
     * <p>Example cURL command (partial update with new photo):</p>
     * <pre>{@code
     * curl -X PATCH http://localhost:8080/api/v1/products/123 \
     * -H "Content-Type: multipart/form-data" \
     * -F "request=@update-details.json;type=application/json" \
     * -F "photo=@/path/to/new-image.png;type=image/png"
     * }</pre>
     * <p>Where {@code update-details.json} might contain:
     * {@code {"price": 28.50, "status": "INACTIVE"}}
     * </p>
     *
     * @param id         The unique identifier of the product to update.
     * @param requestDto The {@link ProductUpdateDto} containing fields to update. This should be sent
     * as a JSON part within the {@code multipart/form-data} request, named "request".
     * Only non-null fields in this DTO will update the corresponding product properties.
     * @param photo      Optional {@link MultipartFile} for a new product image. This should be sent
     * as a file part named "photo". If an empty file is sent, it can be interpreted
     * as a request to remove the existing photo.
     * @return A {@code ResponseEntity} containing the updated {@link ProductResponseDto}
     * and an {@code HttpStatus.OK} (200) status.
     * @throws com.deliciouspizza.exception.ProductNotFoundException if the product with the given ID is not found,
     * resulting in an {@code HttpStatus.NOT_FOUND} (404) response.
     * category (which is not allowed for existing products in a single-table inheritance strategy), resulting in
     * an {@code HttpStatus.BAD_REQUEST} (400) or {@code HttpStatus.CONFLICT} (409) response.
     * @throws RuntimeException if there is an issue reading the uploaded photo.
     * @throws jakarta.validation.ConstraintViolationException (or {@code MethodArgumentNotValidException}) if input
     * validation fails, resulting in an {@code HttpStatus.BAD_REQUEST} (400) response.
     */
    @PatchMapping(value = "/{id}")
    public ResponseEntity<ProductResponseDto> updateProduct(
            @PathVariable long id,
            @Validated @RequestPart("request") ProductUpdateDto requestDto,
            @RequestPart(value = "photo", required = false) MultipartFile photo) {
        return ResponseEntity.ok(productService.updateProduct(id, requestDto, photo));
    }

    /**
     * Deletes a product by its unique ID.
     * <p>
     * This endpoint permanently removes a product from the system.
     * </p>
     * <p>Example usage: {@code DELETE /api/v1/products/123}</p>
     *
     * @param id The unique identifier of the product to delete.
     * @return A {@code ResponseEntity} with no content and an {@code HttpStatus.NO_CONTENT} (204) status
     * upon successful deletion.
     * @throws com.deliciouspizza.exception.ProductNotFoundException if the product with the given ID is not found,
     * resulting in an {@code HttpStatus.NOT_FOUND} (404) response.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable long id) {
        productService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
