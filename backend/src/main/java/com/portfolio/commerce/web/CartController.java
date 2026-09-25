package com.portfolio.commerce.web;

import com.portfolio.commerce.domain.cart.CartStatus;
import com.portfolio.commerce.service.CartService;
import com.portfolio.commerce.web.dto.CartCreateRequest;
import com.portfolio.commerce.web.dto.CartItemQuantityUpdateRequest;
import com.portfolio.commerce.web.dto.CartItemRequest;
import com.portfolio.commerce.web.dto.CartResponse;
import com.portfolio.commerce.web.error.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API for the shopping cart lifecycle and content.
 */
@RestController
@RequestMapping("/api/carts")
@Tag(name = "Carts", description = "Shopping cart management")
public class CartController {

    private final CartService cartService;
    private final com.portfolio.commerce.security.CartOwnership ownership;

    public CartController(CartService cartService, com.portfolio.commerce.security.CartOwnership ownership) {
        this.ownership = ownership;
        this.cartService = cartService;
    }

    @PostMapping
    @Operation(summary = "Create a cart", description = "Creates an ACTIVE cart for the given user, with no items")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cart created"),
            @ApiResponse(responseCode = "400", description = "Invalid request body",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<CartResponse> create(@Valid @RequestBody CartCreateRequest request) {
        if (!ownership.isUser(request.userId(), org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication())) throw new org.springframework.security.access.AccessDeniedException("Not your account");
        return ResponseEntity.status(HttpStatus.CREATED).body(cartService.create(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a cart by id", description = "Returns the cart with its items, item subtotals and cart total")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cart found"),
            @ApiResponse(responseCode = "404", description = "Cart not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public CartResponse getById(@PathVariable Long id) {
        checkOwner(id); return cartService.getById(id);
    }

    @GetMapping
    @Operation(summary = "List carts", description = "Paged listing, optionally filtered by userId and/or status")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paged list of carts"),
            @ApiResponse(responseCode = "400", description = "Invalid pagination, sort or status parameter",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public PagedModel<CartResponse> list(@ParameterObject Pageable pageable,
                                         @RequestParam(required = false) Long userId,
                                         @RequestParam(required = false) CartStatus status) {
        if (userId == null || !ownership.isUser(userId, org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication())) throw new org.springframework.security.access.AccessDeniedException("Filter by your userId");
        Page<CartResponse> page = cartService.list(userId, status, pageable);
        return PagedModel.of(page.getContent(),
                new PagedModel.PageMetadata(page.getSize(), page.getNumber(),
                        page.getTotalElements(), page.getTotalPages()));
    }

    @PostMapping("/{cartId}/items")
    @Operation(summary = "Add a product to the cart", description = "Creates the item, or increments the quantity if the product is already in the cart")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cart updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request body",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Cart or product not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Cart is not ACTIVE or product is inactive",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public CartResponse addItem(@PathVariable Long cartId, @Valid @RequestBody CartItemRequest request) {
        checkOwner(cartId); return cartService.addItem(cartId, request);
    }

    @PutMapping("/{cartId}/items/{productId}")
    @Operation(summary = "Update item quantity", description = "Sets the quantity to the exact value provided (not an increment)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cart updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request body",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Cart or item not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Cart is not ACTIVE",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public CartResponse updateQuantity(@PathVariable Long cartId, @PathVariable Long productId,
                                       @Valid @RequestBody CartItemQuantityUpdateRequest request) {
        checkOwner(cartId); return cartService.updateQuantity(cartId, productId, request);
    }

    @DeleteMapping("/{cartId}/items/{productId}")
    @Operation(summary = "Remove a product from the cart", description = "Removes the item completely; the quantity is not decremented")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Item removed"),
            @ApiResponse(responseCode = "404", description = "Cart or item not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Cart is not ACTIVE",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<Void> removeItem(@PathVariable Long cartId, @PathVariable Long productId) {
        checkOwner(cartId); cartService.removeItem(cartId, productId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel a cart", description = "Logical delete: sets status to CANCELLED, the cart and its items are kept")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cart cancelled"),
            @ApiResponse(responseCode = "404", description = "Cart not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Cart is not ACTIVE",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public CartResponse cancel(@PathVariable Long id) {
        checkOwner(id); return cartService.cancel(id);
    }
    @PostMapping("/{id}/coupon")
    public CartResponse applyCoupon(@PathVariable Long id,@Valid @RequestBody CouponRequest request){
        checkOwner(id);return cartService.applyCoupon(id,request.code());
    }
    @DeleteMapping("/{id}/coupon")
    public CartResponse removeCoupon(@PathVariable Long id){
        checkOwner(id);return cartService.removeCoupon(id);
    }
    public record CouponRequest(@jakarta.validation.constraints.NotBlank String code){}
    private void checkOwner(Long id) {
        if (!ownership.owns(id, org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication())) throw new org.springframework.security.access.AccessDeniedException("Not your cart");
    }
}
