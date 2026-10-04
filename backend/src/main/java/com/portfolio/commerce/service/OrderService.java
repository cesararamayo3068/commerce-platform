package com.portfolio.commerce.service;

import com.portfolio.commerce.domain.cart.*;
import com.portfolio.commerce.domain.order.*;
import com.portfolio.commerce.domain.product.*;
import com.portfolio.commerce.web.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final PromotionService promotionService;

    public OrderService(OrderRepository orderRepository, OrderItemRepository orderItemRepository,
                        CartRepository cartRepository, CartItemRepository cartItemRepository,
                        ProductRepository productRepository, PromotionService promotionService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.promotionService = promotionService;
    }

    @Transactional
    public OrderResponse checkout(Long cartId, Long userId) {
        Cart cart = cartRepository.findByIdForUpdate(cartId).orElseThrow(() -> new CartNotFoundException(cartId));
        if (!cart.getUser().getId().equals(userId)) throw new org.springframework.security.access.AccessDeniedException("Not your cart");
        if (cart.getStatus() != CartStatus.ACTIVE) {
            if (orderRepository.existsByCartId(cartId)) throw new OrderAlreadyExistsException(cartId);
            throw new CartNotActiveException(cartId);
        }

        List<CartItem> cartItems = cartItemRepository.findByCartIdOrderByIdAsc(cartId);
        if (cartItems.isEmpty()) throw new EmptyCartException(cartId);

        BigDecimal subtotal = BigDecimal.ZERO;
        record Snapshot(Product product, int quantity, BigDecimal unitPrice) {}
        java.util.ArrayList<Snapshot> snapshots = new java.util.ArrayList<>();

        for (CartItem item : cartItems) {
            Product product = productRepository.findByIdForUpdate(item.getProduct().getId())
                    .orElseThrow(() -> new ProductNotFoundException(item.getProduct().getId()));
            if (!product.isActive()) throw new ProductNotActiveException(product.getId());
            if (product.getStock() < item.getQuantity()) {
                throw new InsufficientStockException(product.getName(), item.getQuantity(), product.getStock());
            }
            BigDecimal unitPrice = product.getPrice();
            subtotal = subtotal.add(unitPrice.multiply(BigDecimal.valueOf(item.getQuantity())));
            snapshots.add(new Snapshot(product, item.getQuantity(), unitPrice));
        }

        BigDecimal discount = cart.getPromotion() == null
                ? BigDecimal.ZERO
                : promotionService.discount(cart.getPromotion(), subtotal);
        BigDecimal total = subtotal.subtract(discount);
        String promotionCode = discount.signum() > 0 && cart.getPromotion() != null ? cart.getPromotion().getCode() : null;

        Order order = orderRepository.save(new Order(cart.getUser(), cart, subtotal, discount, total, promotionCode));
        for (Snapshot snapshot : snapshots) {
            snapshot.product().decreaseStock(snapshot.quantity());
            orderItemRepository.save(new OrderItem(order, snapshot.product(), snapshot.product().getName(), snapshot.unitPrice(), snapshot.quantity()));
        }
        cart.setStatus(CartStatus.CHECKED_OUT);
        cartRepository.saveAndFlush(cart);
        return toResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> listForUser(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getById(Long orderId, Long userId) {
        return orderRepository.findByIdAndUserId(orderId, userId)
                .map(this::toResponse)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = orderItemRepository.findByOrderIdOrderByIdAsc(order.getId()).stream()
                .map(item -> new OrderItemResponse(item.getProduct().getId(), item.getProductName(), item.getUnitPrice(), item.getQuantity(), item.getSubtotal()))
                .toList();
        return new OrderResponse(order.getId(), order.getUser().getId(), order.getCart().getId(), order.getStatus(), items,
                order.getSubtotal(), order.getDiscount(), order.getTotal(), order.getPromotionCode(), order.getCreatedAt(), order.getUpdatedAt());
    }
}
