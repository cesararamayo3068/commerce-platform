import { Routes } from '@angular/router';
import { CatalogComponent } from './features/products/catalog/catalog.component';
import { ManageComponent } from './features/products/manage/manage.component';
import { CartPageComponent } from './features/cart/cart-page/cart-page.component';

export const routes: Routes = [
  { path: '', redirectTo: '/products', pathMatch: 'full' },
  { path: 'products', component: CatalogComponent },
  { path: 'products/manage', component: ManageComponent },
  { path: 'cart', component: CartPageComponent },
  { path: '**', redirectTo: '/products' },
];
