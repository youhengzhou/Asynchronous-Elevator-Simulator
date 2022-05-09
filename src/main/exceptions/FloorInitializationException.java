package main.exceptions;

/**
 * This class is an implementation of an error that may be thrown when incorrectly initializing a floor object.
 *
 * @author Tony Zeidan
 * @author Evan Lloyd
 * @author Shaopeng Liu
 * @author Leenesh Kumar
 * @author YouHeng Zhou
 * @author Dominic Motora
 *
 * @see main.core.floorsubsystem.Floor
 */
public class FloorInitializationException extends RuntimeException {
    /**
     * Default constructor for instances of FloorInitializationException.
     * Initializes a new exception that corresponding to the direction of the light passed.
     *
     * @param message The error message
     */
    public FloorInitializationException(String message) {
        super(message);
    }
}
