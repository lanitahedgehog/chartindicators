package com.mobiletradingpartners.chartindicatorslib.models;

import com.mobiletradingpartners.chartindicatorslib.IndicatorConstants;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Optional Input Parameter Model
 */
public class OptInputParam {

    private String displayName;
    private String hint;
    private String name;
    private Indicator.OptInputParamType type;
    private double defaultValue;

    public OptInputParam (JSONObject optInputParamJson) throws JSONException {
        parseJson(optInputParamJson);
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getHint() {
        return hint;
    }

    public String getName() {
        return name;
    }

    public Indicator.OptInputParamType getType() {
        return type;
    }

    public double getDefaultValue() {
        return defaultValue;
    }

    private void parseJson(JSONObject optInputParamJson) throws JSONException {
        setType(optInputParamJson.getString(IndicatorConstants.OUTPUT_PARAM_TYPE));
        displayName = optInputParamJson.getString(IndicatorConstants.OPTIONAL_INPUT_PARAM_DISPLAY_NAME);
        hint = optInputParamJson.getString(IndicatorConstants.OPTIONAL_INPUT_PARAM_HINT);
        name = optInputParamJson.getString(IndicatorConstants.OPTIONAL_INPUT_PARAM_NAME);
        defaultValue = optInputParamJson.getDouble(IndicatorConstants.OPTIONAL_INPUT_PARAM_DEF_VALUE);
    }

    private void setType(String typeString) {
        switch(typeString) {
            case IndicatorConstants.OIP_TYPE_INTEGER_LIST:
                type = Indicator.OptInputParamType.INTEGER_LIST;
                break;
            case IndicatorConstants.OIP_TYPE_INTEGER_RANGE:
                type = Indicator.OptInputParamType.INTEGER_RANGE;
                break;
            case IndicatorConstants.OIP_TYPE_REAL_LIST:
                type = Indicator.OptInputParamType.REAL_LIST;
                break;
            case IndicatorConstants.OIP_TYPE_REAL_RANGE:
                type = Indicator.OptInputParamType.REAL_RANGE;
                break;
        }
    }

}
