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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeListenerProxy;

import java.io.Serializable;

import java.util.Date;


/**
 * Data class that holds information about a single day of time
 * for a single task.
 */
@SuppressWarnings("serial")
public class TimelordTaskDay implements Serializable, Cloneable {
    /** Logger. */
    private static Log log = LogFactory.getLog(TimelordTaskDay.class);

    /** Max Listeners for Object. */
    private static final int MAX_LISTENERS = 4;

    /** Property change support for this class. */
    protected PropertyChangeSupport propertyChangeSupport =
        new PropertyChangeSupport(this, MAX_LISTENERS);

    /**
     * The date that this object is holding time for.  This date should always
     * be set to the very start of the day with hours/minutes/seconds all set
     * at zero.
     */
    protected Date date;

    /**
     * Number of hours that have been tracked for this date.
     */
    protected double hours;

    /**
     * A simple note associated with the hours tracked for this day.
     */
    protected String note;

    /**
     * Default constructor.
     */
    public TimelordTaskDay() { }

    /**
     * Getter for the date.
     * @return the date
     */
    public Date getDate() {
        return this.date;
    }

    /**
     * Setter for the date.
     * @param date the date this object represents
     */
    public void setDate(Date date) {
        Date oldDate = this.date;
        this.date = date;
        propertyChangeSupport.firePropertyChange("date", oldDate, this.date);

        if (log.isTraceEnabled()) {
            log.trace("Firing property change [hours]");
        }
    }

    /**
     * Sets a text string note associated with this day's time.
     * @param note the note value
     */
    public void setNote(String note) {
        String oldNote = this.note;
        this.note = note;
        propertyChangeSupport.firePropertyChange("note", oldNote, this.note);
    }

    /**
     * Gets the note associated with this time.  Could be null.
     * @return the note for the day or null
     */
    public String getNote() {
        return this.note;
    }

    /**
     * Gets the number of hours associated with this day.
     * @return number of hours
     */
    public double getHours() {
        return this.hours;
    }

    /**
     * Sets the number of hours associated with this day.
     * @param hours new value for hours.
     */
    public void setHours(double hours) {
        double oldHours = this.hours;
        this.hours = hours;

        propertyChangeSupport.firePropertyChange(
            "hours",
            new Double(oldHours),
            new Double(this.hours));

        if (log.isTraceEnabled()) {
            log.trace("Firing property change [hours]");
        }
    }

    /**
     * Adds more hours to the current amount of hours.
     * @param hours the amount of hours to add
     */
    public synchronized void addHours(double hours) {
        setHours(getHours() + hours);
    }

    /**
     * Adds a property change listener.
     * @param listener the listener to add
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Adds a property change listener for a specific property.
     * @param propertyName property name to
     * @param listener listener to add
     */
    public void addPropertyChangeListener(String propertyName,
        PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(propertyName, listener);

        if (log.isDebugEnabled()) {
            log.debug("Adding PropertyChangeListener [" + listener + "]");

            PropertyChangeListener[] listeners =
                propertyChangeSupport.getPropertyChangeListeners();

            for (int i = 0; i < listeners.length; i++) {
                if (listeners[i] instanceof PropertyChangeListenerProxy) {
                    log.debug(
                            "Current Listener [" + i + "] is ["
                            + ((PropertyChangeListenerProxy) listeners[i])
                            .getListener() + "] on ["
                            + ((PropertyChangeListenerProxy) listeners[i])
                            .getPropertyName() + "]");
                } else {
                    log.debug(
                            "Current Listener [" + i + "] is [" + listeners[i]
                            + "]");
                }
            }
        }
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
     * Removes a property change listener.
     *
     * @param propertyName property name to
     * @param listener the listener to remove
     */
    public void removePropertyChangeListener(String propertyName,
        PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(propertyName,
            listener);
    }

    /**
     * Creates a copy of the object.
     *
     * @return a copy of the object
     */
    public TimelordTaskDay clone() {
        TimelordTaskDay timelordTaskDayClone = new TimelordTaskDay();
        timelordTaskDayClone.date = this.date;
        timelordTaskDayClone.hours = this.hours;
        timelordTaskDayClone.note = this.note;

        return timelordTaskDayClone;
    }

    /**
     * Returns a String representation for debugging.
     * @return string representation.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.getClass().getName());
        sb.append(" [date=");
        sb.append(getDate());
        sb.append(", hours=");
        sb.append(getHours());
        sb.append("]");

        return sb.toString();
    }
}
