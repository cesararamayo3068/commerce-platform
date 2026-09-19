import { Component, inject, OnInit, signal } from '@angular/core';
import { ProductService } from '../../../core/services/product.service';
import { ToastService } from '../../../shared/components/toast/toast.service';
import { Product, ProductCreateRequest, ProductUpdateRequest } from '../../../core/models/product.model';
import { PagedModel } from '../../../core/models/paged.model';
import { toUserMessage } from '../../../core/utils/error-handler';
import { ProductFormComponent } from '../product-form/product-form.component';
import { LoadingComponent } from '../../../shared/components/loading/loading.component';
import { EmptyStateComponent } from '../../../shared/components/empty-state/empty-state.component';
import { CurrencyPipe, DatePipe } from '@angular/common';

type Mode = 'list' | 'create' | 'edit';

@Component({
  selector: 'app-manage',
  standalone: true,
  imports: [ProductFormComponent, LoadingComponent, EmptyStateComponent, CurrencyPipe, DatePipe],
  template: `
    <section class="manage">
      <div class="manage__header">
        <div>
          <h1 class="manage__title">Administración de productos</h1>
          <p class="manage__subtitle">Creá, editá y desactivá productos del catálogo.</p>
        </div>
        @if (mode() === 'list') {
          <button class="btn btn--primary" (click)="startCreate()" data-testid="new-product">+ Nuevo producto</button>
        }
      </div>

      @if (mode() === 'create' || mode() === 'edit') {
        <div class="manage__panel">
          <h2 class="manage__panel-title">{{ mode() === 'create' ? 'Nuevo producto' : 'Editar producto' }}</h2>
          <app-product-form
            [editing]="mode() === 'edit'"
            [product]="editingProduct()"
            (submit)="onSubmit($event)"
            (cancel)="backToList()"
          />
        </div>
      } @else {
        @if (loading()) {
          <app-loading />
        } @else if (error(); as err) {
          <div class="banner banner--error" role="alert">
            <span>{{ err }}</span>
            <button class="btn btn--ghost btn--sm" (click)="load()">Reintentar</button>
          </div>
        } @else if (products().length === 0) {
          <app-empty-state title="No hay productos" message="Creá el primer producto con el botón Nuevo producto." />
        } @else {
          <div class="manage__table-wrap">
            <table class="manage__table">
              <thead>
                <tr>
                  <th>Nombre</th>
                  <th>Descripción</th>
                  <th>Precio</th>
                  <th>Estado</th>
                  <th>Actualizado</th>
                  <th class="manage__th-actions">Acciones</th>
                </tr>
              </thead>
              <tbody>
                @for (product of products(); track product.id) {
                  <tr [class.manage__row--inactive]="!product.active">
                    <td class="manage__cell-name">{{ product.name }}</td>
                    <td class="manage__cell-desc">{{ product.description || '—' }}</td>
                    <td>{{ product.price | currency: 'USD' : 'symbol' : '1.2-2' }}</td>
                    <td>
                      <span class="manage__status" [class.manage__status--inactive]="!product.active">
                        {{ product.active ? 'Activo' : 'Inactivo' }}
                      </span>
                    </td>
                    <td>{{ product.updatedAt | date: 'dd/MM/yyyy HH:mm' }}</td>
                    <td class="manage__cell-actions">
                      <button class="btn btn--ghost btn--sm" (click)="startEdit(product)">Editar</button>
                      @if (product.active) {
                        <button
                          class="btn btn--danger btn--sm"
                          [disabled]="deactivatingId() === product.id"
                          (click)="deactivate(product)"
                          data-testid="deactivate-product"
                        >
                          {{ deactivatingId() === product.id ? 'Desactivando…' : 'Desactivar' }}
                        </button>
                      }
                    </td>
                  </tr>
                }
              </tbody>
            </table>
          </div>
          <div class="manage__pager">
            <button class="btn btn--ghost btn--sm" [disabled]="page() === 0 || loading()" (click)="goToPage(page() - 1)">
              ← Anterior
            </button>
            <span class="manage__page-info">Página {{ page() + 1 }} de {{ totalPages() }}</span>
            <button
              class="btn btn--ghost btn--sm"
              [disabled]="page() >= totalPages() - 1 || loading()"
              (click)="goToPage(page() + 1)"
            >
              Siguiente →
            </button>
          </div>
        }
      }
    </section>
  `,
  styles: `
    .manage { display: flex; flex-direction: column; gap: 1.5rem; }
    .manage__header { display: flex; justify-content: space-between; align-items: flex-end; gap: 1rem; flex-wrap: wrap; }
    .manage__title { font-size: 1.75rem; font-weight: 700; color: #fff; margin: 0; }
    .manage__subtitle { color: rgba(255, 255, 255, 0.6); margin: 0.25rem 0 0; }
    .manage__panel {
      background: rgba(255, 255, 255, 0.04);
      border: 1px solid rgba(255, 255, 255, 0.08);
      border-radius: 0.75rem;
      padding: 1.5rem;
      max-width: 36rem;
    }
    .manage__panel-title { font-size: 1.1rem; font-weight: 600; color: #fff; margin: 0 0 1.25rem; }
    .manage__table-wrap { overflow-x: auto; border: 1px solid rgba(255, 255, 255, 0.08); border-radius: 0.75rem; }
    .manage__table { width: 100%; border-collapse: collapse; font-size: 0.875rem; min-width: 640px; }
    .manage__table th {
      text-align: left;
      padding: 0.75rem 1rem;
      color: rgba(255, 255, 255, 0.55);
      font-weight: 600;
      font-size: 0.75rem;
      text-transform: uppercase;
      letter-spacing: 0.05em;
      border-bottom: 1px solid rgba(255, 255, 255, 0.08);
      background: rgba(255, 255, 255, 0.02);
    }
    .manage__table td { padding: 0.75rem 1rem; border-bottom: 1px solid rgba(255, 255, 255, 0.05); color: rgba(255, 255, 255, 0.85); }
    .manage__table tr:last-child td { border-bottom: none; }
    .manage__row--inactive { opacity: 0.55; }
    .manage__cell-name { font-weight: 600; color: #fff; }
    .manage__cell-desc { max-width: 18rem; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; color: rgba(255, 255, 255, 0.6); }
    .manage__status {
      font-size: 0.7rem;
      font-weight: 600;
      text-transform: uppercase;
      letter-spacing: 0.04em;
      color: #4ade80;
      background: rgba(74, 222, 128, 0.12);
      border: 1px solid rgba(74, 222, 128, 0.3);
      padding: 0.15rem 0.45rem;
      border-radius: 999px;
    }
    .manage__status--inactive { color: #fbbf24; background: rgba(251, 191, 36, 0.12); border-color: rgba(251, 191, 36, 0.3); }
    .manage__cell-actions { display: flex; gap: 0.5rem; }
    .manage__th-actions { text-align: right; }
    .manage__pager { display: flex; align-items: center; gap: 0.75rem; justify-content: flex-end; }
    .manage__page-info { font-size: 0.85rem; color: rgba(255, 255, 255, 0.6); }
    .banner--error {
      background: rgba(179, 38, 30, 0.15);
      border: 1px solid rgba(179, 38, 30, 0.4);
      color: #fca5a5;
      padding: 0.75rem 1rem;
      border-radius: 0.5rem;
      font-size: 0.875rem;
      display: flex;
      justify-content: space-between;
      align-items: center;
      gap: 1rem;
    }
  `,
})
export class ManageComponent implements OnInit {
  private readonly productService = inject(ProductService);
  private readonly toastService = inject(ToastService);

