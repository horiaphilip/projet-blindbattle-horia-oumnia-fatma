import { Routes } from '@angular/router';
import { Accueil } from './accueil/accueil';
import { Inscription } from './inscription/inscription';
import { Joueurs } from './joueurs/joueurs';
import { Page404 } from './page404/page404';
import { Profil } from './profil/profil';

export const routes: Routes = [
  { path: '', component: Accueil },
  { path: 'inscription', component: Inscription },
  { path: 'joueurs', component: Joueurs },
  { path: 'profil/:id', component: Profil },
  { path: 'page404', component: Page404 },
  { path: '**', redirectTo: 'page404' },
];
