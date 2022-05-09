package main.core.elevatorsubsystem;

import main.util.constants.BuildingConfigConstants;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.*;
import java.awt.*;

/**
 * The general user interface for elevator subsystems.
 *
 * @author Tony Zeidan
 * @author Evan Lloyd
 * @author Shaopeng Liu
 * @author Leenesh Kumar
 * @author YouHeng Zhou
 * @author Dominic Motora
 */
public class ElevatorFrame extends JFrame implements ElevatorListener {

    /**
     * The individual info panels for each elevator.
     */
    private ElevatorPanel[] elevatorPanels;

    /**
     * The individual info log panels for each elevator.
     */
    private ElevatorLogPanel[] elevatorLogPanels;

    /**
     * The info container for all elevators.
     */
    private JPanel infoPanes;

    /**
     * The log container for all elevators.
     */
    private JTabbedPane logPanes;

    /**
     * The left panel container.
     */
    private JPanel leftPane;

    /**
     * The top panel container.
     */
    private JPanel topPane;

    /**
     * Constructor for instances of elevator frame.
     * Initializes a new frame with all the required components.
     */
    public ElevatorFrame() {
        super("Elevator Subsystem Monitor");
        getContentPane().setLayout(new BorderLayout());

        initialize();
    }

    /**
     * Initializes the elevator information component.
     */
    private void initElevatorInfos() {
        elevatorPanels = new ElevatorPanel[BuildingConfigConstants.NUMBER_OF_ELEVATORS];
        infoPanes = new JPanel(new GridLayout(1, elevatorPanels.length));

        for (int i = 0; i < elevatorPanels.length; i ++) {
            ElevatorPanel ep = new ElevatorPanel(i);
            elevatorPanels[i] = ep;
            infoPanes.add(ep);
        }
    }

    /**
     * Initializes the loggers component.
     */
    private void initLoggers() {
        // add logs to the bottom of the GUI
        logPanes = new JTabbedPane();
        elevatorLogPanels = new ElevatorLogPanel[BuildingConfigConstants.NUMBER_OF_ELEVATORS];
        for (int i = 0; i < elevatorLogPanels.length; i ++) {
            ElevatorLogPanel logger = new ElevatorLogPanel(i);
            elevatorLogPanels[i] = logger;
            logPanes.addTab(String.format("Elevator (%s)", i), elevatorLogPanels[i]);
        }
    }

    /**
     * Initializes the left panel component.
     */
    private void initLeft() {
        String[] rowNames = new String[] {
                "Floor",
                "Destination",
                "Queue",
                "State",
                "D.Err Flag",
                "M.Err Flag"
        };

        TableModel elevatorInfoModel = new DefaultTableModel(new String[] { " " }, rowNames.length);
        JTable elevatorInfoTable = new JTable(elevatorInfoModel);
        for (int i = 0; i < rowNames.length; i ++) {
            elevatorInfoTable.setValueAt(rowNames[i], i, 0);
        }

        // set the other tables to the selected index
        elevatorInfoTable.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
            public void valueChanged(ListSelectionEvent event) {
                // do some actions here, for example
                // print first column value from selected row
                int row = elevatorInfoTable.getSelectedRow();
                for (ElevatorPanel ep : elevatorPanels) {
                    ep.getSelectionModel().clearSelection();
                    ep.getSelectionModel().addSelectionInterval(row, row);
                }
            }
        });

        leftPane = new JPanel(new BorderLayout());
        JScrollPane tablePane = new JScrollPane(elevatorInfoTable);
        leftPane.add(tablePane, BorderLayout.PAGE_END);
        leftPane.setBorder(BorderFactory.createTitledBorder(""));

        elevatorInfoTable.setPreferredScrollableViewportSize(elevatorInfoTable.getPreferredSize());
        elevatorInfoTable.setFillsViewportHeight(true);
    }

    /**
     * Initializes the top panel component.
     */
    private void initTop() {
        topPane = new JPanel();
    }

    /**
     * Initializes the frame's components.
     */
    private void initialize() {

        // initialize components
        initElevatorInfos();
        initLoggers();
        initLeft();
        initTop();

        int wanted_width = 475* elevatorPanels.length;
        int wanted_height = 700;

        double epanel_perc_height = 0.70;
        double logpanel_perc_height = 0.25;

        infoPanes.setPreferredSize(new Dimension(wanted_width, (int) (wanted_height*epanel_perc_height)));
        logPanes.setPreferredSize(new Dimension(wanted_width, (int) (wanted_height*logpanel_perc_height)));

        // add components to frame
        getContentPane().add(topPane, BorderLayout.PAGE_START);
        getContentPane().add(leftPane, BorderLayout.LINE_START);
        getContentPane().add(infoPanes, BorderLayout.CENTER);
        getContentPane().add(logPanes, BorderLayout.PAGE_END);

        // set frame preferences
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(wanted_width, wanted_height));
        pack();
    }

    /**
     * Invoked when an update has been sent to the frame.
     * Update the contents of the frame's components.
     *
     * @param eso The status object to update with
     * @param logText The log text to update
     */
    @Override
    public void update(ElevatorStatusObj eso, String logText){
        ElevatorLogPanel log = elevatorLogPanels[eso.getElevatorID()];
        ElevatorPanel elevatorPanel = elevatorPanels[eso.getElevatorID()];
        log.addToLog(logText);
        elevatorPanel.setState(eso.getState());
        elevatorPanel.setSliderValue(eso.getFloor());
        elevatorPanel.updateTableInfo(eso);
        int[] plight = new int[eso.getPanelLights().size()];
        int c = 0;
        for (Integer p : eso.getPanelLights()) {
            plight[c] = p;
            c++;
        }
        elevatorPanel.setOnLights(plight);
    }

    /**
     * Executable to test the frame.
     *
     * @param args Not used
     */
    public static void main(String[] args) {
        ElevatorFrame ef = new ElevatorFrame();
        ef.setVisible(true);
    }
}
