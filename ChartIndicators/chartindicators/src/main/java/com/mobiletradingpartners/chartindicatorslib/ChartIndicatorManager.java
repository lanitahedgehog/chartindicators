package com.mobiletradingpartners.chartindicatorslib;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

import com.github.mikephil.charting.data.CandleEntry;
import com.mobiletradingpartners.chartindicatorslib.models.FunctionOutput;
import com.mobiletradingpartners.chartindicatorslib.models.Indicator;
import com.mobiletradingpartners.chartindicatorslib.models.PlotWithData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Class that can be used to manage charts and indicators. Pass LinearLayout that will serve a container
 * for main plot with candle-stick and additional plots with indicators.
 */
public class ChartIndicatorManager {

    private static final String LOG_TAG = "ChartIndicatorManager";
    private static final String LOAD_INDICATORS_URL = "https://admin.smt-data.com/chartsv5_dev/indicators/explain/";

    private final String SHARED_PREF_FILE_WITH_INDICATORS = "indicators";
    private final String INDICATORS_STRING_KEY = "indic_str_key";

    protected final int MAX_SEPARATE_PLOT_INDIC_COUNT = 3;
    private final int MAIN_PLOT_INDEX = 0;
    private static final int MAX_PLOTS_COUNT = 4;

    public static int[] datasetColors = {Color.rgb(2, 191, 208), Color.rgb(252, 73, 148), Color.rgb(51, 114, 44),
            Color.rgb(239, 191, 0), Color.rgb(223, 70, 70), Color.rgb(221, 98, 7), Color.rgb(70, 88, 89), Color.rgb(185, 130, 215)};

    private Context context;

    protected Map<String, Indicator> indicators = new HashMap<>();
    protected Map<String, IndicatorFunction> currentlyAddedIndicators = new LinkedHashMap<>();
    private Map<String, List<String>> indicatorNamesByGroup = new LinkedHashMap<>();

    private List<CandleEntry> previousDataSet;

    private IndicatorParser indicatorParser;
    private List<PlotWithData> plotData = new ArrayList<>();

    private int downloadAttempt = 0;
    private final int MAX_DOWNLOAD_ATTEMPT = 3;
    private LoadIndicatorJsonListener indicatorJsonLoadListener = new LoadIndicatorJsonListener();


    public ChartIndicatorManager(final Context context) {
        this.context = context;
        indicatorParser = new IndicatorParser();
        loadJsonString(indicatorJsonLoadListener);
        addDefaultMainPlot();
    }

    /**
     * Generates string with names of all selected indicators. Format NAME, NAME, NAME
     * @return
     */
    public String getCurrentlySelectedIndicatorsString() {
        StringBuilder indicatorStringBuilder = new StringBuilder();
        List<String> curIndic = new LinkedList<>(currentlyAddedIndicators.keySet());
        for (int i = 0; i < curIndic.size(); i++) {
            indicatorStringBuilder.append(curIndic.get(i) + (i < curIndic.size() - 1 ? ", " : ""));
        }
        return indicatorStringBuilder.toString();
    }

    public Map<String, List<String>> getIndicatorNamesByGroup() {
        return indicatorNamesByGroup;
    }

    public boolean addCurrentIndicator(String indicatorName) {

        IndicatorFunction potentialIndicatorFunction = new IndicatorFunction(indicators.get(indicatorName));
        if(previousDataSet != null) {
            potentialIndicatorFunction.calculateIndicator(previousDataSet);
        }

        boolean success = canAddIndicator(potentialIndicatorFunction);

        if(success) {
            currentlyAddedIndicators.put(indicatorName, potentialIndicatorFunction);
        }
        generateSmartData();

        //maybe after this call container.applyIndicators()

        Logger.log(Logger.LogMode.INFO, LOG_TAG, "was successfully added = " + success);
        printPlotData();

        return success;

    }

    public List<FunctionOutput> getMainPlotIndicators() {
        return plotData.get(MAIN_PLOT_INDEX).getFunctionOutputs();
    }

    public List<List<FunctionOutput>> getOtherPlotIndicators() {
        List<List<FunctionOutput>> result = new ArrayList<>();
        for (int i = 1; i < plotData.size(); i++) {
            List<FunctionOutput> outputs = new ArrayList<>();
            outputs.addAll(plotData.get(i).getFunctionOutputs());
            result.add(outputs);
        }
        return result;
    }

