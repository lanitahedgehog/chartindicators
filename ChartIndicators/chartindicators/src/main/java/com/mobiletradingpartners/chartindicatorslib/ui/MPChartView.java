package com.mobiletradingpartners.chartindicatorslib.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;

import com.github.mikephil.charting.components.Annotation;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarLineScatterCandleBubbleData;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.formatter.YAxisValueFormatter;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.renderer.CombinedChartRenderer;
import com.github.mikephil.charting.renderer.DataRenderer;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.mobiletradingpartners.chartindicatorslib.R;
import com.mobiletradingpartners.chartindicatorslib.Tools;
import com.mobiletradingpartners.chartindicatorslib.Logger;

import java.text.DecimalFormat;
import java.util.List;

import static com.github.mikephil.charting.charts.CombinedChart.DrawOrder.*;

/**
 * CandleStick chart that initially calculates needed scale based on the required candle width.
 */
public class MPChartView extends AbstractChartPlot {

    public static final String CLASSNAME = "MPChartView";

    private final int CANDLE_STICK_WIDTH = 9;

    private boolean annotationEnabled;
    private Annotation currentMarketPrice;

    //used for calculating needed initial scale only once
    private boolean calculateNeededScale;

    private boolean highlightEnabled;
    private boolean drawHighlight;
    private float highlightTextSize = 28;

    public MPChartView(Context context) {
        this(context, null);
    }

