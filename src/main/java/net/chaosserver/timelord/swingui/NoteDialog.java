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

import net.chaosserver.timelord.data.TimelordTask;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.Date;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;


/**
 * A simple dialog box with a text area inside to add/edit a note for a
 * particular task day.
 *
 * @author Jordan Reed
 */
@SuppressWarnings("serial")
public class NoteDialog extends JDialog implements ActionListener {
    /** Action Event for OK Button. */
    private static final String ACTION_OK =
        NoteDialog.class.getName() + ".ACTION_OK";

    /** Action Event for Cancel Button. */
    private static final String ACTION_CANCEL =
        NoteDialog.class.getName() + ".ACTION_CANCEL";

    /** Holds the parent frame of the dialog. */
    protected JFrame parentFrame;

    /** Holds the task to edit a note for. */
    protected TimelordTask timelordTask;

    /** The date of the note to be edited. */
    protected Date date;

    /** Format for the date in the title. */
    protected DateFormat titleDateFormat = new SimpleDateFormat("MM-dd-yyyy");

    /** Text are to display and allow editing of the note. */
    protected JTextArea textArea;

    /**
     * Cteate the dialog with the given frame as the owner.
     *
     * @param frame the owner frame
     */
    public NoteDialog(JFrame frame) {
        super(frame, "Edit Note", true);
        this.parentFrame = frame;
        this.getContentPane().setLayout(new BorderLayout());

        textArea = new JTextArea(
                    LayoutConstants.SMALL_TEXTAREA_HEIGHT,
                    LayoutConstants.SMALL_TEXTAREA_WIDTH);

        JScrollPane scrollPane = new JScrollPane(textArea);

        // textArea.setEditable(false);
        this.getContentPane().add(scrollPane, BorderLayout.CENTER);
        this.getContentPane().add(createButtonPanel(), BorderLayout.SOUTH);

        this.pack();
        this.setLocation(
            (int) parentFrame.getLocation().getX()
                + LayoutConstants.CHILD_FRAME_X_OFFSET,

            (int) parentFrame.getLocation().getY()
                + LayoutConstants.CHILD_FRAME_Y_OFFSET);

        this.setSize(parentFrame.getWidth(), this.getHeight());
    }

    /**
     * Sets the task that will have a note edited.
     *
     * @param timelordTask task to edit a note from
     */
    public void setTimelordTask(TimelordTask timelordTask) {
        this.timelordTask = timelordTask;
    }

    /**
     * Gets the task that is having a note edited.
     *
     * @return task to edit note from
     */
    public TimelordTask getTimelordTask() {
        return this.timelordTask;
    }

    /**
     * Sets the date the note will be edited for.
     *
     * @param date date note is edited for.
     */
    public void setDate(Date date) {
        this.date = date;
    }

    /**
     * Creates the panel that has the OK/Cancel buttons on it.
     *
     * @return the newly created panel
     */
    protected JPanel createButtonPanel() {
        JPanel panel = new JPanel();
        JButton okButton = new JButton("OK");
        okButton.setActionCommand(ACTION_OK);
        okButton.addActionListener(this);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setActionCommand(ACTION_CANCEL);
        cancelButton.addActionListener(this);

        panel.add(okButton);
        panel.add(cancelButton);

        return panel;
    }

    /**
     * Causes the dialog to reset it's display based on the currently assigned
     * task/date and then set visible.
     *
     * @param visible indicate if the dialog should be visible or hidden
     */
    public void setVisible(boolean visible) {
        if (visible) {
            this.setTitle(
                    "Edit Note - " + getTimelordTask().getTaskName() + " ("
                    + titleDateFormat.format(date) + ")");

            textArea.setText(getTimelordTask().getTaskDay(date).getNote());
            textArea.requestFocus();
            super.setVisible(true);
        } else {
            super.setVisible(false);
        }
    }

    /**
     * Listens for button presses in the dialog and updates the note in the data
     * object.
     *
     * @param evt the action event
     */
    public void actionPerformed(ActionEvent evt) {
        textArea.requestFocus();

        if (ACTION_OK.equals(evt.getActionCommand())) {
            String newNote = textArea.getText();

            if ((newNote != null) && newNote.trim().equals("")) {
                newNote = null;
            }

            getTimelordTask().getTaskDay(date).setNote(newNote);
            this.setVisible(false);
        } else if (ACTION_CANCEL.equals(evt.getActionCommand())) {
            this.setVisible(false);
        }
    }
}
