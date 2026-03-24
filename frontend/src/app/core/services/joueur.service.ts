import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';
import { Equipe } from '../models/equipe.model';
import { environment } from '../../../environments/environment';
import { Joueur } from '../models/joueur.model';

@Injectable({ providedIn: 'root' })
export class JoueurService {
    private http = inject(HttpClient);
    private apiUrl = `${environment.apiUrl}/equipes/joueur`;
    

    deleteJoueur(id: string): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/${id}`);
    }

    updateJoueur(id: string, dto: any): Observable<Joueur> {
        return this.http.put<Joueur>(`${this.apiUrl}/${id}`, dto);
    }

    

}