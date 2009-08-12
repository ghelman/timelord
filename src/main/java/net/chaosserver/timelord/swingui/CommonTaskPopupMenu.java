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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.awt.event.ActionListener;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;


/**
 * This is a popup menu designed to be used by the CommonTaskPanel that can
 * manipulate TimelordTaskData as well as individual TimelordTasks.
 *
 * @author Jordan Reed
 */
@SuppressWarnings("serial")
public class CommonTaskPopupMenu extends JPopupMenu {
    /** logger. */
    private static Log log = LogFactory.getLog(CommonTaskPopupMenu.class);

    /** Action Event for Adding a Task. */
    public static final String ACTION_ADDTASK =
        CommonTaskPopupMenu.class.getName() + ".ACTION_ADDTASK";

    /** Action Event for Adding Time. */
    public static final String ACTION_ADDTIME =
        CommonTaskPopupMenu.class.getName() + ".ACTION_ADDTIME";

    /** Action Event for Deleting a Task. */
    public static final String ACTION_DELTASK =
        CommonTaskPopupMenu.class.getName() + ".ACTION_DELTASK";

    /** Action Event for Renaming a Task. */
    public static final String ACTION_RENTASK =
        CommonTaskPopupMenu.class.getName() + ".ACTION_RENTASK";

    /** Action Event for Hiding a Task. */
    public static final String ACTION_HIDETASK =
        CommonTaskPopupMenu.class.getName() + ".ACTION_HIDETASK";

    /** Action Event for Marking a Task for Export. */
    public static final String ACTION_EXPORT =
        CommonTaskPopupMenu.class.getName() + ".ACTION_EXPORT";

    /** Action Event for Editing a Task Note. */
    public static final String ACTION_EDITNOTE =
        CommonTaskPopupMenu.class.getName() + ".ACTION_EDITNOTE";

    /** The task day panel that popup may manipulate. */
    protected TaskDayPanel taskDayPanel;

    /** The Add Task menu item. */
    protected JMenuItem addTaskItem;

    /** The Add Time menu item. */
    protected JMenuItem addTimeItem;

    /** The Delete Task menu item. */
    protected JMenuItem deleteTaskItem;

    /** The Rename Task menu item. */
    protected JMenuItem renameMenuItem;

    /** The Hide Task menu item. */
    protected JMenuItem hideTaskItem;

    /** The Edit Task Note menu item. */
    protected JMenuItem editNoteItem;

    /** The Export Task menu item. */
    protected JCheckBoxMenuItem exportTaskItem;

    /**
     * Constructs the default popup by putting all menu items into their default
     * state.
     */
    public CommonTaskPopupMenu() {
        if(log.isTraceEnabled()) {
            log.trace("Building the CommonTaskPopupMenu");
        }
        addTaskItem = new JMenuItem("Add Task");
        addTaskItem.setActionCommand(ACTION_ADDTASK);
        add(addTaskItem);

        addTimeItem = new JMenuItem("Add Time");
        addTimeItem.setActionCommand(ACTION_ADDTIME);
        add(addTimeItem);

        addSeparator();

        deleteTaskItem = new JMenuItem("Delete Task");
        deleteTaskItem.setActionCommand(ACTION_DELTASK);
        deleteTaskItem.setEnabled(false);
        add(deleteTaskItem);

        renameMenuItem = new JMenuItem("Rename Task");
        renameMenuItem.setActionCommand(ACTION_RENTASK);
        renameMenuItem.setEnabled(false);
        add(renameMenuItem);

        exportTaskItem = new JCheckBoxMenuItem("Export Task");
        exportTaskItem.setActionCommand(ACTION_EXPORT);
        exportTaskItem.setSelected(false);
        exportTaskItem.setEnabled(false);
        add(exportTaskItem);

        hideTaskItem = new JMenuItem("Hide Task");
        hideTaskItem.setActionCommand(ACTION_HIDETASK);
        hideTaskItem.setEnabled(false);
        add(hideTaskItem);

        editNoteItem = new JMenuItem("Edit Task Note");
        editNoteItem.setActionCommand(ACTION_EDITNOTE);
        add(editNoteItem);
}

    /**
     * Sets the TaskDayPanel this popup is triggered by. This will adjust the
     * names of all the menu items that affect a particular task.
     *
     * @param taskDayPanel the panel triggering the popup
     */
    public void setTaskDayPanel(TaskDayPanel taskDayPanel) {
        this.taskDayPanel = taskDayPanel;

        if (taskDayPanel != null) {
            deleteTaskItem.setText(
                    "Delete Task ["
                    + taskDayPanel.getTimelordTask().getTaskName() + "]");
            deleteTaskItem.setEnabled(true);
            renameMenuItem.setText(
                    "Rename Task ["
                    + taskDayPanel.getTimelordTask().getTaskName() + "]");
            renameMenuItem.setEnabled(true);
            exportTaskItem.setText(
                    "Export Task ["
                    + taskDayPanel.getTimelordTask().getTaskName() + "]");
            exportTaskItem.setEnabled(true);
            exportTaskItem.setSelected(
                    taskDayPanel.getTimelordTask().isExportable());

            if (taskDayPanel.getTimelordTask().isHidden()) {
                hideTaskItem.setText(
                        "Show Task ["
                        + taskDayPanel.getTimelordTask().getTaskName()
                        + "]");
                hideTaskItem.setEnabled(true);
            } else {
                hideTaskItem.setText(
                        "Hide Task ["
                        + taskDayPanel.getTimelordTask().getTaskName()
                        + "]");
                hideTaskItem.setEnabled(true);
            }

            editNoteItem.setText(
                    "Edit Task Note ["
                    + taskDayPanel.getTimelordTask().getTaskName() + "]");
            editNoteItem.setEnabled(true);
        } else {
            deleteTaskItem.setText("Delete Task");
            deleteTaskItem.setEnabled(false);
            renameMenuItem.setText("Rename Task");
            renameMenuItem.setEnabled(false);
            exportTaskItem.setText("Export Task");
            exportTaskItem.setSelected(false);
            exportTaskItem.setEnabled(false);
            hideTaskItem.setText("Hide Task");
            hideTaskItem.setEnabled(false);
            editNoteItem.setText("Edit Task Note");
            editNoteItem.setEnabled(false);
        }
    }

    /**
     * Gets the taskDayPanel that triggered the last popup.
     *
     * @return task day panel for last popup
     */
    public TaskDayPanel getTaskDayPanel() {
        return this.taskDayPanel;
    }

    /**
     * Adds an action listener for all the menu items.
     *
     * @param l the listener
     */
    public void addActionListener(ActionListener l) {
        addTaskItem.addActionListener(l);
        addTimeItem.addActionListener(l);
        deleteTaskItem.addActionListener(l);
        renameMenuItem.addActionListener(l);
        hideTaskItem.addActionListener(l);
        exportTaskItem.addActionListener(l);
        editNoteItem.addActionListener(l);
    }

    /**
     * Removes an action listener from all the menu items.
     *
     * @param l the listener
     */
    public void removeActionListener(ActionListener l) {
        addTaskItem.removeActionListener(l);
        addTimeItem.removeActionListener(l);
        deleteTaskItem.removeActionListener(l);
        renameMenuItem.removeActionListener(l);
        hideTaskItem.removeActionListener(l);
        exportTaskItem.removeActionListener(l);
        editNoteItem.removeActionListener(l);
    }
}
