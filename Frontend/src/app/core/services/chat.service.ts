import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { ChatMessage, SendMessageRequest } from '../models';

@Injectable({ providedIn: 'root' })
export class ChatService {
  private http = inject(HttpClient);
  private api = environment.apiUrl;

  getMessages(projectId: string) {
    return this.http.get<ChatMessage[]>(`${this.api}/projects/${projectId}/chat`);
  }

  send(projectId: string, content: string) {
    return this.http.post<ChatMessage>(`${this.api}/projects/${projectId}/chat`, { content });
  }

  countUnread(projectId: string) {
    return this.http.get<{ count: number }>(`${this.api}/projects/${projectId}/chat/unread`);
  }
}
