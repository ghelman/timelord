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

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.prefs.Preferences;

import net.chaosserver.timelord.swingui.Timelord;

/**
 * Provides some basic date formatting tools for use through
 * the Timelord tool.
 *
 * @author Jordan
 */
public final class DateUtil {
    /** Utility Class. */
    private DateUtil() {
    }

    /** The date display is used to display the title of tab. */
    public static final DateFormat DATE_FORMAT =
        new SimpleDateFormat("MMM-dd-yyyy");

    /** Number format for the time. */
    public static final NumberFormat HOURS_FORMAT = new DecimalFormat("00.00");

    /** Number of minutes in one hour. */
    public static final double MINUTE_IN_HOUR = 60d;

    /** The smallest time a user can increment in hours. */
    public static final double DEFAULT_SMALL_TIME_INCREMENT_HOUR = 0.25d;


    /**
     * The generic date format that is used throughout
     * the application.
     */
    public static final DateFormat BASIC_DATE_FORMAT =
        new SimpleDateFormat("MM-dd-yyyy");

    /**
     * Gets the time that is incremented.
     *
     * @return the smallest time incremented.
     */
    public static double getSmallestTimeIncremented() {
        Preferences preferences =
            Preferences.userNodeForPackage(Timelord.class);

        double timeIncrement = preferences.getDouble(
            Timelord.TIME_INCREMENT, DEFAULT_SMALL_TIME_INCREMENT_HOUR);
        return timeIncrement;
    }

    /**
     * Gets the time that incremented as minutes.
     *
     * @return the smallest time as minutes as a decimal
     */
    public static int getSmallestTimeInMinutes() {
        return (int) (60 * getSmallestTimeIncremented());
    }


    /**
     * Truncates a date object by setting to midnight of the
     * current date.
     *
     * @param inputDate the date to truncate to midnight
     * @return truncated date
     */
    public static Date trunc(Date inputDate) {
        Calendar outputCalendar = Calendar.getInstance();
        outputCalendar.setTime(inputDate);
        trunc(outputCalendar);
        return outputCalendar.getTime();
    }

    /**
     * Truncates a date object by setting to midnight of the
     * current date.
     *
     * @param calendar the date to truncate to midnight
     */
    public static void trunc(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    /**
     * Formats the hours display for the given locale.
     *
     * @param locale locale to format for
     * @param totalhours number of hours to display
     * @return the format as a string
     */
    public static String formatHours(Locale locale, double totalhours) {
        /*
        int hours = (int) Math.floor(totalhours);
        int minutes = (int) ((totalhours-hours) * 60);
        String result = hours + "h " + minutes + "m";
        */

        String result = HOURS_FORMAT.format(totalhours);

        return result;
    }
}
