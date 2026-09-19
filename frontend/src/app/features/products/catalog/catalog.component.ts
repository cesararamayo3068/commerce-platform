import { Component, inject, OnInit, signal } from '@angular/core';
import { ProductService } from '../../../core/services/product.service';
import { CartService } from '../../../core/services/cart.service';
import { ToastService } from '../../../shared/components/toast/toast.service';
import { Product } from '../../../core/models/product.model';
import { PagedModel } from '../../../core/models/paged.model';
import { toUserMessage } from '../../../core/utils/error-handler';
import { ProductCardComponent } from '../../../shared/components/product-card/product-card.component';
import { LoadingComponent } from '../../../shared/components/loading/loading.component';
import { EmptyStateComponent } from '../../../shared/components/empty-state/empty-state.component';

@Component({
  selector: 'app-catalog',
  standalone: true,
  imports: [ProductCardComponent, LoadingComponent, EmptyStateComponent],
  template: `
    <section class="catalog">
      <div class="hero">
        <div class="hero__content">
          <span class="hero__eyebrow">NUEVA COLECCIÓN</span>
          <h1>Encontrá lo que necesitás.<br><em>Comprá simple.</em></h1>
          <p>Productos seleccionados, una experiencia clara y todo tu carrito en un solo lugar.</p>
          <a class="hero__cta" href="#catalog-grid">Explorar productos <span>→</span></a>
        </div>
        <div class="hero__art" aria-hidden="true"><div class="orb orb--one"></div><div class="orb orb--two"></div><div class="hero__bag">C</div></div>
      </div>
      <div class="catalog__header">
        <div>
          <div class="catalog__eyebrow">CATÁLOGO</div><h2 class="catalog__title">Productos para vos</h2>
          <p class="catalog__subtitle">Elegí tus favoritos y agregalos al carrito en un clic.</p>
        </div>
        <div class="catalog__pager">
          <button class="btn btn--ghost btn--sm" [disabled]="page() === 0 || loading()" (click)="goToPage(page() - 1)">
            ← Anterior
          </button>
          <span class="catalog__page-info" data-testid="page-info">
            Página {{ page() + 1 }} de {{ totalPages() }}
          </span>
          <button
            class="btn btn--ghost btn--sm"
            [disabled]="page() >= totalPages() - 1 || loading()"
            (click)="goToPage(page() + 1)"
          >
            Siguiente →
          </button>
        </div>
      </div>

      @if (loading()) {
        <app-loading />
      } @else if (error(); as err) {
        <div class="banner banner--error" role="alert">
          <span>{{ err }}</span>
          <button class="btn btn--ghost btn--sm" (click)="load()">Reintentar</button>
        </div>
      } @else if (products().length === 0) {
        <app-empty-state title="No hay productos activos" message="Creá un producto desde Administrar para verlo acá." />
      } @else {
        <div class="catalog__grid" id="catalog-grid">
          @for (product of products(); track product.id) {
            <app-product-card
              [product]="product"
              [adding]="addingProductId() === product.id"
              (add)="addToCart($event)"
            />
          }
        </div>
      }
    </section>
  `,
  styles: `
    .catalog{display:flex;flex-direction:column;gap:2rem}.hero{min-height:250px;border-radius:28px;background:linear-gradient(120deg,#172554 0%,#253ea5 52%,#0ea5e9 130%);color:#fff;overflow:hidden;display:grid;grid-template-columns:1.2fr .8fr;box-shadow:0 18px 45px rgba(30,58,138,.18)}.hero__content{padding:2.35rem 2.75rem;position:relative;z-index:2}.hero__eyebrow{font-size:.72rem;font-weight:800;letter-spacing:.16em;color:#bfdbfe}.hero h1{font-size:2.35rem;line-height:1.05;letter-spacing:-.04em;margin:.7rem 0 1rem}.hero h1 em{font-style:normal;color:#7dd3fc}.hero p{max-width:540px;color:#dbeafe;line-height:1.6;margin:0 0 1.5rem}.hero__cta{display:inline-flex;gap:.6rem;align-items:center;background:#fff;color:#1e3a8a;padding:.78rem 1rem;border-radius:12px;text-decoration:none;font-weight:800;font-size:.9rem}.hero__art{position:relative;display:grid;place-items:center}.hero__bag{position:relative;z-index:2;width:130px;height:130px;border-radius:32px;display:grid;place-items:center;background:rgba(255,255,255,.14);border:1px solid rgba(255,255,255,.3);backdrop-filter:blur(12px);font-size:3.2rem;font-weight:800;box-shadow:0 20px 50px rgba(0,0,0,.18);transform:rotate(8deg)}.orb{position:absolute;border-radius:50%}.orb--one{width:220px;height:220px;background:rgba(125,211,252,.18)}.orb--two{width:120px;height:120px;background:rgba(255,255,255,.13);right:12%;top:10%}.catalog__header{display:flex;justify-content:space-between;align-items:end;gap:1rem;flex-wrap:wrap}.catalog__eyebrow{font-size:.68rem;letter-spacing:.14em;color:#3157d5;font-weight:800;margin-bottom:.35rem}.catalog__title{font-size:1.7rem;color:#172033;margin:0}.catalog__subtitle{color:#6b7280;margin:.35rem 0 0}.catalog__pager{display:flex;align-items:center;gap:.75rem}.catalog__page-info{font-size:.82rem;color:#64748b}.catalog__grid{display:grid;grid-template-columns:repeat(auto-fill,minmax(230px,1fr));gap:1.4rem}.banner{display:flex;align-items:center;justify-content:space-between;gap:1rem;padding:1rem 1.25rem}@media(max-width:760px){.hero{grid-template-columns:1fr;min-height:auto}.hero__content{padding:2.2rem 1.5rem}.hero h1{font-size:2.15rem}.hero__art{display:none}.catalog__pager{width:100%;justify-content:space-between}.catalog__grid{grid-template-columns:1fr}}
  `,
})
export class CatalogComponent implements OnInit {
  private readonly productService = inject(ProductService);
  private readonly cartService = inject(CartService);
  private readonly toastService = inject(ToastService);

  readonly products = signal<Product[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly page = signal(0);
  readonly totalPages = signal(0);
  readonly addingProductId = signal<number | null>(null);

  private readonly pageSize = 12;

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.productService.list(true, this.page(), this.pageSize, 'name,asc').subscribe({
      next: (paged: PagedModel<Product>) => {
        this.products.set(paged._embedded?.productResponseList ?? []);
        this.totalPages.set(paged.page.totalPages);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(toUserMessage(err));
        this.loading.set(false);
      },
    });
  }

  goToPage(target: number): void {
    if (target < 0 || target >= this.totalPages()) {
      return;
    }
    this.page.set(target);
    this.load();
  }

  addToCart(product: Product): void {
    this.addingProductId.set(product.id);
    this.cartService.addItem(product.id, 1).subscribe({
      next: () => {
        this.addingProductId.set(null);
        this.toastService.success(`"${product.name}" agregado al carrito`);
      },
      error: (err) => {
        this.addingProductId.set(null);
        this.toastService.error(toUserMessage(err));
      },
    });
  }
}
