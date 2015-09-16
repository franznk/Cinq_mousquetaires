import java.sql.SQLException;

/**
 * Syst�me de gestion d'une biblioth�que
 *
 *<pre>
 * Ce programme permet de g�rer les transaction de base d'une
 * biblioth�que.  Il g�re des livres, des membres et des
 * r�servations. Les donn�es sont conserv�es dans une base de
 * donn�es relationnelles acc�d�e avec JDBC.
 *
 * Pr�-condition
 *   la base de donn�es de la biblioth�que doit exister
 *
 * Post-condition
 *   le programme effectue les maj associ�es � chaque
 *   transaction
 * </pre>
 */
public class GestionBibliotheque {
    public Connexion cx;

    public Livre livre;

    public Membre membre;

    public Reservation reservation;

    public GestionLivre gestionLivre;

    public GestionMembre gestionMembre;

    public GestionPret gestionPret;

    public GestionReservation gestionReservation;

    public GestionInterrogation gestionInterrogation;

    /**
      * Ouvre une connexion avec la BD relationnelle et
      * alloue les gestionnaires de transactions et de tables.
      * <pre>
      * 
      * @param serveur SQL
      * @param bd nom de la bade de donn�es
      * @param user user id pour �tablir une connexion avec le serveur SQL
      * @param password mot de passe pour le user id
      *</pre>
      */
    public GestionBibliotheque(String serveur,
        String bd,
        String user,
        String password) throws BiblioException,
        SQLException {
        // allocation des objets pour le traitement des transactions
        cx = new Connexion(serveur,
            bd,
            user,
            password);
        livre = new Livre(cx);
        membre = new Membre(cx);
        reservation = new Reservation(cx);
        gestionLivre = new GestionLivre(livre,
            reservation);
        gestionMembre = new GestionMembre(membre,
            reservation);
        gestionPret = new GestionPret(livre,
            membre,
            reservation);
        gestionReservation = new GestionReservation(livre,
            membre,
            reservation);
        gestionInterrogation = new GestionInterrogation(cx);
    }

    public void fermer() throws SQLException {
        // fermeture de la connexion
        cx.fermer();
    }
}
