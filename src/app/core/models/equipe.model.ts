import { Joueur } from "./joueur.model";


export interface Equipe {
  id: string;
  nom: string;
  logoUrl: string;
  couleurHex: string;
  joueurs: Joueur[];
}