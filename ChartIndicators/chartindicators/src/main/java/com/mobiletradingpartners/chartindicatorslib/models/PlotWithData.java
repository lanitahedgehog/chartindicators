package com.mobiletradingpartners.chartindicatorslib.models;

import com.github.mikephil.charting.data.CandleEntry;

import java.util.ArrayList;
import java.util.List;
import com.mobiletradingpartners.chartindicatorslib.Logger;

/**
 * Represents a single plot with yAxis and yAxisShared filled with values from the init output. Controls whether another output can be added to this "plot".
 */
public class PlotWithData {

    private final String LOG_TAG = "PlotWithData";

    protected final int MAX_OUTPUT_PER_PLOT_COUNT = 6;
    private final int DELTA_FACTOR = 7;
    private final int DISTANCE_FACTOR = 5;

    private List<FunctionOutput> functionOutputs;
    private int yAxis;
    private boolean yAxisShared;


    private boolean mainPlotFactorDefined;
    private float mainPlotMin;
    private float mainPlotMax;
    private float mainPlotAvg;

    public PlotWithData () {
        this.functionOutputs = new ArrayList<>();
        yAxis = Indicator.MAIN_PLOT;
        yAxisShared = true;
        mainPlotFactorDefined = false;
    }

    public PlotWithData(FunctionOutput functionOutputs) {
        this.functionOutputs = new ArrayList<>();
        this.functionOutputs.add(functionOutputs);
        yAxis = functionOutputs.getYAxis();
        yAxisShared = functionOutputs.isYAxisShared();
    }

    public void setMainData(List<CandleEntry> mainData) {
        calcMainDataMinMaxAvg(mainData);
    }

    public void add(FunctionOutput functionOutput) {
        functionOutputs.add(functionOutput);
    }

    public int getyAxis() {
        return yAxis;
    }

    public boolean isyAxisShared() {
        return yAxisShared;
    }

    public List<FunctionOutput> getFunctionOutputs() {
        return functionOutputs;
    }

    public boolean canBeCombinedWith(FunctionOutput functionOutput) {

        boolean canBeCombined = true;

        //if candidate output's yAxis and yAxisShared do not match with current plot's params, then we can not place candidate output on this plot
        if(!yAxisShared || yAxis != functionOutput.getYAxis() || yAxisShared != functionOutput.isYAxisShared() || functionOutputs.size() == MAX_OUTPUT_PER_PLOT_COUNT ) {
            canBeCombined = false;
        }

        float candidateDelta = functionOutput.getMax() - functionOutput.getMin();
        float candidateAvg = functionOutput.getAverage();

        if(mainPlotFactorDefined/* && canBeCombined*/) { //uncomment to handle max per plot output count
            canBeCombined = isCombinable(mainPlotMax - mainPlotMin, mainPlotAvg, candidateDelta, candidateAvg);
        }

        for (int i = 0; i < functionOutputs.size() && canBeCombined; i++) {
            float existingDelta = mainPlotFactorDefined ? (mainPlotMax - mainPlotMin) : (functionOutputs.get(i).getMax() - functionOutputs.get(i).getMin());
            float existingAvg = mainPlotFactorDefined ? mainPlotAvg : functionOutputs.get(i).getAverage();

            canBeCombined = isCombinable(existingDelta, existingAvg, candidateDelta, candidateAvg);
        }
        Logger.log(Logger.LogMode.DEBUG, LOG_TAG, "canBeCombinedWith = " + canBeCombined);

        return  canBeCombined;
    }

    private boolean isCombinable(float existingDelta, float existingAvg, float candidateDelta, float candidateAvg) {
        boolean isCombinable = true;

        //checking scaling-range. As we can not place on the same plot output that has a range from 10 to 70 and another output with range from 10 to 11
        if(candidateDelta / existingDelta > DELTA_FACTOR || existingDelta / candidateDelta > DELTA_FACTOR) {
            isCombinable = false;
        }

        //checking placement. As we can not place on the same plot output with data set in a range from 10 to 20 and another output with range from 100 to 200.
        //factor value in this case gives us ability to control blank space between data sets.
        if(Math.abs(candidateAvg - existingAvg) > (DISTANCE_FACTOR * Math.min(candidateDelta, existingDelta))) {
            isCombinable = false;
        }
        return isCombinable;
    }

    private void calcMainDataMinMaxAvg(List<CandleEntry> mainData) {
        float total = 0;
        if(mainData.size() > 0) {
            mainPlotMin = mainData.get(0).getLow();
            mainPlotMax = mainData.get(0).getHigh();
        }
        for(int i = 0; i < mainData.size(); i++) {
            CandleEntry entry = mainData.get(i);
            if(entry.getLow() < mainPlotMin) {
                mainPlotMin = entry.getLow();
            }
            if(entry.getHigh() > mainPlotMax) {
                mainPlotMax = entry.getHigh();
            }
            total+=entry.getClose();
        }
        mainPlotAvg = total/mainData.size();
        mainPlotFactorDefined = true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Outputs.count = " + functionOutputs.size() + " ");
        for(FunctionOutput fo : functionOutputs) {
            sb.append("yAxis = " + fo.getYAxis() + " yAxisShared = " + fo.isYAxisShared() + " min = " + fo.getMin() + " max = " + fo.getMax() + " av = " + fo.getAverage());
        }
        return sb.toString();
    }
}
