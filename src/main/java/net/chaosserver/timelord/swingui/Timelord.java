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

import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.Date;
import java.util.Properties;
import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import net.chaosserver.timelord.data.TimelordData;
import net.chaosserver.timelord.data.TimelordDataException;
import net.chaosserver.timelord.data.TimelordDataReaderWriter;
import net.chaosserver.timelord.data.TimelordDayView;
import net.chaosserver.timelord.data.XmlDataReaderWriter;
import net.chaosserver.timelord.data.engine.AutoSaveThread;
import net.chaosserver.timelord.swingui.data.TimelordDataReaderWriterUI;
import net.chaosserver.timelord.swingui.engine.BringToFrontThread;
import net.chaosserver.timelord.util.DateUtil;
import net.chaosserver.timelord.util.OsUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * The main class to run the Timelord swing application.
 *
 * @author Jordan Reed
 */
public class Timelord {
    /** Logger. */
    private static Log log = LogFactory.getLog(Timelord.class);

    /** Preference key for the annoyance mode. */
    protected static final String ANNOYANCE_MODE = "ANNOYANCE_MODE";

    /** Preferences value for Jordan annoyance mode. */
    public static final String ANNOYANCE_JORDAN = "ANNOYANCE_JORDAN";

    /** Preferences value for Doug annoyance mode. */
    public static final String ANNOYANCE_DOUG = "ANNOYANCE_DOUG";

    /** Preferences value for no annoyance mode. */
    public static final String ANNOYANCE_NONE = "ANNOYANCE_NONE";

    /** Constant for preference of the X location of the frame when saved. */
    private static final String FRAME_X_LOCATION = "FRAME_X_LOCATION";

    /** Constant for preference of the Y location of the frame when saved. */
    private static final String FRAME_Y_LOCATION = "FRAME_Y_LOCATION";

    /** Constant for preference of the width of the frame when saved. */
    private static final String FRAME_WIDTH = "FRAME_WIDTH";

    /** Constant for preference of the height of the frame when saved. */
    private static final String FRAME_HEIGHT = "FRAME_HEIGHT";

    /** The default frame height. */
    private static final int DEFAULT_FRAME_HEIGHT = 480;

    /** The default frame width. */
    private static final int DEFAULT_FRAME_WIDTH = 640;

    /** Constant for preference for default time increment. */
	public static final String TIME_INCREMENT = "TIME_INCREMENT";

    /** Holds the application frame. */
    protected JFrame applicationFrame;

    /** Holds the timelord data object. */
    protected TimelordData timelordData;

    /** Holds the main tabbed pane visual component. */
    protected TimelordTabbedPane timelordTabbedPane;

    /** The engine that brings the main window to the front. */
    protected BringToFrontThread bringToFrontThread;

    /** The engine that saves the data once every few minutes. */
    protected AutoSaveThread autoSaveThread;

    /** The application icon. */
    protected Icon applicationIcon;

