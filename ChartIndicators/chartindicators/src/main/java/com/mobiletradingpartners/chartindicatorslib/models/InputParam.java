package com.mobiletradingpartners.chartindicatorslib.models;


import com.mobiletradingpartners.chartindicatorslib.IndicatorConstants;
import com.mobiletradingpartners.chartindicatorslib.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Input Parameter Model
 */
public class InputParam {

    private final String LOG_TAG = "InputParam";

    private Indicator.InputParamType type;
    private Indicator.DataFlag[] flags;

    public InputParam (JSONObject inputParamJson) throws JSONException {
        setType(inputParamJson.getString(IndicatorConstants.INPUT_PARAM_TYPE));
        setFlags(inputParamJson.getJSONArray(IndicatorConstants.INPUT_PARAM_FLAGS));
    }

    public Indicator.InputParamType getType() {
        return type;
    }

    public Indicator.DataFlag[] getFlags() {
        return flags;
    }

    private void setType(String inputTypeString) {
        switch(inputTypeString) {
            case IndicatorConstants.IP_TYPE_REAL:
                type = Indicator.InputParamType.REAL;
                break;
            case IndicatorConstants.IP_TYPE_PRICE:
                type = Indicator.InputParamType.PRICE;
                break;
            case IndicatorConstants.IP_TYPE_INTEGER:
                type = Indicator.InputParamType.INTEGER;
                break;
        }
    }

    private void setFlags(JSONArray inputFlagsArray) throws JSONException {
        flags = new Indicator.DataFlag[inputFlagsArray.length()];
        for (int i = 0; i < inputFlagsArray.length(); i++) {
            flags[i] = stringToFlag(inputFlagsArray.getString(i));
        }
    }

    private Indicator.DataFlag stringToFlag(String flagName) {
        Indicator.DataFlag dataFlag;
        switch (flagName) {
            case IndicatorConstants.IP_FLAG_OPEN:
                dataFlag = Indicator.DataFlag.OPEN;
                break;
            case IndicatorConstants.IP_FLAG_CLOSE:
                dataFlag = Indicator.DataFlag.CLOSE;
                break;
            case IndicatorConstants.IP_FLAG_HIGH:
                dataFlag = Indicator.DataFlag.HIGH;
                break;
            case IndicatorConstants.IP_FLAG_LOW:
                dataFlag = Indicator.DataFlag.LOW;
                break;
            case IndicatorConstants.IP_FLAG_VOLUME:
                dataFlag = Indicator.DataFlag.VOLUME;
                break;
            case IndicatorConstants.IP_FLAG_OPEN_INTEREST:
                dataFlag = Indicator.DataFlag.OPEN_INTEREST;
                break;
            default:
                dataFlag = Indicator.DataFlag.CLOSE;
                break;
        }
        return dataFlag;
    }

    @Override
    public String toString() {
        StringBuilder flagsLog = new StringBuilder();
        for (int i = 0; i < flags.length; i++) {
            flagsLog.append(flags[i] + " ");
        }
        Logger.log(Logger.LogMode.DEBUG, LOG_TAG, "InputParam: type = " + type + ". Flags: " + flagsLog);
        return super.toString();
    }
}
