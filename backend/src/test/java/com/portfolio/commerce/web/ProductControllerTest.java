package com.portfolio.commerce.web;

import com.portfolio.commerce.domain.product.Product;
import com.portfolio.commerce.service.ProductNotFoundException;
import com.portfolio.commerce.service.ProductService;
import com.portfolio.commerce.web.dto.ProductResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.data.util.TypeInformation;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    private ProductResponse response(Long id, String name, boolean active) {
        return new ProductResponse(id, name, "Description", null, null, null, new BigDecimal("99.99"), active,
                Instant.parse("2026-09-18T10:00:00Z"), Instant.parse("2026-09-18T10:00:00Z"));
    }

    @Test
    void createReturns201() throws Exception {
        when(productService.create(any())).thenReturn(response(1L, "Teclado", true));

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Teclado", "description": "Mecanico", "price": 99.99}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Teclado"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void createWithBlankNameReturns400() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": " ", "price": 10.00}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("name"));
    }

    @Test
    void createWithNegativePriceReturns400() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Teclado", "price": -5.00}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("price"));
    }

    @Test
    void createWithoutPriceReturns400() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Teclado"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("price"));
    }

    @Test
    void getByIdReturns200() throws Exception {
        when(productService.getById(1L)).thenReturn(response(1L, "Teclado", true));

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Teclado"));
    }

    @Test
    void getByIdReturns404WhenNotFound() throws Exception {
        when(productService.getById(99L)).thenThrow(new ProductNotFoundException(99L));

        mockMvc.perform(get("/api/products/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.path").value("/api/products/99"));
    }

    @Test
    void listReturnsPagedModel() throws Exception {
        when(productService.list(any(), any())).thenReturn(new PageImpl<>(List.of(response(1L, "Teclado", true))));

        mockMvc.perform(get("/api/products").param("active", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.productResponseList[0].name").value("Teclado"))
                .andExpect(jsonPath("$.page.totalElements").value(1))
                .andExpect(jsonPath("$.page.number").value(0));
    }

    @Test
    void listWithInvalidActiveParamReturns400() throws Exception {
        mockMvc.perform(get("/api/products").param("active", "not-a-boolean"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    void listWithInvalidSortReturns400() throws Exception {
        when(productService.list(any(), any())).thenThrow(
                new PropertyReferenceException("unknownField", TypeInformation.of(Product.class), List.of()));

        mockMvc.perform(get("/api/products").param("sort", "unknownField"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid sort property: unknownField"));
    }

    @Test
    void updateReturns200() throws Exception {
        when(productService.update(eq(1L), any())).thenReturn(response(1L, "Teclado RGB", true));

        mockMvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Teclado RGB", "price": 129.00}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Teclado RGB"));
    }

    @Test
    void deactivateReturns200WithActiveFalse() throws Exception {
        when(productService.deactivate(1L)).thenReturn(response(1L, "Teclado", false));

        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }
}
