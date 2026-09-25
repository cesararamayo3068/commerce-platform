package com.portfolio.commerce.security;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@SpringBootTest @AutoConfigureMockMvc
class SecurityRulesTest {
 @Autowired MockMvc mvc;
 @Test void anonymousCannotCreateProduct()throws Exception{mvc.perform(post("/api/products")).andExpect(status().isUnauthorized());}
 @Test void anonymousCannotAccessCarts()throws Exception{mvc.perform(get("/api/carts")).andExpect(status().isUnauthorized());}
 @Test void healthIsPublic()throws Exception{mvc.perform(get("/actuator/health")).andExpect(status().isOk());}
}
