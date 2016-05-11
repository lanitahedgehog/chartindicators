package com.mobiletradingpartners.chartindicatorslib.models;



import com.mobiletradingpartners.chartindicatorslib.IndicatorConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Indicator {

    private String name;
    private IndicatorGroup group;
    private List<InputParam> inputParams = new ArrayList<>();
    private List<OptInputParam> optInputParams = new ArrayList<>();
    private List<OutputParam> outputParams = new ArrayList<>();
    private List<PlotParam> plotParam = new ArrayList<>();

    public Indicator(String indicatorName, int indicatorColor, JSONObject indicatorJsonObject) throws JSONException {
        this.name = indicatorName;

        parseJsonObject(indicatorJsonObject, indicatorColor);
    }

    public String getName() {
        return name;
    }

    public IndicatorGroup getGroup() {
        return group;
    }

    public List<InputParam> getInputParams() {
        return inputParams;
    }

    public List<OptInputParam> getOptInputParams() {
        return optInputParams;
    }

    public List<OutputParam> getOutputParams() {
        return outputParams;
    }

    public List<PlotParam> getPlotParam() {
        return plotParam;
    }

    private void parseJsonObject(JSONObject indicatorJson, int indicatorColor) throws JSONException {
        group = IndicatorGroup.values()[indicatorJson.getInt(IndicatorConstants.GROUP)];

        //input params
        JSONObject inputParamsJsonObj = indicatorJson.getJSONObject(IndicatorConstants.INPUT_PARAMS);

        Iterator<String> inputParamsKeys = inputParamsJsonObj.keys();
        while (inputParamsKeys.hasNext()) {
            inputParams.add(new InputParam(inputParamsJsonObj.getJSONObject(inputParamsKeys.next())));
        }

        //nb there are flags! OptInputFlags

        //optional params
        JSONArray optInputParamsArray = indicatorJson.getJSONArray(IndicatorConstants.OPTIONAL_INPUT_PARAMS);
        for (int i = 0; i < optInputParamsArray.length(); i++) {
            optInputParams.add(new OptInputParam(optInputParamsArray.getJSONObject(i)));
        }

        //plot params
        JSONObject plotsJsonObject = indicatorJson.getJSONObject(IndicatorConstants.PLOT_PARAMS);
        Iterator<String> plotKeys = plotsJsonObject.keys();
        while(plotKeys.hasNext()) {
            plotParam.add(new PlotParam(plotsJsonObject.getJSONObject(plotKeys.next())));
        }

        //output params
        JSONArray outputParamsArray = indicatorJson.getJSONArray(IndicatorConstants.OUTPUT_PARAMS);
        for (int i = 0; i < outputParamsArray.length(); i++) {
            outputParams.add(new OutputParam(outputParamsArray.getJSONObject(i), plotParam, indicatorColor));
        }
    }

    public enum IndicatorGroup {
        MATH_OPERATORS,
        MATH_TRANSFORM,
        OVERLAP_STUDIES,
        VOLATILITY_INDICATORS,
        MOMENTUM_INDICATORS,
        CYCLE_INDICATORS,
        VOLUME_INDICATORS,
        PATTERN_RECOGNITION,
        STATISTIC_FUNCTIONS,
        PRICE_TRANSFORM
    }

    public enum InputParamType {
        REAL,
        PRICE,
        INTEGER
    }

    public enum OptInputParamType {
        INTEGER_LIST,
        INTEGER_RANGE,
        REAL_LIST,
        REAL_RANGE
    }

    public enum OutputType {
        REAL,
        INTEGER
    }

    public enum DataFlag {
        OPEN,
        CLOSE,
        HIGH,
        LOW,
        VOLUME,
        OPEN_INTEREST
    }

    public enum PlotType {
        LINE,
        AREARANGE,
        COLUMN,
        INDEFINED // nb for indicators that are mostly like line but have picks. maybe will be represented as dots
    }

    //plot yAxis values
    public static final int MAIN_PLOT = 0;
    public static final int SEPARATE_PLOT = 1;
    public static final int COLUMN_PLOT = 2;

}
