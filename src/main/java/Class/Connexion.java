package Class;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Connexion {

    private static final String SERVER = "jdbc:mysql://localhost:3306/Restaurant";
    private static final String UTILISATEUR = "root";
    private static final String MOT_DE_PASSE = "";

    public static Connection etablirConnexion() {
        Connection connexion = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connexion = DriverManager.getConnection(SERVER, UTILISATEUR, MOT_DE_PASSE);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return connexion;
    }
}
