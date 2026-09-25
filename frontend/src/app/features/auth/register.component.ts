import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
@Component({standalone:true,selector:'app-register',imports:[FormsModule,RouterLink],template:`
<section class="auth"><h1>Crear cuenta</h1><p>Registrate para empezar a comprar.</p>
<form (ngSubmit)="submit()"><label>Nombre<input name="firstName" [(ngModel)]="firstName" required></label><label>Apellido<input name="lastName" [(ngModel)]="lastName" required></label>
<label>DNI<input name="dni" [(ngModel)]="dni" required></label><label>Email<input type="email" name="email" [(ngModel)]="email" required></label>
<label>Contraseña (mínimo 12 caracteres)<input type="password" name="password" [(ngModel)]="password" minlength="12" required></label>
@if(error()){<p role="alert" class="error">{{error()}}</p>}<button type="submit" [disabled]="busy()">Crear cuenta</button></form>
<p>¿Ya tenés cuenta? <a routerLink="/login">Ingresá</a></p></section>`,styles:`.auth{max-width:430px;margin:3rem auto;background:white;padding:2rem;border-radius:20px;box-shadow:0 10px 30px #17203312}.auth label{display:block;margin:1rem 0;font-weight:600}.auth input{display:block;width:100%;padding:.8rem;border:1px solid #cbd5e1;border-radius:9px;margin-top:.4rem}.auth button{background:#3157d5;color:white;border:0;padding:.8rem 2rem;border-radius:9px;cursor:pointer}.error{color:#b91c1c}`})
export class RegisterComponent{private auth=inject(AuthService);private router=inject(Router);firstName='';lastName='';dni='';email='';password='';busy=signal(false);error=signal('');submit(){this.busy.set(true);this.error.set('');this.auth.register({firstName:this.firstName,lastName:this.lastName,dni:this.dni,email:this.email,password:this.password}).subscribe({next:()=>this.router.navigateByUrl('/products'),error:()=>{this.error.set('No se pudo crear la cuenta. Verificá tus datos.');this.busy.set(false)}})}}
