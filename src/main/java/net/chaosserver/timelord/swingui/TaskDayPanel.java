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

import net.chaosserver.timelord.data.TimelordTask;
import net.chaosserver.timelord.data.TimelordTaskDay;
import net.chaosserver.timelord.util.DateUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.util.Date;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;


/**
 * Simple panel of information for a single task day.
 *
 * @author Jordan Reed
 */
@SuppressWarnings("serial")
public class TaskDayPanel extends JPanel implements ActionListener,
    PropertyChangeListener {
    /** The logger. */
    private static Log log = LogFactory.getLog(TaskDayPanel.class);

    /** Action Event for adding 15m. */
    private static final String ACTION_ADD_15 =
        TaskDayPanel.class.getName() + ".ACTION_ADD_15";

    /** Action Event for subtracting 15m. */
    private static final String ACTION_MINUS_15 =
        TaskDayPanel.class.getName() + ".ACTION_MINUS_15";

    /** Hours in a day. */
    private static final int HOURS_PER_DAY = 24;

    /**
     * Date this taskday refers to. This is needed if there is not
     * TimelordTaskDay for the date provided.
     */
    protected Date displayDate;

    /** The timelordTaskDay displayed. */
    protected TimelordTaskDay todayTaskDay;

    /** The surrounding task for the date. */
    protected TimelordTask timelordTask;

    /** The name of the task. */
    protected JLabel taskName;

    /** The label that shows how many hours are added for the day. */
    protected JLabel timeLabel;

    /** The add button. */
    protected JButton addButton;

    /** The minus button. */
    protected JButton minusButton;

    /**
     * Constructs a new instance for the given date. This is useful if there is
     * not TaskDay associated with the task for the given date. It will display
     * zero hours, but adding time will automatically create the task day.
     *
     * @param timelordTask the task that the panel represents
     * @param date the date the panel display
     */
    public TaskDayPanel(TimelordTask timelordTask, Date date) {
        this.displayDate = date;
        setTimelordTask(timelordTask);
        buildPanel();
    }

    /**
     * Constructs a new instance for a given task day. The task day should be a
     * child of the timelordTask.
     *
     * @param timelordTask the task that the panel represents
     * @param todayTaskDay the task day that the panel represents.
     */
    public TaskDayPanel(TimelordTask timelordTask,
        TimelordTaskDay todayTaskDay) {
        setTodayTaskDay(todayTaskDay);
        setTimelordTask(timelordTask);
        buildPanel();
    }

    /**
     * Builds the panel. This uses a GridBag to try and make formatting
     * grid-like between the unconnected rows.
     */
    protected void buildPanel() {
        GridBagLayout gridBagLayout = new GridBagLayout();
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        Insets defaultInsets = gridBagConstraints.insets;

        setLayout(gridBagLayout);

        taskName = new JLabel();

        if (timelordTask.isHidden()) {
            Font taskFont = taskName.getFont();
            Font italicFont =
                new Font(taskFont.getName(), Font.ITALIC, taskFont.getSize());
            taskName.setFont(italicFont);
        }

        updateTaskNameLabel();
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.weightx = LayoutConstants.HEAVY_WEIGHT;
        gridBagConstraints.insets = new Insets(
                0,
                LayoutConstants.SMALL_INSET,
                0,
                LayoutConstants.BIG_INSET);
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagLayout.addLayoutComponent(taskName, gridBagConstraints);
        add(taskName);

        minusButton = new JButton("-"
                + DateUtil.getSmallestTimeInMinutes()
                + "m");

        minusButton.setToolTipText(
                "Remove 15 minutes (0.25 hours) " + "of time from this task.");
        minusButton.setActionCommand(ACTION_MINUS_15);
        minusButton.addActionListener(this);
        gridBagConstraints.anchor = GridBagConstraints.CENTER;
        gridBagConstraints.insets = defaultInsets;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.weightx = 0;
        gridBagLayout.addLayoutComponent(minusButton, gridBagConstraints);
        add(minusButton);

        addButton = new JButton("+"
                + DateUtil.getSmallestTimeInMinutes()
                + "m");

        addButton.setToolTipText(
                "Add 15 minutes (0.25 hours) of " + "time from this task.");
        addButton.setActionCommand(ACTION_ADD_15);
        addButton.addActionListener(this);
        gridBagConstraints.anchor = GridBagConstraints.CENTER;
        gridBagConstraints.insets = defaultInsets;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.weightx = 0;
        gridBagLayout.addLayoutComponent(addButton, gridBagConstraints);
        add(addButton);

        timeLabel = new JLabel();

        if (todayTaskDay != null) {
            timeLabel.setText(
                DateUtil.formatHours(null, todayTaskDay.getHours()));
        } else {
            timeLabel.setText(DateUtil.formatHours(null, 0));
        }

        gridBagConstraints.anchor = GridBagConstraints.EAST;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.weightx = 0;
        gridBagConstraints.insets = new Insets(
                0,
                LayoutConstants.BIG_INSET,
                0,
                LayoutConstants.SMALL_INSET);
        gridBagLayout.addLayoutComponent(timeLabel, gridBagConstraints);
        add(timeLabel);

        enabledButtons();
    }

    /**
     * Setter for the todayTaskDay.
     *
     * @param todayTaskDay new value of todayTaskDay
     */
    public void setTodayTaskDay(TimelordTaskDay todayTaskDay) {
        if (this.todayTaskDay != null) {
            this.todayTaskDay.removePropertyChangeListener("hours", this);
            this.todayTaskDay.removePropertyChangeListener("note", this);
        }

        this.todayTaskDay = todayTaskDay;

        if (this.todayTaskDay != null) {
            todayTaskDay.addPropertyChangeListener("hours", this);
            todayTaskDay.addPropertyChangeListener("note", this);
        }
    }

    /**
     * Setter for the timelordTask.
     *
     * @param timelordTask new value of timelord task
     */
    public void setTimelordTask(TimelordTask timelordTask) {
        if (this.timelordTask != null) {
            this.timelordTask.removePropertyChangeListener("taskName", this);
        }

        this.timelordTask = timelordTask;

        if (this.timelordTask != null) {
            this.timelordTask.addPropertyChangeListener("taskName", this);
        }
    }

    /**
     * Getter from the timelordTask.
     *
     * @return timelord task
     */
    public TimelordTask getTimelordTask() {
        return this.timelordTask;
    }

    /**
     * Return the task day associated with today or null if there is no task
     * day.
     *
     * @return task day for today.
     */
    public TimelordTaskDay getTodayTaskDay() {
        return this.todayTaskDay;
    }

    /**
     * Returns the task day associated with today and potentially creates a new
     * one if there is not task day for today and create is set to true.
     *
     * @param create indicates if a new task day should be created if one does
     * not exists
     * @return the task day for today
     */
    public TimelordTaskDay getTodayTaskDay(boolean create) {
        if ((this.todayTaskDay == null) && create) {
            TimelordTaskDay timelordTaskDay =
                getTimelordTask().add(displayDate);

            setTodayTaskDay(timelordTaskDay);
        }

        return getTodayTaskDay();
    }

    /**
     * Sets the background color on the panel and the child objects.
     *
     * @param color the color to set
     */
    public void setBackground(Color color) {
        super.setBackground(color);

        if (addButton != null) {
            addButton.setBackground(color);
        }

        if (minusButton != null) {
            minusButton.setBackground(color);
        }
    }

    /**
     * Checks how many hours are added to any task and turns on/off the plus
     * minus buttons based on it.
     */
    public void enabledButtons() {
        double hours = 0;

        if (todayTaskDay != null) {
            hours = todayTaskDay.getHours();
        }

        timeLabel.setText(DateUtil.formatHours(null, hours));

        if (hours <= 0) {
            minusButton.setEnabled(false);
            addButton.setEnabled(true);
        } else if (hours >= HOURS_PER_DAY) {
            minusButton.setEnabled(true);
            addButton.setEnabled(false);
        } else {
            minusButton.setEnabled(true);
            addButton.setEnabled(true);
        }
    }

    /**
     * Disposes of the panel by removing property listeners.
     */
    public void dispose() {
        if (log.isDebugEnabled()) {
            log.debug("Removing listener from minusButton");
        }

        minusButton.removePropertyChangeListener(this);

        if (log.isDebugEnabled()) {
            log.debug("Removing listener from addButton");
        }

        addButton.removePropertyChangeListener(this);

        if (log.isDebugEnabled()) {
            log.debug("Removing listener from todayTaskDay");
        }

        setTodayTaskDay(null);

        if (log.isDebugEnabled()) {
            log.debug("Removing listener from timelordTask");
        }

        setTimelordTask(null);
    }

    /**
     * Updates the task name label with value of the task and a double asterix
     * if there is a note associated.
     */
    protected void updateTaskNameLabel() {
        String taskNameString = getTimelordTask().getTaskName();

        if ((getTodayTaskDay() != null)
                && (getTodayTaskDay().getNote() != null)) {
            taskNameString += " ** ";
        }

        taskName.setText(taskNameString);
    }

    /**
     * Listens for buttons on the actions and triggers proper handling.
     *
     * @param evt the action event
     */
    public void actionPerformed(ActionEvent evt) {
        if (ACTION_ADD_15.equals(evt.getActionCommand())) {
            getTodayTaskDay(true).
                addHours(DateUtil.getSmallestTimeIncremented());

        } else if (ACTION_MINUS_15.equals(evt.getActionCommand())) {
            getTodayTaskDay(true).
                addHours(-DateUtil.getSmallestTimeIncremented());
        }
    }

    /**
     * Listens from property changes on the TaskName or TaskDay and triggers
     * updates to the UI.
     *
     * @param evt the property event
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource().equals(getTodayTaskDay())) {
            if ("note".equals(evt.getPropertyName())) {
                updateTaskNameLabel();
            } else if ("hours".equals(evt.getPropertyName())) {
                enabledButtons();
            }
        } else if (evt.getSource().equals(getTimelordTask())) {
            if ("taskName".equals(evt.getPropertyName())) {
                updateTaskNameLabel();
            }
        }
    }
}
