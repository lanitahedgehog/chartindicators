package com.mobiletradingpartners.chartindicatorslib.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.mobiletradingpartners.chartindicatorslib.ChartIndicatorManager;
import com.mobiletradingpartners.chartindicatorslib.R;
import com.mobiletradingpartners.chartindicatorslib.models.FunctionOutput;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * View that encapsulates main chart and all indicators that are added on additional plots. If indicator
 * is supposed to be drawn on the main chart, this view will add it to the main chart. Otherwise
 * it will create additional view for displaying the indicator.
 */
public class ChartIndicatorsView extends LinearLayout {

    private MPChartView mainPlot;
    private ArrayList<IndicatorView> additionalPlots = new ArrayList<>();
    private View progressBar;

    private List<CandleEntry> previousEntries;
    private List<String> previousLabels;

    private ChartIndicatorManager chartIndicatorManager;

    public ChartIndicatorsView(Context context) {
        this(context, null);
    }

    public ChartIndicatorsView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChartIndicatorsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        ((LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.chart_indicator, this, true);
        setOrientation(VERTICAL);
        init();
    }

    private void init() {
        progressBar = findViewById(R.id.chart_progress);

        mainPlot = (MPChartView)findViewById(R.id.main_plot);

        mainPlot.setOnChartGestureListener(new OnChartGestureListener() {
            @Override
            public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

            }

            @Override
            public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

            }

            @Override
            public void onChartLongPressed(MotionEvent me) {

            }

            @Override
            public void onChartDoubleTapped(MotionEvent me) {
                for (IndicatorView v : additionalPlots) {
                    v.getViewPortHandler().refresh(mainPlot.getViewPortHandler().getMatrixTouch(), v, true);
                }
            }

            @Override
            public void onChartSingleTapped(MotionEvent me) {

            }

            @Override
            public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {

            }

            @Override
            public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
                for (IndicatorView v : additionalPlots) {
                    v.getViewPortHandler().refresh(mainPlot.getViewPortHandler().getMatrixTouch(), v, true);
                }
            }

