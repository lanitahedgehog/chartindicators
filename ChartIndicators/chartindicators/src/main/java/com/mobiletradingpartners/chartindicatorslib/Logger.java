package com.mobiletradingpartners.chartindicatorslib;

import android.util.Log;

import com.mobiletradingpartners.chartindicatorslib.BuildConfig;

/**
 * Encapsulates logging logic. Lets to enable/disable logging depending on a BuildConfig
 */
public class Logger {

    public static void log(LogMode logMode, String logTag, String message) {

        switch (logMode) {
            case DEBUG:
                if(BuildConfig.LOGS_LEVEL == LogLevel.FULL) {
                    Log.d(logTag, message);
                }
                break;
            case INFO:
                if(BuildConfig.LOGS_LEVEL == LogLevel.FULL || BuildConfig.LOGS_LEVEL == LogLevel.EMERGENCY || BuildConfig.LOGS_LEVEL == LogLevel.INFO) {
                    Log.i(logTag, message);
                }
                break;
            case ERROR:
                if(BuildConfig.LOGS_LEVEL == LogLevel.FULL || BuildConfig.LOGS_LEVEL == LogLevel.EMERGENCY || BuildConfig.LOGS_LEVEL == LogLevel.INFO) {
                    Log.e(logTag, message);
                }
                break;
        }
    }

    public enum LogMode {
        DEBUG,
        INFO,
        ERROR
    }

    public enum LogLevel {
        NONE,
        INFO,
        EMERGENCY,
        FULL
    }
}
