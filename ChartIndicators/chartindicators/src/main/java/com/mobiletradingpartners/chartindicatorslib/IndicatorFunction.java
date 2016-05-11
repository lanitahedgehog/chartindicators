package com.mobiletradingpartners.chartindicatorslib;

import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;
import com.mobiletradingpartners.chartindicatorslib.models.FunctionOutput;
import com.mobiletradingpartners.chartindicatorslib.models.Indicator;
import com.mobiletradingpartners.chartindicatorslib.models.InputParam;
import com.mobiletradingpartners.chartindicatorslib.models.OptInputParam;
import com.mobiletradingpartners.chartindicatorslib.models.OutputParam;
import com.tictactec.ta.lib.MAType;
import com.tictactec.ta.lib.MInteger;
import com.tictactec.ta.lib.meta.CoreMetaData;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Encapsulates indicator object and indicator calculations
 */

public class IndicatorFunction {

    private final String LOG_TAG = "IndicatorFunction";
    private final String FORMATTER_FOR_INT_VAL = "#0.0";
    private final String DECIMAL_FORMATTER_PREFIX = "#0.";
    private final int DEFAULT_VALUE_IDX = 0;

    private Indicator indicator;

    private List<double[]> outputDouble = new ArrayList<>();
    private List<int[]> outputInt = new ArrayList<>();
    private MInteger outBegIdx;
    private MInteger outNBElement;

    private List<FunctionOutput> functionOutput = new LinkedList<>();

    public IndicatorFunction(Indicator indicator) {
        this.indicator = indicator;
        for(OutputParam current : indicator.getOutputParams()) {
            functionOutput.add(new FunctionOutput(current));
        }
    }

    public List<FunctionOutput> getFunctionOutputs() {
        return functionOutput;
    }

    public void calculateIndicator(List<CandleEntry> dataSet) {

        clearPreviouslyCalculatedData();

        int dataSetSize = dataSet.size();

        //generate data for calling ta-lib function
        CoreMetaData cmd;
        try {
            cmd = CoreMetaData.getInstance(indicator.getName());

            //input params
            generateInputParams(dataSet, cmd);

            //optional input params
            generateOptInputParams(cmd);

            //nb there are flags! OptInputFlags

            //output params
            generateOutputParams(cmd, dataSetSize);

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            Logger.log(Logger.LogMode.ERROR, LOG_TAG, "No such method! Method name = " + indicator.getName());
            return;
        }

        outBegIdx          = new MInteger();
        outNBElement       = new MInteger();

        try {
            cmd.callFunc(0, dataSetSize - 1, outBegIdx, outNBElement);
            logCalculation();
        } catch (IllegalAccessException e) {
            Logger.log(Logger.LogMode.ERROR, LOG_TAG,  "Calculating indicator function gave an IllegalAccessException");
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            Logger.log(Logger.LogMode.ERROR, LOG_TAG, "Calculating indicator function gave an InvocationTargetException");
            e.printStackTrace();
        }

        //after getting results from ta-lib, generating DataSets for MPAndroidCharts lib
        calculateChartData(indicator.getOutputParams().size(), dataSet);
    }

    private void logCalculation() {
        Logger.log(Logger.LogMode.DEBUG, LOG_TAG, "ta-lib calculation result for indicator " + indicator.getName());
        if(outputDouble.size() > 0) {
            for(double[] od : outputDouble) {
                for (double d : od) {
                    Logger.log(Logger.LogMode.DEBUG, LOG_TAG, "ta-lib calculation result double = " + d);
                }
            }
        }

        if(outputInt.size() > 0) {
            for(int[] oi : outputInt) {
                for (int i : oi) {
                    Logger.log(Logger.LogMode.DEBUG, LOG_TAG, "ta-lib calculation result int = " + i);
                }
            }
        }
    }

