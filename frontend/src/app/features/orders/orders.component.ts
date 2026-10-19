import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Order } from '../../core/models/order.model';
import { OrderService } from '../../core/services/order.service';
import { PaymentService } from '../../core/services/payment.service';
import { Payment, PaymentStatus } from '../../core/models/payment.model';
import { toUserMessage } from '../../core/utils/error-handler';
import { EmptyStateComponent } from '../../shared/components/empty-state/empty-state.component';
import { LoadingComponent } from '../../shared/components/loading/loading.component';

@Component({
  selector: 'app-orders',
  standalone: true,
  imports: [CurrencyPipe, DatePipe, RouterLink, EmptyStateComponent, LoadingComponent],
  template: `
    <section class="orders">
      <header class="orders__header">
        <div>
          <span class="eyebrow">MI CUENTA</span>
          <h1>Mis pedidos</h1>
          <p>Historial de compras confirmadas.</p>
        </div>
        <a routerLink="/products" class="btn btn--ghost">Seguir comprando</a>
      </header>

      @if (loading()) {
        <app-loading />
      } @else if (error()) {
        <div class="error" role="alert">{{ error() }}</div>
      } @else if (orders().length === 0) {
        <app-empty-state
          title="Todavía no hay pedidos"
          message="Cuando completes una compra aparecerá acá."
        />
      } @else {
        <div class="orders__list">
          @for (order of orders(); track order.id) {
            <article class="order-card">
              <div class="order-card__top">
                <div>
                  <span class="order-number">Pedido #{{ order.id }}</span>
                  <small>{{ order.createdAt | date:'medium' }}</small>
                </div>
                <span [class]="'status status--' + order.status.toLowerCase()">
                  {{ statusLabel(order.status) }}
                </span>
              </div>

              <div class="payment-panel">
                <div class="payment-panel__header">
                  <span class="payment-panel__title">Estado del pago</span>

                  @if (payments()[order.id]; as payment) {
                    <span [class]="'payment-badge payment-badge--' + payment.status.toLowerCase()">
                      <span class="payment-badge__dot"></span>
                      {{ paymentLabel(payment.status) }}
                    </span>
                  } @else {
                    <span class="payment-badge">Consultando...</span>
                  }
                </div>

                @if (payments()[order.id]?.status === 'PENDING') {
                  <div class="payment-actions">
                    <button
                      type="button"
                      class="btn btn--primary btn--sm"
                      [disabled]="processing()[order.id] === true"
                      (click)="simulate(order.id, 'APPROVED')"
                    >
                      {{ processing()[order.id] ? 'Procesando...' : '✓ Simular aprobación' }}
                    </button>

                    <button
                      type="button"
                      class="btn btn--danger btn--sm"
                      [disabled]="processing()[order.id] === true"
                      (click)="simulate(order.id, 'REJECTED')"
                    >
                      ✕ Simular rechazo
                    </button>
                  </div>
                }

                @if (paymentErrors()[order.id]; as paymentError) {
                  <div class="payment-error" role="alert">{{ paymentError }}</div>
                }
              </div>

              <div class="items">
                @for (item of order.items; track item.productId) {
                  <div class="item">
                    <div>
                      <strong>{{ item.productName }}</strong>
                      <small>
                        {{ item.quantity }} ×
                        {{ item.unitPrice | currency:'USD':'symbol':'1.2-2' }}
                      </small>
                    </div>
                    <b>{{ item.subtotal | currency:'USD':'symbol':'1.2-2' }}</b>
                  </div>
                }
              </div>

              <div class="totals">
                <span>
                  Subtotal
                  <b>{{ order.subtotal | currency:'USD':'symbol':'1.2-2' }}</b>
                </span>

                @if (order.discount > 0) {
                  <span>
                    Descuento
                    @if (order.promotionCode) {
                      ({{ order.promotionCode }})
                    }
                    <b>-{{ order.discount | currency:'USD':'symbol':'1.2-2' }}</b>
                  </span>
                }

                <span class="total">
                  Total
                  <b>{{ order.total | currency:'USD':'symbol':'1.2-2' }}</b>
                </span>
              </div>
            </article>
          }
        </div>
      }
    </section>
  `,
  styles: `
    .orders {
      display: flex;
      flex-direction: column;
      gap: 1.5rem;
    }

    .orders__header {
      display: flex;
      justify-content: space-between;
      align-items: end;
      gap: 1rem;
      flex-wrap: wrap;
    }

    .eyebrow {
      font-size: .72rem;
      font-weight: 800;
      letter-spacing: .12em;
      color: var(--primary);
    }

    h1 {
      margin: .2rem 0;
      color: var(--text);
      font-size: 2rem;
    }

    p {
      margin: 0;
      color: #64748b;
    }

    .orders__list {
      display: grid;
      gap: 1rem;
    }

    .order-card {
      background: #fff;
      border: 1px solid var(--border);
      border-radius: 18px;
      padding: 1.25rem;
      box-shadow: 0 7px 24px rgba(15, 23, 42, .05);
    }

    .order-card__top {
      display: flex;
      justify-content: space-between;
      gap: 1rem;
      border-bottom: 1px solid #edf0f5;
      padding-bottom: .9rem;
    }

    .order-card__top > div {
      display: grid;
      gap: .2rem;
    }

    .order-number {
      font-weight: 800;
      color: var(--text);
    }

    .order-card small {
      color: #64748b;
    }

    .status {
      align-self: start;
      background: #ecfdf5;
      color: #047857;
      border-radius: 999px;
      padding: .3rem .6rem;
      font-size: .7rem;
      font-weight: 800;
    }

    .payment-panel {
      margin-top: 1rem;
      padding: 1rem;
      background: #f8faff;
      border: 1px solid #e5e9f2;
      border-radius: 14px;
    }

    .payment-panel__header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: .75rem;
      flex-wrap: wrap;
    }

    .payment-panel__title {
      font-size: .85rem;
      font-weight: 700;
      color: #475569;
    }

    .payment-badge {
      display: inline-flex;
      align-items: center;
      gap: .45rem;
      padding: .4rem .75rem;
      border-radius: 999px;
      background: #e2e8f0;
      color: #475569;
      font-size: .75rem;
      font-weight: 800;
    }

    .payment-badge__dot {
      width: 7px;
      height: 7px;
      border-radius: 50%;
      background: currentColor;
    }

    .payment-badge--pending {
      background: #fef3c7;
      color: #92400e;
    }

    .payment-badge--approved {
      background: #dcfce7;
      color: #166534;
    }

    .payment-badge--rejected {
      background: #ffe4e6;
      color: #be123c;
    }

    .payment-actions {
      display: flex;
      flex-wrap: wrap;
      gap: .65rem;
      margin-top: 1rem;
    }

    .payment-actions .btn {
      min-height: 38px;
    }

    .payment-error {
      margin-top: .75rem;
      padding: .75rem;
      border-radius: 10px;
      background: #fff1f2;
      color: #be123c;
      font-size: .85rem;
    }

    .items {
      display: grid;
      gap: .65rem;
      padding: 1rem 0;
    }

    .item {
      display: flex;
      justify-content: space-between;
      gap: 1rem;
    }

    .item > div {
      display: grid;
      gap: .15rem;
    }

    .item strong,
    .item b {
      color: #334155;
    }

    .totals {
      border-top: 1px solid #edf0f5;
      padding-top: .8rem;
      display: grid;
      gap: .35rem;
      justify-content: end;
      min-width: 260px;
      margin-left: auto;
    }

    .totals span {
      display: flex;
      justify-content: space-between;
      gap: 2rem;
      color: #64748b;
    }

    .totals b {
      color: var(--text);
    }

    .totals .total {
      font-size: 1.05rem;
      font-weight: 800;
      color: var(--text);
    }

    .error {
      padding: 1rem;
      border-radius: 12px;
      background: #fef2f2;
      color: #b91c1c;
    }

    @media (max-width: 480px) {
      .order-card {
        padding: 1rem;
      }

      .payment-actions {
        flex-direction: column;
      }

      .payment-actions .btn {
        width: 100%;
      }

      .totals {
        min-width: 0;
        width: 100%;
      }
    }
  `,
})
export class OrdersComponent implements OnInit {
  private readonly orderService = inject(OrderService);
  private readonly paymentService = inject(PaymentService);

