package main.exceptions;

/**
 * Custom error for incorrect ElevatorStatusObj initialization.
 *
 * @author Tony Zeidan
 * @author Evan Lloyd
 * @author Shaopeng Liu
 * @author Leenesh Kumar
 * @author YouHeng Zhou
 * @author Dominic Motora
 */
public class ElevatorStatusObjInitializationException extends RuntimeException {

    /**
     * Default constructor for instances of ElevatorStatusObjInitializationException.
     * Initializes a new exception with the corresponding message.
     *
     * @param errmsg The error message associated with the error
     */
    public ElevatorStatusObjInitializationException(String errmsg) {
        super(errmsg);
    }
}
