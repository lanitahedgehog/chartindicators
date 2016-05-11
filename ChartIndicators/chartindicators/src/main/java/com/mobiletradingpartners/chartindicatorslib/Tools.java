package com.mobiletradingpartners.chartindicatorslib;

import android.content.Context;

import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Created by lanitka on 4/20/16.
 */
public class Tools {

    private static DecimalFormatSymbols safeDecimalFormatSymbols;

    public static DecimalFormatSymbols getSafeDecimalFormatSymbol(Context ctx ) {
        if ( safeDecimalFormatSymbols == null ) {
            safeDecimalFormatSymbols = new DecimalFormatSymbols( Locale.UK );
            safeDecimalFormatSymbols.setDecimalSeparator( '.' );
        }
        return safeDecimalFormatSymbols;
    }

}
