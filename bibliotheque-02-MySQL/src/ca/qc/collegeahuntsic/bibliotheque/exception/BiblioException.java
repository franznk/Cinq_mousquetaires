
package ca.qc.collegeahuntsic.bibliotheque.exception;

/**
 * L'exception BiblioException est levée lorsqu'une transaction est inadéquate.
 * Par exemple -- livre inexistant
 */

public final class BiblioException extends Exception {
    public BiblioException(String message) {
        super(message);
    }
}