import { Injectable } from '@angular/core';
import { Client, Stomp } from '@stomp/stompjs';
import { BehaviorSubject, Observable } from 'rxjs';
import SockJS from 'sockjs-client';
import { AnalysisMessage } from '../models/analysis.model';
import { environment } from '../../../environments/environment.prod';

@Injectable({
  providedIn: 'root'
})
export class WebsocketService {
  private client: Client;
  private messageSubject = new BehaviorSubject<AnalysisMessage | null>(null);
  public message$ = this.messageSubject.asObservable();

  private connectionStatusSubject = new BehaviorSubject<boolean>(false);
  public isConnected$ = this.connectionStatusSubject.asObservable();

  constructor() {
    this.client = new Client({
      webSocketFactory: () => new SockJS(`${environment.wsUrl}`),
      onConnect: () => {
        console.log('✅ WebSocket Connected');
        this.connectionStatusSubject.next(true);
        this.client.subscribe('/topic/analysis', (message) => {
          if (message.body) {
            const data: AnalysisMessage = JSON.parse(message.body);
            this.messageSubject.next(data);
          }
        });
      },
      onDisconnect: () => {
        console.log('❌ WebSocket Disconnected');
        this.connectionStatusSubject.next(false);
      },
      onStompError: (frame) => {
        console.error('Broker reported error: ' + frame.headers['message']);
        console.error('Additional details: ' + frame.body);
      }
    });
  }

  public connect(): void {
    this.client.activate();
  }

  public disconnect(): void {
    this.client.deactivate();
  }
}