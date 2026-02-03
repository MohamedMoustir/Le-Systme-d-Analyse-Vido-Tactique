import { Routes } from '@angular/router';
import { VideoDashboardComponent } from './features/video-analysis/video-dashboard/video-dashboard.component';

export const routes: Routes = [
  { path: '', component: VideoDashboardComponent }, 
  {
    path:'register',
    loadComponent: () => import('./features/auth/register/app.register').then(m => m.Register)
  },
  {
    path : 'login',
    loadComponent: () => import('./features/auth/login/app.login').then(m => m.Login)
  }
];