package model;
import model.Compte;
import repository.EpargneInterface;
import repository.EpargneRepository;
import service.CompteService;
import service.EpargneService;
public class CompteCourant extends Compte {
     private double decouvert ;
     
     
    public  CompteCourant(String code , double solde , String type_compte ,double decouvert) {
		super(code , solde,type_compte);
		this.decouvert = decouvert ;
		
	}
	
    
   public  void retirer(double montant) {};
    
   public boolean  calculerInteret() {
	   return true;
   };

}
