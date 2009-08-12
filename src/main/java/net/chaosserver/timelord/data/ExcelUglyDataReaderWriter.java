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
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;

import javax.swing.filechooser.FileFilter;

import net.chaosserver.timelord.util.OsUtil;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

/**
 * This is a Writer requested by Doug that goes into a specific format he uses
 * for other scripts. I find the format ugly and mostly useless and thus have
 * named it so.
 */
public class ExcelUglyDataReaderWriter extends TimelordDataReaderWriter {
    /** Default output filename. */
    public static final String DEFAULT_FILENAME = "TimeLordData.xls";

    /** Date format. */
    protected DateFormat outputDateFormat = new SimpleDateFormat("MM-dd-yyyy");

    /**
     * Gets the file associated with the default location.
     *
     * @return the File for the default location
     */
    public File getDefaultOutputFile() {
        File homeDirectory = new File(System.getProperty("user.home"));
        File defaultFile = new File(homeDirectory, DEFAULT_FILENAME);

        return defaultFile;
    }

    /**
     * Default constructor will always throw an exception since this RW is not
     * intended to read in files.
     *
     * @return will never return
     * @throws UnsupportedOperationException will always throw this excpetion
     */
    public TimelordData readTimelordData()
            throws UnsupportedOperationException {

        throw new UnsupportedOperationException();
    }

    /**
     * The writer will write out a new Excel file and than attempt to trigger
     * Excel to open it.
     *
     * @param timelordData the data object to write out
     * @param outputFile the file to write data to
     * @throws TimelordDataException indicates an error writing.
     */
    public synchronized void writeTimelordData(
                TimelordData timelordData, File outputFile)
            throws TimelordDataException {

        HSSFWorkbook wb = generateWorkbook(timelordData);

        try {
            FileOutputStream fileOut = new FileOutputStream(outputFile);
            wb.write(fileOut);
            fileOut.close();
        } catch (Exception e) {
            throw new TimelordDataException("error saving", e);
        }
        try {
            if(OsUtil.isMac()) {
                Runtime.getRuntime().exec(
                        new String[] {"open",
                                outputFile.getCanonicalPath()});
            } else {
                Runtime.getRuntime().exec(
                        new String[] {"cmd.exe", "/c",
                                outputFile.getCanonicalPath()});
            }
        } catch (IOException e) {
            // TODO log.
        }

    }

    /**
     * Generates the workbook that contains all of the data for the excel
     * document
     *
     * @param timelordData the data object to generate the workbook for
     * @return the workbook of data
     */
    protected HSSFWorkbook generateWorkbook(TimelordData timelordData) {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet();
        sheet.setColumnWidth((short) 1, (short) 10000);

        HSSFCellStyle dateStyle = wb.createCellStyle();
        dateStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("m/d/yy"));

        List<TimelordTask> taskCollection = timelordData.getTaskCollection();
        Iterator<TimelordTask> taskIterator = taskCollection.iterator();
        // Calendar weekStart = Calendar.getInstance();

        // Make the headers
        HSSFRow row = sheet.createRow(0);
        HSSFCell  cell = row.createCell((short) 0);
        cell.setCellValue("Date");
        cell = row.createCell((short) 1);
        cell.setCellValue("Task Name");
        cell = row.createCell((short) 2);
        cell.setCellValue("Hours");
        cell = row.createCell((short) 3);
        cell.setCellValue("Note");


        int rowNum = 1;
        while (taskIterator.hasNext()) {
            TimelordTask timelordTask = (TimelordTask) taskIterator
                    .next();
            if (timelordTask.isExportable()) {
                String taskName = timelordTask.getTaskName();
                List<TimelordTaskDay> taskDayList =
                    timelordTask.getTaskDayList();

                Iterator<TimelordTaskDay> taskDayIterator =
                    taskDayList.iterator();

                while (taskDayIterator.hasNext()) {
                    TimelordTaskDay timelordTaskDay =
                        (TimelordTaskDay) taskDayIterator.next();

                    if (timelordTaskDay.getHours() > 0) {
                        row = sheet.createRow(rowNum);

                        cell = row.createCell((short) 0);
                        cell.setCellStyle(dateStyle);
                        cell.setCellValue(timelordTaskDay.getDate());

                        cell = row.createCell((short) 1);
                        cell.setCellValue(taskName);

                        cell = row.createCell((short) 2);
                        cell.setCellValue(timelordTaskDay.getHours());

                        cell = row.createCell((short) 3);
                        cell.setCellValue(timelordTaskDay.getNote());
                        rowNum++;
                    }
                }

            }
        }
        return wb;
    }

    public FileFilter getFileFilter() {
        return new ExcelFileFilter();
    }
}
