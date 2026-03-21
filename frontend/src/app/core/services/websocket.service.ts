import { Injectable, OnDestroy } from '@angular/core';
import { Client, Stomp, IMessage, StompSubscription } from '@stomp/stompjs';
import { BehaviorSubject, Observable, Subject, filter, take } from 'rxjs';
import SockJS from 'sockjs-client';
import { AnalysisMessage } from '../models/analysis.model';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class WebsocketService implements OnDestroy {
  private client: Client;
  private connectionStatusSubject = new BehaviorSubject<boolean>(false);
  
  public isConnected$ = this.connectionStatusSubject.asObservable();

  private activeSubscriptions: Map<string, StompSubscription> = new Map();

  constructor() {
    this.client = new Client({
      webSocketFactory: () => new SockJS(environment.wsUrl),
      
      reconnectDelay: 5000, 
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,

      debug: (str) => {
        if (!environment.production) console.log(str);
      },

      onConnect: (frame) => {
        console.log(' WebSocket Connected Successfully');
        this.connectionStatusSubject.next(true);
      },

      onDisconnect: (frame) => {
        console.log(' WebSocket Disconnected');
        this.connectionStatusSubject.next(false);
        this.activeSubscriptions.clear(); 
      },

      onStompError: (frame) => {
        console.error(' Broker reported error: ' + frame.headers['message']);
        console.error('Additional details: ' + frame.body);
      },
      
      onWebSocketClose: () => {
        this.connectionStatusSubject.next(false);
      }
    });

    this.connect();
  }

  public connect(): void {
    if (!this.client.active) {
      this.client.activate();
    }
  }

  public disconnect(): void {
    if (this.client.active) {
      this.client.deactivate();
    }
  }

 
  public watchVideoAnalysis(videoId: string): Observable<AnalysisMessage> {
    const subject = new Subject<AnalysisMessage>();

    this.isConnected$.pipe(
      filter(connected => connected === true),
      take(1) 
    ).subscribe(() => {
      
      if (this.activeSubscriptions.has(videoId)) {
        return; 
      }

      console.log(` Subscribing to topic: /topic/analysis/${videoId}`);

      const subscription = this.client.subscribe(`/topic/analysis/${videoId}`, (message: IMessage) => {
        if (message.body) {
          try {
            const data: AnalysisMessage = JSON.parse(message.body);
            subject.next(data);
          } catch (e) {
            console.error('Error parsing JSON', e);
          }
        }
      });

      this.activeSubscriptions.set(videoId, subscription);
    });

    return subject.asObservable();
  }

  
  public unsubscribeFromVideo(videoId: string): void {
    const subscription = this.activeSubscriptions.get(videoId);
    if (subscription) {
      subscription.unsubscribe();
      this.activeSubscriptions.delete(videoId);
      console.log(` Unsubscribed from video: ${videoId}`);
    }
  }

  ngOnDestroy(): void {
    this.disconnect();
  }
}