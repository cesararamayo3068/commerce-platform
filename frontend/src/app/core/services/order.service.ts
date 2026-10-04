import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../config/environment';
import { Order } from '../models/order.model';

@Injectable({ providedIn: 'root' })
export class OrderService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/orders`;

  checkout(cartId: number): Observable<Order> {
    return this.http.post<Order>(`${this.baseUrl}/checkout/${cartId}`, {});
  }

  list(): Observable<Order[]> {
    return this.http.get<Order[]>(this.baseUrl);
  }

  getById(id: number): Observable<Order> {
    return this.http.get<Order>(`${this.baseUrl}/${id}`);
  }
}
