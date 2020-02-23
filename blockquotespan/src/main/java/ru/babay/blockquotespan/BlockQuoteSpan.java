package ru.babay.blockquotespan;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.text.TextPaint;
import android.text.style.LeadingMarginSpan;
import android.text.style.LineHeightSpan;
import android.text.style.MetricAffectingSpan;

/**
 * Created by babay on 27.08.2017.
 */

public class BlockQuoteSpan extends MetricAffectingSpan implements LeadingMarginSpan, LineHeightSpan, DrawBlockBackgroundSpan, EndAwareSpan {

    private final int leftPaddingPx;
    private final Drawable drawable;
    private final int leftMarginPx;
    private int spanEnd;
    private int ascentAdd;
    private int initialAscent;

    public BlockQuoteSpan(int leftMarginPx, int leftPaddingPx, Drawable drawable) {
        this.leftMarginPx = leftMarginPx;
        this.leftPaddingPx = leftPaddingPx;
        this.drawable = drawable;
    }

    @Override
    public int getLeadingMargin(boolean first) {
        return leftPaddingPx + leftMarginPx;
    }

    @Override
    public void drawLeadingMargin(Canvas c, Paint p, int x, int dir, int top, int baseline, int bottom, CharSequence text, int start, int end, boolean first, Layout layout) {
    }

    /**
     * we can change TextPaint here
     * called when measuring
     *
     * @param p
     */
    @Override
    public void updateMeasureState(TextPaint p) {
        //this is to emulate some right padding: increase textScaleX when measuring and do not increase it when drawing
        p.setTextScaleX(1.05f);
    }

    /**
     * we can change TextPaint here
     * called when drawing
     *
     * @param p
     */
    @Override
    public void updateDrawState(TextPaint p) {
    }

    /**
     * if text is multiline, this method is called for each line inside span
     *
     * @param text
     * @param start      current line start in text
     * @param end        current line end in text
     * @param spanstartv span's top position (in pixels)
     * @param v          -- current line's top position (in pixels)
     * @param fm         -- font metrics
     */
    @Override
    public void chooseHeight(CharSequence text, int start, int end, int spanstartv, int v, Paint.FontMetricsInt fm) {
        if (spanstartv == v) { // this is definitely first line, increase line ascent
            initialAscent = fm.ascent;
            fm.ascent *= 1.5;
            ascentAdd = (int) Math.abs(initialAscent * 0.5);
        } else if (initialAscent != 0) { // this is second line, restore line ascent
            fm.ascent = initialAscent;
            initialAscent = 0;
        }

        boolean newlineAfterSpan = text.charAt(spanEnd) == '\n';
        if (newlineAfterSpan) {
            --end;
        }
        if (end == this.spanEnd) { // this is last line, increase descent
            fm.descent += ascentAdd;
        }
    }

    @Override
    public void draw(Canvas c, int blockLeft, int blockTop, int blockRight, int blockBottom, int availWidth) {
        blockLeft += leftMarginPx;
        drawable.setBounds(blockLeft, blockTop, blockRight, blockBottom);
        drawable.draw(c);
    }

    @Override
    public void setSpanEnd(int spanEnd) {
        this.spanEnd = spanEnd;
    }
}
