
package ca.qc.collegeahuntsic.bibliotheque.service;

import java.sql.SQLException;
import ca.qc.collegeahuntsic.bibliotheque.dao.MembreDAO;
import ca.qc.collegeahuntsic.bibliotheque.dao.ReservationDAO;
import ca.qc.collegeahuntsic.bibliotheque.db.Connexion;
import ca.qc.collegeahuntsic.bibliotheque.dto.MembreDTO;
import ca.qc.collegeahuntsic.bibliotheque.exception.BiblioException;

/**
 * Gestion des transactions de reliées à la création et
 * suppresion de membres dans une bibliothèque.
 *
 * Ce programme permet de gérer les transaction reliées à la
 * création et suppresion de membres.
 *
 * Pré-condition
 *   la base de données de la bibliothèque doit exister
 *
 * Post-condition
 *   le programme effectue les maj associées à chaque
 *   transaction
 * </pre>
 */

public class MembreService {

    private Connexion cx;

    private MembreDAO membre;

    private ReservationDAO reservation;

    /**
     * Creation d'une instance
     */
    public MembreService(MembreDAO membre,
        ReservationDAO reservation) {

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
            /* Vérifie si le membre existe déja */
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
            /* Vérifie si le membre existe et son nombre de pret en cours */
            MembreDTO tupleMembre = this.membre.getMembre(idMembre);
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
                    + " a des réservations");
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
