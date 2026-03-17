export interface DashboardStats {
  totalUsers: number;
  totalCoaches: number;
  totalVideosAnalyzed: number;
  totalEquipes: number;
}

export interface UserResponseDTO {
  id: string;
  nom: string;
  email: string;
  role: string;
  activated: boolean; 
}



export interface VideoAdminDTO {
  id: string;
  titre: string;
  coachName: string;      
  status: 'PENDING' | 'ANALYZING' | 'COMPLETED' | 'FAILED'; 
  dateUpload: string;    
  sizeMb: number;        
}

export interface EquipeAdminDTO {
  id: string;
  nom: string;
  logoUrl: string;
  couleurPrimaire: string; 
}


export interface PaymentStats {
  totalRevenue: number;
  activePremiumUsers: number;
  mrr: number;
}

export type UserPlan   = 'FREE' | 'PREMIUM';
export type SubStatus  = 'ACTIVE' | 'TRIALING' | 'CANCELED' | 'PAST_DUE' | 'NONE';

export interface UserPlanDTO {
  userId: string;
  nom: string;
  email: string;
  plan: UserPlan;
  subscriptionStatus: SubStatus;
  currentPeriodEnd?: string;
}