package model;
import model.Compte;
import repository.EpargneInterface;
import repository.EpargneRepository;
import service.CompteService;
import service.EpargneService;
public class CompteEpargne extends Compte {
     private double tauxInteret ;
     //private CompteService compte = new CompteService();
     
     
    public  CompteEpargne(String code , double solde , String type_compte ,double tauxInteret) {
		super(code , solde,type_compte);
		this.tauxInteret = tauxInteret ;
		
	}
	
    
   public  void retirer(double montant) {};
    
   public boolean  calculerInteret() {
	   double interet = getSolde() * tauxInteret ;
	   setSolde(getSolde() + interet);
       return true;
   };

}
