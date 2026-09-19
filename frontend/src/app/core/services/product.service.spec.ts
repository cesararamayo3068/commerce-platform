import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ProductService } from './product.service';
import { environment } from '../config/environment';
import { PagedModel } from '../models/paged.model';
import { Product } from '../models/product.model';

describe('ProductService', () => {
  let service: ProductService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.resetTestingModule();

    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(ProductService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('lists active products using the PagedModel/HAL contract', () => {
    const paged: PagedModel<Product> = {
      _embedded: { productResponseList: [
        {
          id: 1,
          name: 'Teclado',
          description: null,
          brand: null, category: null, imageUrl: null,
          price: 99.99,
          active: true,
          createdAt: '2026-01-01T00:00:00Z',
          updatedAt: '2026-01-01T00:00:00Z',
        },
      ] },
      page: { size: 12, number: 0, totalElements: 1, totalPages: 1 },
    };

    service.list(true, 0, 12, 'name,asc').subscribe((result) => {
      expect(result._embedded!.productResponseList!).toHaveLength(1);
      expect(result._embedded!.productResponseList![0].name).toBe('Teclado');
      expect(result.page.totalElements).toBe(1);
    });

    const req = httpMock.expectOne(
      `${environment.apiUrl}/products?page=0&size=12&active=true&sort=name,asc`,
    );
    expect(req.request.method).toBe('GET');
    req.flush(paged);
  });

  it('creates a product with the ProductCreateRequest body', () => {
    service.create({ name: 'Mouse', description: null, brand: null, category: null, imageUrl: null, price: 25.5 }).subscribe((product) => {
      expect(product.id).toBe(2);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/products`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ name: 'Mouse', description: null, brand: null, category: null, imageUrl: null, price: 25.5 });
    req.flush({
      id: 2,
      name: 'Mouse',
      description: null,
      brand: null, category: null, imageUrl: null,
      price: 25.5,
      active: true,
      createdAt: '2026-01-01T00:00:00Z',
      updatedAt: '2026-01-01T00:00:00Z',
    });
  });

  it('deactivates a product via DELETE', () => {
    service.deactivate(7).subscribe((product) => {
      expect(product.active).toBe(false);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/products/7`);
    expect(req.request.method).toBe('DELETE');
    req.flush({
      id: 7,
      name: 'X',
      description: null,
      brand: null, category: null, imageUrl: null,
      price: 1,
      active: false,
      createdAt: '2026-01-01T00:00:00Z',
      updatedAt: '2026-01-01T00:00:00Z',
    });
  });
});
