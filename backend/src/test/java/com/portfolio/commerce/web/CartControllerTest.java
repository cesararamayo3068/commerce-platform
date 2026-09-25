package com.portfolio.commerce.web;

import com.portfolio.commerce.domain.cart.CartStatus;
import com.portfolio.commerce.service.CartItemNotFoundException;
import com.portfolio.commerce.service.CartNotActiveException;
import com.portfolio.commerce.service.CartNotFoundException;
import com.portfolio.commerce.service.CartService;
import com.portfolio.commerce.web.dto.CartResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(CartController.class)
class CartControllerTest {
    @org.junit.jupiter.api.BeforeEach void allowTestOwnership(){
        org.mockito.Mockito.when(ownership.isUser(org.mockito.ArgumentMatchers.any(),org.mockito.ArgumentMatchers.nullable(org.springframework.security.core.Authentication.class))).thenReturn(true);
        org.mockito.Mockito.when(ownership.owns(org.mockito.ArgumentMatchers.any(),org.mockito.ArgumentMatchers.nullable(org.springframework.security.core.Authentication.class))).thenReturn(true);
    }


    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CartService cartService;
    @MockitoBean private com.portfolio.commerce.security.CartOwnership ownership;

    private CartResponse response(Long id, Long userId, CartStatus status) {
        return new CartResponse(id, userId, status, List.of(), BigDecimal.ZERO,
                Instant.parse("2026-09-18T10:00:00Z"), Instant.parse("2026-09-18T10:00:00Z"));
    }

    @Test
    void createReturns201() throws Exception {
        when(cartService.create(any())).thenReturn(response(1L, 1L, CartStatus.ACTIVE));

        mockMvc.perform(post("/api/carts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"userId": 1}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void createWithoutUserIdReturns400() throws Exception {
        mockMvc.perform(post("/api/carts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("userId"));
    }

    @Test
    void createWithMalformedBodyReturns400() throws Exception {
        mockMvc.perform(post("/api/carts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not-json}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void getByIdReturns200() throws Exception {
        when(cartService.getById(1L)).thenReturn(response(1L, 1L, CartStatus.ACTIVE));

        mockMvc.perform(get("/api/carts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void getByIdReturns404WhenCartNotFound() throws Exception {
        when(cartService.getById(99L)).thenThrow(new CartNotFoundException(99L));

        mockMvc.perform(get("/api/carts/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.path").value("/api/carts/99"));
    }

    @Test
    void listReturnsPagedModel() throws Exception {
        when(cartService.list(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(response(1L, 1L, CartStatus.ACTIVE))));

        mockMvc.perform(get("/api/carts").param("userId", "1").param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.cartResponseList[0].id").value(1))
                .andExpect(jsonPath("$.page.totalElements").value(1))
                .andExpect(jsonPath("$.page.number").value(0));
    }

    @Test
    void listWithInvalidStatusReturns400() throws Exception {
        mockMvc.perform(get("/api/carts").param("status", "BOGUS"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void addItemReturns200() throws Exception {
        when(cartService.addItem(eq(1L), any())).thenReturn(response(1L, 1L, CartStatus.ACTIVE));

        mockMvc.perform(post("/api/carts/1/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productId": 5, "quantity": 2}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void addItemWithQuantityZeroReturns400() throws Exception {
        mockMvc.perform(post("/api/carts/1/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productId": 5, "quantity": 0}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("quantity"));
    }

    @Test
    void addItemWithoutProductIdReturns400() throws Exception {
        mockMvc.perform(post("/api/carts/1/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"quantity": 1}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("productId"));
    }

    @Test
    void addItemToCancelledCartReturns409() throws Exception {
        when(cartService.addItem(eq(1L), any())).thenThrow(new CartNotActiveException(1L));

        mockMvc.perform(post("/api/carts/1/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productId": 5, "quantity": 1}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"));
    }

    @Test
    void updateQuantityReturns200() throws Exception {
        when(cartService.updateQuantity(eq(1L), eq(5L), any())).thenReturn(response(1L, 1L, CartStatus.ACTIVE));

        mockMvc.perform(put("/api/carts/1/items/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"quantity": 4}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void updateQuantityWithZeroReturns400() throws Exception {
        mockMvc.perform(put("/api/carts/1/items/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"quantity": 0}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("quantity"));
    }

    @Test
    void removeItemReturns204() throws Exception {
        mockMvc.perform(delete("/api/carts/1/items/5"))
                .andExpect(status().isNoContent());
    }

    @Test
    void removeItemReturns404WhenItemNotFound() throws Exception {
        doThrow(new CartItemNotFoundException(1L, 5L)).when(cartService).removeItem(1L, 5L);

        mockMvc.perform(delete("/api/carts/1/items/5"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void cancelReturns200WithCancelledStatus() throws Exception {
        when(cartService.cancel(1L)).thenReturn(response(1L, 1L, CartStatus.CANCELLED));

        mockMvc.perform(delete("/api/carts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void cancelReturns409WhenNotActive() throws Exception {
        when(cartService.cancel(1L)).thenThrow(new CartNotActiveException(1L));

        mockMvc.perform(delete("/api/carts/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }
}
