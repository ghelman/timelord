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
package net.chaosserver.timelord.swingui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

import net.chaosserver.timelord.data.TimelordData;

/**
 * Presents a dialog to the user to allow them to change the time
 * of day she began working.
 *
 * @author jordan
 */
@SuppressWarnings("serial")
public class StartTimeDialog extends JDialog implements ActionListener {
    /** Stores the reference to the data object. */
    protected TimelordData timelordData;

    /** Stores the reference to the hour input box. */
    protected JComboBox hourCombo;

    /** Stores the reference to the minute input box. */
    protected JComboBox minuteCombo;

    /** Stores the reference to the meridian (AM/PM) input box. */
    protected JComboBox meridianCombo;

    /** Action for the OK Button. */
    public static final String ACTION_OK =
        StartTimeDialog.class.getName() + ".ACTION_OK";

    /** Action for the Cancel Button. */
    public static final String ACTION_CANCEL =
        StartTimeDialog.class.getName() + ".ACTION_CANCEL";

    /** Hours in the a half day. */
    public static final int HOUR_IN_HALFDAY = 12;

    /** Increments of minutes. */
    public static final int MINUTE_INCREMENT = 15;

    /** Minute increments. */
    public static final double MINUTE_INCREMENTS = 0.25;

    /**
     * Constructs an new instance of the StartTimeDialog box.
     *
     * @param applicationFrame the frame of the application this
     *        dialog box is modal to.
     * @param useCurrentTime tells the dialog if it should use
     *        the current time.  Otherwise it uses the default
     *        time.
     * @param timelordData the data object to be affected.
     */
    public StartTimeDialog(JFrame applicationFrame,
            boolean useCurrentTime,
            TimelordData timelordData) {

        super(applicationFrame, "Set Day Start Time", true);

        this.timelordData = timelordData;

        JPanel spinnerPanel = new JPanel();
        Calendar now = Calendar.getInstance();

        String[] hourChoices = new String[] {"1", "2", "3", "4", "5",
                "6", "7", "8", "9", "10", "11", "12"};

        hourCombo = new JComboBox(hourChoices);

        int hourStart = now.get(Calendar.HOUR);
        if(hourStart > 0) {
            hourCombo.setSelectedIndex(hourStart - 1);
        } else {
            hourCombo.setSelectedIndex(hourChoices.length - 1);
        }
        spinnerPanel.add(hourCombo);

        String[] minuteChoices = new String[]{"00", "15", "30", "45"};
        minuteCombo = new JComboBox(minuteChoices);
        int minuteStart = now.get(Calendar.MINUTE);
        if(minuteStart >= MINUTE_INCREMENT + MINUTE_INCREMENT
                + MINUTE_INCREMENT) {

            minuteCombo.setSelectedIndex(minuteChoices.length - 1);
        } else if (minuteStart >= MINUTE_INCREMENT + MINUTE_INCREMENT) {
            minuteCombo.setSelectedIndex(2);
        } else if (minuteStart >= MINUTE_INCREMENT) {
            minuteCombo.setSelectedIndex(1);
        } else {
            minuteCombo.setSelectedIndex(0);
        }

        spinnerPanel.add(minuteCombo);

        // Create a list spinner
        meridianCombo = new JComboBox(new String[]{"AM", "PM"});
        int meridian = now.get(Calendar.AM_PM);
        meridianCombo.setSelectedIndex(
                meridian == Calendar.AM ? 0 : 1);
        spinnerPanel.add(meridianCombo);

        JButton okayButton = new JButton("OK");
        okayButton.setActionCommand(ACTION_OK);
        okayButton.addActionListener(this);
        spinnerPanel.add(okayButton);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setActionCommand(ACTION_CANCEL);
        cancelButton.addActionListener(this);
        spinnerPanel.add(cancelButton);

        add(spinnerPanel);
        pack();
        setLocationRelativeTo(applicationFrame);
    }

    /**
     * Action Listener that captures button clicks and handles them.
     *
     * @param evt the action event.
     */
    public void actionPerformed(ActionEvent evt) {
        if(ACTION_OK.equals(evt.getActionCommand())) {
            double newtime = 0;

            // The index for hours is from 0 to 11.
            // Increment by 1 to make it from 1 to 12, but then
            // take it mod 13, just in case it rolls over.
            int hourStart = (hourCombo.getSelectedIndex() + 1)
            % (HOUR_IN_HALFDAY);

            int minuteIndex = minuteCombo.getSelectedIndex();
            double minuteStart = minuteIndex * MINUTE_INCREMENTS;

            int meridianIndex = meridianCombo.getSelectedIndex();
            newtime = hourStart + minuteStart
                + (meridianIndex == 0 ? 0 : HOUR_IN_HALFDAY);

            timelordData.setDayStartTime(newtime);

            this.setVisible(false);
        } else if(ACTION_CANCEL.equals(evt.getActionCommand())) {
            this.setVisible(false);
        }
    }
}
