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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This dialog allows the user to change the default annoy time
 * smallest time setting in the system.
 */
public class AnnoyTimeDialog extends JDialog implements ActionListener {
    /** The logger. */
    private static Log log = LogFactory.getLog(AnnoyTimeDialog.class);

    /** Resource Bundle. */
    protected ResourceBundle resourceBundle =
        ResourceBundle.getBundle("TimelordResources");

    /** Action for the OK Button. */
    public static final String ACTION_OK =
        AnnoyTimeDialog.class.getName() + ".ACTION_OK";

    /** Action for the Cancel Button. */
    public static final String ACTION_CANCEL =
        AnnoyTimeDialog.class.getName() + ".ACTION_CANCEL";

    /** Slider for the minutes. */
    protected JSlider minuteSlider;

    /**
     * Constructs a annoy time dialog for setting the dialog.
     *
     * @param applicationFrame the parent frame
     */
    public AnnoyTimeDialog(JFrame applicationFrame) {
        super(applicationFrame, "Set Day Start Time", true);

        JPanel annoyTimePanel = new JPanel();

        minuteSlider = new JSlider(0, 60);
        minuteSlider.setMajorTickSpacing(15);
        minuteSlider.setMinorTickSpacing(3);
        minuteSlider.setPaintLabels(true);
        minuteSlider.setPaintTicks(true);
        minuteSlider.setSnapToTicks(true);

        Preferences preferences =
            Preferences.userNodeForPackage(Timelord.class);

        double timeIncrement =
            preferences.getDouble(Timelord.TIME_INCREMENT, 0.25);

        if(log.isDebugEnabled()) {
            log.debug("Loaded Time Increment Preference ["
                    + timeIncrement
                    + "] from preference ["
                    + Timelord.TIME_INCREMENT
                    + "]");
        }

        minuteSlider.setValue((int) (timeIncrement * 60));


        annoyTimePanel.add(minuteSlider);

        JButton okayButton = new JButton(
                resourceBundle.getString(
                        "net.chaosserver.timelord.swingui.TimelordMenu.okay"));
        okayButton.setActionCommand(ACTION_OK);
        okayButton.addActionListener(this);
        annoyTimePanel.add(okayButton);

        JButton cancelButton = new JButton(
                resourceBundle.getString(
                "net.chaosserver.timelord.swingui.TimelordMenu.cancel"));
        cancelButton.setActionCommand(ACTION_CANCEL);
        cancelButton.addActionListener(this);
        annoyTimePanel.add(cancelButton);

        this.getContentPane().add(annoyTimePanel);
        this.pack();

        this.setLocationRelativeTo(applicationFrame);
    }

    /**
     * Captures the action and processes and closes the dialog.
     *
     * @param evt the action event triggering the method
     */
    public void actionPerformed(ActionEvent evt) {
        if(ACTION_OK.equals(evt.getActionCommand())) {
            int minuteValue = minuteSlider.getValue();
            double fractionValue = minuteValue / 60d;
            if(log.isDebugEnabled()) {
                log.debug("Got back minute value ["
                        + minuteValue
                        + "] as fraction value ["
                        + fractionValue
                        + "]");
            }

            Preferences preferences =
                Preferences.userNodeForPackage(Timelord.class);

            preferences.putDouble(Timelord.TIME_INCREMENT, fractionValue);
            this.setVisible(false);

        } else if(ACTION_CANCEL.equals(evt.getActionCommand())) {
            this.setVisible(false);
        }
    }

}
