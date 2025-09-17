package repository;

import repository.CompteRepositoryInterface;
import model.Compte;
import util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import model.CompteEpargne;
import java.sql.Statement;
import model.CompteCourant;

public class CompteRepositoryimpl implements CompteRepositoryInterface{
	
	
	public void createCompte(Compte compte) {
		String sql = "INSERT INTO compte (code , solde , type_compte) VALUES (?,?,?)";
		
		try(Connection conn = DatabaseConnection.getConnection() ; PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			stmt.setString(1, compte.getCode());
			stmt.setDouble(2, compte.getSolde());
			stmt.setString(3, compte.getType_compte());
            
            int rows = stmt.executeUpdate();
            
            if (rows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1); 
                        compte.setid(id);     
                        System.out.println("Compte créé avec ID = " + id);
                    }
                }
            }
        
		}catch(Exception e){
			  e.printStackTrace();
			  
		}
	}
	
	
	public Compte findByCode(String code) {
		
	    String sql = "SELECT * FROM compte WHERE code = ?";
	    try (Connection conn = DatabaseConnection.getConnection();
	         PreparedStatement ps = conn.prepareStatement(sql)) {

	        ps.setString(1, code);
	        ResultSet rs = ps.executeQuery();

	        if (rs.next()) {
	            double solde = rs.getDouble("solde");
	            String type_compte = rs.getString("type_compte");

	            if (type_compte.equalsIgnoreCase("epargne")) {
	                double tauxInteret = 0.05;
	                return new CompteEpargne(code, solde,type_compte, tauxInteret);
	            } else if (type_compte.equalsIgnoreCase("courant")) {
	                double decouvert = -500; 
	               return new CompteCourant(code, solde,type_compte, decouvert);
	            } else {
	                System.out.println("Type de compte inconnu: " + type_compte);
	                return null;
	            }

	        } else {
	            System.out.println("Aucun compte trouvé avec ce code.");
	            return null;
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	        return null;
	    }
	}
      	 
	  
	      
	  public boolean updateSolde(String code, double solde) {

	        String sql = "UPDATE compte SET solde = ? WHERE code = ?";
	        try (Connection conn = DatabaseConnection.getConnection();
	             PreparedStatement ps = conn.prepareStatement(sql)) {
	            ps.setDouble(1, solde);
	            ps.setString(2, code);
	            int rows = ps.executeUpdate();
                System.out.println(" Inscription réussie !");
	            return rows > 0; 
	        } catch (SQLException e) {
	            e.printStackTrace();
	            return false;       
	        }
	       
	        } 	
}
