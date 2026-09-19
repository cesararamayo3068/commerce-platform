import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { CatalogComponent } from './catalog.component';
import { environment } from '../../../core/config/environment';
import { PagedModel } from '../../../core/models/paged.model';
import { Product } from '../../../core/models/product.model';

function product(id: number, name: string, active = true): Product {
  return {
    id,
    name,
    description: null,
    brand: null,
    category: null,
    imageUrl: null,
    price: 10 + id,
    active,
    createdAt: '2026-01-01T00:00:00Z',
    updatedAt: '2026-01-01T00:00:00Z',
  };
}

describe('CatalogComponent', () => {
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.resetTestingModule();

    TestBed.configureTestingModule({
      imports: [CatalogComponent],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('loads only active products on init', () => {
    const fixture = TestBed.createComponent(CatalogComponent);
    fixture.detectChanges();

    const req = httpMock.expectOne(
      `${environment.apiUrl}/products?page=0&size=12&active=true&sort=name,asc`,
    );
    expect(req.request.method).toBe('GET');

    const paged: PagedModel<Product> = {
      _embedded: { productResponseList: [product(1, 'Teclado'), product(2, 'Mouse')] },
      page: { size: 12, number: 0, totalElements: 2, totalPages: 1 },
    };
    req.flush(paged);

    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelectorAll('app-product-card').length).toBe(2);
    expect(compiled.textContent).toContain('Teclado');
    expect(compiled.textContent).toContain('Mouse');
  });

  it('shows an empty state when there are no active products', () => {
    const fixture = TestBed.createComponent(CatalogComponent);
    fixture.detectChanges();

    const req = httpMock.expectOne(
      `${environment.apiUrl}/products?page=0&size=12&active=true&sort=name,asc`,
    );
    req.flush({ _embedded: { productResponseList: [] }, page: { size: 12, number: 0, totalElements: 0, totalPages: 0 } });

    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('No hay productos activos');
  });

  it('shows a readable error when the backend is unreachable', () => {
    const fixture = TestBed.createComponent(CatalogComponent);
    fixture.detectChanges();

    const req = httpMock.expectOne(
      `${environment.apiUrl}/products?page=0&size=12&active=true&sort=name,asc`,
    );
    req.error(new ProgressEvent('network error'), { status: 0, statusText: 'Unknown Error' });

    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('No se pudo conectar con el servidor');
  });
});
