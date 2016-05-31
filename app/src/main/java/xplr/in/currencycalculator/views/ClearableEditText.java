package xplr.in.currencycalculator.views;

import android.content.Context;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import xplr.in.currencycalculator.R;

/**
 * Created by cheriot on 5/25/16.
 */
public class ClearableEditText extends FrameLayout {

    private static final String LOG_TAG = ClearableEditText.class.getSimpleName();

    @Bind(R.id.text_intput_layout) TextInputLayout textInputLayout;
    @Bind(R.id.edit_text) EditText editText;
    @Bind(R.id.clear_button) ImageButton clearButton;

    public ClearableEditText(Context context, AttributeSet attributeSet) {
        // Accept an attributeSet to work with Android Studio.
        // https://developer.android.com/training/custom-views/create-view.html
        super(context, attributeSet);

        LayoutInflater.from(context).inflate(R.layout.view_clearable_edit_text, this, true);
        ButterKnife.bind(this);

        // Hide the X when there's no text to clear.
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() == 0) {
                    clearButton.setVisibility(View.INVISIBLE);
                } else {
                    clearButton.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Hide the keyboard when the <done> button is pressed.
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                }
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                return true;
            }
        });

        // Clear button clears
        clearButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.getText().clear();
                editText.requestFocus();
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(editText, InputMethodManager.SHOW_FORCED);
                Log.v(LOG_TAG, "Cleared, now show keyboard.");
            }
        });
    }

    public EditText getEditText() {
        return editText;
    }

    public void setHint(String hint) {
        textInputLayout.setHint(hint);
        // Undo the negative margin that will hide the hint.
        MarginLayoutParams textInputLayoutParams = (MarginLayoutParams)textInputLayout.getLayoutParams();
        textInputLayoutParams.setMargins(0,0,0,0);
        // Showing the hint above the editText moves the baseline. Realign the clear button.
        MarginLayoutParams clearButtonParams = (MarginLayoutParams)clearButton.getLayoutParams();
        float density = getContext().getResources().getDisplayMetrics().density;
        clearButtonParams.setMargins(0, (int)(6*density), 0, 0);
    }

    public void moveCursorToEnd() {
        editText.setSelection(editText.length());
    }
}
