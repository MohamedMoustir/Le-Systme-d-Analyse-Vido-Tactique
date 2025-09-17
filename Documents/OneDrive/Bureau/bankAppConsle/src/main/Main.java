package main;

import java.util.Scanner;
import service.CompteService;
import repository.CompteRepositoryInterface;
import repository.CompteRepositoryimpl;
import model.CompteEpargne;
import service.EpargneService;
import repository.EpargneInterface;
import repository.EpargneRepository;

public class Main {
    public static void main(String[] args) {
    Scanner sc = new Scanner(System.in);
    
    CompteRepositoryInterface  repo = new CompteRepositoryimpl();
    CompteService compteService = new CompteService(repo);
    
    EpargneInterface Epargne = new EpargneRepository();
    EpargneService epargneService = new EpargneService(Epargne);
    
    boolean isRunning = true;
     while(isRunning) {
    	 System.out.println("====================================");
    	 System.out.println("          COMPTE BANK         ");
    	 System.out.println("====================================");
    	 System.out.println(" [1]  Create Compte");
    	 System.out.println(" [2]  retirer");
    	 System.out.println(" [3]  Exit");
    	 System.out.println("====================================");
    	 System.out.print(" Enter your choice: ");
    	 int choix = sc.nextInt();
    	 sc.nextLine();
    	 switch(choix) {
    	  case 1:
              System.out.println(" CODE : commence par CPT- suivi de 5 chiffres");
              String code = sc.nextLine();
              System.out.println("SOLDE : Le solde doit être au minimum de 100");
              double solde = sc.nextDouble();
              sc.nextLine();
              System.out.println("TYPE COMPTE");
         	  System.out.println(" [1]  Epargne");
        	  System.out.println(" [3]  Courant");
              int numType = Integer.parseInt(sc.nextLine());
      		

            	if (compteService.createCompte(code, solde, numType)) {
                  System.out.println(" Inscription réussie !");
                  
              } else {
                  System.out.println(" Erreur inscription. Vérifiez vos données ou email déjà utilisé.");
              }  
            
              break;

          case 2:
        	  System.out.println(" CODE : commence par CPT- suivi de 5 chiffres");
               code = sc.nextLine();
              System.out.println("SOLDE : ENTER your moantat");
              double moantat = sc.nextDouble();
              if (compteService.retirer(code, moantat)) {
                  System.out.println("HH");

              } else {
                  System.out.println(" Erreur inscription. Vérifiez vos données ou email déjà utilisé.");
              }
                            
              break;

          case 3:
              System.out.println(" Bye!");
              isRunning = false; 
              break;
          case 4:
              
              break;
          default:
              System.out.println(" Invalid choice, try again!");
      }

    	 }
    	 
    	 sc.close();
     }
     
     
    
}
