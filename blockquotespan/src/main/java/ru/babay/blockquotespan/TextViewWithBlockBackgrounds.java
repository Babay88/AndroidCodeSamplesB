package ru.babay.blockquotespan;

import android.content.Context;
import android.graphics.Canvas;
import android.text.Layout;
import android.util.AttributeSet;
import android.view.Gravity;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

/**
 * Created by babay on 26.08.2017.
 */

public class TextViewWithBlockBackgrounds extends AppCompatTextView {
    private TextViewBackgroundSpanDrawer backgroundSpanDrawer = new TextViewBackgroundSpanDrawer(this);

    private float mShadowRadius;
    private float mShadowDx;
    private float mShadowDy;


    public TextViewWithBlockBackgrounds(Context context) {
        this(context, null);
    }

    public TextViewWithBlockBackgrounds(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TextViewWithBlockBackgrounds(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        final int compoundPaddingLeft = getCompoundPaddingLeft();
        final int compoundPaddingTop = getCompoundPaddingTop();
        final int compoundPaddingBottom = getCompoundPaddingBottom();
        final int scrollX = getScrollX();
        final int scrollY = getScrollY();
        final int right = getRight();
        final int left = getLeft();
        final int bottom = getBottom();
        final int top = getTop();


        int extendedPaddingTop = getExtendedPaddingTop();
        int extendedPaddingBottom = getExtendedPaddingBottom();

        final int vspace = bottom - top - compoundPaddingBottom - compoundPaddingTop;
        final int maxScrollY = getLayout().getHeight() - vspace;

        float clipLeft = compoundPaddingLeft + scrollX;
        float clipTop = (scrollY == 0) ? 0 : extendedPaddingTop + scrollY;
        float clipRight = right - left - getCompoundPaddingRight() + scrollX;
        float clipBottom = bottom - top + scrollY -
                ((scrollY == maxScrollY) ? 0 : extendedPaddingBottom);

        if (mShadowRadius != 0) {
            clipLeft += Math.min(0, mShadowDx - mShadowRadius);
            clipRight += Math.max(0, mShadowDx + mShadowRadius);

            clipTop += Math.min(0, mShadowDy - mShadowRadius);
            clipBottom += Math.max(0, mShadowDy + mShadowRadius);
        }

        canvas.clipRect(clipLeft, clipTop, clipRight, clipBottom);
        int voffsetText = 0;

        // translate in by our padding
        {
            /* shortcircuit calling getVerticaOffset() */
            if ((getGravity() & Gravity.VERTICAL_GRAVITY_MASK) != Gravity.TOP) {
                voffsetText = getVerticalOffset();
            }
            canvas.translate(compoundPaddingLeft, extendedPaddingTop + voffsetText);
        }

        backgroundSpanDrawer.draw(canvas);

        canvas.restore();

        super.onDraw(canvas);
    }

    public void setShadowLayer(float radius, float dx, float dy, int color) {
        super.setShadowLayer(radius, dx, dy, color);
        mShadowRadius = radius;
        mShadowDx = dx;
        mShadowDy = dy;
    }

    private int getVerticalOffset() {
        int voffset = 0;
        final int gravity = getGravity() & Gravity.VERTICAL_GRAVITY_MASK;

        Layout l = getLayout();

        if (gravity != Gravity.TOP) {
            int boxht;

            boxht = getMeasuredHeight() - getExtendedPaddingTop() -
                    getExtendedPaddingBottom();

            int textht = l.getHeight();

            if (textht < boxht) {
                if (gravity == Gravity.BOTTOM)
                    voffset = boxht - textht;
                else // (gravity == Gravity.CENTER_VERTICAL)
                    voffset = (boxht - textht) >> 1;
            }
        }
        return voffset;
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(text, type);
        if (backgroundSpanDrawer != null) {
            backgroundSpanDrawer.onTextSet();
        }
    }
}
