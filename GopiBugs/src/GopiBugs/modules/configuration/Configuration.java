/*
 * Copyright 2007-2010 VTT Biotechnology
 * This file is part of GopiBugs.
 *
 * GopiBugs is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * GopiBugs is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * GopiBugs; if not, write to the Free Software Foundation, Inc., 51 Franklin St,
 * Fifth Floor, Boston, MA 02110-1301 USA
 */
package GopiBugs.modules.configuration;

import GopiBugs.data.BugDataset;
import GopiBugs.data.ParameterSet;
import GopiBugs.data.impl.SimpleParameterSet;
import GopiBugs.desktop.Desktop;
import GopiBugs.desktop.GopiBugsMenu;
import GopiBugs.desktop.impl.DesktopParameters;
import GopiBugs.main.GopiBugsCore;
import GopiBugs.main.GopiBugsModule;
import GopiBugs.taskcontrol.Task;
import GopiBugs.taskcontrol.TaskListener;
import GopiBugs.taskcontrol.TaskStatus;
import GopiBugs.util.GUIUtils;
import GopiBugs.util.dialogs.ExitCode;
import GopiBugs.util.dialogs.ParameterSetupDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.logging.Logger;

/**
 *
 * @author scsandra
 */
public class Configuration implements GopiBugsModule, TaskListener, ActionListener {

        private Logger logger = Logger.getLogger(this.getClass().getName());
        private Desktop desktop;
        final String helpID = GUIUtils.generateHelpID(this);
        SimpleParameterSet configurationParameters;
        private DesktopParameters deskParameters;

        public void initModule() {
                this.configurationParameters = new ConfigurationParameters();
                this.desktop = GopiBugsCore.getDesktop();

                desktop.addMenuItem(GopiBugsMenu.CONFIGURATION, "Configuration..",
                        "Configuration", KeyEvent.VK_C, this, null, null);

                deskParameters = (DesktopParameters) desktop.getParameterSet();
                deskParameters.setSaveConfigurationParameters(configurationParameters);
        }

        public void taskStarted(Task task) {
                logger.info("Configuration");
        }

        public void taskFinished(Task task) {
                if (task.getStatus() == TaskStatus.FINISHED) {
                        logger.info("Finished Configuration ");
                }

                if (task.getStatus() == TaskStatus.ERROR) {

                        String msg = "Error while Configuration  .. ";
                        logger.severe(msg);
                        desktop.displayErrorMessage(msg);

                }
        }

        public void actionPerformed(ActionEvent e) {
                ExitCode exitCode = setupParameters();
                if (exitCode != ExitCode.OK) {
                        return;
                }
                deskParameters.setSaveConfigurationParameters(configurationParameters);
        }

        public ExitCode setupParameters() {
                try {
                        ParameterSetupDialog dialog = new ParameterSetupDialog("Configuration parameters", configurationParameters);
                        dialog.setVisible(true);
                        return dialog.getExitCode();
                } catch (Exception exception) {
                        return ExitCode.CANCEL;
                }
        }

        public ParameterSet getParameterSet() {
                return this.configurationParameters;
        }

        public void setParameters(ParameterSet parameterValues) {
                this.configurationParameters = (SimpleParameterSet) parameterValues;
        }

        public String toString() {
                return "Configuration";
        }
}
