import { inject } from '@angular/core';
import { AuthService } from './auth.service';
import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../config/environment';
import {
  Cart,
  CartCreateRequest,
  CartItemRequest,
  CartItemQuantityUpdateRequest,
} from '../models/cart.model';

const CART_ID_STORAGE_KEY = 'commerce-platform.cartId';

/**
 * Cart state holder.
 *
 * Only the cartId is persisted (localStorage) so a refresh can recover the
 * cart; full cart objects are never stored. The cart is exposed through
 * signals so components react to changes without NgRx.
 */
@Injectable({ providedIn: 'root' })
export class CartService {
  private readonly auth=inject(AuthService);
  private readonly baseUrl = `${environment.apiUrl}/carts`;

  private readonly cartSignal = signal<Cart | null>(null);
  private readonly loadingSignal = signal(false);
  private readonly errorSignal = signal<string | null>(null);

  readonly cart = this.cartSignal.asReadonly();
  readonly loading = this.loadingSignal.asReadonly();
  readonly error = this.errorSignal.asReadonly();
  readonly itemCount = computed(() => this.cartSignal()?.items.reduce((sum, item) => sum + item.quantity, 0) ?? 0);
  readonly total = computed(() => this.cartSignal()?.total ?? 0);

  constructor(private readonly http: HttpClient) {}

  /** Restores the cart from the persisted cartId, if any. */
  restore(): void {
    if (!this.auth.isLoggedIn()) return;
    const cartId = this.readStoredCartId();
    if (cartId === null) {
      return;
    }
    this.loadingSignal.set(true);
    this.http.get<Cart>(`${this.baseUrl}/${cartId}`).subscribe({
      next: (cart) => {
        if (cart.status === 'ACTIVE') {
          this.cartSignal.set(cart);
        } else {
          this.clearStoredCartId();
          this.cartSignal.set(null);
        }
        this.loadingSignal.set(false);
      },
      error: () => {
        // The cart no longer exists (404) or the backend is unreachable:
        // clean the local state so the user can create a new cart.
        this.clearStoredCartId();
        this.cartSignal.set(null);
        this.loadingSignal.set(false);
      },
    });
  }

  /** Creates a new ACTIVE cart for the demo user. */
  create(): Observable<Cart> {
    const request: CartCreateRequest = { userId: this.auth.session()!.userId };
    return this.http.post<Cart>(this.baseUrl, request).pipe(
      tap((cart) => {
        this.persistCartId(cart.id);
        this.cartSignal.set(cart);
      }),
    );
  }

  /** Ensures an ACTIVE cart exists, creating one if needed. */
  ensureCart(): Observable<Cart> {
    const current = this.cartSignal();
    if (current !== null && current.status === 'ACTIVE') {
      return new Observable((subscriber) => {
        subscriber.next(current);
        subscriber.complete();
      });
    }
    return this.create();
  }

  /** Adds a product to the cart, creating the cart first if needed. */
  addItem(productId: number, quantity: number): Observable<Cart> {
    return this.ensureCart().pipe(
      tap((cart) => {
        const request: CartItemRequest = { productId, quantity };
        this.http.post<Cart>(`${this.baseUrl}/${cart.id}/items`, request).subscribe({
          next: (updated) => this.cartSignal.set(updated),
          error: (err) => this.errorSignal.set(this.extractMessage(err)),
        });
      }),
    );
  }

  /** Sets the exact quantity of an item (PUT semantics). */
  updateQuantity(productId: number, quantity: number): Observable<Cart> {
    const cart = this.cartSignal();
    if (cart === null) {
      throw new Error('No active cart');
    }
    const request: CartItemQuantityUpdateRequest = { quantity };
    return this.http.put<Cart>(`${this.baseUrl}/${cart.id}/items/${productId}`, request).pipe(
      tap((updated) => this.cartSignal.set(updated)),
    );
  }

  /** Removes an item completely. */
  removeItem(productId: number): Observable<void> {
    const cart = this.cartSignal();
    if (cart === null) {
      throw new Error('No active cart');
    }
    return this.http.delete<void>(`${this.baseUrl}/${cart.id}/items/${productId}`).pipe(
      tap(() => {
        const updated = this.cartSignal();
        if (updated !== null) {
          const remaining = updated.items.filter((item) => item.productId !== productId);
          this.cartSignal.set({
            ...updated,
            items: remaining,
            total: remaining.reduce((sum, item) => sum + item.subtotal, 0),
          });
        }
      }),
    );
  }

  /** Cancels the cart (logical delete on the backend). */
  cancel(): Observable<Cart> {
    const cart = this.cartSignal();
    if (cart === null) {
      throw new Error('No active cart');
    }
    return this.http.delete<Cart>(`${this.baseUrl}/${cart.id}`).pipe(
      tap((cancelled) => {
        this.cartSignal.set(cancelled);
        this.clearStoredCartId();
      }),
    );
  }

  /** Clears the local cart state (e.g. after cancellation). */
  clearLocalState(): void {
    this.clearStoredCartId();
    this.cartSignal.set(null);
    this.errorSignal.set(null);
  }

  private readStoredCartId(): number | null {
    const raw = localStorage.getItem(CART_ID_STORAGE_KEY);
    if (raw === null) {
      return null;
    }
    const parsed = Number(raw);
    return Number.isInteger(parsed) && parsed > 0 ? parsed : null;
  }

  private persistCartId(cartId: number): void {
    localStorage.setItem(CART_ID_STORAGE_KEY, String(cartId));
  }

  private clearStoredCartId(): void {
    localStorage.removeItem(CART_ID_STORAGE_KEY);
  }

  private extractMessage(err: unknown): string {
    if (typeof err === 'object' && err !== null && 'message' in err) {
      return String((err as { message: unknown }).message);
    }
    return 'Unexpected error';
  }
}
