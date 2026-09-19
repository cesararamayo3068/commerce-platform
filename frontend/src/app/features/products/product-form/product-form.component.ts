import { Component, inject, input, OnInit, output, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Product } from '../../../core/models/product.model';

/**
 * Product create/edit form.
 *
 * Validations mirror the backend DTOs exactly:
 *  - name: required, max 150 chars
 *  - price: required, >= 0
 *  - description: optional
 *  - active: only present when editing (ProductUpdateRequest)
 */
@Component({
  selector: 'app-product-form',
  standalone: true,
  imports: [ReactiveFormsModule],
  template: `
    <form [formGroup]="form" (ngSubmit)="onSubmit()" class="product-form" novalidate>
      <div class="field">
        <label class="field__label" for="name">Nombre *</label>
        <input
          id="name"
          type="text"
          formControlName="name"
          class="field__input"
          placeholder="Ej: Teclado mecánico"
          data-testid="product-name"
        />
        @if (form.controls.name.touched && form.controls.name.errors) {
          <p class="field__error" data-testid="name-error">
            @if (form.controls.name.errors['required']) { El nombre es obligatorio. }
            @if (form.controls.name.errors['maxlength']) { El nombre debe tener como máximo 150 caracteres. }
          </p>
        }
      </div>

      <div class="field">
        <label class="field__label" for="description">Descripción</label>
        <textarea
          id="description"
          formControlName="description"
          class="field__input field__input--area"
          rows="3"
          placeholder="Descripción opcional del producto"
        ></textarea>
      </div>


      <div class="field-row">
        <div class="field">
          <label class="field__label" for="brand">Marca</label>
          <input id="brand" type="text" formControlName="brand" class="field__input" placeholder="Ej: NovaGear" />
        </div>
        <div class="field">
          <label class="field__label" for="category">Categoría</label>
          <input id="category" type="text" formControlName="category" class="field__input" placeholder="Ej: Periféricos" />
        </div>
      </div>

      <div class="field">
        <label class="field__label" for="imageUrl">URL de imagen</label>
        <input id="imageUrl" type="text" formControlName="imageUrl" class="field__input" placeholder="/assets/products/producto.svg" />
      </div>

      <div class="field">
        <label class="field__label" for="price">Precio (USD) *</label>
        <input
          id="price"
          type="number"
          step="0.01"
          min="0"
          formControlName="price"
          class="field__input"
          placeholder="0.00"
          data-testid="product-price"
        />
        @if (form.controls.price.touched && form.controls.price.errors) {
          <p class="field__error" data-testid="price-error">
            @if (form.controls.price.errors['required']) { El precio es obligatorio. }
            @if (form.controls.price.errors['min']) { El precio debe ser mayor o igual a 0. }
          </p>
        }
      </div>

      @if (editing()) {
        <div class="field field--inline">
          <label class="field__label" for="active">Activo</label>
          <input id="active" type="checkbox" formControlName="active" class="field__checkbox" />
        </div>
      }

      @if (serverError(); as err) {
        <div class="banner banner--error" role="alert" data-testid="server-error">{{ err }}</div>
      }

      <div class="product-form__actions">
        <button type="button" class="btn btn--ghost" (click)="cancel.emit()">Cancelar</button>
        <button
          type="submit"
          class="btn btn--primary"
          [disabled]="form.invalid || submitting()"
          data-testid="product-submit"
        >
          @if (submitting()) {
            <span class="btn__spinner" aria-hidden="true"></span>
            <span>Guardando…</span>
          } @else {
            <span>{{ editing() ? 'Guardar cambios' : 'Crear producto' }}</span>
          }
        </button>
      </div>
    </form>
  `,
  styles: `
    .product-form { display: flex; flex-direction: column; gap: 1.25rem; }
    .field { display: flex; flex-direction: column; gap: 0.375rem; flex:1; }
    .field-row { display:grid; grid-template-columns:1fr 1fr; gap:1rem; }
    .field--inline { flex-direction: row; align-items: center; gap: 0.75rem; }
    .field__label { font-size: 0.875rem; font-weight: 600; color: rgba(255, 255, 255, 0.85); }
    .field__input {
      background: rgba(255, 255, 255, 0.05);
      border: 1px solid rgba(255, 255, 255, 0.14);
      border-radius: 0.5rem;
      color: #fff;
      padding: 0.625rem 0.75rem;
      font-size: 0.9rem;
      transition: border-color 0.15s, box-shadow 0.15s;
    }
    .field__input:focus {
      outline: none;
      border-color: #38bdf8;
      box-shadow: 0 0 0 3px rgba(56, 189, 248, 0.2);
    }
    .field__input--area { resize: vertical; font-family: inherit; }
    .field__checkbox { width: 1.1rem; height: 1.1rem; accent-color: #38bdf8; }
    .field__error { color: #fca5a5; font-size: 0.8rem; margin: 0; }
    .banner--error {
      background: rgba(179, 38, 30, 0.15);
      border: 1px solid rgba(179, 38, 30, 0.4);
      color: #fca5a5;
      padding: 0.75rem 1rem;
      border-radius: 0.5rem;
      font-size: 0.875rem;
    }
    .product-form__actions { display: flex; justify-content: flex-end; gap: 0.75rem; }
    .btn__spinner {
      width: 0.85rem;
      height: 0.85rem;
      border-radius: 50%;
      border: 2px solid rgba(255, 255, 255, 0.35);
      border-top-color: #fff;
      animation: spin 0.7s linear infinite;
      display: inline-block;
    }
    @keyframes spin { to { transform: rotate(360deg); } }
  `,
})
export class ProductFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);

  readonly editing = input(false);
  readonly product = input<Product | null>(null);
  readonly submitting = signal(false);
  readonly serverError = signal<string | null>(null);
  readonly submit = output<{ name: string; description: string | null; brand: string | null; category: string | null; imageUrl: string | null; price: number; active: boolean | null }>();
  readonly cancel = output<void>();

  readonly form = this.fb.group({
    name: ['', [Validators.required, Validators.maxLength(150)]],
    description: [''],
    brand: ['', [Validators.maxLength(100)]],
    category: ['', [Validators.maxLength(100)]],
    imageUrl: ['', [Validators.maxLength(500)]],
    price: [null as number | null, [Validators.required, Validators.min(0)]],
    active: [true],
  });

  ngOnInit(): void {
    const product = this.product();
    if (product !== null) {
      this.form.patchValue({
        name: product.name,
        description: product.description ?? '',
        brand: product.brand ?? '',
        category: product.category ?? '',
        imageUrl: product.imageUrl ?? '',
        price: product.price,
        active: product.active,
      });
    }
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const raw = this.form.getRawValue();
    const description = raw.description?.trim() || null;
    this.submit.emit({
      name: (raw.name ?? '').trim(),
      description,
      brand: raw.brand?.trim() || null,
      category: raw.category?.trim() || null,
      imageUrl: raw.imageUrl?.trim() || null,
      price: raw.price as number,
      active: this.editing() ? (raw.active ?? true) : null,
    });
  }

  setSubmitting(value: boolean): void {
    this.submitting.set(value);
  }

  setServerError(message: string | null): void {
    this.serverError.set(message);
  }
}
