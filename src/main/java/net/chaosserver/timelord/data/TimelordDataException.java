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


/**
 * Represents the highest level possible exception for package exceptions.
 * Generally this should be subclassed to more useful exceptions by the general
 * one can be thrown for ease of use.
 *
 * @author Jordan Reed
 */
@SuppressWarnings("serial")
public class TimelordDataException extends Exception {
    /**
     * Constructor.
     *
     * @param s descrition of error
     * @param e wrapped exception
     */
    public TimelordDataException(String s, Exception e) {
        super(s, e);
    }
}
