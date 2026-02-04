import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { SidebarComponent } from '../sidebar/app-sidebar';

@Component({
  selector: 'app-main-layout',
  imports: [RouterOutlet,SidebarComponent],
  templateUrl: './app-main-layout.html',
  styleUrl: './app-main-layout.css',
})
export class MainLayout {

}
