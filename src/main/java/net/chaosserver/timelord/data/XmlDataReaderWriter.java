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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Writer;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.Calendar;
import java.util.zip.GZIPOutputStream;


/**
 * The main ReaderWriter that uses the XMLEncoder/XMLDecoder to write the data
 * out to file. This is a very fast operation but does not produce an outfile
 * meant to be read by other applications.
 */
public class XmlDataReaderWriter extends TimelordDataReaderWriter
    implements FilenameFilter {
    /** Logger. */
    private static Log log =
        LogFactory.getLog(TimelordDataReaderWriter.class);

    /** Default filename not including the extension. */
    public static final String DEFAULT_FILENAME = "TimelordData";

    /** Default extension used for the output file. */
    public static final String DEFAULT_EXTENSION = ".xml";

    /**
     * The format for the start of the week used to generate the backup files
     * during writing.
     */
    protected DateFormat startWeekFormat = new SimpleDateFormat("MM-dd-yyyy");

    /**
     * Gets the file associated with the default location.
     *
     * @return the File for the default location
     */
    public File getDefaultOutputFile() {
        File homeDirectory = new File(System.getProperty("user.home"));
        File defaultFile =
            new File(homeDirectory, (DEFAULT_FILENAME + DEFAULT_EXTENSION));

        return defaultFile;
    }

    /**
     * Reads the timelordData using the default file name.
     *
     * @return a timelordData object as read from file.
     * @throws TimelordDataException indicates an error reading in the file
     */
    public TimelordData readTimelordData()
        throws TimelordDataException {

        /*
         * Need to do a sed on these two items:
         * net.chaosserver.timetracker.data.TimeTrackerData =
         *     net.chaosserver.timelord.data.TimelordData
         *
         * net.chaosserver.timetracker.data.TimeTrackerTask =
         *     net.chaosserver.timelord.data.TimelordTask
         */
        TimelordData timelordData;

        /*
        File homeDirectory = new File(System.getProperty("user.home"));
        File[] datafile = homeDirectory.listFiles(this);
        */
        File datafile = new File(System.getProperty("user.home")
                + File.separatorChar + DEFAULT_FILENAME + DEFAULT_EXTENSION);

        if(!datafile.exists()) {
            File oldfile = new File(System.getProperty("user.home")
                + File.separatorChar + "TimeTrackerData" + DEFAULT_EXTENSION);

            if(oldfile.exists()) {
                if (log.isInfoEnabled()) {
                    log.info("Found older version of the file, "
                            + "running the converstion.");
                }
                try {
                    convertTrackerToLord(oldfile, datafile);
                } catch (IOException e) {
                    throw new TimelordDataException(
                            "Failed to convert file", e);
                }
            }
        }

        if (datafile.exists()) {
            try {
                FileInputStream fileInputStream =
                    new FileInputStream(datafile);
                BufferedInputStream bufferedInputStream =
                    new BufferedInputStream(fileInputStream);
                XMLDecoder xmlDecoder = new XMLDecoder(bufferedInputStream);
                timelordData = (TimelordData) xmlDecoder.readObject();
                timelordData.setTimelordReaderWriter(this);

                if (log.isInfoEnabled()) {
                    log.info(
                            "Finished loading [" + datafile + "]");
                }
            } catch (FileNotFoundException e) {
                throw new TimelordDataException("Failed to read", e);
            }
        } else {
            if (log.isInfoEnabled()) {
                log.info(
                        "Failed to find [" + datafile + "]");
            }
            timelordData = new TimelordData();
        }

        return timelordData;
    }

    /**
     * Converts and old XML file from the "TimeTracker" application into
     * the new version with "Timelord" data.
     *
     * @param oldfile the old file from TimeTracker
     * @param datafile the new file for Timelord
     * @throws IOException indicates and error processing the file
     */
    protected void convertTrackerToLord(File oldfile, File datafile)
            throws IOException {

        BufferedReader reader = new BufferedReader(new FileReader(oldfile));
        Writer writer = new BufferedWriter(new FileWriter(datafile));

        String line = reader.readLine();
        while(line != null) {
            line = line.replace("timetracker", "timelord");
            line = line.replace("TimeTracker", "Timelord");
            writer.write(line);
            line = reader.readLine();
        }

        reader.close();
        writer.close();
    }

    /**
     * Filter based on the default file name.
     *
     * @param dir the directory to filter
     * @param name the name of the file to filter against
     * @return if the file given passes the filter
     */
    public boolean accept(File dir, String name) {
        boolean result = false;

        if ((DEFAULT_FILENAME + DEFAULT_EXTENSION).equalsIgnoreCase(name)) {
            result = true;
        }

        return result;
    }

    /**
     * Writes out the timelordData object to the default filename in the
     * user's home directory. Also generates a backup version of the file.
     *
     * @param timelordData the data to write to file
     * @param outputFile the file to output to
     * @throws TimelordDataException indicates an error writing the
     *         data out to file.
     */
    public void writeTimelordData(TimelordData timelordData,
        File outputFile) throws TimelordDataException {
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_YEAR, -1);

        File homeDirectory = new File(System.getProperty("user.home"));

        try {
            FileOutputStream fileOutputStream =
                new FileOutputStream(outputFile);
            BufferedOutputStream bufferedOutputStream =
                new BufferedOutputStream(fileOutputStream);

            XMLEncoder xmlEncoder = new XMLEncoder(bufferedOutputStream);
            xmlEncoder.writeObject(timelordData);
            xmlEncoder.close();

            File backupFile =
                new File(
                        homeDirectory,
                        (DEFAULT_FILENAME + "."
                        + startWeekFormat.format(yesterday.getTime())
                        + DEFAULT_EXTENSION
                        + ".gzip"));
            fileOutputStream = new FileOutputStream(backupFile);
            bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
            GZIPOutputStream zipOutputStream =
                new GZIPOutputStream(bufferedOutputStream);

            xmlEncoder = new XMLEncoder(zipOutputStream);
            xmlEncoder.writeObject(timelordData);
            xmlEncoder.close();
        } catch (FileNotFoundException e) {
            throw new TimelordDataException("Failed to output", e);
        } catch (IOException e) {
            throw new TimelordDataException("Failed to output", e);
        }
    }
}
