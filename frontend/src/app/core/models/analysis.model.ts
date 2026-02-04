export interface VideoResponse {
  id: string;        
  titre: string;     
  urlFichier: string; 
}

export interface VideoUploadResponse extends VideoResponse {}

export interface FrameAnalysis {
  type: string;              
  frame_num: number;          
  ball_detected: boolean;   
  ball_holder_id: number | null;
  players_count: number;
  team_1_count: number;
  team_2_count: number;
  possession: { [key: string]: number }; 
  players: any[];           
  percent?: number;           
  message?: string;        
}

export interface AnalysisMessage extends FrameAnalysis {}