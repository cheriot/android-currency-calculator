package xplr.in.currencycalculator.views;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
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

    private TextView.OnEditorActionListener onEditorActionListener;

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
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                if(onEditorActionListener != null) onEditorActionListener.onEditorAction(v, actionId, event);
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
                imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
                Log.v(LOG_TAG, "Cleared, now show keyboard.");
            }
        });
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        return new SavedState(
                super.onSaveInstanceState(),
                getText(),
                editText.getHint()
        );
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Log.v(LOG_TAG, "onRestoreInstanceState " + getId() + "  " + state);
        SavedState savedState = (SavedState)state;
        super.onRestoreInstanceState(savedState.getSuperState());
        setText(savedState.getText());
        String hint = savedState.getHint();
        if (hint != null) setHint(hint);
    }

    @Override
    protected void dispatchSaveInstanceState(SparseArray<Parcelable> container) {
        // As we save our own instance state, ensure our children don't save and restore their state as well.
        // (it can overwrite the state restored here)
        super.dispatchFreezeSelfOnly(container);
    }

    @Override
    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        /** See comment in {@link #dispatchSaveInstanceState(android.util.SparseArray)} */
        super.dispatchThawSelfOnly(container);
    }

    public void setText(String text) {
        editText.setText(text);
    }

    public String getText() {
        return editText.getText().toString();
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

    public void setOnEditorActionListener(TextView.OnEditorActionListener onEditorActionListener) {
        this.onEditorActionListener = onEditorActionListener;
    }

    public void moveCursorToEnd() {
        editText.setSelection(editText.length());
    }

    public static class SavedState extends View.BaseSavedState {
        private final String text;
        private final String hint;

        public SavedState(Parcelable superState, String text, CharSequence hint) {
            super(superState);
            this.text = text;
            this.hint = hint != null ? hint.toString() : null;
        }

        private SavedState(Parcel in) {
            super(in);
            text = in.readString();
            hint = in.readString();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeString(text);
            out.writeString(hint);
        }

        public String getText() {
            return text;
        }

        public String getHint() {
            return hint;
        }

        public static final Parcelable.Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };

        @Override
        public String toString() {
            return "SavedState{" +
                    "text='" + text + '\'' +
                    ", hint='" + hint + '\'' +
                    '}';
        }
    }
}
