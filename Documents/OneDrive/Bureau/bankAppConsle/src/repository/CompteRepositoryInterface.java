package repository;
import model.Compte;
public interface CompteRepositoryInterface {
   void createCompte(Compte compte);
   Compte findByCode( String code );
   boolean updateSolde(String code, double nouveauSolde);
}
