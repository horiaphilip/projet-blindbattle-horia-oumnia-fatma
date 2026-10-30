export interface User {
  id: number;
  name: string;
  email: string;
  createdAt: string;
  updatedAt: string | null;
}

export interface UserRequest {
  name: string;
  email: string;
}

export interface Badge {
  code: string;
  earnedAt: string;
}

export interface Stats {
  userId: number;
  gamesPlayed: number;
  totalScore: number;
  bestScore: number;
  badges: Badge[];
}