    public MPChartView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MPChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.MPChartView, 0, 0);
        try {
            annotationEnabled = a.getBoolean(R.styleable.MPChartView_enableAnnotationRefactor, true);
            highlightEnabled = a.getBoolean(R.styleable.MPChartView_enableHighlightRefactor, true);
        } finally {
            a.recycle();
        }

        initSelf(context);
    }

    private void initSelf(Context context) {

        setHighlightPerTapEnabled(false);
        setHighlightsEnabled(highlightEnabled);
        setAnnotationEnabled(annotationEnabled);
        setDrawOrder(new DrawOrder[]{CANDLE, BAR, LINE, BUBBLE, SCATTER});

        highlightTextSize = convertSpToPx(context, getResources().getInteger(R.integer.highlight_text_size));
        calculateNeededScale = true;

    }

    public void setListener(OnChartGestureListener listener) {
        setOnChartGestureListener(listener);
    }

    public void onNewDataReady(List<CandleEntry> newEntries, List<String> newLabels) {

        updateData(resetChartZoom, newEntries, newLabels);

        invalidate();

        resetChartZoom = false;
    }

    /**
     * If set to true, enables the highlight when long click on the chart's viewport
     * @param enabled
     */
    public void setHighlightsEnabled(boolean enabled) {
        drawHighlight = enabled;
        setHighlightPerLongClickEnable(drawHighlight);
    }

    public void setAnnotationEnabled(boolean enabled) {
        this.annotationEnabled = enabled;
        if(enabled) {
            currentMarketPrice = new Annotation();
            currentMarketPrice.setIsLabelDrawEnabled(enabled);
            currentMarketPrice.setAnnotationColor(getResources().getColor(R.color.dark_navy));
            currentMarketPrice.setTextColor(Color.WHITE);
            currentMarketPrice.setTextSize(11);
            currentMarketPrice.setLineWidth(1.2f);
            yAxis.addAnnotation(currentMarketPrice);
        }
    }

    public void processMarketPriceUpdate(float marketPrice, long priceTimestamp) {
        if(annotationEnabled && currentMarketPrice != null) {
            currentMarketPrice.updateAnnotationValue(marketPrice);
        }
        processRealTimeCandle(marketPrice, priceTimestamp);
        invalidate();
    }

    public void changeYAxisFormat(String pattern) {
        yAxis.setValueFormatter(new YAxisFormatter(pattern));
        if(annotationEnabled && currentMarketPrice != null) {
            currentMarketPrice.setLabelFormat(new DecimalFormat(pattern,
                    Tools.getSafeDecimalFormatSymbol(getContext())));
        }
    }

    public void changeYAxisFormat(DecimalFormat format) {
        yAxis.setValueFormatter(new YAxisReadyFormatter(format));
        if(annotationEnabled && currentMarketPrice != null) {
            currentMarketPrice.setLabelFormat(format);
        }
    }

    public int getSingleYLabelLength(float value) {
        yAxis.getValueFormatter().getFormattedValue(value, yAxis);
        return String.valueOf(yAxis.getValueFormatter().getFormattedValue(value, yAxis)).length();
    }

    public void updateData(boolean resetChartZoom, List<CandleEntry> entries, List<String> labels) {


        if (resetChartZoom) {
            //this is a workaround for a bug in MPCharts library. As if the chart was dragged with fling gesture
            //the method moveViewToX doesn't work. So imitate tap. Temporary solution
            imitateTap();
        }

        clear();

        CandleDataSet candleDataSet = createCandleDataSet(entries);
        candleDataSet.setHighlightLineWidth(1.2f);
        candleDataSet.setColor(ColorTemplate.COLOR_SKIP);

        CandleData candleData = new CandleData(labels);

        candleData.setHighlightEnabled(drawHighlight);
        candleData.setDrawValues(false);
        candleData.addDataSet(candleDataSet);

        CombinedData data = new CombinedData(labels);
        data.setData(candleData);

        for(BarLineScatterCandleBubbleData current : createIndicatorData(labels)) {
            data.setData(current);
        }

        setData(data);

        //in a CombinedChartRenderer there is a list of renderers that are recreated every time a new data is set.
        //That's why we need to setDrawHighlightDetails every time we set new data.
        for (DataRenderer dataRenderer : ((CombinedChartRenderer) getRenderer()).getSubRenderers()) {
            dataRenderer.setDrawHighlightDetails(drawHighlight);
        }

        //set it always after setting data
        setVisibleXRangeMinimum(1f);

        if(resetChartZoom) {
            RectF rect = getViewPortHandler().getContentRect();
            /*if (calculateNeededScale) { //calculate needed zoom once
                float candleWidth = candleDataSet.getShadowWidth();
                neededScaleFactor = (int) (CANDLE_STICK_WIDTH / candleWidth);
                CustomLog.print(CustomLog.UI, CLASSNAME, "need to calculate initial scale. candleWidth = " + candleWidth + " scaleFactor = " + neededScaleFactor);
                zoom(neededScaleFactor, 1, rect.left, getHeight() / 2);
                calculateNeededScale = false;
            }*/

            if (getScaleX() != neededScaleFactor) {
                zoom(neededScaleFactor / getScaleX(), 1, rect.left, getHeight() / 2);
            }
            moveViewToX(labels.size() - 1 - 0.1f);
            handleChartRedraw = true;
        }

    }

    private void processRealTimeCandle(float marketPrice, long priceTimestamp) {

        if(getCandleData() == null || getCandleData().getXValCount() < 2) {
            return;
        }

        long interval = Long.parseLong(getCandleData().getXVals().get(1)) - Long.parseLong(getCandleData().getXVals().get(0));
        float close = marketPrice;
        if(priceTimestamp - Long.parseLong(getCandleData().getXVals().get(getCandleData().getXValCount()-1)) > interval) {
            Logger.log(Logger.LogMode.DEBUG, LOG_TAG, "NEED TO ADD NEW ENTRY");
            //add new candle
            CandleEntry currentLastEntry = getCandleData().getDataSetByIndex(0).getEntryForXIndex(getCandleData().getXValCount()-1);

            getCandleData().addXValue(String.valueOf(priceTimestamp));
            float open = currentLastEntry.getClose();
            float high = open > close ? open : close;
            float low = open < close ? open : close;

            getCandleData().addEntry(new CandleEntry(getCandleData().getXValCount()-1, high, low, open, close), 0);

        } else {
            Logger.log(Logger.LogMode.DEBUG, LOG_TAG, "NEED TO UPDATE LAST ENTRY");
            //update last candle
            CandleEntry currentLastEntry = getCandleData().getDataSetByIndex(0).getEntryForXIndex(getCandleData().getXValCount()-1);
            if(close > currentLastEntry.getHigh()) {
                currentLastEntry.setHigh(close);
            } else if(close < currentLastEntry.getLow()){
                currentLastEntry.setLow(close);
            }
            currentLastEntry.setClose(close);
            getCandleData().notifyDataChanged();
        }

        notifyDataSetChanged();
    }

    private CandleDataSet createCandleDataSet(List<CandleEntry> entries) {
        CandleDataSet candleDataSet = new CandleDataSet(entries, "");
        candleDataSet.setAxisDependency(YAxis.AxisDependency.RIGHT);
        candleDataSet.setShadowWidth(1f);
        candleDataSet.setDecreasingColor(getResources().getColor(R.color.demo_price_down));
        candleDataSet.setDecreasingPaintStyle(Paint.Style.FILL);
        candleDataSet.setIncreasingColor(getResources().getColor(R.color.demo_price_up));
        candleDataSet.setIncreasingPaintStyle(Paint.Style.FILL);
        candleDataSet.setShadowColor(getResources().getColor(R.color.demo_price_down)); //used for candle and stick when open and close prices are the same
        candleDataSet.setShadowColorSameAsCandle(true);
        candleDataSet.setDrawValues(false);
        candleDataSet.setDrawHighlightIndicators(drawHighlight);
        candleDataSet.setHighlightTextSize(highlightTextSize);
        return candleDataSet;
    }

    private float convertSpToPx(Context context, int valueInSp) {
        float scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
        return valueInSp * scaledDensity;
    }

    private class YAxisFormatter implements YAxisValueFormatter {

        private DecimalFormat format;

        public YAxisFormatter(String pattern) {
            format = new DecimalFormat(pattern,
                Tools.getSafeDecimalFormatSymbol(getContext()));
        }

        @Override
        public String getFormattedValue(float value, YAxis yAxis) {
            return format.format(value);
        }
    }

    private class YAxisReadyFormatter implements YAxisValueFormatter {

        private DecimalFormat format;

        public YAxisReadyFormatter(DecimalFormat format) {
            this.format = format;
        }

        @Override
        public String getFormattedValue(float value, YAxis yAxis) {
            return format.format(value);
        }
    }

}


