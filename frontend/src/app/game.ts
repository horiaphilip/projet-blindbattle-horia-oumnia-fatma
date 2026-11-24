export type AnswerMode = 'TYPE_IT' | 'QUICK_CHOICE';
export type GuessTarget = 'TITLE' | 'ARTIST' | 'BOTH';
export type Difficulty = 'EASY' | 'HARD';
export type GameStatus = 'LOBBY' | 'PLAYING' | 'FINISHED';

export interface GameSettings {
  answerMode: AnswerMode;
  guessTarget: GuessTarget;
  roundCount: number;
  roundSeconds: number;
  difficulty: Difficulty;
  themeId: string | null;
  quizId: number | null;
}

export interface GamePlayer {
  id: number;
  nickname: string;
  score: number;
  guest: boolean;
}

export interface Game {
  code: string;
  status: GameStatus;
  settings: GameSettings;
  hostId: number;
  hostName: string;
  players: GamePlayer[];
  createdAt: string;
}

export interface GameState {
  code: string;
  status: GameStatus;
  currentRound: number;
  totalRounds: number;
  endsAt: string | null;
  serverTime: string;
  playerCount: number;
}

export interface JoinResponse {
  playerId: number;
  nickname: string;
  playerToken: string;
}

export interface AnswerRequest {
  playerToken: string;
  text?: string;
  choiceIndex?: number;
}

export interface Preview {
  roundId: number;
  roundNumber: number;
  previewUrl: string;
}

export interface Theme {
  id: string;
  name: string;
  category: string;
  coverUrl: string | null;
}

export interface Standing {
  playerId: number;
  nickname: string;
  score: number;
  answered?: boolean;
  correct?: boolean;
  points?: number;
  streak?: number;
}

export interface PlayerJoined {
  playerId: number;
  nickname: string;
  guest: boolean;
  playerCount: number;
}

export interface RoundStarted {
  roundId: number;
  roundNumber: number;
  totalRounds: number;
  startedAt: string;
  endsAt: string;
  serverTime: string;
  choices: string[];
}

export interface AnswerCount {
  roundId: number;
  answered: number;
  players: number;
}

export interface RoundEnded {
  roundId: number;
  roundNumber: number;
  totalRounds: number;
  title: string;
  artist: string;
  coverUrl: string | null;
  fastest: string | null;
  leaderboard: Standing[];
}

export interface GameFinished {
  leaderboard: Standing[];
}

export type GameEvent =
  | { type: 'PLAYER_JOINED'; data: PlayerJoined }
  | { type: 'GAME_STARTED'; data: { totalRounds: number } }
  | { type: 'ROUND_STARTED'; data: RoundStarted }
  | { type: 'ANSWER_COUNT'; data: AnswerCount }
  | { type: 'ROUND_ENDED'; data: RoundEnded }
  | { type: 'GAME_FINISHED'; data: GameFinished };

export type GameEventType = GameEvent['type'];
