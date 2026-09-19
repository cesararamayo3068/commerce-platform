package com.portfolio.commerce.web;

import com.portfolio.commerce.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * CORS behavior for the Angular frontend.
 *
 * The allowed origin is configured via {@code cors.allowed-origins}
 * (default {@code http://localhost:14200}); the wildcard is never used.
 */
@WebMvcTest(ProductController.class)
class CorsConfigTest {

    private static final String ALLOWED_ORIGIN = "http://localhost:14200";
    private static final String DISALLOWED_ORIGIN = "http://evil.example.com";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @Test
    void preflightFromAllowedOriginIsAccepted() throws Exception {
        mockMvc.perform(options("/api/products")
                        .header("Origin", ALLOWED_ORIGIN)
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", ALLOWED_ORIGIN))
                .andExpect(header().string("Access-Control-Allow-Methods",
                        org.hamcrest.Matchers.containsString("GET")));
    }

    @Test
    void preflightFromDisallowedOriginIsRejected() throws Exception {
        mockMvc.perform(options("/api/products")
                        .header("Origin", DISALLOWED_ORIGIN)
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }
}
