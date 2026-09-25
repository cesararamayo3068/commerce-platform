import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { CartPageComponent } from './cart-page.component';
import { CartService } from '../../../core/services/cart.service';
import { AuthService } from '../../../core/services/auth.service';
import { environment } from '../../../core/config/environment';
import { Cart } from '../../../core/models/cart.model';

function activeCart(id: number, items: Cart['items'] = []): Cart {
  return {
    id,
    userId: environment.demoUserId,
    status: 'ACTIVE',
    items,
    total: items.reduce((sum, item) => sum + item.subtotal, 0),
    createdAt: '2026-01-01T00:00:00Z',
    updatedAt: '2026-01-01T00:00:00Z',
  };
}

describe('CartPageComponent', () => {
  let httpMock: HttpTestingController;
  let cartService: CartService;

  beforeEach(() => {
    TestBed.resetTestingModule();

    localStorage.clear();
    TestBed.configureTestingModule({
      imports: [CartPageComponent],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    });
    httpMock = TestBed.inject(HttpTestingController);
    cartService = TestBed.inject(CartService);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('renders items with subtotals and the cart total', () => {
    cartService['cartSignal'].set(
      activeCart(1, [
        { id: 1, productId: 10, productName: 'Teclado', unitPrice: 50, quantity: 2, subtotal: 100 },
        { id: 2, productId: 11, productName: 'Mouse', unitPrice: 25, quantity: 1, subtotal: 25 },
      ]),
    );

    const fixture = TestBed.createComponent(CartPageComponent);
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelectorAll('[data-testid="cart-item"]').length).toBe(2);
    expect(compiled.textContent).toContain('Teclado');
    expect(compiled.textContent).toContain('Mouse');
    expect(compiled.querySelector('[data-testid="cart-total"]')?.textContent).toContain('125.00');
  });

  it('shows an empty state for an empty active cart', () => {
    cartService['cartSignal'].set(activeCart(1));

    const fixture = TestBed.createComponent(CartPageComponent);
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('Tu carrito está vacío');
  });

  it('cancels the cart and clears the persisted cartId', () => {
    // Restoring a cart now requires an authenticated user.
    const authService = TestBed.inject(AuthService);
    authService.login('test@example.com', 'test-password').subscribe();
    const loginReq = httpMock.expectOne(`${environment.apiUrl}/auth/login`);
    expect(loginReq.request.method).toBe('POST');
    loginReq.flush({ token: 'test-token', userId: 1, email: 'test@example.com', role: 'USER' });

    localStorage.setItem('commerce-platform.cartId', '1');
    cartService['cartSignal'].set(
      activeCart(1, [
        { id: 1, productId: 10, productName: 'Teclado', unitPrice: 50, quantity: 1, subtotal: 50 },
      ]),
    );

    const fixture = TestBed.createComponent(CartPageComponent);
    fixture.detectChanges();

    // ngOnInit restores the cart from the persisted cartId.
    const restoreReq = httpMock.expectOne(`${environment.apiUrl}/carts/1`);
    expect(restoreReq.request.method).toBe('GET');
    restoreReq.flush(
      activeCart(1, [
        { id: 1, productId: 10, productName: 'Teclado', unitPrice: 50, quantity: 1, subtotal: 50 },
      ]),
    );
    fixture.detectChanges();

    const cancelButton = (fixture.nativeElement as HTMLElement).querySelector(
      '[data-testid="cancel-cart"]',
    ) as HTMLButtonElement;
    cancelButton.click();
    fixture.detectChanges();

    const req = httpMock.expectOne(`${environment.apiUrl}/carts/1`);
    expect(req.request.method).toBe('DELETE');
    req.flush({ ...activeCart(1), status: 'CANCELLED' });

    expect(cartService.cart()?.status).toBe('CANCELLED');
    expect(localStorage.getItem('commerce-platform.cartId')).toBeNull();
  });
});
