import { Routes } from '@angular/router';
import { RocketsComponent } from './rockets/rockets.component';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'rockets' },
  { path: 'rockets', component: RocketsComponent }
];
