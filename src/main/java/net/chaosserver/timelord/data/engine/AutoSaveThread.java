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
package net.chaosserver.timelord.data.engine;

import net.chaosserver.timelord.data.TimelordData;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * A thread run in the background that will auto-save the data file
 * on a set duration.
 *
 * @author Jordan Reed
 */
public class AutoSaveThread extends Thread {
    /** Logger. */
    private static Log log = LogFactory.getLog(AutoSaveThread.class);

    /** Sleep Time. */
    public static final int SLEEP_TIME = 300000;

    /** The data to be saved. */
    protected TimelordData timelordData;

    /** If set to true, will stop the thread on the next loop. */
    protected boolean stop = false;

    /**
     * Constructs a new version of the thread.
     *
     * @param timelordData the data to save
     */
    public AutoSaveThread(TimelordData timelordData) {
        super();
        setName("TimelordAutoSaveThread");
        setTimelordData(timelordData);
    }

    /**
     * Sets the timelord data object.
     *
     * @param timelordData timelord data object
     */
    public void setTimelordData(TimelordData timelordData) {
        this.timelordData = timelordData;
    }

    /**
     * Gets the timelord data object.
     *
     * @return timelord data object
     */
    public TimelordData getTimelordData() {
        return this.timelordData;
    }

    /**
     * Sets the stop flag.
     *
     * @param stop the new value of the stop flag
     */
    public void setStop(boolean stop) {
        this.stop = stop;
    }

    /**
     * Returns if the stop flag is set.
     *
     * @return if the thread should stop
     */
    public boolean isStop() {
        return this.stop;
    }

    /**
     * The main loop wakes ever few minutes and saves the file.
     * To stop the thread set the stop flag and trigger an interrupt
     * or wait patiently.
     */
    public void run() {
        if (log.isInfoEnabled()) {
            log.info("Starting up the AutoSaveThread");
        }

        while (!isStop()) {
            try {
                Thread.sleep(SLEEP_TIME);

                if (log.isTraceEnabled()) {
                    log.trace("AutoSave File");
                }

                try {
                    getTimelordData().write();
                } catch (Exception e) {
                    if (log.isFatalEnabled()) {
                        log.fatal("Couldn't autosave", e);
                    }
                }

                if (log.isTraceEnabled()) {
                    log.trace("AutoSave File Complete");
                }
            } catch (InterruptedException e) {
                if (log.isTraceEnabled()) {
                    log.trace("Interrupted the AutoSaveThread");
                }
            }
        }

        if (log.isInfoEnabled()) {
            log.info("Shutting down the AutoSaveThread");
        }

        if (log.isTraceEnabled()) {
            log.trace("Shutdown Save File");
        }

        try {
            getTimelordData().write();
        } catch (Exception e) {
            if (log.isFatalEnabled()) {
                log.fatal("Couldn't autosave", e);
            }
        }

        if (log.isTraceEnabled()) {
            log.trace("Shutdown Save File Complete");
        }
    }
}
