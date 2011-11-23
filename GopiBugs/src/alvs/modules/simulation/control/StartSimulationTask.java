/*
 * Copyright 2007-2010 VTT Biotechnology
 * This file is part of ALVS.
 *
 * ALVS is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * ALVS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * ALVS; if not, write to the Free Software Foundation, Inc., 51 Franklin St,
 * Fifth Floor, Boston, MA 02110-1301 USA
 */
package alvs.modules.simulation.control;

import alvs.data.BugDataset;
import alvs.data.DatasetType;
import alvs.data.PeakListRow;
import alvs.data.impl.SimpleParameterSet;
import alvs.desktop.impl.DesktopParameters;
import alvs.main.ALVSCore;
import alvs.modules.configuration.ConfigurationParameters;
import alvs.modules.simulation.Bug;
import alvs.modules.simulation.CanvasWorld;
import alvs.modules.simulation.Result;
import alvs.modules.simulation.TestBug;
import alvs.modules.simulation.World;
import alvs.modules.simulation.classifiersEnum;
import alvs.taskcontrol.TaskStatus;
import alvs.util.Range;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import java.util.List;
import java.util.Random;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import org.apache.commons.math.optimization.fitting.PolynomialFitter;

/**
 *
 * @author scsandra
 */
public class StartSimulationTask {

        private BugDataset training, validation;
        private TaskStatus status = TaskStatus.WAITING;
        private String errorMessage;
        private sinkThread thread;
        private CanvasWorld canvas;
        private JPanel canvasPanel;
        private World world;
        private JInternalFrame frame;
        private int numberOfBugsCopies, worldSize, bugLife, iterations, maxBugs = 1000;
        private classifiersEnum classifier = classifiersEnum.Automatic_Selection;
        private JTextArea textArea;
        private List<Range> ranges;
        private Random rand;
        private int totalIDs, stoppingCriteria, stopCounting = 0, counter = 0;
        private double minCountId = 100;
        private List<Result> results;
        private boolean showResults, showCanvas;

        public StartSimulationTask(BugDataset[] datasets, SimpleParameterSet parameters) {
                for (BugDataset dataset : datasets) {
                        if (dataset.getType() == DatasetType.TRAINING) {
                                training = dataset;
                                this.totalIDs = training.getNumberRows();
                        } else if (dataset.getType() == DatasetType.VALIDATION) {
                                validation = dataset;
                        }
                }
                this.numberOfBugsCopies = (Integer) parameters.getParameterValue(StartSimulationParameters.numberOfBugs);
                this.worldSize = (Integer) parameters.getParameterValue(StartSimulationParameters.worldSize);
                this.bugLife = (Integer) parameters.getParameterValue(StartSimulationParameters.bugLife);
                this.iterations = (Integer) parameters.getParameterValue(StartSimulationParameters.iterations);
                this.maxBugs = (Integer) parameters.getParameterValue(StartSimulationParameters.bugLimit);
                this.stoppingCriteria = (Integer) parameters.getParameterValue(StartSimulationParameters.stoppingCriteria);
                this.stoppingCriteria = (Integer) parameters.getParameterValue(StartSimulationParameters.stoppingCriteria);
                this.classifier = (classifiersEnum) parameters.getParameterValue(StartSimulationParameters.classifier);



                DesktopParameters desktopParameters = (DesktopParameters) ALVSCore.getDesktop().getParameterSet();
                ConfigurationParameters configuration = (ConfigurationParameters) desktopParameters.getSaveConfigurationParameters();
                this.showCanvas = (Boolean) configuration.getParameterValue(ConfigurationParameters.showCanvas);
                this.showResults = (Boolean) configuration.getParameterValue(ConfigurationParameters.showResults);


                this.ranges = new ArrayList<Range>();
                this.results = new ArrayList<Result>();
                this.rand = new Random();
        }

        public String getTaskDescription() {
                return "Start simulation... ";
        }

