package com.mobiletradingpartners.chartindicatorslib.ui;

import android.content.Context;
import android.graphics.RectF;

import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarLineScatterCandleBubbleData;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.formatter.YAxisValueFormatter;
import com.mobiletradingpartners.chartindicatorslib.Logger;

import java.text.DecimalFormat;
import java.util.List;


public class IndicatorView extends AbstractChartPlot {

    private static final String CLASSNAME = "IndicatorView";

    private final int DEFAULT_DATA_IDX = 0;

    public IndicatorView(Context context) {
        super(context);
    }

    public void onNewDataReady(List<String> newLabels) {

        clear();

        CombinedData data = new CombinedData(newLabels);

        for(BarLineScatterCandleBubbleData current : createIndicatorData(newLabels)) {
            data.setData(current);
        }

        yAxis.setValueFormatter(new IndicatorValueFormatter(indicatorsOutput.get(DEFAULT_DATA_IDX).getFormatter()));

        setData(data);
        //set it always after setting data
        setVisibleXRangeMinimum(1f);
        if(resetChartZoom) {
            RectF rect = getViewPortHandler().getContentRect();

            if (getScaleX() != neededScaleFactor) {
                zoom(neededScaleFactor / getScaleX(), 1, rect.left, getHeight() / 2);
            }
            moveViewToX(newLabels.size() - 1 - 0.1f);
            handleChartRedraw = true;
        }

    }

    public int getSingleYLabelLength() {
        DecimalFormat df = indicatorsOutput.get(DEFAULT_DATA_IDX).getFormatter();
        float min = indicatorsOutput.get(DEFAULT_DATA_IDX).getMin();
        float max = indicatorsOutput.get(DEFAULT_DATA_IDX).getMax();
        float value = String.valueOf(min).length() > String.valueOf(max).length() ? min : max;
        Logger.log(Logger.LogMode.DEBUG, LOG_TAG, "getSingleYLabelLength. value = " + value);
        return String.valueOf(df.format(value)).length();
    }

    private class IndicatorValueFormatter implements YAxisValueFormatter {

        private DecimalFormat format;

        public IndicatorValueFormatter(DecimalFormat format) {
            this.format = format;
        }

        @Override
        public String getFormattedValue(float value, YAxis yAxis) {
            return format.format(value);
        }
    }

}
