import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.StringTokenizer;

/**
 * Interface du syst�me de gestion d'une biblioth�que
 *
 * Ce programme permet d'appeler les transactions de base d'une
 * biblioth�que.  Il g�re des livres, des membres et des
 * r�servations. Les donn�es sont conserv�es dans une base de
 * donn�es relationnelles acc�d�e avec JDBC. Pour une liste des
 * transactions trait�es, voir la m�thode afficherAide().
 *
 * Param�tres
 * 0- site du serveur SQL ("local", "distant" ou "postgres")
 * 1- nom de la BD
 * 2- user id pour �tablir une connexion avec le serveur SQL
 * 3- mot de passe pour le user id
 * 4- fichier de transaction [optionnel]
 *           si non sp�cifi�, les transactions sont lues au
 *           clavier (System.in)
 *
 * Pr�-condition
 *   la base de donn�es de la biblioth�que doit exister
 *
 * Post-condition
 *   le programme effectue les maj associ�es � chaque
 *   transaction
 * </pre>
 */
public class Biblio {
    private static GestionBibliotheque gestionBiblio;

    private static boolean lectureAuClavier;

    /**
     * Ouverture de la BD,
     * traitement des transactions et
     * fermeture de la BD.
     */
    public static void main(String argv[]) throws Exception {
        // validation du nombre de param�tres
        if(argv.length < 4) {
            System.out.println("Usage: java Biblio <serveur> <bd> <user> <password> [<fichier-transactions>]");
            System.out.println(Connexion.serveursSupportes());
            return;
        }

        try {
            // ouverture du fichier de transactions
            // s'il est sp�cifi� comme argument
            lectureAuClavier = true;
            InputStream sourceTransaction = System.in;
            if(argv.length > 4) {
                sourceTransaction = new FileInputStream(argv[4]);
                lectureAuClavier = false;
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(sourceTransaction));

            gestionBiblio = new GestionBibliotheque(argv[0],
                argv[1],
                argv[2],
                argv[3]);
            traiterTransactions(reader);
        } catch(Exception e) {
            e.printStackTrace(System.out);
        } finally {
            gestionBiblio.fermer();
        }
    }

    /**
      * Traitement des transactions de la biblioth�que
      */
    static void traiterTransactions(BufferedReader reader) throws Exception {
        afficherAide();
        String transaction = lireTransaction(reader);
        while(!finTransaction(transaction)) {
            /* d�coupage de la transaction en mots*/
            StringTokenizer tokenizer = new StringTokenizer(transaction,
                " ");
            if(tokenizer.hasMoreTokens())
                executerTransaction(tokenizer);
            transaction = lireTransaction(reader);
        }
    }

    /**
      * Lecture d'une transaction
      */
    static String lireTransaction(BufferedReader reader) throws IOException {
        System.out.print("> ");
        String transaction = reader.readLine();
        /* echo si lecture dans un fichier */
        if(!lectureAuClavier)
            System.out.println(transaction);
        return transaction;
    }

    /**
      * D�codage et traitement d'une transaction
      */
    static void executerTransaction(StringTokenizer tokenizer) throws Exception {
        try {
            String command = tokenizer.nextToken();

            /* ******************* */
            /*         HELP        */
            /* ******************* */
            if("aide".startsWith(command))
                afficherAide();
            /* ******************* */
            /* ACQUERIR            */
            /* ******************* */
            else if("acquerir".startsWith(command))
                gestionBiblio.gestionLivre.acquerir(readInt(tokenizer) /* idLivre */,
                    readString(tokenizer) /* titre */,
                    readString(tokenizer) /* auteur */,
                    readDate(tokenizer) /* dateAcquisition */);
            /* ******************* */
            /* VENDRE              */
            /* ******************* */
            else if("vendre".startsWith(command))
                gestionBiblio.gestionLivre.vendre(readInt(tokenizer) /* idLivre */);
            /* ********************* */
            /* PRETER                */
            /* ********************* */
            else if("preter".startsWith(command))
                gestionBiblio.gestionPret.preter(readInt(tokenizer) /* idLivre */,
                    readInt(tokenizer) /* idMembre */,
                    readDate(tokenizer) /* dateEmprunt */);
            /* ******************* */
            /* RENOUVELER          */
            /* ******************* */
            else if("renouveler".startsWith(command))
                gestionBiblio.gestionPret.renouveler(readInt(tokenizer) /* idLivre */,
                    readDate(tokenizer) /* dateRenouvellement */);
            /* ********************* */
            /* RETOURNER             */
            /* ********************* */
            else if("retourner".startsWith(command))
                gestionBiblio.gestionPret.retourner(readInt(tokenizer) /* idLivre */,
                    readDate(tokenizer) /* dateRetour */);
            /* ********************* */
            /* INSCRIRE              */
            /* ********************* */
            else if("inscrire".startsWith(command))
                gestionBiblio.gestionMembre.inscrire(readInt(tokenizer) /* idMembre */,
                    readString(tokenizer) /* nom */,
                    readLong(tokenizer) /* tel */,
                    readInt(tokenizer) /* limitePret */);
            /* ******************* */
            /* DESINSCRIRE         */
            /* ******************* */
            else if("desinscrire".startsWith(command))
                gestionBiblio.gestionMembre.desinscrire(readInt(tokenizer) /* idMembre */);
            /* ******************* */
            /* RESERVER            */
            /* ******************* */
            else if("reserver".startsWith(command))
                gestionBiblio.gestionReservation.reserver(readInt(tokenizer) /* idReservation */,
                    readInt(tokenizer) /* idLivre */,
                    readInt(tokenizer) /* idMembre */,
                    readDate(tokenizer) /* dateReservation */);
            /* ******************* */
            /* PRENDRE RESERVATION */
            /* ******************* */
            else if("prendreRes".startsWith(command))
                gestionBiblio.gestionReservation.prendreRes(readInt(tokenizer) /* idReservation */,
                    readDate(tokenizer) /* dateReservation */);
            /* ******************* */
            /* ANNULER RESERVATION */
            /* ******************* */
            else if("annulerRes".startsWith(command))
                gestionBiblio.gestionReservation.annulerRes(readInt(tokenizer) /* idReservation */);
            /* *********************           */
            /* AFFICHER LA LISTE DE tous les livres  */
            /* *********************           */
            else if("listerLivres".startsWith(command))
                gestionBiblio.gestionInterrogation.listerLivres();
            /* *********************           */
            /* AFFICHER LA LISTE DES LIVRES DONT LE TITRE CONTIENT UN MOT DONNE   */
            /* *********************           */
            else if("listerLivresTitre".startsWith(command))
                gestionBiblio.gestionInterrogation.listerLivresTitre(readString(tokenizer) /* mot */);
            /* *********************           */
            /* commentaire : ligne d�butant par --   */
            /* *********************           */
            else if("--".startsWith(command)) {
            }// ne rien faire; c'est un commentaire
            /* ***********************   */
            /* TRANSACTION NON RECONNUEE */
            /* ***********************   */
            else
                System.out.println("  Transactions non reconnue.  Essayer \"aide\"");
        } catch(BiblioException e) {
            System.out.println("** "
                + e.toString());
        }
    }

