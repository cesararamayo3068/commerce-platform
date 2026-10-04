package com.portfolio.commerce.domain.order;

import com.portfolio.commerce.domain.AuditableEntity;
import com.portfolio.commerce.domain.cart.Cart;
import com.portfolio.commerce.domain.user.User;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "orders")
public class Order extends AuditableEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false) private User user;
    @OneToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "cart_id", nullable = false, unique = true) private Cart cart;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private OrderStatus status;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal subtotal;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal discount;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal total;
    @Column(name = "promotion_code", length = 40) private String promotionCode;

    protected Order() {}

    public Order(User user, Cart cart, BigDecimal subtotal, BigDecimal discount, BigDecimal total, String promotionCode) {
        this.user = user;
        this.cart = cart;
        this.status = OrderStatus.CONFIRMED;
        this.subtotal = subtotal;
        this.discount = discount;
        this.total = total;
        this.promotionCode = promotionCode;
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public Cart getCart() { return cart; }
    public OrderStatus getStatus() { return status; }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getDiscount() { return discount; }
    public BigDecimal getTotal() { return total; }
    public String getPromotionCode() { return promotionCode; }
}
