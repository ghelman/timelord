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
package net.chaosserver.timelord.swingui.data;

import java.awt.Frame;

import javax.swing.JDialog;


/**
 * Interface that states a TimelordDataReaderWriter is able to display
 * a configuration Dialog Box to the user as part of saving the file.
 *
 * @author Jordan
 */
public interface TimelordDataReaderWriterUI {
    /**
     * Sets the application frame that will be used as parent of
     * a modal dialog box.
     *
     * @param parentFrame the parent frame for the dialog
     */
    void setParentFrame(Frame parentFrame);

    /**
     * Returns the dialog panel that is used to collect detailed
     * information from the user for configuration settings.
     *
     * @return dialog panel for user data
     */
    JDialog getConfigDialog();
}