    /**
     * Constructs the new timelord object.
     */
    public Timelord() {
        // Set the UI to the system look and feel so the application
        // appears more native.
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            if (log.isWarnEnabled()) {
                log.warn("Failed to set look and feel", e);
            }
        }
    }

    /**
     * Sets the timelord data object this is a visual display for.
     *
     * @param timelordData the data object.
     */
    public void setTimelordData(TimelordData timelordData) {
        this.timelordData = timelordData;
    }

    /**
     * Getter for the timelord data object this is displaying.
     *
     * @return the timelord data object
     */
    public TimelordData getTimelordData() {
        return this.timelordData;
    }

    /**
     * Gets the annoyance mode from the perferences or returns the default.
     *
     * @return the annoyance mode
     */
    public String getAnnoyanceMode() {
        Preferences preferences =
            Preferences.userNodeForPackage(this.getClass());

        return preferences.get(ANNOYANCE_MODE, ANNOYANCE_JORDAN);
    }

    /**
     * Sets the annoyance mode. This should be one of the constants else things
     * will behave erradically.
     *
     * @param annoyanceMode the annoyance mode
     */
    public void setAnnoyanceMode(String annoyanceMode) {
        Preferences preferences =
            Preferences.userNodeForPackage(this.getClass());

        preferences.put(ANNOYANCE_MODE, annoyanceMode);
    }

    /**
     * Persists out the timelord file and allows the user to choose where
     * the file should go.
     *
     * @param rwClassName the name of the RW class
     *        (e.g. "net.chaosserver.timelord.data.ExcelDataReaderWriter")
     * @param userSelect allows the user to select where the file should
     *        be persisted.
     */
    public void writeTimeTrackData(String rwClassName, boolean userSelect) {
        try {
            Class<?> rwClass = Class.forName(rwClassName);
            TimelordDataReaderWriter timelordDataRW =
                (TimelordDataReaderWriter) rwClass.newInstance();

            int result = JFileChooser.APPROVE_OPTION;
            File outputFile = timelordDataRW.getDefaultOutputFile();

            if(timelordDataRW instanceof TimelordDataReaderWriterUI) {
                TimelordDataReaderWriterUI timelordDataReaderWriterUI
                    = (TimelordDataReaderWriterUI) timelordDataRW;

                timelordDataReaderWriterUI.setParentFrame(applicationFrame);
                JDialog configDialog =
                    timelordDataReaderWriterUI.getConfigDialog();

                configDialog.pack();
                configDialog.setLocationRelativeTo(applicationFrame);
                configDialog.setVisible(true);
            }

            if (userSelect) {
                if(OsUtil.isMac()) {
                    FileDialog fileDialog =
                        new FileDialog(applicationFrame,
                                "Select File", FileDialog.SAVE);

                    fileDialog.setDirectory(outputFile.getParent());
                    fileDialog.setFile(outputFile.getName());
                    fileDialog.setVisible(true);
                    if(fileDialog.getFile() != null) {
                         outputFile =
                             new File(fileDialog.getDirectory(),
                                     fileDialog.getFile());
                    }

                } else {
                    JFileChooser fileChooser =
                        new JFileChooser(outputFile.getParentFile());

                    fileChooser.setSelectedFile(outputFile);
                    fileChooser.setFileFilter(timelordDataRW.getFileFilter());
                    result = fileChooser.showSaveDialog(applicationFrame);

                    if (result == JFileChooser.APPROVE_OPTION) {
                        outputFile = fileChooser.getSelectedFile();
                    }
                }
            }

            if (result == JFileChooser.APPROVE_OPTION) {
                timelordDataRW.writeTimelordData(
                    getTimelordData(),
                    outputFile
                );
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                applicationFrame,
                "Error writing to file.\n"
                + "Do you have the output file open?",
                "Save Error",
                JOptionPane.ERROR_MESSAGE,
                applicationIcon
            );

            if (log.isErrorEnabled()) {
                log.error("Error persisting file", e);
            }
        }
    }

    /**
     * Saves the current location of the applicatioFrame.
     */
    protected void saveFrameLocation() {
        if (applicationFrame != null) {
            Point frameLocation = applicationFrame.getLocation();

            Preferences preferences =
                Preferences.userNodeForPackage(this.getClass());
            preferences.putDouble(FRAME_X_LOCATION, frameLocation.getX());
            preferences.putDouble(FRAME_Y_LOCATION, frameLocation.getY());
        }
    }

    /**
     * Gets back the point location where the frame was last saved.
     *
     * @return location where the frame was last saved
     */
    protected Point loadLastFrameLocation() {
        Preferences preferences =
            Preferences.userNodeForPackage(this.getClass());

        Point windowLocation = new Point();
        windowLocation.setLocation(
            preferences.getDouble(FRAME_X_LOCATION, 0),
            preferences.getDouble(FRAME_Y_LOCATION, 0)
        );

        return windowLocation;
    }

    /**
     * Returns the dimension of the last saved framesize.
     *
     * @return last saved frame size in preferences
     */
    protected Dimension loadLastFrameSize() {
        Preferences preferences =
            Preferences.userNodeForPackage(this.getClass());

        Dimension windowSize = new Dimension();
        windowSize.setSize(
            preferences.getDouble(FRAME_WIDTH, DEFAULT_FRAME_WIDTH),
            preferences.getDouble(FRAME_HEIGHT, DEFAULT_FRAME_HEIGHT)
        );

        return windowSize;
    }

    /**
     * Saves the size of the current frame into preferences.
     */
    protected void saveFrameSize() {
        if (applicationFrame != null) {
            Dimension windowSize = applicationFrame.getSize();

            Preferences preferences =
                Preferences.userNodeForPackage(this.getClass());
            preferences.putDouble(FRAME_WIDTH, windowSize.getWidth());
            preferences.putDouble(FRAME_HEIGHT, windowSize.getHeight());
        }
    }

    /**
     * Gets the common task panel (the one for the current date) from the
     * container.
     *
     * @return the common task panel
     */
    protected CommonTaskPanel getCommonTaskPanel() {
        return timelordTabbedPane.getCommonTaskPanel();
    }

    /**
     * Returns if the system is already running based on the creation of a lock
     * file that is deleted when the JVM exits and creates a new instance of the
     * lockfile for this JVM. If the lockfile is detected it presents a dialog
     * giving an option for the end user to overwrite the lock file.
     *
     * @return indicates another instance of timelord is already running
     */
    public boolean isAlreadyRunning() {
        boolean alreadyRunning = false;
        File homeDirectory = new File(System.getProperty("user.home"));
        File lockFile = new File(homeDirectory, "Timelord.lockfile");

        if (lockFile.exists()) {
            String startAnyway = "Start Anyway";
            String dontStart = "Cancel Start";
            Object[] options = { dontStart, startAnyway };
            int result =
                JOptionPane.showOptionDialog(
                        null,
                        "A lockfile for an instance of timelord "
                        + "has been found.  If you are already "
                        + "running timelord, click \"" + dontStart
                        + "\" and use the running program.",
                        "Timelord Already Running",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.WARNING_MESSAGE,
                        null,
                        options,
                        dontStart
                    );

            if (result == 0) {
                alreadyRunning = true;
            }
        }

        if (!alreadyRunning) {
            try {
                lockFile.createNewFile();
                lockFile.deleteOnExit();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return alreadyRunning;
    }

    /**
     * Starts up the timelord application and displays the frame.
     */
    public void start() {
        applicationFrame = new JFrame("Timelord");

        // Get the pretty application icon
        URL iconUrl =
            this.getClass()
                .getResource(
                "/net/chaosserver/timelord/TimelordIcon.gif"
            );

        if (log.isTraceEnabled()) {
            log.trace("iconUrl is [" + iconUrl + "]");
        }

        if (iconUrl != null) {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            Image applicationImage = toolkit.getImage(iconUrl);
            applicationIcon = new ImageIcon(applicationImage);
            applicationFrame.setIconImage(applicationImage);
        } else {
            if (log.isWarnEnabled()) {
                log.warn("Cound not find icon url");
            }
        }

        if (!isAlreadyRunning()) {
            TimelordMenu menu = new TimelordMenu(this);
            applicationFrame.setJMenuBar(menu);
            applicationFrame.addWindowListener(new WindowCloser());

            if(OsUtil.isMac()) {
                try {
                    Class<?> macSwingerClass =
                        Class.forName("net.chaosserver.timelord."
                                + "swingui.macos.MacSwinger");

                    Constructor<?> macSwingerConstructor =
                        macSwingerClass.getConstructor(
                                new Class[]{Timelord.class});

                    macSwingerConstructor.newInstance(new Object[] {this});
                } catch (Exception e) {
                    // Shouldn't happen, but not a big deal
                    if (log.isWarnEnabled()) {
                        log.warn("Failed to create the MacSwinger", e);
                    }
                }



            }

            TimelordDataReaderWriter timelordDataRW =
                new XmlDataReaderWriter();

            try {
                TimelordData inputTimelordData =
                    timelordDataRW.readTimelordData();
                inputTimelordData.cleanse();
                inputTimelordData.resetTaskListeners();
                setTimelordData(inputTimelordData);
                menu.setTimelordData(getTimelordData());

                applicationFrame.setSize(loadLastFrameSize());
                timelordTabbedPane =
                    new TimelordTabbedPane(getTimelordData());
                applicationFrame.getContentPane().add(timelordTabbedPane);

                applicationFrame.setLocation(loadLastFrameLocation());
                applicationFrame.setVisible(true);

                // If there is no data for Today, let the user set the
                // start time.
                TimelordDayView timelordDayView =
                    new TimelordDayView(inputTimelordData,
                            DateUtil.trunc(new Date()));

                if(timelordDayView.getTotalTimeToday(true) == 0) {
                    menu.timelord.changeStartTime(true);
                }

                bringToFrontThread = new BringToFrontThread(
                        applicationFrame, this
                    );
                bringToFrontThread.start();

                autoSaveThread = new AutoSaveThread(getTimelordData());
                autoSaveThread.start();
            } catch (TimelordDataException e) {
                String shutdown = "Shutdown";
                Object[] options = { shutdown };

                JOptionPane.showOptionDialog(
                        null,
                        "There was an unrecoverable error trying to load "
                        + "the file.\nTimelord was probably shutdown "
                        + "in the middle of the last write.\nThere "
                        + "should be a lot of backup files inside the "
                        + "defaut location.\nCopy one of the backups "
                        + "over the most recent, restart, and keep your "
                        + "fingers crossed.\n" + e,
                        "Timelord Data File Corrupted",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.ERROR_MESSAGE,
                        null,
                        options,
                        shutdown
                    );

                stop();
            }
        } else {
            stop();
        }
    }

    /**
     * The stop method cleans up the application and exits.
     */
    public void stop() {
        if (bringToFrontThread != null) {
            bringToFrontThread.setStop(true);
            bringToFrontThread.interrupt();
        }

        if (autoSaveThread != null) {
            autoSaveThread.setStop(true);
            autoSaveThread.interrupt();

            if (log.isTraceEnabled()) {
                log.trace("Waiting for the AutoSaveThread to terminate.");
            }

            try {
                autoSaveThread.join();

                if (log.isTraceEnabled()) {
                    log.trace("AutoSaveThread has terminated.");
                }
            } catch (InterruptedException e) {
                if (log.isWarnEnabled()) {
                    log.warn("AutoSaveThread was interrupted.", e);
                }
            }
        }

        saveFrameLocation();
        saveFrameSize();

        System.exit(0);
    }

    /**
     * Sets the todayTab as the proper tab to show to the user.
     */
    public void showTodayTab() {
        // Since the Today Tab is always tab zero, set this to tab zero.
        timelordTabbedPane.setSelectedIndex(0);
    }

    /**
     * Shows the about dialog that tells about the application.
     */
    public void showAboutDialog() {
        Package packageInfo =
            Package.getPackage("net.chaosserver.timelord.swingui");

        if (log.isTraceEnabled()) {
            if (packageInfo != null) {
                StringBuffer sb = new StringBuffer();
                sb.append(packageInfo.getClass().getName());
                sb.append(" [name=");
                sb.append(packageInfo.getName());
                sb.append(", specificationTitle=");
                sb.append(packageInfo.getSpecificationTitle());
                sb.append(", specificationVersion=");
                sb.append(packageInfo.getSpecificationVersion());
                sb.append(", specificationVendor=");
                sb.append(packageInfo.getSpecificationVendor());
                sb.append(", implementationTitle=");
                sb.append(packageInfo.getImplementationTitle());
                sb.append(", implementationVersion=");
                sb.append(packageInfo.getImplementationVersion());
                sb.append(", implementationVendor=");
                sb.append(packageInfo.getImplementationVendor());
                sb.append("]");
                log.trace(sb.toString());
            }
        }

        StringBuffer sb = new StringBuffer();
        sb.append("Timelord");

        if (
            (packageInfo != null)
                && (packageInfo.getImplementationVersion() != null)
        ) {
            sb.append(" [");
            sb.append(packageInfo.getImplementationVersion());
            sb.append("]");
        } else {
            InputStream appPropertiesStream =
                this.getClass().getResourceAsStream(
                        "/net/chaosserver/timelord/Timelord.properties"
                    );

            if (appPropertiesStream != null) {
                try {
                    Properties appProperties = new Properties();
                    appProperties.load(appPropertiesStream);

                    sb.append(" ");
                    sb.append(
                        appProperties.getProperty(
                            "implementation.version",
                            "[Unknown Version]"
                        )
                    );
                } catch (IOException e) {
                    sb.append(" [Unknown Version]");
                }
            } else {
                sb.append(" [Unknown Version]");
            }
        }

        sb.append("\n");
        sb.append("Created by Jordan Reed\n");
        sb.append("Copyright 2007 Jordan Reed.  All rights reserved.  ");
        sb.append("Use subject to license.\n");
        sb.append("Plesae route questions to >/dev/null");

        JOptionPane.showMessageDialog(
            applicationFrame,
            sb.toString(),
            "About Timelord",
            JOptionPane.INFORMATION_MESSAGE,
            applicationIcon
        );
    }

    /**
     * Present a dialog to allow the user to change the start time for the
     * day.
     *
     * @param useCurrentTime has the dialog default be the current time
     */
    public void changeStartTime(boolean useCurrentTime) {
        StartTimeDialog startTimeDialog =
            new StartTimeDialog(applicationFrame,
                    useCurrentTime,
                    getTimelordData());

        startTimeDialog.setVisible(true);
        startTimeDialog.dispose();
    }
    
    /**
     * Presents a dialog to allow the changing of the annoy time
     * for the various annoy modes.
     */
    public void changeAnnoyTime() {
    	AnnoyTimeDialog annoyTimeDialog =
    		new AnnoyTimeDialog(applicationFrame);
    	
    	annoyTimeDialog.setVisible(true);
    	annoyTimeDialog.dispose();
    }

    /**
     * The main method creates a new instance and starts it.
     *
     * @param args command line args
     */
    public static void main(String[] args) {
        Timelord timelord = new Timelord();
        timelord.start();
    }

    /**
     * Basic adapter that listens for window close events and stops the
     * application.
     */
    protected class WindowCloser extends WindowAdapter {
        /**
         * Stops the application on window closing.
         *
         * @param evt the window event
         */
        public void windowClosing(WindowEvent evt) {
            stop();
        }
    }
}
