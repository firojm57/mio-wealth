import { Routes } from '@angular/router';
import { authGuard } from './guards/auth.guard';
import { LoginComponent } from './components/login/login.component';
import { SignupComponent } from './components/signup/signup.component';
import { DashboardViewportComponent } from './components/dashboard-viewport/dashboard-viewport.component';
import { InvestmentComponent } from './components/investment/investment.component';
import { BalanceComponent } from './components/balance/balance.component';
import { SectionPlaceholderComponent } from './components/section-placeholder/section-placeholder.component';

import { CashFlowComponent } from './components/cash-flow/cash-flow.component';
import { DebtComponent } from './components/debt/debt.component';

export const routes: Routes = [
  {
    path: 'login',
    component: LoginComponent
  },
  {
    path: 'signup',
    component: SignupComponent
  },
  {
    path: 'overview',
    component: DashboardViewportComponent,
    canActivate: [authGuard]
  },
  {
    path: 'investments',
    component: InvestmentComponent,
    canActivate: [authGuard]
  },
  {
    path: 'balance',
    component: BalanceComponent,
    canActivate: [authGuard]
  },
  {
    path: 'cash-flow',
    component: CashFlowComponent,
    canActivate: [authGuard]
  },
  {
    path: 'expenses',
    redirectTo: 'cash-flow',
    pathMatch: 'full'
  },
  {
    path: 'debt',
    component: DebtComponent,
    canActivate: [authGuard]
  },
  {
    path: 'performance',
    redirectTo: 'debt',
    pathMatch: 'full'
  },
  {
    path: 'goals',
    component: SectionPlaceholderComponent,
    canActivate: [authGuard]
  },
  {
    path: 'settings',
    component: SectionPlaceholderComponent,
    canActivate: [authGuard]
  },
  {
    path: '',
    redirectTo: 'overview',
    pathMatch: 'full'
  },
  {
    path: '**',
    redirectTo: 'overview'
  }
];
