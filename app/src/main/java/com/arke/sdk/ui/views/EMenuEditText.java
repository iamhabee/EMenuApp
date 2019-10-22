package com.arke.sdk.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatEditText;

import com.arke.sdk.R;
import com.arke.sdk.utilities.FontUtils;
//import com.elitepath.android.emenu.R;


/**
 * @author Wan Clem
 */

public class EMenuEditText extends AppCompatEditText {
    private static final String DEFAULT_SCHEMA = "xmlns:android=\"http://schemas.android.com/apk/res/android\"";
    private int textStyle = 0;

    public EMenuEditText(Context context) {
        this(context, null);
    }

    public EMenuEditText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EMenuEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (!isInEditMode()) {
            init(context, attrs);
        }
    }

    private void init(Context context, AttributeSet attrs) {

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.EMenuTextView);

            textStyle = 0;
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

    private void applyCustomFont(Context context, int textStyle) {
        Typeface typeface = FontUtils.selectTypeface(context, textStyle);
        setTypeface(typeface);
    }

    @Override
    public void setInputType(int type) {
        super.setInputType(type);
        if (getContext() != null) {
            applyCustomFont(getContext(), textStyle);
        }
    }
}

