import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from './auth.service';
export const authGuard=()=>inject(AuthService).isLoggedIn()?true:inject(Router).createUrlTree(['/login']);
export const adminGuard=()=>inject(AuthService).isAdmin()?true:inject(Router).createUrlTree(['/login']);
