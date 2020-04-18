package com.yoyo.smtpms.view;

import android.app.Activity;
import android.app.Dialog;
import android.inputmethodservice.KeyboardView;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.yoyo.smtpms.R;

/**
 * @author Administrator
 * @date 2019-10-27
 */
public class MyDialog {

    private Activity mHostActivity;
    private TextView txtDialog;
    private EditText etDialogNum;
    private Button btnComfirm;
    private Button btnCancel;
    private AlertDialog.Builder builder;
    private KeyboardView keyboardView;
    private final KeyboarBuilder keyboarBuilder;
    private Dialog dialog;

    public MyDialog(Activity activity){
        mHostActivity = activity;
        View inflate = mHostActivity.getLayoutInflater().inflate(R.layout.dialog, null);
        txtDialog = inflate.findViewById(R.id.txtDialog);
        btnComfirm = inflate.findViewById(R.id.btn_dialog_OK);
        btnCancel = inflate.findViewById(R.id.btn_dialog_cancel);
        keyboardView = inflate.findViewById(R.id.keyboardview);
        etDialogNum = inflate.findViewById(R.id.et_dialog_num);
        builder = new AlertDialog.Builder(mHostActivity);
        builder.setView(inflate);
        keyboarBuilder = new KeyboarBuilder(mHostActivity, keyboardView, R.xml.hexkbd, etDialogNum);
    }

    public MyDialog setText(String text){
        txtDialog.setText(text);
        return this;
    }

    public MyDialog setComfirmListener(final View.OnClickListener comfirmListener) {
        btnComfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(comfirmListener != null) {
                    comfirmListener.onClick(etDialogNum);
                }
                if(dialog != null){
                    dialog.dismiss();
                }
                keyboarBuilder.hideCustomKeyboard();
            }
        });
        return this;
    }

    public MyDialog setCancelListener(final View.OnClickListener cancelListener){
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(cancelListener != null) {
                    cancelListener.onClick(etDialogNum);
                }
                if(dialog != null){
                    dialog.dismiss();
                }
                keyboarBuilder.hideCustomKeyboard();
            }
        });
        return this;
    }

    public void show(){
        dialog = builder.show();
        dialog.getWindow().setGravity(Gravity.TOP | Gravity.CENTER);
        dialog.setCanceledOnTouchOutside(false);

    }
}
