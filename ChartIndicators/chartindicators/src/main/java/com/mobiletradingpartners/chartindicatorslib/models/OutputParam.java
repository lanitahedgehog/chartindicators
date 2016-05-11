package com.mobiletradingpartners.chartindicatorslib.models;

import com.mobiletradingpartners.chartindicatorslib.IndicatorConstants;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Output parameter model
 */
public class OutputParam {

    private String name;
    private String flag; //nb maybe not need it
    private Indicator.OutputType type;

    private int yAxis;
    private boolean yAxisShared;
    private Indicator.PlotType graphType;

    /**
     * is used to group output params when drawing arearange. If set to -1, LineDataSet is ignored
     */
    private int arearangeId = -1;

    //maybe move it to some object
    private int colorForIndicator;

    public OutputParam (JSONObject outputParamJson, List<PlotParam> plotParams, int colorForIndicator) throws JSONException {
        parseJson(outputParamJson);
        processPlotParams(plotParams);
        this.colorForIndicator = colorForIndicator;
    }

    private void parseJson(JSONObject outputParamJson) throws JSONException {
        setType(outputParamJson.getString(IndicatorConstants.OUTPUT_PARAM_TYPE));
        name = outputParamJson.getString(IndicatorConstants.OUTPUT_PARAM_NAME);
        flag = outputParamJson.getString(IndicatorConstants.OUTPUT_PARAM_FLAG);
    }

    private void setType(String typeString) {
        switch (typeString) {
            case IndicatorConstants.OP_TYPE_REAL:
                type = Indicator.OutputType.REAL;
                break;
            case IndicatorConstants.OP_TYPE_INTEGER:
                type = Indicator.OutputType.INTEGER;
                break;
        }
    }

    private void processPlotParams(List<PlotParam> plotParams) {
        if(plotParams.size() == 1) {
            PlotParam singlePlotParam = plotParams.get(0);
            initPlotParam(singlePlotParam);
        } else {
            boolean plotFound = false;
            for(PlotParam current : plotParams) {
                for(String series : current.getSeries()) {
                    if(name.toLowerCase().equalsIgnoreCase(series)) {
                        initPlotParam(current);
                        plotFound = true;
                        break;
                    }
                }
                if(plotFound) {
                    break;
                }
            }
        }
    }

    private void initPlotParam(PlotParam plotParam) {
        yAxis = plotParam.getyAxis();
        yAxisShared = plotParam.isSharedYAxis();
        graphType = plotParam.getType();
        if(graphType == Indicator.PlotType.AREARANGE) {
            arearangeId = plotParam.getArearangeId();
        }
    }

    public String getName() {
        return name;
    }

    public String getFlag() {
        return flag;
    }

    public int getColorForIndicator() {
        return colorForIndicator;
    }

    public Indicator.OutputType getType() {
        return type;
    }

    public int getyAxis() {
        return yAxis;
    }

    public int getArearangeId() {
        return arearangeId;
    }

    public boolean isyAxisShared() {
        return yAxisShared;
    }

    public Indicator.PlotType getGraphType() {
        return graphType;
    }
}
