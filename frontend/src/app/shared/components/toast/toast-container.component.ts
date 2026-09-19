import { Component, inject } from '@angular/core';
import { ToastService } from './toast.service';

@Component({
  selector: 'app-toast-container',
  standalone: true,
  template: `
    <div class="toast-container" aria-live="polite">
      @for (toast of toastService.toasts(); track toast.id) {
        <div class="toast toast--{{ toast.type }}" role="status">
          <span class="toast__icon" aria-hidden="true">
            @switch (toast.type) {
              @case ('success') { ✓ }
              @case ('error') { ✕ }
              @default { ℹ }
            }
          </span>
          <span class="toast__message">{{ toast.message }}</span>
          <button class="toast__close" (click)="toastService.dismiss(toast.id)" aria-label="Cerrar">×</button>
        </div>
      }
    </div>
  `,
  styles: `
    .toast-container {
      position: fixed;
      top: 1rem;
      right: 1rem;
      z-index: 1000;
      display: flex;
      flex-direction: column;
      gap: 0.5rem;
      max-width: min(24rem, calc(100vw - 2rem));
    }
    .toast {
      display: flex;
      align-items: center;
      gap: 0.625rem;
      padding: 0.75rem 1rem;
      border-radius: 0.5rem;
      color: #fff;
      font-size: 0.875rem;
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.18);
      animation: toast-in 0.2s ease-out;
    }
    .toast--success { background: #1a7f4b; }
    .toast--error { background: #b3261e; }
    .toast--info { background: #1d4ed8; }
    .toast__icon { font-weight: 700; }
    .toast__message { flex: 1; }
    .toast__close {
      background: none;
      border: none;
      color: inherit;
      font-size: 1.25rem;
      line-height: 1;
      cursor: pointer;
      padding: 0 0.25rem;
    }
    @keyframes toast-in {
      from { opacity: 0; transform: translateY(-0.5rem); }
      to { opacity: 1; transform: translateY(0); }
    }
  `,
})
export class ToastContainerComponent {
  readonly toastService = inject(ToastService);
}
