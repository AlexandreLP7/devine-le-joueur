import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface GameResponse {
  message: string;
  hint: string;
  currentStep: number;
  gameOver: boolean;
  win: boolean;
  playerId: number; 
}

@Injectable({
  providedIn: 'root'
})
export class GameService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) { }

  startGame(): Observable<GameResponse> {
    return this.http.get<GameResponse>(`${this.apiUrl}/start`);
  }

  makeGuess(name: string): Observable<GameResponse> {
    return this.http.post<GameResponse>(`${this.apiUrl}/guess?name=${name}`, {});
  }

  getPlayerNames(): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/player-names`);
  }

  getPlayerImageUrl(playerId: number): string {
    return `https://api.sofascore.app/api/v1/player/${playerId}/image`;
  }

  getHistory(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/history`);
  }

}
 