            @Override
            public void onChartTranslate(MotionEvent me, float dX, float dY) {
                for (IndicatorView v : additionalPlots) {
                    v.getViewPortHandler().refresh(mainPlot.getViewPortHandler().getMatrixTouch(), v, true);
                }
            }
        });

    }

    public void setChartIndicatorManager(ChartIndicatorManager indicatorManager) {
        this.chartIndicatorManager = indicatorManager;
        applyIndicators();
        showCharts(false);

        manageSingleXAxis();
    }

    private void manageSingleXAxis() {
        if(additionalPlots.size() == 0) {
            mainPlot.showXAxis(true);
        } else {
            mainPlot.showXAxis(false);
            for(int i = 0; i < additionalPlots.size(); i++) {
                if(i == additionalPlots.size() - 1) {
                    additionalPlots.get(i).showXAxis(true);
                } else {
                    additionalPlots.get(i).showXAxis(false);
                }
            }
        }
    }

    private void setListenerToAddPlot(final int index) {
        additionalPlots.get(index).setOnChartGestureListener(new OnChartGestureListener() {
            @Override
            public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

            }

            @Override
            public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

            }

            @Override
            public void onChartLongPressed(MotionEvent me) {

            }

            @Override
            public void onChartDoubleTapped(MotionEvent me) {
                mainPlot.getViewPortHandler().refresh(additionalPlots.get(index).getViewPortHandler().getMatrixTouch(), mainPlot, true);
                for (int i = 0; i < additionalPlots.size(); i++) {
                    if (i != index) {
                        additionalPlots.get(i).getViewPortHandler().refresh(mainPlot.getViewPortHandler().getMatrixTouch(), additionalPlots.get(i), true);
                    }
                }
            }

            @Override
            public void onChartSingleTapped(MotionEvent me) {

            }

            @Override
            public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {

            }

            @Override
            public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
                mainPlot.getViewPortHandler().refresh(additionalPlots.get(index).getViewPortHandler().getMatrixTouch(), mainPlot, true);
                for (int i = 0; i < additionalPlots.size(); i++) {
                    if (i != index) {
                        additionalPlots.get(i).getViewPortHandler().refresh(mainPlot.getViewPortHandler().getMatrixTouch(), additionalPlots.get(i), true);
                    }
                }
            }

            @Override
            public void onChartTranslate(MotionEvent me, float dX, float dY) {
                mainPlot.getViewPortHandler().refresh(additionalPlots.get(index).getViewPortHandler().getMatrixTouch(), mainPlot, true);
                for (int i = 0; i < additionalPlots.size(); i++) {
                    if (i != index) {
                        additionalPlots.get(i).getViewPortHandler().refresh(mainPlot.getViewPortHandler().getMatrixTouch(), additionalPlots.get(i), true);
                    }
                }
            }
        });
    }

    public void setIntervalLabelModulus(int modulus) {
        mainPlot.setIntervalLabelModulus(modulus);
        for(IndicatorView v : additionalPlots) {
            v.setIntervalLabelModulus(modulus);
        }
    }

    public void setResetChartZoom(boolean resetChartZoom) {
        mainPlot.setResetChartZoom(resetChartZoom);
        for (IndicatorView plot : additionalPlots) {
            plot.setResetChartZoom(resetChartZoom);
        }
    }

    public void setYAxisFormatter(String pattern) {
        mainPlot.changeYAxisFormat(pattern);
    }

    public void setYAxisFormatter(DecimalFormat format) {
        mainPlot.changeYAxisFormat(format);
    }

    public void onMarketPriceUpdate(float marketPrice, long timestamp) {
        mainPlot.processMarketPriceUpdate(marketPrice, timestamp);

        if(previousEntries == null || previousLabels == null) {
            return;
        }

        //if marketPrice's time can not be included in the last entry timeperiod, then
        //a new CandleEntry will be created on the main plot and indicator charts will be drawn
        //one entry behind. So we need to recalculate indicators with a manually added CandleEntry
        long interval = Long.parseLong(previousLabels.get(1)) - Long.parseLong(previousLabels.get(0));

        if(timestamp - Long.parseLong(previousLabels.get(previousLabels.size() - 1)) > interval) {
            updateCandleDataWithNewEntry(marketPrice, timestamp);
            processCandleDataUpdate(previousEntries, previousLabels);
        }
    }

    public void clearChart() {
        mainPlot.clear();
    }

    private void updateCandleDataWithNewEntry(float marketPrice, long timestamp) {
        previousLabels.add(String.valueOf(timestamp));
        CandleEntry lastCandleEntry = previousEntries.get(previousEntries.size()-1);
        float open = lastCandleEntry.getClose();
        float close = marketPrice;
        float high = open > close ? open : close;
        float low = open < close ? open : close;
        previousEntries.add(new CandleEntry(previousLabels.size()-1, high, low, open, close));
    }

    public void processCandleDataUpdate(List<CandleEntry> candleEntries, List<String> candleLables) {

        previousEntries = new ArrayList<>(candleEntries);
        previousLabels = new ArrayList<>(candleLables);

        if(candleEntries != null && candleLables != null && candleEntries.size() != 0 && candleLables.size() != 0) {

            chartIndicatorManager.calculateCurrentIndicators(candleEntries);

            showCharts(true);
            managePlotsAlignment();
            mainPlot.onNewDataReady(candleEntries, candleLables);
            for(IndicatorView plot : additionalPlots) {
                plot.onNewDataReady(candleLables);
            }
        }
    }

    public void applyIndicators() {
        //addingToMainPlot
        for(FunctionOutput current : chartIndicatorManager.getMainPlotIndicators()) {
            mainPlot.addIndicatorOutput(current);
        }

        for(List<FunctionOutput> list : chartIndicatorManager.getOtherPlotIndicators()) {
            IndicatorView iv = createIndicatorView();
            for(FunctionOutput fo : list) {
                iv.addIndicatorOutput(fo);
            }
            additionalPlots.add(iv);
            setListenerToAddPlot(additionalPlots.size() - 1);
        }
    }

    private void managePlotsAlignment() {
        AbstractChartPlot plotToAlignWith = mainPlot;

        int mainPlotYLabelsLength = mainPlot.getSingleYLabelLength(previousEntries.get(0).getClose());

        int maxDigitsLength = mainPlotYLabelsLength;

        for(IndicatorView indicatorView : additionalPlots) {
            int indicatorViewLabelLength = indicatorView.getSingleYLabelLength();
            if(indicatorViewLabelLength > maxDigitsLength) {
                maxDigitsLength = indicatorViewLabelLength;
                plotToAlignWith = indicatorView;
            }
        }

        mainPlot.setTargetViewPort(plotToAlignWith.getViewPortHandler());

        for(IndicatorView iv : additionalPlots) {
            iv.setTargetViewPort(plotToAlignWith.getViewPortHandler());
        }

    }

    private IndicatorView createIndicatorView() {
        IndicatorView indicatorView = new IndicatorView(getContext());
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.weight = 1f;
        indicatorView.setLayoutParams(params);

        addView(indicatorView);
        return indicatorView;
    }

    private void showCharts(boolean showCharts) {
        if(showCharts) {
            progressBar.setVisibility(GONE);
            mainPlot.setVisibility(VISIBLE);
            for(IndicatorView iv : additionalPlots) {
                iv.setVisibility(VISIBLE);
            }
        } else {
            progressBar.setVisibility(VISIBLE);
            mainPlot.setVisibility(GONE);
            for(IndicatorView iv : additionalPlots) {
                iv.setVisibility(GONE);
            }
        }
    }

}
