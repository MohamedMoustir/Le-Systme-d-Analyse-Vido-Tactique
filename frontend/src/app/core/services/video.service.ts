import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { map, Observable, tap } from 'rxjs';
import { VideoUploadResponse } from '../models/analysis.model';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class VideoService {
  private apiUrl = `${environment.apiUrl}/videos`;
  constructor(private http: HttpClient) { }

  uploadVideo(file: File): Observable<VideoUploadResponse> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('data', new Blob([JSON.stringify({ title: file.name })], { type: 'application/json' }));

    return this.http.post<VideoUploadResponse>(this.apiUrl, formData);
  }

  startAnalysis(videoId: number): Observable<any> {
    return this.http.post(`${this.apiUrl}/${videoId}/analyze`, {});
  }

  stopAnalysis(): Observable<any> {
    return this.http.post(`${environment.apiUrl}/stop-analysis`, {});
  }

  stopStream(): Observable<any> {
    return this.http.post(`${environment.streamUrl}/stop`, {});
  }

 
}