package main.core.elevatorsubsystem;

import javax.swing.*;
import java.awt.*;

/**
 * This class implements a component that contains a log for elevators.
 *
 * @author Tony Zeidan
 * @author Evan Lloyd
 * @author Shaopeng Liu
 * @author Leenesh Kumar
 * @author YouHeng Zhou
 * @author Dominic Motora
 */
public class ElevatorLogPanel extends JPanel {

    /**
     * The maximum amount of log lines that the log area can contain.
     */
    private static final int MAX_LOGS = 100;

    /**
     * The log area component itself.
     */
    private JTextArea log;

    /**
     * The ID of the elevator that this log is associated with.
     */
    private final int elevatorID;

    /**
     * Constructor for instances of ElevatorLogPanel.
     * Initializes a log panel with the given Elevator ID.
     *
     * @param elevatorID The ID of the elevator that this log panel is associated with
     */
    public ElevatorLogPanel(int elevatorID) {
        super(new BorderLayout());
        this.elevatorID = elevatorID;

        initializePanel();
    }

    /**
     * Initialize the panel's components.
     */
    private void initializePanel() {

        JLabel lbl = new JLabel(String.format("Elevator %s's log:", elevatorID));
        log = new JTextArea();
        JScrollPane scroller = new JScrollPane(log, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        add(lbl, BorderLayout.NORTH);
        add(scroller, BorderLayout.CENTER);
    }

    /**
     * Add an item to the panel's log component.
     *
     * @param logText The text to add
     */
    public void addToLog(String logText) {
        if (logText != null) {
            if (log.getLineCount() == MAX_LOGS)
                clearLog();

            if (!logText.endsWith("\n"))
                logText += "\n";

            log.append(logText);
            log.setCaretPosition(log.getText().length() - 1);
        }
    }

    /**
     * Clears the panel's log component.
     */
    public void clearLog() {
        log.setText("");
        log.setCaretPosition(log.getText().length());
    }
}
