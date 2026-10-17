import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../config/environment';
import { Payment, PaymentStatus } from '../models/payment.model';
@Injectable({ providedIn: 'root' })
export class PaymentService {
 private readonly http=inject(HttpClient);
 private readonly base=`${environment.apiUrl}/payments/orders`;
 get(orderId:number):Observable<Payment>{return this.http.get<Payment>(`${this.base}/${orderId}`);}
 simulate(orderId:number,outcome:Exclude<PaymentStatus,'PENDING'>):Observable<Payment>{return this.http.post<Payment>(`${this.base}/${orderId}/simulate`,{outcome});}
}
