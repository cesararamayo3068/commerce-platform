import { Component, input } from '@angular/core';

@Component({
  selector: 'app-empty-state',
  standalone: true,
  template: `
    <div class="empty">
      <span class="empty__icon" aria-hidden="true">◌</span>
      <p class="empty__title">{{ title() }}</p>
      @if (message(); as msg) {
        <p class="empty__message">{{ msg }}</p>
      }
    </div>
  `,
  styles: `
    .empty {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 0.5rem;
      padding: 3rem 1rem;
      text-align: center;
      color: #64748b;
    }
    .empty__icon {
      font-size: 2.5rem;
      color: #0284c7;
    }
    .empty__title { font-size: 1.05rem; font-weight: 600; color: #172033;}
    .empty__message { font-size: 0.9rem; max-width: 32rem; }
  `,
})
export class EmptyStateComponent {
  readonly title = input.required<string>();
  readonly message = input<string>();
}
