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
package net.chaosserver.timelord.swingui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Utility class to attempt to handle various common swing settings.
 *
 * @author jordan
 */
public final class SwingUtil {
    /** The logger. */
    private static Log log = LogFactory.getLog(SwingUtil.class);

    /** Utility method has a private constructor. */
    private SwingUtil() {
    }

    /**
     * Repair location is designed to detect if a box is partially
     * off-screen and move the box back onto the screen.
     *
     * @param component component to repair
     */
    public static void repairLocation(Component component) {
            Point locationPoint = component.getLocation();
            Point locationOnScreenPoint = null;
            if(component.isVisible()) {
                locationOnScreenPoint = component.getLocationOnScreen();
            }
            Dimension componentSize = component.getSize();
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

            if(log.isDebugEnabled()) {
                log.debug("Repairing location on ["
                        + component.getClass().getName()
                        + "].  Original location point = ["
                        + locationPoint
                        + "] and location on screen point = ["
                        + locationOnScreenPoint
                        + "].  The screen size is ["
                        + screenSize
                        + "] and the component size is ["
                        + componentSize
                        + "]");
            }

            // Is the dialog to far to the left?  Then fix.
            if(locationPoint.getX() < 0) {
                locationPoint.setLocation(0, locationPoint.getY());
            }
            if(locationPoint.getY() < 0) {
                locationPoint.setLocation(locationPoint.getX(), 0);
            }
            // component.setLocation(locationPoint);

            // Is the dialog too wide?
            if(locationPoint.getX() + componentSize.getWidth()
                    > screenSize.getWidth()) {

                componentSize.width =
                    (int) (screenSize.getWidth() - locationPoint.getX());
            }
            if(locationPoint.getY() + componentSize.getHeight()
                    > screenSize.getHeight()) {

                componentSize.height =
                    (int) (screenSize.getHeight() - locationPoint.getY());
            }

            // component.setSize(componentSize);
    }
}
