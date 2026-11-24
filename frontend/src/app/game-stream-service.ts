import { Injectable, NgZone } from '@angular/core';
import { Observable } from 'rxjs';
import { GameEvent, GameEventType } from './game';

const EVENT_TYPES: GameEventType[] = [
  'PLAYER_JOINED',
  'GAME_STARTED',
  'ROUND_STARTED',
  'ANSWER_COUNT',
  'ROUND_ENDED',
  'GAME_FINISHED',
];

@Injectable({
  providedIn: 'root',
})
export class GameStreamService {
  constructor(private zone: NgZone) {}

  public events(code: string): Observable<GameEvent> {
    return new Observable<GameEvent>((subscriber) => {
      const source = new EventSource(
        '/api/games/' + encodeURIComponent(code.toUpperCase()) + '/stream',
      );
      const listener = (message: MessageEvent<string>) => {
        const event = JSON.parse(message.data) as GameEvent;
        this.zone.run(() => subscriber.next(event));
      };
      EVENT_TYPES.forEach((type) => source.addEventListener(type, listener));
      return () => source.close();
    });
  }
}
