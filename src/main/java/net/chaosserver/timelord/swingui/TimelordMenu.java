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

import net.chaosserver.timelord.data.TimelordData;
import net.chaosserver.timelord.util.OsUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.log4j.lf5.DefaultLF5Configurator;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import java.io.IOException;

import java.net.URL;

import javax.help.CSH;
import javax.help.HelpBroker;
import javax.help.HelpSet;
import javax.help.HelpSetException;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;


/**
 * This it the menu that is applied to the application frame.
 *
 * @author Jordan Reed
 */
@SuppressWarnings("serial")
public class TimelordMenu extends JMenuBar implements ActionListener {
    /** The logger. */
    private static Log log = LogFactory.getLog(TimelordMenu.class);

    /** Action Event for Exit. */
    private static final String ACTION_EXIT =
        TimelordMenu.class.getName() + ".ACTION_EXIT";

    /** Action Event for Exporting Jordan Style. */
    private static final String ACTION_EXPORT_JORDAN =
        TimelordMenu.class.getName() + ".ACTION_EXPORT_JORDAN";

    /** Action Event for Exporting Doug Style. */
    private static final String ACTION_EXPORT_DOUG =
        TimelordMenu.class.getName() + ".ACTION_EXPORT_DOUG";

    /** Action Event for Exporting XML Style. */
    private static final String ACTION_EXPORT_XML =
        TimelordMenu.class.getName() + ".ACTION_EXPORT_XML";

    /** Action Event for Adding a Task. */
    public static final String ACTION_ADDTASK =
        TimelordMenu.class.getName() + ".ACTION_ADDTASK";

    /** Action Event for hiding a Task. */
    public static final String ACTION_HIDETASK =
        TimelordMenu.class.getName() + ".ACTION_HIDETASK";

    /** Action Event for Unhiding a Task. */
    public static final String ACTION_UNHIDETASK =
        TimelordMenu.class.getName() + ".ACTION_UNHIDETASK";

    /** Action Event for adding time to a Task. */
    public static final String ACTION_ADDTIME =
        TimelordMenu.class.getName() + ".ACTION_ADDTIME";

    /** Action Event for Sorting the List. */
    public static final String ACTION_SORT =
        TimelordMenu.class.getName() + ".ACTION_SORT";

    /** Action Event for Display Log Window. */
    public static final String ACTION_LOG =
        TimelordMenu.class.getName() + ".ACTION_LOG";

    /** Action Event for Cause Leak Log Window. */
    public static final String ACTION_LEAK =
        TimelordMenu.class.getName() + ".ACTION_LEAK";

    /** Action Event for Display About Dialog. */
    public static final String ACTION_ABOUT =
        TimelordMenu.class.getName() + ".ACTION_ABOUT";

    /** Action Event for Setting Annoy to Jordan Mode. */
    public static final String ACTION_ANNOY_JORDAN =
        TimelordMenu.class.getName() + ".ACTION_ANNOY_JORDAN";

    /** Action Event for Setting Annoy to Doug Mode. */
    public static final String ACTION_ANNOY_DOUG =
        TimelordMenu.class.getName() + ".ACTION_ANNOY_DOUG";

    /** Action Event for Turning off Annoy Mode. */
    public static final String ACTION_ANNOY_NONE =
        TimelordMenu.class.getName() + ".ACTION_ANNOY_NONE";

    /** Action Event for Refreshing the View. */
    public static final String ACTION_REFRESH_VIEW =
        TimelordMenu.class.getName() + ".ACTION_REFRESH_VIEW";

    /** Action Event for Changing the Start Time. */
    public static final String ACTION_CHANGE_START =
        TimelordMenu.class.getName() + ".ACTION_CHANGE_START";

    /** The checkbox item for Jordan Annoyance Mode. */
    protected JCheckBoxMenuItem annoyanceJordanCheckbox;

    /** The checkbox item for Doug Annoyance Mode. */
    protected JCheckBoxMenuItem annoyanceDougCheckbox;

