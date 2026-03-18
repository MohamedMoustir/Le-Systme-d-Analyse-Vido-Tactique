import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { DashboardStats, EquipeAdminDTO, UserResponseDTO } from '../models/admin.model';
import { environment } from '../../../environments/environment';
import { VideoAdminDTO } from '../models/admin.model';

@Injectable({
    providedIn: 'root'
})
export class AdminService {
    private http = inject(HttpClient);

    private adminApiUrl = `${environment.apiUrl}/admin/dashboard`;
    private usersApiUrl = `${environment.apiUrl}/users`;
    private authApiUrl = `${environment.apiUrl}/auth`;
    private videoApiUrl = `${environment.apiUrl}/admin/videos`;
    private equipesApiUrl = `${environment.apiUrl}/admin/equipes`;


    getGlobalStats(): Observable<DashboardStats> {
        return this.http.get<DashboardStats>(`${this.adminApiUrl}/stats`);
    }

    getAllUsers(): Observable<UserResponseDTO[]> {
        return this.http.get<UserResponseDTO[]>(this.usersApiUrl);
    }

    createUser(userData: any): Observable<any> {
        return this.http.post(`${this.authApiUrl}/register`, userData);
    }

    toggleUserStatus(userId: string): Observable<string> {
        return this.http.patch(`${this.usersApiUrl}/${userId}/status`, {}, { responseType: 'text' });
    }

    changeUserRole(userId: string, newRole: string): Observable<string> {
        return this.http.patch(`${this.usersApiUrl}/${userId}/role?newRole=${newRole}`, {}, { responseType: 'text' });
    }

    deleteUser(userId: string): Observable<string> {
        return this.http.delete(`${this.usersApiUrl}/${userId}`, { responseType: 'text' });
    }


    getAllVideos(): Observable<VideoAdminDTO[]> {
        return this.http.get<VideoAdminDTO[]>(this.videoApiUrl);
    }

    deleteVideoAdmin(videoId: string): Observable<string> {
        return this.http.delete(`${this.videoApiUrl}/${videoId}`, { responseType: 'text' });
    }

    getAllEquipes(): Observable<EquipeAdminDTO[]> {
        return this.http.get<EquipeAdminDTO[]>(this.equipesApiUrl);
    }

    createEquipe(equipeData: any): Observable<EquipeAdminDTO> {
        return this.http.post<EquipeAdminDTO>(this.equipesApiUrl, equipeData);
    }

    deleteEquipe(equipeId: string): Observable<string> {
        return this.http.delete(`${this.equipesApiUrl}/${equipeId}`, { responseType: 'text' });
    }
}