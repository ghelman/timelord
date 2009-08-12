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

import java.beans.PropertyChangeListener;

import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


/**
 * Representation of a task inside of timelord. Underneath the task object
 * are individual task days to indicate data for a day. For synchronization
 * purposes, anything that modifies the taskDayList property should be
 * synchronized on the object itself.
 */
public class TimelordTask implements Cloneable {
    /** Logger. */
    private static Log log = LogFactory.getLog(TimelordTask.class);

    /** Max Listeners. */
    private static final int MAX_LISTENERS = 3;

    /** Property Change Support. */
    protected PropertyChangeSupport propertyChangeSupport =
        new PropertyChangeSupport(this, MAX_LISTENERS);

    /** The name of the task. */
    protected String taskName;

    /** Flags if the task is exportable. */
    protected boolean exportable = true;

    /** Flag if the task has been marked hidden. */
    protected boolean hidden = false;

    /** List of task days associated for this task. */
    protected LinkedList<TimelordTaskDay> taskDayList;

    /** Default constructor. */
    public TimelordTask() {
        taskDayList = new LinkedList<TimelordTaskDay>();

        if (log.isDebugEnabled()) {
            log.debug("Created a new TimelordTask");
        }
    }

    /**
     * Creates a new Task with a given name.
     *
     * @param taskName the name of the new task
     */
    public TimelordTask(String taskName) {
        this();
        setTaskName(taskName);

        if (log.isDebugEnabled()) {
            log.debug("Created a new TimelordTask [" + taskName + "]");
        }
    }

    /**
     * Setter for the name of the task.
     *
     * @param taskName the new name of the task
     */
    public void setTaskName(String taskName) {
        String oldTaskName = this.taskName;
        this.taskName = taskName;
        propertyChangeSupport.firePropertyChange(
            "taskName",
            oldTaskName,
            this.taskName);
    }

    /**
     * Getter for the name of the task.
     *
     * @return the name of the task
     */
    public String getTaskName() {
        return this.taskName;
    }

    /**
     * Sets the exportable flag.
     *
     * @param exportable is the item exportable
     */
    public void setExportable(boolean exportable) {
        boolean oldExportable = this.exportable;
        this.exportable = exportable;
        propertyChangeSupport.firePropertyChange(
            "exportable",
            oldExportable,
            this.exportable);
    }

    /**
     * Gets the exportable flag. When exporting this object to any non-internal
     * format, this field should not be sent.
     *
     * @return if the item is exportable.
     */
    public boolean isExportable() {
        return this.exportable;
    }

    /**
     * Sets the hidden flag.
     *
     * @param hidden the hidden flag
     */
    public void setHidden(boolean hidden) {
        this.hidden = hidden;
        propertyChangeSupport.firePropertyChange(
            "hidden",
            !this.hidden,
            this.hidden);
    }

    /**
     * Gets the hidden flag.
     *
     * @return the hidden flag
     */
    public boolean isHidden() {
        return this.hidden;
    }

    /**
     * Sets the task day list for the object.
     *
     * @param taskDayList the new task day list
     */
    public synchronized void setTaskDayList(List<TimelordTaskDay> taskDayList) {
        if (taskDayList instanceof LinkedList) {
            this.taskDayList = (LinkedList<TimelordTaskDay>) taskDayList;
        } else {
            this.taskDayList = new LinkedList<TimelordTaskDay>(taskDayList);
        }
    }

    /**
     * Gets the list of task days. The list consists of TimelordTaskDay
     * objects. Only one object per day should be in the list and it is sorted
     * so that the most recent item comes at the start of the list.
     * This method is meant for serialization and no direct manipulations of the
     * list should be made.
     *
     * @return the task day list
     */
    public List<TimelordTaskDay> getTaskDayList() {
        return this.taskDayList;
    }

    /**
     * Checks if the task already has some hours tracked for today.
     *
     * @return if there is a task for today
     */
    public boolean isTodayPresent() {
        boolean todayPresent = false;
        List<TimelordTaskDay> taskList = getTaskDayList();

        if ((taskList != null) && !taskList.isEmpty()) {
            Date todayDate = DateUtil.trunc(new Date());

            TimelordTaskDay taskDay = (TimelordTaskDay) taskList.get(0);
            Date taskDayDate = taskDay.getDate();

            if (taskDayDate != null) {
                if (log.isTraceEnabled()) {
                    log.trace(
                            "Testing taskDayDate [" + this.getTaskName()
                            + "] [" + taskDayDate.getTime()
                            + "] against today [" + todayDate.getTime() + "]");
                }

                if (taskDayDate.equals(todayDate)
                        || taskDayDate.after(todayDate)) {
                    todayPresent = true;
                }
            } else {
                if (log.isWarnEnabled()) {
                    log.warn("Empty date field.  Removing taskDay.");
                    taskList.remove(taskDay);
                }
            }
        }

        return todayPresent;
    }

