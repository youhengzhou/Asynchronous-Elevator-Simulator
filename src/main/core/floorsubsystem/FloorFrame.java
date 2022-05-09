package main.core.floorsubsystem;

import main.util.constants.BuildingConfigConstants;
import javax.swing.*;
import java.awt.*;

/**
 * This class implements a user interface for floor-related information.
 *
 * @author Tony Zeidan
 * @author Evan Lloyd
 * @author Shaopeng Liu
 * @author Leenesh Kumar
 * @author YouHeng Zhou
 * @author Dominic Motora
 */
public class FloorFrame extends JFrame implements FloorListener {

    /**
     * The maximum amount of logs the log box should contain.
     */
    public static final int MAX_LOGS = 100;

    /**
     * The panel containing floor light components.
     */
    private JPanel floorPanes;

    /**
     * The panel containing the log box.
     */
    private JScrollPane logPane;

    /**
     * The individual floor light components.
     */
    private FloorPanel[] floorPanels;

    /**
     * The individual log component.
     */
    private JTextArea log;

    /**
     * Default constructor for instances of FloorFrame.
     * Initializes a new frame with the necessary GUI components.
     */
    public FloorFrame() {
        super("Floor Subsystem Monitor");
        floorPanels = new FloorPanel[BuildingConfigConstants.NUMBER_OF_FLOORS];
        initialize();
    }

    /**
     * Initialize the frame components.
     */
    private void initialize() {

        getContentPane().setLayout(new BorderLayout());

        floorPanes = new JPanel(new GridLayout((int) Math.ceil(((double) floorPanels.length)/2.0), 2));

        FloorPanel fp = new FloorPanel(1, true, false);
        fp.update(true, false);
        floorPanels[0] = fp;
        floorPanes.add(floorPanels[0]);
        for (int i = 1; i < floorPanels.length - 1; i ++) {
            fp = new FloorPanel(i + 1, true, true);
            fp.update(true, false);
            fp.update(false, false);
            floorPanels[i] = fp;
            floorPanes.add(floorPanels[i]);
        }
        fp = new FloorPanel(floorPanels.length, false, true);
        fp.update(false, false);
        floorPanels[floorPanels.length-1] = fp;
        floorPanes.add(floorPanels[floorPanels.length-1]);


        log = new JTextArea();
        logPane = new JScrollPane(log, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        getContentPane().add(floorPanes, BorderLayout.CENTER);
        floorPanes.setPreferredSize(new Dimension(300, 600));
        getContentPane().add(logPane, BorderLayout.PAGE_END);
        logPane.setPreferredSize(new Dimension(300, 150));

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        //setPreferredSize(new Dimension());
        pack();
    }

    /**
     * Updates when receiving an event for a floor.
     *
     * @param floor The floor to update
     * @param up Whether it is the up light or down light
     * @param on Whether the light is on or off
     */
    @Override
    public void update(int floor, boolean up, boolean on) {
        floorPanels[floor-1].update(up, on);
    }

    /**
     * Update when receiving a log.
     *
     * @param logText The log to add
     */
    @Override
    public void updateLog(String logText) {
        addToLog(logText);
    }

    /**
     * Add to the log box.
     *
     * @param logText The text to add
     */
    private void addToLog(String logText) {
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
     * Clear the log box.
     */
    private void clearLog() {
        log.setText("");
        log.setCaretPosition(log.getText().length());
    }

    /**
     * Executable for the testing of the frame.
     *
     * @param args Not used
     */
    public static void main(String[] args) {
        FloorFrame ff = new FloorFrame();
        ff.setVisible(true);
    }
}
