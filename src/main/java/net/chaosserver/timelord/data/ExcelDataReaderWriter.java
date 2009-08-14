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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.filechooser.FileFilter;

import net.chaosserver.timelord.util.OsUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFPrintSetup;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

/**
 * This is the main reader/writer to export to a very pretty excel format using
 * the POI libraries.
 */
public class ExcelDataReaderWriter extends TimelordDataReaderWriter {
    /** The logger. */
    private static Log logger = LogFactory.getLog(ExcelDataReaderWriter.class);

    /**
     * Default file name that this writes to in the home directory.
     */
    public static final String DEFAULT_FILENAME = "TimelordData.xls";

    /**
     * The max column for a row. This should always be 8 with the task name at
     * column 0 and the days as 1-7.
     */
    protected static final short MAX_COLUMN = 8;

    /**
     * The format for the dates.
     */
    protected DateFormat sheetNameFormat = new SimpleDateFormat("MM-dd-yyyy");

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
    public TimelordData readTimelordData() {

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
        if(logger.isDebugEnabled()) {
            logger.debug("Outputting file named [" + outputFile + "]");
        }

        try {
            FileOutputStream fileOut = new FileOutputStream(outputFile);
            wb.write(fileOut);
            fileOut.close();
        } catch (Exception e) {
            throw new TimelordDataException("error saving", e);
        }
        try {
            if(OsUtil.isMac()) {
                if(logger.isDebugEnabled()) {
                    logger.debug("Opening on Mac with [open "
                        + outputFile.getCanonicalPath()
                        + "]");
                }
                Runtime.getRuntime().exec(
                        new String[] {"open",
                                outputFile.getCanonicalPath()});
            } else {
                if(logger.isDebugEnabled()) {
                    logger.debug("Opening on PC with [cmd.exe /c "
                        + outputFile.getCanonicalPath()
                        + "]");
                }
                Runtime.getRuntime().exec(
                        new String[] {"cmd.exe", "/c",
                                outputFile.getCanonicalPath()});
            }
        } catch (IOException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Unable to run the program associated with "
                        + "Excel files.", e);
            }
        }

    }

    /**
     * Builds a map of style name to HSSFCellStyle objects that can be used to
     * mark cells with similar styles.
     *
     * @param wb the workbook needed to create the objects
     * @return the map of styles
     */
    protected Map<String,HSSFCellStyle> buildStyleMap(HSSFWorkbook wb) {
        Map<String, HSSFCellStyle> styleMap =
            new HashMap<String, HSSFCellStyle>();

        HSSFCellStyle style;
        HSSFFont font;

        style = wb.createCellStyle();
        font = wb.createFont();
        // font.setItalic(true);
        font.setColor((short) 0xc); // blue
        style.setFont(font);
        styleMap.put("taskNoteStyle", style);

        style = wb.createCellStyle();
        font = wb.createFont();
        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        style.setFont(font);
        style.setBorderLeft(HSSFCellStyle.BORDER_DOUBLE);
        style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        styleMap.put("taskNameHeaderStyle", style);

        style = wb.createCellStyle();
        style.setBorderLeft(HSSFCellStyle.BORDER_DOUBLE);
        styleMap.put("taskNameStyle", style);

        style = wb.createCellStyle();
        style.setBorderTop(HSSFCellStyle.BORDER_DOUBLE);
        style.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
        styleMap.put("topRowStyle", style);

        style = wb.createCellStyle();
        style.setBorderTop(HSSFCellStyle.BORDER_DOUBLE);
        style.setBorderLeft(HSSFCellStyle.BORDER_DOUBLE);
        styleMap.put("topLeftStyle", style);

        style = wb.createCellStyle();
        style.setBorderTop(HSSFCellStyle.BORDER_DOUBLE);
        style.setBorderRight(HSSFCellStyle.BORDER_DOUBLE);
        styleMap.put("topRightStyle", style);

        style = wb.createCellStyle();
        font = wb.createFont();
        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        style.setFont(font);
        style.setBorderRight(HSSFCellStyle.BORDER_DOUBLE);
        style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        style.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
        styleMap.put("totalHeaderStyle", style);

        style = wb.createCellStyle();
        style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        style.setBorderRight(HSSFCellStyle.BORDER_DOUBLE);
        styleMap.put("totalColumnStyle", style);

        style = wb.createCellStyle();
        style.setDataFormat(HSSFDataFormat.getBuiltinFormat("m/d/yy"));
        style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        style.setFont(font);
        styleMap.put("boldDateStyle", style);

        style = wb.createCellStyle();
        style.setBorderTop(HSSFCellStyle.BORDER_THIN);
        style.setBorderBottom(HSSFCellStyle.BORDER_DOUBLE);
        style.setBorderLeft(HSSFCellStyle.BORDER_DOUBLE);
        styleMap.put("bottomLeftStyle", style);

        style = wb.createCellStyle();
        style.setBorderTop(HSSFCellStyle.BORDER_THIN);
        style.setBorderBottom(HSSFCellStyle.BORDER_DOUBLE);
        style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        style.setBorderRight(HSSFCellStyle.BORDER_DOUBLE);
        styleMap.put("bottomRightStyle", style);

        style = wb.createCellStyle();
        style.setBorderTop(HSSFCellStyle.BORDER_THIN);
        style.setBorderBottom(HSSFCellStyle.BORDER_DOUBLE);
        styleMap.put("bottomStyle", style);

        return styleMap;
    }

    /**
     * Generates the actual workbook of data.
     *
     * @param timelordData the data to generate a workbook for
     * @return the workbook
     */
    protected HSSFWorkbook generateWorkbook(TimelordData timelordData) {
        HSSFWorkbook wb = new HSSFWorkbook();

        // Build the Map of the Styles that will be applied to cells
        // in the workbook
        Map<String, HSSFCellStyle> styleMap = buildStyleMap(wb);
        Map<String, List<String>> sheetToNotes =
            new TreeMap<String, List<String>>(new DateComparator());


        // Since there is an issue re-ordering sheets after they
        // have been created.  First create the book with all needed
        // sheets
        preCreateAllSheets(wb, timelordData, sheetToNotes, styleMap);

        // After all the sheets have been pre-created, iterate through all
        // the tasks to add them into the sheets.
        int rowNum = addAllTasks(wb, timelordData, sheetToNotes, styleMap);

        // This section applies all the styles, creates the footers and adds
        // the notes onto the sheet.
        for (int i = 0; i < wb.getNumberOfSheets(); i++) {
            HSSFSheet sheet = wb.getSheetAt(i);
            String sheetName = wb.getSheetName(i);
            createFooterRows(sheet, rowNum, styleMap);

            // This will apply styles to the rows that had no task associated
            // for a given week.
            for (int j = 1; j < rowNum - 1; j++) {
                HSSFRow row = sheet.getRow(j);
                if (row == null) {
                    row = sheet.createRow(j);
                    row.setHeight((short) 0);
                    HSSFCell cell = row.createCell((short) 0);
                    cell.setCellStyle((HSSFCellStyle) styleMap
                            .get("taskNameStyle"));
                    cell.setCellValue("");

                    cell = row.createCell(MAX_COLUMN);
                    cell.setCellStyle((HSSFCellStyle) styleMap
                            .get("totalColumnStyle"));
                    cell.setCellFormula("SUM(B" + (j + 1) + ":H" + (j + 1)
                            + ")");
                }
            }

            List<String> noteList = sheetToNotes.get(sheetName);
            createNotesRows(sheet, noteList);

            HSSFPrintSetup ps = sheet.getPrintSetup();
            ps.setLandscape(true);
        }

        // Finally order the sheets properly
        if (logger.isDebugEnabled()) {
            logger.debug("Re-ordering sheets under final order.");
        }

        return wb;
    }

    /**
     * This creates all of the sheets for the workbook.  The sheets
     * have names to associated with the proper dates, but have no data
     * filled into them.
     *
     * This also builds out the sheetToNotes map by associating all the
     * notes data in the application with the sheet that the data will
     * eventually be written out to.
     *
     * @param workbook the workbook to create sheets on
     * @param timelordData the timelord data used for the sheets.
     * @param sheetToNotes the map of sheets to the notes associated with it
     * @param styleMap the map of styles
     */
    protected void preCreateAllSheets(HSSFWorkbook workbook,
            TimelordData timelordData,
            Map<String, List<String>> sheetToNotes,
            Map<String, HSSFCellStyle> styleMap) {

        // This holds the most recent date in the entire data file.
        // This date starts out as null, and any time a task if found
        // with a more recent date it replaces this value.
        Date mostRecentDate = null;

        // This holds the oldest date in the entire data file.
        // This date starts out null and any time a task is found
        // with an older date it is replaced with this one.
        Date oldestDate = null;

        List<TimelordTask> taskCollection = timelordData.getTaskCollection();
        Iterator<TimelordTask> taskIterator = taskCollection.iterator();

        // Iterator through all the tasks in the data to create the
        // output sheet
        while (taskIterator.hasNext()) {
            TimelordTask timelordTask =
                (TimelordTask) taskIterator.next();

            // Only exportable tasks should be considered.  Tasks that aren't
            // exportable must be skipped
            if (timelordTask.isExportable()) {
                List<TimelordTaskDay> taskDayList =
                    timelordTask.getTaskDayList();

                if (!taskDayList.isEmpty()) {

                    // Since the days associated with this task aren't empty,
                    // pull out the first task.  This is the most recent day
                    // that has time tracked to it.
                    TimelordTaskDay firstDay
                        = (TimelordTaskDay) taskDayList.get(0);

                    // If the first date in this task is more recent than the
                    // most recent temporary date, replace it with this one.
                    if (mostRecentDate == null
                            || mostRecentDate.before(firstDay.getDate())) {

                        if (logger.isTraceEnabled()) {
                            logger.trace("Updating mostRecentDate from ["
                                    + mostRecentDate
                                    + "] to ["
                                    + firstDay.getDate()
                                    + "]");
                        }
                        mostRecentDate = firstDay.getDate();
                    }

                    // Just as a double check see if the earliest date is
                    // the oldest and do the replacement.
                    if (oldestDate == null
                            || oldestDate.after(firstDay.getDate())) {

                        if (logger.isTraceEnabled()) {
                            logger.trace("Updating oldestDate from ["
                                    + oldestDate
                                    + "] to ["
                                    + firstDay.getDate()
                                    + "]");
                        }
                        oldestDate = firstDay.getDate();
                    }

                    // Grab the very last item in the list.  This should
                    // be the oldest date associated with the task.
                    TimelordTaskDay lastDay = (TimelordTaskDay)
                        taskDayList.get(taskDayList.size() - 1);

                    if (mostRecentDate == null
                            || mostRecentDate.before(lastDay.getDate())) {

                        if (logger.isTraceEnabled()) {
                            logger.trace("Updating mostRecentDate from ["
                                    + mostRecentDate
                                    + "] to ["
                                    + lastDay.getDate()
                                    + "]");
                        }
                        mostRecentDate = lastDay.getDate();
                    }
                    if (oldestDate == null
                            || oldestDate.after(lastDay.getDate())) {

                        if (logger.isTraceEnabled()) {
                            logger.trace("Updating oldestDate from ["
                                    + oldestDate
                                    + "] to ["
                                    + lastDay.getDate()
                                    + "]");
                        }
                        oldestDate = lastDay.getDate();
                    }
                }
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Found the oldestDate ["
                    + oldestDate
                    + "] to the mostRecentDate ["
                    + mostRecentDate
                    + "]");
        }

        Calendar createSheetsCalendar = Calendar.getInstance();
        createSheetsCalendar.setTime(mostRecentDate);

        // Start at the most recent date in the system and roll back
        // the dates creating the sheets until all the dates have been
        // covered.
        while (createSheetsCalendar.getTime().after(oldestDate)) {
            Date weekStartDate =
                convertToWeekStart(createSheetsCalendar.getTime());

            String sheetName = sheetNameFormat.format(weekStartDate);
            HSSFSheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Creating HSSFSheet named ["
                            + sheetName
                            + "]");
                }
                sheet = workbook.createSheet(sheetName);
                sheetToNotes.put(sheetName, new ArrayList<String>());
                createHeaderRows(sheet, weekStartDate, styleMap);
            }
            createSheetsCalendar.add(Calendar.DAY_OF_YEAR, -1);
        }
    }

    /**
     * Adds all of the tasks to sheets that have already been created inside
     * the workbook and adds any notes associated with the tasks to the
     * sheetToNotes map.
     *
     * @param workbook the workbook to create sheets on
     * @param timelordData the timelord data used for the sheets.
     * @param sheetToNotes the map of sheets to the notes associated with it
     * @param styleMap the map of styles
     *
     * @return the total rows added to Excel after adding all the tasks
     */
    protected int addAllTasks(HSSFWorkbook workbook,
            TimelordData timelordData, Map<String, List<String>> sheetToNotes,
            Map<String, HSSFCellStyle> styleMap) {

        // Start on row number two.  The first row contains the header data.
        int rowNum = 2;

        List<TimelordTask> taskCollection = timelordData.getTaskCollection();
        Iterator<TimelordTask> taskIterator = taskCollection.iterator();
        while (taskIterator.hasNext()) {
            TimelordTask timelordTask = (TimelordTask) taskIterator
                    .next();
            if (timelordTask.isExportable()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Processing exportable task named ["
                        + timelordTask.getTaskName()
                        + "]");
                }

                String taskName = timelordTask.getTaskName();
                List<TimelordTaskDay> taskDayList =
                    timelordTask.getTaskDayList();

                Iterator<TimelordTaskDay> taskDayIterator =
                    taskDayList.iterator();

                while (taskDayIterator.hasNext()) {
                    TimelordTaskDay timelordTaskDay =
                        (TimelordTaskDay) taskDayIterator.next();

                    Date timelordDate = timelordTaskDay.getDate();
                    double hours = timelordTaskDay.getHours();

                    if (logger.isDebugEnabled()) {
                        logger.debug("Processing task named ["
                            + timelordTask.getTaskName()
                            + "] for date ["
                            + timelordDate
                            + "] with hours ["
                            + hours
                            + "]");
                    }

                    if(hours > 0) {
                        Date weekStartDate = convertToWeekStart(timelordDate);
                        String sheetName =
                            sheetNameFormat.format(weekStartDate);

                        HSSFSheet sheet = workbook.getSheet(sheetName);
                        if (sheet == null) {
                            throw new NullPointerException("Failed to find "
                                    + "sheet with name ["
                                    + sheetName
                                    + "]");
                        }
                        List<String> noteList = sheetToNotes.get(sheetName);

                        HSSFRow row = sheet.getRow(rowNum);
                        if (row == null) {
                            row = sheet.createRow(rowNum);

                            // First create the left column "header" with the
                            // name of the task on column 0.
                            HSSFCell cell = row.createCell((short) 0);
                            cell.setCellStyle((HSSFCellStyle) styleMap
                                    .get("taskNameStyle"));
                            cell.setCellValue(taskName);

                            // Over in the far right column create the sum
                            // column
                            cell = row.createCell(MAX_COLUMN);
                            cell.setCellStyle((HSSFCellStyle) styleMap
                                    .get("totalColumnStyle"));
                            cell.setCellFormula("SUM(B" + (rowNum + 1) + ":H"
                                    + (rowNum + 1) + ")");
                        }

                        // Process the task day and add the hours into the
                        // given row.
                        addTaskDay(row, taskName, timelordTaskDay,
                                noteList, styleMap);
                    }
                }
                rowNum++;
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Skipping non-exportable task named ["
                        + timelordTask.getTaskName()
                        + "]");
                }
            }
        }

        return rowNum;
    }

    /**
     * Adds the list of notes associated with the sheet to end of the sheet.
     *
     * @param sheet the sheet to add notes to
     * @param noteList the list of notes to add to the sheet
     */
    private void createNotesRows(HSSFSheet sheet, List<String> noteList) {
        int notesRow = sheet.getLastRowNum() + 1;

        if (noteList != null) {
            Iterator<String> noteListIterator = noteList.iterator();
            while (noteListIterator.hasNext()) {
                notesRow++;
                String note = (String) noteListIterator.next();
                HSSFRow row = sheet.createRow(notesRow);
                HSSFCell cell = row.createCell((short) 0);
                cell.setCellValue(note);
            }
        }
    }

    /**
     * Creates the footer rows for a given sheet.
     *
     * @param sheet the sheet to create the rows for.
     * @param rowNum the rownum that is the footer row
     * @param styleMap the map of styles
     */
    protected void createFooterRows(HSSFSheet sheet,
            int rowNum, Map<String, HSSFCellStyle> styleMap) {

        HSSFRow row = sheet.createRow(rowNum);

        HSSFCell cell = row.createCell((short) 0);
        cell.setCellStyle((HSSFCellStyle) styleMap.get("bottomLeftStyle"));
        cell.setCellValue("Total");

        cell = row.createCell((short) 1);
        cell.setCellStyle((HSSFCellStyle) styleMap.get("bottomStyle"));
        cell.setCellFormula("SUM(B3:B" + rowNum + ")");
        cell = row.createCell((short) 2);
        cell.setCellStyle((HSSFCellStyle) styleMap.get("bottomStyle"));
        cell.setCellFormula("SUM(C3:C" + rowNum + ")");
        cell = row.createCell((short) 3);
        cell.setCellStyle((HSSFCellStyle) styleMap.get("bottomStyle"));
        cell.setCellFormula("SUM(D3:D" + rowNum + ")");
        cell = row.createCell((short) 4);
        cell.setCellStyle((HSSFCellStyle) styleMap.get("bottomStyle"));
        cell.setCellFormula("SUM(E3:E" + rowNum + ")");
        cell = row.createCell((short) 5);
        cell.setCellStyle((HSSFCellStyle) styleMap.get("bottomStyle"));
        cell.setCellFormula("SUM(F3:F" + rowNum + ")");
        cell = row.createCell((short) 6);
        cell.setCellStyle((HSSFCellStyle) styleMap.get("bottomStyle"));
        cell.setCellFormula("SUM(G3:G" + rowNum + ")");
        cell = row.createCell((short) 7);
        cell.setCellStyle((HSSFCellStyle) styleMap.get("bottomStyle"));
        cell.setCellFormula("SUM(H3:H" + rowNum + ")");
        cell = row.createCell(MAX_COLUMN);
        cell.setCellStyle((HSSFCellStyle) styleMap.get("bottomRightStyle"));
        cell.setCellFormula("SUM(I3:I" + rowNum + ")");
    }

    /**
     * Adds the task day into a row for a task.
     *
     * @param row the row to add the task day for
     * @param taskName the name of the task to add a date for
     * @param timelordTaskDay the day to add
     * @param noteList the list of notes for the sheet.  If the task has a
     *        note added to it, that note will be appended to this list.
     * @param styleMap the map of styles to apply
     */
    private void addTaskDay(HSSFRow row, String taskName,
            TimelordTaskDay timelordTaskDay, List<String> noteList,
            Map<String, HSSFCellStyle> styleMap) {

        Calendar calendarDay = Calendar.getInstance();
        calendarDay.setTime(timelordTaskDay.getDate());
        // Monday is (1) in day of week and (1) in rownum.
        int dayOfWeek = calendarDay.get(Calendar.DAY_OF_WEEK) - 1;
        if(dayOfWeek == 0) {
            dayOfWeek = 7;
        }

        double hours = timelordTaskDay.getHours();
        HSSFCell cell = row.createCell((short) (dayOfWeek));
        if (timelordTaskDay.getNote() != null) {
            cell.setCellStyle(
                    (HSSFCellStyle) styleMap.get("taskNoteStyle"));
        }
        cell.setCellValue(hours);

        if (timelordTaskDay.getNote() != null) {
            String note =
                " ("
                + sheetNameFormat.format(timelordTaskDay.getDate())
                + ") "
                + taskName
                + " - "
                + timelordTaskDay.getNote();

            noteList.add(note);
        }
    }

    /**
     * Creates the header rows for a sheet.
     *
     * @param sheet the sheet to add header rows for
     * @param weekStartDate the start date for the week that is used to create
     * the date headers on the top
     * @param styleMap the style map for the header styles
     */
    protected void createHeaderRows(HSSFSheet sheet, Date weekStartDate,
            Map<String, HSSFCellStyle> styleMap) {
        HSSFRow headerRow = sheet.createRow(0);
        Calendar calendarDay = Calendar.getInstance();
        calendarDay.setTime(weekStartDate);
        sheet.setDefaultColumnWidth((short) 9);

        HSSFCell cell = headerRow.createCell((short) 0);
        cell.setCellStyle((HSSFCellStyle) styleMap.get("topLeftStyle"));
        cell.setCellValue("");
        sheet.setColumnWidth((short) 0, (short) 10000);

        cell = headerRow.createCell((short) 1);
        cell.setCellStyle((HSSFCellStyle) styleMap.get("topRowStyle"));
        cell.setCellValue("Monday");

        cell = headerRow.createCell((short) 2);
        cell.setCellStyle((HSSFCellStyle) styleMap.get("topRowStyle"));
        cell.setCellValue("Tuesday");

        cell = headerRow.createCell((short) 3);
        cell.setCellStyle((HSSFCellStyle) styleMap.get("topRowStyle"));
        cell.setCellValue("Wednesday");

        cell = headerRow.createCell((short) 4);
        cell.setCellStyle((HSSFCellStyle) styleMap.get("topRowStyle"));
        cell.setCellValue("Thusday");

        cell = headerRow.createCell((short) 5);
        cell.setCellStyle((HSSFCellStyle) styleMap.get("topRowStyle"));
        cell.setCellValue("Friday");

        cell = headerRow.createCell((short) 6);
        cell.setCellStyle((HSSFCellStyle) styleMap.get("topRowStyle"));
        cell.setCellValue("Saturday");

        cell = headerRow.createCell((short) 7);
        cell.setCellStyle((HSSFCellStyle) styleMap.get("topRowStyle"));
        cell.setCellValue("Sunday");

        cell = headerRow.createCell(MAX_COLUMN);
        cell.setCellStyle((HSSFCellStyle) styleMap.get("topRightStyle"));
        cell.setCellValue("");
        sheet.setColumnWidth(MAX_COLUMN, (short) 1500);

        headerRow = sheet.createRow(1);
        cell = headerRow.createCell((short) 0);
        cell.setCellValue("Task Name");
        cell.setCellStyle((HSSFCellStyle) styleMap.get("taskNameHeaderStyle"));

        cell = headerRow.createCell((short) 1);
        cell.setCellValue(calendarDay.getTime());
        cell.setCellStyle((HSSFCellStyle) styleMap.get("boldDateStyle"));

        cell = headerRow.createCell((short) 2);
        calendarDay.add(Calendar.DAY_OF_WEEK, 1);
        cell.setCellValue(calendarDay.getTime());
        cell.setCellStyle((HSSFCellStyle) styleMap.get("boldDateStyle"));

        cell = headerRow.createCell((short) 3);
        calendarDay.add(Calendar.DAY_OF_WEEK, 1);
        cell.setCellValue(calendarDay.getTime());
        cell.setCellStyle((HSSFCellStyle) styleMap.get("boldDateStyle"));

        cell = headerRow.createCell((short) 4);
        calendarDay.add(Calendar.DAY_OF_WEEK, 1);
        cell.setCellValue(calendarDay.getTime());
        cell.setCellStyle((HSSFCellStyle) styleMap.get("boldDateStyle"));

        cell = headerRow.createCell((short) 5);
        calendarDay.add(Calendar.DAY_OF_WEEK, 1);
        cell.setCellValue(calendarDay.getTime());
        cell.setCellStyle((HSSFCellStyle) styleMap.get("boldDateStyle"));

        cell = headerRow.createCell((short) 6);
        calendarDay.add(Calendar.DAY_OF_WEEK, 1);
        cell.setCellValue(calendarDay.getTime());
        cell.setCellStyle((HSSFCellStyle) styleMap.get("boldDateStyle"));

        cell = headerRow.createCell((short) 7);
        calendarDay.add(Calendar.DAY_OF_WEEK, 1);
        cell.setCellValue(calendarDay.getTime());
        cell.setCellStyle((HSSFCellStyle) styleMap.get("boldDateStyle"));

        cell = headerRow.createCell(MAX_COLUMN);
        cell.setCellValue("Total");
        cell.setCellStyle((HSSFCellStyle) styleMap.get("totalHeaderStyle"));
    }

    /**
     * Converts a given date to the date that marks the start of a week.
     * This is 12:00am on Monday.
     *
     * @param inputDate the date to find the start of week for
     * @return 12:00am on Monday the week of the input
     */
    protected Date convertToWeekStart(Date inputDate) {
        Calendar weekStartCalendar = Calendar.getInstance();
        weekStartCalendar.setTime(inputDate);
        weekStartCalendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        weekStartCalendar.set(Calendar.HOUR_OF_DAY, 0);
        weekStartCalendar.set(Calendar.MINUTE, 0);
        weekStartCalendar.set(Calendar.SECOND, 0);
        weekStartCalendar.set(Calendar.MILLISECOND, 0);

        // Since the day of week has changed from Monday it no
        // longer matches Java's natural interpretation.  So if
        // the date is a Monday, than it's important to roll back
        // the week start by a week
        Calendar inputCalendar = Calendar.getInstance();
        inputCalendar.setTime(inputDate);
        if(inputCalendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            weekStartCalendar.add(Calendar.WEEK_OF_YEAR, -1);
        }

        return weekStartCalendar.getTime();
    }

    /**
     * Comparator compares two task date "strings" to put them into
     * the proper date order.  So the strings of "01-01-2005" and
     * "01-02-2004" would be orders to have the "2004" appear first.
     */
    public class DateComparator implements Comparator<String> {
        /**
        * Comparator compares two task date "strings" to put them into
        * the proper date order.  So the strings of "01-01-2005" and
        * "01-02-2004" would be orders to have the "2004" appear first.
        *
        * @param o1 first string to compare
        * @param o2 second string to compare
        * @return the comparison
        */
        public int compare(String o1, String o2) {
            String s1 = (String) o1;
            String s2 = (String) o2;
            // MM-dd-yyyy => yyyyMM-dd-yyyy
            String dateString1 = s1.substring(6) + s1;
            String dateString2 = s2.substring(6) + s2;

            return (dateString1.compareTo(dateString2));
        }
    }

     /**
      * Return the Excel file filter.
      *
      * @return the excel file filter
      */
     public FileFilter getFileFilter() {
        return new ExcelFileFilter();
     }


}
