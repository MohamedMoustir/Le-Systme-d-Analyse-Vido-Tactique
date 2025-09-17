

package util;

import java.sql.Connection;
import java.sql.DriverManager;
public class DatabaseConnection {
private static final String URL = "jdbc:mysql://localhost:3306/banque_db" ;
private static final String USER = "root";
private static final String PASSWORD = "root";
    

 public static Connection getConnection() {
	 try {
		 return DriverManager.getConnection(URL ,USER ,PASSWORD);
	 }catch(Exception e){
         throw new RuntimeException("Erreur connexion DB", e);
	 }
	 
 }
}
