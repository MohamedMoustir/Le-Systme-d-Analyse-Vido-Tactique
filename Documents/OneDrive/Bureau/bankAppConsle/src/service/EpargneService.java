package service;

import model.Compte;
import repository.EpargneInterface;
public class EpargneService {
	  private final EpargneInterface epargneInterface ;

	  public EpargneService(EpargneInterface epargneInterface) {
	        this.epargneInterface = epargneInterface;
	    }
	  
	public void createEpargne(Compte compte , double tauxInteret) {
		epargneInterface.createEpargne( compte ,  tauxInteret);
    }
}
