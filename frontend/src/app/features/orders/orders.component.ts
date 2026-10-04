import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Order } from '../../core/models/order.model';
import { OrderService } from '../../core/services/order.service';
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
        <div><span class="eyebrow">MI CUENTA</span><h1>Mis pedidos</h1><p>Historial de compras confirmadas.</p></div>
        <a routerLink="/products" class="btn btn--ghost">Seguir comprando</a>
      </header>
      @if (loading()) { <app-loading /> }
      @else if (error()) { <div class="error">{{ error() }}</div> }
      @else if (orders().length === 0) { <app-empty-state title="Todavía no hay pedidos" message="Cuando completes una compra aparecerá acá." /> }
      @else {
        <div class="orders__list">
          @for (order of orders(); track order.id) {
            <article class="order-card">
              <div class="order-card__top">
                <div><span class="order-number">Pedido #{{ order.id }}</span><small>{{ order.createdAt | date:'medium' }}</small></div>
                <span class="status">{{ order.status }}</span>
              </div>
              <div class="items">
                @for (item of order.items; track item.productId) {
                  <div class="item"><div><strong>{{ item.productName }}</strong><small>{{ item.quantity }} × {{ item.unitPrice | currency:'USD':'symbol':'1.2-2' }}</small></div><b>{{ item.subtotal | currency:'USD':'symbol':'1.2-2' }}</b></div>
                }
              </div>
              <div class="totals">
                <span>Subtotal <b>{{ order.subtotal | currency:'USD':'symbol':'1.2-2' }}</b></span>
                @if (order.discount > 0) { <span>Descuento @if(order.promotionCode){({{ order.promotionCode }})} <b>-{{ order.discount | currency:'USD':'symbol':'1.2-2' }}</b></span> }
                <span class="total">Total <b>{{ order.total | currency:'USD':'symbol':'1.2-2' }}</b></span>
              </div>
            </article>
          }
        </div>
      }
    </section>
  `,
  styles: `
    .orders{display:flex;flex-direction:column;gap:1.5rem}.orders__header{display:flex;justify-content:space-between;align-items:end;gap:1rem;flex-wrap:wrap}.eyebrow{font-size:.72rem;font-weight:800;letter-spacing:.12em;color:#3157d5}h1{margin:.2rem 0;color:#172033;font-size:2rem}p{margin:0;color:#64748b}.orders__list{display:grid;gap:1rem}.order-card{background:#fff;border:1px solid #e6eaf2;border-radius:18px;padding:1.25rem;box-shadow:0 7px 24px rgba(15,23,42,.05)}.order-card__top{display:flex;justify-content:space-between;gap:1rem;border-bottom:1px solid #edf0f5;padding-bottom:.9rem}.order-card__top>div{display:grid;gap:.2rem}.order-number{font-weight:800;color:#172033}.order-card small{color:#64748b}.status{align-self:start;background:#ecfdf5;color:#047857;border-radius:999px;padding:.3rem .6rem;font-size:.7rem;font-weight:800}.items{display:grid;gap:.65rem;padding:1rem 0}.item{display:flex;justify-content:space-between;gap:1rem}.item>div{display:grid;gap:.15rem}.item strong,.item b{color:#334155}.totals{border-top:1px solid #edf0f5;padding-top:.8rem;display:grid;gap:.35rem;justify-content:end;min-width:260px;margin-left:auto}.totals span{display:flex;justify-content:space-between;gap:2rem;color:#64748b}.totals b{color:#172033}.totals .total{font-size:1.05rem;font-weight:800;color:#172033}.error{padding:1rem;border-radius:12px;background:#fef2f2;color:#b91c1c}
  `,
})
export class OrdersComponent implements OnInit {
  private readonly orderService = inject(OrderService);
  readonly orders = signal<Order[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.orderService.list().subscribe({
      next: orders => { this.orders.set(orders); this.loading.set(false); },
      error: err => { this.error.set(toUserMessage(err)); this.loading.set(false); },
    });
  }
}
