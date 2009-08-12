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

import net.chaosserver.timelord.data.TimelordDayView;
import net.chaosserver.timelord.util.DateUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.Calendar;
import java.util.Date;

import javax.swing.JLabel;
import javax.swing.JPanel;


/**
 * The summary panel on the bottom of the display that appears if the outer
 * panel is for today.
 */
@SuppressWarnings("serial")
public class SummaryPanel extends JPanel {
    /** The logger. */
    private static Log log = LogFactory.getLog(SummaryPanel.class);

    /** The date formatter for the summary row. */
    protected final DateFormat hourDisplay = new SimpleDateFormat("hh:mm aa");

    /** Shows the total time tracked today. */
    protected JLabel totalTimeLabel;

    /** Shows the time left today. */
    protected JLabel timeLeftLabel;

    /** Data to be displayed. */
    protected TimelordDayView timelordDayView;

    /** The date that is used. */
    protected Date dateDisplayed;

    /**
     * Public Constructor.
     *
     * @param timelordDayView the data object for the summary panel
     * @param dateDisplayed the date that will be displayed by the view
     */
    public SummaryPanel(TimelordDayView timelordDayView,
        Date dateDisplayed) {
        setTimelordDayView(timelordDayView);
        setDateDisplayed(dateDisplayed);
        buildSummaryPanel();
    }

    /**
     * Sets the DayView for the summary panel.
     *
     * @param timelordDayView the date view for the summary panel
     */
    public void setTimelordDayView(TimelordDayView timelordDayView) {
        this.timelordDayView = timelordDayView;
    }

    /**
     * Gets the DayView for the Summary Panel.
     *
     * @return the day view.
     */
    public TimelordDayView getTimelordDayView() {
        return this.timelordDayView;
    }

    /**
     * Setter for the display date of the summary panel.
     *
     * @param dateDisplayed the date to be displayed
     */
    public void setDateDisplayed(Date dateDisplayed) {
        this.dateDisplayed = dateDisplayed;
    }

    /**
     * Getter for the Display Date of the summary panel.
     *
     * @return the summary date
     */
    public Date getDateDisplayed() {
        return dateDisplayed;
    }

    /**
     * Constructs the panel.
     */
    protected void buildSummaryPanel() {
        GridBagLayout gridBagLayout = new GridBagLayout();
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        setLayout(gridBagLayout);

        if (isToday()) {
            JLabel remainderName = new JLabel("Remainder");
            gridBagConstraints.anchor = GridBagConstraints.SOUTHWEST;
            gridBagConstraints.weightx = LayoutConstants.HEAVY_WEIGHT;
            gridBagConstraints.insets = new Insets(
                    0,
                    LayoutConstants.SMALL_INSET,
                    0,
                    LayoutConstants.BIG_INSET);
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints.gridwidth = 1;
            gridBagLayout.addLayoutComponent(remainderName, gridBagConstraints);
            add(remainderName);

            timeLeftLabel = new JLabel();
            gridBagConstraints.anchor = GridBagConstraints.SOUTHEAST;
            gridBagConstraints.fill = GridBagConstraints.NONE;
            gridBagConstraints.weightx = 0;
            gridBagConstraints.insets = new Insets(
                    0,
                    LayoutConstants.BIG_INSET,
                    0,
                    LayoutConstants.SMALL_INSET);
            gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
            gridBagLayout.addLayoutComponent(timeLeftLabel, gridBagConstraints);
            add(timeLeftLabel);
        }

        JLabel taskName = new JLabel("Total");
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = LayoutConstants.HEAVY_WEIGHT;
        gridBagConstraints.insets = new Insets(
                0,
                LayoutConstants.SMALL_INSET,
                0,
                LayoutConstants.BIG_INSET);
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.gridwidth = 1;
        gridBagLayout.addLayoutComponent(taskName, gridBagConstraints);
        add(taskName);

        totalTimeLabel = new JLabel();
        gridBagConstraints.anchor = GridBagConstraints.NORTHEAST;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.weightx = 0;
        gridBagConstraints.insets = new Insets(
                0,
                LayoutConstants.BIG_INSET,
                0,
                LayoutConstants.SMALL_INSET);
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagLayout.addLayoutComponent(totalTimeLabel, gridBagConstraints);
        add(totalTimeLabel);

        updateTotalTimeLabel();
        updateTimeLeftLabel();
    }

    /**
     * Updates the text of the total time label in an overly complex
     * fashion.
     */
    public void updateTotalTimeLabel() {
        if (log.isDebugEnabled()) {
            log.debug("Updating TotalTimeLabel");
        }
        if (isToday()) {
            int hour =
                (int) Math.floor(getTimelordDayView().getDayStartTime());
            int minute =
                (int) Math.floor(
                        (getTimelordDayView().getDayStartTime() - hour)
                        * DateUtil.MINUTE_IN_HOUR);

            Calendar dayStart = Calendar.getInstance();
            dayStart.setTime(dateDisplayed);
            dayStart.set(Calendar.HOUR_OF_DAY, hour);
            dayStart.set(Calendar.MINUTE, minute);
            dayStart.set(Calendar.SECOND, 0);
            dayStart.set(Calendar.MILLISECOND, 0);

            totalTimeLabel.setText(
                    "(" + hourDisplay.format(dayStart.getTime()) + " - "
                    + hourDisplay.format(
                            getTimelordDayView().getTimeTrackedTo()) + ")  "
                    + TaskDayPanel.HOURS_FORMAT.format(
                            getTimelordDayView().getTotalTimeToday(true)));
        } else {
            totalTimeLabel.setText(
                    getTimelordDayView().getTotalTimeToday(true) + "");
        }
    }

    /**
     * Updates the text of the time left label in an overly complex fashion.
     */
    public void updateTimeLeftLabel() {
        if (log.isDebugEnabled()) {
            log.debug("Updating TimeLeftLabel");
        }

        if (isToday()) {
            timeLeftLabel.setText(
                    TaskDayPanel.HOURS_FORMAT.format(
                            getTimelordDayView().getUntrackedTime()));
        }
    }

    /**
     * Checks if the date being displayed is equal to the start of the current
     * time.
     *
     * @return indicates if the date is the start of the current time
     */
    public boolean isToday() {
        boolean result = false;

        Date todayDate = DateUtil.trunc(new Date());

        if (log.isTraceEnabled()) {
            log.trace(
                    "Testing today [" + todayDate + "] compared to "
                    + "dateDisplayed [" + getDateDisplayed() + "]");
        }

        if (todayDate.equals(getDateDisplayed())) {
            result = true;
        }

        return result;
    }
}
