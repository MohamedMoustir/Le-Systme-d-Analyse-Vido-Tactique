import { Routes } from '@angular/router';
import { VideoDashboardComponent } from './features/video-analysis/video-dashboard/video-dashboard.component';
import { authGuard } from './core/guards/auth-guard';
import { adminGuard } from './core/guards/admin-guard';
import { userResolver } from './core/resolvers/user-resolver';
import { MainLayout } from './core/layout/main-layout/app-main-layout';
import { canExitGuard } from './core/guards/can-exit-guard';

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
        redirectTo: 'analysis',
        pathMatch: 'full'
      },
      {
        path: 'analysis',
        component: VideoDashboardComponent
      },
      {
        path: 'subscription',
        loadComponent: () => import('./features/subscription/subscription.component').then(m => m.SubscriptionComponent)
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
  },
  {
    path: 'create-team',
    canActivate: [authGuard],
    loadComponent: () => import('./features/team/create-team/create-team.component').then(m => m.CreateTeamComponent)
  },
  {
    path: 'team',
    // canDeactivate:[canExitGuard],
    loadComponent: () => import('./features/team/team.component').then(m => m.TeamComponent)
  },
  {
    path: 'tactique',
    loadComponent: () => import('./features/tableau-tactique/tableau-tactique.component').then(m => m.TableauTactiqueComponent)
  },
  {
    path: 'rapport',
    loadComponent: () => import('./features/rapports/rapports.component').then(m => m.RapportsComponent)
  },
  {
    path: 'bibliotheque',
    loadComponent: () => import('./features/video-library/video-library').then(m => m.VideoLibraryComponent)
  },
  {
    path: 'settings',
    loadComponent: () => import('./features/reglages/reglages.component').then(m => m.ReglagesComponent)
  },

  {
    path: 'admin',
    canActivate: [adminGuard],
   
    loadComponent: () => import('./features/admin-dashboard/admin-layout/admin-layout').then(m => m.AdminLayoutComponent),
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },

      {
        path: 'dashboard',
        loadComponent: () => import('./features/admin-dashboard/admin-dashboard/admin-dashboard.component').then(m => m.AdminDashboardComponent)
      },
      {
        path: 'users',
        loadComponent: () => import('./features/admin-dashboard/admin-users/admin-users.component').then(m => m.AdminUsersComponent)
      },
      
      {
        path: 'payments',
        loadComponent: () => import('./features/payment-management/payment-management.component').then(m => m.PaymentManagementComponent)
      }
    ]
  }
];