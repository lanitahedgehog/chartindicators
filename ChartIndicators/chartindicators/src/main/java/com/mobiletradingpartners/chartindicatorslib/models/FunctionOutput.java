package com.mobiletradingpartners.chartindicatorslib.models;

import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.Entry;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Wrapper class that encapsulates output params and calculated dataset for this outputparam
 */
public class FunctionOutput {

    private final String LOG_TAG = "FunctionOutput";

    private DataSet dataSet;
    private OutputParam outputParam;
    private DecimalFormat format;

    private float min = -1;
    private float max = -1;

    public FunctionOutput(OutputParam outputParam) {
        this.outputParam = outputParam;
    }

    public void setDataSet(DataSet dataSet) {
        this.dataSet = dataSet;
        min = calcMin();
        max = calcMax();
    }

    public DataSet getDataSet() {
        return dataSet;
    }

    public void setValueFormatter(DecimalFormat format) {
        this.format = format;
    }

    public DecimalFormat getFormatter() {
        return format;
    }

    public int getYAxis() {
        return outputParam.getyAxis();
    }

    public boolean isYAxisShared() {
        return outputParam.isyAxisShared();
    }

    public int getColor() {
        return outputParam.getColorForIndicator();
    }

    public Indicator.PlotType getGraphType() {
        return outputParam.getGraphType();
    }

    public int getArearangeId() {
        return outputParam.getArearangeId();
    }

    public float getMin() {
        return min;
    }

    public float getMax() {
        return max;
    }

    public float getAverage() {
        return dataSet.getAverage();
    }

    private float calcMin() {
        List<Entry> entries = dataSet.getYVals();
        float min = -1;
        if(entries.size() > 0) {
            min = entries.get(0).getVal();
            for (int i = 1; i < entries.size(); i++) {
                if (entries.get(i).getVal() < min) {
                    min = entries.get(i).getVal();
                }
            }
        }
        return min;
    }

    private float calcMax() {
        List<Entry> entries = dataSet.getYVals();
        float max = -1;
        if(entries.size() > 0) {
            max = entries.get(0).getVal();
            for (int i = 1; i < entries.size(); i++) {
                if (entries.get(i).getVal() > max) {
                    max = entries.get(i).getVal();
                }
            }
        }
        return max;
    }


}