    public List<String> getCurrentlySelectedGroups() {
        List<String> selectedGroups = new LinkedList<>();

        Set<String> currentlySelectedIndicatorNames = currentlyAddedIndicators.keySet();

        for(String indicName : currentlySelectedIndicatorNames) {
            Set<String> groupNames = indicatorNamesByGroup.keySet();
            for (String groupName : groupNames) {
                List<String> indicatorNames = indicatorNamesByGroup.get(groupName);
                if (indicatorNames.contains(indicName)) {
                    selectedGroups.add(groupName);
                }
            }
        }
        return selectedGroups;
    }

    private void generateSmartData() {
        plotData.clear();
        addDefaultMainPlot();
        for(String key : currentlyAddedIndicators.keySet()) {
            IndicatorFunction indicatorFunction = currentlyAddedIndicators.get(key);
            for(FunctionOutput fo :indicatorFunction.getFunctionOutputs()) {
                addOutputToSuitablePlot(fo);
            }
        }
    }

    private void addDefaultMainPlot() {
        PlotWithData mainPlot = new PlotWithData();
        if(previousDataSet != null) {
            mainPlot.setMainData(previousDataSet);
        }
        plotData.add(mainPlot);
    }

    private void addOutputToSuitablePlot(FunctionOutput functionOutput) {
        for(PlotWithData ilw : plotData) {
            if(ilw.canBeCombinedWith(functionOutput)) {
                ilw.add(functionOutput);
                return;
            }
        }
        plotData.add(new PlotWithData(functionOutput));
    }

    private boolean canAddIndicator(IndicatorFunction indicatorFunction) {

        boolean canBeAdded = true;
        for(int j = 0; j < indicatorFunction.getFunctionOutputs().size() && canBeAdded; j++) {
            FunctionOutput fo = indicatorFunction.getFunctionOutputs().get(j);
            if(!fo.isYAxisShared()) {
                if(plotData.size() < MAX_PLOTS_COUNT) { //nb maybe refactor this
                    plotData.add(new PlotWithData(fo));
                } else {
                    canBeAdded = false;
                }
            } else {
                for (int i = 0; i < plotData.size(); i++) {
                    PlotWithData currentPlot = plotData.get(i);
                    if (currentPlot.canBeCombinedWith(fo)) {
                        currentPlot.add(fo);
                        break;
                    }
                    if (i == plotData.size() - 1) {
                        if(plotData.size() < MAX_PLOTS_COUNT) {
                            plotData.add(new PlotWithData(fo));
                        } else {
                            canBeAdded = false;
                        }
                    }
                }
            }
        }
        return canBeAdded;
    }

    private void printPlotData() {
        for(PlotWithData ilw : plotData) {
            Logger.log(Logger.LogMode.DEBUG, LOG_TAG, ilw.toString());
        }
    }

    public int getCurrentlyAddedIndicatorsCount() {
        return currentlyAddedIndicators.size();
    }


    public boolean deleteCurrentIndicator(String indicatorName) {

        boolean success = true;
        if(currentlyAddedIndicators.size() > 0) {
            currentlyAddedIndicators.remove(indicatorName);
            generateSmartData();
        } else {
            success = false;
            Logger.log(Logger.LogMode.INFO, LOG_TAG, "There are no currently added indicators");
        }
        return success;
    }

    public boolean deleteAllCurrentIndicators() {

        boolean success = true;
        if(currentlyAddedIndicators.size() > 0) {
            currentlyAddedIndicators.clear();
            plotData.clear();
            addDefaultMainPlot();
        } else {
            success = false;
            Logger.log(Logger.LogMode.INFO, LOG_TAG, "There are no currently added indicators");
        }
        return success;

    }

    private void loadJsonString(IndicatorsLoader.OnDataLoadListener listener) {
        IndicatorsLoader indicatorsLoader = new IndicatorsLoader(); //nb maybe use some singleton
        indicatorsLoader.load(LOAD_INDICATORS_URL, listener);
    }

    public boolean isIndicatorCurrentlySelected(String indicatorName) {
        return currentlyAddedIndicators.get(indicatorName) != null;
    }

    public void calculateCurrentIndicators(List<CandleEntry> dataSet) {
        Logger.log(Logger.LogMode.DEBUG, LOG_TAG, "calculateCurrentIndicators");
        Set<String> indicatorNames = currentlyAddedIndicators.keySet();
        for(String currentIndicatorName : indicatorNames) {
            currentlyAddedIndicators.get(currentIndicatorName).calculateIndicator(dataSet);
        }

        previousDataSet = dataSet;
        plotData.get(MAIN_PLOT_INDEX).setMainData(dataSet);  //min max will be calculated every time we get new data
    }


