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
package net.chaosserver.timelord.swingui;

import java.awt.Dimension;
import java.util.Calendar;

import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import net.chaosserver.timelord.data.TimelordData;
import net.chaosserver.timelord.data.TimelordDayView;
import net.chaosserver.timelord.util.DateUtil;

/**
 * The Charting Panel is used just to give some eye candy for the
 * historical data that is being charted for the user.
 *
 * @author jordan
 */
@SuppressWarnings("serial")
public class ChartingPanel extends JPanel {
    /** logger. */
    private static Log log = LogFactory.getLog(ChartingPanel.class);

    /** Indicates how far to go back in time when showing the chart. */
    public static final int FIRST_DATE = 90;

    /** The default chart width. */
    public static final int CHART_WIDTH = 500;

    /** The default chart height. */
    public static final int CHART_HEIGHT = 270;

    /** Hold the data object that is being charted. */
    protected TimelordData timelordData;

    /**
     * Creates the charting panel.
     *
     * @param timelordData the data object to be charted.
     */
    public ChartingPanel(TimelordData timelordData) {
        this.timelordData = timelordData;

        CategoryDataset dataset = createDataset();
        JFreeChart chart = createChart(dataset);
        ChartPanel chartPanel = new ChartPanel(chart, false);
        chartPanel.setPreferredSize(new Dimension(CHART_WIDTH, CHART_HEIGHT));
        add(chartPanel);

    }

    /**
     * Creates the Chart Object for display.
     *
     * @param dataset the dataset to be charted
     * @return the chart object for display
     */
    private JFreeChart createChart(CategoryDataset dataset) {
        JFreeChart chart = ChartFactory.createBarChart3D(
                "Hours Worked Per Day",       // chart title
                "Category",               // domain axis label
                "Value",                  // range axis label
                dataset,                  // data
                PlotOrientation.VERTICAL, // orientation
                true,                     // include legend
                true,                     // tooltips?
                false                     // URLs?
            );

        return chart;
    }

    /**
     * Creates the dataset to be charted.
     *
     * @return the dataset to be charted.
     */
    private CategoryDataset createDataset() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        Calendar calendar = Calendar.getInstance();
        DateUtil.trunc(calendar);
        calendar.add(Calendar.DATE, -FIRST_DATE);

        String series1 = "Total Hours";
        for(int i = 0; i < FIRST_DATE; i++) {
            if(Calendar.SATURDAY != calendar.get(Calendar.DAY_OF_WEEK)
                    && Calendar.SUNDAY != calendar.get(Calendar.DAY_OF_WEEK)) {

                TimelordDayView timelordDayView =
                    new TimelordDayView(timelordData, calendar.getTime());

                double totalTime = timelordDayView.getTotalTimeToday(false);

                if(log.isTraceEnabled()) {
                    log.trace("Creating Value of totalTime ["
                            + totalTime
                            + "], series1 = "
                            + series1
                            + " category ["
                            + calendar.getTime()
                            + "]");
                }
                dataset.addValue(totalTime, series1, calendar.getTime());


                timelordDayView.dispose();
            }
            calendar.add(Calendar.DATE, 1);
        }

        return dataset;
    }


}