    /** The checkbox item for no Annoyance Mode. */
    protected JCheckBoxMenuItem annoyanceNoneCheckbox;

    /** The timelord data this menu affects. */
    protected TimelordData timelordData;

    /** The timelord application this menu runs against. */
    protected Timelord timelord;

    /**
     * Creates a new instance of the menu.
     *
     * @param timelord the application this menu is applied to.
     */
    public TimelordMenu(Timelord timelord) {
        this.timelord = timelord;

        this.add(createFileMenu());
        this.add(createViewMenu());
        this.add(createTaskMenu());
        this.add(createHelpMenu());
    }

    /**
     * Creates the file menu.
     *
     * @return the new file menu
     */
    protected JMenu createFileMenu() {
        JMenu fileMenu = new JMenu("File");
        JMenuItem menuItem;

        fileMenu.setMnemonic(KeyEvent.VK_F);

        JMenu exportMenu = new JMenu("Export...");
        exportMenu.setMnemonic(KeyEvent.VK_A);

        menuItem = new JMenuItem("Excel, Jordan Style...", KeyEvent.VK_J);
        menuItem.setToolTipText("For Cool People");
        menuItem.setActionCommand(ACTION_EXPORT_JORDAN);
        menuItem.addActionListener(this);
        exportMenu.add(menuItem);

        menuItem = new JMenuItem("Excel, Doug Style...", KeyEvent.VK_D);
        menuItem.setToolTipText("For Losers");
        menuItem.setActionCommand(ACTION_EXPORT_DOUG);
        menuItem.addActionListener(this);
        exportMenu.add(menuItem);

        exportMenu.addSeparator();
        menuItem = new JMenuItem("XML...", KeyEvent.VK_X);
        menuItem.setActionCommand(ACTION_EXPORT_XML);
        menuItem.addActionListener(this);
        exportMenu.add(menuItem);

        fileMenu.add(exportMenu);

        if(!OsUtil.isMac()) {
            fileMenu.addSeparator();
            menuItem = new JMenuItem("Exit", KeyEvent.VK_X);
            menuItem.setActionCommand(ACTION_EXIT);
            menuItem.addActionListener(this);
            fileMenu.add(menuItem);
        }

        return fileMenu;
    }

    /**
     * Creates the view menu.
     *
     * @return the new view menu
     */
    protected JMenu createViewMenu() {
        JMenuItem menuItem;
        JMenu viewMenu = new JMenu("View");
        viewMenu.setMnemonic(KeyEvent.VK_V);

        JMenu annoyanceModeMenu = new JMenu("Annoyance Mode");
        annoyanceJordanCheckbox = new JCheckBoxMenuItem("Jordan Mode");
        annoyanceJordanCheckbox.setToolTipText("For Cool People");
        annoyanceJordanCheckbox.setActionCommand(ACTION_ANNOY_JORDAN);
        annoyanceJordanCheckbox.addActionListener(this);
        annoyanceModeMenu.add(annoyanceJordanCheckbox);

        annoyanceDougCheckbox = new JCheckBoxMenuItem("Doug Mode");
        annoyanceDougCheckbox.setToolTipText("For Losers");
        annoyanceJordanCheckbox.setActionCommand(ACTION_ANNOY_DOUG);
        annoyanceDougCheckbox.addActionListener(this);
        annoyanceModeMenu.add(annoyanceDougCheckbox);

        annoyanceNoneCheckbox = new JCheckBoxMenuItem("None");
        annoyanceJordanCheckbox.setActionCommand(ACTION_ANNOY_NONE);
        annoyanceNoneCheckbox.addActionListener(this);
        annoyanceModeMenu.add(annoyanceNoneCheckbox);
        updateAnnoyanceButtons();

        viewMenu.add(annoyanceModeMenu);

        menuItem = new JMenuItem("Refresh View", KeyEvent.VK_R);
        menuItem.setAccelerator(
            KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_MASK)
        );
        menuItem.setActionCommand(ACTION_REFRESH_VIEW);
        menuItem.addActionListener(this);
        viewMenu.add(menuItem);

