DROP TABLE IF EXISTS membre CASCADE;
DROP TABLE IF EXISTS livre CASCADE;
DROP TABLE IF EXISTS reservation CASCADE;

CREATE TABLE membre (	idMembre 		INTEGER(3) check(idMembre > 0),
						nom 			varchar(10) NOT NULL,
						telephone 		BIGINT(10) ,
						limitePret      INTEGER(2) check(limitePret > 0 and limitePret <= 10) ,
						nbpret          INTEGER(2) default 0 check(nbpret >= 0) ,
						CONSTRAINT 		cleMembre PRIMARY KEY (idMembre),
						CONSTRAINT 		limiteNbPret check(nbpret <= limitePret)
					);
					
CREATE TABLE livre ( 	idLivre         INTEGER(3) check(idLivre > 0),
						titre           varchar(10) NOT NULL,
						auteur          varchar(10) NOT NULL,
						dateAcquisition date not null,
						idMembre        INTEGER(3),
						datePret        date,
						CONSTRAINT 		cleLivre PRIMARY KEY (idLivre),
						CONSTRAINT 		refPretMembre FOREIGN KEY (idMembre) REFERENCES membre (idMembre)
					);

CREATE TABLE reservation ( 	idReservation   INTEGER(3) ,
							idMembre        INTEGER(3) ,
							idLivre         INTEGER(3) ,
							dateReservation date ,
							CONSTRAINT 		cleReservation PRIMARY KEY (idReservation) ,
							CONSTRAINT 		cleCandidateReservation UNIQUE (idMembre,idLivre) ,
							CONSTRAINT 		refReservationMembre FOREIGN KEY (idMembre) REFERENCES membre (idMembre) ON DELETE CASCADE,
							CONSTRAINT 		refReservationLivre FOREIGN KEY (idLivre) REFERENCES livre (idLivre) ON DELETE CASCADE
						);