import { Component, input, output } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { Product } from '../../../core/models/product.model';

@Component({
  selector: 'app-product-card', standalone: true, imports: [CurrencyPipe],
  template: `
    <article class="card" [class.inactive]="!product().active">
      <div class="visual">
        @if (product().imageUrl) {
          <img [src]="product().imageUrl!" [alt]="product().name" loading="lazy" />
        } @else {
          <div class="fallback" aria-hidden="true">{{ product().name.charAt(0) }}</div>
        }
        @if (product().category) { <span class="category">{{ product().category }}</span> }
        @if (!product().active) { <span class="status">Inactivo</span> }
      </div>
      <div class="body">
        <div class="brand">{{ product().brand || 'Commerce Select' }}</div>
        <h3>{{ product().name }}</h3>
        <p>{{ product().description || 'Producto seleccionado para nuestro catálogo.' }}</p>
        <div class="bottom">
          <div class="price"><small>Precio</small><strong>{{ product().price | currency:'USD':'symbol':'1.2-2' }}</strong></div>
          <button class="btn btn--primary" [disabled]="!product().active || adding()" (click)="add.emit(product())" data-testid="add-to-cart">
            {{ adding() ? 'Agregando…' : 'Agregar' }}
          </button>
        </div>
      </div>
    </article>
  `,
  styles: `
    .card{height:100%;background:#fff;border:1px solid #e6eaf2;border-radius:20px;overflow:hidden;box-shadow:0 7px 24px rgba(15,23,42,.06);transition:transform .2s ease,box-shadow .2s ease;display:flex;flex-direction:column}.card:hover{transform:translateY(-4px);box-shadow:0 16px 36px rgba(15,23,42,.11)}.inactive{opacity:.65}.visual{height:210px;position:relative;display:grid;place-items:center;background:#f7f9fc;overflow:hidden}.visual img{width:100%;height:100%;object-fit:contain;padding:1.2rem;box-sizing:border-box;transition:transform .25s ease}.card:hover .visual img{transform:scale(1.035)}.fallback{width:86px;height:86px;border-radius:24px;display:grid;place-items:center;background:#e8efff;color:#3157d5;font-size:2rem;font-weight:800}.category,.status{position:absolute;top:12px;padding:.35rem .6rem;border-radius:999px;font-size:.65rem;font-weight:800}.category{left:12px;background:rgba(255,255,255,.92);color:#475569;border:1px solid #e2e8f0}.status{right:12px;background:#fff7ed;color:#c2410c}.body{padding:1.15rem;display:flex;flex-direction:column;flex:1}.brand{text-transform:uppercase;letter-spacing:.1em;color:#3157d5;font-size:.62rem;font-weight:800;margin-bottom:.4rem}h3{margin:0;color:#111827;font-size:1.08rem}p{margin:.5rem 0 1.1rem;color:#64748b;font-size:.82rem;line-height:1.5;min-height:3.7rem}.bottom{margin-top:auto;border-top:1px solid #edf0f5;padding-top:.9rem;display:flex;align-items:end;justify-content:space-between;gap:.75rem}.price{display:grid;gap:.1rem}.price small{color:#94a3b8;font-size:.66rem}.price strong{font-size:1.15rem;color:#111827}.bottom .btn{padding:.62rem .85rem}
  `
})
export class ProductCardComponent { readonly product=input.required<Product>(); readonly adding=input(false); readonly add=output<Product>(); }
