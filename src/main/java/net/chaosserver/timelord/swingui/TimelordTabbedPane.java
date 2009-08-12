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

import net.chaosserver.timelord.data.TimelordData;
import net.chaosserver.timelord.util.DateUtil;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.Date;

import javax.swing.JTabbedPane;


/**
 * The tabbed pane is a basic two tab pane to show the current day and a
 * selection for to edit or view a pervious day.
 *
 * @author Jordan
 */
@SuppressWarnings("serial")
public class TimelordTabbedPane extends JTabbedPane
    implements PropertyChangeListener {
    /** The hour display is used to display the title of tab. */
    protected DateFormat hourDisplay = new SimpleDateFormat("MMM-dd-yyyy");

    /** Holds the common task panel. */
    protected CommonTaskPanel commonTaskPanel;

    /** Holds the TimelordData object. */
    protected TimelordData timelordData;

    /** Holds the previous day panel. */
    protected PreviousDayPanel previousDayPanel;

    /** Holds the charting panel. */
    protected ChartingPanel chartingPanel;

    /** Holds the date being shown in the common task panel. */
    protected Date dateOfToday;

    /**
     * Constructs the new pane.
     *
     * @param timelordData the data object represented
     */
    public TimelordTabbedPane(TimelordData timelordData) {
        this.timelordData = timelordData;

        this.dateOfToday = DateUtil.trunc(new Date());

        CommonTaskPanel taskPanel =
            new CommonTaskPanel(timelordData, dateOfToday);

        setCommonTaskPanel(taskPanel);

        add(hourDisplay.format(taskPanel.getDateDisplayed()), taskPanel);

        previousDayPanel = new PreviousDayPanel(timelordData);
        previousDayPanel.addPropertyChangeListener(this);

        add(
            hourDisplay.format(previousDayPanel.getDisplayDate()),
            previousDayPanel
        );

        // Add the Charting Panel Magic
        chartingPanel = new ChartingPanel(timelordData);
        add("Eye Candy", chartingPanel);
    }

    /**
     * Sets the common task panel.
     *
     * @param commonTaskPanel new value of the common task panel
     */
    public void setCommonTaskPanel(CommonTaskPanel commonTaskPanel) {
        this.commonTaskPanel = commonTaskPanel;
    }

    /**
     * Gets the current value of the common task panel.
     *
     * @return the common task panel
     */
    public CommonTaskPanel getCommonTaskPanel() {
        return this.commonTaskPanel;
    }

    /**
     * Catches a property on the display date and updates the
     * name of the tab.
     *
     * @param evt the change event.
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if ("displayDate".equals(evt.getPropertyName())) {
            setTitleAt(
                1, hourDisplay.format(previousDayPanel.getDisplayDate())
            );
        }
    }
}
