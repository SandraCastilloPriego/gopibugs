/*
 * Copyright 2010
 * This file is part of XXXXXX.
 * XXXXXX is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * XXXXXX is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * XXXXXX; if not, write to the Free Software Foundation, Inc., 51 Franklin St,
 * Fifth Floor, Boston, MA 02110-1301 USA
 */
package alvs.modules.simulation;

import alvs.util.Range;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 *
 * @author bicha
 */
public class Cell {

        String type;
        String sampleName;
        List<Bug> bugsInside;
        int bugLife, maxVariable;
        Random rand;
        Range range;

        public Cell(int bugLife, int maxVariable) {
                this.rand = new Random();
                this.maxVariable = maxVariable;
                this.bugLife = bugLife;

        }

        public void setParameters(String sampleName, Range range, String type) {
                this.sampleName = sampleName;
                this.bugsInside = new ArrayList<Bug>();
                this.range = range;
                this.type = type;
        }

        public void setMaxVariable(int maxVariable) {
                this.maxVariable = maxVariable;
        }

        public Range getRange() {
                return range;
        }

        public void addBug(Bug bug) {
                this.bugsInside.add(bug);
        }

        public void removeBug(Bug bug) {
                this.bugsInside.remove(bug);
        }

        public String getSampleName() {
                return this.sampleName;
        }

        public String getType() {
                return this.type;
        }

        public synchronized List<Bug> reproduction() {
                try {
                        Comparator<Bug> c = new Comparator<Bug>() {

                                public int compare(Bug o1, Bug o2) {
                                        if (o1.getCombinedMetric() < o2.getCombinedMetric()) {
                                                return 1;
                                        } else {
                                                return -1;
                                        }
                                }
                        };
                        List<Bug> childs = new ArrayList<Bug>();
                        if (bugsInside.size() > 1) {
                                Collections.sort(bugsInside, c);
                                Bug mother = bugsInside.get(0);
                                for (Bug father : this.bugsInside) {
                                        if (mother.getAge() > (this.bugLife / 3) && father.getAge() > (this.bugLife / 3)) {
                                                if (isKilling(mother.getCombinedMetric() - father.getCombinedMetric())) {
                                                        father.kill();
                                                       // mother.increaseEnergy();
                                                } else if (isKilling(father.getCombinedMetric() - mother.getCombinedMetric())) {
                                                        mother.kill();
                                                      //  father.increaseEnergy();
                                                } else if (mother != father && mother.isClassified() && father.isClassified()) {
                                                        mother.addLife();
                                                        childs.add(new Bug(mother, father, mother.getDataset(), bugLife, maxVariable));
                                                }
                                        }
                                }
                        }
                        return childs;
                } catch (Exception e) {
                        return null;
                }
        }

        private boolean isKilling(double difference) {
                if (difference < 0) {
                        return false;
                }
                double value = Math.random();
                if (value < difference) {
                        return true;
                } else {
                        return false;
                }
        }
}
