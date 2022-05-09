package main.core.elevatorsubsystem;

import main.util.constants.BuildingConfigConstants;
import main.util.constants.ElevatorConfigConstants;
import main.util.constants.SystemConfigConstants;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;

public class ElevatorPanel extends JPanel {

    /**
     * How many rows of input for the status table.
     */
    public static final int STATUS_ROWS = 6;

    /**
     * The model for the current floor slider.
     */
    private BoundedRangeModel currentFloorModel;

    /**
     * The individual models for each light button.
     */
    private ButtonModel[] lightModels;

    /**
     * The individual models for each state button.
     */
    private ButtonModel[] stateModels;

    /**
     * The elevator ID for this panel.
     */
    private final int elevatorID;

    /**
     * A list of state checkboxes.
     */
    private JCheckBox[] states;

    /**
     * A model for the status information table.
     */
    private TableModel elevatorInfoModel;

    /**
     * The status information table.
     */
    private JTable elevatorInfoTable;

    /**
     * The panel containing the status information table.
     */
    private JScrollPane tablePane;

    /**
     * The panel containing the state checkboxes.
     */
    private JPanel statesPanel;

    /**
     * The panel containing the light checkboxes.
     */
    private JPanel lightPanel;

    /**
     * The panel containing the current position slider.
     */
    private JPanel posPanel;

    /**
     * Constructor for instances of ElevatorPanel.
     * Initializes a new panel associated with the given elevator id.
     *
     * @param elevatorID The elevator ID for this panel
     */
    public ElevatorPanel(int elevatorID) {
        super();
        this.elevatorID = elevatorID;
        initializePanel();
    }

    /**
     * Initializes the state checkboxes.
     *
     * @param sname Whether to use official state names or english words
     */
    private void initStates(boolean sname) {
        states = new JCheckBox[ElevatorState.values().length];
        stateModels = new ButtonModel[states.length];

        statesPanel = new JPanel(new GridLayout(states.length, 1));

        for (int i = 0; i < states.length; i ++) {
            ElevatorState st = ElevatorState.values()[i];
            ButtonModel model = new DefaultButtonModel();
            String boxTitle = (sname) ? st.toString() : st.getRep();
            JCheckBox ch = new JCheckBox(boxTitle, false);
            ch.setEnabled(false);
            ch.setModel(model);
            stateModels[i] = model;
            states[i] = ch;
            statesPanel.add(states[i]);
        }
        statesPanel.setBorder(BorderFactory.createTitledBorder("State"));
    }

    /**
     * Initializes the lights checkboxes.
     */
    private void initLights() {
        double perf = (double) BuildingConfigConstants.NUMBER_OF_FLOORS / 2.0;
        int col1floors = (int) Math.ceil(perf);

        lightPanel = new JPanel(new GridLayout(col1floors,2));

        JCheckBox[] lights = new JCheckBox[BuildingConfigConstants.NUMBER_OF_FLOORS];
        lightModels = new ButtonModel[lights.length];

        for (int i = 1; i < BuildingConfigConstants.NUMBER_OF_FLOORS+1; i ++) {
            ButtonModel model = new DefaultButtonModel();
            JCheckBox ch = new JCheckBox(String.valueOf(i), false);
            ch.setSelected(false);
            ch.setModel(model);
            lightModels[i-1] = model;
            lights[i-1] = ch;
            lightPanel.add(lights[i-1]);
        }
        lightPanel.setBorder(BorderFactory.createTitledBorder("Panel Lights"));
    }

    /**
     * Initializes the position panel/slider.
     */
    private void initPosition() {

        currentFloorModel = new DefaultBoundedRangeModel(ElevatorConfigConstants.STARTING_FLOORS[elevatorID], 0, 1, BuildingConfigConstants.NUMBER_OF_FLOORS);

        posPanel = new JPanel();
        JSlider currentFloor = new JSlider(JSlider.VERTICAL);
        currentFloor.setModel(currentFloorModel);
        currentFloor.setMinorTickSpacing(1);
        currentFloor.setMajorTickSpacing(2);
        currentFloor.setPaintTicks(true);
        currentFloor.setPaintLabels(true);
        posPanel.add(currentFloor);
        posPanel.setBorder(BorderFactory.createTitledBorder("Position"));
    }

    /**
     * Initializes the status table/panel.
     */
    private void initStatus() {
        // create the table of elevator info
        String[] colNames = new String[] { String.format("Elevator (%s)", elevatorID) };

        elevatorInfoModel = new DefaultTableModel(colNames,STATUS_ROWS);
        elevatorInfoTable = new JTable(elevatorInfoModel);
        for (int i = 0; i < STATUS_ROWS; i ++) {
            elevatorInfoTable.setValueAt("", i, 0);
        }
        tablePane = new JScrollPane(elevatorInfoTable);
        elevatorInfoTable.setPreferredScrollableViewportSize(elevatorInfoTable.getPreferredSize());
        elevatorInfoTable.setFillsViewportHeight(true);
    }

