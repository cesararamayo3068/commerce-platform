import { Injectable, computed, signal, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { tap } from 'rxjs';
import { environment } from '../config/environment';
export interface AuthSession { token: string; userId: number; email: string; role: 'USER' | 'ADMIN'; }
@Injectable({providedIn:'root'})
export class AuthService {
 private readonly http=inject(HttpClient);
 private readonly sessionSignal=signal<AuthSession|null>(null);
 readonly session=this.sessionSignal.asReadonly();
 readonly isAdmin=computed(()=>this.sessionSignal()?.role==='ADMIN');
 readonly isLoggedIn=computed(()=>!!this.sessionSignal());
 login(email:string,password:string){return this.http.post<AuthSession>(`${environment.apiUrl}/auth/login`,{email,password}).pipe(tap(s=>this.sessionSignal.set(s)));}
 register(data:{email:string;password:string;dni:string;firstName:string;lastName:string}){return this.http.post<AuthSession>(`${environment.apiUrl}/auth/register`,data).pipe(tap(s=>this.sessionSignal.set(s)));}
 logout(){this.sessionSignal.set(null);}
 token(){return this.sessionSignal()?.token??null;}
}
