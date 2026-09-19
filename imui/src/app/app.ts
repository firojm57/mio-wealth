import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { TopMenuComponent } from './components/top-menu/top-menu.component';
import { SideMenuComponent } from './components/side-menu/side-menu.component';
import { ContentComponent } from './components/content/content.component';
import { ToastComponent } from './components/toast/toast.component';
import { DashboardService } from './services/dashboard.service';
import { AuthService } from './services/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    TranslateModule,
    TopMenuComponent,
    SideMenuComponent,
    ContentComponent,
    ToastComponent
  ],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  private translate = inject(TranslateService);
  protected readonly dashboardService = inject(DashboardService);
  protected readonly authService = inject(AuthService);

  constructor() {
    this.translate.setDefaultLang('en');
    this.translate.use('en');
  }
}
