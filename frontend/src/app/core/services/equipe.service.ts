import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http'; // 👈 زيد HttpParams هنا
import { Observable } from 'rxjs';
import { Equipe } from '../models/equipe.model';
import { environment } from '../../../environments/environment';
import { Joueur } from '../models/joueur.model';

@Injectable({
    providedIn: 'root'
})
export class EquipeService {
    private http = inject(HttpClient);
    private apiUrl = `${environment.apiUrl}/equipes`;

    createTeam(nomEquipe: string, couleurHex?: string): Observable<Equipe> {
        let params = new HttpParams().set('nomEquipe', nomEquipe);

        if (couleurHex) {
            params = params.set('couleurHex', couleurHex);
        }

        return this.http.post<Equipe>(`${this.apiUrl}/create`, {}, { params });
    }

    getMyTeam(): Observable<Equipe> {
        return this.http.get<Equipe>(`${this.apiUrl}/my-team`);
    }

    importJoueursCsv(file: File): Observable<Equipe> {
        const formData = new FormData();
        formData.append('file', file);
        return this.http.post<Equipe>(`${this.apiUrl}/my-team/import-csv`, formData);
    }

    addJoueur(dto: any): Observable<Joueur> {
        return this.http.post<Joueur>(`${this.apiUrl}/my-team/joueur`, dto);
    }

    
}