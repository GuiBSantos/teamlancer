export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
  role: 'CLIENT' | 'MEMBER';
  location?: string;
  bio?: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface AuthResponse {
  userId: string;
  name: string;
  email: string;
  role: 'CLIENT' | 'MEMBER' | 'ADMIN';
  accessToken: string;
  refreshToken: string;
}


export interface TeamSummary {
  id: string;
  name: string;
  slug: string;
  techStack: string[];
  location: string;
  teamScore: number;
}

export interface TeamMember {
  userId: string;
  name: string;
  avatarUrl: string | null;
  roleInTeam: string;
}

export interface TeamPortfolio {
  id: string;
  title: string;
  description: string;
  url: string | null;
}

export interface TeamDetail {
  id: string;
  name: string;
  slug: string;
  description: string;
  techStack: string[];
  location: string;
  teamScore: number;
  ownerId: string;
  ownerName: string;
  members: TeamMember[];
  portfolio: TeamPortfolio[];
}

export interface CreateTeamRequest {
  name: string;
  description?: string;
  techStack?: string[];
  location?: string;
}


export interface CreateProjectRequest {
  teamId: string;
  projectName: string;
  description: string;
  budgetRange?: string;
  deadline?: string;
}

export interface ProjectRequest {
  id: string;
  clientId: string;
  clientName: string;
  teamId: string;
  teamName: string;
  teamSlug: string;
  projectName: string;
  description: string;
  budgetRange: string;
  deadline: string;
  status: 'PENDING' | 'ACCEPTED' | 'REJECTED' | 'CANCELLED';
  projectId: string | null;
  createdAt: string;
}

export interface TeamInvite {
  id: string;
  teamId: string;
  teamName: string;
  teamSlug: string;
  invitedUserId: string;
  invitedUserName: string;
  roleInTeam: string;
  status: 'PENDING' | 'ACCEPTED' | 'REJECTED';
  createdAt: string;
}

export interface CreateInviteRequest {
  email: string;
  roleInTeam: string;
}

export interface JoinRequest {
  id: string;
  teamId: string;
  teamName: string;
  teamSlug: string;
  userId: string;
  userName: string;
  message: string;
  status: 'PENDING' | 'ACCEPTED' | 'REJECTED';
  createdAt: string;
}

export interface CreateJoinRequest {
  teamId: string;
  message?: string;
}


export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
}

export type ProjectStatus = 'IN_PROGRESS' | 'IN_TESTING' | 'COMPLETED' | 'ON_HOLD' | 'CANCELLED';

export interface Project {
  id: string;
  requestId: string;
  clientId: string;
  clientName: string;
  teamId: string;
  teamName: string;
  teamSlug: string;
  name: string;
  description: string;
  status: ProjectStatus;
  startedAt: string;
  finishedAt: string | null;
  createdAt: string;
}

export interface ChatMessage {
  id: string;
  senderId: string;
  senderName: string;
  content: string;
  isRead: boolean;
  createdAt: string;
}

export interface SendMessageRequest {
  content: string;
}

export type RaterType = 'CLIENT' | 'TEAM';

export interface Rating {
  id: string;
  projectId: string;
  raterType: RaterType;
  score: number;
  comment: string | null;
  createdAt: string;
}

export interface ProjectRatings {
  clientRating: Rating | null;
  teamRating: Rating | null;
}

export interface CreateRatingRequest {
  score: number;
  comment?: string;
}