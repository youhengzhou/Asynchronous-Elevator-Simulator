package main.core.floorsubsystem;

import javax.swing.*;
import java.awt.*;

/**
 * This class is an implementation of a component that displays the lights for a floor.
 *
 * @author Tony Zeidan
 * @author Evan Lloyd
 * @author Shaopeng Liu
 * @author Leenesh Kumar
 * @author YouHeng Zhou
 * @author Dominic Motora
 */
public class FloorPanel extends JPanel {

    /**
     * The floor ID.
     */
    private final int floorID;

    /**
     * Component for up light.
     */
    private JCheckBox upLight;

    /**
     * Component for down light.
     */
    private JCheckBox downLight;

    /**
     * Default constructor for instances of FloorPanel.
     * Initializes the frame according to the given id and required lights.
     *
     * @param floorID The floor ID
     * @param hasUp Whether this panel has an up light or not
     * @param hasDown Whether this panel has a down light or not
     */
    public FloorPanel(int floorID, boolean hasUp, boolean hasDown) {
        this.floorID = floorID;
        initialize(hasUp, hasDown);
    }

    /**
     * Initializes the panel with components.
     *
     * @param hasUp Whether this panel has an up light or not
     * @param hasDown Whether this panel has a down light or not
     */
    private void initialize(boolean hasUp, boolean hasDown) {
        if (hasUp && hasDown)
            setLayout(new GridLayout(2, 1));
        else if ((hasUp && !hasDown) || (!hasUp && hasDown))
            setLayout(new GridLayout(1, 1));

        setBorder(BorderFactory.createTitledBorder(String.format("Floor (%s)", floorID)));

        if (hasUp) {
            upLight = new JCheckBox("Up");
            add(upLight);
        }
        if (hasDown) {
            downLight = new JCheckBox("Down");
            add(downLight);
        }
    }

    /**
     * Updates the one of the lights on this panel.
     *
     * @param up If it is the up light or down light
     * @param on Whether this light is to be on or off
     */
    public void update(boolean up, boolean on) {
        if (up) {
            if (on) {
                upLight.setEnabled(true);
                upLight.setSelected(true);
            } else {
                upLight.setEnabled(false);
                upLight.setSelected(false);
            }
        } else {
            if (on) {
                downLight.setEnabled(true);
                downLight.setSelected(true);
            } else {
                downLight.setEnabled(false);
                downLight.setSelected(false);
            }
        }
    }
}
