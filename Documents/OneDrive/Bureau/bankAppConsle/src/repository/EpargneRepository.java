package repository;

import repository.CompteRepositoryInterface;
import model.Compte;
import util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import model.CompteEpargne;
import repository.EpargneInterface;

public class EpargneRepository implements EpargneInterface{

	public void createEpargne(Compte compte , double tauxInteret) {
		String sql = "INSERT INTO compteepargne ( compte_id , tauxInteret) VALUES (?,?)";
		
		try(Connection conn = DatabaseConnection.getConnection() ; PreparedStatement  stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, compte.getid());
			stmt.setDouble(2, tauxInteret);
            stmt.executeUpdate();
		}catch(Exception e){
			  e.printStackTrace();
			  
		}
	}
}
