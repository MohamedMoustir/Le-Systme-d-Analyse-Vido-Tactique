package model;


public abstract class Compte {
  private String code ;
  private double solde;
  private String type_compte ;
  private int id ;
  
  public Compte( String code , double solde ,String type_compte ) {
	  this.code = code;
	  this.solde = solde;
	  this.type_compte  = type_compte ;
  }
  
  public Compte(int id, String code, double solde, String typeCompte) {
      this.id = id;
      this.code = code;
      this.solde = solde;
      this.type_compte = typeCompte;
  }
  
//Getters & Setters
  public int getid() {
      return id;
  }

  public void setid(int id) {
      this.id = id;
  }
  
  public String getCode() {
      return code;
  }

  public void setCode(String code) {
      this.code = code;
  }

  public double getSolde() {
      return solde;
  }

  public void setSolde(double solde) {
      this.solde = solde;
  }

  public String getType_compte() {
      return type_compte;
  }

  public void setType_compte(String type_compte) {
      this.type_compte = type_compte;
  }
  
  
  public abstract void retirer(double montant);
  public abstract boolean calculerInteret();
  //public abstract void afficherDetails();
}


















