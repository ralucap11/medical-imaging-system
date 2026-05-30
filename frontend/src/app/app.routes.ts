import {authGuard} from './core/guards/auth.guard';
import {Routes} from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'auth/login',
    pathMatch: 'full'
  },
  {
    path: 'auth',
    children: [
      {
        path: 'login',
        loadComponent: () =>
          import('./features/auth/login/login.component')
            .then(m => m.LoginComponent)
      },
      {
        path: 'register',
        loadComponent: () =>
          import('./features/auth/register/register.component')
            .then(m => m.RegisterComponent)
      }
    ]
  },
  {
    path: 'dashboard',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/dashboard/dashboard.component')
        .then(m => m.DashboardComponent)
  },
  {
    path: 'patient/:id',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/patient-detail/patient-detail')
        .then(m => m.PatientDetail)
  },
  {
    path: '**',
    redirectTo: 'auth/login'
  }
];
