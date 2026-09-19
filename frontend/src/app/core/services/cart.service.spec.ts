import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { CartService } from './cart.service';
import { environment } from '../config/environment';
import { Cart } from '../models/cart.model';

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

describe('CartService', () => {
  let service: CartService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.resetTestingModule();

    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(CartService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('creates a cart with the demoUserId from environment', () => {
    service.create().subscribe((cart) => {
      expect(cart.id).toBe(5);
      expect(service.cart()?.id).toBe(5);
      expect(service.itemCount()).toBe(0);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/carts`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ userId: environment.demoUserId });
    req.flush(activeCart(5));
  });

  it('adds an item to an existing active cart', () => {
    service['cartSignal'].set(activeCart(1));

    service.addItem(10, 2).subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/carts/1/items`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ productId: 10, quantity: 2 });
    req.flush(
      activeCart(1, [
        { id: 1, productId: 10, productName: 'Teclado', unitPrice: 50, quantity: 2, subtotal: 100 },
      ]),
    );

    expect(service.cart()?.items).toHaveLength(1);
    expect(service.itemCount()).toBe(2);
    expect(service.total()).toBe(100);
  });

  it('creates a cart first when adding an item without an active cart', () => {
    service.addItem(10, 1).subscribe();

    const createReq = httpMock.expectOne(`${environment.apiUrl}/carts`);
    expect(createReq.request.method).toBe('POST');
    createReq.flush(activeCart(2));

    const addReq = httpMock.expectOne(`${environment.apiUrl}/carts/2/items`);
    expect(addReq.request.method).toBe('POST');
    addReq.flush(
      activeCart(2, [
        { id: 1, productId: 10, productName: 'Teclado', unitPrice: 50, quantity: 1, subtotal: 50 },
      ]),
    );

    expect(service.cart()?.id).toBe(2);
    expect(service.itemCount()).toBe(1);
  });

  it('updates the exact quantity of an item (PUT semantics)', () => {
    service['cartSignal'].set(
      activeCart(1, [
        { id: 1, productId: 10, productName: 'Teclado', unitPrice: 50, quantity: 1, subtotal: 50 },
      ]),
    );

    service.updateQuantity(10, 4).subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/carts/1/items/10`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual({ quantity: 4 });
    req.flush(
      activeCart(1, [
        { id: 1, productId: 10, productName: 'Teclado', unitPrice: 50, quantity: 4, subtotal: 200 },
      ]),
    );

    expect(service.cart()?.items[0].quantity).toBe(4);
    expect(service.total()).toBe(200);
  });

  it('removes an item and recomputes the total locally', () => {
    service['cartSignal'].set(
      activeCart(1, [
        { id: 1, productId: 10, productName: 'Teclado', unitPrice: 50, quantity: 2, subtotal: 100 },
        { id: 2, productId: 11, productName: 'Mouse', unitPrice: 25, quantity: 1, subtotal: 25 },
      ]),
    );

    service.removeItem(10).subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/carts/1/items/10`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);

    expect(service.cart()?.items).toHaveLength(1);
    expect(service.cart()?.items[0].productId).toBe(11);
    expect(service.total()).toBe(25);
  });

  it('cancels the cart and clears the persisted cartId', () => {
    localStorage.setItem('commerce-platform.cartId', '1');
    service['cartSignal'].set(activeCart(1));

    service.cancel().subscribe((cart) => {
      expect(cart.status).toBe('CANCELLED');
      expect(service.cart()?.status).toBe('CANCELLED');
      expect(localStorage.getItem('commerce-platform.cartId')).toBeNull();
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/carts/1`);
    expect(req.request.method).toBe('DELETE');
    req.flush({ ...activeCart(1), status: 'CANCELLED' });
  });

  it('restores an ACTIVE cart from the persisted cartId', () => {
    localStorage.setItem('commerce-platform.cartId', '3');
    service.restore();

    const req = httpMock.expectOne(`${environment.apiUrl}/carts/3`);
    expect(req.request.method).toBe('GET');
    req.flush(activeCart(3));

    expect(service.cart()?.id).toBe(3);
    expect(service.loading()).toBe(false);
  });

  it('clears local state when the persisted cart is CANCELLED', () => {
    localStorage.setItem('commerce-platform.cartId', '3');
    service.restore();

    const req = httpMock.expectOne(`${environment.apiUrl}/carts/3`);
    req.flush({ ...activeCart(3), status: 'CANCELLED' });

    expect(service.cart()).toBeNull();
    expect(localStorage.getItem('commerce-platform.cartId')).toBeNull();
  });
});
