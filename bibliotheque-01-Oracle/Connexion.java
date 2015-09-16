import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Gestionnaire d'une connexion avec une BD relationnelle via JDBC.
 *
 * Ce programme ouvrir une connexion avec une BD via JDBC.
 * La m�thode serveursSupportes() indique les serveurs support�s.
 *
 * Pr�-condition
 *   le driver JDBC appropri� doit �tre accessible.
 *
 * Post-condition
 *   la connexion est ouverte en mode autocommit false et s�rialisable,
 *   (s'il est support� par le serveur).
 * </pre>
 */
public class Connexion {

    private Connection conn;

    /**
     * Ouverture d'une connexion en mode autocommit false et s�rialisable (si support�)
     * @param serveur serveur SQL de la BD
     * @bd nom de la base de donn�es
     * @user userid sur le serveur SQL
     * @pass mot de passe sur le serveur SQL
     */
    public Connexion(String serveur,
        String bd,
        String user,
        String pass) throws SQLException {
        Driver d;
        try {
            if(serveur.equals("local")) {
                /*
                 * Class.forName("com.mysql.jdbc.Driver");
                 * urlBD = "jdbc:mysql://localhost:3306/" + bd
                 * */

                d = (Driver) Class.forName("com.mysql.jdbc.Driver").newInstance();
                DriverManager.registerDriver(d);
                this.conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/"
                    + bd,
                    user,
                    pass);
            } else if(serveur.equals("distant")) {
                d = (Driver) Class.forName("oracle.jdbc.driver.OracleDriver").newInstance();
                DriverManager.registerDriver(d);
                this.conn = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:"
                    + bd,
                    user,
                    pass);
            }
            /*
            else if (serveur.equals("postgres"))
                {
                d = (Driver) Class.forName("org.postgresql.Driver").newInstance();
                DriverManager.registerDriver(d);
                conn = DriverManager.getConnection(
                    "jdbc:postgresql:" + bd,
                    user, pass);
                }
            else // access
                {
                d = (Driver) Class.forName("org.postgresql.Driver").newInstance();
                DriverManager.registerDriver(new sun.jdbc.odbc.JdbcOdbcDriver());
                conn = DriverManager.getConnection(
                    "jdbc:odbc:" + bd,
                    "", "");
                }
             */

            // mettre en mode de commit manuel
            this.conn.setAutoCommit(false);

            // mettre en mode s�rialisable si possible
            // (plus haut niveau d'integrit� l'acc�s concurrent aux donn�es)
            DatabaseMetaData dbmd = this.conn.getMetaData();
            if(dbmd.supportsTransactionIsolationLevel(Connection.TRANSACTION_SERIALIZABLE)) {
                this.conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
                System.out.println("Ouverture de la connexion en mode s�rialisable :\n"
                    + "Estampille "
                    + System.currentTimeMillis()
                    + " "
                    + this.conn);
            } else {
                System.out.println("Ouverture de la connexion en mode read committed (default) :\n"
                    + "Heure "
                    + System.currentTimeMillis()
                    + " "
                    + this.conn);
            }
        }// try

        catch(SQLException e) {
            throw e;
        } catch(Exception e) {
            e.printStackTrace(System.out);
            throw new SQLException("JDBC Driver non instanci�");
        }
    }

    /**
     *fermeture d'une connexion
     */
    public void fermer() throws SQLException {
        this.conn.rollback();
        this.conn.close();
        System.out.println("Connexion ferm�e"
            + " "
            + this.conn);
    }

    /**
     *commit
     */
    public void commit() throws SQLException {
        this.conn.commit();
    }

    /**
     *rollback
     */
    public void rollback() throws SQLException {
        this.conn.rollback();
    }

    /**
     *retourne la Connection jdbc
     */
    public Connection getConnection() {
        return this.conn;
    }

    /**
     * Retourne la liste des serveurs support�s par ce gestionnaire de connexions
     */
    public static String serveursSupportes() {
        return "local : MySQL install� localement\n"
            + "distant : Oracle install� au D�partement d'Informatique du Coll�ge Ahuntsic\n"
            + "postgres : Postgres install� localement\n"
            + "access : Microsoft Access install� localement et inscrit dans ODBC";
    }
}// Classe Connexion
