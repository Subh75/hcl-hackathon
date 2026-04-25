import { Routes } from '@angular/router';
import { authGuard } from './services/auth.guard';
import { LoginPageComponent } from './pages/login-page.component';
import { RegisterPageComponent } from './pages/register-page.component';
import { PayeeListPageComponent } from './pages/payee-list-page.component';
import { PayeeFormPageComponent } from './pages/payee-form-page.component';

export const routes: Routes = [
  {
    path: 'login',
    component: LoginPageComponent
  },
  {
    path: 'register',
    component: RegisterPageComponent
  },
  {
    path: 'payees',
    component: PayeeListPageComponent,
    canActivate: [authGuard]
  },
  {
    path: 'payees/add',
    component: PayeeFormPageComponent,
    canActivate: [authGuard]
  },
  {
    path: 'payees/edit/:id',
    component: PayeeFormPageComponent,
    canActivate: [authGuard]
  },
  {
    path: 'payees/view/:id',
    component: PayeeFormPageComponent,
    canActivate: [authGuard]
  },
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'login'
  },
  {
    path: '**',
    redirectTo: 'login'
  }
];
