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
package net.chaosserver.timelord.data;

import net.chaosserver.timelord.util.DateUtil;
import net.chaosserver.timelord.util.PropertyChangeSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;


/**
 * This class provides a materialized view of a particular day
 * and contains a collection of all the visibile TaskDays
 * for that date.
 */
public class TimelordDayView implements PropertyChangeListener {
    /** The logger. */
    private static Log log = LogFactory.getLog(TimelordDayView.class);

    /** Property Change Support. */
    protected PropertyChangeSupport propertyChangeSupport =
        new PropertyChangeSupport(this, 1);

    /** The main data object being shown. */
    protected TimelordData timelordData;

    /** The date this view represents. */
    protected Date viewDate;

    /** List of all the tasks. */
    protected List<TimelordTask> viewTaskList;

    /** Collection holding all the listeners for the various days. */
    protected Collection<TimelordTaskDay> taskDayListeners;

    /**
     * Constructs a new view for the given date.
     *
     * @param timelordData the main data file.
     * @param viewDate the date being viewed.
     */
    public TimelordDayView(TimelordData timelordData, Date viewDate) {
        setTimelordData(timelordData);
        this.viewDate = DateUtil.trunc(viewDate);
        buildTaskCollection();
    }

    /**
     * Setter for the timelordData object.
     *
     * @param timelordData the new value for this object
     */
    public void setTimelordData(TimelordData timelordData) {
        if (this.timelordData != null) {
            this.timelordData.removePropertyChangeListener(this);
        }

        this.timelordData = timelordData;

        if (timelordData != null) {
            this.timelordData.addPropertyChangeListener(this);
        }
    }

    /**
     * Getter for the timelordData property.
     *
     * @return value fo the property
     */
    public TimelordData getTimelordData() {
        return this.timelordData;
    }

    /**
     * Disposes of all the listeners for each timeTrackTaskDay.
     */
    protected synchronized void disposeListeners() {
        if (taskDayListeners != null) {
            Iterator<TimelordTaskDay> taskDayListenersIterator =
                taskDayListeners.iterator();

            while (taskDayListenersIterator.hasNext()) {
                TimelordTaskDay timelordTaskDay =
                    (TimelordTaskDay) taskDayListenersIterator.next();

                timelordTaskDay.removePropertyChangeListener(this);
            }
        }
    }

    /**
     * Rebuilds the task collection that is displayed to the user.
     */
    protected synchronized void buildTaskCollection() {
        // First remove all the existing listeners to avoid a memory leak
        disposeListeners();

        taskDayListeners = new ArrayList<TimelordTaskDay>();

        List<TimelordTask> newViewTaskList = new ArrayList<TimelordTask>();
        Collection<TimelordTask> taskCollection =
            getTimelordData().getTaskCollection();

        Iterator<TimelordTask> taskCollectionIterator =
            taskCollection.iterator();

        while (taskCollectionIterator.hasNext()) {
            TimelordTask timelordTask =
                (TimelordTask) taskCollectionIterator.next();

            TimelordTaskDay taskDay =
                timelordTask.getTaskDay(viewDate, true);

            taskDay.addPropertyChangeListener(this);
            taskDayListeners.add(taskDay);
            newViewTaskList.add(timelordTask);
        }

        viewTaskList = newViewTaskList;
        propertyChangeSupport.firePropertyChange(
            "viewTaskList",
            null,
            viewTaskList);
    }

    /**
     * Gets the task collection.
     *
     * @return a collection of TimeTrackTaskDays
     */
    public List<TimelordTask> getTaskCollection() {
        return viewTaskList;
    }

    /**
     * Returns the day start time.
     *
     * @return the day start time
     */
    public double getDayStartTime() {
        return getTimelordData().getDayStartTime();
    }

