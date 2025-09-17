package service;

import model.Compte;
import repository.CourantRepositoryIntefece;

public class CourantService {
	  private final CourantRepositoryIntefece courantRepositoryIntefece ;

	  public CourantService(CourantRepositoryIntefece courantRepositoryIntefece) {
	        this.courantRepositoryIntefece = courantRepositoryIntefece;
	    }
	  
	public void createCourant(Compte compte , double tauxInteret) {
		courantRepositoryIntefece.createCourant( compte ,  tauxInteret);
    }
}