        public double getFinishedPercentage() {
                return 0.0f;
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
                        status = TaskStatus.PROCESSING;
                        frame = new JInternalFrame("Simulation", true, true, true, true);
                        frame.setSize(new Dimension(700, 700));
                        frame.setLayout(new FlowLayout());
                        canvasPanel = new JPanel();
                        canvasPanel.setLayout(new FlowLayout());
                        canvasPanel.setSize(new Dimension(700, 700));
                        canvasPanel.setVisible(true);
                        frame.add(canvasPanel);
                        ALVSCore.getDesktop().addInternalFrame(frame);


                        JInternalFrame frame2 = new JInternalFrame("Results", true, true, true, true);
                        frame2.setSize(new Dimension(700, 700));

                        textArea = new JTextArea("");
                        textArea.setSize(new Dimension(700, 700));
                        JScrollPane panel = new JScrollPane(textArea);
                        frame2.add(panel);
                        ALVSCore.getDesktop().addInternalFrame(frame2);

                        // Creates the ranges of samples that will be used in the world
                        createRanges();

                        // Selects one of the ranges randomly
                        int index = rand.nextInt(ranges.size() - 1);
                        Range range = ranges.get(index);

                        // Starts the simulation (first cicle with the selected range of samples)
                        this.startCicle(range, null, null, 1, null, false);

                        status = TaskStatus.FINISHED;
                } catch (Exception e) {
                        e.printStackTrace();
                        status = TaskStatus.ERROR;
                }
        }

        private void startCicle(Range range, List<Bug> bugs, List<Result> results, int maxVariables, PolynomialFitter fitter, boolean fixNumber) {
                DesktopParameters desktopParameters = (DesktopParameters) ALVSCore.getDesktop().getParameterSet();
                ConfigurationParameters configuration = (ConfigurationParameters) desktopParameters.getSaveConfigurationParameters();

                this.showCanvas = (Boolean) configuration.getParameterValue(ConfigurationParameters.showCanvas);
                this.showResults = (Boolean) configuration.getParameterValue(ConfigurationParameters.showResults);

                world = new World(training, validation, this.worldSize, range, bugs, this.numberOfBugsCopies, this.bugLife, textArea, results, this.maxBugs, maxVariables, this.classifier, fitter, fixNumber);
                if (bugs == null) {
                        world.setVariables();
                }
                canvas = new CanvasWorld(world);
                canvasPanel.removeAll();
                canvasPanel.add(canvas);
                canvas.setVisible(true);
                thread = new sinkThread();
                thread.start();
        }

        private void createRanges() {
                int cont = 0;
                int unit = training.getNumberCols() / 10;
                while (cont < 11) {
                        this.ranges.add(new Range(unit * cont, (unit * cont) + unit));
                        cont++;
                }

        }

        private int countIDs() {
                int count = 0;
                List<Integer> alreadyCount = new ArrayList<Integer>();
                for (Bug bug : world.getBugs()) {
                        for (PeakListRow row : bug.getRows()) {
                                if (!alreadyCount.contains(row.getID())) {
                                        alreadyCount.add(row.getID());
                                        count++;
                                }

                        }
                }
                double result = ((double) ((double) count / (double) this.totalIDs)) * 100;
                System.out.println("Count : " + count + "/" + this.totalIDs + " result: " + result + "%");
                return count;
        }

        public class sinkThread extends Thread {

                @Override
                public void run() {
                        for (int i = 0; i < iterations; i++) {
                                if (showCanvas) {
                                        // Paints the graphics
                                        canvas.update(canvas.getGraphics());
                                }
                                world.cicle();
                                world.addNewPoint();
                        }
                        counter++;
                        if (counter > 10) {
                                world.setVariables();
                                counter = 0;
                        }
                        int index = rand.nextInt(ranges.size() - 1);
                        Range range = ranges.get(index);
                        if (showResults) {
                                printResult(world.getBugs(), range);
                        }


                        // Counting the number of variables in the world
                        int count = countIDs();
                        double result = ((double) ((double) count / (double) training.getNumberRows())) * 100;
                        if (result < minCountId) {
                                minCountId = result;
                                stopCounting = 0;
                        } else {
                                stopCounting++;
                        }


                        // Checking the stopping criteria
                        if (result <= stoppingCriteria || stopCounting > 25000) {
                                printResult(world.getBugs(), range);
                        } else {
                                startCicle(range, world.getBugs(), world.getResult(), world.getMaxVariables(), world.getfitter(), world.getFixNumber());
                        }
                }
        }

        public void printResult(List<Bug> bugs, Range range) {

                Comparator<Result> c = new Comparator<Result>() {

                        public int compare(Result o1, Result o2) {
                                if (o1.count < o2.count) {
                                        return 1;
                                } else {
                                        return -1;
                                }
                        }
                };

                for (Bug bug : bugs) {
                        if (bug.getAge() > ((this.bugLife / 3) * 2)) {
                                Result result = new Result();
                                result.Classifier = bug.getClassifierType().name();
                                List<Integer> ids = new ArrayList<Integer>();
                                for (PeakListRow row : bug.getRows()) {
                                        result.addValue(String.valueOf(row.getID()));
                                        ids.add(row.getID());
                                }

                                TestBug testing = new TestBug(ids, bug.getClassifierType(), training, validation);
                                double[] values = testing.prediction();
                                result.tspecificity = values[0];
                                result.tsensitivity = values[1];
                                result.aucT = values[2];
                                result.vspecificity = values[3];
                                result.vsensitivity = values[4];
                                result.aucV = values[5];
                                boolean isIt = false;
                                for (Result r : this.results) {
                                        if (r.isIt(result.getValues(), result.Classifier)) {
                                                r.count();
                                                isIt = true;
                                        }
                                }

                                if (!isIt) {
                                        this.results.add(result);
                                }
                        }

                }


                Collections.sort(results, c);


                int contbug = 0;
                String result = range.toString() + " \n";

                for (Result r : results) {
                        result += r.toString();
                        contbug++;
                        if (contbug > 500) {
                                break;
                        }
                }

                this.textArea.setText(result);

        }
}