    /**
     * Updates the table with the information in the given status object.
     *
     * @param eso The status with information for the table
     */
    public void updateTableInfo(ElevatorStatusObj eso){

        String currDest;
        if (eso.getDestinations().isEmpty())
            currDest = "NA";
        else
            currDest = String.valueOf(eso.getDestinations().peek());

        elevatorInfoTable.setValueAt(eso.getFloor(),0,0);
        elevatorInfoTable.setValueAt(currDest,1,0);
        elevatorInfoTable.setValueAt(eso.getDestinations(),2,0);
        elevatorInfoTable.setValueAt(eso.getState(),3,0);
        elevatorInfoTable.setValueAt(eso.isDoorErrorFlag(), 4, 0);
        elevatorInfoTable.setValueAt(eso.isMotorErrorFlag(), 5, 0);
    }

    /**
     * General initializer for the panel
     */
    private void initializePanel() {

        setLayout(new BorderLayout());
        JPanel topPane = new JPanel(new GridLayout(1,3));

        switch (SystemConfigConstants.DEBUG_MODE) {
            case NONE -> initStates(false);
            case DEBUG_LOW, DEBUG_HIGH -> initStates(true);
        }

        // initialize all components
        initLights();
        initPosition();
        initStatus();

        topPane.add(statesPanel);
        topPane.add(posPanel);
        topPane.add(lightPanel);
        add(topPane, BorderLayout.CENTER);
        add(tablePane, BorderLayout.PAGE_END);

        setBorder(BorderFactory.createTitledBorder(String.format("Elevator (%s)", elevatorID)));
    }

    /**
     * Sets the state of the panel.
     *
     * @param state The current state to display.
     */
    public void setState(ElevatorState state) {

        // turn all states off
        for (ButtonModel bm : stateModels) {
            bm.setSelected(false);
            bm.setEnabled(false);
        }

        boolean setto = false;

        switch (SystemConfigConstants.DEBUG_MODE) {
            case DEBUG_LOW, DEBUG_HIGH -> setto = true;
        }

        for (int i = 0; i < stateModels.length; i ++) {
            JCheckBox bm = states[i];
            if ((setto && bm.getText().equals(state.toString())) || (!setto && bm.getText().equals(state.getRep()))) {
                stateModels[i].setSelected(true);
                stateModels[i].setEnabled(true);
            }
        }


/*        if (state == null) {
            for (ButtonModel bm : stateModels) {
                bm.setSelected(false);
                bm.setEnabled(false);
            }
        } else {
            boolean setto = false;



            for (JCheckBox bm : states) {
                // check if the given state is referred to by this button
                if ((setto && bm.getText().equals(state.toString())) || (!setto && bm.getText().equals(state.getRep()))) {
                    bm.setSelected(true);
                    bm.setEnabled(true);
                } else {
                    bm.setSelected(false);
                    bm.setEnabled(false);
                }
            }
        }*/
    }

    /**
     * Turns on the i-th light on the panel.
     *
     * @param i The light to turn on
     */
    public void turnOnLight(int i) {
        lightModels[i].setEnabled(true);
        lightModels[i].setSelected(true);
    }

    /**
     * Turns off the i-th light on the panel.
     *
     * @param i The light to turn off
     */
    public void turnOffLight(int i) {
        lightModels[i].setEnabled(false);
        lightModels[i].setSelected(false);
    }

    /**
     * Sets the lights to on, and all others to off.
     *
     * @param onLights The lights to turn on (i+1)
     */
    public void setOnLights(int[] onLights) {
        for (int i = 0; i < lightModels.length; i ++) {
            turnOffLight(i);
        }

        for (int on : onLights) {
            turnOnLight(on - 1);
        }
    }

    /**
     * Gets the selection model for the status information table.
     *
     * @return The selection model.
     */
    public ListSelectionModel getSelectionModel() {
        return elevatorInfoTable.getSelectionModel();
    }

    /**
     * Sets the position value of the panel.
     *
     * @param floorNum The floor that the slider should be at
     */
    public void setSliderValue(int floorNum) {
        this.currentFloorModel.setValue(floorNum);
    }

    /**
     * Test executable for the panel.
     *
     * @param args Not used
     */
    public static void main(String[] args) {
        JFrame f = new JFrame("Test");
        f.getContentPane().add(new ElevatorPanel(0));
        f.pack();
        f.setVisible(true);
    }
}
