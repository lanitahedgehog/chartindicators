
package com.github.mikephil.charting.data;

import android.graphics.Color;

import java.util.List;

/**
 * Baseclass of all DataSets for Bar-, Line-, Scatter- and CandleStickChart.
 * 
 * @author Philipp Jahoda
 */
public abstract class BarLineScatterCandleBubbleDataSet<T extends Entry> extends DataSet<T> {

    /** default highlight color */
    protected int mHighLightColor = Color.rgb(255, 187, 115);

    // fields that are used by MTP project to draw details about highlight
    private int mHighlightBoxBackground = Color.argb(160, 255, 255, 255);
    private int mHighlightTextColor = Color.BLACK;
    private float mHighlightTextSize = 28f;

    public BarLineScatterCandleBubbleDataSet(List<T> yVals, String label) {
        super(yVals, label);
    }

    /**
     * Sets the color that is used for drawing the highlight indicators. Dont
     * forget to resolve the color using getResources().getColor(...) or
     * Color.rgb(...).
     * 
     * @param color
     */
    public void setHighLightColor(int color) {
        mHighLightColor = color;
    }

    /**
     * Returns the color that is used for drawing the highlight indicators.
     * 
     * @return
     */
    public int getHighLightColor() {
        return mHighLightColor;
    }

    public int getHighlightBoxBackground() {
        return mHighlightBoxBackground;
    }

    public void setHighlightBoxBackground(int mHighlightBoxBackground) {
        this.mHighlightBoxBackground = mHighlightBoxBackground;
    }

    public int getHighlightTextColor() {
        return mHighlightTextColor;
    }

    public void setHighlightTextColor(int mHighlightTextColor) {
        this.mHighlightTextColor = mHighlightTextColor;
    }

    public float getHighlightTextSize() {
        return mHighlightTextSize;
    }

    /**
     *
     * @param mHighlightTextSize value of text size in px
     */
    public void setHighlightTextSize(float mHighlightTextSize) {
        this.mHighlightTextSize = mHighlightTextSize;
    }
}
