export type QuizVisibility = 'PRIVATE' | 'PUBLIC';

export interface QuizRequest {
  title: string;
  description: string | null;
  visibility: QuizVisibility;
  trackIds: number[];
}

export interface QuizTrack {
  position: number;
  trackId: number;
  title: string;
  artist: string;
  coverUrl: string | null;
  hasPreview: boolean;
}

export type SelectedTrack = Omit<QuizTrack, 'position'>;

export interface Quiz {
  id: number;
  title: string;
  description: string | null;
  visibility: QuizVisibility;
  shareToken: string;
  ownerId: number;
  ownerName: string;
  trackCount: number;
  playableTrackCount: number;
  createdAt: string;
  updatedAt: string | null;
  tracks: QuizTrack[];
}

export interface QuizSummary {
  id: number;
  title: string;
  visibility: QuizVisibility;
  trackCount: number;
  createdAt: string;
}

export interface TrackResult {
  id: number;
  deezerId: number;
  title: string;
  artist: string;
  album: string;
  coverUrl: string | null;
  rank: number;
  durationSeconds: number;
  hasPreview: boolean;
}
