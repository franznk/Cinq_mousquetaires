
package ca.qc.collegeahuntsic.bibliotheque.dto;

import java.sql.Date;

/**
 * Permet de représenter un tuple de la table livre.
 *
 */

public class LivreDTO {

    public int idLivre;

    public String titre;

    public String auteur;

    public Date dateAcquisition;

    public int idMembre;

    public Date datePret;
}
