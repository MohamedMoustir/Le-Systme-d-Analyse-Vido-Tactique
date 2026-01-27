import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { VideoResponse } from '../models/analysis.model';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private baseUrl = `${environment.apiUrl}/videos`;

  constructor(private http: HttpClient) {}

  uploadVideo(file: File): Observable<VideoResponse> {
    const formData = new FormData();
    formData.append('file', file);
    
    const jsonData = JSON.stringify({ titre: file.name });
    formData.append('data', new Blob([jsonData], { type: 'application/json' }));

    return this.http.post<VideoResponse>(this.baseUrl, formData);
  }

  startAnalysis(videoId: string): Observable<any> {
    return this.http.post(`${this.baseUrl}/${videoId}/analyze`, {});
  }

  stopAnalysis(): Observable<any> {
    return this.http.post(`${environment.apiUrl}/stream/stop`, {});
  }
}