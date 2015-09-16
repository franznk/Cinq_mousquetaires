
package ca.qc.collegeahuntsic.bibliotheque;

import java.sql.SQLException;

/**
 * Gestion des transactions de reli�es � la cr�ation et
 * suppresion de membres dans une biblioth�que.
 *
 * Ce programme permet de g�rer les transaction reli�es � la
 * cr�ation et suppresion de membres.
 *
 * Pr�-condition
 *   la base de donn�es de la biblioth�que doit exister
 *
 * Post-condition
 *   le programme effectue les maj associ�es � chaque
 *   transaction
 * </pre>
 */

public class GestionMembre {

    private Connexion cx;

    private Membre membre;

    private Reservation reservation;

    /**
     * Creation d'une instance
     */
    public GestionMembre(Membre membre,
        Reservation reservation) {

        this.cx = membre.getConnexion();
        this.membre = membre;
        this.reservation = reservation;
    }

    /**
     * Ajout d'un nouveau membre dans la base de donnees.
     * S'il existe deja, une exception est levee.
     */
    public void inscrire(int idMembre,
        String nom,
        long telephone,
        int limitePret) throws SQLException,
        BiblioException,
        Exception {
        try {
            /* V�rifie si le membre existe d�ja */
            if(this.membre.existe(idMembre)) {
                throw new BiblioException("Membre existe deja: "
                    + idMembre);
            }

            /* Ajout du membre. */
            this.membre.inscrire(idMembre,
                nom,
                telephone,
                limitePret);
            this.cx.commit();
        } catch(Exception e) {
            this.cx.rollback();
            throw e;
        }
    }

    /**
     * Suppression d'un membre de la base de donnees.
     */
    public void desinscrire(int idMembre) throws SQLException,
    BiblioException,
    Exception {
        try {
            /* V�rifie si le membre existe et son nombre de pret en cours */
            TupleMembre tupleMembre = this.membre.getMembre(idMembre);
            if(tupleMembre == null) {
                throw new BiblioException("Membre inexistant: "
                    + idMembre);
            }
            if(tupleMembre.nbPret > 0) {
                throw new BiblioException("Le membre "
                    + idMembre
                    + " a encore des prets.");
            }
            if(this.reservation.getReservationMembre(idMembre) != null) {
                throw new BiblioException("Membre "
                    + idMembre
                    + " a des r�servations");
            }

            /* Suppression du membre */
            int nb = this.membre.desinscrire(idMembre);
            if(nb == 0) {
                throw new BiblioException("Membre "
                    + idMembre
                    + " inexistant");
            }
            this.cx.commit();
        } catch(Exception e) {
            this.cx.rollback();
            throw e;
        }
    }
}//class
