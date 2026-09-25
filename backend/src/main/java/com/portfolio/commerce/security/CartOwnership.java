package com.portfolio.commerce.security;
import com.portfolio.commerce.domain.cart.CartRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
@Component("cartOwnership")
public class CartOwnership {
 private final CartRepository carts;
 public CartOwnership(CartRepository carts){this.carts=carts;}
 public boolean owns(Long cartId,Authentication authentication){if(!(authentication instanceof JwtAuthenticationToken token))return false;Long userId=((Number)token.getToken().getClaim("userId")).longValue();return carts.findById(cartId).map(c->c.getUser().getId().equals(userId)).orElse(false);}
 public boolean isUser(Long userId,Authentication authentication){return authentication instanceof JwtAuthenticationToken token && userId.equals(((Number)token.getToken().getClaim("userId")).longValue());}
}
