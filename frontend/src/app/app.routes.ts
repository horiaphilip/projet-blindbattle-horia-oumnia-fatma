import { Routes } from '@angular/router';
import { Accueil } from './accueil/accueil';
import { authGuard } from './auth-guard';
import { Classement } from './classement/classement';
import { Connexion } from './connexion/connexion';
import { Inscription } from './inscription/inscription';
import { Jeu } from './jeu/jeu';
import { Joueurs } from './joueurs/joueurs';
import { MesQuiz } from './mes-quiz/mes-quiz';
import { NouvellePartie } from './nouvelle-partie/nouvelle-partie';
import { Page404 } from './page404/page404';
import { PartieHote } from './partie-hote/partie-hote';
import { Profil } from './profil/profil';
import { QuizEditor } from './quiz-editor/quiz-editor';
import { QuizPartage } from './quiz-partage/quiz-partage';
import { Rejoindre } from './rejoindre/rejoindre';

export const routes: Routes = [
  { path: '', component: Accueil },
  { path: 'classement', component: Classement },
  { path: 'connexion', component: Connexion },
  { path: 'inscription', component: Inscription },
  { path: 'joueurs', component: Joueurs },
  { path: 'profil', component: Profil, canActivate: [authGuard] },
  { path: 'profil/:id', component: Profil },
  { path: 'mes-quiz', component: MesQuiz, canActivate: [authGuard] },
  { path: 'quiz/nouveau', component: QuizEditor, canActivate: [authGuard] },
  { path: 'quiz/:id/edit', component: QuizEditor, canActivate: [authGuard] },
  { path: 'quiz/partage/:token', component: QuizPartage },
  { path: 'partie/nouvelle', component: NouvellePartie, canActivate: [authGuard] },
  { path: 'host/:code', component: PartieHote, canActivate: [authGuard] },
  { path: 'rejoindre', component: Rejoindre },
  { path: 'rejoindre/:code', component: Rejoindre },
  { path: 'jeu/:code', component: Jeu },
  { path: 'page404', component: Page404 },
  { path: '**', redirectTo: 'page404' },
];
