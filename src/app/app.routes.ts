import { Routes } from '@angular/router';
import { VideoDashboardComponent } from './features/video-analysis/video-dashboard/video-dashboard.component';
import { authGuard } from './core/guards/auth-guard';
import { userResolver } from './core/resolvers/user-resolver';
import { MainLayout } from './core/layout/main-layout/app-main-layout';

export const routes: Routes = [
  {
    path: '',
    component: MainLayout,
    canActivate: [authGuard],
    resolve: {
      userProfile: userResolver
    },
    children: [
      {
        path: '',
        component: VideoDashboardComponent
      }
    ]

  },
  {
    path: 'register',
    loadComponent: () => import('./features/auth/register/app.register').then(m => m.Register)
  },
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/app.login').then(m => m.Login)
  }
];