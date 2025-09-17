package service;

import repository.CompteRepositoryInterface;
import repository.CompteRepositoryimpl;
import repository.EpargneInterface;
import repository.EpargneRepository;
import repository.CourantRepository;
import repository.CourantRepositoryIntefece;
import model.Compte;
import util.Validation;
import model.CompteEpargne;
import service.EpargneService;
import service.CourantService;
import model.CompteCourant;


public class CompteService {
	private final CompteRepositoryInterface compteRepositoryInterface;
	
	private  CompteRepositoryimpl compte = new CompteRepositoryimpl();

	private Validation validation ;
	
	
	    //
	   EpargneInterface Epargne = new EpargneRepository();
	    EpargneService epargneService = new EpargneService(Epargne);
	    
	   //  
	    CourantRepositoryIntefece courantRepositoryIntefec = new CourantRepository();
	    CourantService courantService = new CourantService(courantRepositoryIntefec);
	    
	    
	
	public CompteService(CompteRepositoryInterface repo) {
	    this.compteRepositoryInterface = repo;
	}

	
	    public boolean createCompte(String code , double solde , int numType ) {

		
		if(validation.validateCode(code) || validation.validateSolde(solde) ) {
	        System.out.println("Le nom OR solde est invalide.");
           return false;
		}else{
			if(numType == 1) {
				Compte compte = new CompteEpargne(code,solde,"epargne",0.5);
				compte.calculerInteret();
				compteRepositoryInterface.createCompte(compte);
        		epargneService.createEpargne(compte, 0.5);
				 return true;
			}else if(numType == 2) {
				Compte compte = new CompteCourant(code,solde,"courant",0);
				compte.calculerInteret();
				compteRepositoryInterface.createCompte(compte);
				courantService.createCourant(compte, 0);
				 return true;
			}else {
				System.out.println("Le Type compte  est invalide.");
				return false;
			}
			  
			 }

		  }
	
	

	      public boolean retirer(String code , double montant) {
              

	    	  Compte compt = compte.findByCode(code);
	    	  
	    	  if (compt == null) {
	                System.out.println("Aucun compte trouvé avec ce code.");
	                return false;
	            }
	    	  boolean updated ;

	    	  double oldSolde = compt.getSolde();
                  double newSolde = oldSolde - montant ;
	            String type = compt.getType_compte();
                
	            	if(type.equals("epargne") && newSolde >= 0) {
	            		 updated = compte.updateSolde(code, newSolde);
	            		if (updated) {
	                    System.out.println("Retrait effectué avec succès. Nouveau solde: " + newSolde);
	                
	            }
	            	}else if(type.equals("courant") && compt.getSolde() - montant >= - 500){
		            	
				updated = compte.updateSolde(code, newSolde);
			    System.out.println("Retrait effectué avec succès. Nouveau solde: " + newSolde);
	
	            	}else {
		                System.out.println("Retrait impossible. Solde insuffisant. Solde actuel: " + newSolde);

	            	}
	          return true;
			  }
	
       

}
