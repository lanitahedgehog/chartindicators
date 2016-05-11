
package com.github.mikephil.charting.renderer;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import com.github.mikephil.charting.animation.ChartAnimator;
import com.github.mikephil.charting.buffer.CandleBodyBuffer;
import com.github.mikephil.charting.buffer.CandleShadowBuffer;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.interfaces.CandleDataProvider;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.Transformer;
import com.github.mikephil.charting.utils.Utils;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CandleStickChartRenderer extends LineScatterCandleRadarRenderer {

    private final int HIGHLIGHT_DETAILS_BOX_OFFSET = 2;
    protected CandleDataProvider mChart;

    private CandleShadowBuffer[] mShadowBuffers;
    private CandleBodyBuffer[] mBodyBuffers;

    private Paint mHighlightDetailsFrame = new Paint();
    private Paint mHighlightDetailsText = new Paint();
    private SimpleDateFormat mHighlightDateFormat = new SimpleDateFormat("dd MMM yyy" + " " + "HH:mm", Locale.getDefault());

    public CandleStickChartRenderer(CandleDataProvider chart, ChartAnimator animator,
            ViewPortHandler viewPortHandler) {
        super(animator, viewPortHandler);
        mChart = chart;
    }

    @Override
    public void initBuffers() {
        CandleData candleData = mChart.getCandleData();
        mShadowBuffers = new CandleShadowBuffer[candleData.getDataSetCount()];
        mBodyBuffers = new CandleBodyBuffer[candleData.getDataSetCount()];

        for (int i = 0; i < mShadowBuffers.length; i++) {
            CandleDataSet set = candleData.getDataSetByIndex(i);
            mShadowBuffers[i] = new CandleShadowBuffer(set.getValueCount() * 4);
            mBodyBuffers[i] = new CandleBodyBuffer(set.getValueCount() * 4);
        }
    }

    @Override
    public void drawData(Canvas c) {

        CandleData candleData = mChart.getCandleData();

        for (CandleDataSet set : candleData.getDataSets()) {

            if (set.isVisible() && set.getEntryCount() > 0)
                drawDataSet(c, set);
        }
    }

    protected void drawDataSet(Canvas c, CandleDataSet dataSet) {

        Transformer trans = mChart.getTransformer(dataSet.getAxisDependency());

        float phaseX = mAnimator.getPhaseX();
        float phaseY = mAnimator.getPhaseY();

        int dataSetIndex = mChart.getCandleData().getIndexOfDataSet(dataSet);

        List<CandleEntry> entries = dataSet.getYVals();

        int minx = entries.size() -1;
        while ( minx > 0 && entries.get(minx).getXIndex() > mMinDisplayedIndex ) minx --;
        int maxx = 0;
        while ( maxx <= entries.size() -1 && entries.get(maxx).getXIndex() <= mMaxDisplayedIndex+1 ) maxx++;

        int range = (maxx - minx) * 4;

        CandleBodyBuffer bodyBuffer = mBodyBuffers[dataSetIndex];
        bodyBuffer.setBodySpace(dataSet.getBodySpace());
        bodyBuffer.setPhases(phaseX, phaseY);
        bodyBuffer.limitFrom(minx);
        bodyBuffer.limitTo(maxx);
        bodyBuffer.feed(entries);

        trans.pointValuesToPixel(bodyBuffer.buffer);

        CandleShadowBuffer shadowBuffer = mShadowBuffers[dataSetIndex];
        shadowBuffer.setPhases(phaseX, phaseY);
        shadowBuffer.limitFrom(minx);
        shadowBuffer.limitTo(maxx);
        shadowBuffer.feed(entries);

        trans.pointValuesToPixel(shadowBuffer.buffer);

        mRenderPaint.setStrokeWidth(dataSet.getShadowWidth());

        // draw the body
        for (int j = 0; j < range; j += 4) {

            // get the entry
            CandleEntry e = entries.get(j / 4 + minx);

            if (dataSet.getShadowColorSameAsCandle()) {

                if (e.getOpen() > e.getClose())
                    mRenderPaint.setColor(
                            dataSet.getDecreasingColor() == ColorTemplate.COLOR_NONE ?
                                    dataSet.getColor(j) :
                                    dataSet.getDecreasingColor()
                    );

                else if (e.getOpen() < e.getClose())
                    mRenderPaint.setColor(
                            dataSet.getIncreasingColor() == ColorTemplate.COLOR_NONE ?
                                    dataSet.getColor(j) :
                                    dataSet.getIncreasingColor()
                    );

                else
                    mRenderPaint.setColor(
                            dataSet.getShadowColor() == ColorTemplate.COLOR_NONE ?
                                    dataSet.getColor(j) :
                                    dataSet.getShadowColor()
                    );

            } else {
                mRenderPaint.setColor(
                        dataSet.getShadowColor() == ColorTemplate.COLOR_NONE ?
                                dataSet.getColor(j) :
                                dataSet.getShadowColor()
                );
            }

            mRenderPaint.setStyle(Paint.Style.STROKE);

            // draw the shadow
            c.drawLine(shadowBuffer.buffer[j], shadowBuffer.buffer[j + 1],
                    shadowBuffer.buffer[j + 2], shadowBuffer.buffer[j + 3],
                    mRenderPaint);

            float leftBody = bodyBuffer.buffer[j];
            float open = bodyBuffer.buffer[j + 1];
            float rightBody = bodyBuffer.buffer[j + 2];
            float close = bodyBuffer.buffer[j + 3];

            // draw body differently for increasing and decreasing entry
            if (open > close) { // decreasing

                if (dataSet.getDecreasingColor() == ColorTemplate.COLOR_NONE) {
                    mRenderPaint.setColor(dataSet.getColor(j / 4 + minx));
                } else {
                    mRenderPaint.setColor(dataSet.getDecreasingColor());
                }

                mRenderPaint.setStyle(dataSet.getDecreasingPaintStyle());
                // draw the body
                c.drawRect(leftBody, close, rightBody, open, mRenderPaint);

            } else if(open < close) {

                if (dataSet.getIncreasingColor() == ColorTemplate.COLOR_NONE) {
                    mRenderPaint.setColor(dataSet.getColor(j / 4 + minx));
                } else {
                    mRenderPaint.setColor(dataSet.getIncreasingColor());
                }

                mRenderPaint.setStyle(dataSet.getIncreasingPaintStyle());
                // draw the body
                c.drawRect(leftBody, open, rightBody, close, mRenderPaint);
            } else { // equal values

                mRenderPaint.setColor(dataSet.getShadowColor());
                c.drawLine(leftBody, open, rightBody, close, mRenderPaint);
            }

        }
    }

    // /**
    // * Transforms the values of an entry in order to draw the candle-body.
    // *
    // * @param bodyPoints
    // * @param e
    // * @param bodySpace
    // */
    // private void transformBody(float[] bodyPoints, CandleEntry e, float
    // bodySpace, Transformer trans) {
    //
    // float phase = mAnimator.getPhaseY();
    //
    // bodyPoints[0] = e.getXIndex() - 0.5f + bodySpace;
    // bodyPoints[1] = e.getClose() * phase;
    // bodyPoints[2] = e.getXIndex() + 0.5f - bodySpace;
    // bodyPoints[3] = e.getOpen() * phase;
    //
    // trans.pointValuesToPixel(bodyPoints);
    // }
    //
    // /**
    // * Transforms the values of an entry in order to draw the candle-shadow.
    // *
    // * @param shadowPoints
    // * @param e
    // */
    // private void transformShadow(float[] shadowPoints, CandleEntry e,
    // Transformer trans) {
    //
    // float phase = mAnimator.getPhaseY();
    //
    // shadowPoints[0] = e.getXIndex();
    // shadowPoints[1] = e.getHigh() * phase;
    // shadowPoints[2] = e.getXIndex();
    // shadowPoints[3] = e.getLow() * phase;
    //
    // trans.pointValuesToPixel(shadowPoints);
    // }

    @Override
    public void drawValues(Canvas c) {

        // if values are drawn
        if (mChart.getCandleData().getYValCount() < mChart.getMaxVisibleCount()
                * mViewPortHandler.getScaleX()) {

            List<CandleDataSet> dataSets = mChart.getCandleData().getDataSets();

            for (int i = 0; i < dataSets.size(); i++) {

                CandleDataSet dataSet = dataSets.get(i);

                if (!dataSet.isDrawValuesEnabled() || dataSet.getEntryCount() == 0)
                    continue;

                // apply the text-styling defined by the DataSet
                applyValueTextStyle(dataSet);

                Transformer trans = mChart.getTransformer(dataSet.getAxisDependency());

                List<CandleEntry> entries = dataSet.getYVals();

                int minx = Math.max(mMinX, 0);
                int maxx = Math.min(mMaxX + 1, entries.size());

                float[] positions = trans.generateTransformedValuesCandle(
                        entries, mAnimator.getPhaseX(), mAnimator.getPhaseY(), minx, maxx);

                float yOffset = Utils.convertDpToPixel(5f);

                for (int j = 0; j < positions.length; j += 2) {

                    float x = positions[j];
                    float y = positions[j + 1];

                    if (!mViewPortHandler.isInBoundsRight(x))
                        break;

                    if (!mViewPortHandler.isInBoundsLeft(x) || !mViewPortHandler.isInBoundsY(y))
                        continue;

                    CandleEntry entry = entries.get(j / 2 + minx);

                    drawValue(c, dataSet.getValueFormatter(), entry.getHigh(), entry, i, x, y - yOffset);
                }
            }
        }
    }

    @Override
    public void drawExtras(Canvas c) {
    }

    @Override
    public void drawHighlighted(Canvas c, Highlight[] indices) {

        for (int i = 0; i < indices.length; i++) {

            int xIndex = indices[i].getXIndex(); // get the
                                                 // x-position

            CandleDataSet set = mChart.getCandleData().getDataSetByIndex(
                    indices[i].getDataSetIndex());

            if (set == null || !set.isHighlightEnabled())
                continue;

            CandleEntry e = set.getEntryForXIndex(xIndex);

            if (e == null || e.getXIndex() != xIndex)
                continue;

            float low = e.getLow() * mAnimator.getPhaseY();
            float high = e.getHigh() * mAnimator.getPhaseY();
            float y = (low + high) / 2f;

            float min = mChart.getYChartMin();
            float max = mChart.getYChartMax();


            float[] pts = new float[] {
                    xIndex, y
            };

            mChart.getTransformer(set.getAxisDependency()).pointValuesToPixel(pts);

            // draw the lines
            drawHighlightLines(c, pts, set);

            if(drawHighlightDetails) {
                Date date = new Date(Long.parseLong((String) mChart.getData().getXVals().get(xIndex)));
                initHighlightTextPaint(set);
                drawHighlightLabel(c, calculateHighlightFrame(pts), set, e, mHighlightDateFormat.format(date));
            }
        }
    }

    /**Draws box with detailed info about a highlighted point
     *
     * @param canvas
     * @param labelFrameRect coordinates of the box to be drawn
     * @param dataSet
     * @param candleEntry
     * @param xAxisLabel label on x axis that corresponds to the highlighted candle entry
     */
    private void drawHighlightLabel(Canvas canvas, RectF labelFrameRect, CandleDataSet dataSet, CandleEntry candleEntry, String xAxisLabel) {
        mHighlightDetailsFrame.setColor(dataSet.getHighlightBoxBackground());
        mHighlightDetailsFrame.setStrokeWidth(2f);
        mHighlightDetailsFrame.setStyle(Paint.Style.FILL);
        //draw white semi-transparent rectangle
        canvas.drawRoundRect(labelFrameRect, 10, 10, mHighlightDetailsFrame);

        mHighlightDetailsFrame.setColor(dataSet.getHighLightColor());
        mHighlightDetailsFrame.setStyle(Paint.Style.STROKE);
        //draw frame around semi-transparent rectangle
        canvas.drawRoundRect(labelFrameRect, 10, 10, mHighlightDetailsFrame);

        String[] labelsToDraw = {
                "O: " + String.valueOf(candleEntry.getOpen()),
                "H: " + String.valueOf(candleEntry.getHigh()),
                "L: " + String.valueOf(candleEntry.getLow()),
                "C: " + String.valueOf(candleEntry.getClose()),
                xAxisLabel
        };

        String current;
        float textHeight =  Utils.calcTextHeight(mHighlightDetailsText, "A");
        float textWidth;
        float textYCoord = labelFrameRect.top + 2*textHeight;

        for (int i = 0; i < labelsToDraw.length; i++,  textYCoord += textHeight*2) {
            current = labelsToDraw[i];
            textWidth = Utils.calcTextWidth(mHighlightDetailsText, current);
            canvas.drawText(current, labelFrameRect.left + ((labelFrameRect.right - labelFrameRect.left) - textWidth)/2, textYCoord, mHighlightDetailsText);
        }
    }

    private void initHighlightTextPaint(CandleDataSet dataSet) {
        mHighlightDetailsText.setColor(dataSet.getHighlightTextColor());
        mHighlightDetailsText.setTextSize(dataSet.getHighlightTextSize());
        mHighlightDetailsText.setAntiAlias(true);
    }

    /**
     * Calculates coordinates for box-container (in which highlight point info will be drawn).
     * @param pts coordinates for highlight value
     * @return
     */
    private RectF calculateHighlightFrame(float[] pts) {
        RectF highlightFrame = new RectF();

        float approximateWidth = Utils.calcTextWidth(mHighlightDetailsText, " 22 Dec 2015 12:55 ");
        float approximateHeight = Utils.calcTextHeight(mHighlightDetailsText, "A");
        float allLinesHeight = approximateHeight * 11;

        if((mViewPortHandler.contentBottom() - pts[1]) > pts[1]) {
            //draw details in the bottom
            highlightFrame.bottom = pts[1] + (mViewPortHandler.contentBottom() - pts[1]) / 2 + allLinesHeight / 2;
            if(highlightFrame.bottom > mViewPortHandler.contentBottom()) {
                highlightFrame.bottom = mViewPortHandler.contentBottom() - mViewPortHandler.offsetBottom() - HIGHLIGHT_DETAILS_BOX_OFFSET;
            }
            highlightFrame.top = highlightFrame.bottom - allLinesHeight;
        } else {
            //draw details in the top
            highlightFrame.top = (pts[1] - mViewPortHandler.contentTop()) / 2 - allLinesHeight / 2;
            if(highlightFrame.top < mViewPortHandler.contentTop()) {
                highlightFrame.top = mViewPortHandler.contentTop() + mViewPortHandler.offsetTop() + HIGHLIGHT_DETAILS_BOX_OFFSET;
            }
            highlightFrame.bottom = highlightFrame.top + allLinesHeight;
        }

        if(pts[0] < mViewPortHandler.contentLeft() || pts[0] > mViewPortHandler.contentRight() ||
                pts[0] - approximateWidth/2 >= mViewPortHandler.contentLeft() && pts[0] + approximateWidth/2 <= mViewPortHandler.contentRight()) {
            highlightFrame.left = pts[0] - approximateWidth/2;
            highlightFrame.right = pts[0] + approximateWidth/2;
        }
        else if (pts[0] - approximateWidth/2 < mViewPortHandler.contentLeft()) {
            highlightFrame.left = mViewPortHandler.contentLeft() + HIGHLIGHT_DETAILS_BOX_OFFSET;
            highlightFrame.right = highlightFrame.left + approximateWidth;
        } else if(pts[0] + approximateWidth/2 > mViewPortHandler.contentRight()) {
            highlightFrame.right = mViewPortHandler.contentRight() - HIGHLIGHT_DETAILS_BOX_OFFSET;
            highlightFrame.left = highlightFrame.right - approximateWidth;
        }
        return highlightFrame;
    }

}
