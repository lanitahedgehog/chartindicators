package com.github.mikephil.charting.components;

import android.graphics.Color;
import android.graphics.Paint;

import com.github.mikephil.charting.utils.Utils;

import java.text.DecimalFormat;

/**
 * Annotation is similar to LimitLine but displays the value (is this feature enabled) un a rectangle next to the annotation line.
 * Also it gives you ability to update value of the annotation (the chart will be redrawn). Now Annotations are available only for
 * right yAxis.
 */

public class Annotation extends ComponentBase {

    /** value (the y-value) */
    private float mValue = 0f;

    /** the width of the annotation line */
    private float mLineWidth = 2f;

    /** the color of the annotation line */
    protected int mLineColor = Color.rgb(237, 91, 91);

    /** indicates whether the label in a rectangle will be drawn */
    private boolean isLabelDrawEnabled = false;

    /** the style of the label text */
    private Paint.Style mTextStyle = Paint.Style.FILL_AND_STROKE;

    private DecimalFormat labelFormat;

    public Annotation () {}

    public Annotation(float value) {
        mValue = value;
    }

    /**
     * Returns the value that is set for this annotation.
     *
     * @return
     */
    public float getValue() {
        return mValue;
    }

    /** Returns if the label to the right of annotation should be drown */
    public boolean isLabelDrawEnabled() {
        return isLabelDrawEnabled;
    }

    /** Sets if the label to the right of annotation should be drown */
    public void setIsLabelDrawEnabled(boolean isLabelDrawEnabled) {
        this.isLabelDrawEnabled = isLabelDrawEnabled;
    }

    /**
     * set the line width of the chart (min = 0.2f, max = 12f); default 2f NOTE:
     * thinner line == better performance, thicker line == worse performance
     *
     * @param width
     */
    public void setLineWidth(float width) {

        if (width < 0.2f)
            width = 0.2f;
        if (width > 12.0f)
            width = 12.0f;
        mLineWidth = Utils.convertDpToPixel(width);
    }

    /**
     * returns the width of Annotation line
     *
     * @return
     */
    public float getLineWidth() {
        return mLineWidth;
    }

    public void setLabelFormat(DecimalFormat labelFormat) {
        this.labelFormat = labelFormat;
    }

    /**
     * Sets the color that will be used to draw Annotation line and rectangle with annotation value. Make sure to use
     * getResources().getColor(...)
     *
     * @param color
     */
    public void setAnnotationColor(int color) {
        mLineColor = color;
    }

    /**
     * Returns the color that is used for this Annotation line and rectangle with annotation value
     *
     * @return
     */
    public int getAnnotationColor() {
        return mLineColor;
    }

    /**
     * Sets the color of the value-text that is drawn next to the Annotation line.
     * Default: Paint.Style.FILL_AND_STROKE
     *
     * @param style
     */
    public void setTextStyle(Paint.Style style) {
        this.mTextStyle = style;
    }

    /**
     * Returns the color of the value-text that is drawn next to the Annotation line.
     *
     * @return
     */
    public Paint.Style getTextStyle() {
        return mTextStyle;
    }

    /**
     * Returns the label that is drawn next to the Annotation line.
     *
     * @return
     */
    public String getLabel() {
        return labelFormat.format(mValue);
    }

    public void updateAnnotationValue(float newValue) {
        mValue = newValue;
    }
}
