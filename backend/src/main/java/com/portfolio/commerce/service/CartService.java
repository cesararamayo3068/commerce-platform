package com.portfolio.commerce.service;

import com.portfolio.commerce.domain.cart.Cart;
import com.portfolio.commerce.domain.cart.CartItem;
import com.portfolio.commerce.domain.cart.CartItemRepository;
import com.portfolio.commerce.domain.cart.CartRepository;
import com.portfolio.commerce.domain.cart.CartStatus;
import com.portfolio.commerce.domain.product.Product;
import com.portfolio.commerce.domain.product.ProductRepository;
import com.portfolio.commerce.domain.user.User;
import com.portfolio.commerce.domain.user.UserRepository;
import com.portfolio.commerce.web.dto.CartCreateRequest;
import com.portfolio.commerce.web.dto.CartItemQuantityUpdateRequest;
import com.portfolio.commerce.web.dto.CartItemRequest;
import com.portfolio.commerce.web.dto.CartItemResponse;
import com.portfolio.commerce.web.dto.CartResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Business logic for the shopping cart lifecycle and content.
 *
 * Entity <-> DTO conversion is explicit and kept in this class, following
 * the same style as {@link ProductService}: no MapStruct, no generic mappers.
 *
 * The cart total is always computed from the current items (sum of item
 * subtotals) and never persisted. No discounts are applied at this stage.
 */
@Service
public class CartService {

    private final PromotionService promotionService;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository,
                       UserRepository userRepository, ProductRepository productRepository, PromotionService promotionService) {
        this.promotionService = promotionService;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public CartResponse create(CartCreateRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new UserNotFoundException(request.userId()));
        Cart cart = new Cart(user, CartStatus.ACTIVE);
        return toResponse(cartRepository.save(cart));
    }

    @Transactional(readOnly = true)
    public CartResponse getById(Long id) {
        return toResponse(findCart(id));
    }

    @Transactional(readOnly = true)
    public Page<CartResponse> list(Long userId, CartStatus status, Pageable pageable) {
        Page<Cart> carts;
        if (userId != null && status != null) {
            carts = cartRepository.findByUserIdAndStatus(userId, status, pageable);
        } else if (userId != null) {
            carts = cartRepository.findByUserId(userId, pageable);
        } else if (status != null) {
            carts = cartRepository.findByStatus(status, pageable);
        } else {
            carts = cartRepository.findAll(pageable);
        }
        return carts.map(this::toResponse);
    }

    @Transactional
    public CartResponse addItem(Long cartId, CartItemRequest request) {
        Cart cart = findActiveCart(cartId);
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ProductNotFoundException(request.productId()));
        if (!product.isActive()) {
            throw new ProductNotActiveException(request.productId());
        }
        cartItemRepository.findByCartIdAndProductId(cartId, request.productId())
                .ifPresentOrElse(
                        item -> item.setQuantity(item.getQuantity() + request.quantity()),
                        () -> cartItemRepository.save(new CartItem(cart, product, request.quantity())));
        return toResponse(cart);
    }

    @Transactional
    public CartResponse updateQuantity(Long cartId, Long productId, CartItemQuantityUpdateRequest request) {
        Cart cart = findActiveCart(cartId);
        CartItem item = findItem(cartId, productId);
        item.setQuantity(request.quantity());
        return toResponse(cart);
    }

    @Transactional
    public void removeItem(Long cartId, Long productId) {
        findActiveCart(cartId);
        CartItem item = findItem(cartId, productId);
        cartItemRepository.delete(item);
    }

    @Transactional
    public CartResponse cancel(Long cartId) {
        Cart cart = findActiveCart(cartId);
        cart.setStatus(CartStatus.CANCELLED);
        return toResponse(cartRepository.saveAndFlush(cart));
    }

    @Transactional
    public CartResponse applyCoupon(Long id,String code){
        Cart cart=findActiveCart(id);
        cart.setPromotion(promotionService.validCoupon(code));
        return toResponse(cart);
    }

    @Transactional
    public CartResponse removeCoupon(Long id){
        Cart cart=findActiveCart(id);
        cart.setPromotion(null);
        return toResponse(cart);
    }

    private Cart findCart(Long id) {
        return cartRepository.findById(id)
                .orElseThrow(() -> new CartNotFoundException(id));
    }

    private Cart findActiveCart(Long cartId) {
        Cart cart = findCart(cartId);
        if (cart.getStatus() != CartStatus.ACTIVE) {
            throw new CartNotActiveException(cartId);
        }
        return cart;
    }

    private CartItem findItem(Long cartId, Long productId) {
        return cartItemRepository.findByCartIdAndProductId(cartId, productId)
                .orElseThrow(() -> new CartItemNotFoundException(cartId, productId));
    }

    private CartResponse toResponse(Cart cart) {
        List<CartItemResponse> items = cartItemRepository.findByCartIdOrderByIdAsc(cart.getId())
                .stream()
                .map(this::toItemResponse)
                .toList();
        BigDecimal total = items.stream()
                .map(CartItemResponse::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal discount=cart.getPromotion()==null?BigDecimal.ZERO:promotionService.discount(cart.getPromotion(),total);
        return new CartResponse(
                cart.getId(),
                cart.getUser().getId(),
                cart.getStatus(),
                items,
                total.subtract(discount),
                total,
                discount,
                discount.signum()>0?cart.getPromotion().getCode():null,
                cart.getCreatedAt(),
                cart.getUpdatedAt());
    }

    private CartItemResponse toItemResponse(CartItem item) {
        Product product = item.getProduct();
        BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        return new CartItemResponse(
                item.getId(),
                product.getId(),
                product.getName(),
                product.getPrice(),
                item.getQuantity(),
                subtotal);
    }
}