    private void calculateChartData(int outputCount, List<CandleEntry> dataSet ) {

        ArrayList<Entry>[] entries = new ArrayList[outputCount];
        ArrayList<BarEntry>[] barEntries = new ArrayList[outputCount];

        for (int i = 0; i < outputCount; i++) {
            entries[i] = new ArrayList<>();
            barEntries[i] = new ArrayList<>();
        }

        for (int index = 0, datasetIndex; index < outNBElement.value; index++) {
            datasetIndex = dataSet.get(index + outBegIdx.value).getXIndex();

            for(int j = 0; j < outputCount; j++) {
                Entry entry = null;
                BarEntry barEntry = null;
                switch(indicator.getOutputParams().get(j).getType()) {
                    case REAL:
                        entry = new Entry((float) outputDouble.get(j)[index], datasetIndex);
                        barEntry = new BarEntry((float) outputDouble.get(j)[index], datasetIndex);
                        break;
                    case INTEGER:
                        entry = new Entry((float) outputInt.get(j)[index], datasetIndex);
                        barEntry = new BarEntry((float) outputInt.get(j)[index], datasetIndex);
                        break;
                }
                entries[j].add(entry);
                barEntries[j].add(barEntry);
            }
        }

        for (int i = 0; i < functionOutput.size(); i++) {
            functionOutput.get(i).setValueFormatter(indicator.getOutputParams().get(i).getType() == Indicator.OutputType.REAL ? generateDoubleValueFormatter(outputDouble.get(i)) :
                    new DecimalFormat(FORMATTER_FOR_INT_VAL));
            DataSet ds = null;
            switch(functionOutput.get(i).getGraphType()) {
                case LINE:
                case AREARANGE:
                    ds = createLineDataSet(functionOutput.get(i).getColor(), indicator.getName(), entries[i], functionOutput.get(i).getArearangeId());
                    break;
                case COLUMN:
                    ds = createBarDataSet(functionOutput.get(i).getColor(), indicator.getName(), barEntries[i]);
                    break;
            }
            functionOutput.get(i).setDataSet(ds);
        }
    }

    private DecimalFormat generateDoubleValueFormatter(double[] output) {
        int minDigitsAfterComaCount = 1;
        int digitAfterComaCount = 0;

        double testValue = output[output.length > 15 ? 15 : 0];
        if(testValue != 0) {
            while(Math.abs(testValue) < 1) {
                testValue*=10;
                digitAfterComaCount++;
            }
        }

        if(digitAfterComaCount == 0) {
            digitAfterComaCount = minDigitsAfterComaCount;
        }

        StringBuilder format = new StringBuilder(DECIMAL_FORMATTER_PREFIX);
        for (int i = 0; i < digitAfterComaCount; i++) {
            format.append("0");
        }
        return new DecimalFormat(format.toString());
    }


    private LineDataSet createLineDataSet(int color, String name, List<Entry> entries, int arearangeId) {
        LineDataSet lineDataSet = new LineDataSet(entries, name);
        lineDataSet.setColor(color);
        lineDataSet.setLineWidth(1f);
        lineDataSet.setDrawCircles(false);
        lineDataSet.setDrawValues(false);
        lineDataSet.setAxisDependency(YAxis.AxisDependency.RIGHT);
        lineDataSet.setArearangeId(arearangeId);
        return lineDataSet;
    }

    private BarDataSet createBarDataSet(int color, String name, List<BarEntry> barEntries) {
        BarDataSet barDataSet = new BarDataSet(barEntries, name);
        barDataSet.setColor(color);
        barDataSet.setDrawValues(false);
        barDataSet.setAxisDependency(YAxis.AxisDependency.RIGHT);
        return barDataSet;
    }

    private void generateInputParams(List<CandleEntry> dataset, CoreMetaData cmd) {
        int dataSetSize = dataset.size();

        List<InputParam> inputParams = indicator.getInputParams();

        InputParam current;

        for (int paramIndex = 0; paramIndex < inputParams.size(); paramIndex++) {

            current = inputParams.get(paramIndex);

            switch(current.getType()) {
                case REAL:
                    double inReal[] = new double[dataSetSize];
                    for (int i = 0; i < inReal.length; i++) {
                        switch(current.getFlags()[0]) {
                            case CLOSE:
                                inReal[i] = dataset.get(i).getClose();
                                break;
                            case OPEN:
                                inReal[i] = dataset.get(i).getOpen();
                                break;
                            case LOW:
                                inReal[i] = dataset.get(i).getLow();
                                break;
                            case HIGH:
                                inReal[i] = dataset.get(i).getHigh();
                                break;
                        }
                    }
                    cmd.setInputParamReal(paramIndex, inReal);
                    break;
                case INTEGER:
                    // do not know what to do with it
                    break;
                case PRICE:

                    double[] open = new double[dataSetSize];
                    double[] close = new double[dataSetSize];
                    double[] high = new double[dataSetSize];
                    double[] low = new double[dataSetSize];
                    double[] volume = new double[dataSetSize]; // nb maybe create a static array of 0.0 values
                    double[] openInterest = new double[dataSetSize];

                    StringBuilder flagsForLog = new StringBuilder();
                    for(int i = 0; i < current.getFlags().length; i++) {
                        switch(current.getFlags()[i]) {
                            case CLOSE:
                                generateDataForType(close, Indicator.DataFlag.CLOSE, dataset);
                                flagsForLog.append("TA_IN_PRICE_CLOSE ");
                                break;
                            case OPEN:
                                generateDataForType(open, Indicator.DataFlag.OPEN, dataset);
                                flagsForLog.append("TA_IN_PRICE_OPEN ");
                                break;
                            case LOW:
                                generateDataForType(low, Indicator.DataFlag.LOW, dataset);
                                flagsForLog.append("TA_IN_PRICE_LOW ");
                                break;
                            case HIGH:
                                generateDataForType(high, Indicator.DataFlag.HIGH, dataset);
                                flagsForLog.append("TA_IN_PRICE_HIGH ");
                                break;
                            case VOLUME:
                                generateDataForType(volume, Indicator.DataFlag.VOLUME, dataset);
                                flagsForLog.append("TA_IN_PRICE_VOLUME ");
                                break;
                            case OPEN_INTEREST:
                                generateDataForType(openInterest, Indicator.DataFlag.OPEN_INTEREST, dataset);
                                flagsForLog.append("TA_IN_PRICE_OPENINTEREST ");
                                break;
                        }
                    }
                    Logger.log(Logger.LogMode.DEBUG, LOG_TAG, "generate Price. Flags are: " + flagsForLog.toString());

                    cmd.setInputParamPrice(paramIndex, open, high, low, close, volume, openInterest);
                    break;
            }
        }

    }

