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
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.util.Calendar;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import net.chaosserver.timelord.data.TimelordData;
import net.chaosserver.timelord.util.DateUtil;
import net.chaosserver.timelord.util.PropertyChangeSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * A panel to display a CommonTaskPanel for an arbitrary day.
 *
 * @author Jordan Reed
 */
@SuppressWarnings("serial")
public class PreviousDayPanel extends JPanel implements ActionListener {
    /** Logger. */
    private static Log log = LogFactory.getLog(PreviousDayPanel.class);

    /** Action Event for Pick Day Button. */
    public static final String ACTION_PICK_DAY =
        PreviousDayPanel.class.getName() + ".ACTION_PICK_DAY";

    /** Property Change Support. */
    protected PropertyChangeSupport propertyChangeSupport =
        new PropertyChangeSupport(this, 1);

    /** The data file being manipulated. */
    protected TimelordData timelordData;

    /** The date being displayed. */
    protected Date displayDate;

    /** The common task panel used to render the various days. */
    protected CommonTaskPanel commonTaskPanel;

    /**
     * Constructs a new previous day panel.
     *
     * @param timelordData the timelord data to pull information from.
     */
    public PreviousDayPanel(TimelordData timelordData) {
        this.timelordData = timelordData;

        setLayout(new BorderLayout());

        JButton pickDayButton = new JButton("Select Day");
        pickDayButton.setActionCommand(ACTION_PICK_DAY);
        pickDayButton.addActionListener(this);
        add(pickDayButton, BorderLayout.NORTH);

        Calendar calendarDay = Calendar.getInstance();
        calendarDay.add(Calendar.DAY_OF_WEEK, -1);
        setDisplayDate(DateUtil.trunc(calendarDay.getTime()));
    }

    /**
     * Setting the display causes the panel to dispose of whatever is currently
     * being displayed and construct a new commonTaskPanel with the new date.
     *
     * @param displayDate date of information to display.
     */
    public void setDisplayDate(Date displayDate) {
        if (log.isInfoEnabled()) {
            log.info("Setting the display date to [" + displayDate + "]");
        }

        Date oldDisplayDate = this.displayDate;
        this.displayDate = DateUtil.trunc(displayDate);

        if (commonTaskPanel != null) {
            commonTaskPanel.dispose();
            this.remove(commonTaskPanel);
        }

        commonTaskPanel =
            new CommonTaskPanel(this.timelordData, displayDate);

        this.add(commonTaskPanel, BorderLayout.CENTER);
        this.doLayout();
        this.validate();

        propertyChangeSupport.firePropertyChange(
            "displayDate",
            oldDisplayDate,
            this.displayDate);
    }

    /**
     * Returns the display date if one has been set, or null.
     *
     * @return the display date or null
     */
    public Date getDisplayDate() {
        return this.displayDate;
    }

    /**
     * Listens for action from this panel. If the pick date button is choosen
     * displays a Calendar allowing a user to select a date for display.
     *
     * @param evt the event triggering things
     */
    public void actionPerformed(ActionEvent evt) {
        if (ACTION_PICK_DAY.equals(evt.getActionCommand())) {
            Frame ownerFrame =
                (Frame) SwingUtilities.getAncestorOfClass(Frame.class, this);
            JCalendarDialog calendarDialog =
                new JCalendarDialog(ownerFrame, getDisplayDate());

            calendarDialog.pack();

            Point ownerFrameLocation = ownerFrame.getLocation();
            ownerFrameLocation.setLocation(
                ownerFrameLocation.getX()
                    + LayoutConstants.CHILD_FRAME_X_OFFSET,
                ownerFrameLocation.getY()
                    + LayoutConstants.CHILD_FRAME_Y_OFFSET);

            calendarDialog.setLocation(ownerFrameLocation);
            calendarDialog.setVisible(true);

            Date choosenDate = calendarDialog.getChoosenDate();

            if (choosenDate != null) {
                setDisplayDate(choosenDate);
            }
        }
    }

    /**
     * Adds a property change listener against all properties in the file.
     *
     * @param listener the listener to add
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Removes a property change listener.
     *
     * @param listener the listener to remove
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }
}
