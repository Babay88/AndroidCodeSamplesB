package ru.babay.blockquotespan;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.text.Layout;
import android.text.Spanned;
import android.text.style.LeadingMarginSpan;
import android.widget.TextView;

/**
 * Created by babay on 26.08.2017.
 */

public class TextViewBackgroundSpanDrawer {

    private static final Rect sTempRect = new Rect();
    private final TextView mTextView;
    private SpanSet<DrawBlockBackgroundSpan> mBackgroundSpans;

    public TextViewBackgroundSpanDrawer(TextView textView) {
        this.mTextView = textView;
    }

    public void draw(Canvas canvas) {
        CharSequence text = mTextView.getText();
        if (text instanceof Spanned) {
            if (mBackgroundSpans == null) {
                mBackgroundSpans = new SpanSet<>(DrawBlockBackgroundSpan.class);
            }
            mBackgroundSpans.init((Spanned) text, 0, text.length());
            drawSpans(canvas, mTextView.getLayout());
            mBackgroundSpans.recycle();
        }
    }

    private void drawSpans(Canvas canvas, Layout layout) {
        int drawTop;
        int drawBottom;
        int availWidth;
        synchronized (sTempRect) {
            if (!canvas.getClipBounds(sTempRect)) {
                // Negative range  -- do nothing
                return;
            }

            drawTop = sTempRect.top;
            drawBottom = sTempRect.bottom;
            availWidth = sTempRect.right - sTempRect.left;
        }


        for (int i = 0; i < mBackgroundSpans.numberOfSpans; i++) {
            int firstLine = layout.getLineForOffset(mBackgroundSpans.spanStarts[i]);
            int lastLine = layout.getLineForOffset(mBackgroundSpans.spanEnds[i] - 1);
            int blockTop = layout.getLineTop(firstLine);
            int blockBottom = layout.getLineBottom(lastLine);
            float blockLeft = layout.getLineLeft(firstLine);
            float blockRight = layout.getLineRight(firstLine);

            if (blockBottom <= drawTop || blockTop >= drawBottom) continue;

            for (int j = firstLine + 1; j <= lastLine; j++) {
                float right = layout.getLineRight(j);
                if (right > blockRight)
                    blockRight = right;
            }

            int spanStart = mBackgroundSpans.spanStarts[i];
            int spanEnd = mBackgroundSpans.spanEnds[i];
            for (int j = 0; j < mBackgroundSpans.numberOfSpans; j++) {
                DrawBlockBackgroundSpan otherSpan = mBackgroundSpans.spans[j];
                if (j != i && otherSpan instanceof LeadingMarginSpan &&
                        mBackgroundSpans.spanStarts[j] <= spanStart && mBackgroundSpans.spanEnds[j] >= spanEnd) {
                    blockLeft += ((LeadingMarginSpan) otherSpan).getLeadingMargin(false);
                }
            }

            mBackgroundSpans.spans[i].draw(canvas, (int) blockLeft, blockTop, (int) blockRight, blockBottom, availWidth);
        }
    }

    public void onTextSet() {
        CharSequence text = mTextView.getText();
        if (text instanceof Spanned) {
            EndAwareSpan[] spans = ((Spanned) text).getSpans(0, text.length(), EndAwareSpan.class);
            for (EndAwareSpan span : spans) {
                span.setSpanEnd(((Spanned) text).getSpanEnd(span));
            }
        }
    }
}
