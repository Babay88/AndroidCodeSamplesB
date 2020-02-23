package ru.babay.blockquotespanexample;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;

import androidx.appcompat.app.AppCompatActivity;

import ru.babay.blockquotespan.BlockQuoteSpan;
import ru.babay.blockquotespan.TextViewWithBlockBackgrounds;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextViewWithBlockBackgrounds text = findViewById(R.id.text);

        Drawable dr = getResources().getDrawable(R.drawable.bg_blockquote);
        dr.setBounds(0, 0, dr.getIntrinsicWidth(), dr.getIntrinsicHeight());
        int margin = (int) (8 * getResources().getDisplayMetrics().density);

        String msg = getString(R.string.text);
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(msg);

        insertBlockQuote(builder, margin, margin, 218, 219 + 357, dr);
        insertBlockQuote(builder, margin, margin, 710, 710 + 527, dr);
        insertBlockQuote(builder, margin, margin, 710 + 170, 710 + 528, dr);

        text.setText(builder);
    }

    private void insertBlockQuote(SpannableStringBuilder builder, int leftMargin, int leftPadding, int start, int end, Drawable drawable) {
        BlockQuoteSpan span = new BlockQuoteSpan(leftMargin, leftPadding, drawable);
        if (start > 0 && builder.charAt(start - 1) != '\n') {
            builder.insert(start, "\n");
            ++start;
            ++end;
        }

        if (builder.charAt(end) != '\n') {
            builder.insert(end, "\n");
            ++end;
        }

        builder.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }
}
