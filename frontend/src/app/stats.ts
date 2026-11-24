export interface Page<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface HistoryEntry {
  gameCode: string;
  playedAt: string;
  rank: number;
  playerCount: number;
  finalScore: number;
  correctAnswers: number;
  totalRounds: number;
}

export interface LeaderboardEntry {
  userId: number;
  name: string;
  gamesPlayed: number;
  totalScore: number;
  bestScore: number;
  wins: number;
}
