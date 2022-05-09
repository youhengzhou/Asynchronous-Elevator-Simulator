package main.core.scheduler;

/**
 * This interface provides a generalization for Scheduler views.
 *
 * @author Tony Zeidan
 * @author Evan Lloyd
 * @author Shaopeng Liu
 * @author Leenesh Kumar
 * @author YouHeng Zhou
 * @author Dominic Motora
 */
public interface SchedulerListener {

    /**
     * Every view must update upon receiving logs.
     *
     * @param time The timestamp
     * @param thNum The thread id
     * @param logText The log information
     */
    void update(String time, long thNum, String logText);
}
