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

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.toedter.calendar.JCalendar;


/**
 * A simple wrapper dialog around the JCalendar utility.
 */
public class JCalendarDialog extends JDialog implements ActionListener {
    /** Logger. */
    private static Log log = LogFactory.getLog(JCalendarDialog.class);

    /** Action Event for Select Button. */
    protected final String actionSelect =
        JCalendarDialog.class.getName() + ".ACTION_SELECT";

    /** Action Event for Cancel Button. */
    protected final String actionCancel =
        JCalendarDialog.class.getName() + ".ACTION_CANCEL";

    /** Holds the date the user has choosen. */
    protected Date choosenDate;

    /** Holds the JCalendar helper object. */
    protected JCalendar calendar;

    /**
     * Creates the dialog.
     *
     * @param owner the owner frame
     * @param selectedDate the date to have the calendar on, or null for
     *        current date.
     */
    public JCalendarDialog(Frame owner, Date selectedDate) {
        super(owner, "Pick Date", true);
        calendar = new JCalendar();

        if (selectedDate != null) {
            calendar.setDate(selectedDate);
        }

        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add(calendar, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        JButton selectButton = new JButton("Select");
        selectButton.setActionCommand(actionSelect);
        selectButton.addActionListener(this);
        buttonPanel.add(selectButton);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setActionCommand(actionCancel);
        cancelButton.addActionListener(this);
        buttonPanel.add(cancelButton);

        this.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * Gets the date selected by the choosen dialog or null if no date has
     * been selected.
     *
     * @return the choosen date or null
     */
    public Date getChoosenDate() {
        return this.choosenDate;
    }

    /**
     * Listens for the user selecting a button.
     *
     * @param evt the event performed.
     */
    public void actionPerformed(ActionEvent evt) {
        if (actionSelect.equals(evt.getActionCommand())) {
            choosenDate = calendar.getDate();

            if (log.isDebugEnabled()) {
                log.debug(
                    "JCalendar returned a date of [" + choosenDate + "]");
            }

            this.setVisible(false);
        } else if (actionCancel.equals(evt.getActionCommand())) {
            choosenDate = null;
            this.setVisible(false);
        }
    }
}
