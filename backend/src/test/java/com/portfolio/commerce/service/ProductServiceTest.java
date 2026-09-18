package com.portfolio.commerce.service;

import com.portfolio.commerce.domain.product.Product;
import com.portfolio.commerce.domain.product.ProductRepository;
import com.portfolio.commerce.web.dto.ProductCreateRequest;
import com.portfolio.commerce.web.dto.ProductResponse;
import com.portfolio.commerce.web.dto.ProductUpdateRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product product(Long id, String name, BigDecimal price, boolean active) {
        Product p = new Product(name, "Description of " + name, price, active);
        setId(p, id);
        return p;
    }

    private void setId(Product p, Long id) {
        try {
            var field = Product.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(p, id);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private void setUpdatedAt(Product p, Instant updatedAt) {
        try {
            var field = Product.class.getSuperclass().getDeclaredField("updatedAt");
            field.setAccessible(true);
            field.set(p, updatedAt);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    void createPersistsProductActiveByDefault() {
        ProductCreateRequest request = new ProductCreateRequest("Teclado", "Teclado mecanico", new BigDecimal("99.99"));
        Product saved = product(1L, "Teclado", new BigDecimal("99.99"), true);
        when(productRepository.save(any(Product.class))).thenReturn(saved);

        ProductResponse response = productService.create(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Teclado");
        assertThat(response.price()).isEqualByComparingTo("99.99");
        assertThat(response.active()).isTrue();
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void getByIdReturnsExistingProduct() {
        Product existing = product(1L, "Teclado", new BigDecimal("99.99"), true);
        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));

        ProductResponse response = productService.getById(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Teclado");
    }

    @Test
    void getByIdThrowsWhenProductDoesNotExist() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getById(99L))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void updateChangesFieldsAndKeepsActiveWhenNotProvided() {
        Product existing = product(1L, "Teclado", new BigDecimal("99.99"), true);
        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(productRepository.saveAndFlush(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductUpdateRequest request = new ProductUpdateRequest("Teclado RGB", "Nuevo", new BigDecimal("129.00"), null);
        ProductResponse response = productService.update(1L, request);

        assertThat(response.name()).isEqualTo("Teclado RGB");
        assertThat(response.price()).isEqualByComparingTo("129.00");
        assertThat(response.active()).isTrue();
    }

    @Test
    void updateAppliesActiveWhenProvided() {
        Product existing = product(1L, "Teclado", new BigDecimal("99.99"), true);
        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(productRepository.saveAndFlush(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductUpdateRequest request = new ProductUpdateRequest("Teclado", "Desc", new BigDecimal("99.99"), false);
        ProductResponse response = productService.update(1L, request);

        assertThat(response.active()).isFalse();
    }

    @Test
    void updateFlushesBeforeBuildingResponseSoUpdatedAtIsCurrent() {
        Product existing = product(1L, "Teclado", new BigDecimal("99.99"), true);
        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(productRepository.saveAndFlush(any(Product.class))).thenAnswer(inv -> {
            Product p = inv.getArgument(0);
            setUpdatedAt(p, Instant.parse("2026-09-18T12:00:00Z"));
            return p;
        });

        ProductUpdateRequest request = new ProductUpdateRequest("Teclado RGB", "Nuevo", new BigDecimal("129.00"), null);
        ProductResponse response = productService.update(1L, request);

        assertThat(response.updatedAt()).isEqualTo(Instant.parse("2026-09-18T12:00:00Z"));
        verify(productRepository).saveAndFlush(any(Product.class));
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void deactivateSetsActiveToFalse() {
        Product existing = product(1L, "Teclado", new BigDecimal("99.99"), true);
        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(productRepository.saveAndFlush(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductResponse response = productService.deactivate(1L);

        assertThat(response.active()).isFalse();
        verify(productRepository).saveAndFlush(any(Product.class));
    }

    @Test
    void listWithoutFilterUsesFindAll() {
        Pageable pageable = PageRequest.of(0, 10);
        when(productRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(product(1L, "A", new BigDecimal("1.00"), true))));

        Page<ProductResponse> page = productService.list(null, pageable);

        assertThat(page.getContent()).hasSize(1);
        verify(productRepository, never()).findByActive(anyBoolean(), any(Pageable.class));
    }

    @Test
    void listWithActiveFilterUsesFindByActive() {
        Pageable pageable = PageRequest.of(0, 10);
        when(productRepository.findByActive(eq(true), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(product(1L, "A", new BigDecimal("1.00"), true))));

        Page<ProductResponse> page = productService.list(true, pageable);

        assertThat(page.getContent()).hasSize(1);
        verify(productRepository, never()).findAll(any(Pageable.class));
    }
}
