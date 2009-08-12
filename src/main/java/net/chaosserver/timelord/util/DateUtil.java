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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

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

    /** Number of minutes in one hour. */
    public static final double MINUTE_IN_HOUR = 60d;

    /** The smallest time a user can increment in hours. */
    public static final double SMALL_TIME_INCREMENT_HOUR = 0.25d;


    /**
     * The generic date format that is used throughout
     * the application.
     */
    public static final DateFormat BASIC_DATE_FORMAT =
        new SimpleDateFormat("MM-dd-yyyy");

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
}
