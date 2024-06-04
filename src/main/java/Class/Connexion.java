package Class;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Connexion {

    private static final String SERVER = "jdbc:mysql://sql.freedb.tech:3306/freedb_tables?allowPublicKeyRetrieval=true&useSSL=false";
    private static final String UTILISATEUR = "freedb_id22109572_cssurvivors";
    private static final String MOT_DE_PASSE = "3FjH5z4mpV!ap@#";

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