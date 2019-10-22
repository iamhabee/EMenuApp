package com.arke.sdk.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

import com.arke.sdk.R;
import com.arke.sdk.utilities.FontUtils;
//import com.elitepath.android.emenu.R;

public class EMenuTextView extends AppCompatTextView {

    private static final String DEFAULT_SCHEMA = "xmlns:android=\"http://schemas.android.com/apk/res/android\"";

    public EMenuTextView(Context context) {
        super(context);
    }

    public EMenuTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.EMenuTextView);
            int textStyle;
            if (a.hasValue(R.styleable.EMenuTextView_textStyle)) {
                textStyle = a.getInt(R.styleable.EMenuTextView_textStyle, 0);
            } else {
                //use default schema
                textStyle = attrs.getAttributeIntValue(DEFAULT_SCHEMA, "textStyle", 0);
            }
            a.recycle();
            applyCustomFont(context, textStyle);
        }
    }

    public void applyCustomFont(Context context, int textStyle) {
        Typeface typeface = FontUtils.selectTypeface(context, textStyle);
        if (typeface != null) {
            setTypeface(typeface);
        }
    }

}
