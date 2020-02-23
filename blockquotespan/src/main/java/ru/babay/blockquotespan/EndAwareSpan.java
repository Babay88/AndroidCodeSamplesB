package ru.babay.blockquotespan;


/**
 * a span that should know, where it's end is
 */
public interface EndAwareSpan {
    void setSpanEnd(int end);
}
