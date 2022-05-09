package main.exceptions;

/**
 * Custom error for incorrect accessing of a light on a floor.
 *
 * @author Tony Zeidan
 * @author Evan Lloyd
 * @author Shaopeng Liu
 * @author Leenesh Kumar
 * @author YouHeng Zhou
 * @author Dominic Motora
 */
public class FloorLightNotAccessibleException extends RuntimeException {

    /**
     * Default constructor for instances of FloorLightNotAccessibleException.
     * Initializes a new exception that corresponding to the direction of the light passed.
     *
     * @param up The direction light that was illegally accessed
     */
    public FloorLightNotAccessibleException(boolean up) {
        super(String.format("Can not access the %s light of a floor that does not have one.", up ? "up": "down"));
    }
}
