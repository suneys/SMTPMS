package com.yoyo.smtpms.view;

import android.app.Activity;
import android.content.Context;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.text.Editable;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

/**
 * Created by Administrator on 2017/5/3 0003.
 */

public class KeyboarBuilder {
    private static final String TAG = "KeyboardBuilder";
    private Context mActivity;

    private KeyboardView mKeyboardView;
    private EditText mEditText;

    public KeyboarBuilder(Context ac, KeyboardView keyboardView, int keyBoardXmlResId, final EditText mEditText) {
        mActivity = ac;
        mKeyboardView = keyboardView;
        this.mEditText = mEditText;

        Keyboard mKeyboard = new Keyboard(mEditText.getContext(), keyBoardXmlResId);
        // Attach the keyboard to the view
        mKeyboardView.setKeyboard(mKeyboard);
        // Do not show the preview balloons
        mKeyboardView.setPreviewEnabled(false);
        //ac.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
//        if(mEditText.isFocusable()) {
//            ((InputMethodManager) mActivity.getSystemService(Activity.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
//        }
        showCustomKeyboard(mEditText);
        KeyboardView.OnKeyboardActionListener keyboardListener = new KeyboardView.OnKeyboardActionListener() {
            @Override
            public void onKey(int primaryCode, int[] keyCodes) {
                // Get the EditText and its Editable
//                View focusCurrent = mActivity.getWindow().getCurrentFocus();
//                if (focusCurrent == null || !(focusCurrent instanceof EditText)) {
//                    return;
//                }
                if(mEditText == null){
                    return;
                }

               // EditText edittext = (EditText) focusCurrent;
                Editable editable = mEditText.getText();
                int start = mEditText.getSelectionStart();

                // Handle key
                if (primaryCode == Keyboard.KEYCODE_CANCEL) {
                    mEditText.setText("");
                } else if (primaryCode == Keyboard.KEYCODE_DELETE) {
                    if (editable != null && start > 0) {
                        editable.delete(start - 1, start);
                    }
                } else {
                    // Insert character
                    editable.insert(start, Character.toString((char) primaryCode));
                }
            }

            @Override
            public void onPress(int arg0) {
            }

            @Override
            public void onRelease(int primaryCode) {
            }

            @Override
            public void onText(CharSequence text) {
            }

            @Override
            public void swipeDown() {
            }

            @Override
            public void swipeLeft() {
            }

            @Override
            public void swipeRight() {
            }

            @Override
            public void swipeUp() {
            }
        };
        mKeyboardView.setOnKeyboardActionListener(keyboardListener);
        registerEditText();
    }

    //绑定一个EditText
    public void registerEditText() {
        // Make the custom keyboard appear
        mEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
//                if (v != null) {
//                    ((InputMethodManager) mActivity.getSystemService(Activity.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(v.getWindowToken(), 0);
//                }
                if (hasFocus) {
                    showCustomKeyboard(v);
                } else {
                    hideCustomKeyboard();
                }
            }
        });
        mEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick");

                showCustomKeyboard(v);
            }
        });

        mEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d(TAG, "onTouch");

                EditText edittext = (EditText) v;
                int inType = edittext.getInputType();       // Backup the input type
                edittext.setInputType(InputType.TYPE_NULL); // Disable standard keyboard
                edittext.onTouchEvent(event);               // Call native handler
                edittext.setInputType(inType);              // Restore input type
                edittext.setSelection(edittext.getText().length());

                return true;
            }
        });
    }

    public void hideCustomKeyboard() {
        mKeyboardView.setVisibility(View.GONE);
        mKeyboardView.setEnabled(false);
    }

    public void showCustomKeyboard(View v) {
        if (v != null) {
            ((InputMethodManager) mActivity.getSystemService(Activity.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(v.getWindowToken(), 0);
        }

        mKeyboardView.setVisibility(View.VISIBLE);
        mKeyboardView.setEnabled(true);


    }

    public boolean isCustomKeyboardVisible() {
        return mKeyboardView.getVisibility() == View.VISIBLE;

    }
}
