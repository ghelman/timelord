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

import net.chaosserver.timelord.util.PropertyChangeSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

/**
 * This is the data object used to hold data for Timelord. It is a JavaBean
 * meant to be serialized through the XML encoder/decoder and stores all
 * information for a users particular time throughout history.
 *
 * @author Jordan Reed
 */
@SuppressWarnings("serial")
public class TimelordData implements Serializable,
            PropertyChangeListener, Cloneable {

    /** The logger. */
    private static Log log = LogFactory.getLog(TimelordData.class);

    /** The default day start time. */
    public static final int DEFAULT_DAY_START_TIME = 8;

    /** Property Change Support object. */
    protected PropertyChangeSupport propertyChangeSupport =
        new PropertyChangeSupport(this, 5);

    /** The RW used to persist the data. */
    protected TimelordDataReaderWriter timelordReaderWriter =
        new XmlDataReaderWriter();

    /** Holds the list of TimelordTasks objects of this data. */
    protected LinkedList<TimelordTask> taskCollection;

    /** The start time preference. */
    protected double dayStartTime = DEFAULT_DAY_START_TIME;

    /** The time zone associated with the data. */
    protected TimeZone timeZone;

    /**
     * Default constructor for an empty piece of data.
     */
    public TimelordData() {
        taskCollection = new LinkedList<TimelordTask>();
        resetTaskListeners();
    }

    /**
     * Sets the ReaderWriter to use when storing the data.
     *
     * @param timelordReaderWriter readerwriter to use
     */
    public void setTimelordReaderWriter(
        TimelordDataReaderWriter timelordReaderWriter) {
        this.timelordReaderWriter = timelordReaderWriter;
    }

    /**
     * Writes out this object using the default Reader/Writer.
     *
     * @throws TimelordDataException if there is an error writing the data.
     */
    public synchronized void write() throws TimelordDataException {
        if (log.isTraceEnabled()) {
            log.trace("Writing file using default writer.");
        }

        timelordReaderWriter.writeTimelordData(
            this,
            timelordReaderWriter.getDefaultOutputFile());
    }

    /**
     * Gets the number of TimeTrackTaskDays inside the timelord collection.
     *
     * @return number of tasks in the taskCollection
     */
    public int getTaskCollectionSize() {
        return taskCollection.size();
    }

    /**
     * Gets the task collection.
     *
     * @return a collection of TimeTrackTaskDays
     */
    public List<TimelordTask> getTaskCollection() {
        return this.taskCollection;
    }

    /**
     * Sets the task collection which is a list of TimelordTasks.
     * Generally this should only be called by the serialization engines.
     *
     * @param taskCollection a collection of TimeTrackTaskDays
     */
    public void setTaskCollection(List<TimelordTask> taskCollection) {
        if (taskCollection instanceof LinkedList) {
            this.taskCollection = (LinkedList<TimelordTask>) taskCollection;
        } else {
            this.taskCollection = new LinkedList<TimelordTask>(taskCollection);
        }

        propertyChangeSupport.firePropertyChange(
            "taskCollection",
            null,
            this.taskCollection);
        resetTaskListeners();
    }

    /**
     * Gets a list of TimelordTask objects that have been marked as hidden.
     *
     * @return List of TimelordTask objects that have been marked as hidden
     */
    public List<TimelordTask> getHiddenTasks() {
        List<TimelordTask> hiddenTaskNames = new ArrayList<TimelordTask>();
        Collection<TimelordTask> taskCollection = getTaskCollection();
        Iterator<TimelordTask> taskCollectionIterator =
            taskCollection.iterator();

        while (taskCollectionIterator.hasNext()) {
            TimelordTask timelordTask =
                (TimelordTask) taskCollectionIterator.next();

            if (timelordTask.isHidden()) {
                hiddenTaskNames.add(timelordTask);
            }
        }

        return hiddenTaskNames;
    }

    /**
     * Returns the day start time.
     *
     * @return the day start time
     */
    public double getDayStartTime() {
        return this.dayStartTime;
    }

    /**
     * Sets the default start time. This time isn't tracked on a day by day
     * basis and is just used as a convenient way to store this information that
     * is separated from the view.
     *
     * @param dayStartTime the time to start the day
     */
    public void setDayStartTime(double dayStartTime) {
        double oldDayStartTime = this.dayStartTime;
        this.dayStartTime = dayStartTime;
        propertyChangeSupport.firePropertyChange(
            "dayStartTime",
            new Double(oldDayStartTime),
            new Double(this.dayStartTime));
    }

    /**
     * Gets back the time zone associated with all the data.
     *
     * @return the timezone associated with all the data
     */
    public TimeZone getTimeZone() {
        return this.timeZone;
    }

    /**
     * Sets the timezone to be assocaited with all of the data.
     *
     * @param timeZone the new timezone to set
     */
    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    /**
     * Sorts the task collection using the default comparator. This causes the
     * task list to be sorted alphabetically.
     */
    public void sortTaskCollection() {
        List<TimelordTask> taskCollection = getTaskCollection();
        Collections.sort(taskCollection, new TaskNameComparator());

        if (log.isDebugEnabled()) {
            log.debug("Firing [taskCollection] property change event");
        }

        propertyChangeSupport.firePropertyChange(
            "taskCollection",
            null,
            this.taskCollection);
    }

    /**
     * Create a new task with the given name and add it to the list.
     *
     * @param taskName the name of the task to create and add
     * @return instance of the task that has been added to the data object
     */
    public TimelordTask addTask(String taskName) {
        // TODO: check if a task of this name exists
        TimelordTask timelordTask = new TimelordTask(taskName);
        timelordTask.addToday();
        addTask(timelordTask);

        return timelordTask;
    }

    /**
     * Adds a task to the list.
     *
     * @param timelordTask the task to add.
     */
    public void addTask(TimelordTask timelordTask) {
        taskCollection.add(timelordTask);
        resetTaskListeners();
        sortTaskCollection(); // fires a change
    }

    /**
     * Fires the event indicating how much untracked time is left
     * for the day.
     *
     * @param untrackedTimeLeftToday the amount of untracked time today
     *        as a double.
     */
    public void fireUntrackedTimeLeftToday(double untrackedTimeLeftToday) {
        if (log.isDebugEnabled()) {
            log.debug(
                    "Firing [untrackedTimeLeftToday] with value of ["
                    + (int) untrackedTimeLeftToday + "]");
        }

        propertyChangeSupport.firePropertyChange(
            "untrackedTimeLeftToday",
            -1,
            (int) untrackedTimeLeftToday);
    }

    /**
     * Deletes a task from the task collection.
     *
     * @param timelordTask the name of the task to remove.
     * @return indicates if the task passed in was actually in the list and
     * removed.
     */
    public boolean removeTask(TimelordTask timelordTask) {
        timelordTask.removePropertyChangeListener("hidden", this);

        boolean isRemoved = taskCollection.remove(timelordTask);
        resetTaskListeners();
        propertyChangeSupport.firePropertyChange(
            "taskCollection",
            null,
            this.taskCollection);

        return isRemoved;
    }

    /**
     * Resets the today listeners. This cycles through the listener collection
     * and removes this object as a listener against everything in it. Then it
     * cycles through the task collection and adds itself to anything that has a
     * task day for today.
     */
    public void resetTaskListeners() {
        if (log.isTraceEnabled()) {
            log.trace("Reseting Task Listeners");
        }

        Collection<TimelordTask> taskCollection = getTaskCollection();
        Iterator<TimelordTask> taskCollectionIterator =
            taskCollection.iterator();

        while (taskCollectionIterator.hasNext()) {
            TimelordTask timelordTask =
                (TimelordTask) taskCollectionIterator.next();

            timelordTask.removePropertyChangeListener("hidden", this);
            timelordTask.addPropertyChangeListener("hidden", this);
        }

        if (log.isTraceEnabled()) {
            log.trace("Finished Reseting Task Listeners");
        }
    }

    /**
     * Removes all the time tracking data that is outside the range
     * provided.
     *
     * @param startDate the state date to remove data before
     * @param endDate the end date to remove data after
     */
    public void removeTrackingOutsideRange(Date startDate, Date endDate) {
        Collection<TimelordTask> taskCollection = getTaskCollection();
        Iterator<TimelordTask> taskCollectionIterator =
            taskCollection.iterator();

        while (taskCollectionIterator.hasNext()) {
            TimelordTask timelordTask =
                (TimelordTask) taskCollectionIterator.next();

            timelordTask.removeTrackingOutsideRange(startDate, endDate);
        }

    }

    /**
     * This method cleans the data and removes any corrupt entries. This method
     * will cycle through all tasks and tasks days looking for inconsistent or
     * unneeded data and cleaning it. The common tasks it cleans for are:
     * <ul>
     * <li>Sort tasking in descending days</li>
     * <li>Removing any task days that have zero hours.
     * </ul>
     */
    public void cleanse() {
        Collection<TimelordTask> taskCollection = getTaskCollection();
        Iterator<TimelordTask> taskCollectionIterator =
            taskCollection.iterator();

        while (taskCollectionIterator.hasNext()) {
            TimelordTask timelordTask =
                (TimelordTask) taskCollectionIterator.next();

            timelordTask.sort();
            timelordTask.removeEmpty();
            timelordTask.removeFuture();
            timelordTask.correctTimeZone();
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
     * Adds a property change listener against a specific property.
     *
     * @param propertyName the name of the property to listen to
     * @param listener the listener to add
     */
    public void addPropertyChangeListener(String propertyName,
        PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
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
     * Genenerally catches a property change in of the contained tasks and
     * propogates that as a property change in one of the derrived properties of
     * this class.
     *
     * @param evt the property change
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if ("hidden".equals(evt.getPropertyName())) {
            propertyChangeSupport.firePropertyChange(
                "taskCollection",
                null,
                this.taskCollection);
        }
    }

    /**
     * Creates a cloned copy of the TimelordData object.
     *
     * @return a cloned instance of the object
     */
    public TimelordData clone() {
        TimelordData timelordDataClone;

        try {
            timelordDataClone = (TimelordData) super.clone();
        } catch (CloneNotSupportedException e) {
            if (log.isErrorEnabled()) {
                log.error("Failed to clone the base object.");
            }

            timelordDataClone = new TimelordData();
        }

        // Change all the primitives or others it's safe to copy
        timelordDataClone.timelordReaderWriter = this.timelordReaderWriter;
        timelordDataClone.dayStartTime = this.dayStartTime;
        timelordDataClone.timeZone = this.timeZone;

        // Do an element by element copy of the task collection
        LinkedList<TimelordTask> taskCollectionClone =
            new LinkedList<TimelordTask>();

        Iterator<TimelordTask> timelordTaskIterator =
            this.taskCollection.iterator();

        while(timelordTaskIterator.hasNext()) {
            taskCollectionClone.add(timelordTaskIterator.next().clone());
        }

        timelordDataClone.taskCollection = taskCollectionClone;

        return timelordDataClone;
    }

    /**
     * This is a basic comparator to allow sorting of TimeTrackTask objects
     * based on the task name.
     */
    public class TaskNameComparator implements Comparator<TimelordTask> {
        /**
         * Compares two timeTrackTask objects based on the task name.
         *
         * @param o1 the first (TimelordTask) object
         * @param o2 the second (TimelordTask) object
         * @return the result of task1name.compareTo(task2name)
         */
        public int compare(TimelordTask o1, TimelordTask o2) {
            String task1name = ((TimelordTask) o1).getTaskName();
            String task2name = ((TimelordTask) o2).getTaskName();

            return task1name.toUpperCase().compareTo(task2name.toUpperCase());
        }
    }
}
