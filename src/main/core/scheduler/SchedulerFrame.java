package main.core.scheduler;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import java.awt.*;

/**
 * This class implements a user interface for the logging of Scheduler information.
 *
 * @author Tony Zeidan
 * @author Evan Lloyd
 * @author Shaopeng Liu
 * @author Leenesh Kumar
 * @author YouHeng Zhou
 * @author Dominic Motora
 */
public class SchedulerFrame extends JFrame implements SchedulerListener {

    /**
     * The maximum amount of logs the table should hold.
     */
    private static final int MAX_LOGS = 100;

    /**
     * The table of logs.
     */
    private JTable logTable;

    /**
     * The model for the table of logs.
     */
    private TableModel logModel;

    /**
     * Default constructor for instances of SchedulerFrame.
     * Initializes a new SchedulerFrame.
     */
    public SchedulerFrame() {
        super("Scheduler Monitor");
        initialize();
    }

    /**
     * Initialize the frame with components.
     */
    private void initialize() {

        getContentPane().setLayout(new BorderLayout());

        String[] colNames = new String[] {
                "Time",
                "Thread",
                "Log"
        };

        logModel = new DefaultTableModel(0, colNames.length);
        ((DefaultTableModel) logModel).setColumnIdentifiers(colNames);
        logTable = new JTable(logModel);


        initializeCols();

        logTable.setPreferredSize(new Dimension(logTable.getPreferredSize().width, 700));

        JScrollPane scrollPane = new JScrollPane(logTable, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        //logTable.setPreferredScrollableViewportSize(logTable.getPreferredSize());
        //logTable.setFillsViewportHeight(true);

        getContentPane().add(scrollPane, BorderLayout.CENTER);

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        pack();
    }

    /**
     * Make column sizes.
     */
    private void initializeCols() {
        int max_col1 = 170;
        int max_col2 = 80;
        int max_col3 = 600;

        TableColumnModel colModel = logTable.getColumnModel();

        colModel.getColumn(0).setPreferredWidth(max_col1);
        colModel.getColumn(1).setPreferredWidth(max_col2);
        colModel.getColumn(2).setPreferredWidth(max_col3);

        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer();
        cellRenderer.setHorizontalAlignment(JLabel.CENTER);
        colModel.getColumn(1).setCellRenderer(cellRenderer);
    }

    /**
     * Try to scroll to the last log.
     */
    private void scroll() {
        Rectangle cellRect = logTable.getCellRect(logTable.getRowCount()-1, logTable.getColumnCount(), true);
        logTable.scrollRectToVisible(cellRect);
    }

    /**
     * Update the log table with a new log.
     *
     * @param time The timestamp
     * @param thNum The thread id
     * @param logText The log information
     */
    @Override
    public void update(String time, long thNum, String logText) {
        if (logModel.getRowCount() == MAX_LOGS) {
            clearLogs();
        }
        String[] toAppend = new String[3];
        toAppend[0] = time;
        toAppend[1] = String.valueOf(thNum);
        toAppend[2] = logText;
        ((DefaultTableModel) logModel).addRow(toAppend);
        scroll();
    }

    /**
     * Clear log table.
     */
    public void clearLogs() {
        ((DefaultTableModel) logModel).setRowCount(0);
    }

    /**
     * Executable for the testing of the frame.
     *
     * @param args Not used
     */
    public static void main(String[] args) {
        SchedulerFrame sf = new SchedulerFrame();
        sf.setVisible(true);
    }
}
