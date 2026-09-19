import { Component } from '@angular/core';

@Component({
  selector: 'app-loading',
  standalone: true,
  template: `
    <div class="loading" role="status" aria-label="Cargando">
      <span class="loading__spinner" aria-hidden="true"></span>
      <span class="loading__text">Cargando…</span>
    </div>
  `,
  styles: `
    .loading {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 0.75rem;
      padding: 3rem 1rem;
      color: rgba(255, 255, 255, 0.6);
    }
    .loading__spinner {
      width: 2.25rem;
      height: 2.25rem;
      border-radius: 50%;
      border: 3px solid rgba(56, 189, 248, 0.25);
      border-top-color: #38bdf8;
      animation: spin 0.8s linear infinite;
    }
    .loading__text { font-size: 0.9rem; }
    @keyframes spin { to { transform: rotate(360deg); } }
  `,
})
export class LoadingComponent {}
