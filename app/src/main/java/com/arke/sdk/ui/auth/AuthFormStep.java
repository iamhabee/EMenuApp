package com.arke.sdk.ui.auth;

import android.annotation.SuppressLint;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ToggleButton;

import androidx.core.content.ContextCompat;

import com.arke.sdk.R;
import com.arke.sdk.utilities.UiUtils;
//import com.elitepath.android.emenu.R;
import com.arke.sdk.companions.Globals;
import com.arke.sdk.preferences.AppPrefs;
import com.arke.sdk.ui.views.EMenuEditText;

import java.util.concurrent.atomic.AtomicBoolean;

import ernestoyaquello.com.verticalstepperform.Step;
import ernestoyaquello.com.verticalstepperform.VerticalStepperFormView;

@SuppressWarnings("ConstantConditions")
public class AuthFormStep extends Step<String> {

    private Globals.AuthFormStepType authFormStepType;
    private String hint;
    private AtomicBoolean inputEntered = new AtomicBoolean(false);
    private EMenuEditText stepInputView;
    private VerticalStepperFormView verticalStepperFormView;

    public AuthFormStep(String title, String hint, Globals.AuthFormStepType authFormStepType, VerticalStepperFormView stepperFormView) {
        super(title);
        this.hint = hint;
        this.authFormStepType = authFormStepType;
        this.verticalStepperFormView = stepperFormView;
    }

    @Override
    public String getStepData() {
        //We get the step's data from the value that the user has typed in the EditText view.
        Editable inputValue = stepInputView.getText();
        return inputValue != null ? inputValue.toString() : "";
    }

    @Override
    public String getStepDataAsHumanReadableString() {
        // Because the step's data is already a human-readable string, we don't need to convert it.
        // However, we return "(Empty)" if the text is empty to avoid not having any text to display.
        // This string will be displayed in the subtitle of the step whenever the step gets closed.
        String stepData = getStepData();
        return !stepData.isEmpty() ? stepData : "(Empty)";
    }

    @Override
    public void restoreStepData(String stepData) {
        stepInputView.setText(stepData);
    }

    @Override
    protected IsDataValid isStepDataValid(String stepData) {
        // The step's data (i.e., the user name) will be considered valid only if it is longer than
        // three characters. In case it is not, we will display an error message for feedback.
        // In an optional step, you should implement this method to always return a valid value.
        String errorMessage = "";
        if (authFormStepType == Globals.AuthFormStepType.STEP_TYPE_EMAIL) {
            if (!Patterns.EMAIL_ADDRESS.matcher(stepData).find()) {
                errorMessage = "Email Address not yet valid";
            }
        } else if (authFormStepType == Globals.AuthFormStepType.STEP_TYPE_REPEAT_PASSWORD) {
            String previousPassword = AppPrefs.getPreviousAuthPassword();
            if (!previousPassword.equals(stepData)) {
                errorMessage = "Passwords yet to match";
            }
        }
        if (!inputEntered.get()) {
            return new IsDataValid(true, errorMessage);
        }
        return new IsDataValid(errorMessage.isEmpty(), errorMessage);
    }

    @Override
    protected View createStepContentLayout() {
        // Here we generate the view that will be used by the library as the content of the step.
        // In this case we do it programmatically, but we could also do it by inflating an XML layout.
        @SuppressLint("InflateParams") View rootView = LayoutInflater.from(getContext()).inflate(R.layout.auth_form_layout, null, false);
        stepInputView = rootView.findViewById(R.id.auth_form_box);
        ToggleButton passwordVisibilityView = rootView.findViewById(R.id.toggle_sign_up_password_visibility);
        UiUtils.toggleViewVisibility(passwordVisibilityView, false);
        stepInputView.setHintTextColor(ContextCompat.getColor(getContext(), R.color.gray13));
        stepInputView.setSingleLine(false);
        if (authFormStepType == Globals.AuthFormStepType.STEP_TYPE_TEXT) {
            stepInputView.setInputType(InputType.TYPE_CLASS_TEXT);
        } else if (authFormStepType == Globals.AuthFormStepType.STEP_TYPE_PASSWORD || authFormStepType == Globals.AuthFormStepType.STEP_TYPE_REPEAT_PASSWORD) {
            UiUtils.toggleViewVisibility(passwordVisibilityView, true);
            stepInputView.setSingleLine(true);
            stepInputView.setTransformationMethod(PasswordTransformationMethod.getInstance());
        } else if (authFormStepType == Globals.AuthFormStepType.STEP_TYPE_EMAIL) {
            stepInputView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS | InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS);
            stepInputView.setSingleLine(true);
        }
        stepInputView.setHint(hint);
        passwordVisibilityView.setOnCheckedChangeListener((compoundButton, checked) -> {
            if (checked) {
                stepInputView.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                stepInputView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                stepInputView.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
            int pwdLength = stepInputView.getText().toString().trim().length();
            stepInputView.setSelection(pwdLength > 0 ? pwdLength : 0);
        });
        stepInputView.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                inputEntered.set(true);
                // Whenever the user updates the user name text, we update the state of the step.
                // The step will be marked as completed only if its data is valid, which will be
                // checked automatically by the form with a call to isStepDataValid().
                if (authFormStepType == Globals.AuthFormStepType.STEP_TYPE_PASSWORD) {
                    AppPrefs.persistRestaurantOrBarPassword(s.toString().trim());
                    AppPrefs.persistPreviousAuthPassword(s.toString().trim());
                } else if (authFormStepType == Globals.AuthFormStepType.STEP_TYPE_EMAIL) {
                    AppPrefs.persistRestaurantOrBarEmailAddress(s.toString().trim());
                } else if (authFormStepType == Globals.AuthFormStepType.STEP_TYPE_TEXT) {
                    AppPrefs.persistRestaurantOrBarName(s.toString().trim());
                }
                markAsCompletedOrUncompleted(true);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }

        });
        stepInputView.setOnEditorActionListener((textView, action, keyEvent) -> {
            if (action == EditorInfo.IME_ACTION_DONE || action == EditorInfo.IME_ACTION_NEXT) {
                markAsCompletedOrUncompleted(true);
                if (isCompleted()) {
                    verticalStepperFormView.goToNextStep(true);
                }
            }
            return false;
        });
        return rootView;
    }

    @Override
    protected void onStepOpened(boolean animated) {

    }

    @Override
    protected void onStepClosed(boolean animated) {

    }

    @Override
    protected void onStepMarkedAsCompleted(boolean animated) {

    }

    @Override
    protected void onStepMarkedAsUncompleted(boolean animated) {

    }

}
