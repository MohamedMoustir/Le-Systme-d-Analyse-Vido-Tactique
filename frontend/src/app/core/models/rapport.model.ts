
export interface RapportGlobalResponse {
  statsGlobales: KPIDTO;
  derniersMatchs: MatchStatDTO[];
  topPerformers: TopPerformerDTO[];
}

export interface KPIDTO {
  matchsJoues: number;
  victoires: number;
  nuls: number;
  defaites: number;
  butsMarques: number;
  butsEncaisses: number;
  possessionMoyenne: number;
  distanceMoyenne: number;
}

export interface MatchStatDTO {
  adversaire: string;
  resultat: string; 
  score: string;
  possession: number;
}

export interface TopPerformerDTO {
  nom: string;
  stat: string;
  role: string;
  photoUrl: string;
}