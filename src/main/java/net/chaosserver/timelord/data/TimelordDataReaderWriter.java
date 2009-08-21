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
package net.chaosserver.timelord.data;

import java.io.File;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Abstract class that Reader/Writers should inherit from.
 */
public abstract class TimelordDataReaderWriter {
    /** The logger. */
    private static Log logger = LogFactory.getLog(ExcelDataReaderWriter.class);

    /**
     * Reads the timelordData from a default location or location specified
     * by calling concrete setters.
     *
     * @return the newly read data
     * @throws TimelordDataException indicates an error reading
     */
    public abstract TimelordData readTimelordData()
            throws TimelordDataException;

    /**
     * Writes the given timelordData to a default location or location
     * specified by calling concrete setters.
     *
     * @param timelordData the data to write
     * @param outputFile the output file where the data will be written
     * @throws TimelordDataException indicates an error writing
     */
    public abstract void writeTimelordData(
            TimelordData timelordData, File outputFile)
        throws TimelordDataException;

    /**
     * Gets the file associated with the default location.
     *
     * @return the File for the default location
     */
    public abstract File getDefaultOutputFile();

     /**
      * Returns the basic file filter that allows everything.
      *
      * @return the basic file filter
      */
     public FileFilter getFileFilter() {
        return new BasicFileFilter();
     }

     /**
      * Basic file filter that allows everything.
      */
     public class BasicFileFilter extends FileFilter {
        /**
         * Filter logic to see if the passed in file matches
         * the required filtering technology.
         *
         * @param file the file check for filter match
         * @return if the file matches the filter
         */
        public boolean accept(File file) {
            return true;
        }

        /**
         * Returns the descriptions of the filter that appears in the
         * dialog box.
         *
         * @return the description
         */
        public String getDescription() {
            return "All Files (*.*)";
        }
     }

     /**
      * Simple file filter to filter down to XLS files.
      */
     public class ExcelFileFilter extends FileFilter {
        /**
         * Filter logic to see if the passed in file matches
         * the required filtering technology.
         *
         * @param file the file check for filter match
         * @return if the file matches the filter
         */
        public boolean accept(File file) {
            boolean result;
            String filename = file.getName();
            String regexFilter = ".xls";
            if(filename.endsWith(regexFilter)) {
                if(logger.isTraceEnabled()) {
                    logger.trace("FilterMatch of name ["
                        + filename
                        + "] to regex ["
                        + regexFilter
                        + "] matches");
                }
                result = true;
            } else {
                if(logger.isTraceEnabled()) {
                    logger.trace("FilterMatch of name ["
                        + filename
                        + "] to regex ["
                        + regexFilter
                        + "] does not match");
                }
                result = false;
            }

            return result;
        }

        /**
         * Returns the descriptions of the filter that appears in the
         * dialog box.
         *
         * @return the description
         */
        public String getDescription() {
            return "Microsoft Excel Workbook (*.xls)";
        }
     }
}
