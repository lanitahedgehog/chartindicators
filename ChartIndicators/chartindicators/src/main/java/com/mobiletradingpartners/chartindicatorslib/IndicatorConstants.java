package com.mobiletradingpartners.chartindicatorslib;


/**
 * Constants to work with JSON representing indicators
 */
public class IndicatorConstants {

    public static final String INDICATORS = "ta";
    public static final String INDICATOR_BY_GROUP = "index";
    public static final String INDICATOR_GROUP = "group";

    public static final String GROUP = "_g";

    // constants to work with INPUT
    public static final String INPUT_PARAMS = "_inputs";

    public static final String INPUT_PARAM_TYPE = "type";
    public static final String IP_TYPE_REAL = "real";
    public static final String IP_TYPE_INTEGER = "integer";
    public static final String IP_TYPE_PRICE = "price";

    public static final String INPUT_PARAM_FLAGS = "fields"; // TODO: 2/24/16 ask Paul to rename it to flag
    public static final String IP_FLAG_OPEN = "open";
    public static final String IP_FLAG_CLOSE = "close";
    public static final String IP_FLAG_HIGH = "high";
    public static final String IP_FLAG_LOW = "low";
    public static final String IP_FLAG_VOLUME = "volume";
    public static final String IP_FLAG_OPEN_INTEREST = "openInterest";

    // constants to work with OPTIONAL_INPUT
    public static final String OPTIONAL_INPUT_PARAMS = "optInputs";

    public static final String OPTIONAL_INPUT_PARAM_TYPE = "type";
    public static final String OIP_TYPE_INTEGER_LIST = "integer_list"; //nb always MAType
    public static final String OIP_TYPE_INTEGER_RANGE = "integer_range";
    public static final String OIP_TYPE_REAL_LIST = "real_list";
    public static final String OIP_TYPE_REAL_RANGE = "real_range";

    public static final String OPTIONAL_INPUT_PARAM_DEF_VALUE = "defaultValue";
    public static final String OPTIONAL_INPUT_PARAM_DISPLAY_NAME = "displayName";
    public static final String OPTIONAL_INPUT_PARAM_NAME = "name";  //nb maybe no need for this param
    public static final String OPTIONAL_INPUT_PARAM_HINT = "hint";

    //constants to work with OUTPUT
    public static final String OUTPUT_PARAMS = "outputs";

    public static final String OUTPUT_PARAM_TYPE = "type";
    public static final String OP_TYPE_INTEGER = "integer";
    public static final String OP_TYPE_REAL = "real";

    public static final String OUTPUT_PARAM_FLAG = "flags";
    public static final String OUTPUT_PARAM_NAME = "name";

    // constants to work with Plot
    public static final String PLOT_PARAMS = "_plots";
    public static final String PLOT_SERIES = "series";
    public static final String PLOT_TITLE = "title";

    public static final String PLOT_TYPE = "type";
    public static final String P_TYPE_LINE = "line";
    public static final String P_TYPE_AREARANGE = "arearange";
    public static final String P_TYPE_COLUMN = "column";
    //nb maybe some more plot types

    public static final String PLOT_YAXIS = "yAxis";
    public static final String PLOT_YAXIS_SHARED = "yAxisShared";



}






