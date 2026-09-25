import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, NgForm } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../core/config/environment';
import { ToastService } from '../../shared/components/toast/toast.service';
import { toUserMessage } from '../../core/utils/error-handler';

interface Promotion {
  id: number;
  code: string;
  discountType: 'PERCENT' | 'FIXED';
  amount: number;
  startsAt: string;
  endsAt: string;
  active: boolean;
}

@Component({
  selector: 'app-promotions',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <section class="promotions-page">
      <header class="page-header">
        <div>
          <span class="eyebrow">ADMINISTRACIÓN</span>
          <h1>Promociones y cupones</h1>
          <p>Creá descuentos para tus clientes y administrá su vigencia desde un solo lugar.</p>
        </div>
        <span class="counter">{{ promotions().length }} promociones</span>
      </header>

      <div class="form-card">
        <div class="card-heading">
          <div class="heading-icon">%</div>
          <div><h2>Nueva promoción</h2><p>Configurá el descuento y las fechas en que estará disponible.</p></div>
        </div>
        <form #promotionForm="ngForm" (ngSubmit)="create(promotionForm)">
          <div class="form-grid">
            <label class="field field-wide">
              <span>Código del cupón</span>
              <input name="code" [(ngModel)]="code" required minlength="3" maxlength="40"
                placeholder="Ej.: BIENVENIDA10" autocomplete="off" />
              <small>Entre 3 y 40 caracteres. Compartí este código con tus clientes.</small>
            </label>
            <label class="field">
              <span>Tipo de descuento</span>
              <select name="type" [(ngModel)]="type">
                <option value="PERCENT">Porcentaje (%)</option>
                <option value="FIXED">Importe fijo ($)</option>
              </select>
            </label>
            <label class="field">
              <span>Valor del descuento</span>
              <div class="input-suffix">
                <input name="amount" type="number" [min]="0.01" [max]="type === 'PERCENT' ? 100 : null"
                  step="0.01" [(ngModel)]="amount" required />
                <span>{{ type === 'PERCENT' ? '%' : '$' }}</span>
              </div>
            </label>
            <label class="field">
              <span>Fecha y hora de inicio</span>
              <input name="start" type="datetime-local" [(ngModel)]="start" required />
            </label>
            <label class="field">
              <span>Fecha y hora de finalización</span>
              <input name="end" type="datetime-local" [(ngModel)]="end" required />
            </label>
          </div>
          @if (start && end && end <= start) {
            <p class="validation">La fecha de finalización debe ser posterior al inicio.</p>
          }
          <div class="form-actions">
            <span>Podés desactivar el cupón cuando quieras.</span>
            <button class="btn btn--primary create-button" type="submit"
              [disabled]="busy() || promotionForm.invalid || (start !== '' && end !== '' && end <= start)">
              {{ busy() ? 'Guardando…' : '+ Crear promoción' }}
            </button>
          </div>
        </form>
      </div>

      <div class="list-heading">
        <div><h2>Promociones</h2><p>Consultá los cupones creados y administrá su estado.</p></div>
      </div>
      <div class="table-card">
        @if (promotions().length === 0) {
          <div class="empty-state"><span class="empty-icon">%</span><h3>Todavía no hay promociones</h3>
            <p>Creá tu primer cupón usando el formulario de arriba.</p></div>
        } @else {
          <div class="table-scroll">
            <table>
              <thead><tr><th>CÓDIGO</th><th>DESCUENTO</th><th>VIGENCIA</th><th>ESTADO</th><th>ACCIONES</th></tr></thead>
              <tbody>
                @for (p of promotions(); track p.id) {
                  <tr>
                    <td><strong class="coupon-code">{{ p.code }}</strong></td>
                    <td><strong>{{ p.discountType === 'PERCENT' ? (p.amount + ' %') : ('$' + p.amount) }}</strong>
                      <small>{{ p.discountType === 'PERCENT' ? 'Porcentaje' : 'Importe fijo' }}</small></td>
                    <td><span class="date-range">{{ p.startsAt | date:'dd/MM/yyyy HH:mm' }}</span>
                      <small>hasta {{ p.endsAt | date:'dd/MM/yyyy HH:mm' }}</small></td>
                    <td><span class="status" [class.status-inactive]="!p.active">{{ p.active ? 'ACTIVA' : 'INACTIVA' }}</span></td>
                    <td>@if (p.active) {
                      <button class="btn btn--danger btn--sm" type="button" [disabled]="busy()" (click)="deactivate(p.id)">Desactivar</button>
                    } @else { <span class="muted">Sin acciones</span> }</td>
                  </tr>
                }
              </tbody>
            </table>
          </div>
        }
      </div>
    </section>
  `,
  styles: [`
    :host { display:block; }
    .promotions-page { max-width:1280px; margin:0 auto; padding:2rem 1.5rem 4rem; color:#17243d; }
    .page-header { display:flex; justify-content:space-between; align-items:center; gap:1rem; margin-bottom:2rem; }
    .eyebrow { color:#345bd1; font-size:.75rem; font-weight:800; letter-spacing:.13em; }
    h1 { margin:.4rem 0 .5rem; font-size:clamp(1.9rem,3vw,2.5rem); line-height:1.2; }
    h2 { font-size:1.35rem; margin:0 0 .3rem; }
    p { margin:0; color:#63718a; line-height:1.5; }
    .counter { background:#edf2ff; color:#3155c8; padding:.55rem .85rem; border-radius:999px; font-size:.9rem; font-weight:700; white-space:nowrap; }
    .form-card,.table-card { background:#fff; border:1px solid #e1e7f2; border-radius:18px; box-shadow:0 12px 35px rgba(24,48,93,.045); }
    .form-card { padding:1.8rem; margin-bottom:2.5rem; }
    .card-heading { display:flex; align-items:center; gap:1rem; padding-bottom:1.5rem; margin-bottom:1.5rem; border-bottom:1px solid #e9edf5; }
    .heading-icon { width:48px; height:48px; flex-shrink:0; display:grid; place-items:center; border-radius:14px; background:#eaf0ff; color:#3157d1; font-size:1.5rem; font-weight:800; }
    .form-grid { display:grid; grid-template-columns:repeat(2,minmax(0,1fr)); gap:1.3rem 1.5rem; }
    .field { display:flex; flex-direction:column; gap:.5rem; font-size:.94rem; font-weight:700; min-width:0; }
    .field-wide { grid-column:1/-1; }
    .field input,.field select { width:100%; min-width:0; height:48px; padding:.7rem .9rem; border:1px solid #cad4e5; border-radius:10px; background:#fff; color:#17243d; font:inherit; font-weight:500; box-sizing:border-box; }
    .field input:focus,.field select:focus { outline:2px solid #94b0ff; border-color:#345bd1; outline-offset:1px; }
    .field small { color:#78859a; font-weight:400; line-height:1.4; }
    .input-suffix { display:flex; position:relative; align-items:center; }
    .input-suffix input { padding-right:3rem; }
    .input-suffix > span { position:absolute; right:1rem; color:#6a7891; }
    .validation { margin-top:1rem; color:#b93848; font-size:.9rem; }
    .form-actions { display:flex; align-items:center; justify-content:space-between; flex-wrap:wrap; gap:1rem; border-top:1px solid #e9edf5; margin-top:1.7rem; padding-top:1.5rem; }
    .form-actions > span { color:#7b879b; font-size:.9rem; }
    .create-button { min-width:200px; min-height:46px; }
    .list-heading { margin-bottom:1.1rem; }
    .table-card { overflow:hidden; }
    .table-scroll { overflow-x:auto; }
    table { width:100%; border-collapse:collapse; text-align:left; }
    th { background:#f9fbff; color:#62718a; font-size:.75rem; letter-spacing:.07em; padding:1.1rem 1.25rem; white-space:nowrap; }
    td { padding:1.15rem 1.25rem; border-top:1px solid #e9edf5; vertical-align:middle; }
    td small { display:block; margin-top:.35rem; color:#738198; font-size:.8rem; }
    .coupon-code { color:#2448ad; letter-spacing:.03em; }
    .date-range { white-space:nowrap; }
    .status { display:inline-block; padding:.35rem .65rem; border-radius:999px; background:#e8fbf0; color:#18965b; font-size:.75rem; font-weight:800; }
    .status-inactive { background:#f0f2f6; color:#7b8595; }
    .muted { color:#8993a5; font-size:.85rem; }
    .empty-state { text-align:center; padding:3rem 1rem; }
    .empty-state h3 { margin:.8rem 0 .35rem; }
    .empty-icon { display:inline-grid; place-items:center; width:56px; height:56px; background:#edf2ff; border-radius:16px; color:#345bd1; font-size:1.8rem; font-weight:800; }
    @media (max-width:680px) { .promotions-page { padding:1.4rem .9rem 3rem; } .page-header { align-items:flex-start; flex-direction:column; } .form-card { padding:1.2rem; } .form-grid { grid-template-columns:1fr; } .form-actions { align-items:stretch; } .create-button { width:100%; } th,td { padding:.9rem .8rem; } }
  `]
})
export class PromotionsComponent implements OnInit {
  private http = inject(HttpClient);
  private toast = inject(ToastService);
  private url = `${environment.apiUrl}/promotions`;
  promotions = signal<Promotion[]>([]);
  busy = signal(false);
  code = '';
  type: 'PERCENT' | 'FIXED' = 'PERCENT';
  amount = 10;
  start = '';
  end = '';

  ngOnInit() { this.load(); }
  load() {
    this.http.get<Promotion[]>(this.url).subscribe({
      next: result => this.promotions.set(result),
      error: error => this.toast.error(toUserMessage(error))
    });
  }
  create(form: NgForm) {
    if (form.invalid || !this.start || !this.end || this.end <= this.start || this.busy()) return;
    this.busy.set(true);
    this.http.post(this.url, {
      code: this.code.trim(), discountType: this.type, amount: this.amount,
      startsAt: new Date(this.start).toISOString(), endsAt: new Date(this.end).toISOString()
    }).subscribe({
      next: () => {
        this.busy.set(false);
        this.code = '';
        this.load();
        this.toast.success('Promoción creada');
      },
      error: error => { this.busy.set(false); this.toast.error(toUserMessage(error)); }
    });
  }
  deactivate(id: number) {
    if (this.busy()) return;
    this.busy.set(true);
    this.http.put(`${this.url}/${id}/deactivate`, {}).subscribe({
      next: () => { this.busy.set(false); this.load(); this.toast.success('Promoción desactivada'); },
      error: error => { this.busy.set(false); this.toast.error(toUserMessage(error)); }
    });
  }
}
