import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../config/environment';
import { Product, ProductCreateRequest, ProductUpdateRequest } from '../models/product.model';
import { PagedModel } from '../models/paged.model';

/**
 * Product catalog API client.
 *
 * The list endpoint returns Spring Data PagedModel/HAL: the page metadata
 * lives under the "page" key and the items under "content".
 */
@Injectable({ providedIn: 'root' })
export class ProductService {
  private readonly baseUrl = `${environment.apiUrl}/products`;

  constructor(private readonly http: HttpClient) {}

  list(active: boolean | null, page: number, size: number, sort?: string): Observable<PagedModel<Product>> {
    let params = new HttpParams().set('page', String(page)).set('size', String(size));
    if (active !== null) {
      params = params.set('active', String(active));
    }
    if (sort) {
      params = params.set('sort', sort);
    }
    return this.http.get<PagedModel<Product>>(this.baseUrl, { params });
  }

  getById(id: number): Observable<Product> {
    return this.http.get<Product>(`${this.baseUrl}/${id}`);
  }

  create(request: ProductCreateRequest): Observable<Product> {
    return this.http.post<Product>(this.baseUrl, request);
  }

  update(id: number, request: ProductUpdateRequest): Observable<Product> {
    return this.http.put<Product>(`${this.baseUrl}/${id}`, request);
  }

  deactivate(id: number): Observable<Product> {
    return this.http.delete<Product>(`${this.baseUrl}/${id}`);
  }
}
