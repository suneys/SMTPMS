package com.yoyo.smtpms;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.yoyo.smtpms.util.SPUtil;

public class LoginActivity extends AppCompatActivity {

    EditText etName;
    EditText etIp;
    Button btnLogin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!SPUtil.getString(this,"name","").equals("")){
            Intent intent = new Intent(this,MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        etName = findViewById(R.id.et_name);
        etIp = findViewById(R.id.et_ip);
        btnLogin = findViewById(R.id.bt_login);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!etName.getText().toString().equals("") && !etIp.getText().toString().equals("")){
                    SPUtil.saveString(LoginActivity.this,"name",etName.getText().toString().trim());
                    SPUtil.saveString(LoginActivity.this,"severIP",etIp.getText().toString().trim());
                    Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                    startActivity(intent);
                    finish();
                }else {
                    Toast.makeText(LoginActivity.this,"用户名和服务器IP不能为空！",Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