    /**
     * Adds a task day for today if one doesn't already exist.
     *
     * @return the newly added task day for today
     */
    public synchronized TimelordTaskDay addToday() {
        if (!isTodayPresent()) {
            TimelordTaskDay timelordTaskDay = new TimelordTaskDay();

            Date todayDate = DateUtil.trunc(new Date());

            timelordTaskDay.setDate(todayDate);
            timelordTaskDay.setHours(0);

            addTaskDay(timelordTaskDay);

            return timelordTaskDay;
        } else {
            return (TimelordTaskDay) taskDayList.getFirst();
        }
    }

    /**
     * Adds a task for particular date.
     *
     * @param date the date to add a task for
     * @return the newly added task day
     */
    public synchronized TimelordTaskDay add(Date date) {
        Date addDate = DateUtil.trunc(date);
        TimelordTaskDay timelordTaskDay = getTaskDay(addDate);

        if (timelordTaskDay == null) {
            timelordTaskDay = new TimelordTaskDay();
            timelordTaskDay.setDate(addDate);
            timelordTaskDay.setHours(0);

            addTaskDay(timelordTaskDay);
        }

        return timelordTaskDay;
    }

    /**
     * Gets the task day for the current date.
     *
     * @return the task day for the current date
     */
    public TimelordTaskDay getToday() {
        addToday();

        return (TimelordTaskDay) taskDayList.getFirst();
    }

    /**
     * Gets the task day for a particular day and returns null if there is no
     * taskDay for that date.
     *
     * @param dateToFind the date to find a taskDay for
     * @return the taskDay for the date given or a null
     */
    public TimelordTaskDay getTaskDay(Date dateToFind) {
        return getTaskDay(dateToFind, false);
    }

    /**
     * Gets the TaskDay object for the date to find.  If the create
     * flag is true, than a new object will be created if there is no
     * existing one.
     *
     * @param dateToFind the date to retrieve the task date object for.
     * @param create should the object be created if one doesn't exist
     * @return the TaskDate for the day, or a null if create is false
     *         and no object currently exists
     */
    public synchronized TimelordTaskDay getTaskDay(Date dateToFind,
        boolean create) {
        if (log.isDebugEnabled()) {
            log.debug(
                    "Searching [" + getTaskName() + "] for date of ["
                    + dateToFind + "]");
        }

        TimelordTaskDay timelordTaskDay = null;

        Iterator<TimelordTaskDay> taskDayListIterator =
            getTaskDayList().iterator();

        while ((timelordTaskDay == null) && taskDayListIterator.hasNext()) {
            TimelordTaskDay tempTimelordTaskDay =
                (TimelordTaskDay) taskDayListIterator.next();

            if (log.isDebugEnabled()) {
                log.debug(
                        "Testing tempTimelordTaskDay.getDate ["
                        + tempTimelordTaskDay.getDate()
                        + "] againt dateToFind of [" + dateToFind + "]");
            }

            if (dateToFind.equals(tempTimelordTaskDay.getDate())) {
                timelordTaskDay = tempTimelordTaskDay;
            } else if (dateToFind.after(tempTimelordTaskDay.getDate())) {
                // If the date to find is after the date we want, then we are
                // DONE.
                break;
            }
        }

        if ((timelordTaskDay == null) && create) {
            if (log.isDebugEnabled()) {
                log.debug(
                        "Creating new TimelordTaskDay for [" + dateToFind
                        + "]");
            }

            timelordTaskDay = add(dateToFind);
        }

        return timelordTaskDay;
    }

    /**
     * Adds a new task day object into the proper location in the list.
     *
     * @param timelordTaskDay the day to add
     */
    public synchronized void addTaskDay(TimelordTaskDay timelordTaskDay) {
        Date newTaskDate = timelordTaskDay.getDate();
        boolean added = false;

        for (int i = 0; i < taskDayList.size(); i++) {
            TimelordTaskDay tempTimelordTaskDay =
                (TimelordTaskDay) taskDayList.get(i);

            if (newTaskDate.after(tempTimelordTaskDay.getDate())) {
                taskDayList.add(i, timelordTaskDay);
                added = true;

                break;
            }
        }

        if (!added) {
            taskDayList.addLast(timelordTaskDay);
        }

        propertyChangeSupport.firePropertyChange("taskDayList", null,
            taskDayList);
    }

    /**
     * Sorts the task list so that newest is always first.
     */
    public synchronized void sort() {
        Collections.sort(getTaskDayList(), new DateTaskComparator());

        if (log.isTraceEnabled()) {
            log.trace(
                    "First element of [" + this.getTaskName() + "] is ["
                    + taskDayList.getFirst());
        }
    }

    /**
     * One of the cleanup methods to find any TaskDay that has zero hours and
     * remove it from the list.
     */
    public synchronized void removeEmpty() {
        for (int i = 0; i < taskDayList.size(); i++) {
            TimelordTaskDay tempTimelordTaskDay =
                (TimelordTaskDay) taskDayList.get(i);

            if ((i != 0) && (tempTimelordTaskDay.getHours() == 0)) {
                if (log.isDebugEnabled()) {
                    log.debug(
                            "Found a TaskDay with zero hours.  Removing ["
                            + tempTimelordTaskDay + "]");
                }

                taskDayList.remove(i);
                i--;
            }
        }
    }

