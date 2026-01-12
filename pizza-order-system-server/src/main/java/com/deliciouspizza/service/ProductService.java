package com.deliciouspizza.service;

import com.deliciouspizza.dto.product.ProductFilterDto;
import com.deliciouspizza.dto.product.ProductInputDto;
import com.deliciouspizza.dto.product.ProductResponseDto;
import com.deliciouspizza.dto.product.ProductUpdateDto;
import com.deliciouspizza.exception.InvalidProductException;
import com.deliciouspizza.exception.OrderProductAlreadyExistsException;
import com.deliciouspizza.exception.ProductNotFoundException;
import com.deliciouspizza.model.product.Drink;
import com.deliciouspizza.model.product.Pizza;
import com.deliciouspizza.model.product.Product;
import com.deliciouspizza.model.product.Sauce;
import com.deliciouspizza.repository.ProductRepository;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<ProductResponseDto> getAllProducts(ProductFilterDto filterDto) {
        Specification<Product> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filterDto != null) {

                if (filterDto.getCategory() != null) {
                    predicates.add(criteriaBuilder.equal(root.get("category"), filterDto.getCategory()));
                }

                if (filterDto.getStatus() != null) {
                    predicates.add(criteriaBuilder.equal(root.get("status"), filterDto.getStatus()));
                }

                if (filterDto.getSize() != null) {
                    predicates.add(criteriaBuilder.equal(root.get("size"), filterDto.getSize()));
                }

                if (filterDto.getActive() != null) {
                    predicates.add(criteriaBuilder.equal(root.get("active"), filterDto.getActive()));
                }

                if (filterDto.getMinPrice() != null) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), filterDto.getMinPrice()));
                }

                if (filterDto.getMaxPrice() != null) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), filterDto.getMaxPrice()));
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return productRepository.findAll(spec)
                .stream()
                .map(this::mapProductToProductResponseDto)
                .toList();
    }

    public ProductResponseDto getById(long id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException("Id: " + id));

        return mapProductToProductResponseDto(product);
    }

    @Transactional
    public ProductResponseDto createProduct(ProductInputDto productInputDto, MultipartFile photo) {
        Product newProduct = switch (productInputDto.getCategory()) {
            case PIZZA -> new Pizza(
                    productInputDto.getStatus(),
                    productInputDto.getName(),
                    productInputDto.getDescription(),
                    productInputDto.getSize(),
                    productInputDto.getPrice(),
                    productInputDto.isActive(),
                    productInputDto.getTotalAmount()
            );
            case SAUCE -> new Sauce(
                    productInputDto.getStatus(),
                    productInputDto.getName(),
                    productInputDto.getDescription(),
                    productInputDto.getSize(),
                    productInputDto.getPrice(),
                    productInputDto.isActive(),
                    productInputDto.getTotalAmount()
            );
            case DRINK -> new Drink(
                    productInputDto.getStatus(),
                    productInputDto.getName(),
                    productInputDto.getDescription(),
                    productInputDto.getSize(),
                    productInputDto.getPrice(),
                    productInputDto.isActive(),
                    productInputDto.getTotalAmount(),
                    productInputDto.getAlcoholic()
            );
            default ->
                    throw new InvalidProductException("Product from category is invalid: " + productInputDto.getCategory());
        };

        try {
            newProduct.setImageData(photo != null ? photo.getBytes() : null);
        } catch (IOException e) {
            throw new RuntimeException("An error occurred while converting the image to bytes: " + e.getMessage(), e);
        }

        productRepository.saveAndFlush(newProduct);

        return mapProductToProductResponseDto(newProduct);
    }

    public void deleteById(long id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException("Id: " + id));

        productRepository.delete(product);
    }

    /**
     * Updates an existing product's details partially based on provided non-null fields in the DTO.
     * Ensures that the product's category (discriminator) is not changed.
     *
     * @param id The ID of the product to update.
     * @param requestDto The DTO containing the updated product details. Only non-null fields will be updated.
     * @param photo Optional: New photo to update. Can be null.
     * @return The updated Product entity.
     * @throws ProductNotFoundException If the product with the given ID is not found.
     * @throws RuntimeException If there's an issue processing the photo.
     */
    @Transactional
    public ProductResponseDto updateProduct(long id, ProductUpdateDto requestDto, MultipartFile photo) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + id));

        if (requestDto.getCategory() != null && !existingProduct.getCategory().equals(requestDto.getCategory())) {
            throw new OrderProductAlreadyExistsException.ProductCategoryMismatchException(
                    "Cannot change product category from " + existingProduct.getCategory() +
                            " to " + requestDto.getCategory() + " for existing product with ID: " + id
            );
        }

        if (requestDto.getStatus() != null) {
            existingProduct.setStatus(requestDto.getStatus());
        }
        if (requestDto.getCapacity() != null) {
            existingProduct.setCapacity(requestDto.getCapacity());
        }
        if (requestDto.getPrice() != null) {
            existingProduct.setPrice(requestDto.getPrice());
        }
        if (requestDto.getActive() != null) {
            existingProduct.setActive(requestDto.getActive());
        }
        if (requestDto.getTotalAmount() != null) {
            existingProduct.setTotalAmount(requestDto.getTotalAmount());
        }
        if (requestDto.getName() != null) {
            existingProduct.setName(requestDto.getName());
        }
        if (requestDto.getDescription() != null) {
            existingProduct.setDescription(requestDto.getDescription());
        }

        existingProduct.setUpdatedAt(Instant.now());

        switch (existingProduct) {
            case Pizza pizza -> {
                if (requestDto.getPizzaType() != null) {
                    pizza.setName(requestDto.getPizzaType());
                }
            }
            case Drink drink -> {
                if (requestDto.getDrinkType() != null) {
                    drink.setName(requestDto.getDrinkType());
                }
                if (requestDto.getIsAlcoholic() != null) {
                    drink.setAlcoholic(requestDto.getIsAlcoholic());
                }
            }
            case Sauce sauce -> {}
            default -> throw new IllegalStateException("Unexpected value: " + existingProduct.getCategory());
        }


        if (photo != null && !photo.isEmpty()) {
            try {
                existingProduct.setImageData(photo.getBytes()); // Update byte array
            } catch (IOException e) {
                throw new RuntimeException("Failed to read image data for product update", e);
            }
        } else if (photo != null && photo.isEmpty() && existingProduct.getImageData() != null) {
            existingProduct.setImageData(null);
        }

        productRepository.save(existingProduct);

        return mapProductToProductResponseDto(existingProduct);
    }

    private ProductResponseDto mapProductToProductResponseDto(Product product) {
        ProductResponseDto dto = new ProductResponseDto();
        dto.setId(product.getId());
        dto.setCategory(product.getCategory());
        dto.setStatus(product.getStatus());
        dto.setSize(product.getCapacity());
        dto.setPrice(product.getPrice());
        dto.setActive(product.isActive()); // primitive boolean directly maps to Boolean wrapper
        dto.setTotalAmount(product.getTotalAmount());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());

        dto.setName(product.getName());
        dto.setDescription(product.getDescription());

        if (product instanceof Pizza pizza) {
            dto.setName(pizza.getName());
        } else if (product instanceof Drink drink) {
            dto.setName(drink.getName());
            dto.setIsAlcoholic(drink.getAlcoholic());
        }

        dto.setHasImage(product.getImageData() != null && product.getImageData().length > 0);

        return dto;
    }
}
