import { authGuard, adminGuard } from './core/services/auth.guard';
import { LoginComponent } from './features/auth/login.component';
import { RegisterComponent } from './features/auth/register.component';
import { PromotionsComponent } from './features/promotions/promotions.component';
import { Routes } from '@angular/router';
import { CatalogComponent } from './features/products/catalog/catalog.component';
import { ManageComponent } from './features/products/manage/manage.component';
import { CartPageComponent } from './features/cart/cart-page/cart-page.component';

export const routes: Routes = [
  { path: '', redirectTo: '/products', pathMatch: 'full' },
  { path: 'products', component: CatalogComponent },
  { path: 'products/manage', component: ManageComponent, canActivate: [adminGuard] },
  { path: 'promotions', component: PromotionsComponent, canActivate: [adminGuard] },
  { path: 'cart', component: CartPageComponent, canActivate: [authGuard] },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: '**', redirectTo: '/products' },
];
