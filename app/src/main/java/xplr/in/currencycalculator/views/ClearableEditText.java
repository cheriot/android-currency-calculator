package xplr.in.currencycalculator.views;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

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

    // set* followed by get* does not always return the desired value. Track them here.
    private String nextText;
    private String nextHint;

    private ArrayList<TextChangeListener> textChangeListeners;
    private TextView.OnEditorActionListener onEditorActionListener;
    private TextClearListener textClearListener;
    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        /**
         * Synchronized because google's PhoneNumberFormattingTextWatcher#afterTextChanged is, which
         * makes me thing this can be called again before its finished.
         */
        @Override
        public synchronized void afterTextChanged(Editable s) {
            // Prevent infinite recursion when we modify s.
            if(s.toString().equals(nextText)) return;


            // 1. Show the clear button when there is text to clear.
            // 2. Format the number as typed.
            if(s.length() == 0) {
                nextText = s.toString();
                clearButton.setVisibility(View.INVISIBLE);
            } else {
                String original = s.toString();
                int cursorIdx = editText.getSelectionStart();
                int cursorFromEnd = original.length() - cursorIdx;
                nextText = DisplayUtils.formatWhileTyping(original);
                if(!original.equals(nextText)) {
                    editText.removeTextChangedListener(this);
                    editText.setText(nextText);
                    editText.addTextChangedListener(this);
                    Log.v(LOG_TAG, "replace " + s + " with " + nextText);
                    // Setting the new text messes up the cursor's position.
                    // Set the cursor to be the same number of chars from the end as it was before.
                    editText.setSelection(nextText.length() - cursorFromEnd);
                }
                clearButton.setVisibility(View.VISIBLE);
            }

            // Resize editText to fit its content.
            resizeWidth();

            // Call external listeners.
            for(TextChangeListener textChangeListener : textChangeListeners) {
                String number = DisplayUtils.stripFormatting(nextText);
                textChangeListener.onTextChanged(number);
            }
        }
    };

    public ClearableEditText(Context context, AttributeSet attributeSet) {
        // Accept an attributeSet to work with Android Studio.
        // https://developer.android.com/training/custom-views/create-view.html
        super(context, attributeSet);

        textChangeListeners = new ArrayList<>();

        LayoutInflater.from(context).inflate(R.layout.view_clearable_edit_text, this, true);
        ButterKnife.bind(this);

        // Resize and hide the X when there's no text to clear.
        editText.addTextChangedListener(textWatcher);
        // Initialize even if there are no calls to setText.
        textWatcher.afterTextChanged(new SpannableStringBuilder());

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
                Log.v(LOG_TAG, "Text cleared, now show keyboard.");
                if(textClearListener != null) textClearListener.onTextCleared();
            }
        });
    }

    private static final int EMPTY_WIDTH_DP = 50;
    private void resizeWidth() {
        CharSequence text = nextText;
        CharSequence hint = nextHint;
        if(!TextUtils.isEmpty(text) || !TextUtils.isEmpty(hint)) {
            // Wide enough to show the longer of the text and the hint.
            calculateWidth(text, hint);
        } else {
            // Wide enough to make the editText clear and an easy hit target.
            editText.setWidth((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, EMPTY_WIDTH_DP, getResources().getDisplayMetrics()));
        }
    }

    private static final int EXTRA_WIDTH_DP = 10; // leave extra space after the numbers and before the clear button
    private void calculateWidth(CharSequence text, CharSequence hint) {
        float clearWidthPx = editText.getPaddingLeft() + editText.getPaddingRight();
        float extraSpacePx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, EXTRA_WIDTH_DP, getResources().getDisplayMetrics());
        float textWidthPx = text == null ? 0 : editText.getPaint().measureText(text, 0, text.length());
        float hintWidthPx = hint == null ? 0 : editText.getPaint().measureText(hint, 0, hint.length());
        // Max of the text and the hint so the size doesn't change when the user starts typing.
        float widthPx = extraSpacePx + Math.max(textWidthPx, hintWidthPx) + clearWidthPx;
        editText.setWidth((int) Math.ceil(widthPx));
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
        return DisplayUtils.stripFormatting(editText.getText().toString());
    }

    public void setHint(String hint) {
        Log.v(LOG_TAG, "setHint " + hint);
        nextHint = hint;
        textInputLayout.setHint(hint);
        // Undo the negative margin that will hide the hint.
        MarginLayoutParams textInputLayoutParams = (MarginLayoutParams)textInputLayout.getLayoutParams();
        textInputLayoutParams.setMargins(0,0,0,0);
        // Showing the hint above the editText moves the baseline. Realign the clear button.
        MarginLayoutParams clearButtonParams = (MarginLayoutParams)clearButton.getLayoutParams();
        float density = getContext().getResources().getDisplayMetrics().density;
        clearButtonParams.setMargins(0, (int)(6*density), 0, 0);
        resizeWidth();
    }

    public void setOnEditorActionListener(TextView.OnEditorActionListener onEditorActionListener) {
        this.onEditorActionListener = onEditorActionListener;
    }

    public void moveCursorToEnd() {
        editText.setSelection(editText.length());
    }

    public void addTextChangedListener(TextChangeListener textChangeListener) {
        textChangeListeners.add(textChangeListener);
    }

    public void removeTextChangedListener(TextChangeListener textChangeListener) {
        textChangeListeners.remove(textChangeListener);
    }

    public void setTextClearListener(TextClearListener textClearListener) {
        this.textClearListener = textClearListener;
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

    public interface TextChangeListener {
        void onTextChanged(String text);
    }

    public interface TextClearListener {
        void onTextCleared();
    }
}
