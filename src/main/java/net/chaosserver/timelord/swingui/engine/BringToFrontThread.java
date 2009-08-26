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
package net.chaosserver.timelord.swingui.engine;

import net.chaosserver.timelord.data.TimelordTask;
import net.chaosserver.timelord.data.TimelordTaskDay;
import net.chaosserver.timelord.swingui.Timelord;
import net.chaosserver.timelord.util.DateUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.awt.Frame;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import javax.swing.JFrame;


/**
 * A thread meant to run in the background and trigger the main panel to
 * come to take focus when the annoyance mode setting has occurred.
 *
 * @author jordan
 */
public class BringToFrontThread extends Thread {
    /** Logger. */
    private static Log log = LogFactory.getLog(BringToFrontThread.class);

    /** The smallest time a user can increment in milliseconds. */
    private static final double SMALL_TIME_INCREMENT_MILLI =
        3600000 * DateUtil.getSmallestTimeIncremented();

    /** Time thread should sleep between polling. */
    private static final int SLEEP_TIME_MILLI = 60000;

    /** The application that will brought to the front. */
    protected Timelord timelord;

    /** The container frame. */
    protected JFrame frame;

    /** Holds the last time the window was brought to the front. */
    protected long lastAnnoy = System.currentTimeMillis();

    /** If set to true, will stop the thread on the next loop. */
    protected boolean stop = false;

    /**
     * Constructs a new version of the thread.
     *
     * @param frame the frame that holds the object to the brought forward
     * @param timelord the main application
     */
    public BringToFrontThread(JFrame frame, Timelord timelord) {
        super();
        setName("BringToFrontThread");
        setTimelord(timelord);
        setFrame(frame);
    }

    /**
     * Sets the timelord application object.
     *
     * @param timelord timelord application
     */
    public void setTimelord(Timelord timelord) {
        this.timelord = timelord;
    }

    /**
     * Gets the timelord application.
     *
     * @return timelord application
     */
    public Timelord getTimelord() {
        return this.timelord;
    }

    /**
     * Sets the containing frame.
     *
     * @param frame containg frame
     */
    public void setFrame(JFrame frame) {
        this.frame = frame;
    }

    /**
     * Gets the containing frame.
     *
     * @return containging frame.
     */
    public JFrame getFrame() {
        return this.frame;
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
     * The main loop wakes each minute and checks annoy mode to see if should
     * bring the thread to the front. To stop the thread set the stop flag and
     * trigger an interrupt or wait patiently.
     */
    public void run() {
        if (log.isInfoEnabled()) {
            log.info("Starting up the BrintToFrontThread");
        }

        while (!isStop()) {
            double untrackedTimeLeftToday = getUntrackedTimeLeftToday();
            getTimelord().getTimelordData()
                .fireUntrackedTimeLeftToday(untrackedTimeLeftToday);

            String annoymode = getTimelord().getAnnoyanceMode();

            if (Timelord.ANNOYANCE_JORDAN.equals(annoymode)) {
                if (log.isTraceEnabled()) {
                    log.trace(
                        "TimeLeftToday is [" + untrackedTimeLeftToday + "]");
                }

                if (untrackedTimeLeftToday
                        >= DateUtil.getSmallestTimeIncremented()) {

                    annoy();
                } else {
                    if (log.isTraceEnabled()) {
                        log.trace("Leaving Window Alone");
                    }
                }
            } else if (Timelord.ANNOYANCE_DOUG.equals(annoymode)) {
                double timeSinceLastAnnoy =
                    System.currentTimeMillis() - lastAnnoy;

                if (log.isTraceEnabled()) {
                    log.trace(
                            "Last annoyed [" + timeSinceLastAnnoy
                            + "] millis ago.");
                }

                if ((timeSinceLastAnnoy >= SMALL_TIME_INCREMENT_MILLI)
                        && (untrackedTimeLeftToday
                            >= DateUtil.getSmallestTimeIncremented())) {
                    annoy();
                }
            }

            try {
                Thread.sleep(SLEEP_TIME_MILLI);
            } catch (InterruptedException e) {
                if (log.isTraceEnabled()) {
                    log.trace("Interrupted the BrintToFrontThread");
                }
            }
        }

        if (log.isInfoEnabled()) {
            log.info("Shutting down the BrintToFrontThread");
        }
    }

    /**
     * Gets the amount of time for today that has not yet been tracked. This is
     * calculated by getting the time tracked today and subtracting it fromt he
     * current time.
     *
     * @return the amount of time that is not yet tracked.
     */
    public double getUntrackedTimeLeftToday() {
        Date today = DateUtil.trunc(new Date());

        double hourOfDay =
            Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            + (Calendar.getInstance().get(
                Calendar.MINUTE) / DateUtil.MINUTE_IN_HOUR);

        double dayStartTime =
            getTimelord().getTimelordData().getDayStartTime();

        double totalTimeToday = 0;

        Collection<TimelordTask> taskCollection =
            getTimelord().getTimelordData().getTaskCollection();

        Iterator<TimelordTask> viewTaskListIterator = taskCollection.iterator();

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
     * Trigger an annoy by un-minimizing the window and bringing it to the
     * front.
     */
    protected void annoy() {
        lastAnnoy = System.currentTimeMillis();
        getFrame().setExtendedState(Frame.NORMAL);
        getFrame().toFront();
        getTimelord().showTodayTab();

        // This bounces the Dock in Panther/Tiger
        try {
            Class<?> nsApplicationClass =
                Class.forName("com.apple.cocoa.application.NSApplication");

            Method sharedAppMethod =
                nsApplicationClass.getDeclaredMethod(
                    "sharedApplication",
                    new Class[] {});

            Object nsApplicationObject =
                sharedAppMethod.invoke(null, new Object[] {});

            Field userAttentionRequestCriticalField =
                nsApplicationClass.getDeclaredField(
                    "UserAttentionRequestCritical");

            Method requestUserAttentionMethod =
                nsApplicationClass.getDeclaredMethod(
                    "requestUserAttention",
                    new Class[] {
                        userAttentionRequestCriticalField.getType()
                        });

            requestUserAttentionMethod.invoke(
                nsApplicationObject,
                new Object[] { userAttentionRequestCriticalField.get(null) });
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Issue bouncing dock", e);
            }
        }

        if (log.isTraceEnabled()) {
            log.trace("Bringing Window to Front");
        }
    }
}
