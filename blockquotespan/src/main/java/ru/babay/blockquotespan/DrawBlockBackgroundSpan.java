package ru.babay.blockquotespan;

import android.graphics.Canvas;
import android.text.style.ParagraphStyle;

/**
 * Created by babay on 27.08.2017.
 */

public interface DrawBlockBackgroundSpan extends ParagraphStyle {
    /**
     * draws a block background to canvas;
     * <p>
     * block SHOULD start and end on line break (i.e. block should contain one or more paragraphs).
     * block cannot start or end in the middle of a line
     * <p>
     * (0, 0) of canvas is positioned at left top padding.
     */
    void draw(Canvas c, int blockLeft, int blockTop, int blockRight, int blockBottom, int availWidth);

}
