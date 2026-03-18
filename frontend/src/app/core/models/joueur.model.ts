export interface Joueur {
    id: string;
    nomComplet: string;
    numeroMaillot: number;
    poste: string;
    photoUrl: string
}
export interface JoueurUpdateDTO {
  nomComplet: string;
  numeroMaillot: number;
  poste: string;
  photoUrl: string;
}