    private void generateOptInputParams(CoreMetaData cmd) {

        List<OptInputParam> optInputParams = indicator.getOptInputParams();
        Logger.log(Logger.LogMode.DEBUG, LOG_TAG, "optInputParams. size = " + optInputParams.size());

        if(optInputParams.size() == 0) {
            Logger.log(Logger.LogMode.DEBUG, LOG_TAG, "NO OPT input params");
            Field field;
            try {
                field = cmd.getClass().getDeclaredField("callOptInputParams");
                field.setAccessible(true);
                field.set(cmd, new Object[0]);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        OptInputParam current;
        for (int paramIndex = 0; paramIndex < optInputParams.size(); paramIndex++) {
            current = optInputParams.get(paramIndex);

            Logger.log(Logger.LogMode.DEBUG, LOG_TAG, "INPUT_PARAM # " + paramIndex + " Type = " + current.getType() + " def value = " + current.getDefaultValue() + " param name = " +
                    current.getDefaultValue());
            switch(current.getType()) {
                case INTEGER_LIST:
                    String maTypeStr = MAType.values()[((int) current.getDefaultValue())].toString();
                    cmd.setOptInputParamInteger(paramIndex, maTypeStr);
                    break;
                case INTEGER_RANGE:
                    cmd.setOptInputParamInteger(paramIndex, (int)current.getDefaultValue());
                    break;
                case REAL_LIST:
                    //nb do not know what to set here
                    break;
                case REAL_RANGE:
                    cmd.setOptInputParamReal(paramIndex, current.getDefaultValue());
                    break;
            }
        }
    }

    private void generateOutputParams(CoreMetaData cmd, int dataSetSize) {
        List<OutputParam> outputParams = indicator.getOutputParams();

        OutputParam current;
        for (int paramIndex = 0; paramIndex < outputParams.size(); paramIndex++) {
            current = outputParams.get(paramIndex);

            switch(current.getType()) {
                case REAL:
                    outputDouble.add(new double[dataSetSize]);
                    cmd.setOutputParamReal(paramIndex, outputDouble.get(paramIndex));
                    break;
                case INTEGER:
                    outputInt.add(new int[dataSetSize]);
                    cmd.setOutputParamInteger(paramIndex, outputInt.get(paramIndex));
                    break;
            }
        }
    }

    private double[] generateDataForType(double[] container, Indicator.DataFlag dataFlag, List<CandleEntry> dataset) {
        CandleEntry dataSetEntry;
        double dataEntry = 0;
        for (int i = 0; i < container.length; i++) {
            dataSetEntry = dataset.get(i);
            switch (dataFlag) {
                case OPEN:
                    dataEntry = dataSetEntry.getOpen();
                    break;
                case CLOSE:
                    dataEntry = dataSetEntry.getClose();
                    break;
                case HIGH:
                    dataEntry = dataSetEntry.getHigh();
                    break;
                case LOW:
                    dataEntry = dataSetEntry.getLow();
                    break;
            }
            container[i] = dataEntry;
        }
        return container;
    }

    private void clearPreviouslyCalculatedData() {
        outputDouble.clear();
        outputInt.clear();
    }
}
