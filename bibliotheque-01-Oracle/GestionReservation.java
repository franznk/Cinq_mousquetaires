import java.sql.Date;
import java.sql.SQLException;

/**
 * Gestion des transactions de reli�es aux r�servations de livres
 * par les membres dans une biblioth�que.
 *
 * Ce programme permet de g�rer les transactions r�server,
 * prendre et annuler.
 *
 * Pr�-condition
 *   la base de donn�es de la biblioth�que doit exister
 *
 * Post-condition
 *   le programme effectue les maj associ�es � chaque
 *   transaction
 * </pre>
 */

public class GestionReservation {

    private Livre livre;

    private Membre membre;

    private Reservation reservation;

    private Connexion cx;

    /**
      * Creation d'une instance.
      * La connection de l'instance de livre et de membre doit �tre la m�me que cx,
      * afin d'assurer l'int�grit� des transactions.
      */
    public GestionReservation(Livre livre,
        Membre membre,
        Reservation reservation) throws BiblioException {
        if(livre.getConnexion() != membre.getConnexion()
            || reservation.getConnexion() != membre.getConnexion())
            throw new BiblioException("Les instances de livre, de membre et de reservation n'utilisent pas la m�me connexion au serveur");
        this.cx = livre.getConnexion();
        this.livre = livre;
        this.membre = membre;
        this.reservation = reservation;
    }

    /**
      * R�servation d'un livre par un membre.
      * Le livre doit �tre pr�t�.
      */
    public void reserver(int idReservation,
        int idLivre,
        int idMembre,
        String dateReservation) throws SQLException,
        BiblioException,
        Exception {
        try {
            /* Verifier que le livre est pret� */
            TupleLivre tupleLivre = livre.getLivre(idLivre);
            if(tupleLivre == null)
                throw new BiblioException("Livre inexistant: "
                    + idLivre);
            if(tupleLivre.idMembre == 0)
                throw new BiblioException("Livre "
                    + idLivre
                    + " n'est pas prete");
            if(tupleLivre.idMembre == idMembre)
                throw new BiblioException("Livre "
                    + idLivre
                    + " deja prete a ce membre");

            /* V�rifier que le membre existe */
            TupleMembre tupleMembre = membre.getMembre(idMembre);
            if(tupleMembre == null)
                throw new BiblioException("Membre inexistant: "
                    + idMembre);

            /* Verifier si date reservation >= datePret */
            if(Date.valueOf(dateReservation).before(tupleLivre.datePret))
                throw new BiblioException("Date de reservation inferieure � la date de pret");

            /* V�rifier que la r�servation n'existe pas */
            if(reservation.existe(idReservation))
                throw new BiblioException("R�servation "
                    + idReservation
                    + " existe deja");

            /* Creation de la reservation */
            reservation.reserver(idReservation,
                idLivre,
                idMembre,
                dateReservation);
            cx.commit();
        } catch(Exception e) {
            cx.rollback();
            throw e;
        }
    }

    /**
      * Prise d'une r�servation.
      * Le livre ne doit pas �tre pr�t�.
      * Le membre ne doit pas avoir d�pass� sa limite de pret.
      * La r�servation doit la �tre la premi�re en liste.
      */
    public void prendreRes(int idReservation,
        String datePret) throws SQLException,
        BiblioException,
        Exception {
        try {
            /* V�rifie s'il existe une r�servation pour le livre */
            TupleReservation tupleReservation = reservation.getReservation(idReservation);
            if(tupleReservation == null)
                throw new BiblioException("R�servation inexistante : "
                    + idReservation);

            /* V�rifie que c'est la premi�re r�servation pour le livre */
            TupleReservation tupleReservationPremiere = reservation.getReservationLivre(tupleReservation.idLivre);
            if(tupleReservation.idReservation != tupleReservationPremiere.idReservation)
                throw new BiblioException("La r�servation n'est pas la premi�re de la liste "
                    + "pour ce livre; la premiere est "
                    + tupleReservationPremiere.idReservation);

            /* Verifier si le livre est disponible */
            TupleLivre tupleLivre = livre.getLivre(tupleReservation.idLivre);
            if(tupleLivre == null)
                throw new BiblioException("Livre inexistant: "
                    + tupleReservation.idLivre);
            if(tupleLivre.idMembre != 0)
                throw new BiblioException("Livre "
                    + tupleLivre.idLivre
                    + " deja pr�t� � "
                    + tupleLivre.idMembre);

            /* V�rifie si le membre existe et sa limite de pret */
            TupleMembre tupleMembre = membre.getMembre(tupleReservation.idMembre);
            if(tupleMembre == null)
                throw new BiblioException("Membre inexistant: "
                    + tupleReservation.idMembre);
            if(tupleMembre.nbPret >= tupleMembre.limitePret)
                throw new BiblioException("Limite de pr�t du membre "
                    + tupleReservation.idMembre
                    + " atteinte");

            /* Verifier si datePret >= tupleReservation.dateReservation */
            if(Date.valueOf(datePret).before(tupleReservation.dateReservation))
                throw new BiblioException("Date de pr�t inf�rieure � la date de r�servation");

            /* Enregistrement du pret. */
            if(livre.preter(tupleReservation.idLivre,
                tupleReservation.idMembre,
                datePret) == 0)
                throw new BiblioException("Livre supprim� par une autre transaction");
            if(membre.preter(tupleReservation.idMembre) == 0)
                throw new BiblioException("Membre supprim� par une autre transaction");
            /* Eliminer la r�servation */
            reservation.annulerRes(idReservation);
            cx.commit();
        } catch(Exception e) {
            cx.rollback();
            throw e;
        }
    }

    /**
      * Annulation d'une r�servation.
      * La r�servation doit exister.
      */
    public void annulerRes(int idReservation) throws SQLException,
        BiblioException,
        Exception {
        try {

            /* V�rifier que la r�servation existe */
            if(reservation.annulerRes(idReservation) == 0)
                throw new BiblioException("R�servation "
                    + idReservation
                    + " n'existe pas");

            cx.commit();
        } catch(Exception e) {
            cx.rollback();
            throw e;
        }
    }
}
