import { TestBed } from '@angular/core/testing';
import { GameEvent } from './game';
import { GameStreamService } from './game-stream-service';

class FakeEventSource {
  static last: FakeEventSource;
  public listeners: Record<string, (message: MessageEvent<string>) => void> = {};
  public closed = false;

  constructor(public url: string) {
    FakeEventSource.last = this;
  }

  addEventListener(type: string, listener: (message: MessageEvent<string>) => void) {
    this.listeners[type] = listener;
  }

  close() {
    this.closed = true;
  }

  emit(type: string, data: unknown) {
    this.listeners[type](new MessageEvent(type, { data: JSON.stringify(data) }));
  }
}

describe('GameStreamService', () => {
  let service: GameStreamService;
  let original: typeof EventSource;

  beforeEach(() => {
    original = window.EventSource;
    (window as unknown as { EventSource: unknown }).EventSource = FakeEventSource;
    TestBed.configureTestingModule({});
    service = TestBed.inject(GameStreamService);
  });

  afterEach(() => {
    window.EventSource = original;
  });

  it('should emit the events of the game stream and close it on unsubscribe', () => {
    const received: GameEvent[] = [];
    const subscription = service.events('abc234').subscribe((event) => received.push(event));
    const source = FakeEventSource.last;
    expect(source.url).toBe('/api/games/ABC234/stream');

    source.emit('PLAYER_JOINED', { type: 'PLAYER_JOINED', data: { nickname: 'Ana' } });
    source.emit('ROUND_ENDED', { type: 'ROUND_ENDED', data: { title: 'Tube' } });
    expect(received.map((event) => event.type)).toEqual(['PLAYER_JOINED', 'ROUND_ENDED']);

    subscription.unsubscribe();
    expect(source.closed).toBeTrue();
  });
});
