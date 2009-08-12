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
package net.chaosserver.timelord.swingui.macos;

import net.chaosserver.timelord.swingui.Timelord;

import com.apple.eawt.Application;
import com.apple.eawt.ApplicationAdapter;
import com.apple.eawt.ApplicationEvent;
import com.apple.eawt.ApplicationListener;

/**
 * Class provides interfaces for Swing directory into the Mac UI to
 * provide stronger integration with the OS.
 *
 * @author jordan
 */
public class MacSwinger extends ApplicationAdapter
        implements ApplicationListener {

    /** Reference to the Timelord application. */
    protected Timelord timelord;

    /**
     * Creates a new instance of the MacSwinger object that handles Mac-
     * specific events inside the Swing UI.
     *
     * @param timelord instance of timelord to manipulate on the events.
     */
    public MacSwinger(Timelord timelord) {
        this.timelord = timelord;

        Application application = Application.getApplication();
        application.addApplicationListener(this);
    }

    /**
     * Handles a call to the Quit menu item in the Mac.
     *
     * @param event the Quit Event that triggers the call.
     */
    public void handleQuit(ApplicationEvent event) {
        timelord.stop();
    }

    /**
     * Handles a call to the About menu item in the Mac.
     *
     * @param event the About Event that triggers the call.
     */
    public void handleAbout(ApplicationEvent event) {
        timelord.showAboutDialog();
        event.setHandled(true);
    }
}