  readonly mode = signal<Mode>('list');
  readonly products = signal<Product[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly page = signal(0);
  readonly totalPages = signal(0);
  readonly editingProduct = signal<Product | null>(null);
  readonly deactivatingId = signal<number | null>(null);

  private readonly pageSize = 10;

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.productService.list(null, this.page(), this.pageSize, 'name,asc').subscribe({
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

  startCreate(): void {
    this.editingProduct.set(null);
    this.mode.set('create');
  }

  startEdit(product: Product): void {
    this.editingProduct.set(product);
    this.mode.set('edit');
  }

  backToList(): void {
    this.mode.set('list');
    this.editingProduct.set(null);
    this.load();
  }

  onSubmit(payload: { name: string; description: string | null; brand: string | null; category: string | null; imageUrl: string | null; price: number; active: boolean | null }): void {
    if (this.mode() === 'create') {
      const request: ProductCreateRequest = { name: payload.name, description: payload.description, brand: payload.brand, category: payload.category, imageUrl: payload.imageUrl, price: payload.price };
      this.productService.create(request).subscribe({
        next: (created) => {
          this.toastService.success(`Producto "${created.name}" creado`);
          this.backToList();
        },
        error: (err) => this.toastService.error(toUserMessage(err)),
      });
    } else {
      const product = this.editingProduct();
      if (product === null) {
        return;
      }
      const request: ProductUpdateRequest = {
        name: payload.name,
        description: payload.description,
        brand: payload.brand,
        category: payload.category,
        imageUrl: payload.imageUrl,
        price: payload.price,
        active: payload.active,
      };
      this.productService.update(product.id, request).subscribe({
        next: (updated) => {
          this.toastService.success(`Producto "${updated.name}" actualizado`);
          this.backToList();
        },
        error: (err) => this.toastService.error(toUserMessage(err)),
      });
    }
  }

  deactivate(product: Product): void {
    this.deactivatingId.set(product.id);
    this.productService.deactivate(product.id).subscribe({
      next: (updated) => {
        this.deactivatingId.set(null);
        this.toastService.success(`Producto "${updated.name}" desactivado`);
        this.load();
      },
      error: (err) => {
        this.deactivatingId.set(null);
        this.toastService.error(toUserMessage(err));
      },
    });
  }
}
