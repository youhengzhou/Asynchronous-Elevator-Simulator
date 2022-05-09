package main.util;

import java.sql.Timestamp;

import main.util.constants.AnsiConstants;

/**
 * This class provides methods for the console printing of threads.
 *
 * @author Tony Zeidan
 * @author Evan Lloyd
 * @author Shaopeng Liu
 * @author Leenesh Kumar
 * @author YouHeng Zhou
 * @author Dominic Motora
 */
public class ThreadConsoleHelper {

	/**
	 * Creates a properly-formatted console string of a Thread-related event.
	 * 
	 * @param entity The thread this event is related to
	 * @param statement The context of this print
	 * @param item The item passed in with this print
	 */
    public static String createPrintString(String entity, String statement, Object item) {

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        if (item != null)
            return String.format("%s | %s %s\n%s\n", AnsiConstants.Blue.colorize(timestamp.toString()), AnsiConstants.Yellow.colorize(entity), statement, item);
        else
            return String.format("%s | %s %s\n", AnsiConstants.Blue.colorize(timestamp.toString()), AnsiConstants.Yellow.colorize(entity), statement);
    }


    /**
     * Creates a tiny string describing a thread.
     *
     * @param th The thread
     * @return The descriptor
     */
    public static String createThreadInfoString(Thread th) {
        return String.format("%s-%s-%s", th.getId(), th.getPriority(), th.getName());
    }

    /**
     * Creates a properly-formatted console print of a Thread-related event.
     *
     * @param entity The thread this event is related to
     * @param statement The context of this print
     * @param item The item passed in with this print
     */
    public static void createThreadPrint(String entity, String statement, Object item) {
        System.out.println(createPrintString(entity, statement, item));
    }

    /**
     * Creates a properly-formatted console print of a Thread-related event.
     *
     * @param entity The thread this event is related to
     * @param statement The context of this print
     * @param item The item passed in with this print
     */
    public static void createThreadPrint(Thread entity, String statement, Object item) {
        System.out.println(createPrintString(createThreadInfoString(entity), statement, item));
    }

    /**
     * Returns the current timestamp as a string.
     *
     * @return Timestamp in string form
     */
    public static String currTimestamp() {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        return timestamp.toString();
    }
}