    private class LoadIndicatorJsonListener implements IndicatorsLoader.OnDataLoadListener {
        @Override
        public IndicatorsLoader.ServerResponse onLoadBackground(String data) {
            if (indicatorParser.parse(data)) {
                saveIndicatorsStringToPreferences(context);
                Logger.log(Logger.LogMode.DEBUG, LOG_TAG, "parsing json from server was succeeded = true");
            } else {
                boolean parseSavedJsonSuccess = indicatorParser.parse(getIndicatorsStringFromPreferences(context));
                Logger.log(Logger.LogMode.DEBUG, LOG_TAG, "parsing saved json succeeded = " + parseSavedJsonSuccess);
            }
            return null;
        }

        @Override
        public void onLoad(IndicatorsLoader.ServerResponse serverResponse) {
            Logger.log(Logger.LogMode.DEBUG, LOG_TAG, "LoadingIndicatorsJson. onLoad");
        }

        @Override
        public void onError(IndicatorsLoader.ResponseCode errorCode) {
            Logger.log(Logger.LogMode.ERROR, LOG_TAG, "LoadingIndicatorsJson. onError. error.code = " + errorCode);
            if(downloadAttempt <= MAX_DOWNLOAD_ATTEMPT) {
                downloadAttempt++;
                loadJsonString(indicatorJsonLoadListener);
            } else {
                indicatorParser.parse(getIndicatorsStringFromPreferences(context));
            }
        }
    }

    private void saveIndicatorsStringToPreferences(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_FILE_WITH_INDICATORS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(INDICATORS_STRING_KEY, indicatorParser.getJsonString());
        editor.commit();
    }

    private String getIndicatorsStringFromPreferences(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_FILE_WITH_INDICATORS, Context.MODE_PRIVATE);
        return sharedPreferences.getString(INDICATORS_STRING_KEY, "");
    }

    private class IndicatorParser {

        private String jsonString;

        public boolean parse(String jsonString) {
            this.jsonString = jsonString;

            String indicatorName = null;
            boolean success = true;

            try {
                JSONObject json = new JSONObject(jsonString);
                JSONObject indicatorsJson = json.getJSONObject(IndicatorConstants.INDICATORS);

                indicators.clear();

                Iterator<?> keys = indicatorsJson.keys();
                while(keys.hasNext()) {
                    indicatorName = (String)keys.next();
                    indicators.put(indicatorName, new Indicator(indicatorName, datasetColors[getRandomInt()], indicatorsJson.getJSONObject(indicatorName)));
                }
                generateIndicatorNamesByGroupCollection(json);

            } catch (JSONException e) {
                e.printStackTrace();
                Logger.log(Logger.LogMode.ERROR, LOG_TAG, "IndicatorManager. Json from server is corrupted. Corruption detected at indicator with name " + indicatorName);
                success = false;
            }
            return success;
        }

        private void generateIndicatorNamesByGroupCollection(JSONObject json) throws JSONException {

            indicatorNamesByGroup.clear();
            JSONObject availableGroups = json.getJSONObject(IndicatorConstants.INDICATOR_GROUP);
            JSONObject indicatorsSortedByGroups = json.getJSONObject(IndicatorConstants.INDICATOR_BY_GROUP);
            Iterator availableGroupKeys = availableGroups.keys();
            Iterator indicatorsByGroupKeys = indicatorsSortedByGroups.keys();
            while(availableGroupKeys.hasNext() && indicatorsByGroupKeys.hasNext()) {
                List<String> indicators = new LinkedList<>();
                JSONArray indicatorsForGroupArray = indicatorsSortedByGroups.getJSONArray((String) indicatorsByGroupKeys.next());
                for(int i = 0; i < indicatorsForGroupArray.length(); i++) {
                    indicators.add(indicatorsForGroupArray.getString(i));
                }
                indicatorNamesByGroup.put(availableGroups.getString((String) availableGroupKeys.next()), indicators);
            }

        }

        private int getRandomInt() {
            Random random = new Random();
            return random.nextInt(datasetColors.length);
        }

        public String getJsonString() {
            return jsonString;
        }
    }

}
