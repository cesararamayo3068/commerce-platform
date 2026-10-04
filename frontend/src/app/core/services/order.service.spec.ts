import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { OrderService } from './order.service';
import { environment } from '../config/environment';

describe('OrderService', () => {
  let service: OrderService;
  let http: HttpTestingController;
  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(OrderService);
    http = TestBed.inject(HttpTestingController);
  });
  afterEach(() => http.verify());

  it('checks out a cart', () => {
    service.checkout(7).subscribe();
    const req = http.expectOne(`${environment.apiUrl}/orders/checkout/7`);
    expect(req.request.method).toBe('POST');
    req.flush({});
  });

  it('lists current user orders', () => {
    service.list().subscribe();
    const req = http.expectOne(`${environment.apiUrl}/orders`);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });
});
