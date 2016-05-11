package com.mobiletradingpartners.chartindicatorslib.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarLineScatterCandleBubbleData;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.listener.OnDrawListener;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.mobiletradingpartners.chartindicatorslib.R;
import com.mobiletradingpartners.chartindicatorslib.models.FunctionOutput;
import com.mobiletradingpartners.chartindicatorslib.models.Indicator;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Class-parent for all chart views.
 */
public class AbstractChartPlot extends CombinedChart {

    protected YAxis yAxis;
    protected XAxis xAxis;

    //fields that are used to handle needed scale when changing instrument/interval despite user's zooming in/out
    protected float neededScaleFactor = 2;
    //field that is used as a temporary solution to handle moving the charts to the latest data when changing instrument/interval
    protected boolean handleChartRedraw = false;

    protected boolean resetChartZoom = false;

    //needed for rounding xAxisValues
    protected int intervalLabelModulus;

    /**
     * this is a reference to ViewPortHandler of View that will be used as a aligner by the right side.
     */
    protected ViewPortHandler targetViewPort;

    protected List<FunctionOutput> indicatorsOutput = new LinkedList<>();

    public AbstractChartPlot(Context context) {
        this(context, null);
    }

    public AbstractChartPlot(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AbstractChartPlot(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        generalInit();
    }

    private void generalInit() {
        Resources res = getResources();
        setDrawBorders(false);
        setDescription(null);
        setPinchZoom(true);
        setVerticalScrollBarEnabled(false);
        setAutoScaleMinMaxEnabled(true);

        xAxis = getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(res.getColor(R.color.charts_labels));
        xAxis.setDrawAxisLine(false);
        xAxis.setGridColor(res.getColor(R.color.charts_stripe));
        xAxis.setAvoidFirstLastClipping(true);
        //set this flag to true to enable XAxisRenderer use different DateFormats depending on chart scaling
        xAxis.setAxisShowDates(true);
        xAxis.setDrawEvenGridSpaceInDiffColor(true);
        xAxis.setEvenGridSpaceColor(getResources().getColor(R.color.charts_stripe));
        xAxis.setFixLabelsListeners(new XAxis.fixLabelsListeners() {
            @Override
            public int fixMin(int min) {
                int minIndex = min;
                try {
                    while (minIndex >= 0 && Long.parseLong(xAxis.getValues().get(minIndex)) % (15 * 60000) != 0)
                        minIndex--;
                    if (minIndex == 0) {
                        minIndex = min;
                        while (minIndex >= 0 && Long.parseLong(xAxis.getValues().get(minIndex)) % (5 * 60000) != 0)
                            minIndex--;
                    }
                    if (minIndex == 0) {
                        minIndex = min;
                        while (minIndex >= 0 && Long.parseLong(xAxis.getValues().get(minIndex)) % (5 * 60000) != 0)
                            minIndex++;
                    }
                } catch (IndexOutOfBoundsException e) {
                }
                return minIndex;
            }

            @Override
            public int fixModulus(int modulus) {
                return Math.max(1, Math.round(((float) modulus) / intervalLabelModulus) * intervalLabelModulus);
            }
        });

        getLegend().setEnabled(false);
/*        getLegend().setEnabled(true);
        getLegend().setPosition(Legend.LegendPosition.LEFT_OF_CHART);
        getLegend().setYOffset(2);
        getLegend().setXOffset(3);*/

        YAxis leftYAxis = getAxisLeft();
        leftYAxis.setStartAtZero(false);
        leftYAxis.setEnabled(false);

        yAxis = getAxisRight();
        yAxis.setStartAtZero(false);
        yAxis.setDrawAxisLine(false);
        yAxis.setGridColor(res.getColor(R.color.charts_grid));
        yAxis.setGridLineWidth(0); //setting the line width to 0 in order to draw line in a hairline mode when the width of line is let to 1px
//        yAxis.setTypeface(FontManager.getInstance(getContext()).getTypeface(Typeface.ITALIC));
        yAxis.setTextColor(res.getColor(R.color.charts_labels));
        yAxis.setXOffset(2);

        setScaleYEnabled(false);
        setDoubleTapFitScreenEnabled(true);

        setOnDrawListener(new OnDrawListener() {
            @Override
            public void onEntryAdded(Entry entry) {

            }

            @Override
            public void onEntryMoved(Entry entry) {

            }

            @Override
            public void onDrawFinished(DataSet<?> dataSet) {
                if (handleChartRedraw) {

                    //align viewport with the target one
                    if(targetViewPort != null) {
                        getViewPortHandler().setChartDimens(targetViewPort.getChartWidth() - (targetViewPort.offsetRight() - getViewPortHandler().offsetRight()), getViewPortHandler().getChartHeight());
                    }

                    //if instrument/interval was changed we need to imitateTap and invalidate the chart. If we don't do this,
                    //the grid lines may not be rendered correctly
                    imitateTap();
                    postInvalidate();
                    handleChartRedraw = false;
                }
            }
        });
    }

    public void showXAxis(boolean enabled) {
        if(enabled) {
            xAxis.setTextColor(getResources().getColor(R.color.charts_labels));
        } else {
            xAxis.setTextColor(getResources().getColor(android.R.color.transparent));
        }
    }

    public void addIndicatorOutput(FunctionOutput functionOutput) {
        indicatorsOutput.add(functionOutput);
    }

    /**
     *
     * @param targetViewPort Pass here a reference to ViewPortHandler from which This view will take the right border coordinates.
     *                       Pass ViewPortHandler by which This view will be aligned.
     */
    public void setTargetViewPort(ViewPortHandler targetViewPort) {
        this.targetViewPort = targetViewPort;
    }

    public void setResetChartZoom(boolean resetChartZoom) {
        this.resetChartZoom = resetChartZoom;
    }

    public void setIntervalLabelModulus(int intervalLabelModulus) {
        this.intervalLabelModulus = intervalLabelModulus;
    }

    protected List<BarLineScatterCandleBubbleData> createIndicatorData(List<String> labels) {

        List<BarLineScatterCandleBubbleData> data = new ArrayList<>();
        //generate line data
        BarLineScatterCandleBubbleData lineData = new LineData(labels);
        for(FunctionOutput currentOutput : indicatorsOutput) {
            if((currentOutput.getGraphType() == Indicator.PlotType.LINE) || (currentOutput.getGraphType() == Indicator.PlotType.AREARANGE)) {
                lineData.addDataSet(currentOutput.getDataSet());
            }
        }
        if(lineData.getDataSetCount() > 0) {
            lineData.setHighlightEnabled(false);
            data.add(lineData);
        }

        //generate column data
        BarLineScatterCandleBubbleData columnData = new BarData(labels);
        for(FunctionOutput currentOutput : indicatorsOutput) {
            if(currentOutput.getGraphType() == Indicator.PlotType.COLUMN) {
                columnData.addDataSet(currentOutput.getDataSet());
            }
        }
        if(columnData.getDataSetCount() > 0) {
            columnData.setHighlightEnabled(false);
            data.add(columnData);
        }
        return data;
    }

    protected void imitateTap() {
        long downTime = System.currentTimeMillis();
        long eventTime = downTime + 100;
        float x = getViewPortHandler().contentLeft() + (getViewPortHandler().contentRight() - getViewPortHandler().contentLeft())/2;
        float y = getViewPortHandler().contentTop() + (getViewPortHandler().contentBottom() - getViewPortHandler().contentTop())/2;
        int metaState = 0;
        MotionEvent motionEvent = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, x, y, metaState);

        dispatchTouchEvent(motionEvent);
    }



}
