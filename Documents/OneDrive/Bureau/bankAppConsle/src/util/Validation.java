package util;

public class Validation {
	
	
	
	public static boolean validateCode(String code) {
		
	    return !code.matches("^CPT-\\d{5}$") || code == null || code.trim().isEmpty();
	}


    public static boolean validateSolde(double solde) {
			return solde < 0 || solde < 100 ;
		}
  
}