    /**
     * Gets the total amount of time that has been tracked today.
     *
     * @param includeNonExportable flag if the non-exportable time should
     *        be included.
     * @return the total amount of hours that has been tracked today.
     */
    public double getTotalTimeToday(boolean includeNonExportable) {
        if (log.isTraceEnabled()) {
            log.trace("Calculating total time for today.");
        }

        double totalTimeToday = 0;

        if (log.isTraceEnabled()) {
            log.trace("viewTaskList has size of [" + viewTaskList.size() + "]");
        }

        Iterator<TimelordTask> viewTaskListIterator = viewTaskList.iterator();

        while (viewTaskListIterator.hasNext()) {
            TimelordTask timelordTask =
                (TimelordTask) viewTaskListIterator.next();

            log.trace(
                    "[" + timelordTask.getTaskName() + "] checking "
                    + "for date of [" + viewDate + "]");

            if(includeNonExportable || timelordTask.isExportable()) {
                TimelordTaskDay taskDay = timelordTask.getTaskDay(viewDate);

                if (taskDay != null) {
                    double todayTime = taskDay.getHours();
                    totalTimeToday += todayTime;

                    if (log.isTraceEnabled()) {
                        log.trace(
                                "[" + timelordTask.getTaskName()
                                + "] has ["
                                + todayTime
                                + "] hours for today.  Total time = ["
                                + totalTimeToday
                                + "]");
                    }
                }
            } else {
                if (log.isTraceEnabled()) {
                    log.trace(
                            "[" + timelordTask.getTaskName()
                            + "] skipped non exportable task.");
                }

            }
        }

        if (log.isDebugEnabled()) {
            log.debug("Total time = [" + totalTimeToday + "]");
        }

        return totalTimeToday;
    }

    /**
     * Calculates the time that has been tracked to so far today. This is done
     * by adding the dayStartTime plus all the hours tracked today.
     *
     * @return the time today that has been tracked up to
     */
    public Date getTimeTrackedTo() {
        double dayStartTime = getDayStartTime();
        double totalTimeToday = getTotalTimeToday(true);
        double placeInDay = dayStartTime + totalTimeToday;
        double minutesSoFar = placeInDay * DateUtil.MINUTE_IN_HOUR;

        if (log.isTraceEnabled()) {
            log.trace(
                    "Setting placeInDay [" + placeInDay + "] = dayStartTime ["
                    + dayStartTime + "] - totalTimeToday [" + totalTimeToday
                    + "]");
        }

        Calendar today = Calendar.getInstance();
        DateUtil.trunc(today);

        today.add(Calendar.MINUTE, (int) minutesSoFar);

        return today.getTime();
    }

    /**
     * Gets the amount of time for today that has not yet been tracked. This is
     * calculated by getting the time tracked today and subtracting it fromt he
     * current time.
     *
     * @return the amount of time that is not yet tracked.
     */
    public double getUntrackedTime() {
        Date today = DateUtil.trunc(new Date());

        double hourOfDay =
            Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            + (Calendar.getInstance().get(Calendar.MINUTE)
            / DateUtil.MINUTE_IN_HOUR);

        double dayStartTime = getTimelordData().getDayStartTime();
        double totalTimeToday = 0;

        Iterator<TimelordTask> viewTaskListIterator =
            getTaskCollection().iterator();

        while (viewTaskListIterator.hasNext()) {
            TimelordTask timelordTask =
                (TimelordTask) viewTaskListIterator.next();

            TimelordTaskDay taskDay = timelordTask.getTaskDay(today);

            if (taskDay != null) {
                double todayTime = taskDay.getHours();
                totalTimeToday += todayTime;
            }
        }

        double untrackedTimeLeftToday =
            hourOfDay - dayStartTime - totalTimeToday;

        if (log.isInfoEnabled()) {
            log.info(
                    "untrackedTimeLeftToday [" + untrackedTimeLeftToday
                    + "] = hourOfDay [" + hourOfDay + "] - dayStartTime ["
                    + dayStartTime + "] - totalTimeToday [" + totalTimeToday
                    + "]");
        }

        return untrackedTimeLeftToday;
    }

    /**
     * Disposes of the view.
     */
    public void dispose() {
        disposeListeners();
        setTimelordData(null);
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

    /**
     * listens for property changes for hours or the task list to
     * update the view.
     *
     * @param evt the trigger event
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if (log.isDebugEnabled()) {
            log.debug(
                "Got propertyChangeEvent [" + evt.getPropertyName() + "]");
        }

        if ("hours".equals(evt.getPropertyName())) {
            propertyChangeSupport.firePropertyChange("totalTimeToday", 0, 1);
        } else if ("taskCollection".equals(evt.getPropertyName())) {
            buildTaskCollection();
        }
    }
}
