/*
This file is part of Timelord.
Copyright 2005-2009 Jordan Reed

Timelord is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Timelord is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Timelord.  If not, see <http://www.gnu.org/licenses/>.
*/
package net.chaosserver.timelord.swingui.data;

import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Calendar;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.chaosserver.timelord.data.ExcelDataReaderWriter;
import net.chaosserver.timelord.data.TimelordData;
import net.chaosserver.timelord.data.TimelordDataException;
import net.chaosserver.timelord.swingui.JCalendarDialog;
import net.chaosserver.timelord.util.DateUtil;

/**
 * A version of the ExcelDataReaderWriter that allows a user
 * to select a date range.
 *
 * @author Jordan
 */
public class ExcelDataReaderWriterUI extends ExcelDataReaderWriter
        implements TimelordDataReaderWriterUI, ActionListener {

    /** Action Event for the "Forever" button. */
    public static final String ACTION_FOREVER =
        ExcelDataReaderWriterUI.class.getName() + ".ACTION_FOREVER";

    /** Action event for the "Last Week" button. */
    public static final String ACTION_LASTWEEK =
        ExcelDataReaderWriterUI.class.getName() + ".ACTION_LASTWEEK";

    /** Action Event for the Set Start Button. */
    public static final String ACTION_SETSTART =
        ExcelDataReaderWriterUI.class.getName() + ".ACTION_SETSTART";

    /** Action event for the Set End button. */
    public static final String ACTION_SETEND =
        ExcelDataReaderWriterUI.class.getName() + ".ACTION_SETEND";

    /** Action Event for the Cancel Button. */
    public static final String ACTION_CANCEL =
        ExcelDataReaderWriterUI.class.getName() + ".ACTION_CANCEL";

    /** Action even for the OK button. */
    public static final String ACTION_OK =
        ExcelDataReaderWriterUI.class.getName() + ".ACTION_OK";

    /** The parent frame for the modal dialog. */
    protected Frame parentFrame;

    /** Indicates if the dialog was cancelled. */
    protected boolean cancelled = true;

    /** Reference to the dialog box that is displayed for user config. */
    protected JDialog configDialog;

    /** The start date for writing. */
    protected Date startDate = null;

    /** The end date for writing. */
    protected Date endDate = null;

    /** The text field that displays the start date. */
    protected JTextField startDateField;

    /** The text field that displays the end date. */
    protected JTextField endDateField;

    /**
     * Does the writing of timelord data.  Based on the data range
     * that was set during configuration restricts the data that is
     * being written.
     *
     * @param timelordData the data file to be written out
     * @param outputFile the file for the data to be written to
     * @throws TimelordDataException indicates an error while writing out
     *         the file to disk.
     */
    public synchronized void writeTimelordData(
            TimelordData timelordData, File outputFile)
        throws TimelordDataException {

        if(!cancelled) {
            TimelordData timelordDataClone =
                (TimelordData) timelordData.clone();

            if(startDate != null || endDate != null) {
                timelordDataClone.removeTrackingOutsideRange(
                        startDate, endDate);
            }

            super.writeTimelordData(timelordDataClone, outputFile);
        }
    }

    /**
     * Creates a JDialog that will allow the user to configure options
     * for the reader/writer.  This allows the selection of a ran of time.
     *
     * @return the config dialog box to the display to the user
     */
    public JDialog getConfigDialog() {
        configDialog = new JDialog(getParentFrame(),
                "Configure Export", true);

        Container configContainer = configDialog.getContentPane();

        configContainer.setLayout(new GridLayout(2, 1));

        JButton newButton;

        JPanel rangePanel = new JPanel();
        rangePanel.setLayout(new FlowLayout());

        newButton = new JButton("Set Start");
        newButton.setActionCommand(ACTION_SETSTART);
        newButton.addActionListener(this);
        rangePanel.add(newButton);
        startDateField = new JTextField("Forever");
        startDateField.setEditable(false);
        startDateField.setColumns(10);
        rangePanel.add(startDateField);

        newButton = new JButton("Set End");
        newButton.setActionCommand(ACTION_SETEND);
        newButton.addActionListener(this);
        rangePanel.add(newButton);
        endDateField = new JTextField("Forever");
        endDateField.setEditable(false);
        endDateField.setColumns(10);
        rangePanel.add(endDateField);

        configContainer.add(rangePanel);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());

        newButton = new JButton("Forever");
        newButton.setActionCommand(ACTION_FOREVER);
        newButton.addActionListener(this);
        buttonPanel.add(newButton);

        newButton = new JButton("Last Week");
        newButton.setActionCommand(ACTION_LASTWEEK);
        newButton.addActionListener(this);
        buttonPanel.add(newButton);

        newButton = new JButton("Cancel");
        newButton.setActionCommand(ACTION_CANCEL);
        newButton.addActionListener(this);
        buttonPanel.add(newButton);
        configContainer.add(buttonPanel);

        newButton = new JButton("OK");
        newButton.setActionCommand(ACTION_OK);
        newButton.addActionListener(this);
        buttonPanel.add(newButton);
        configContainer.add(buttonPanel);

        return configDialog;
    }

    /**
     * Sets the parent frame to display the modal dialog.
     *
     * @param parentFrame the parent frame
     */
    public void setParentFrame(Frame parentFrame) {
        this.parentFrame = parentFrame;
    }

    /**
     * Gets the parent frame to display the modal dialog.
     *
     * @return the parent frame
     */
    protected Frame getParentFrame() {
        return this.parentFrame;
    }

    /**
     * Listens for actions on the dialog box for range and adjusts
     * the UI as required.
     *
     * @param evt the action event being performed.
     */
    public void actionPerformed(ActionEvent evt) {
        if(ACTION_SETSTART.equals(evt.getActionCommand())) {
            JCalendarDialog calendarDialog =
                new JCalendarDialog(getParentFrame(), this.startDate);
            calendarDialog.pack();
            calendarDialog.setLocationRelativeTo(getParentFrame());
            calendarDialog.setVisible(true);

            Date choosenDate = calendarDialog.getChoosenDate();

            if (choosenDate != null) {
                this.startDate = choosenDate;
                startDateField.setText(
                        DateUtil.BASIC_DATE_FORMAT.format(
                                choosenDate.getTime()));

            }
        } else if(ACTION_SETEND.equals(evt.getActionCommand())) {
                JCalendarDialog calendarDialog =
                    new JCalendarDialog(getParentFrame(), this.endDate);
                calendarDialog.pack();
                calendarDialog.setLocationRelativeTo(getParentFrame());
                calendarDialog.setVisible(true);

                Date choosenDate = calendarDialog.getChoosenDate();

                if (choosenDate != null) {
                    this.endDate = choosenDate;
                    endDateField.setText(
                            DateUtil.BASIC_DATE_FORMAT.format(
                                    choosenDate.getTime()));

                }
        } else if(ACTION_FOREVER.equals(evt.getActionCommand())) {
            startDate = null;
            startDateField.setText("Forever");
            endDate = null;
            endDateField.setText("Forever");
        } else if (ACTION_LASTWEEK.equals(evt.getActionCommand())) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
            DateUtil.trunc(calendar);
            endDate = calendar.getTime();
            endDateField.setText(
                    DateUtil.BASIC_DATE_FORMAT.format(calendar.getTime()));
            calendar.roll(Calendar.DAY_OF_YEAR, -6);
            startDate = calendar.getTime();
            startDateField.setText(
                    DateUtil.BASIC_DATE_FORMAT.format(calendar.getTime()));
        } else if(ACTION_OK.equals(evt.getActionCommand())) {
            cancelled = false;
            configDialog.setVisible(false);
        } else if(ACTION_CANCEL.equals(evt.getActionCommand())) {
            cancelled = true;
            configDialog.setVisible(false);

        }
    }


}
