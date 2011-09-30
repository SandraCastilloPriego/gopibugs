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
    double repThreshold;
    Random rand;
    Range range;

    public Cell(int bugLife, double repThreshold, int maxVariable) {
        this.rand = new Random();
        this.maxVariable = maxVariable;
        this.repThreshold = repThreshold;
        this.bugLife = bugLife;

    }

    public void setParameters(String sampleName, Range range, String type) {
        this.sampleName = sampleName;
        this.bugsInside = new ArrayList<Bug>();
        this.range = range;
        this.type = type;
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
                    if (o1.getSpecSenAverage() < o2.getSpecSenAverage()) {
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
                    if (mother != father && father.isClassify() && mother.isClassify() && mother.getSpecSenAverage() > this.repThreshold && father.getSpecSenAverage() > this.repThreshold && mother.getAge() > (this.bugLife / 3) && father.getAge() > (this.bugLife / 3)) {
                            mother.addLife();
                            childs.add(new Bug(mother, father, mother.getDataset(), bugLife, maxVariable));
                    }
                }
            }
            return childs;
        } catch (Exception e) {
            return null;
        }
    }
}
