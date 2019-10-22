package com.arke.sdk.ui.views;

import android.content.Context;
import android.graphics.Rect;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.EditText;

import androidx.appcompat.widget.AppCompatEditText;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class CurrencyEditText extends AppCompatEditText {
    private static final int MAX_LENGTH = 20;
    private static final int MAX_DECIMAL = 3;
    private CurrencyTextWatcher currencyTextWatcher = new CurrencyTextWatcher(this);

    public CurrencyEditText(Context context) {
        this(context, null);
    }

    public CurrencyEditText(Context context, AttributeSet attrs) {
        this(context, attrs, androidx.appcompat.R.attr.editTextStyle);
    }

    public CurrencyEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        this.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_LENGTH)});
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        if (focused) {
            this.addTextChangedListener(currencyTextWatcher);
        } else {
            this.removeTextChangedListener(currencyTextWatcher);
        }
    }

    private static class CurrencyTextWatcher implements TextWatcher {
        private final EditText editText;
        private String previousCleanString;

        CurrencyTextWatcher(EditText editText) {
            this.editText = editText;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // do nothing
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // do nothing
        }

        @Override
        public void afterTextChanged(Editable editable) {
            String str = editable.toString();

            // cleanString this the string which not contain prefix and ,
            String cleanString = str.replaceAll("[,]", "");
            // for prevent afterTextChanged recursive call
            if (cleanString.equals(previousCleanString) || cleanString.isEmpty()) {
                return;
            }
            previousCleanString = cleanString;

            String formattedString;
            if (cleanString.contains(".")) {
                formattedString = formatDecimal(cleanString);
            } else {
                formattedString = formatInteger(cleanString);
            }
            editText.removeTextChangedListener(this); // Remove listener
            editText.setText(formattedString);
            handleSelection();
            editText.addTextChangedListener(this); // Add back the listener
        }

        private String formatInteger(String str) {
            BigDecimal parsed = new BigDecimal(str);
            DecimalFormat formatter =
                    new DecimalFormat( "#,###", new DecimalFormatSymbols(Locale.US));
            return formatter.format(parsed);
        }

        private String formatDecimal(String str) {
            if (str.equals(".")) {
                return ".";
            }
            BigDecimal parsed = new BigDecimal(str);
            // example pattern VND #,###.00
            DecimalFormat formatter = new DecimalFormat( "#,###." + getDecimalPattern(str),
                    new DecimalFormatSymbols(Locale.US));
            formatter.setRoundingMode(RoundingMode.DOWN);
            return formatter.format(parsed);
        }

        /**
         * It will return suitable pattern for format decimal
         * For example: 10.2 -> return 0 | 10.23 -> return 00, | 10.235 -> return 000
         */
        private String getDecimalPattern(String str) {
            int decimalCount = str.length() - str.indexOf(".") - 1;
            StringBuilder decimalPattern = new StringBuilder();
            for (int i = 0; i < decimalCount && i < MAX_DECIMAL; i++) {
                decimalPattern.append("0");
            }
            return decimalPattern.toString();
        }

        private void handleSelection() {
            if (editText.getText().length() <= MAX_LENGTH) {
                editText.setSelection(editText.getText().length());
            } else {
                editText.setSelection(MAX_LENGTH);
            }
        }
    }
}