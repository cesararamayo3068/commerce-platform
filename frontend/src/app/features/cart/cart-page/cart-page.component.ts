import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CurrencyPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { CartService } from '../../../core/services/cart.service';
import { ToastService } from '../../../shared/components/toast/toast.service';
import { toUserMessage } from '../../../core/utils/error-handler';
import { LoadingComponent } from '../../../shared/components/loading/loading.component';
import { EmptyStateComponent } from '../../../shared/components/empty-state/empty-state.component';

@Component({
  selector: 'app-cart-page',
  standalone: true,
  imports: [FormsModule, CurrencyPipe, RouterLink, LoadingComponent, EmptyStateComponent],
  template: `
    <section class="cart-page">
      <div class="cart-page__header">
        <div>
          <h1 class="cart-page__title">Tu carrito</h1>
          <p class="cart-page__subtitle">
            @if (cartService.cart(); as cart) {
              Estado: <span class="cart-page__status">{{ cart.status }}</span>
            }
          </p>
        </div>
      </div>

      @if (cartService.loading()) {
        <app-loading />
      } @else if (cartService.cart(); as cart) {
        @if (cart.status !== 'ACTIVE') {
          <app-empty-state
            title="Carrito {{ cart.status.toLowerCase() }}"
            message="Este carrito ya no está activo. Creá uno nuevo para seguir comprando."
          />
          <div class="cart-page__actions">
            <button class="btn btn--primary" (click)="createNewCart()">Crear nuevo carrito</button>
          </div>
        } @else if (cart.items.length === 0) {
          <app-empty-state title="Tu carrito está vacío" message="Agregá productos desde el catálogo." />
          <div class="cart-page__actions">
            <a class="btn btn--primary" routerLink="/products">Ir al catálogo</a>
          </div>
        } @else {
          <div class="cart-page__layout">
            <div class="cart-page__items">
              @for (item of cart.items; track item.productId) {
                <article class="cart-item" data-testid="cart-item">
                  <div class="cart-item__visual" aria-hidden="true">◈</div>
                  <div class="cart-item__info">
                    <h3 class="cart-item__name">{{ item.productName }}</h3>
                    <p class="cart-item__price">{{ item.unitPrice | currency: 'USD' : 'symbol' : '1.2-2' }} c/u</p>
                  </div>
                  <div class="cart-item__qty">
                    <button
                      class="btn btn--ghost btn--sm"
                      [disabled]="busy()"
                      (click)="decrease(item.productId, item.quantity)"
                      aria-label="Disminuir cantidad"
                    >−</button>
                    <span class="cart-item__qty-value" data-testid="item-quantity">{{ item.quantity }}</span>
                    <button
                      class="btn btn--ghost btn--sm"
                      [disabled]="busy()"
                      (click)="increase(item.productId, item.quantity)"
                      aria-label="Aumentar cantidad"
                    >+</button>
                  </div>
                  <span class="cart-item__subtotal">{{ item.subtotal | currency: 'USD' : 'symbol' : '1.2-2' }}</span>
                  <button
                    class="btn btn--danger btn--sm"
                    [disabled]="busy()"
                    (click)="remove(item.productId)"
                    data-testid="remove-item"
                  >Eliminar</button>
                </article>
              }
            </div>

            <aside class="cart-summary">
              <h2 class="cart-summary__title">Resumen</h2>
              <div class="cart-summary__row">
                <span>Items</span>
                <span>{{ cart.items.length }}</span>
              </div>
              <div class="cart-summary__row">
                <span>Unidades</span>
                <span>{{ cartService.itemCount() }}</span>
              </div>
              <label for="coupon-code">Cupón de descuento</label>
              <div style="display:flex;gap:.5rem;flex-wrap:wrap">
                <input id="coupon-code" [(ngModel)]="couponCode" maxlength="40" placeholder="Código" [disabled]="busy()" style="min-width:0;flex:1;padding:.5rem" />
                <button class="btn btn--primary" [disabled]="busy() || !couponCode.trim()" (click)="applyCoupon()">Aplicar</button>
              </div>
              @if (cart.couponCode) {
                <div class="cart-summary__row"><span>{{ cart.couponCode }}</span><button class="btn btn--ghost btn--sm" [disabled]="busy()" (click)="removeCoupon()">Quitar</button></div>
              }
              <div class="cart-summary__row"><span>Subtotal</span><span>{{ (cart.subtotal ?? cart.total) | currency:'USD':'symbol':'1.2-2' }}</span></div>
              <div class="cart-summary__row"><span>Descuento</span><span>-{{ (cart.discount ?? 0) | currency:'USD':'symbol':'1.2-2' }}</span></div>
              <div class="cart-summary__row cart-summary__row--total">
                <span>Total</span>
                <span data-testid="cart-total">{{ cart.total | currency: 'USD' : 'symbol' : '1.2-2' }}</span>
              </div>
              <button
                class="btn btn--danger btn--block"
                [disabled]="busy()"
                (click)="cancelCart()"
                data-testid="cancel-cart"
              >
                {{ cancelling() ? 'Cancelando…' : 'Cancelar carrito' }}
              </button>
            </aside>
          </div>
        }
      } @else {
        <app-empty-state
          title="No hay carrito activo"
          message="Creá un carrito para empezar a comprar."
        />
        <div class="cart-page__actions">
          <button class="btn btn--primary" (click)="createNewCart()" data-testid="create-cart">Crear carrito</button>
        </div>
      }
    </section>
  `,
  styles: `
    .cart-page { display: flex; flex-direction: column; gap: 1.5rem; }
    .cart-page__header { display: flex; justify-content: space-between; align-items: flex-end; gap: 1rem; flex-wrap: wrap; }
    .cart-page__title { font-size: 1.75rem; font-weight: 700; color: #fff; margin: 0; }
    .cart-page__subtitle { color: rgba(255, 255, 255, 0.6); margin: 0.25rem 0 0; }
    .cart-page__status {
      font-weight: 600;
      color: #4ade80;
      text-transform: uppercase;
      font-size: 0.8rem;
      letter-spacing: 0.04em;
    }
    .cart-page__layout { display: grid; grid-template-columns: 1fr 320px; gap: 1.5rem; align-items: start; }
    .cart-page__items { display: flex; flex-direction: column; gap: 0.75rem; }
    .cart-page__actions { display: flex; gap: 0.75rem; }
    .cart-item {
      display: flex;
      align-items: center;
      gap: 1rem;
      background: rgba(255, 255, 255, 0.04);
      border: 1px solid rgba(255, 255, 255, 0.08);
      border-radius: 0.75rem;
      padding: 0.875rem 1rem;
    }
    .cart-item__visual {
      width: 2.75rem;
      height: 2.75rem;
      border-radius: 0.5rem;
      display: grid;
      place-items: center;
      background: radial-gradient(circle at 30% 20%, rgba(56, 189, 248, 0.2), transparent 60%),
        rgba(255, 255, 255, 0.03);
      color: rgba(56, 189, 248, 0.6);
      font-size: 1.25rem;
      flex-shrink: 0;
    }
    .cart-item__info { flex: 1; min-width: 0; }
    .cart-item__name { font-size: 0.95rem; font-weight: 600; color: #fff; margin: 0; }
    .cart-item__price { font-size: 0.8rem; color: rgba(255, 255, 255, 0.55); margin: 0.15rem 0 0; }
    .cart-item__qty { display: flex; align-items: center; gap: 0.5rem; }
    .cart-item__qty-value { min-width: 1.5rem; text-align: center; font-weight: 600; color: #fff; }
    .cart-item__subtotal { font-weight: 700; color: #38bdf8; min-width: 5.5rem; text-align: right; }
    .cart-summary {
      background: rgba(255, 255, 255, 0.04);
      border: 1px solid rgba(255, 255, 255, 0.08);
      border-radius: 0.75rem;
      padding: 1.25rem;
      display: flex;
      flex-direction: column;
      gap: 0.75rem;
      position: sticky;
      top: 5rem;
    }
    .cart-summary__title { font-size: 1.05rem; font-weight: 600; color: #fff; margin: 0 0 0.25rem; }
    .cart-summary__row { display: flex; justify-content: space-between; font-size: 0.9rem; color: rgba(255, 255, 255, 0.7); }
    .cart-summary__row--total {
      font-size: 1.1rem;
      font-weight: 700;
      color: #fff;
      border-top: 1px solid rgba(255, 255, 255, 0.1);
      padding-top: 0.75rem;
      margin-top: 0.25rem;
    }
    @media (max-width: 900px) {
      .cart-page__layout { grid-template-columns: 1fr; }
      .cart-summary { position: static; }
    }
    @media (max-width: 640px) {
      .cart-item { flex-wrap: wrap; }
      .cart-item__subtotal { min-width: auto; }
    }
  `,
})
export class CartPageComponent implements OnInit {
  readonly cartService = inject(CartService);
  private readonly toastService = inject(ToastService);

