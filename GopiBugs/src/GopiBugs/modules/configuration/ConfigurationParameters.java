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

import GopiBugs.data.Parameter;
import GopiBugs.data.ParameterType;
import GopiBugs.data.impl.SimpleParameter;
import GopiBugs.data.impl.SimpleParameterSet;

public class ConfigurationParameters extends SimpleParameterSet {

        public static final Parameter showResults = new SimpleParameter(
                ParameterType.BOOLEAN, "Write results",
                "Write the results in each iteration", false);
        public static final Parameter showCanvas = new SimpleParameter(
                ParameterType.BOOLEAN, "Paint simulation",
                "Paint the bugs into the canvas", false);

        public ConfigurationParameters() {
                super(new Parameter[]{showResults, showCanvas});
        }
}