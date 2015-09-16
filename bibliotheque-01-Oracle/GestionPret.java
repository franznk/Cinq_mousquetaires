import java.sql.Date;
import java.sql.SQLException;

/**
 * Gestion des transactions de reli�es aux pr�ts de livres
 * aux membres dans une biblioth�que.
 *
 * Ce programme permet de g�rer les transactions pr�ter,
 * renouveler et retourner.
 *
 * Pr�-condition
 *   la base de donn�es de la biblioth�que doit exister
 *
 * Post-condition
 *   le programme effectue les maj associ�es � chaque
 *   transaction
 * </pre>
 */

public class GestionPret {

    private Livre livre;

    private Membre membre;

    private Reservation reservation;

    private Connexion cx;

    /**
      * Creation d'une instance.
      * La connection de l'instance de livre et de membre doit �tre la m�me que cx,
      * afin d'assurer l'int�grit� des transactions.
      */
    public GestionPret(Livre livre,
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
      * Pret d'un livre � un membre.
      * Le livre ne doit pas �tre pr�t�.
      * Le membre ne doit pas avoir d�pass� sa limite de pret.
      */
    public void preter(int idLivre,
        int idMembre,
        String datePret) throws SQLException,
        BiblioException,
        Exception {
        try {
            /* Verfier si le livre est disponible */
            TupleLivre tupleLivre = livre.getLivre(idLivre);
            if(tupleLivre == null)
                throw new BiblioException("Livre inexistant: "
                    + idLivre);
            if(tupleLivre.idMembre != 0)
                throw new BiblioException("Livre "
                    + idLivre
                    + " deja prete a "
                    + tupleLivre.idMembre);

            /* V�rifie si le membre existe et sa limite de pret */
            TupleMembre tupleMembre = membre.getMembre(idMembre);
            if(tupleMembre == null)
                throw new BiblioException("Membre inexistant: "
                    + idMembre);
            if(tupleMembre.nbPret >= tupleMembre.limitePret)
                throw new BiblioException("Limite de pret du membre "
                    + idMembre
                    + " atteinte");

            /* V�rifie s'il existe une r�servation pour le livre */
            TupleReservation tupleReservation = reservation.getReservationLivre(idLivre);
            if(tupleReservation != null)
                throw new BiblioException("Livre r�serv� par : "
                    + tupleReservation.idMembre
                    + " idReservation : "
                    + tupleReservation.idReservation);

            /* Enregistrement du pret. */
            int nb1 = livre.preter(idLivre,
                idMembre,
                datePret);
            if(nb1 == 0)
                throw new BiblioException("Livre supprim� par une autre transaction");
            int nb2 = membre.preter(idMembre);
            if(nb2 == 0)
                throw new BiblioException("Membre supprim� par une autre transaction");
            cx.commit();
        } catch(Exception e) {
            cx.rollback();
            throw e;
        }
    }

    /**
      * Renouvellement d'un pret.
      * Le livre doit �tre pr�t�.
      * Le livre ne doit pas �tre r�serv�.
      */
    public void renouveler(int idLivre,
        String datePret) throws SQLException,
        BiblioException,
        Exception {
        try {
            /* Verifier si le livre est pr�t� */
            TupleLivre tupleLivre = livre.getLivre(idLivre);
            if(tupleLivre == null)
                throw new BiblioException("Livre inexistant: "
                    + idLivre);
            if(tupleLivre.idMembre == 0)
                throw new BiblioException("Livre "
                    + idLivre
                    + " n'est pas prete");

            /* Verifier si date renouvellement >= datePret */
            if(Date.valueOf(datePret).before(tupleLivre.datePret))
                throw new BiblioException("Date de renouvellement inferieure � la date de pret");

            /* V�rifie s'il existe une r�servation pour le livre */
            TupleReservation tupleReservation = reservation.getReservationLivre(idLivre);
            if(tupleReservation != null)
                throw new BiblioException("Livre r�serv� par : "
                    + tupleReservation.idMembre
                    + " idReservation : "
                    + tupleReservation.idReservation);

            /* Enregistrement du pret. */
            int nb1 = livre.preter(idLivre,
                tupleLivre.idMembre,
                datePret);
            if(nb1 == 0)
                throw new BiblioException("Livre supprime par une autre transaction");
            cx.commit();
        } catch(Exception e) {
            cx.rollback();
            throw e;
        }
    }

    /**
      * Retourner un livre pr�t�
      * Le livre doit �tre pr�t�.
      */
    public void retourner(int idLivre,
        String dateRetour) throws SQLException,
        BiblioException,
        Exception {
        try {
            /* Verifier si le livre est pr�t� */
            TupleLivre tupleLivre = livre.getLivre(idLivre);
            if(tupleLivre == null)
                throw new BiblioException("Livre inexistant: "
                    + idLivre);
            if(tupleLivre.idMembre == 0)
                throw new BiblioException("Livre "
                    + idLivre
                    + " n'est pas pr�t� ");

            /* Verifier si date retour >= datePret */
            if(Date.valueOf(dateRetour).before(tupleLivre.datePret))
                throw new BiblioException("Date de retour inferieure � la date de pret");

            /* Retour du pret. */
            int nb1 = livre.retourner(idLivre);
            if(nb1 == 0)
                throw new BiblioException("Livre supprim� par une autre transaction");

            int nb2 = membre.retourner(tupleLivre.idMembre);
            if(nb2 == 0)
                throw new BiblioException("Livre supprim� par une autre transaction");
            cx.commit();
        } catch(Exception e) {
            cx.rollback();
            throw e;
        }
    }
}
