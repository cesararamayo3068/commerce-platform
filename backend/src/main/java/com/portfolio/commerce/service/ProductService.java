package com.portfolio.commerce.service;

import com.portfolio.commerce.domain.product.Product;
import com.portfolio.commerce.domain.product.ProductRepository;
import com.portfolio.commerce.web.dto.ProductCreateRequest;
import com.portfolio.commerce.web.dto.ProductResponse;
import com.portfolio.commerce.web.dto.ProductUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Business logic for the product catalog.
 *
 * Entity <-> DTO conversion is explicit and kept in this class on purpose:
 * no MapStruct, no generic mappers, easy to read and defend in an interview.
 */
@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public ProductResponse create(ProductCreateRequest request) {
        Product product = new Product(request.name(), request.description(), request.price(), true);
        return toResponse(productRepository.save(product));
    }

    public ProductResponse getById(Long id) {
        return toResponse(findProduct(id));
    }

    public Page<ProductResponse> list(Boolean active, Pageable pageable) {
        Page<Product> products = (active == null)
                ? productRepository.findAll(pageable)
                : productRepository.findByActive(active, pageable);
        return products.map(this::toResponse);
    }

    @Transactional
    public ProductResponse update(Long id, ProductUpdateRequest request) {
        Product product = findProduct(id);
        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        if (request.active() != null) {
            product.setActive(request.active());
        }
        // saveAndFlush ejecuta el UPDATE (y @PreUpdate) antes de construir la
        // respuesta, para que updatedAt refleje la actualizacion real.
        return toResponse(productRepository.saveAndFlush(product));
    }

    /**
     * Logical delete: the row is kept, {@code active} is set to false.
     */
    @Transactional
    public ProductResponse deactivate(Long id) {
        Product product = findProduct(id);
        product.setActive(false);
        return toResponse(productRepository.saveAndFlush(product));
    }

    private Product findProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    private ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.isActive(),
                product.getCreatedAt(),
                product.getUpdatedAt());
    }
}
