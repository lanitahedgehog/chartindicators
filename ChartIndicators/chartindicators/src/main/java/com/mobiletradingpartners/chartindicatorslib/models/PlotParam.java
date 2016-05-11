package com.mobiletradingpartners.chartindicatorslib.models;

import com.mobiletradingpartners.chartindicatorslib.IndicatorConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Plot params model
 */
public class PlotParam {

    private List<String> series = new ArrayList<>();
    private String title;
    private Indicator.PlotType type;
    private int yAxis;
    private boolean sharedYAxis;
    /**
     * is used to group output params when drawing arearange. If set to -1, LineDataSet is ignored
     */
    private int arearangeId = -1;

    public PlotParam (JSONObject plotParamJson) throws JSONException {
        parseJsonObject(plotParamJson);
    }

    public List<String> getSeries() {
        return series;
    }

    public String getTitle() {
        return title;
    }

    public Indicator.PlotType getType() {
        return type;
    }

    public int getyAxis() {
        return yAxis;
    }

    public int getArearangeId() {
        return arearangeId;
    }

    public boolean isSharedYAxis() {
        return sharedYAxis;
    }

    private void parseJsonObject(JSONObject jsonObject) throws JSONException {
        setSeries(jsonObject.getJSONArray(IndicatorConstants.PLOT_SERIES));
        title = jsonObject.getString(IndicatorConstants.PLOT_TITLE);
        setType(jsonObject.getString(IndicatorConstants.PLOT_TYPE));
        yAxis = jsonObject.getInt(IndicatorConstants.PLOT_YAXIS);
        sharedYAxis = jsonObject.getBoolean(IndicatorConstants.PLOT_YAXIS_SHARED);
    }

    private void setSeries(JSONArray seriesArray) throws JSONException {
        for (int i = 0; i < seriesArray.length(); i++) {
            series.add(seriesArray.getString(i));
        }
    }

    private void setType(String typeString) {
        switch(typeString) {
            case IndicatorConstants.P_TYPE_LINE:
                type = Indicator.PlotType.LINE;
                break;
            case IndicatorConstants.P_TYPE_AREARANGE:
                type = Indicator.PlotType.AREARANGE;
                arearangeId = hashCode();
                break;
            case IndicatorConstants.P_TYPE_COLUMN:
                type = Indicator.PlotType.COLUMN;
                break;

        }
    }
}
