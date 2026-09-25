import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
@Component({standalone:true,selector:'app-login',imports:[FormsModule,RouterLink],template:`
<section class="auth"><h1>Ingresar</h1><p>Iniciá sesión para comprar o administrar tus productos.</p>
<form (ngSubmit)="submit()"><label>Email<input type="email" name="email" [(ngModel)]="email" required autocomplete="email"></label>
<label>Contraseña<input type="password" name="password" [(ngModel)]="password" required autocomplete="current-password"></label>
@if(error()){<p role="alert" class="error">{{error()}}</p>}
<button type="submit" [disabled]="busy()">{{busy()?'Ingresando...':'Ingresar'}}</button></form>
<p>¿No tenés cuenta? <a routerLink="/register">Registrate</a></p></section>`,styles:`.auth{max-width:430px;margin:3rem auto;background:white;padding:2rem;border-radius:20px;box-shadow:0 10px 30px #17203312}.auth label{display:block;margin:1rem 0;font-weight:600}.auth input{display:block;width:100%;padding:.8rem;border:1px solid #cbd5e1;border-radius:9px;margin-top:.4rem}.auth button{background:#3157d5;color:white;border:0;padding:.8rem 2rem;border-radius:9px;cursor:pointer}.error{color:#b91c1c}`})
export class LoginComponent{private auth=inject(AuthService);private router=inject(Router);email='';password='';busy=signal(false);error=signal('');submit(){this.busy.set(true);this.error.set('');this.auth.login(this.email,this.password).subscribe({next:s=>this.router.navigateByUrl(s.role==='ADMIN'?'/products/manage':'/products'),error:()=>{this.error.set('Email o contraseña incorrectos');this.busy.set(false)}})}}
