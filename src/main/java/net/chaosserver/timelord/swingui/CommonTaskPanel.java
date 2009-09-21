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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import net.chaosserver.timelord.data.TimelordData;
import net.chaosserver.timelord.data.TimelordDayView;
import net.chaosserver.timelord.data.TimelordTask;
import net.chaosserver.timelord.data.TimelordTaskDay;
import net.chaosserver.timelord.util.DateUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * A basic panel used to display a set of TimelordTasks for a particular day.
 * If the date being displayed is today, then a summary bar is shown across the
 * bottom.
 *
 * @author Jordan Reed
 */
@SuppressWarnings("serial")
public class CommonTaskPanel extends JPanel implements ActionListener,
    PropertyChangeListener {
    /** The logger. */
    private static Log log = LogFactory.getLog(CommonTaskPanel.class);

    /** The TimelordData object that is the main data. */
    protected TimelordData timelordData;

    /** The TimelordDayView object that information is pulled from. */
    protected TimelordDayView timelordDayView;

    /** The date that is used. */
    protected Date dateDisplayed;

    /** The task name filter string. */
    protected String tasknameFilter;

    /** Holds the search text. */
    protected JTextField searchTextField;

    /** The inner task panel holding the tasks. */
    protected JPanel commonTaskPanel;

    /** The summary panel at the bottom if this is display today. */
    protected SummaryPanel summaryPanel;

    /** The popup menu for right click on a task. */
    protected CommonTaskPopupMenu popupMenu;

    /** The dialog to allow a person to input notes for a task. */
    protected NoteDialog noteDialog;

    /** Action Event for Exit. */
    private static final String ACTION_SEARCHTEXT =
        TimelordMenu.class.getName() + ".ACTION_SEARCHTEXT";


    /**
     * The main constructor to setup the display.
     *
     * @param timelordData the data object being displayed.
     * @param dateDisplayed the date to be displayed.
     * @param tasknameFilter a filter string for the tasks displayed
     * @see #setDateDisplayed
     */
    public CommonTaskPanel(TimelordData timelordData,
            Date dateDisplayed,
            String tasknameFilter) {

        setTimelordData(timelordData);
        setTimelordDayView(
            new TimelordDayView(timelordData, dateDisplayed));
        setDateDisplayed(dateDisplayed);
        this.tasknameFilter = tasknameFilter;

        setLayout(new BorderLayout());

        add(buildSearchPanel(), BorderLayout.NORTH);

        summaryPanel = new SummaryPanel(
                getTimelordDayView(),
                getDateDisplayed());

        add(summaryPanel, BorderLayout.SOUTH);

        commonTaskPanel = new JPanel();
        commonTaskPanel.setLayout(
            new BoxLayout(commonTaskPanel, BoxLayout.PAGE_AXIS));
        
        JPanel packingPanel = new JPanel();
        packingPanel.setLayout(new BorderLayout());
        packingPanel.add(commonTaskPanel, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(packingPanel);
        buildTaskList();
        add(scrollPane, BorderLayout.CENTER);

        popupMenu = new CommonTaskPopupMenu();
        popupMenu.addActionListener(this);
        commonTaskPanel.addMouseListener(new PopupListener());
    }

    /**
     * Builds the search panel at the top of the page.
     *
     * @return the search panel
     */
    protected JPanel buildSearchPanel() {
        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

        searchTextField = new JTextField();
        searchTextField.setColumns(50);
        searchTextField.setActionCommand(ACTION_SEARCHTEXT);
        searchTextField.addActionListener(this);
        searchPanel.add(searchTextField);

        return searchPanel;
    }


    /**
     * Setter for the dateDisplayed property.
     *
     * @param dateDisplayed Whatever is passed in will be rolled back to 0's for
     * hour, minute, second, milisecond.
     */
    public void setDateDisplayed(Date dateDisplayed) {
        this.dateDisplayed = DateUtil.trunc(dateDisplayed);
    }

    /**
     * Getter for the date displayed property.
     *
     * @return the date being displayed
     */
    public Date getDateDisplayed() {
        return this.dateDisplayed;
    }

    /**
     * Sets the task name filter for this view.  This will trigger
     * a re-filtering and update of the task list.
     *
     * @param tasknameFilter a taskname filter
     */
    public void setTasknameFilter(String tasknameFilter) {
        String oldTasknameFilter = this.tasknameFilter;
        this.tasknameFilter = tasknameFilter;

        if((oldTasknameFilter == null && this.tasknameFilter != null)
                || !oldTasknameFilter.equals(this.tasknameFilter)) {

            this.buildTaskList();
        }
    }

    /**
     * Returns the taskname filter that is applied to this task
     * panel.
     *
     * @return the filter for this panel
     */
    public String getTasknameFitler() {
        return this.tasknameFilter;
    }

    /**
     * Setter for the timelordDayView object.
     *
     * @param timelordDayView the new value for this object
     */
    public void setTimelordDayView(TimelordDayView timelordDayView) {
        if (this.timelordDayView != null) {
            this.timelordDayView.removePropertyChangeListener(this);
        }

        this.timelordDayView = timelordDayView;

        if (timelordDayView != null) {
            this.timelordDayView.addPropertyChangeListener(this);
        }
    }

    /**
     * Getter for the timelordDayView property.
     *
     * @return value fot he property
     */
    public TimelordDayView getTimelordDayView() {
        return this.timelordDayView;
    }

    /**
     * Setter for the timelordData object.
     *
     * @param timelordData the new value for this object
     */
    public void setTimelordData(TimelordData timelordData) {
        if (this.timelordData != null) {
            this.timelordData.removePropertyChangeListener(this);
        }

        this.timelordData = timelordData;

        if (timelordData != null) {
            this.timelordData.addPropertyChangeListener(this);
        }
    }

    /**
     * Getter for the timelordData property.
     *
     * @return value fo the property
     */
    public TimelordData getTimelordData() {
        return this.timelordData;
    }

    /**
     * Checks if the date being displayed is equal to the start of the current
     * time.
     *
     * @return indicates if the date is the start of the current time
     */
    public boolean isToday() {
        boolean result = false;

        Date todayDate = DateUtil.trunc(new Date());

        if (todayDate.equals(getDateDisplayed())) {
            result = true;
        }

        return result;
    }

    /**
     * Builds the common task list in the main panel. This cycles through all
     * components currently in the task list and disposes of them. Then removes
     * them from the container. Afterwards it gets all TimelordTaskDays
     * associated with the displayDate and creates TaskDayPanels for them. This
     * is a slow operation and should be avoided.
     */
    protected synchronized void buildTaskList() {
        // Dispose of the child components.
        disposeChildComponents();

        // For good measure null out the listeners
        if (popupMenu != null) {
            popupMenu.setTaskDayPanel(null);
        }

        // Remove the components.
        commonTaskPanel.removeAll();

        // After all components have been disposed of, cycle through all
        // the timelord days for ones with the display date and generate
        // new TaskDayPanels for each of them.
        Collection<TimelordTask> taskCollection =
            getTimelordDayView().getTaskCollection();

        Iterator<TimelordTask> taskCollectionIterator =
            taskCollection.iterator();

        int i = 0;

        while (taskCollectionIterator.hasNext()) {
            TimelordTask timelordTask =
                (TimelordTask) taskCollectionIterator.next();

            TimelordTaskDay todayTaskDay;

            if (isToday()) {
                todayTaskDay = timelordTask.getToday();
            } else {
                todayTaskDay = timelordTask.getTaskDay(
                        this.getDateDisplayed());
            }

            // If the task filter is on, show what matches fitler.
            // If not on, show all non-hidden and hidden with time tracked.
            boolean showLine = false;
            if(tasknameFilter == null || tasknameFilter.trim().length() == 0) {
                showLine = !timelordTask.isHidden()
                    || todayTaskDay.getHours() > 0;

            } else {
                showLine = isFilterPassed(timelordTask, tasknameFilter);
            }

            if (showLine) {
                TaskDayPanel taskDayPanel;

                if (todayTaskDay != null) {
                    taskDayPanel = new TaskDayPanel(timelordTask,
                            todayTaskDay);
                } else {
                    taskDayPanel =
                        new TaskDayPanel(timelordTask, getDateDisplayed());
                }

                // If this does not pass, then leave the original
                // color.
                if ((i % 2) != 0) {
                    Color color = taskDayPanel.getBackground();
                    Color color2 = new Color(
                        color.getRed() + LayoutConstants.LIGHTEN_AMOUNT,
                        color.getGreen() + LayoutConstants.LIGHTEN_AMOUNT,
                        color.getBlue() + LayoutConstants.LIGHTEN_AMOUNT);

                    taskDayPanel.setBackground(color2);
                }

                commonTaskPanel.add(taskDayPanel);
                i++;
            }
        }

        // Now that we have rebuilt with todays data (or possibly build for
        // the first time) it may have triggered the creation of a bunch of
        // task days for today. So we reset the today listeners. This should
        // be the TimelordDayView's responsility, but putting it here allows
        // for a performance increase.
        // TODO jdr - This really shouldn't be the views responsibility.
        getTimelordData().resetTaskListeners();

        // Force the container to re-layout the components in the layout
        // manager.
        this.doLayout();
        this.validate();
    }

    /**
     * Test if a given task passes the filter test or not.
     *
     * @param timelordTask the task to check the filter on
     * @param tasknameFilter the keyword list being filtered
     * @return if the filter is passed
     */
    protected boolean isFilterPassed(TimelordTask timelordTask,
            String tasknameFilter) {

        boolean result = false;
        String upperTaskName = timelordTask.getTaskName().toUpperCase();
        String upperTasknameFilter = tasknameFilter.toUpperCase();

        if(tasknameFilter == null) {
            result = true;
        } else {
            String[] upperTaskFilterTokens = upperTasknameFilter.split("\\s+");

            result = true;
            for(int i = 0; i < upperTaskFilterTokens.length; i++) {
                if(!upperTaskName.contains(upperTaskFilterTokens[i])) {
                    result = false;
                    break;
                }
            }
        }

        if(log.isDebugEnabled()) {
            log.debug("Test of match of task named ["
                    + upperTaskName
                    + "] against filter ["
                    + upperTasknameFilter
                    + "] returned: "
                    + result);
        }
        return result;
    }

    /**
     * Bring up a simple dialog to allow the user to add a task.
     */
    public void showAddTaskDialog() {
        String taskName =
            JOptionPane.showInputDialog(
                this,
                "New Task Name",
                "Add Task",
                JOptionPane.QUESTION_MESSAGE);

        if (taskName != null) {
            getTimelordData().addTask(taskName);
        }
    }

    /**
     * Brings up a simple dialog to allow the user to search for
     * tasks and then add time to specific task.
     */
    public void showFindTask() {
        searchTextField.requestFocusInWindow();
    }

    /**
     *  Cycles through all non-hidden tasks and asks to hide any
     *  that do not have data in the last 30 days.
     */
    public void showHideTaskDialog() {
        List<TimelordTask> timelordTasks =
            getTimelordData().getTaskCollection();

        Iterator<TimelordTask> timelordTaskIterator =
            timelordTasks.iterator();

        Calendar oldestDateCalendar = Calendar.getInstance();
        oldestDateCalendar.add(Calendar.MONTH, -1);
        DateUtil.trunc(oldestDateCalendar);
        Date oldestDate = oldestDateCalendar.getTime();

        while(timelordTaskIterator.hasNext()) {
            TimelordTask timelordTask = timelordTaskIterator.next();
            if(!timelordTask.isHidden()) {
                List<TimelordTaskDay> timelordTaskDays =
                    timelordTask.getTaskDayList();

                Iterator<TimelordTaskDay> timelordTaskDayIterator =
                    timelordTaskDays.iterator();

                TASKDAY_LOOP: while(timelordTaskDayIterator.hasNext()) {
                    TimelordTaskDay timelordTaskDay =
                        timelordTaskDayIterator.next();

                    if(timelordTaskDay.getHours() > 0) {
                        Date lastInputDate = timelordTaskDay.getDate();

                        if(oldestDate.after(lastInputDate)) {
                            int result = showHideTaskDialog(
                                            timelordTask.getTaskName(),
                                            lastInputDate);

                            if (result == 0) {
                                timelordTask.setHidden(true);
                            }
                        }

                        // After the oldest date has been found, break
                        // out of the date loop.
                        break TASKDAY_LOOP;
                    }
                }
            }
        }
    }

    /**
     * Shows a dialog the gives the user the option to hide a task
     * that has not been written to for a long time.
     *
     * @param taskName the name of the task to prompt the user to hide
     * @param lastInputDate the last date the user added time to the task
     *
     * @return Either (0) for hiding the task or (1) for continuing to show
     */
    protected int showHideTaskDialog(String taskName, Date lastInputDate) {
        int result;

        String hideTask = "Hide Task";
        String ignore = "Ignore";
        Object[] options = { hideTask, ignore };

        result = JOptionPane.showOptionDialog(
                null,
                "No time has been tracked to the task ["
                    + taskName
                    + "] since ["
                    + DateUtil.BASIC_DATE_FORMAT.format(lastInputDate)
                    + "].  Do you wish to hide it?",

                "Hide Very Old Task",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                hideTask
            );

        return result;
    }

    /**
     * Bring up a single dialog to allow a user to unhide a task.
     */
    public void showUnhideTaskDialog() {
        List<TimelordTask> hiddenTasks = getTimelordData().getHiddenTasks();
        TimelordTask timelordTask =
            (TimelordTask) JOptionPane.showInputDialog(
                this,
                "Unhide Task",
                "Unhide Task",
                JOptionPane.QUESTION_MESSAGE,
                null,
                hiddenTasks.toArray(),
                null);

        if (timelordTask != null) {
            timelordTask.setHidden(false);
        }
    }

    /**
     * Shows the add time dialog to let the user add time to a task.
     */
    public void showAddTimeDialog() {
        JFrame frame =
            (JFrame) SwingUtilities.getAncestorOfClass(JFrame.class, this);

        AddTimeDialog addTimeDialog =
            new AddTimeDialog(frame, getTimelordData(), getDateDisplayed());
        addTimeDialog.setVisible(true);
        addTimeDialog.dispose();
    }

    /**
     * Bring up a dialog to allow a user to edit the note associated with a
     * task.
     *
     * @param timelordTask the task to edit the note on
     */
    public void showEditNoteDialog(TimelordTask timelordTask) {
        // If the noteDialog doesn't exist, create it. But don't dispose
        // of it after use. It's simple enough that it's easy to re-use it.
        if (this.noteDialog == null) {
            JFrame frame =
                (JFrame) SwingUtilities.getAncestorOfClass(JFrame.class, this);
            this.noteDialog = new NoteDialog(frame);
        }

        noteDialog.setTimelordTask(timelordTask);
        noteDialog.setDate(getDateDisplayed());
        SwingUtil.repairLocation(noteDialog);
        noteDialog.setVisible(true);
    }

    /**
     * Disposes of this component and should be called before removing from a
     * display. Removes all of property changes listeners registered to this
     * component and also disposes of any sub-components.
     */
    public void dispose() {
        // Dispose of all the child components.
        disposeChildComponents();

        // Dispose of these listeners.
        timelordData.removePropertyChangeListener(this);
        timelordDayView.removePropertyChangeListener(this);
        timelordDayView.dispose();

        if (noteDialog != null) {
            noteDialog.dispose();
        }
    }

    /**
     * Cycle through all of the containers components and call dispose where
     * appropriate.
     */
    protected void disposeChildComponents() {
        if (log.isDebugEnabled()) {
            log.debug("Disposing of all child components.");
        }

        // Get all of the components currently added to the container.
        // For every component, call this dispose method to allow the
        // component to dispose of any listener activities.
        Component[] componentArray = commonTaskPanel.getComponents();

        for (int i = 0; i < componentArray.length; i++) {
            if (componentArray[i] instanceof TaskDayPanel) {
                TaskDayPanel taskDayPanel = (TaskDayPanel) componentArray[i];

                if (log.isDebugEnabled()) {
                    log.debug(
                            "Disposing of child taskDayPanel [" + taskDayPanel
                            + ".");
                }

                taskDayPanel.dispose();
            /*
            } else if (componentArray[i] instanceof SummaryPanel) {
                // Nothing to do for the summary panel
            } else if (componentArray[i] instanceof JScrollPane) {
                // Nothing for the scroll pane
            */
            } else {
                if (log.isWarnEnabled()) {
                    log.warn(
                            "Unknown component of type ["
                            + componentArray[i].getClass().getName() + "]");
                }
            }
        }
    }

    /**
     * Catch action events generated by the popup menu.
     *
     * @param evt the event
     */
    public void actionPerformed(ActionEvent evt) {
        if (log.isTraceEnabled()) {
            log.trace(
                    "Got ActionEvent.getActionCommand ["
                    + evt.getActionCommand() + "]");
        }

        if (CommonTaskPopupMenu.ACTION_ADDTASK.equals(evt.getActionCommand())) {
            showAddTaskDialog();
        } else if (CommonTaskPopupMenu.ACTION_ADDTIME.equals(
                        evt.getActionCommand())) {
            showAddTimeDialog();
        } else if (CommonTaskPopupMenu.ACTION_DELTASK.equals(
                        evt.getActionCommand())) {
            TaskDayPanel taskDayPanel = popupMenu.getTaskDayPanel();

            if (taskDayPanel != null) {
                TimelordTask timelordTask =
                    taskDayPanel.getTimelordTask();

                int result =
                    JOptionPane.showConfirmDialog(
                        this,
                        "Delete [" + timelordTask.getTaskName() + "] Task?",
                        "Delete Task",
                        JOptionPane.YES_NO_OPTION);

                if (result == 0) {
                    getTimelordData().removeTask(timelordTask);
                }
            }
        } else if (CommonTaskPopupMenu.ACTION_RENTASK.equals(
                        evt.getActionCommand())) {
            TaskDayPanel taskDayPanel = popupMenu.getTaskDayPanel();
            TimelordTask timelordTask = taskDayPanel.getTimelordTask();

            String newName =
                JOptionPane.showInputDialog(
                    this,
                    "Rename Task",
                    timelordTask.getTaskName());

            if (newName != null) {
                if (taskDayPanel != null) {
                    timelordTask.setTaskName(newName);
                    getTimelordData().sortTaskCollection();
                }
            }
        } else if (CommonTaskPopupMenu.ACTION_EXPORT.equals(
                    evt.getActionCommand())) {
            TaskDayPanel taskDayPanel = popupMenu.getTaskDayPanel();

            if (taskDayPanel != null) {
                TimelordTask timelordTask =
                    taskDayPanel.getTimelordTask();
                timelordTask.setExportable(!timelordTask.isExportable());
            }
        } else if (CommonTaskPopupMenu.ACTION_HIDETASK.equals(
                        evt.getActionCommand())) {
            TaskDayPanel taskDayPanel = popupMenu.getTaskDayPanel();

            if (taskDayPanel != null) {
                TimelordTask timelordTask =
                    taskDayPanel.getTimelordTask();

                if (timelordTask.isHidden()) {
                    timelordTask.setHidden(false);
                } else {
                    timelordTask.setHidden(true);
                }
            }
        } else if (CommonTaskPopupMenu.ACTION_EDITNOTE.equals(
                        evt.getActionCommand())) {
            TaskDayPanel taskDayPanel = popupMenu.getTaskDayPanel();

            if (taskDayPanel != null) {
                TimelordTask timelordTask =
                    taskDayPanel.getTimelordTask();
                showEditNoteDialog(timelordTask);
            }
        } else if(ACTION_SEARCHTEXT.equals(evt.getActionCommand())) {
            if(log.isDebugEnabled()) {
                log.debug("Executing search with given input:"
                    + searchTextField.getText());
                this.setTasknameFilter(searchTextField.getText());
            }
        }

    }

    /**
     * Hides the application window out of site and mind on Win and Mac.
     */
    protected void hideFrame() {
        JFrame frame =
            (JFrame) SwingUtilities.getAncestorOfClass(JFrame.class, this);
        frame.toBack();

        // Use reflection for compile-time avoidance.
        try {
            Class<?> nsApplicationClass =
                Class.forName("com.apple.cocoa.application.NSApplication");
            Method sharedAppMethod =
                nsApplicationClass.getDeclaredMethod(
                    "sharedApplication",
                    new Class<?>[] {  });

            Object nsApplicationObject =
                sharedAppMethod.invoke(null, new Object[] {  });

            /*
            Field userAttentionRequestCriticalField =
                nsApplicationClass.getDeclaredField(
                    "UserAttentionRequestCritical");
            */

            Method hideMethod =
                nsApplicationClass.getDeclaredMethod(
                    "hide",
                    new Class[] { Object.class });

            hideMethod.invoke(
                nsApplicationObject,
                new Object[] { nsApplicationObject });
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Issue bouncing dock", e);
            }
        }
    }

    /**
     * Catch all property events generated by TimelordDayView.
     *
     * @param evt the event
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if (log.isDebugEnabled()) {
            log.debug("Got propertyChange [" + evt.getPropertyName() + "]");
        }

        // This will set the focus on the panel, and more importantly,
        // remove the focus from any particular button.
        requestFocus();

        if (log.isTraceEnabled()) {
            log.trace(
                    "Got PropertyChangeEvent.getPropertyName ["
                    + evt.getPropertyName() + "]");
        }

        if ("viewTaskList".equals(evt.getPropertyName())) {
            buildTaskList();
        } else if ("totalTimeToday".equals(evt.getPropertyName())) {
            summaryPanel.updateTotalTimeLabel();
            summaryPanel.updateTimeLeftLabel();

            /*
            if (isToday()) {
                double untrackedTimeLeftToday =
                    getTimelordDayView().getUntrackedTime();

                if (untrackedTimeLeftToday
                    <= DateUtil.getSmallestTimeIncremented()) {

                    Preferences preferences =
                        Preferences.userNodeForPackage(Timelord.class);

                    if (Timelord.ANNOYANCE_JORDAN.equals(
                                    preferences.get(
                                        Timelord.ANNOYANCE_MODE,
                                        null))) {
                        // hideFrame();
                    }
                }
            }
            */
        } else if ("untrackedTimeLeftToday".equals(evt.getPropertyName())) {
            summaryPanel.updateTimeLeftLabel();
            summaryPanel.updateTotalTimeLabel();
        } else if ("dayStartTime".equals(evt.getPropertyName())) {
            summaryPanel.updateTimeLeftLabel();
            summaryPanel.updateTotalTimeLabel();
        }
    }

    /**
     * Simple popup listener to catch the right-click events.
     */
    protected class PopupListener extends MouseAdapter {
        /**
         * Test for showing popup.
         *
         * @param evt event to test
         */
        public void mousePressed(MouseEvent evt) {
            maybeShowPopup(evt);
        }

        /**
         * Test for showing popup.
         *
         * @param evt event to test
         */
        public void mouseReleased(MouseEvent evt) {
            maybeShowPopup(evt);
        }

        /**
         * Check if a popup is triggered. If there is a popup, get the
         * TaskDayPanel that generated the popup and set it on the menu before
         * showing so the menu will render correctly and act on the proper task.
         *
         * @param evt the event to trigger the popup
         */
        protected void maybeShowPopup(MouseEvent evt) {
            if (evt.isPopupTrigger()) {
                Component component =
                    SwingUtilities.getDeepestComponentAt(
                        evt.getComponent(),
                        evt.getX(),
                        evt.getY());
                TaskDayPanel taskDayPanel =
                    (TaskDayPanel) SwingUtilities.getAncestorOfClass(
                            TaskDayPanel.class,
                            component);
                popupMenu.setTaskDayPanel(taskDayPanel);

                popupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
            }
        }
    }
}
