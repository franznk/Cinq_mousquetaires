
package ca.qc.collegeahuntsic.bibliotheque.dto;

import java.sql.Date;

/**
 * Permet de représenter un tuple de la table membre.
 *
 */

public class ReservationDTO {

    public int idReservation;

    public int idLivre;

    public int idMembre;

    public Date dateReservation;
}
