package repository;

import repository.CompteRepositoryInterface;
import model.Compte;
import util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import model.CompteCourant;
import repository.CourantRepositoryIntefece;

public class CourantRepository implements  CourantRepositoryIntefece{

	
	 
	public void createCourant(Compte compte , double decouvert) {
		String sql = "INSERT INTO comptecourant ( compte_id , decouvert) VALUES (?,?)";
		
		try(Connection conn = DatabaseConnection.getConnection() ; PreparedStatement  stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, compte.getid());
			stmt.setDouble(2, decouvert);
            stmt.executeUpdate();
		}catch(Exception e){
			  e.printStackTrace();
			  
		}
	}
}
