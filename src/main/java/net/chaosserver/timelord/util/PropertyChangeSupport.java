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
package net.chaosserver.timelord.util;

import java.beans.PropertyChangeListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Extension of the standard PropertyChangeListener.  The value add
 * of this version is that when an object goes over the max listeners
 * the listener will start logging warning messages.  This greatly
 * reduces the chances of causing memory leaks on the property
 * change listeners.
 *
 * @author Jordan
 */
@SuppressWarnings("serial")
public class PropertyChangeSupport extends java.beans.PropertyChangeSupport {
    /** The logger. */
    private static Log log = LogFactory.getLog(PropertyChangeSupport.class);

    /** The default number of maximum listeners. */
    private static final int DEFAULT_MAX_LISTENERS = 3;

    /** The source bean object that is having listeners added. */
    protected Object sourceBean;

    /** The maximum number of listeners for this bean. */
    protected int maxListeners = DEFAULT_MAX_LISTENERS;

    /**
     * Constructor that takes a sourceBean and the maximum number
     * of listeners before throwing errors.
     *
     * @param sourceBean the source bean for this object.
     * @param maxListeners the max listeners before throwing errors.
     */
    public PropertyChangeSupport(Object sourceBean, int maxListeners) {
        super(sourceBean);
        this.sourceBean = sourceBean;
        this.maxListeners = maxListeners;
    }

    /**
     * Adds a property change listener to the source and will log
     * a warning error if it goes over the maxListeners.
     *
     * @param listener the listener to add.
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        super.addPropertyChangeListener(listener);

        if (log.isWarnEnabled()) {
            PropertyChangeListener[] listeners = getPropertyChangeListeners();
            if(listeners.length > maxListeners) {
                log.warn("["
                    + sourceBean.getClass().getName()
                    + "] has too many listeners ["
                    + listeners.length
                    + "]");
            }
        }
    }

    /**
     * Adds a property change listener to the source and will log
     * a warning error if it goes over the maxListeners.
     *
     * @param propertyName the name of the property to listen on
     * @param listener the listener to add.
     */
    public void addPropertyChangeListener(String propertyName,
            PropertyChangeListener listener) {

        super.addPropertyChangeListener(propertyName, listener);

        if (log.isWarnEnabled()) {
            PropertyChangeListener[] listeners = getPropertyChangeListeners();
            if(listeners.length > maxListeners) {
                log.warn("["
                    + sourceBean.getClass().getName()
                    + "] has too many listeners ["
                    + listeners.length
                    + "]");
            }
        }
    }
}