  couponCode = '';
  applyCoupon(): void {
    this.busy.set(true);
    this.cartService.applyCoupon(this.couponCode).subscribe({
      next: () => { this.busy.set(false); this.toastService.success('Cupón aplicado'); },
      error: err => { this.busy.set(false); this.toastService.error(toUserMessage(err)); },
    });
  }
  removeCoupon(): void {
    this.busy.set(true);
    this.cartService.removeCoupon().subscribe({
      next: () => { this.busy.set(false); this.couponCode = ''; },
      error: err => { this.busy.set(false); this.toastService.error(toUserMessage(err)); },
    });
  }
  readonly busy = signal(false);
  readonly cancelling = signal(false);

  ngOnInit(): void {
    this.cartService.restore();
  }

  createNewCart(): void {
    this.busy.set(true);
    this.cartService.create().subscribe({
      next: () => {
        this.busy.set(false);
        this.toastService.success('Carrito creado');
      },
      error: (err) => {
        this.busy.set(false);
        this.toastService.error(toUserMessage(err));
      },
    });
  }

  increase(productId: number, current: number): void {
    this.changeQuantity(productId, current + 1);
  }

  decrease(productId: number, current: number): void {
    if (current <= 1) {
      return;
    }
    this.changeQuantity(productId, current - 1);
  }

  private changeQuantity(productId: number, quantity: number): void {
    this.busy.set(true);
    this.cartService.updateQuantity(productId, quantity).subscribe({
      next: () => this.busy.set(false),
      error: (err) => {
        this.busy.set(false);
        this.toastService.error(toUserMessage(err));
      },
    });
  }

  remove(productId: number): void {
    this.busy.set(true);
    this.cartService.removeItem(productId).subscribe({
      next: () => {
        this.busy.set(false);
        this.toastService.success('Producto eliminado del carrito');
      },
      error: (err) => {
        this.busy.set(false);
        this.toastService.error(toUserMessage(err));
      },
    });
  }

  cancelCart(): void {
    this.cancelling.set(true);
    this.cartService.cancel().subscribe({
      next: () => {
        this.cancelling.set(false);
        this.toastService.success('Carrito cancelado');
      },
      error: (err) => {
        this.cancelling.set(false);
        this.toastService.error(toUserMessage(err));
      },
    });
  }
}