    /** Affiche le menu des transactions accept�es par le syst�me */
    static void afficherAide() {
        System.out.println();
        System.out.println("Chaque transaction comporte un nom et une liste d'arguments");
        System.out.println("separes par des espaces. La liste peut etre vide.");
        System.out.println(" Les dates sont en format yyyy-mm-dd.");
        System.out.println("");
        System.out.println("Les transactions sont:");
        System.out.println("  aide");
        System.out.println("  exit");
        System.out.println("  acquerir <idLivre> <titre> <auteur> <dateAcquisition>");
        System.out.println("  preter <idLivre> <idMembre> <dateEmprunt>");
        System.out.println("  renouveler <idLivre> <dateRenouvellement>");
        System.out.println("  retourner <idLivre> <dateRetour>");
        System.out.println("  vendre <idLivre>");
        System.out.println("  inscrire <idMembre> <nom> <telephone> <limitePret>");
        System.out.println("  desinscrire <idMembre>");
        System.out.println("  reserver <idReservation> <idLivre> <idMembre> <dateReservation>");
        System.out.println("  prendreRes <idReservation> <dateEmprunt>");
        System.out.println("  annulerRes <idReservation>");
        System.out.println("  listerLivresRetard <dateCourante>");
        System.out.println("  listerLivresTitre <mot>");
        System.out.println("  listerLivres");
    }

    /**
     * V�rifie si la fin du traitement des transactions est
     * atteinte.
     */
    static boolean finTransaction(String transaction) {
        /* fin de fichier atteinte */
        if(transaction == null)
            return true;

        StringTokenizer tokenizer = new StringTokenizer(transaction,
            " ");

        /* ligne ne contenant que des espaces */
        if(!tokenizer.hasMoreTokens())
            return false;

        /* commande "exit" */
        String commande = tokenizer.nextToken();
        if(commande.equals("exit"))
            return true;
        else
            return false;
    }

    /** lecture d'une cha�ne de caract�res de la transaction entr�e � l'�cran */
    static String readString(StringTokenizer tokenizer) throws BiblioException {
        if(tokenizer.hasMoreElements())
            return tokenizer.nextToken();
        else
            throw new BiblioException("autre param�tre attendu");
    }

    /**
      * lecture d'un int java de la transaction entr�e � l'�cran
      */
    static int readInt(StringTokenizer tokenizer) throws BiblioException {
        if(tokenizer.hasMoreElements()) {
            String token = tokenizer.nextToken();
            try {
                return Integer.valueOf(token).intValue();
            } catch(NumberFormatException e) {
                throw new BiblioException("Nombre attendu � la place de \""
                    + token
                    + "\"");
            }
        } else
            throw new BiblioException("autre param�tre attendu");
    }

    /**
      * lecture d'un long java de la transaction entr�e � l'�cran
      */
    static long readLong(StringTokenizer tokenizer) throws BiblioException {
        if(tokenizer.hasMoreElements()) {
            String token = tokenizer.nextToken();
            try {
                return Long.valueOf(token).longValue();
            } catch(NumberFormatException e) {
                throw new BiblioException("Nombre attendu � la place de \""
                    + token
                    + "\"");
            }
        } else
            throw new BiblioException("autre param�tre attendu");
    }

    /**
      * lecture d'une date en format YYYY-MM-DD
      */
    static String readDate(StringTokenizer tokenizer) throws BiblioException {
        if(tokenizer.hasMoreElements()) {
            String token = tokenizer.nextToken();
            try {
                FormatDate.convertirDate(token);
                return token;
            } catch(ParseException e) {
                throw new BiblioException("Date en format YYYY-MM-DD attendue � la place  de \""
                    + token
                    + "\"");
            }
        } else
            throw new BiblioException("autre param�tre attendu");
    }
}//class