        viewMenu.addSeparator();

        menuItem = new JMenuItem("Change Start Time");
        menuItem.setActionCommand(ACTION_CHANGE_START);
        menuItem.addActionListener(this);
        viewMenu.add(menuItem);

        return viewMenu;
    }

    /**
     * Creates the task menu.
     *
     * @return the new task menu
     */
    protected JMenu createTaskMenu() {
        JMenuItem menuItem;
        JMenu taskMenu = new JMenu("Task");
        taskMenu.setMnemonic(KeyEvent.VK_T);
        this.add(taskMenu);

        menuItem = new JMenuItem("Add Task", KeyEvent.VK_T);
        menuItem.setAccelerator(
            KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_MASK)
        );
        menuItem.setActionCommand(ACTION_ADDTASK);
        menuItem.addActionListener(this);
        taskMenu.add(menuItem);

        menuItem = new JMenuItem("Hide Old Tasks", KeyEvent.VK_H);
        menuItem.setAccelerator(
            KeyStroke.getKeyStroke(KeyEvent.VK_H, InputEvent.CTRL_MASK)
        );
        menuItem.setActionCommand(ACTION_HIDETASK);
        menuItem.addActionListener(this);
        taskMenu.add(menuItem);

        menuItem = new JMenuItem("Unhide Task", KeyEvent.VK_U);
        menuItem.setAccelerator(
            KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.CTRL_MASK)
        );
        menuItem.setActionCommand(ACTION_UNHIDETASK);
        menuItem.addActionListener(this);
        taskMenu.add(menuItem);

        taskMenu.addSeparator();

        menuItem = new JMenuItem("Add Time", KeyEvent.VK_A);
        menuItem.setAccelerator(
            KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK)
        );
        menuItem.setActionCommand(ACTION_ADDTIME);
        menuItem.addActionListener(this);
        taskMenu.add(menuItem);

        return taskMenu;
    }

    /**
     * Creates a new instance of the help menu.
     *
     * @return the new help menu
     */
    protected JMenu createHelpMenu() {
        JMenuItem menuItem;
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic(KeyEvent.VK_H);
        this.add(helpMenu);

        menuItem = new JMenuItem("Help Topics", KeyEvent.VK_H);
        menuItem.setAccelerator(
            KeyStroke.getKeyStroke(KeyEvent.VK_H, InputEvent.CTRL_MASK)
        );

        try {
            URL hsURL =
                HelpSet.findHelpSet(
                    this.getClass().getClassLoader(),
                    "net/chaosserver/timelord/help/TimelordHelp.hs"
                );
            HelpSet hs = new HelpSet(null, hsURL);
            HelpBroker hb = hs.createHelpBroker();
            menuItem.addActionListener(new CSH.DisplayHelpFromSource(hb));
        } catch (HelpSetException e) {
            menuItem.setEnabled(false);
        }

        // menuItem.setActionCommand(ACTION_ABOUT);
        helpMenu.add(menuItem);

        menuItem = new JMenuItem("Log Window");
        menuItem.setActionCommand(ACTION_LOG);
        menuItem.addActionListener(this);
        menuItem.setEnabled(false);
        helpMenu.add(menuItem);

        menuItem = new JMenuItem("Cause Memory Leak");
        menuItem.setActionCommand(ACTION_LEAK);
        menuItem.addActionListener(this);
        menuItem.setEnabled(false);
        helpMenu.add(menuItem);

        if(!OsUtil.isMac()) {
            helpMenu.addSeparator();

            menuItem = new JMenuItem("About Timelord", KeyEvent.VK_A);
            menuItem.setActionCommand(ACTION_ABOUT);
            menuItem.addActionListener(this);
            helpMenu.add(menuItem);
        }

        return helpMenu;
    }

    /**
     * Sets the timelord data this menu applies to.
     *
     * @param timelordData the data this menu applies to
     */
    public void setTimelordData(TimelordData timelordData) {
        this.timelordData = timelordData;
    }

    /**
     * Gets the timelord data this menu is acting on.
     *
     * @return data this menu is acting on
     */
    public TimelordData getTimelordData() {
        return this.timelordData;
    }

    /**
     * Update the checked state of the annoyance mode menu based on the current
     * settings of the data object.
     */
    public void updateAnnoyanceButtons() {
        annoyanceJordanCheckbox.setSelected(false);
        annoyanceDougCheckbox.setSelected(false);
        annoyanceNoneCheckbox.setSelected(false);

        String annoyanceMode = timelord.getAnnoyanceMode();

        if (Timelord.ANNOYANCE_JORDAN.equals(annoyanceMode)) {
            annoyanceJordanCheckbox.setSelected(true);
        } else if (Timelord.ANNOYANCE_DOUG.equals(annoyanceMode)) {
            annoyanceDougCheckbox.setSelected(true);
        } else if (Timelord.ANNOYANCE_NONE.equals(annoyanceMode)) {
            annoyanceNoneCheckbox.setSelected(true);
        }
    }

    /**
     * Listens for action events from the menu commands and executes them.
     *
     * @param evt the event to trigger things.
     */
    public void actionPerformed(ActionEvent evt) {
        if (ACTION_EXIT.equals(evt.getActionCommand())) {
            this.timelord.stop();
        } else if (ACTION_EXPORT_JORDAN.equals(evt.getActionCommand())) {
            timelord.writeTimeTrackData(
                    "net.chaosserver.timelord.swingui."
                        + "data.ExcelDataReaderWriterUI",
                    true
                );
        } else if (ACTION_EXPORT_DOUG.equals(evt.getActionCommand())) {
            timelord.writeTimeTrackData(
                    "net.chaosserver.timelord.data."
                    + "ExcelUglyDataReaderWriter",
                    true
                );
        } else if (ACTION_EXPORT_XML.equals(evt.getActionCommand())) {
            timelord.writeTimeTrackData(
                    "net.chaosserver.timelord.data.XmlDataReaderWriter",
                    true
                );
        } else if (ACTION_ADDTASK.equals(evt.getActionCommand())) {
            timelord.getCommonTaskPanel().showAddTaskDialog();
        } else if (ACTION_HIDETASK.equals(evt.getActionCommand())) {
            timelord.getCommonTaskPanel().showHideTaskDialog();
        } else if (ACTION_UNHIDETASK.equals(evt.getActionCommand())) {
            timelord.getCommonTaskPanel().showUnhideTaskDialog();
        } else if (ACTION_ADDTIME.equals(evt.getActionCommand())) {
            timelord.getCommonTaskPanel().showAddTimeDialog();
        } else if (ACTION_SORT.equals(evt.getActionCommand())) {
            getTimelordData().sortTaskCollection();
        } else if (ACTION_ABOUT.equals(evt.getActionCommand())) {
            timelord.showAboutDialog();
        } else if (ACTION_LOG.equals(evt.getActionCommand())) {
            try {
                DefaultLF5Configurator.configure();
            } catch (IOException e) {
                if (log.isErrorEnabled()) {
                    log.error("Failed to open help", e);
                }
            }
        } else if (ACTION_REFRESH_VIEW.equals(evt.getActionCommand())) {
            timelord.getCommonTaskPanel().doLayout();
        } else if (ACTION_CHANGE_START.equals(evt.getActionCommand())) {
            timelord.changeStartTime(false);
        } else if (evt.getSource().equals(annoyanceJordanCheckbox)) {
            timelord.setAnnoyanceMode(Timelord.ANNOYANCE_JORDAN);
            updateAnnoyanceButtons();
        } else if (evt.getSource().equals(annoyanceDougCheckbox)) {
            timelord.setAnnoyanceMode(Timelord.ANNOYANCE_DOUG);
            updateAnnoyanceButtons();
        } else if (evt.getSource().equals(annoyanceNoneCheckbox)) {
            timelord.setAnnoyanceMode(Timelord.ANNOYANCE_NONE);
            updateAnnoyanceButtons();
        }
    }
}