  readonly orders = signal<Order[]>([]);
  readonly payments = signal<Record<number, Payment>>({});
  readonly processing = signal<Record<number, boolean>>({});
  readonly paymentErrors = signal<Record<number, string>>({});
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  statusLabel(status: Order['status']): string {
    return ({
      CONFIRMED: 'Confirmado',
      PROCESSING: 'En preparación',
      SHIPPED: 'Enviado',
      DELIVERED: 'Entregado',
      CANCELLED: 'Cancelado',
    })[status];
  }

  paymentLabel(status: PaymentStatus): string {
    return ({
      PENDING: 'Pendiente',
      APPROVED: 'Aprobado',
      REJECTED: 'Rechazado',
    })[status];
  }

  simulate(id: number, outcome: Exclude<PaymentStatus, 'PENDING'>): void {
    if (this.processing()[id] || this.payments()[id]?.status !== 'PENDING') {
      return;
    }

    this.processing.update(current => ({ ...current, [id]: true }));
    this.paymentErrors.update(current => ({ ...current, [id]: '' }));

    this.paymentService.simulate(id, outcome).subscribe({
      next: () => {
        this.refreshPayment(id);
        this.processing.update(current => ({ ...current, [id]: false }));
      },
      error: err => {
        this.paymentErrors.update(current => ({
          ...current,
          [id]: toUserMessage(err),
        }));
        this.processing.update(current => ({ ...current, [id]: false }));
      },
    });
  }

  refreshPayment(id: number): void {
    this.paymentService.get(id).subscribe({
      next: payment => {
        this.payments.update(current => ({ ...current, [id]: payment }));
      },
      error: () => {
        this.paymentErrors.update(current => ({
          ...current,
          [id]: 'No se pudo consultar el estado del pago.',
        }));
      },
    });
  }

  ngOnInit(): void {
    this.orderService.list().subscribe({
      next: orders => {
        this.orders.set(orders);
        orders.forEach(order => this.refreshPayment(order.id));
        this.loading.set(false);
      },
      error: err => {
        this.error.set(toUserMessage(err));
        this.loading.set(false);
      },
    });
  }
}