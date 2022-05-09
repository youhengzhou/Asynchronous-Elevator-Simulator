package main.exceptions;

/**
 * Custom error for incorrect EventObj conversion.
 *
 * @author Tony Zeidan
 * @author Evan Lloyd
 * @author Shaopeng Liu
 * @author Leenesh Kumar
 * @author YouHeng Zhou
 * @author Dominic Motora
 */
public class EventObjConversionException extends RuntimeException {

    /**
     * Default constructor for instances of ElevatorStatusObjConversionException.
     * Initializes a new exception with the corresponding message.
     *
     * @param toPacket Whether the error happened during conversion to or from a packet
     * @param errmsg The error message associated with the error
     */
    public EventObjConversionException(boolean toPacket, String errmsg) {
        super(String.format(
                "There was a problem translating a EventObj %s.\n%s",
                toPacket ? "TO a packet" : "FROM a packet",
                errmsg
                ));
    }
}
