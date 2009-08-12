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

/**
 * Contains utility methods that are specific to operating systems
 * such as a Mac OS versus Windows OS.
 *
 * @since 2.1
 */
public final class OsUtil {
    /** Utility class constructor. */
    private OsUtil() {
        // Cannot be created.
    }
    /**
     * Tests the OS that the application is running on.
     * @return if it is in OSX
     */
     public static boolean isMac() {
         boolean mac = false;
        try {
            // This test works great for Panther/Tiger, but Leopard fails it
            Class.forName("com.apple.cocoa.application.NSApplication");
            mac = true;
        } catch (ClassNotFoundException e) {
            // This is okay, just means it's not Panther/Tiger
            if(System.getProperty("mrj.version") != null) {
                mac = true;
            }

        }
        return mac;
     }

}