    /**
     * One of the cleanup methods to find any TaskDay that has zero hours int
     * the future remove it from the list.
     */
    public void removeFuture() {
        for (int i = 0; i < taskDayList.size(); i++) {
            TimelordTaskDay tempTimelordTaskDay =
                (TimelordTaskDay) taskDayList.get(i);

            if(tempTimelordTaskDay.getDate().after(
                    DateUtil.trunc(new Date()))) {

                if (log.isWarnEnabled()) {
                    log.warn(
                            "Found a TaskDay that is in the future.  ["
                            + getTaskName() + "] [" + tempTimelordTaskDay
                            + "]");
                }

                taskDayList.remove(i);
                i--;
            }
        }
    }

    /**
     * Removes all task day items in the list that is before
     * the start date.
     *
     * @param startDate the date to remove items before
     * @param endDate the date to remove items after
     */
    public void removeTrackingOutsideRange(Date startDate, Date endDate) {
        for (int i = 0; i < taskDayList.size(); i++) {
            TimelordTaskDay tempTimelordTaskDay =
                (TimelordTaskDay) taskDayList.get(i);

            if(tempTimelordTaskDay.getDate().before(
                    DateUtil.trunc(startDate))) {

                taskDayList.remove(i);
                i--;
            } else if(tempTimelordTaskDay.getDate().after(
                    DateUtil.trunc(endDate))) {

                taskDayList.remove(i);
                i--;
            }
        }
    }

    /**
     * One of the cleanup methods to handle if the time zone is incorrect.
     */
    public synchronized void correctTimeZone() {
        for (int i = 0; i < taskDayList.size(); i++) {
            TimelordTaskDay tempTimelordTaskDay =
                (TimelordTaskDay) taskDayList.get(i);

            Calendar calendarDate = Calendar.getInstance();

            calendarDate.setTime(tempTimelordTaskDay.getDate());

            if ((calendarDate.get(Calendar.HOUR_OF_DAY) != 0)
                    || (calendarDate.get(Calendar.MINUTE) != 0)
                    || (calendarDate.get(Calendar.SECOND) != 0)
                    || (calendarDate.get(Calendar.MILLISECOND) != 0)) {
                if (log.isWarnEnabled()) {
                    log.warn(
                            "Found a TaskDay that is not set a midnight.  ["
                            + getTaskName() + "] [" + tempTimelordTaskDay
                            + "]");
                }

                // taskDayList.remove(i);
                // i--;
            }
        }
    }

    /**
     * Adds a property change listener for a property.
     *
     * @param propertyName property to listen for
     * @param listener listener
     */
    public void addPropertyChangeListener(String propertyName,
        PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
    }

    /**
     * Removes a property change listener.
     *
     * @param propertyName the property to listen to
     * @param listener listener
     */
    public void removePropertyChangeListener(String propertyName,
        PropertyChangeListener listener) {
        if (log.isTraceEnabled()) {
            log.trace(
                    "[" + getTaskName() + "] removing PropertyChangeListener ["
                    + listener + "]");
        }

        propertyChangeSupport.removePropertyChangeListener(propertyName,
            listener);
    }

    /**
     * Creates a copy of the object.
     *
     * @return a copy of the object
     */
    public TimelordTask clone() {
        TimelordTask timelordTaskClone = new TimelordTask();

        timelordTaskClone.taskName = this.taskName;
        timelordTaskClone.exportable = this.exportable;
        timelordTaskClone.hidden = this.hidden;

        LinkedList<TimelordTaskDay> taskDayListClone =
            new LinkedList<TimelordTaskDay>();

        Iterator<TimelordTaskDay> timelordTaskDayIterator =
            this.taskDayList.iterator();
        while(timelordTaskDayIterator.hasNext()) {
            taskDayListClone.add(timelordTaskDayIterator.next().clone());
        }

        timelordTaskClone.taskDayList = taskDayListClone;
        return timelordTaskClone;
    }

    /**
     * Returns the task name and is used for end-user display.
     *
     * @return the task name
     */
    public String toString() {
        return this.getTaskName();
    }

    /**
     * Date comparator to compare two TimelordTaskDay objects by date.
     */
    protected class DateTaskComparator implements Comparator<TimelordTaskDay> {
        /**
         * Compares two TimelordTaskDay objects by the date property.
         *
         * @param o1 the first
         * @param o2 the second
         * @return if o1's date is less than o2's
         */
        public int compare(TimelordTaskDay o1, TimelordTaskDay o2) {
            TimelordTaskDay t1 = (TimelordTaskDay) o1;
            TimelordTaskDay t2 = (TimelordTaskDay) o2;

            return t2.getDate().compareTo(t1.getDate());
        }
    }
}
