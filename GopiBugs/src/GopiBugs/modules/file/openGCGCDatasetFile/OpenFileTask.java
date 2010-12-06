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
package GopiBugs.modules.file.openGCGCDatasetFile;

import GopiBugs.data.parser.impl.GCGCParserXLS;
import GopiBugs.data.impl.datasets.SimpleGCGCDataset;
import GopiBugs.data.parser.Parser;
import GopiBugs.data.parser.impl.GCGCParserCSV;
import GopiBugs.taskcontrol.Task;
import GopiBugs.taskcontrol.TaskStatus;
import GopiBugs.util.GUIUtils;
import java.io.IOException;

/**
 *
 * @author scsandra
 */
public class OpenFileTask implements Task {

    private String fileDir;
    private int numColumns;
    private TaskStatus status = TaskStatus.WAITING;
    private String errorMessage;
    private Parser parser;

    public OpenFileTask(String fileDir, int numColumns) {
        if (fileDir != null) {
            this.fileDir = fileDir;
        }        
        this.numColumns = numColumns;
    }

    public String getTaskDescription() {
        return "Opening File... ";
    }

    public double getFinishedPercentage() {
        if (parser != null) {
			return parser.getProgress();
		} else {
			return 0.0f;
		}
    }

    public TaskStatus getStatus() {
        return status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void cancel() {
        status = TaskStatus.CANCELED;
    }

    public void run() {
        try {
            this.openFile();
        } catch (Exception e) {
            status = TaskStatus.ERROR;
            errorMessage = e.toString();
            return;
        }
    }

    public void openFile() {
        status = TaskStatus.PROCESSING;
        try {
            if (fileDir.matches(".*xls")) {
                try {
                    Parser parserName = new GCGCParserXLS(fileDir, null, numColumns);
                    String[] sheetsNames = ((GCGCParserXLS) parserName).getSheetNames(fileDir);
                    for (String Name : sheetsNames) {
                        try {
                            if (status != TaskStatus.CANCELED) {
                                parser = new GCGCParserXLS(fileDir, Name, numColumns);
                                parser.fillData();
                                this.open(parser);
                            }
                        } catch (Exception exception) {
                            exception.printStackTrace();
                        }
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } else if (fileDir.matches(".*csv")) {
                try {
                    if (status != TaskStatus.CANCELED) {
                        parser = new GCGCParserCSV(fileDir, numColumns);
                        parser.fillData();
                        this.open(parser);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }       
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        status = TaskStatus.FINISHED;
    }

    public void open(Parser parser) {
        try {
            if (status != TaskStatus.CANCELED) {
                SimpleGCGCDataset dataset = (SimpleGCGCDataset) parser.getDataset();
                GUIUtils.showNewTable(dataset, true);
            }
        } catch (Exception exception) {
            // exception.printStackTrace();
        }
    }
}
