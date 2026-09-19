import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { NavbarComponent } from './shared/components/navbar/navbar.component';
import { FooterComponent } from './shared/components/footer/footer.component';
import { ToastContainerComponent } from './shared/components/toast/toast-container.component';

@Component({
  selector: 'app-root', standalone: true,
  imports: [RouterOutlet, NavbarComponent, FooterComponent, ToastContainerComponent],
  template: `<app-navbar /><main class="main"><router-outlet /></main><app-footer /><app-toast-container />`,
  styles: `.main{width:100%;max-width:1240px;margin:0 auto;padding:2rem 1.5rem 4rem;flex:1}@media(max-width:640px){.main{padding:1.25rem 1rem 3rem}}`,
})
export class App {}
