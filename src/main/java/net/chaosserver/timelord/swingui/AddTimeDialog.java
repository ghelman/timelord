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
import net.chaosserver.timelord.data.TimelordTask;
import net.chaosserver.timelord.data.TimelordTaskDay;
import net.chaosserver.timelord.util.DateUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Collection;
import java.util.Date;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * A Dialog Box used to add a single time increment to any existing
 * task or to a new task.
 */
@SuppressWarnings("serial")
public class AddTimeDialog extends JDialog implements ActionListener {
    /** The logger. */
    private static Log log = LogFactory.getLog(AddTimeDialog.class);

    /** Action Event for OK Button. */
    private static final String ACTION_OK =
        AddTimeDialog.class.getName() + ".ACTION_OK";

    /** The data object being manipulated. */
    protected TimelordData timelordData;

    /** The date time is being added to. */
    protected Date addDate;

    /** The combo box that holds the task to add time to. */
    protected JComboBox comboBox;

    /**
     * Constructor to create the add time dialog.
     *
     * @param owner the owner frame for this one.
     * @param timelordData the data object to add time to.
     * @param addDate the date time is being added to
     */
    public AddTimeDialog(Frame owner, TimelordData timelordData,
        Date addDate) {
        super(owner,
            "Timelord - Add Time "
            + DateUtil.BASIC_DATE_FORMAT.format(addDate), true);
        this.timelordData = timelordData;
        this.addDate = addDate;

        getContentPane().add(buildAddTimePanel());
        pack();
        setLocationRelativeTo(owner);
    }

    /**
     * Builds the Add Time Panel.
     *
     * @return the newly created add time panel
     */
    public JPanel buildAddTimePanel() {
        JPanel addTimePanel = new JPanel();

        Collection<TimelordTask> taskCollection =
            timelordData.getTaskCollection();

        Vector<TimelordTask> taskVector =
            new Vector<TimelordTask>(taskCollection);

        comboBox = new JComboBox(taskVector);
        comboBox.setEditable(true);
        addTimePanel.add(comboBox);

        JButton button = new JButton("+0.25");
        button.setActionCommand(ACTION_OK);
        button.addActionListener(this);
        addTimePanel.add(button);

        return addTimePanel;
    }

    /**
     * @param evt the action event
     */
    public void actionPerformed(ActionEvent evt) {
        if (ACTION_OK.equals(evt.getActionCommand())) {
            Object selectedItem = comboBox.getSelectedItem();

            if (log.isDebugEnabled()) {
                log.debug(
                        "ComboBox selected item is ["
                        + ((selectedItem == null) ? "null"
                                                  : selectedItem.getClass()
                                                                .getName())
                        + "] with value ["
                        + ((selectedItem == null) ? "null" : selectedItem)
                        + "]");
            }

            TimelordTask timelordTask = null;

            if (selectedItem instanceof TimelordTask) {
                timelordTask = (TimelordTask) selectedItem;
            } else if (selectedItem instanceof String) {
                String taskName = (String) selectedItem;
                int result =
                    JOptionPane.showConfirmDialog(
                        this,
                        "Add new [" + taskName + "] Task?",
                        "Add Task",
                        JOptionPane.YES_NO_OPTION);

                if (result == 0) {
                    timelordTask = timelordData.addTask(taskName);
                }
            }

            if (timelordTask != null) {
                TimelordTaskDay timelordTaskDay =
                    timelordTask.getTaskDay(addDate, true);

                timelordTaskDay.addHours(
                    DateUtil.SMALL_TIME_INCREMENT_HOUR);

                if (timelordTask.isHidden()) {
                    timelordTask.setHidden(true);
                }

                this.setVisible(false);
            }
        }
    }
}
